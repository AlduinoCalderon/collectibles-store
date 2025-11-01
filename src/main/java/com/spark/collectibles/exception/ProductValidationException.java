package com.spark.collectibles.exception;

/**
 * Exception thrown when product validation fails
 */
public class ProductValidationException extends CollectiblesException {
    public ProductValidationException(String message) {
        super("PRODUCT_VALIDATION_ERROR", message, 400);
    }
    
    public ProductValidationException(String field, String reason) {
        super("PRODUCT_VALIDATION_ERROR", 
              "Invalid " + field + ": " + reason, 
              400);
    }
    
    public ProductValidationException(String message, Throwable cause) {
        super("PRODUCT_VALIDATION_ERROR", message, 400, cause);
    }
}

