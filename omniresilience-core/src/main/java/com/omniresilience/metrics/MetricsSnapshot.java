package com.omniresilience.metrics;

import com.omniresilience.circuitbreaker.State;

import java.time.Instant;

/**
 * Immutable snapshot of circuit breaker matrics at a point in time.
 */
public class MetricsSnapshot{
    private final State state;
    private final int consecutiveFailures;
    private final long successfulCalls;
    private final long failedCalls;
    private final long rejectedCalls;
    private final Instant lastStateTransition;

    public MetricsSnapshot(State state, int failureCount,
                           long successfulCalls, long failedCalls, long rejectedCalls,
                           Instant lastStateTransition) {
        this.state = state;
        this.consecutiveFailures = failureCount;
        this.successfulCalls = successfulCalls;
        this.failedCalls = failedCalls;
        this.rejectedCalls = rejectedCalls;
        this.lastStateTransition = lastStateTransition;
    }

    public State getState(){
        return state;
    }

    public int getConsecutiveFailures(){
        return consecutiveFailures;
    }

    public long getSuccessfulCalls() {
        return successfulCalls;
    }

    public long getFailedCalls() {
        return failedCalls;
    }

    public long getRejectedCalls() {
        return rejectedCalls;
    }

    public Instant getLastStateTransition() {
        return lastStateTransition;
    }
    // Derived metrics (computed on-demand)
    public long getTotalNumberOfCalls() {
        return successfulCalls + failedCalls + rejectedCalls;
    }

    public float getFailureRate() {
        long total = successfulCalls + failedCalls;
        if (total == 0) return 0.0f;
        return (float) failedCalls/ total;
    }
    @Override
    public String toString(){
        return String.format(
                "MetricsSnapshot{state=%s, failures=%d,  success=%d, failed=%d, rejected=%d}",
                state, consecutiveFailures, successfulCalls, failedCalls, rejectedCalls
        );
    }

}