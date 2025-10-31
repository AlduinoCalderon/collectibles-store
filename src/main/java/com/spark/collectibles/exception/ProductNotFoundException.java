package com.spark.collectibles.exception;

/**
 * Exception thrown when a product is not found
 */
public class ProductNotFoundException extends CollectiblesException {
    public ProductNotFoundException(String productId) {
        super("PRODUCT_NOT_FOUND", 
              "Product with ID '" + productId + "' not found", 
              404);
    }
    
    public ProductNotFoundException(String productId, Throwable cause) {
        super("PRODUCT_NOT_FOUND", 
              "Product with ID '" + productId + "' not found", 
              404, 
              cause);
    }
}

