package com.omniresilience.circuitbreaker;

/**
 * Enum representing the state of a Circuit Breaker
 */
public enum State {
    /**
     * Circuit Closed, requests flow normally.
     * Transitions to OPEN if failure threshold is reached.
     */
    CLOSED,

    /**
     * Circuit Open, requests are rejected immediately.
     * Transitions to HALF_OPEN after timeout.
     */
    OPEN,

    /**
     * It is a testing phase to check if the service is recovered.
     * Allows few requests through to check the service.
     * Transitions to CLOSED if Success threshold is reached.
     */
    HALF_OPEN
}