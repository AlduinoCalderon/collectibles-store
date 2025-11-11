package com.spark.collectibles.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import com.spark.collectibles.repository.impl.MySQLUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * 
 * Tests cover:
 * - User registration with validation
 * - User login with password verification
 * - JWT token generation and validation
 * - Password hashing and verification
 * - Edge cases and error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private AuthService authService;
    private String testJwtSecret;
    private int testJwtExpirationHours;
    
    @BeforeEach
    void setUp() {
        // Use a test JWT secret for testing
        testJwtSecret = "test-jwt-secret-key-for-unit-testing-minimum-32-characters-long";
        testJwtExpirationHours = 24;
        authService = new AuthService(userRepository, testJwtSecret, testJwtExpirationHours);
    }
    
    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "Test";
        String lastName = "User";
        User.UserRole role = User.UserRole.CUSTOMER;
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        
        User savedUser = new User();
        savedUser.setId("user1");
        savedUser.setUsername(username);
        savedUser.setEmail(email);
        savedUser.setFirstName(firstName);
        savedUser.setLastName(lastName);
        savedUser.setRole(role);
        savedUser.setActive(true);
        
        when(userRepository.create(any(User.class))).thenReturn(savedUser);
        
        // Act
        AuthService.AuthResult result = authService.register(username, email, password, firstName, lastName, role);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertNotNull(result.getToken());
        assertEquals(username, result.getUser().getUsername());
        assertEquals(email, result.getUser().getEmail());
        assertEquals(role, result.getUser().getRole());
        assertTrue(result.getToken().length() > 0);
        
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with empty username")
    void testRegisterUser_EmptyUsername() {
        // Act
        AuthService.AuthResult result = authService.register("", "test@example.com", "password123", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with null username")
    void testRegisterUser_NullUsername() {
        // Act
        AuthService.AuthResult result = authService.register(null, "test@example.com", "password123", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with invalid email")
    void testRegisterUser_InvalidEmail() {
        // Act
        AuthService.AuthResult result = authService.register("testuser", "invalid-email", "password123", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with short password")
    void testRegisterUser_ShortPassword() {
        // Act
        AuthService.AuthResult result = authService.register("testuser", "test@example.com", "12345", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with existing username")
    void testRegisterUser_DuplicateUsername() {
        // Arrange
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);
        
        // Act
        AuthService.AuthResult result = authService.register(username, "test@example.com", "password123", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail registration with existing email")
    void testRegisterUser_DuplicateEmail() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        // Act
        AuthService.AuthResult result = authService.register("testuser", email, "password123", "Test", "User", null);
        
        // Assert
        assertNull(result);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).create(any(User.class));
    }
    
    @Test
    @DisplayName("Should successfully login with username")
    void testLogin_WithUsername_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = authService.hashPassword(password);
        
        User user = new User();
        user.setId("user1");
        user.setUsername(username);
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        user.setActive(true);
        user.setRole(User.UserRole.CUSTOMER);
        
        MySQLUserRepository mysqlRepo = mock(MySQLUserRepository.class);
        when(mysqlRepo.findByUsernameWithPassword(username)).thenReturn(Optional.of(user));
        
        AuthService authServiceWithMock = new AuthService(mysqlRepo, testJwtSecret, testJwtExpirationHours);
        
        // Act
        AuthService.AuthResult result = authServiceWithMock.login(username, password);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertNotNull(result.getToken());
        assertEquals(username, result.getUser().getUsername());
        assertTrue(result.getToken().length() > 0);
    }
    
    @Test
    @DisplayName("Should successfully login with email")
    void testLogin_WithEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = authService.hashPassword(password);
        
        User user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setActive(true);
        user.setRole(User.UserRole.CUSTOMER);
        
        MySQLUserRepository mysqlRepo = mock(MySQLUserRepository.class);
        when(mysqlRepo.findByUsernameWithPassword(email)).thenReturn(Optional.empty());
        when(mysqlRepo.findByEmailWithPassword(email)).thenReturn(Optional.of(user));
        
        AuthService authServiceWithMock = new AuthService(mysqlRepo, testJwtSecret, testJwtExpirationHours);
        
        // Act
        AuthService.AuthResult result = authServiceWithMock.login(email, password);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertNotNull(result.getToken());
        assertEquals(email, result.getUser().getEmail());
    }
    
    @Test
    @DisplayName("Should fail login with wrong password")
    void testLogin_WrongPassword() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = authService.hashPassword(correctPassword);
        
        User user = new User();
        user.setId("user1");
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setActive(true);
        
        MySQLUserRepository mysqlRepo = mock(MySQLUserRepository.class);
        when(mysqlRepo.findByUsernameWithPassword(username)).thenReturn(Optional.of(user));
        
        AuthService authServiceWithMock = new AuthService(mysqlRepo, testJwtSecret, testJwtExpirationHours);
        
        // Act
        AuthService.AuthResult result = authServiceWithMock.login(username, wrongPassword);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should fail login with non-existent user")
    void testLogin_NonExistentUser() {
        // Arrange
        MySQLUserRepository mysqlRepo = mock(MySQLUserRepository.class);
        when(mysqlRepo.findByUsernameWithPassword(anyString())).thenReturn(Optional.empty());
        when(mysqlRepo.findByEmailWithPassword(anyString())).thenReturn(Optional.empty());
        
        AuthService authServiceWithMock = new AuthService(mysqlRepo, testJwtSecret, testJwtExpirationHours);
        
        // Act
        AuthService.AuthResult result = authServiceWithMock.login("nonexistent", "password123");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should fail login with inactive user")
    void testLogin_InactiveUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = authService.hashPassword(password);
        
        User user = new User();
        user.setId("user1");
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setActive(false); // Inactive user
        
        MySQLUserRepository mysqlRepo = mock(MySQLUserRepository.class);
        when(mysqlRepo.findByUsernameWithPassword(username)).thenReturn(Optional.of(user));
        
        AuthService authServiceWithMock = new AuthService(mysqlRepo, testJwtSecret, testJwtExpirationHours);
        
        // Act
        AuthService.AuthResult result = authServiceWithMock.login(username, password);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should fail login with empty username")
    void testLogin_EmptyUsername() {
        // Act
        AuthService.AuthResult result = authService.login("", "password123");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should fail login with empty password")
    void testLogin_EmptyPassword() {
        // Act
        AuthService.AuthResult result = authService.login("testuser", "");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should successfully validate a valid JWT token")
    void testValidateToken_ValidToken() {
        // Arrange
        User user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setActive(true);
        user.setRole(User.UserRole.CUSTOMER);
        
        // Generate a valid token
        String token = generateTestToken(user.getId(), user.getUsername(), user.getRole().name());
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Act
        User validatedUser = authService.validateToken(token);
        
        // Assert
        assertNotNull(validatedUser);
        assertEquals("user1", validatedUser.getId());
        assertEquals("testuser", validatedUser.getUsername());
    }
    
    @Test
    @DisplayName("Should fail validation with invalid token")
    void testValidateToken_InvalidToken() {
        // Act
        User validatedUser = authService.validateToken("invalid-token");
        
        // Assert
        assertNull(validatedUser);
    }
    
    @Test
    @DisplayName("Should fail validation with null token")
    void testValidateToken_NullToken() {
        // Act
        User validatedUser = authService.validateToken(null);
        
        // Assert
        assertNull(validatedUser);
    }
    
    @Test
    @DisplayName("Should fail validation with empty token")
    void testValidateToken_EmptyToken() {
        // Act
        User validatedUser = authService.validateToken("");
        
        // Assert
        assertNull(validatedUser);
    }
    
    @Test
    @DisplayName("Should fail validation when user not found")
    void testValidateToken_UserNotFound() {
        // Arrange
        String token = generateTestToken("nonexistent", "testuser", "CUSTOMER");
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        User validatedUser = authService.validateToken(token);
        
        // Assert
        assertNull(validatedUser);
    }
    
    @Test
    @DisplayName("Should fail validation with inactive user")
    void testValidateToken_InactiveUser() {
        // Arrange
        User user = new User();
        user.setId("user1");
        user.setActive(false);
        
        String token = generateTestToken("user1", "testuser", "CUSTOMER");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Act
        User validatedUser = authService.validateToken(token);
        
        // Assert
        assertNull(validatedUser);
    }
    
    @Test
    @DisplayName("Should handle token with Bearer prefix")
    void testValidateToken_WithBearerPrefix() {
        // Arrange
        User user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setActive(true);
        
        String token = generateTestToken("user1", "testuser", "CUSTOMER");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Act
        User validatedUser = authService.validateToken("Bearer " + token);
        
        // Assert
        assertNotNull(validatedUser);
        assertEquals("user1", validatedUser.getId());
    }
    
    @Test
    @DisplayName("Should hash password correctly")
    void testHashPassword() {
        // Act
        String hash1 = authService.hashPassword("password123");
        String hash2 = authService.hashPassword("password123");
        
        // Assert
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertTrue(hash1.startsWith("$2"));
        // BCrypt hashes should be different each time (due to salt)
        assertNotEquals(hash1, hash2);
    }
    
    @Test
    @DisplayName("Should verify password correctly")
    void testVerifyPassword() {
        // Arrange
        String password = "password123";
        String hash = authService.hashPassword(password);
        
        // Act & Assert
        assertTrue(authService.verifyPassword(password, hash));
        assertFalse(authService.verifyPassword("wrongpassword", hash));
        assertFalse(authService.verifyPassword(null, hash));
        assertFalse(authService.verifyPassword(password, null));
    }
    
    @Test
    @DisplayName("Should handle null password in hashPassword")
    void testHashPassword_NullPassword() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.hashPassword(null);
        });
    }
    
    @Test
    @DisplayName("Should register user with ADMIN role")
    void testRegisterUser_WithAdminRole() {
        // Arrange
        String username = "adminuser";
        String email = "admin@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        
        User savedUser = new User();
        savedUser.setId("user1");
        savedUser.setUsername(username);
        savedUser.setEmail(email);
        savedUser.setRole(User.UserRole.ADMIN);
        savedUser.setActive(true);
        
        when(userRepository.create(any(User.class))).thenReturn(savedUser);
        
        // Act
        AuthService.AuthResult result = authService.register(username, email, password, "Admin", "User", User.UserRole.ADMIN);
        
        // Assert
        assertNotNull(result);
        assertEquals(User.UserRole.ADMIN, result.getUser().getRole());
    }
    
    @Test
    @DisplayName("Should register user with default CUSTOMER role when role is null")
    void testRegisterUser_DefaultRole() {
        // Arrange
        String username = "customeruser";
        String email = "customer@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        
        User savedUser = new User();
        savedUser.setId("user1");
        savedUser.setUsername(username);
        savedUser.setEmail(email);
        savedUser.setRole(User.UserRole.CUSTOMER);
        savedUser.setActive(true);
        
        when(userRepository.create(any(User.class))).thenReturn(savedUser);
        
        // Act
        AuthService.AuthResult result = authService.register(username, email, password, "Customer", "User", null);
        
        // Assert
        assertNotNull(result);
        assertEquals(User.UserRole.CUSTOMER, result.getUser().getRole());
    }
    
    /**
     * Generate a test JWT token for testing purposes
     */
    private String generateTestToken(String userId, String username, String role) {
        Algorithm algorithm = Algorithm.HMAC256(testJwtSecret);
        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new java.util.Date())
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + testJwtExpirationHours * 3600L * 1000))
                .sign(algorithm);
    }
}



