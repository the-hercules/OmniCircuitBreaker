package com.omniresilience.circuitbreaker;

import com.omniresilience.exception.CircuitBreakerOpenException;
import com.omniresilience.metrics.CircuitBreakerMetrics;
import com.omniresilience.metrics.MetricsSnapshot;
import com.omniresilience.retry.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe circuit breaker implementation.
 * <p>
 * Implements the circuit breaker pattern with three states:
 * - CLOSED: Requests flow normally, failures are counted
 * - OPEN: Requests are rejected immediately (fast-fail)
 * - HALF_OPEN: Limited requests allowed to test if service recovered
 */
public class CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private final String name;
    private final CircuitBreakerConfig circuitBreakerConfig;

    // State transitioning variables (thread-safe)
    private volatile State state = State.CLOSED;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong openedAt = new AtomicLong(0);
    private final AtomicLong halfOpenCalls = new AtomicLong(0);

    // Metrics
    private final CircuitBreakerMetrics metrics = new CircuitBreakerMetrics();

    // Retry policy
    private final RetryPolicy retryPolicy;

    /**
     * Create a new CircuitBreaker.
     */
    public CircuitBreaker(String name, CircuitBreakerConfig circuitBreakerConfig) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Circuit Breaker name cannot be null or empty");
        }
        if (circuitBreakerConfig == null) {
            throw new IllegalArgumentException("Circuit Breaker circuitBreakerConfig cannot be null");
        }
        this.name = name;
        this.circuitBreakerConfig = circuitBreakerConfig;
        this.retryPolicy = new RetryPolicy(circuitBreakerConfig.getRetryConfig());

        logger.info("Circuit breaker '{}' created with circuitBreakerConfig: failureThreshold={}, timeout={}, halfOpenMaxCalls={}", name, circuitBreakerConfig.getFailureThreshold(), circuitBreakerConfig.getTimeout(), circuitBreakerConfig.getHalfOpenThreshold());
    }

    /**
     * Execute an operation protected by this circuit breaker.
     */
    public <T> T execute(Callable<T> operation) throws Exception {
        if (!allowRequest()) {
            metrics.recordRejection();
            throw new CircuitBreakerOpenException(name);
        }

        // Track HALF_OPEN calls.
        if (state == State.HALF_OPEN) {
            halfOpenCalls.incrementAndGet();
        }

        try {
            T result = retryPolicy.execute(name, operation);
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
        /**
         * Determine if a request should be allowed through.
         */
        private boolean allowRequest () {
            State currentState = state;

            switch (currentState) {
                case CLOSED:
                    return true;

                case OPEN:
                    // Check if timeout has expired
                    if (hasTimeoutExpired()) {
                        transitionToHalfOpen();
                        return true;
                    }
                    return false;

                case HALF_OPEN:
                    // Allow limited number of test requests
                    return halfOpenCalls.get() < circuitBreakerConfig.getHalfOpenThreshold();

                default:
                    return false;
            }
        }
        /**
         * Check if the timeout period has expired while in OPEN state.
         */
        private boolean hasTimeoutExpired () {
            long opened = openedAt.get();
            if (opened == 0) {
                return false;
            }

            long now = System.currentTimeMillis();
            long elapsed = now - opened;
            return elapsed >= circuitBreakerConfig.getTimeoutMillis();
        }

        /**
         * Handle successful operation.
         */
        private void onSuccess () {
            consecutiveFailures.set(0);
            metrics.recordSuccess();

            State currentState = state;

            if (currentState == State.HALF_OPEN) {
                // Success in HALF_OPEN → transition to CLOSED
                transitionToClosed();
            }
        }

        /**
         * Handle failed operation (after all retries exhausted).
         */
        private void onFailure () {
            int failures = consecutiveFailures.incrementAndGet();
            metrics.recordFailure();

            State currentState = state;

            if (currentState == State.CLOSED && failures >= circuitBreakerConfig.getFailureThreshold()) {
                // Threshold reached → open circuit
                transitionToOpen("Failure threshold reached: " + failures);

            } else if (currentState == State.HALF_OPEN) {
                // Any failure in HALF_OPEN → back to OPEN
                transitionToOpen("Failure during half-open test");
            }
        }

        /**
         * Transition to OPEN state.
         */
        private void transitionToOpen (String reason){
            State previousState = state;
            state = State.OPEN;
            openedAt.set(System.currentTimeMillis());
            halfOpenCalls.set(0);
            metrics.recordStateTransition();

            StateTransition.transition(name, previousState, State.OPEN, reason);
        }

        /**
         * Transition to HALF_OPEN state.
         */
        private void transitionToHalfOpen () {
            State previousState = state;
            state = State.HALF_OPEN;
            halfOpenCalls.set(0);
            metrics.recordStateTransition();

            StateTransition.transition(name, previousState, State.HALF_OPEN, "Timeout expired");
        }

        /**
         * Transition to CLOSED state.
         */
        private void transitionToClosed () {
            State previousState = state;
            state = State.CLOSED;
            consecutiveFailures.set(0);
            openedAt.set(0);
            halfOpenCalls.set(0);
            metrics.recordStateTransition();

            StateTransition.transition(name, previousState, State.CLOSED, "Service recovered");
        }

        /**
         * Get current state of the circuit breaker.
         */
        public State getState () {
            return state;
        }

        /**
         * Get current metrics snapshot.
         */
        public MetricsSnapshot getMetrics () {
            return metrics.snapshot(state, consecutiveFailures.get());
        }

        /**
         * Reset metrics (does not affect circuit breaker state).
         */
        public void resetMetrics () {
            metrics.reset();
            logger.debug("Circuit breaker '{}' metrics reset", name);
        }

        /**
         * Force circuit breaker to CLOSED state and reset all counters.
         * Use with caution - typically for testing or after manual intervention.
         */
        public void reset () {
            State previousState = state;
            state = State.CLOSED;
            consecutiveFailures.set(0);
            openedAt.set(0);
            halfOpenCalls.set(0);
            metrics.reset();

            logger.warn("Circuit breaker '{}' manually reset from {} to CLOSED", name, previousState);
        }

        /**
         * Force circuit breaker to OPEN state.
         * Useful for maintenance or emergency situations.
         */
        public void forceOpen () {
            transitionToOpen("Manually forced open");
        }

        /**
         * Get the name of this circuit breaker.
         */
        public String getName () {
            return name;
        }

        /**
         * Get the configuration.
         */
        public CircuitBreakerConfig getConfig () {
            return circuitBreakerConfig;
        }

    }