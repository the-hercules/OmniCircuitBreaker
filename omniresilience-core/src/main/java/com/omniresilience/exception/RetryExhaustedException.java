package com.omniresilience.exception;

/**
 * Exception thrown when all retry attempts are exhausted
 */
public class RetryExhaustedException extends ResilienceException {
    private final int attempts;
    private final Throwable lastCause;

    public RetryExhaustedException(int attempts, Throwable lastCause) {
        super(String.format("Retry exhausted after %d attempt(s)", attempts), lastCause);
        this.attempts = attempts;
        this.lastCause = lastCause;
    }

    public int getAttempts() {
        return attempts;
    }

    public Throwable getLastCause() {
        return lastCause;
    }
}

