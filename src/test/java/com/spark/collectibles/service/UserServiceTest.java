package com.spark.collectibles.service;

import com.spark.collectibles.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UserService
 * 
 * This class contains unit tests for the UserService business logic
 */
@DisplayName("UserService Tests")
class UserServiceTest {
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService();
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        
        // When
        User createdUser = userService.createUser(user);
        
        // Then
        assertNotNull(createdUser);
        assertEquals("user1", createdUser.getId());
        assertEquals("john_doe", createdUser.getUsername());
        assertEquals("john@example.com", createdUser.getEmail());
        assertEquals("John", createdUser.getFirstName());
        assertEquals("Doe", createdUser.getLastName());
        assertTrue(createdUser.isActive());
        assertEquals(User.UserRole.CUSTOMER, createdUser.getRole());
    }
    
    @Test
    @DisplayName("Should not create user with duplicate ID")
    void testCreateUserDuplicateId() {
        // Given
        User user1 = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User user2 = new User("user1", "jane_doe", "jane@example.com", "Jane", "Doe");
        
        // When
        userService.createUser(user1);
        User duplicateUser = userService.createUser(user2);
        
        // Then
        assertNull(duplicateUser);
    }
    
    @Test
    @DisplayName("Should not create user with invalid data")
    void testCreateUserInvalidData() {
        // Given
        User invalidUser = new User("", "john_doe", "invalid-email", "John", "Doe");
        
        // When
        User result = userService.createUser(invalidUser);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUserById() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        userService.createUser(user);
        
        // When
        User retrievedUser = userService.getUserById("user1");
        
        // Then
        assertNotNull(retrievedUser);
        assertEquals("user1", retrievedUser.getId());
        assertEquals("john_doe", retrievedUser.getUsername());
    }
    
    @Test
    @DisplayName("Should return null for non-existent user")
    void testGetUserByIdNotFound() {
        // When
        User user = userService.getUserById("non-existent");
        
        // Then
        assertNull(user);
    }
    
    @Test
    @DisplayName("Should check if user exists")
    void testUserExists() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        userService.createUser(user);
        
        // When & Then
        assertTrue(userService.userExists("user1"));
        assertFalse(userService.userExists("non-existent"));
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        userService.createUser(user);
        
        User updatedUser = new User("user1", "john_doe_updated", "john.updated@example.com", "John", "Doe");
        updatedUser.setRole(User.UserRole.ADMIN);
        
        // When
        User result = userService.updateUser("user1", updatedUser);
        
        // Then
        assertNotNull(result);
        assertEquals("john_doe_updated", result.getUsername());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals(User.UserRole.ADMIN, result.getRole());
    }
    
    @Test
    @DisplayName("Should not update non-existent user")
    void testUpdateUserNotFound() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        
        // When
        User result = userService.updateUser("non-existent", user);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        userService.createUser(user);
        
        // When
        boolean deleted = userService.deleteUser("user1");
        
        // Then
        assertTrue(deleted);
        assertFalse(userService.userExists("user1"));
    }
    
    @Test
    @DisplayName("Should not delete non-existent user")
    void testDeleteUserNotFound() {
        // When
        boolean deleted = userService.deleteUser("non-existent");
        
        // Then
        assertFalse(deleted);
    }
    
    @Test
    @DisplayName("Should retrieve all users")
    void testGetAllUsers() {
        // Given
        User user1 = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User user2 = new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe");
        userService.createUser(user1);
        userService.createUser(user2);
        
        // When
        List<User> users = userService.getAllUsers();
        
        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getId().equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.getId().equals("user2")));
    }
    
    @Test
    @DisplayName("Should search users by query")
    void testSearchUsers() {
        // Given
        User user1 = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User user2 = new User("user2", "jane_smith", "jane@example.com", "Jane", "Smith");
        userService.createUser(user1);
        userService.createUser(user2);
        
        // When
        List<User> johnResults = userService.searchUsers("john");
        List<User> janeResults = userService.searchUsers("jane");
        List<User> smithResults = userService.searchUsers("smith");
        
        // Then
        assertEquals(1, johnResults.size());
        assertEquals("user1", johnResults.get(0).getId());
        
        assertEquals(1, janeResults.size());
        assertEquals("user2", janeResults.get(0).getId());
        
        assertEquals(1, smithResults.size());
        assertEquals("user2", smithResults.get(0).getId());
    }
    
    @Test
    @DisplayName("Should get users by role")
    void testGetUsersByRole() {
        // Given
        User admin = new User("admin1", "admin_user", "admin@example.com", "Admin", "User");
        admin.setRole(User.UserRole.ADMIN);
        
        User customer = new User("customer1", "customer_user", "customer@example.com", "Customer", "User");
        customer.setRole(User.UserRole.CUSTOMER);
        
        userService.createUser(admin);
        userService.createUser(customer);
        
        // When
        List<User> admins = userService.getUsersByRole(User.UserRole.ADMIN);
        List<User> customers = userService.getUsersByRole(User.UserRole.CUSTOMER);
        
        // Then
        assertEquals(1, admins.size());
        assertEquals("admin1", admins.get(0).getId());
        
        assertEquals(1, customers.size());
        assertEquals("customer1", customers.get(0).getId());
    }
    
    @Test
    @DisplayName("Should get user count")
    void testGetUserCount() {
        // Given
        assertEquals(0, userService.getUserCount());
        
        User user1 = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User user2 = new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe");
        
        // When
        userService.createUser(user1);
        assertEquals(1, userService.getUserCount());
        
        userService.createUser(user2);
        assertEquals(2, userService.getUserCount());
    }
}
