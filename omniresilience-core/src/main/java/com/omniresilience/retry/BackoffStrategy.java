package com.omniresilience.retry;

/**
 * Strategy for calculating backoff delays between retry attempts.
 */
public interface BackoffStrategy {

    /**
     * todo
     * Calculate the delay before the next retry attempt.
     *
     * @param attemptNumber The attempt number (0-indexed, so first retry is 0)
     * @return Delay in milliseconds
     */
    long calculateDelayMillis(int attemptNumber);
}