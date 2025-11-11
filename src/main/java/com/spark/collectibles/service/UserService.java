package com.spark.collectibles.service;

import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import com.spark.collectibles.repository.impl.MySQLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user operations
 * 
 * This class provides business logic for user CRUD operations
 * using the UserRepository for database persistence.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new MySQLUserRepository();
    }
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        logger.info("Retrieving all users");
        return userRepository.findAll();
    }
    
    /**
     * Get a user by ID
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User getUserById(String id) {
        logger.info("Retrieving user with ID: {}", id);
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * Check if a user exists by ID
     * @param id User ID
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String id) {
        logger.info("Checking if user exists with ID: {}", id);
        return userRepository.existsById(id);
    }
    
    /**
     * Create a new user
     * @param user User to create
     * @return Created user if successful, null if user already exists or invalid
     */
    public User createUser(User user) {
        if (user == null || !user.isValid()) {
            logger.warn("Invalid user data provided for creation");
            return null;
        }
        
        if (userRepository.existsById(user.getId())) {
            logger.warn("User with ID {} already exists", user.getId());
            return null;
        }
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Username {} already exists", user.getUsername());
            return null;
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Email {} already exists", user.getEmail());
            return null;
        }
        
        user.touch();
        User createdUser = userRepository.create(user);
        if (createdUser != null) {
            logger.info("User created successfully with ID: {}", user.getId());
        }
        return createdUser;
    }
    
    /**
     * Update an existing user
     * @param id User ID
     * @param user Updated user data
     * @return Updated user if successful, null if user not found or invalid
     */
    public User updateUser(String id, User user) {
        if (user == null || !user.isValid()) {
            logger.warn("Invalid user data provided for update");
            return null;
        }
        
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            logger.warn("User with ID {} not found for update", id);
            return null;
        }
        
        // Check if username or email conflicts with other users
        Optional<User> usernameUser = userRepository.findByUsername(user.getUsername());
        if (usernameUser.isPresent() && !usernameUser.get().getId().equals(id)) {
            logger.warn("Username {} already exists for another user", user.getUsername());
            return null;
        }
        
        Optional<User> emailUser = userRepository.findByEmail(user.getEmail());
        if (emailUser.isPresent() && !emailUser.get().getId().equals(id)) {
            logger.warn("Email {} already exists for another user", user.getEmail());
            return null;
        }
        
        // Preserve creation timestamp and password hash if not updating password
        user.setId(id);
        user.setCreatedAt(existingUser.getCreatedAt());
        // Only update password if provided
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            // Keep existing password hash
            user.setPasswordHash(existingUser.getPasswordHash());
        }
        user.touch();
        
        User updatedUser = userRepository.update(user);
        if (updatedUser != null) {
            logger.info("User updated successfully with ID: {}", id);
        }
        return updatedUser;
    }
    
    /**
     * Delete a user by ID
     * @param id User ID
     * @return true if user was deleted, false if user not found
     */
    public boolean deleteUser(String id) {
        boolean deleted = userRepository.deleteById(id);
        if (deleted) {
            logger.info("User deleted successfully with ID: {}", id);
        } else {
            logger.warn("User with ID {} not found for deletion", id);
        }
        return deleted;
    }
    
    /**
     * Search users by username, email, first name, or last name
     * @param query Search query
     * @return List of matching users
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        
        return userRepository.search(query.trim());
    }
    
    /**
     * Get users by role
     * @param role User role
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Get active users only
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findActive();
    }
    
    /**
     * Get user count
     * @return Total number of users
     */
    public int getUserCount() {
        return (int) userRepository.count();
    }
}
