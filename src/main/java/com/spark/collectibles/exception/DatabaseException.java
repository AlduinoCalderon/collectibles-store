package com.spark.collectibles.exception;

/**
 * Exception thrown when database operations fail
 */
public class DatabaseException extends CollectiblesException {
    public DatabaseException(String message) {
        super("DATABASE_ERROR", message, 500);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super("DATABASE_ERROR", message, 500, cause);
    }
}

