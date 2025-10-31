package com.spark.collectibles.exception;

import com.spark.collectibles.util.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler module for Collectibles Store
 * 
 * This module provides centralized exception handling for the application,
 * integrating with the existing ErrorHandler utility and custom exceptions.
 */
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    
    /**
     * Create error details map
     * @param type Error type
     * @param message Error message
     * @return Error details map
     */
    private static Map<String, Object> createErrorDetails(String type, String message) {
        Map<String, Object> details = new HashMap<>();
        details.put("type", type);
        details.put("message", message);
        details.put("timestamp", java.time.Instant.now().toString());
        return details;
    }
    
    /**
     * Handle application-specific exceptions
     * @param exception The exception to handle
     * @param request The request that caused the error (can be null)
     * @param response The response object (can be null)
     * @return ErrorResponse with appropriate status code and message
     */
    public static ErrorHandler.ErrorResponse handle(CollectiblesException exception, 
                                                    Object request, 
                                                    Object response) {
        logger.error("Handling CollectiblesException: {} - {}", 
                    exception.getErrorCode(), exception.getMessage());
        
        return new ErrorHandler.ErrorResponse(
            exception.getErrorCode(),
            exception.getMessage(),
            exception.getStatusCode(),
            createErrorDetails(exception.getErrorCode(), exception.getMessage())
        );
    }
    
    /**
     * Handle any exception by converting it to ErrorResponse
     * @param exception The exception to handle
     * @param request The request that caused the error
     * @param response The response object
     * @return ErrorResponse with appropriate status code and message
     */
    public static ErrorHandler.ErrorResponse handleGeneric(Exception exception, 
                                                           Object request, 
                                                           Object response) {
        // If it's already a CollectiblesException, use specific handler
        if (exception instanceof CollectiblesException) {
            return handle((CollectiblesException) exception, request, response);
        }
        
        // Otherwise, delegate to existing ErrorHandler
        return ErrorHandler.handleException(exception, request, response);
    }
    
    /**
     * Wrap and rethrow as CollectiblesException if needed
     * @param exception The exception to wrap
     * @param context Context information for the error
     * @return CollectiblesException
     */
    public static CollectiblesException wrapException(Exception exception, String context) {
        if (exception instanceof CollectiblesException) {
            return (CollectiblesException) exception;
        }
        
        logger.error("Wrapping exception with context: {}", context, exception);
        return new DatabaseException("Error in " + context + ": " + exception.getMessage(), exception);
    }
}

