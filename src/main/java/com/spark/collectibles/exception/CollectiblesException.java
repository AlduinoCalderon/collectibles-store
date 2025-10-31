package com.spark.collectibles.exception;

/**
 * Base exception class for Collectibles Store application
 * 
 * All custom exceptions should extend this class to provide
 * consistent error handling across the application.
 */
public class CollectiblesException extends Exception {
    private final String errorCode;
    private final int statusCode;
    
    public CollectiblesException(String message) {
        super(message);
        this.errorCode = "COLLECTIBLES_ERROR";
        this.statusCode = 500;
    }
    
    public CollectiblesException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "COLLECTIBLES_ERROR";
        this.statusCode = 500;
    }
    
    public CollectiblesException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    public CollectiblesException(String errorCode, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}

