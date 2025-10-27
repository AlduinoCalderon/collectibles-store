package com.spark.collectibles.repository.impl;

import com.spark.collectibles.database.DatabaseConnectionManager;
import com.spark.collectibles.model.Product;
import com.spark.collectibles.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of ProductRepository
 * 
 * This class implements the ProductRepository interface using PostgreSQL
 * as the data store, following SOLID principles and best practices.
 */
public class PostgreSQLProductRepository implements ProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLProductRepository.class);
    private final DatabaseConnectionManager connectionManager;
    
    public PostgreSQLProductRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products WHERE is_deleted = false ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    @Override
    public List<Product> findAllIncludingDeleted() {
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    @Override
    public Optional<Product> findById(String id) {
        String sql = "SELECT * FROM products WHERE id = ? AND is_deleted = false";
        return executeQueryForSingle(sql, id);
    }
    
    @Override
    public Optional<Product> findByIdIncludingDeleted(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        return executeQueryForSingle(sql, id);
    }
    
    @Override
    public Product save(Product product) {
        if (existsById(product.getId())) {
            return update(product);
        } else {
            return create(product);
        }
    }
    
    @Override
    public Product update(Product product) {
        String sql = """
            UPDATE products 
            SET name = ?, description = ?, price = ?, currency = ?, 
                category = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_deleted = false
            """;
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setString(4, product.getCurrency());
            statement.setString(5, product.getCategory());
            statement.setBoolean(6, product.isActive());
            statement.setString(7, product.getId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product updated successfully: {}", product.getId());
                return product;
            } else {
                logger.warn("No product found to update with ID: {}", product.getId());
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getId(), e);
            throw new RuntimeException("Failed to update product", e);
        }
    }
    
    @Override
    public boolean softDeleteById(String id) {
        String sql = """
            UPDATE products 
            SET is_deleted = true, is_active = false, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_deleted = false
            """;
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Product soft deleted successfully: {}", id);
                return true;
            } else {
                logger.warn("No product found to soft delete with ID: {}", id);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Error soft deleting product: {}", id, e);
            throw new RuntimeException("Failed to soft delete product", e);
        }
    }
    
    @Override
    public boolean hardDeleteById(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Product hard deleted successfully: {}", id);
                return true;
            } else {
                logger.warn("No product found to hard delete with ID: {}", id);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Error hard deleting product: {}", id, e);
            throw new RuntimeException("Failed to hard delete product", e);
        }
    }
    
    @Override
    public boolean restoreById(String id) {
        String sql = """
            UPDATE products 
            SET is_deleted = false, is_active = true, deleted_at = NULL, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_deleted = true
            """;
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Product restored successfully: {}", id);
                return true;
            } else {
                logger.warn("No deleted product found to restore with ID: {}", id);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Error restoring product: {}", id, e);
            throw new RuntimeException("Failed to restore product", e);
        }
    }
    
    @Override
    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM products WHERE category = ? AND is_deleted = false ORDER BY created_at DESC";
        return executeQuery(sql, category);
    }
    
    @Override
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String sql = "SELECT * FROM products WHERE price >= ? AND price <= ? AND is_deleted = false ORDER BY price ASC";
        return executeQuery(sql, minPrice, maxPrice);
    }
    
    @Override
    public List<Product> search(String query) {
        String sql = """
            SELECT * FROM products 
            WHERE (name ILIKE ? OR description ILIKE ?) 
            AND is_deleted = false 
            ORDER BY created_at DESC
            """;
        String searchPattern = "%" + query + "%";
        return executeQuery(sql, searchPattern, searchPattern);
    }
    
    @Override
    public List<Product> findActive() {
        String sql = "SELECT * FROM products WHERE is_active = true AND is_deleted = false ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_deleted = false";
        return executeCountQuery(sql);
    }
    
    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = true AND is_deleted = false";
        return executeCountQuery(sql);
    }
    
    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM products WHERE id = ? AND is_deleted = false";
        return executeExistsQuery(sql, id);
    }
    
    @Override
    public boolean existsByIdIncludingDeleted(String id) {
        String sql = "SELECT 1 FROM products WHERE id = ?";
        return executeExistsQuery(sql, id);
    }
    
    /**
     * Create a new product
     */
    private Product create(Product product) {
        String sql = """
            INSERT INTO products (id, name, description, price, currency, category, is_active, is_deleted, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, product.getId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setString(5, product.getCurrency());
            statement.setString(6, product.getCategory());
            statement.setBoolean(7, product.isActive());
            statement.setBoolean(8, product.isDeleted());
            statement.setTimestamp(9, Timestamp.valueOf(product.getCreatedAt()));
            statement.setTimestamp(10, Timestamp.valueOf(product.getUpdatedAt()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product created successfully: {}", product.getId());
                return product;
            } else {
                logger.error("Failed to create product: {}", product.getId());
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Error creating product: {}", product.getId(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }
    
    /**
     * Execute query and return list of products
     */
    private List<Product> executeQuery(String sql, Object... parameters) {
        List<Product> products = new ArrayList<>();
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapResultSetToProduct(resultSet));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Database query failed", e);
        }
        
        return products;
    }
    
    /**
     * Execute query and return single product
     */
    private Optional<Product> executeQueryForSingle(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToProduct(resultSet));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing single query: {}", sql, e);
            throw new RuntimeException("Database query failed", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Execute count query
     */
    private long executeCountQuery(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing count query: {}", sql, e);
            throw new RuntimeException("Database count query failed", e);
        }
        
        return 0;
    }
    
    /**
     * Execute exists query
     */
    private boolean executeExistsQuery(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
            
        } catch (SQLException e) {
            logger.error("Error executing exists query: {}", sql, e);
            throw new RuntimeException("Database exists query failed", e);
        }
    }
    
    /**
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getString("id"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setCurrency(resultSet.getString("currency"));
        product.setCategory(resultSet.getString("category"));
        product.setActive(resultSet.getBoolean("is_active"));
        product.setDeleted(resultSet.getBoolean("is_deleted"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        Timestamp deletedAt = resultSet.getTimestamp("deleted_at");
        if (deletedAt != null) {
            product.setDeletedAt(deletedAt.toLocalDateTime());
        }
        
        return product;
    }
}
