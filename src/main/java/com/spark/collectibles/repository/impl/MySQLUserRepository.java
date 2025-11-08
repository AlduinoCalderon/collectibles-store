package com.spark.collectibles.repository.impl;

import com.spark.collectibles.database.DatabaseConnectionManager;
import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of UserRepository
 * 
 * This class implements the UserRepository interface using MySQL
 * as the data store, following SOLID principles and best practices.
 * Note: password_hash is never returned in queries for security.
 */
public class MySQLUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLUserRepository.class);
    private final DatabaseConnectionManager connectionManager;
    
    public MySQLUserRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE id = ?";
        return executeQueryForSingle(sql, id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE username = ?";
        return executeQueryForSingle(sql, username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE email = ?";
        return executeQueryForSingle(sql, email);
    }
    
    /**
     * Find user by username including password hash (for authentication)
     * @param username Username
     * @return Optional containing user with password hash if found
     */
    public Optional<User> findByUsernameWithPassword(String username) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE username = ?";
        return executeQueryForSingleWithPassword(sql, username);
    }
    
    /**
     * Find user by email including password hash (for authentication)
     * @param email Email
     * @return Optional containing user with password hash if found
     */
    public Optional<User> findByEmailWithPassword(String email) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE email = ?";
        return executeQueryForSingleWithPassword(sql, email);
    }
    
    @Override
    public User save(User user) {
        if (existsById(user.getId())) {
            return update(user);
        } else {
            return create(user);
        }
    }
    
    @Override
    public User create(User user) {
        String sql =
            "INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, user.getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPasswordHash()); // password_hash field
            statement.setString(5, user.getFirstName());
            statement.setString(6, user.getLastName());
            statement.setString(7, user.getRole().name());
            statement.setBoolean(8, user.isActive());
            statement.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            statement.setTimestamp(10, Timestamp.valueOf(user.getUpdatedAt()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User created successfully: {}", user.getId());
                // Return user without password hash
                return findById(user.getId()).orElse(user);
            } else {
                logger.error("Failed to create user: {}", user.getId());
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Error creating user: {}", user.getId(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    @Override
    public User update(User user) {
        // Check if password_hash needs to be updated
        String sql;
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            // Update including password
            sql =
                "UPDATE users " +
                "SET username = ?, email = ?, password_hash = ?, first_name = ?, last_name = ?, " +
                "role = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";
        } else {
            // Update without password
            sql =
                "UPDATE users " +
                "SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                "role = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";
        }
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            int paramIndex = 1;
            statement.setString(paramIndex++, user.getUsername());
            statement.setString(paramIndex++, user.getEmail());
            
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                statement.setString(paramIndex++, user.getPasswordHash());
            }
            
            statement.setString(paramIndex++, user.getFirstName());
            statement.setString(paramIndex++, user.getLastName());
            statement.setString(paramIndex++, user.getRole().name());
            statement.setBoolean(paramIndex++, user.isActive());
            statement.setString(paramIndex, user.getId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User updated successfully: {}", user.getId());
                return findById(user.getId()).orElse(user);
            } else {
                logger.warn("No user found to update with ID: {}", user.getId());
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getId(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("User deleted successfully: {}", id);
                return true;
            } else {
                logger.warn("No user found to delete with ID: {}", id);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    @Override
    public List<User> findByRole(User.UserRole role) {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE role = ? ORDER BY created_at DESC";
        return executeQuery(sql, role.name());
    }
    
    @Override
    public List<User> findActive() {
        String sql = "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users WHERE is_active = TRUE ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        return executeExistsQuery(sql, id);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        return executeExistsQuery(sql, username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        return executeExistsQuery(sql, email);
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        return executeCountQuery(sql);
    }
    
    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
        return executeCountQuery(sql);
    }
    
    @Override
    public List<User> search(String query) {
        String sql =
            "SELECT id, username, email, first_name, last_name, role, is_active, created_at, updated_at FROM users " +
            "WHERE username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ? " +
            "ORDER BY created_at DESC";
        String searchPattern = "%" + query + "%";
        return executeQuery(sql, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    /**
     * Execute query and return list of users (without password hash)
     */
    private List<User> executeQuery(String sql, Object... parameters) {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet, false));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Database query failed", e);
        }
        
        return users;
    }
    
    /**
     * Execute query and return single user (without password hash)
     */
    private Optional<User> executeQueryForSingle(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToUser(resultSet, false));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing single query: {}", sql, e);
            throw new RuntimeException("Database query failed", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Execute query and return single user (with password hash for authentication)
     */
    private Optional<User> executeQueryForSingleWithPassword(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToUser(resultSet, true));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error executing single query with password: {}", sql, e);
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
     * Map ResultSet to User object
     * @param resultSet ResultSet from database
     * @param includePassword Whether to include password_hash field
     */
    private User mapResultSetToUser(ResultSet resultSet, boolean includePassword) throws SQLException {
        User user = new User();
        user.setId(resultSet.getString("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        
        if (includePassword) {
            user.setPasswordHash(resultSet.getString("password_hash"));
        }
        
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        
        String roleStr = resultSet.getString("role");
        if (roleStr != null) {
            user.setRole(User.UserRole.valueOf(roleStr));
        }
        
        user.setActive(resultSet.getBoolean("is_active"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
}

