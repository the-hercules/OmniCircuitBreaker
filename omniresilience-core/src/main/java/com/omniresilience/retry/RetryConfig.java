package com.omniresilience.retry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Configuration for retry behavior.
 */
public class RetryConfig {

    private final int maxAttempts;
    private final BackoffStrategy backoffStrategy;
    private final Set<Class<? extends Throwable>> retryableExceptions;
    private final Set<Class<? extends Throwable>> ignoreExceptions;
    private final Predicate<Throwable> retryPredicate;

    private RetryConfig (Builder builder){
        this.maxAttempts = builder.maxAttempts;
        this.backoffStrategy = builder.backoffStrategy;
        this.retryableExceptions = new HashSet<>(builder.retryableExceptions);
        this.ignoreExceptions = new HashSet<>(builder.ignoreExceptions);
        this.retryPredicate = builder.retryPredicate;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    public Set<Class<? extends Throwable>> getRetryableExceptions() {
        return new HashSet<>(retryableExceptions);
    }

    public Set<Class<? extends Throwable>> getIgnoreExceptions() {
        return new HashSet<>(ignoreExceptions);
    }

    /**
     * Checking if the incoming exception warrants a retry.
     */
    public boolean shouldRetry(Throwable throwable){
        // Check the ignore list
        for (Class <? extends Throwable> ignoreClass :ignoreExceptions){
            if (ignoreClass.isInstance(throwable)){
                return false;
            }
        }

        // Check if it is included in retryable exceptions.
        if(!retryableExceptions.isEmpty()){
            boolean isRetryable = false;
            for(Class<? extends Throwable> retryableClass : retryableExceptions){
                if(retryableClass.isInstance(throwable)){
                    isRetryable = true;
                    break;
                }
            }
            if(isRetryable){
                return true;
            }
        }
        // Apply customer predicate if provided.
        return retryPredicate == null || retryPredicate.test(throwable);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxAttempts = 3;
        private BackoffStrategy backoffStrategy = new ExponentialBackoff();
        private Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();
        private Set<Class<? extends Throwable>> ignoreExceptions = new HashSet<>();
        private Predicate<Throwable> retryPredicate;

        public Builder maxAttempts(int maxAttempts){
            if(maxAttempts<0){
                throw new IllegalArgumentException("Max attempts must be >=0");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }
        public Builder backoffStrategy(BackoffStrategy backoffStrategy) {
            this.backoffStrategy = backoffStrategy;
            return this;
        }

        public Builder retryOnException(Class<? extends Throwable> exceptionClass) {
            this.retryableExceptions.add(exceptionClass);
            return this;
        }

        public Builder ignoreException(Class<? extends Throwable> exceptionClass) {
            this.ignoreExceptions.add(exceptionClass);
            return this;
        }

        public Builder retryPredicate(Predicate<Throwable> predicate) {
            this.retryPredicate = predicate;
            return this;
        }
        public RetryConfig build(){
            return new RetryConfig(this);
        }
    }

}