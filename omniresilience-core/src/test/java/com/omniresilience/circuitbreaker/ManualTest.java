package com.omniresilience.circuitbreaker;

import com.omniresilience.circuitbreaker.CircuitBreaker;
import com.omniresilience.circuitbreaker.CircuitBreakerConfig;
import com.omniresilience.exception.CircuitBreakerOpenException;
import com.omniresilience.metrics.MetricsSnapshot;
import com.omniresilience.retry.RetryConfig;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manual test to demonstrate circuit breaker behavior.
 * Run this main method to see the circuit breaker in action.
 */
public class ManualTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Circuit Breaker Manual Test ===\n");

        // Create circuit breaker with low thresholds for easy testing
        CircuitBreakerConfig config = CircuitBreakerConfig.builder()
                .failureThreshold(3)  // Open after 3 failures
                .timeout(Duration.ofSeconds(5))  // Stay open for 5 seconds
                .halfOpenThreshold(2)  // Allow 2 test requests in half-open
                .retryConfig(RetryConfig.builder()
                        .maxAttempts(2)  // Retry once
                        .build())
                .build();

        CircuitBreaker circuitBreaker = new CircuitBreaker("test-service", config);

        // Simulated service that fails initially
        FlakyService service = new FlakyService();

        // Phase 1: Demonstrate CLOSED → OPEN transition
        System.out.println("Phase 1: Causing failures to open circuit\n");

        for (int i = 1; i <= 5; i++) {
            try {
                String result = circuitBreaker.execute(() -> service.call());
                System.out.println("Request " + i + ": SUCCESS - " + result);
            } catch (CircuitBreakerOpenException e) {
                System.out.println("Request " + i + ": REJECTED - Circuit is OPEN");
            } catch (Exception e) {
                System.out.println("Request " + i + ": FAILED - " + e.getMessage());
            }

            printMetrics(circuitBreaker);
            Thread.sleep(500);
        }

        // Phase 2: Wait for timeout and test HALF_OPEN
        System.out.println("\nPhase 2: Waiting for timeout (5 seconds)...\n");
        Thread.sleep(5000);

        // Service starts recovering
        service.startRecovering();

        System.out.println("Testing circuit in HALF_OPEN state:\n");

        for (int i = 6; i <= 8; i++) {
            try {
                String result = circuitBreaker.execute(() -> service.call());
                System.out.println("Request " + i + ": SUCCESS - " + result);
            } catch (CircuitBreakerOpenException e) {
                System.out.println("Request " + i + ": REJECTED - Circuit is OPEN");
            } catch (Exception e) {
                System.out.println("Request " + i + ": FAILED - " + e.getMessage());
            }

            printMetrics(circuitBreaker);
            Thread.sleep(500);
        }

        System.out.println("\n=== Test Complete ===");
    }

    private static void printMetrics(CircuitBreaker cb) {
        MetricsSnapshot metrics = cb.getMetrics();
        System.out.printf("  Metrics: %s, Consecutive Failures: %d, Success: %d, Failed: %d, Rejected: %d\n",
                metrics.getState(),
                metrics.getConsecutiveFailures(),
                metrics.getSuccessfulCalls(),
                metrics.getFailedCalls(),
                metrics.getRejectedCalls());
    }

    /**
     * Simulated flaky service for testing.
     */
    static class FlakyService {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private volatile boolean recovering = false;

        public String call() throws Exception {
            int count = callCount.incrementAndGet();

            if (!recovering) {
                throw new RuntimeException("Service is down!");
            }

            return "Success on call " + count;
        }

        public void startRecovering() {
            this.recovering = true;
        }
    }
}