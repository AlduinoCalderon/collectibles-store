package com.spark.collectibles.repository;

import com.spark.collectibles.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * 
 * This interface defines the contract for user data access operations
 * following the Repository pattern and SOLID principles.
 */
public interface UserRepository {
    
    /**
     * Find all users (excluding soft-deleted if implemented)
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findById(String id);
    
    /**
     * Find user by username
     * @param username Username
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     * @param email Email address
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Save user (create or update)
     * @param user User to save
     * @return Saved user
     */
    User save(User user);
    
    /**
     * Create a new user
     * @param user User to create
     * @return Created user
     */
    User create(User user);
    
    /**
     * Update user
     * @param user User to update
     * @return Updated user
     */
    User update(User user);
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(String id);
    
    /**
     * Find users by role
     * @param role User role
     * @return List of users with the specified role
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * Find active users only
     * @return List of active users
     */
    List<User> findActive();
    
    /**
     * Check if user exists by ID
     * @param id User ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
    
    /**
     * Check if user exists by username
     * @param username Username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if user exists by email
     * @param email Email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Count total users
     * @return Total number of users
     */
    long count();
    
    /**
     * Count active users
     * @return Number of active users
     */
    long countActive();
    
    /**
     * Search users by username, email, first name, or last name
     * @param query Search query
     * @return List of matching users
     */
    List<User> search(String query);
}

