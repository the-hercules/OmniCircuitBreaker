package com.omniresilience.circuitbreaker;

import com.omniresilience.retry.RetryConfig;

import java.time.Duration;

/**
 * Config of Circuit Breaker
 */
public class CircuitBreakerConfig {
    private final int failureThreshold;
    private final Duration timeout;
    private final int halfOpenThreshold;
    private final RetryConfig retryConfig;

    private CircuitBreakerConfig(Builder builder){
        this.failureThreshold = builder.failureThreshold;
        this.timeout = builder.timeout;
        this.halfOpenThreshold = builder.halfOpenThreshold;
        this.retryConfig = builder.retryConfig;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public long getTimeoutMillis() {
        return timeout.toMillis();
    }

    public int getHalfOpenThreshold() {
        return halfOpenThreshold;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private int failureThreshold = 5;
        private Duration timeout = Duration.ofSeconds(60);
        private int halfOpenThreshold = 2;
        private RetryConfig retryConfig = RetryConfig.Builder.build();

        public Builder failureThreshold(int failureThreshold){
            if(failureThreshold<=0){
                throw new IllegalArgumentException("Failure threshold must be > 0");
            }
            this.failureThreshold = failureThreshold;
            return this;
        }

        public Builder timeout (Duration timeout){
            if (timeout.isNegative() || timeout.isZero()){
                throw new IllegalArgumentException("Timeout must be positive");
            }
            this.timeout = timeout;
            return this;
        }

        public Builder halfOpenThreshold(int halfOpenThreshold){
            if(halfOpenThreshold<=0){
                throw new IllegalArgumentException("Half open threshold must be > 0");
            }
            this.halfOpenThreshold = halfOpenThreshold;
            return this;
        }

        public Builder retryConfig(RetryConfig retryConfig){
            this.retryConfig= retryConfig;
            return this;
        }

       public CircuitBreakerConfig build(){
            return new CircuitBreakerConfig(this);
       }
    }

}