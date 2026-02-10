package com.omniresilience.retry;

import com.sun.net.httpserver.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Handles retry logic with configurable backoff strategy.
 */
public class RetryPolicy{

    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
    private final RetryConfig config;

    public RetryPolicy(RetryConfig config){
        this.config = config;
    }

    public <T> T execute(String name, Callable<T> callable) throws Exception {
        int attemptNumber = 0;
        Throwable lastException = null;

        // Do not go over max attempts.
        while(attemptNumber <= config.getMaxAttempts()){
            try {
                T result = callable.call();

                if (attemptNumber>0){
                    logger.info("Operation '{}' was successful after '{}' attempts",name,attemptNumber);
                }
                return result;
            }
            catch (Exception e){
                lastException = e;

                // Checking for retrying this exception.
                if(!config.shouldRetry(e)){
                    logger.debug("Operation '{}' failed with non-retryable exception'{}'",name,e.getClass().getSimpleName());
                    throw e;
                }

                // Check if there are attempts left.
                if(attemptNumber>=config.getMaxAttempts()){
                   logger.warn("Operation '{}' failed after '{}' attempts",name,attemptNumber+1);
                   throw new RetryExhaustedException(attemptNumber+1,e);
                }

                // Calculate backoff and delayMillis;
                long delayMillis = config.getBackoffStrategy().calculateDelayMillis(attemptNumber);
                logger.debug("Operation '{}' failed (attempt {}/{}),retrying in {}ms:{}",name,attemptNumber,config.getMaxAttempts(),delayMillis,e.getMessage());

                try {
                    Thread.sleep(delayMillis);
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(attemptNumber + 1, ie);
                }
                attemptNumber++;
            }
        }
        throw new RetryExhaustedException(attemptNumber,lastException);
    }
}