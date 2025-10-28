package com.spark.collectibles.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global error handler utility
 * 
 * This class provides centralized error handling and response formatting
 * for the API, ensuring consistent error responses across all endpoints.
 */
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    
    /**
     * Handle different types of exceptions and return appropriate error responses
     * @param exception The exception to handle
     * @param request The request that caused the error
     * @param response The response to modify
     * @return Error response object
     */
    public static ErrorResponse handleException(Exception exception, Object request, Object response) {
        logger.error("Handling exception: {}", exception.getClass().getSimpleName(), exception);
        
        if (exception instanceof IllegalArgumentException) {
            return handleValidationError(exception);
        } else if (exception instanceof SQLException) {
            return handleDatabaseError((SQLException) exception);
        } else if (exception instanceof SecurityException) {
            return handleSecurityError(exception);
        } else if (exception instanceof RuntimeException) {
            return handleRuntimeError((RuntimeException) exception);
        } else {
            return handleGenericError(exception);
        }
    }
    
    /**
     * Handle validation errors
     * @param exception The validation exception
     * @return Error response
     */
    private static ErrorResponse handleValidationError(Exception exception) {
        logger.warn("Validation error: {}", exception.getMessage());
        return new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid input data: " + exception.getMessage(),
            400,
            createErrorDetails("validation", exception.getMessage())
        );
    }
    
    /**
     * Handle database errors
     * @param exception The SQL exception
     * @return Error response
     */
    private static ErrorResponse handleDatabaseError(SQLException exception) {
        logger.error("Database error: {}", exception.getMessage(), exception);
        
        String errorCode = "DATABASE_ERROR";
        String message = "A database error occurred";
        int statusCode = 500;
        
        // Handle specific SQL error codes
        switch (exception.getSQLState()) {
            case "23505": // Unique constraint violation
                errorCode = "DUPLICATE_ENTRY";
                message = "A record with this information already exists";
                statusCode = 409;
                break;
            case "23503": // Foreign key constraint violation
                errorCode = "FOREIGN_KEY_CONSTRAINT";
                message = "Cannot perform this operation due to related records";
                statusCode = 400;
                break;
            case "23502": // Not null constraint violation
                errorCode = "MISSING_REQUIRED_FIELD";
                message = "Required field is missing";
                statusCode = 400;
                break;
            case "08006": // Connection failure
                errorCode = "DATABASE_CONNECTION_ERROR";
                message = "Unable to connect to database";
                statusCode = 503;
                break;
            case "42P01": // Table does not exist
                errorCode = "DATABASE_SCHEMA_ERROR";
                message = "Database schema error";
                statusCode = 500;
                break;
        }
        
        return new ErrorResponse(
            errorCode,
            message,
            statusCode,
            createErrorDetails("database", exception.getMessage())
        );
    }
    
    /**
     * Handle security errors
     * @param exception The security exception
     * @return Error response
     */
    private static ErrorResponse handleSecurityError(Exception exception) {
        logger.warn("Security error: {}", exception.getMessage());
        return new ErrorResponse(
            "SECURITY_ERROR",
            "Access denied or security violation",
            403,
            createErrorDetails("security", exception.getMessage())
        );
    }
    
    /**
     * Handle runtime errors
     * @param exception The runtime exception
     * @return Error response
     */
    private static ErrorResponse handleRuntimeError(RuntimeException exception) {
        logger.error("Runtime error: {}", exception.getMessage(), exception);
        
        String errorCode = "RUNTIME_ERROR";
        String message = "An unexpected error occurred";
        int statusCode = 500;
        
        // Handle specific runtime exceptions
        if (exception.getMessage() != null) {
            if (exception.getMessage().contains("SQL injection")) {
                errorCode = "SECURITY_VIOLATION";
                message = "Invalid input detected";
                statusCode = 400;
            } else if (exception.getMessage().contains("validation")) {
                errorCode = "VALIDATION_ERROR";
                message = "Input validation failed";
                statusCode = 400;
            } else if (exception.getMessage().contains("not found")) {
                errorCode = "NOT_FOUND";
                message = "Requested resource not found";
                statusCode = 404;
            }
        }
        
        return new ErrorResponse(
            errorCode,
            message,
            statusCode,
            createErrorDetails("runtime", exception.getMessage())
        );
    }
    
    /**
     * Handle generic errors
     * @param exception The generic exception
     * @return Error response
     */
    private static ErrorResponse handleGenericError(Exception exception) {
        logger.error("Generic error: {}", exception.getMessage(), exception);
        return new ErrorResponse(
            "INTERNAL_ERROR",
            "An internal server error occurred",
            500,
            createErrorDetails("generic", exception.getMessage())
        );
    }
    
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
     * Create a simple error response for basic errors
     * @param message Error message
     * @return Simple error response
     */
    public static ErrorResponse createSimpleError(String message) {
        return new ErrorResponse(
            "ERROR",
            message,
            400,
            createErrorDetails("simple", message)
        );
    }
    
    /**
     * Create a not found error response
     * @param resource Resource that was not found
     * @return Not found error response
     */
    public static ErrorResponse createNotFoundError(String resource) {
        return new ErrorResponse(
            "NOT_FOUND",
            resource + " not found",
            404,
            createErrorDetails("not_found", resource + " not found")
        );
    }
    
    /**
     * Create a validation error response
     * @param field Field that failed validation
     * @param reason Reason for validation failure
     * @return Validation error response
     */
    public static ErrorResponse createValidationError(String field, String reason) {
        return new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid " + field + ": " + reason,
            400,
            createErrorDetails("validation", field + " validation failed: " + reason)
        );
    }
    
    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        private int statusCode;
        private Map<String, Object> details;
        private String timestamp;
        
        public ErrorResponse(String code, String message, int statusCode, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.statusCode = statusCode;
            this.details = details;
            this.timestamp = java.time.Instant.now().toString();
        }
        
        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public int getStatusCode() { return statusCode; }
        public Map<String, Object> getDetails() { return details; }
        public String getTimestamp() { return timestamp; }
        
        // Setters
        public void setCode(String code) { this.code = code; }
        public void setMessage(String message) { this.message = message; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}


