package com.spark.collectibles.service;

import com.spark.collectibles.model.Product;
import com.spark.collectibles.repository.ProductRepository;
import com.spark.collectibles.repository.impl.PostgreSQLProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class for managing product operations
 * 
 * This class provides business logic for product CRUD operations
 * following SOLID principles and best practices.
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    
    public ProductService() {
        this.productRepository = new PostgreSQLProductRepository();
    }
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Get all active products
     * @return List of all active products
     */
    public List<Product> getAllProducts() {
        logger.info("Retrieving all products");
        return productRepository.findAll();
    }
    
    /**
     * Get all products including soft-deleted
     * @return List of all products including deleted ones
     */
    public List<Product> getAllProductsIncludingDeleted() {
        logger.info("Retrieving all products including deleted");
        return productRepository.findAllIncludingDeleted();
    }
    
    /**
     * Get product by ID
     * @param id Product ID
     * @return Product if found, null otherwise
     */
    public Product getProductById(String id) {
        logger.info("Retrieving product with ID: {}", id);
        return productRepository.findById(id).orElse(null);
    }
    
    /**
     * Get product by ID including soft-deleted
     * @param id Product ID
     * @return Product if found (including deleted), null otherwise
     */
    public Product getProductByIdIncludingDeleted(String id) {
        logger.info("Retrieving product with ID (including deleted): {}", id);
        return productRepository.findByIdIncludingDeleted(id).orElse(null);
    }
    
    /**
     * Check if product exists
     * @param id Product ID
     * @return true if exists, false otherwise
     */
    public boolean productExists(String id) {
        logger.info("Checking if product exists with ID: {}", id);
        return productRepository.existsById(id);
    }
    
    /**
     * Create a new product
     * @param product Product to create
     * @return Created product if successful, null if product already exists or invalid
     */
    public Product createProduct(Product product) {
        if (product == null) {
            logger.warn("Null product provided for creation");
            return null;
        }
        
        // Generate ID automatically if not provided or empty
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            String generatedId = generateProductId();
            product.setId(generatedId);
            logger.info("Generated product ID: {}", generatedId);
        }
        
        // Validate product data (excluding ID check since we just generated it)
        if (!isProductDataValid(product)) {
            logger.warn("Invalid product data provided for creation");
            return null;
        }
        
        if (productRepository.existsById(product.getId())) {
            logger.warn("Product with ID {} already exists", product.getId());
            return null;
        }
        
        // Check if name already exists
        boolean nameExists = productRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(product.getName()));
        
        if (nameExists) {
            logger.warn("Product with name {} already exists", product.getName());
            return null;
        }
        
        product.touch();
        Product createdProduct = productRepository.save(product);
        if (createdProduct != null) {
            logger.info("Product created successfully with ID: {}", product.getId());
        }
        return createdProduct;
    }
    
    /**
     * Generate a unique product ID
     * @return Generated product ID
     */
    private String generateProductId() {
        // Get the highest numeric ID from existing products
        long maxId = 0;
        try {
            List<Product> allProducts = productRepository.findAllIncludingDeleted();
            for (Product p : allProducts) {
                String id = p.getId();
                if (id != null && id.startsWith("item")) {
                    try {
                        String numPart = id.substring(4); // Skip "item"
                        long num = Long.parseLong(numPart);
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore IDs that don't match the pattern
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error generating product ID, using timestamp fallback", e);
            return "item" + System.currentTimeMillis();
        }
        
        return "item" + (maxId + 1);
    }
    
    /**
     * Validate product data (excluding ID check)
     * @param product Product to validate
     * @return true if valid, false otherwise
     */
    private boolean isProductDataValid(Product product) {
        return product.getName() != null && !product.getName().trim().isEmpty() &&
               product.getDescription() != null && !product.getDescription().trim().isEmpty() &&
               product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
               product.getCurrency() != null && !product.getCurrency().trim().isEmpty();
    }
    
    /**
     * Update an existing product
     * @param id Product ID
     * @param product Updated product data
     * @return Updated product if successful, null if product not found or invalid
     */
    public Product updateProduct(String id, Product product) {
        if (product == null || !product.isValid()) {
            logger.warn("Invalid product data provided for update");
            return null;
        }
        
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            logger.warn("Product with ID {} not found for update", id);
            return null;
        }
        
        // Check if name conflicts with other products
        boolean nameConflict = productRepository.findAll().stream()
                .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(product.getName()));
        
        if (nameConflict) {
            logger.warn("Product name {} already exists for another product", product.getName());
            return null;
        }
        
        // Preserve creation timestamp and set ID
        product.setId(id);
        product.setCreatedAt(existingProduct.getCreatedAt());
        product.touch();
        
        Product updatedProduct = productRepository.update(product);
        if (updatedProduct != null) {
            logger.info("Product updated successfully with ID: {}", id);
        }
        return updatedProduct;
    }
    
    /**
     * Soft delete a product by ID
     * @param id Product ID
     * @return true if product was deleted, false if product not found
     */
    public boolean deleteProduct(String id) {
        boolean deleted = productRepository.softDeleteById(id);
        if (deleted) {
            logger.info("Product soft deleted successfully with ID: {}", id);
        } else {
            logger.warn("Product with ID {} not found for deletion", id);
        }
        return deleted;
    }
    
    /**
     * Hard delete a product by ID
     * @param id Product ID
     * @return true if product was deleted, false if product not found
     */
    public boolean hardDeleteProduct(String id) {
        boolean deleted = productRepository.hardDeleteById(id);
        if (deleted) {
            logger.info("Product hard deleted successfully with ID: {}", id);
        } else {
            logger.warn("Product with ID {} not found for hard deletion", id);
        }
        return deleted;
    }
    
    /**
     * Restore a soft-deleted product
     * @param id Product ID
     * @return true if product was restored, false if product not found or not deleted
     */
    public boolean restoreProduct(String id) {
        boolean restored = productRepository.restoreById(id);
        if (restored) {
            logger.info("Product restored successfully with ID: {}", id);
        } else {
            logger.warn("Product with ID {} not found for restoration or not deleted", id);
        }
        return restored;
    }
    
    /**
     * Search products by name or description
     * @param query Search query
     * @return List of matching products
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        
        logger.info("Searching products with query: {}", query);
        return productRepository.search(query.trim());
    }
    
    /**
     * Get products by category
     * @param category Product category
     * @return List of products in the category
     */
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllProducts();
        }
        
        logger.info("Getting products by category: {}", category);
        return productRepository.findByCategory(category.trim());
    }
    
    /**
     * Get products by price range
     * Supports filtering with only min (>=), only max (<=), or both (between)
     * @param minPrice Minimum price (can be null if only max is provided)
     * @param maxPrice Maximum price (can be null if only min is provided)
     * @return List of products in the price range
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        // If both are null, return all products
        if (minPrice == null && maxPrice == null) {
            return getAllProducts();
        }
        
        // If both are provided, validate range
        if (minPrice != null && maxPrice != null) {
            if (minPrice.compareTo(maxPrice) > 0) {
                logger.warn("Invalid price range provided: {} > {}", minPrice, maxPrice);
                return List.of();
            }
            logger.info("Getting products by price range: {} - {}", minPrice, maxPrice);
            return productRepository.findByPriceRange(minPrice, maxPrice);
        }
        
        // If only min is provided (>= minPrice)
        if (minPrice != null && maxPrice == null) {
            logger.info("Getting products with price >= {}", minPrice);
            return productRepository.findByMinPrice(minPrice);
        }
        
        // If only max is provided (<= maxPrice)
        if (minPrice == null && maxPrice != null) {
            logger.info("Getting products with price <= {}", maxPrice);
            return productRepository.findByMaxPrice(maxPrice);
        }
        
        return List.of();
    }
    
    /**
     * Get active products only
     * @return List of active products
     */
    public List<Product> getActiveProducts() {
        logger.info("Retrieving active products");
        return productRepository.findActive();
    }
    
    /**
     * Get product count
     * @return Total number of products
     */
    public long getProductCount() {
        return productRepository.count();
    }
    
    /**
     * Get active product count
     * @return Number of active products
     */
    public long getActiveProductCount() {
        return productRepository.countActive();
    }
    
    /**
     * Get product statistics
     * @return Product statistics
     */
    public ProductStats getProductStats() {
        long totalProducts = getProductCount();
        long activeProducts = getActiveProductCount();
        long deletedProducts = totalProducts - activeProducts;
        
        return new ProductStats(totalProducts, activeProducts, deletedProducts);
    }
    
    /**
     * Product statistics class
     */
    public static class ProductStats {
        private final long totalProducts;
        private final long activeProducts;
        private final long deletedProducts;
        
        public ProductStats(long totalProducts, long activeProducts, long deletedProducts) {
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.deletedProducts = deletedProducts;
        }
        
        public long getTotalProducts() {
            return totalProducts;
        }
        
        public long getActiveProducts() {
            return activeProducts;
        }
        
        public long getDeletedProducts() {
            return deletedProducts;
        }
    }
}
