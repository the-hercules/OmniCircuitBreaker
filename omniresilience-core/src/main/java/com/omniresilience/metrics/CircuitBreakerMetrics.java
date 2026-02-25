package com.omniresilience.metrics;

import com.omniresilience.circuitbreaker.State;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thead safe Metrics of Circuit Breaker.
 */
public class CircuitBreakerMetrics{


    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);
    private final AtomicLong rejectedCalls = new AtomicLong(0);

    private volatile Instant lastStateTransition = Instant.now();

    public void recordSuccess(){
        successfulCalls.incrementAndGet();
    }

    public void recordFailure(){
        failedCalls.incrementAndGet();
    }

    public void recordRejection(){
        rejectedCalls.incrementAndGet();
    }

    public void recordStateTransition(){
        lastStateTransition = Instant.now();
    }

    public MetricsSnapshot snapshot(State currentState, int failureCount){
        return new MetricsSnapshot(
                currentState,
                failureCount,
                successfulCalls.get(),
                failedCalls.get(),
                rejectedCalls.get(),
                lastStateTransition
        );
    }

    /**
     * Reset all counters to zero.
     * Does NOT affect circuit breaker state or consecutive failures count.
     */
    public void reset(){
        failedCalls.set(0);
        successfulCalls.set(0);
        rejectedCalls.set(0);
        lastStateTransition = Instant.now();
    }
}