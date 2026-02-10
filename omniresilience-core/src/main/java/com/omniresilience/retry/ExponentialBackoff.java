package com.omniresilience.retry;

/**
 * Exponential backoff strategy: delay = initialDelay * (multiplier ^ attempt);
 */
public class ExponentialBackoff implements BackoffStrategy{

    private final long initialDelayMillisecond;
    private final long maxDelayMillisecond;
    private final double multiplier;

    public ExponentialBackoff(long initialDelayMillisecond,long maxDelayMillisecond,double multiplier){
        if (initialDelayMillisecond<=0){
            throw new IllegalArgumentException("Initial Delay must be positive");
        }
        if (maxDelayMillisecond<initialDelayMillisecond){
            throw new IllegalArgumentException("Max delay must be >= initial delay");
        }
        if (multiplier < 1.0){
            throw new IllegalArgumentException("Multiplier must be >= 1.0");
        }

        this.initialDelayMillisecond = initialDelayMillisecond;
        this.maxDelayMillisecond = maxDelayMillisecond;
        this.multiplier = multiplier;
    }

    public ExponentialBackoff(){
        this(100,30000,2.0);
    }

    @Override
    public long calculateDelayMillis(int attemptNumber){
        long delay = (long) (initialDelayMillisecond*Math.pow(multiplier,attemptNumber));
        return Math.min(delay,maxDelayMillisecond);
    }

    public long getInitialDelayMillisecond(){
        return initialDelayMillisecond;
    }

    public double getMultiplier(){
        return multiplier;
    }

    public long getMaxDelayMillisecond() {
        return maxDelayMillisecond;
    }
}