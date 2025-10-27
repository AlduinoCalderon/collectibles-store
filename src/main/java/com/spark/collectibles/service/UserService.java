package com.spark.collectibles.service;

import com.spark.collectibles.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class for managing user operations
 * 
 * This class provides business logic for user CRUD operations
 * In a real application, this would interact with a database
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // In-memory storage for demonstration purposes
    // In a real application, this would be replaced with database operations
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        logger.info("Retrieving all users");
        return new ArrayList<>(users.values());
    }
    
    /**
     * Get a user by ID
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User getUserById(String id) {
        logger.info("Retrieving user with ID: {}", id);
        return users.get(id);
    }
    
    /**
     * Check if a user exists by ID
     * @param id User ID
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String id) {
        logger.info("Checking if user exists with ID: {}", id);
        return users.containsKey(id);
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
        
        if (users.containsKey(user.getId())) {
            logger.warn("User with ID {} already exists", user.getId());
            return null;
        }
        
        // Check if username or email already exists
        boolean usernameExists = users.values().stream()
                .anyMatch(u -> u.getUsername().equals(user.getUsername()));
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        
        if (usernameExists) {
            logger.warn("Username {} already exists", user.getUsername());
            return null;
        }
        
        if (emailExists) {
            logger.warn("Email {} already exists", user.getEmail());
            return null;
        }
        
        user.touch();
        users.put(user.getId(), user);
        logger.info("User created successfully with ID: {}", user.getId());
        return user;
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
        
        User existingUser = users.get(id);
        if (existingUser == null) {
            logger.warn("User with ID {} not found for update", id);
            return null;
        }
        
        // Check if username or email conflicts with other users
        boolean usernameConflict = users.values().stream()
                .anyMatch(u -> !u.getId().equals(id) && u.getUsername().equals(user.getUsername()));
        boolean emailConflict = users.values().stream()
                .anyMatch(u -> !u.getId().equals(id) && u.getEmail().equals(user.getEmail()));
        
        if (usernameConflict) {
            logger.warn("Username {} already exists for another user", user.getUsername());
            return null;
        }
        
        if (emailConflict) {
            logger.warn("Email {} already exists for another user", user.getEmail());
            return null;
        }
        
        // Preserve creation timestamp
        user.setCreatedAt(existingUser.getCreatedAt());
        user.touch();
        users.put(id, user);
        logger.info("User updated successfully with ID: {}", id);
        return user;
    }
    
    /**
     * Delete a user by ID
     * @param id User ID
     * @return true if user was deleted, false if user not found
     */
    public boolean deleteUser(String id) {
        User removedUser = users.remove(id);
        if (removedUser != null) {
            logger.info("User deleted successfully with ID: {}", id);
            return true;
        } else {
            logger.warn("User with ID {} not found for deletion", id);
            return false;
        }
    }
    
    /**
     * Search users by username or email
     * @param query Search query
     * @return List of matching users
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        
        String lowerQuery = query.toLowerCase();
        return users.values().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowerQuery) ||
                              user.getEmail().toLowerCase().contains(lowerQuery) ||
                              user.getFirstName().toLowerCase().contains(lowerQuery) ||
                              user.getLastName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Get users by role
     * @param role User role
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return users.values().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active users only
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return users.values().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user count
     * @return Total number of users
     */
    public int getUserCount() {
        return users.size();
    }
}
