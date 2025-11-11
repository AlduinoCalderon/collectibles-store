package com.spark.collectibles.service;

import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthService
 * 
 * This test class covers all authentication functionality including:
 * - User registration with password hashing
 * - User login with password verification
 * - JWT token generation and validation
 * - Edge cases and error handling
 * - Security validations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }
    
    // ========== Registration Tests ==========
    
    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegisterUserSuccess() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setRole(User.UserRole.CUSTOMER);
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            User.UserRole.CUSTOMER
        );
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertNotNull(result.getToken());
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertEquals(User.UserRole.CUSTOMER, result.getUser().getRole());
        assertTrue(result.getToken().length() > 0);
        verify(userRepository).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with empty username")
    void testRegisterUserEmptyUsername() {
        // When
        AuthService.AuthResult result = authService.register(
            "",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with null username")
    void testRegisterUserNullUsername() {
        // When
        AuthService.AuthResult result = authService.register(
            null,
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with invalid email")
    void testRegisterUserInvalidEmail() {
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "invalid-email",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with empty email")
    void testRegisterUserEmptyEmail() {
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with weak password")
    void testRegisterUserWeakPassword() {
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "12345", // Less than 6 characters
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with null password")
    void testRegisterUserNullPassword() {
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            null,
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with duplicate username")
    void testRegisterUserDuplicateUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should not register user with duplicate email")
    void testRegisterUserDuplicateEmail() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should hash password during registration")
    void testRegisterUserPasswordHashing() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Verify password is hashed (BCrypt hash starts with $2a$ or $2b$)
            assertNotNull(user.getPasswordHash());
            assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
            assertNotEquals("password123", user.getPasswordHash());
            return user;
        });
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNotNull(result);
        verify(userRepository).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should use default CUSTOMER role when role is null")
    void testRegisterUserDefaultRole() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(User.UserRole.CUSTOMER, user.getRole());
            return user;
        });
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            user.setRole(User.UserRole.CUSTOMER);
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNotNull(result);
        assertEquals(User.UserRole.CUSTOMER, result.getUser().getRole());
    }
    
    // ========== Password Hashing Tests ==========
    
    @Test
    @DisplayName("Should hash password with BCrypt")
    void testHashPassword() {
        // When
        String hash = authService.hashPassword("password123");
        
        // Then
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertNotEquals("password123", hash);
        assertTrue(hash.length() >= 60); // BCrypt hashes are 60 characters
    }
    
    @Test
    @DisplayName("Should verify correct password")
    void testVerifyPasswordCorrect() {
        // Given
        String password = "password123";
        String hash = authService.hashPassword(password);
        
        // When
        boolean isValid = authService.verifyPassword(password, hash);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should not verify incorrect password")
    void testVerifyPasswordIncorrect() {
        // Given
        String hash = authService.hashPassword("correctpassword");
        
        // When
        boolean isValid = authService.verifyPassword("wrongpassword", hash);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should not verify password with null hash")
    void testVerifyPasswordNullHash() {
        // When
        boolean isValid = authService.verifyPassword("password123", null);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should not verify null password")
    void testVerifyPasswordNullPassword() {
        // Given
        String hash = authService.hashPassword("password123");
        
        // When
        boolean isValid = authService.verifyPassword(null, hash);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should generate different hashes for same password")
    void testHashPasswordUniqueness() {
        // When
        String hash1 = authService.hashPassword("password123");
        String hash2 = authService.hashPassword("password123");
        
        // Then
        assertNotEquals(hash1, hash2); // BCrypt uses random salt
        // But both should verify correctly
        assertTrue(authService.verifyPassword("password123", hash1));
        assertTrue(authService.verifyPassword("password123", hash2));
    }
    
    // ========== JWT Token Tests ==========
    
    @Test
    @DisplayName("Should generate valid JWT token during registration")
    void testGenerateJwtToken() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            user.setRole(User.UserRole.ADMIN);
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            User.UserRole.ADMIN
        );
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertTrue(result.getToken().length() > 0);
        assertTrue(result.getToken().contains(".")); // JWT has 3 parts separated by dots
    }
    
    @Test
    @DisplayName("Should validate valid JWT token")
    void testValidateValidToken() {
        // Given
        User user = new User("user1", "testuser", "test@example.com", "Test", "User");
        user.setActive(true);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            User.UserRole.CUSTOMER
        );
        
        // When
        User validatedUser = authService.validateToken(result.getToken());
        
        // Then
        assertNotNull(validatedUser);
        assertEquals("user1", validatedUser.getId());
        assertEquals("testuser", validatedUser.getUsername());
    }
    
    @Test
    @DisplayName("Should not validate invalid JWT token")
    void testValidateInvalidToken() {
        // When
        User user = authService.validateToken("invalid.token.here");
        
        // Then
        assertNull(user);
    }
    
    @Test
    @DisplayName("Should not validate null token")
    void testValidateNullToken() {
        // When
        User user = authService.validateToken(null);
        
        // Then
        assertNull(user);
    }
    
    @Test
    @DisplayName("Should not validate empty token")
    void testValidateEmptyToken() {
        // When
        User user = authService.validateToken("");
        
        // Then
        assertNull(user);
    }
    
    @Test
    @DisplayName("Should handle Bearer prefix in token")
    void testValidateTokenWithBearerPrefix() {
        // Given
        User user = new User("user1", "testuser", "test@example.com", "Test", "User");
        user.setActive(true);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // When
        User validatedUser = authService.validateToken("Bearer " + result.getToken());
        
        // Then
        assertNotNull(validatedUser);
        assertEquals("user1", validatedUser.getId());
    }
    
    @Test
    @DisplayName("Should not validate token for inactive user")
    void testValidateTokenInactiveUser() {
        // Given
        User user = new User("user1", "testuser", "test@example.com", "Test", "User");
        user.setActive(false);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // When
        User validatedUser = authService.validateToken(result.getToken());
        
        // Then
        assertNull(validatedUser); // Inactive user should not be validated
    }
    
    // ========== Edge Cases and Error Handling ==========
    
    @Test
    @DisplayName("Should handle registration with null firstName and lastName")
    void testRegisterUserNullNames() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            user.setFirstName("");
            user.setLastName("");
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            null,
            null,
            null
        );
        
        // Then
        assertNotNull(result);
        assertEquals("", result.getUser().getFirstName());
        assertEquals("", result.getUser().getLastName());
    }
    
    @Test
    @DisplayName("Should handle registration failure in repository")
    void testRegisterUserRepositoryFailure() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenReturn(null);
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should handle whitespace in username and email")
    void testRegisterUserWithWhitespace() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "  testuser  ",
            "  test@example.com  ",
            "password123",
            "Test",
            "User",
            null
        );
        
        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("test@example.com", result.getUser().getEmail());
    }
    
    @Test
    @DisplayName("Should handle very long password")
    void testRegisterUserLongPassword() {
        // Given
        String longPassword = "a".repeat(1000);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            longPassword,
            "Test",
            "User",
            null
        );
        
        // Then
        assertNotNull(result);
        // Password should be hashed regardless of length
        verify(userRepository).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should handle special characters in password")
    void testRegisterUserSpecialCharacters() {
        // Given
        String specialPassword = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            User user = new User();
            user.setId(invocation.getArgument(0));
            user.setUsername("testuser");
            return Optional.of(user);
        });
        
        // When
        AuthService.AuthResult result = authService.register(
            "testuser",
            "test@example.com",
            specialPassword,
            "Test",
            "User",
            null
        );
        
        // Then
        assertNotNull(result);
        verify(userRepository).create(any(User.class));
    }
}
