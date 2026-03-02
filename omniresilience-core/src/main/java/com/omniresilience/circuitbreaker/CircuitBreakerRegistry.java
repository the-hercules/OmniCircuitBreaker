package com.omniresilience.circuitbreaker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing multiple circuit breakers.
 * Follows Singleton Pattern per configuration.
 */
public class CircuitBreakerRegistry{
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerRegistry.class);
    private final Map<String,CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private CircuitBreakerConfig defaultConfig;


    /**
     * Create a registry with default config.
     */
    public CircuitBreakerRegistry() {
        this(CircuitBreakerConfig.builder().build());
    }

    /**
     * Circuit Breaker Registry with custom configuration.
     */
    public CircuitBreakerRegistry(CircuitBreakerConfig defaultConfig){
        this.defaultConfig = defaultConfig;
        logger.info("Circuit breaker registry created with default config : {}",defaultConfig);
    }

    /**
     * Get or create a circuit breaker with the given name using default configuration if circuit breaker doesn't exist.
     */
    public CircuitBreaker circuitBreaker(String name){
        return circuitBreakers.computeIfAbsent(name,n->{
            logger.info("Creating new circuit breaker : {}",n);
            return new CircuitBreaker(n,defaultConfig);
        });
    }

    /**
     * Check if a circuit breaker with the given name exists.
     */
    public boolean contains(String name) {
        return circuitBreakers.containsKey(name);
    }

    /**
     * Remove a circuit breaker from the registry.
     *
     * @param name Circuit breaker name
     * @return The removed circuit breaker, or null if it didn't exist
     */
    public CircuitBreaker remove(String name) {
        CircuitBreaker removed = circuitBreakers.remove(name);
        if (removed != null) {
            logger.info("Circuit breaker '{}' removed from registry", name);
        }
        return removed;
    }

    /**
     * Get all registered circuit breakers.
     */
    public Collection<CircuitBreaker> getAllCircuitBreakers() {
        return circuitBreakers.values();
    }

    /**
     * Get the number of registered circuit breakers.
     */
    public int size() {
        return circuitBreakers.size();
    }

    /**
     * Remove all circuit breakers from the registry.
     */
    public void clear() {
        int size = circuitBreakers.size();
        circuitBreakers.clear();
        logger.info("Cleared {} circuit breakers from registry", size);
    }

}