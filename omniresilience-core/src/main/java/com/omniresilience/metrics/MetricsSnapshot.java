package com.omniresilience.metrics;

import com.omniresilience.circuitbreaker.State;

import java.time.Instant;

/**
 * Immutable snapshot of circuit breaker matrics at a point in time.
 */
public class MetricsSnapshot{
    private final State state;
    private final int failureCount;
    private final long totalCalls;
    private final long successfulCalls;
    private final long failedCalls;
    private final long rejectedCalls;
    private final Instant lastStateTransition;

    public MetricsSnapshot(State state, int failureCount, long totalCalls,
                           long successfulCalls, long failedCalls, long rejectedCalls,
                           Instant lastStateTransition) {
        this.state = state;
        this.failureCount = failureCount;
        this.totalCalls = totalCalls;
        this.successfulCalls = successfulCalls;
        this.failedCalls = failedCalls;
        this.rejectedCalls = rejectedCalls;
        this.lastStateTransition = lastStateTransition;
    }

    public State getState(){
        return state;
    }

    public int getFailureCount(){
        return failureCount;
    }
    public long getTotalCalls() {
        return totalCalls;
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

    @Override
    public String toString(){
        return String.format(
                "MetricsSnapshot{state=%s, failures=%d, total=%d, success=%d, failed=%d, rejected=%d}",
                state, failureCount, totalCalls, successfulCalls, failedCalls, rejectedCalls
        );
    }

}