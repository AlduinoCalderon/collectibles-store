package com.spark.collectibles.exception;

/**
 * Exception thrown when attempting to create a duplicate product
 */
public class DuplicateProductException extends CollectiblesException {
    public DuplicateProductException(String productId) {
        super("DUPLICATE_PRODUCT", 
              "Product with ID '" + productId + "' already exists", 
              409);
    }
    
    public DuplicateProductException(String productId, String message) {
        super("DUPLICATE_PRODUCT", message, 409);
    }
}

