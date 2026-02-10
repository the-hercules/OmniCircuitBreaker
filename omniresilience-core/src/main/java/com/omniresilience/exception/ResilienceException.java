package com.omniresilience.exception;

/**
 * Base Exception for OmniResilience
 */

public class ResilienceException extends RuntimeException{
    public ResilienceException(String message){
        super(message);
    }

    public ResilienceException (String message, Throwable cause){
        super(message,cause);
    }
}