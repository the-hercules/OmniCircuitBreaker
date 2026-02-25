package com.omniresilience.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Represents a state transition event in the circuit breaker.
 * Used for logging and debugging state changes.
 */
public class StateTransition{
   private static final Logger logger = LoggerFactory.getLogger(StateTransition.class);

    private final String circuitBreakerName;
    private final String fromState;
    private final String toState;
    private final Instant timestamp;
    private final String reason;


    private StateTransition(String circuitBreakerName, String fromState, String toState, String reason){
        this.circuitBreakerName = circuitBreakerName;
        this.fromState = fromState;
        this.toState = toState;
        this.timestamp = Instant.now();
        this.reason = reason;
    }

    /**
     * Create and log state transition.
     */
    public static StateTransition transition(String circuitBreakerName, String fromState, String toState, String reason){
       StateTransition transition = new StateTransition(circuitBreakerName,fromState,toState,reason);
       transition.log();
       return transition;
    }

    private void log(){
        logger.info("Circuit breaker '{}' transitioned: {} -> {} (reason : {})", circuitBreakerName,fromState,toState,reason);
    }
    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }

    public String getFromState() {
        return fromState;
    }

    public String getToState() {
        return toState;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s) at %s", fromState, toState, reason, timestamp);
    }
}