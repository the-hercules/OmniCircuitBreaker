package com.omniresilience.exception;

/**
 * Thrown when a request is rejected because the circuit breaker is OPEN.
 */
public class CircuitBreakerOpenException extends ResilienceException {

    private final String circuitBreakerName;

    public CircuitBreakerOpenException(String circuitBreakerName) {
        super(String.format("Circuit breaker '%s' is OPEN and not accepting requests", circuitBreakerName));
        this.circuitBreakerName = circuitBreakerName;
    }

    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }
}