package com.spark.collectibles.repository;

import com.spark.collectibles.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * 
 * This interface defines the contract for product data access operations
 * following the Repository pattern and SOLID principles.
 */
public interface ProductRepository {
    
    /**
     * Find all products (excluding soft-deleted)
     * @return List of all active products
     */
    List<Product> findAll();
    
    /**
     * Find all products including soft-deleted
     * @return List of all products including deleted ones
     */
    List<Product> findAllIncludingDeleted();
    
    /**
     * Find product by ID
     * @param id Product ID
     * @return Optional containing product if found, empty otherwise
     */
    Optional<Product> findById(String id);
    
    /**
     * Find product by ID including soft-deleted
     * @param id Product ID
     * @return Optional containing product if found (including deleted), empty otherwise
     */
    Optional<Product> findByIdIncludingDeleted(String id);
    
    /**
     * Save product (create or update)
     * @param product Product to save
     * @return Saved product
     */
    Product save(Product product);
    
    /**
     * Update product
     * @param product Product to update
     * @return Updated product
     */
    Product update(Product product);
    
    /**
     * Soft delete product by ID
     * @param id Product ID
     * @return true if deleted, false if not found
     */
    boolean softDeleteById(String id);
    
    /**
     * Hard delete product by ID
     * @param id Product ID
     * @return true if deleted, false if not found
     */
    boolean hardDeleteById(String id);
    
    /**
     * Restore soft-deleted product
     * @param id Product ID
     * @return true if restored, false if not found or not deleted
     */
    boolean restoreById(String id);
    
    /**
     * Find products by category
     * @param category Product category
     * @return List of products in the category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products by price range
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of products in the price range
     */
    List<Product> findByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    
    /**
     * Find products with price greater than or equal to minPrice
     * @param minPrice Minimum price
     * @return List of products with price >= minPrice
     */
    List<Product> findByMinPrice(java.math.BigDecimal minPrice);
    
    /**
     * Find products with price less than or equal to maxPrice
     * @param maxPrice Maximum price
     * @return List of products with price <= maxPrice
     */
    List<Product> findByMaxPrice(java.math.BigDecimal maxPrice);
    
    /**
     * Search products by name or description
     * @param query Search query
     * @return List of matching products
     */
    List<Product> search(String query);
    
    /**
     * Find active products only
     * @return List of active products
     */
    List<Product> findActive();
    
    /**
     * Count total products
     * @return Total number of products
     */
    long count();
    
    /**
     * Count active products
     * @return Number of active products
     */
    long countActive();
    
    /**
     * Check if product exists
     * @param id Product ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
    
    /**
     * Check if product exists including soft-deleted
     * @param id Product ID
     * @return true if exists (including deleted), false otherwise
     */
    boolean existsByIdIncludingDeleted(String id);
}
