package com.spark.collectibles.routes;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spark.collectibles.model.Product;
import com.spark.collectibles.service.ProductService;
import com.spark.collectibles.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static spark.Spark.*;

/**
 * Route definitions for product management API endpoints
 * 
 * This class defines all the RESTful routes for product CRUD operations
 * following REST principles and proper route grouping.
 */
public class ProductRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ProductRoutes.class);
    private static final Gson gson = new Gson();
    
    /**
     * Initialize all product-related routes
     * @param productService ProductService instance for business logic
     */
    public static void initialize(ProductService productService) {
        
        // Route grouping for better organization
        // All product routes are grouped under /api/v1/products
        
        // GET /api/v1/products — Retrieve the list of all products with name and price
        get("/api/v1/products", (request, response) -> {
            logger.info("GET /api/v1/products - Retrieving all products");
            try {
                return productService.getAllProducts();
            } catch (Exception e) {
                logger.error("Error retrieving products", e);
                response.status(500);
                return new ErrorResponse("Failed to retrieve products");
            }
        }, JsonUtil::toJson);
        
        // GET /api/v1/products/:id — Retrieve a product by the given ID with description
        get("/api/v1/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("GET /api/v1/products/{} - Retrieving product by ID", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            Product product = productService.getProductById(id);
            if (product == null) {
                response.status(404);
                return new ErrorResponse("Product not found");
            }
            
            return product;
        }, JsonUtil::toJson);
        
        // POST /api/v1/products — Create a new product
        post("/api/v1/products", (request, response) -> {
            logger.info("POST /api/v1/products - Creating new product");
            
            try {
                Product product = gson.fromJson(request.body(), Product.class);
                if (product == null) {
                    response.status(400);
                    return new ErrorResponse("Invalid product data");
                }
                
                Product createdProduct = productService.createProduct(product);
                if (createdProduct == null) {
                    response.status(409);
                    return new ErrorResponse("Product already exists or invalid data");
                }
                
                response.status(201);
                return createdProduct;
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("Error creating product", e);
                response.status(500);
                return new ErrorResponse("Failed to create product");
            }
        }, JsonUtil::toJson);
        
        // PUT /api/v1/products/:id — Update an existing product
        put("/api/v1/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("PUT /api/v1/products/{} - Updating product", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            try {
                Product product = gson.fromJson(request.body(), Product.class);
                if (product == null) {
                    response.status(400);
                    return new ErrorResponse("Invalid product data");
                }
                
                Product updatedProduct = productService.updateProduct(id, product);
                if (updatedProduct == null) {
                    response.status(404);
                    return new ErrorResponse("Product not found or invalid data");
                }
                
                return updatedProduct;
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("Error updating product", e);
                response.status(500);
                return new ErrorResponse("Failed to update product");
            }
        }, JsonUtil::toJson);
        
        // DELETE /api/v1/products/:id — Soft delete a product
        delete("/api/v1/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("DELETE /api/v1/products/{} - Soft deleting product", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            boolean deleted = productService.deleteProduct(id);
            if (!deleted) {
                response.status(404);
                return new ErrorResponse("Product not found");
            }
            
            response.status(200);
            return new StatusResponse("Product deleted successfully", true);
        }, JsonUtil::toJson);
        
        // OPTIONS /api/v1/products/:id — Check whether a product with the given ID exists
        options("/api/v1/products/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("OPTIONS /api/v1/products/{} - Checking if product exists", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            boolean exists = productService.productExists(id);
            response.status(exists ? 200 : 404);
            return new StatusResponse(exists ? "Product exists" : "Product not found", exists);
        }, JsonUtil::toJson);
        
        // Additional helpful endpoints for e-commerce functionality
        
        // GET /api/v1/products/search?q=query — Search products
        get("/api/v1/products/search", (request, response) -> {
            String query = request.queryParams("q");
            logger.info("GET /api/v1/products/search?q={} - Searching products", query);
            
            try {
                return productService.searchProducts(query);
            } catch (Exception e) {
                logger.error("Error searching products", e);
                response.status(500);
                return new ErrorResponse("Failed to search products");
            }
        }, JsonUtil::toJson);
        
        // GET /api/v1/products/category/:category — Get products by category
        get("/api/v1/products/category/:category", (request, response) -> {
            String category = request.params(":category");
            logger.info("GET /api/v1/products/category/{} - Getting products by category", category);
            
            try {
                return productService.getProductsByCategory(category);
            } catch (Exception e) {
                logger.error("Error getting products by category", e);
                response.status(500);
                return new ErrorResponse("Failed to get products by category");
            }
        }, JsonUtil::toJson);
        
        // GET /api/v1/products/price-range?min=minPrice&max=maxPrice — Get products by price range
        get("/api/v1/products/price-range", (request, response) -> {
            String minPriceStr = request.queryParams("min");
            String maxPriceStr = request.queryParams("max");
            logger.info("GET /api/v1/products/price-range?min={}&max={} - Getting products by price range", 
                       minPriceStr, maxPriceStr);
            
            try {
                if (minPriceStr == null || maxPriceStr == null) {
                    response.status(400);
                    return new ErrorResponse("Both min and max price parameters are required");
                }
                
                BigDecimal minPrice = new BigDecimal(minPriceStr);
                BigDecimal maxPrice = new BigDecimal(maxPriceStr);
                
                return productService.getProductsByPriceRange(minPrice, maxPrice);
            } catch (NumberFormatException e) {
                response.status(400);
                return new ErrorResponse("Invalid price format");
            } catch (Exception e) {
                logger.error("Error getting products by price range", e);
                response.status(500);
                return new ErrorResponse("Failed to get products by price range");
            }
        }, JsonUtil::toJson);
        
        // GET /api/v1/products/active — Get active products only
        get("/api/v1/products/active", (request, response) -> {
            logger.info("GET /api/v1/products/active - Getting active products");
            
            try {
                return productService.getActiveProducts();
            } catch (Exception e) {
                logger.error("Error getting active products", e);
                response.status(500);
                return new ErrorResponse("Failed to get active products");
            }
        }, JsonUtil::toJson);
        
        // GET /api/v1/products/stats — Get product statistics
        get("/api/v1/products/stats", (request, response) -> {
            logger.info("GET /api/v1/products/stats - Getting product statistics");
            
            try {
                return productService.getProductStats();
            } catch (Exception e) {
                logger.error("Error getting product statistics", e);
                response.status(500);
                return new ErrorResponse("Failed to get product statistics");
            }
        }, JsonUtil::toJson);
        
        // POST /api/v1/products/:id/restore — Restore soft-deleted product
        post("/api/v1/products/:id/restore", (request, response) -> {
            String id = request.params(":id");
            logger.info("POST /api/v1/products/{}/restore - Restoring product", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            boolean restored = productService.restoreProduct(id);
            if (!restored) {
                response.status(404);
                return new ErrorResponse("Product not found or not deleted");
            }
            
            response.status(200);
            return new StatusResponse("Product restored successfully", true);
        }, JsonUtil::toJson);
        
        // DELETE /api/v1/products/:id/hard — Hard delete a product (permanent)
        delete("/api/v1/products/:id/hard", (request, response) -> {
            String id = request.params(":id");
            logger.info("DELETE /api/v1/products/{}/hard - Hard deleting product", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("Product ID is required");
            }
            
            boolean deleted = productService.hardDeleteProduct(id);
            if (!deleted) {
                response.status(404);
                return new ErrorResponse("Product not found");
            }
            
            response.status(200);
            return new StatusResponse("Product permanently deleted", true);
        }, JsonUtil::toJson);
    }
    
    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Status response class
     */
    public static class StatusResponse {
        private String message;
        private boolean success;
        
        public StatusResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
}
