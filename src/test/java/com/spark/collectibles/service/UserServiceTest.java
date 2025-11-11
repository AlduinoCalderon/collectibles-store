package com.spark.collectibles.service;

import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserService
 * 
 * This test class contains unit tests for the UserService business logic
 * using mocked UserRepository to test service layer in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.existsById("user1")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenReturn(user);
        
        // When
        User createdUser = userService.createUser(user);
        
        // Then
        assertNotNull(createdUser);
        assertEquals("user1", createdUser.getId());
        assertEquals("john_doe", createdUser.getUsername());
        verify(userRepository).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not create user with duplicate ID")
    void testCreateUserDuplicateId() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.existsById("user1")).thenReturn(true);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not create user with duplicate username")
    void testCreateUserDuplicateUsername() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.existsById("user1")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not create user with duplicate email")
    void testCreateUserDuplicateEmail() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.existsById("user1")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
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
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not create null user")
    void testCreateUserNull() {
        // When
        User result = userService.createUser(null);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUserById() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // When
        User retrievedUser = userService.getUserById("user1");
        
        // Then
        assertNotNull(retrievedUser);
        assertEquals("user1", retrievedUser.getId());
        assertEquals("john_doe", retrievedUser.getUsername());
        verify(userRepository).findById("user1");
    }
    
    @Test
    @DisplayName("Should return null for non-existent user")
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // When
        User user = userService.getUserById("non-existent");
        
        // Then
        assertNull(user);
        verify(userRepository).findById("non-existent");
    }
    
    @Test
    @DisplayName("Should check if user exists")
    void testUserExists() {
        // Given
        when(userRepository.existsById("user1")).thenReturn(true);
        when(userRepository.existsById("non-existent")).thenReturn(false);
        
        // When & Then
        assertTrue(userService.userExists("user1"));
        assertFalse(userService.userExists("non-existent"));
        verify(userRepository).existsById("user1");
        verify(userRepository).existsById("non-existent");
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        // Given
        User existingUser = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User updatedUser = new User("user1", "john_doe_updated", "john.updated@example.com", "John", "Doe");
        updatedUser.setRole(User.UserRole.ADMIN);
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("john_doe_updated")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john.updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);
        
        // When
        User result = userService.updateUser("user1", updatedUser);
        
        // Then
        assertNotNull(result);
        assertEquals("john_doe_updated", result.getUsername());
        assertEquals("john.updated@example.com", result.getEmail());
        verify(userRepository).update(any(User.class));
    }
    
    @Test
    @DisplayName("Should not update non-existent user")
    void testUpdateUserNotFound() {
        // Given
        User user = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // When
        User result = userService.updateUser("non-existent", user);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).update(any(User.class));
    }
    
    @Test
    @DisplayName("Should not update user with duplicate username")
    void testUpdateUserDuplicateUsername() {
        // Given
        User existingUser = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User otherUser = new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe");
        User updatedUser = new User("user1", "jane_doe", "john@example.com", "John", "Doe");
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("jane_doe")).thenReturn(Optional.of(otherUser));
        
        // When
        User result = userService.updateUser("user1", updatedUser);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).update(any(User.class));
    }
    
    @Test
    @DisplayName("Should not update user with duplicate email")
    void testUpdateUserDuplicateEmail() {
        // Given
        User existingUser = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User otherUser = new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe");
        User updatedUser = new User("user1", "john_doe", "jane@example.com", "John", "Doe");
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));
        
        // When
        User result = userService.updateUser("user1", updatedUser);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).update(any(User.class));
    }
    
    @Test
    @DisplayName("Should not update user with invalid data")
    void testUpdateUserInvalidData() {
        // Given
        User existingUser = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        User invalidUser = new User("user1", "", "invalid-email", "John", "Doe");
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(existingUser));
        
        // When
        User result = userService.updateUser("user1", invalidUser);
        
        // Then
        assertNull(result);
        verify(userRepository, never()).update(any(User.class));
    }
    
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Given
        when(userRepository.deleteById("user1")).thenReturn(true);
        
        // When
        boolean deleted = userService.deleteUser("user1");
        
        // Then
        assertTrue(deleted);
        verify(userRepository).deleteById("user1");
    }
    
    @Test
    @DisplayName("Should not delete non-existent user")
    void testDeleteUserNotFound() {
        // Given
        when(userRepository.deleteById("non-existent")).thenReturn(false);
        
        // When
        boolean deleted = userService.deleteUser("non-existent");
        
        // Then
        assertFalse(deleted);
        verify(userRepository).deleteById("non-existent");
    }
    
    @Test
    @DisplayName("Should retrieve all users")
    void testGetAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(
            new User("user1", "john_doe", "john@example.com", "John", "Doe"),
            new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe")
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);
        
        // When
        List<User> users = userService.getAllUsers();
        
        // Then
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should search users by query")
    void testSearchUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(
            new User("user1", "john_doe", "john@example.com", "John", "Doe")
        );
        when(userRepository.search("john")).thenReturn(expectedUsers);
        
        // When
        List<User> results = userService.searchUsers("john");
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("user1", results.get(0).getId());
        verify(userRepository).search("john");
    }
    
    @Test
    @DisplayName("Should return all users when search query is empty")
    void testSearchUsersEmptyQuery() {
        // Given
        List<User> allUsers = Arrays.asList(
            new User("user1", "john_doe", "john@example.com", "John", "Doe"),
            new User("user2", "jane_doe", "jane@example.com", "Jane", "Doe")
        );
        when(userRepository.findAll()).thenReturn(allUsers);
        
        // When
        List<User> results = userService.searchUsers("");
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should return all users when search query is null")
    void testSearchUsersNullQuery() {
        // Given
        List<User> allUsers = Arrays.asList(
            new User("user1", "john_doe", "john@example.com", "John", "Doe")
        );
        when(userRepository.findAll()).thenReturn(allUsers);
        
        // When
        List<User> results = userService.searchUsers(null);
        
        // Then
        assertNotNull(results);
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should get users by role")
    void testGetUsersByRole() {
        // Given
        User admin = new User("admin1", "admin_user", "admin@example.com", "Admin", "User");
        admin.setRole(User.UserRole.ADMIN);
        List<User> admins = Arrays.asList(admin);
        
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(admins);
        
        // When
        List<User> result = userService.getUsersByRole(User.UserRole.ADMIN);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin1", result.get(0).getId());
        verify(userRepository).findByRole(User.UserRole.ADMIN);
    }
    
    @Test
    @DisplayName("Should get active users")
    void testGetActiveUsers() {
        // Given
        List<User> activeUsers = Arrays.asList(
            new User("user1", "john_doe", "john@example.com", "John", "Doe")
        );
        when(userRepository.findActive()).thenReturn(activeUsers);
        
        // When
        List<User> result = userService.getActiveUsers();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findActive();
    }
    
    @Test
    @DisplayName("Should get user count")
    void testGetUserCount() {
        // Given
        when(userRepository.count()).thenReturn(5L);
        
        // When
        int count = userService.getUserCount();
        
        // Then
        assertEquals(5, count);
        verify(userRepository).count();
    }
    
    @Test
    @DisplayName("Should handle update with null password hash")
    void testUpdateUserPreservePasswordHash() {
        // Given
        User existingUser = new User("user1", "john_doe", "john@example.com", "John", "Doe");
        existingUser.setPasswordHash("existing-hash");
        User updatedUser = new User("user1", "john_doe", "john@example.com", "John", "Doe Updated");
        updatedUser.setPasswordHash(null); // Not updating password
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });
        
        // When
        User result = userService.updateUser("user1", updatedUser);
        
        // Then
        assertNotNull(result);
        assertEquals("existing-hash", result.getPasswordHash()); // Password hash preserved
    }
}
