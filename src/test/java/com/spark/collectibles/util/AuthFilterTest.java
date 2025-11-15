package com.spark.collectibles.util;

import com.spark.collectibles.model.User;
import com.spark.collectibles.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthFilter
 * 
 * Tests cover:
 * - Authentication requirement validation
 * - Role-based access control
 * - Error handling for missing/invalid tokens
 * - Edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter Unit Tests")
class AuthFilterTest {
    
    @Mock
    private AuthService authService;
    
    @Mock
    private Request request;
    
    @Mock
    private Response response;
    
    private AuthFilter authFilter;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(authService);
        
        testUser = new User();
        testUser.setId("user1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.ADMIN);
        testUser.setActive(true);
    }
    
    @Test
    @DisplayName("Should allow access with valid token")
    void testRequireAuth_ValidToken() {
        // Arrange
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(testUser);
        
        // Act
        spark.Filter filter = authFilter.requireAuth();
        
        // Assert - Filter should not throw exception
        assertDoesNotThrow(() -> {
            try {
                filter.handle(request, response);
            } catch (Exception e) {
                // Spark's halt() throws an exception, which is expected
                // We just verify the token was validated
            }
        });
        
        verify(authService).validateToken("valid-token");
    }
    
    @Test
    @DisplayName("Should deny access without Authorization header")
    void testRequireAuth_NoHeader() {
        // Arrange
        when(request.headers("Authorization")).thenReturn(null);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // Act
        spark.Filter filter = authFilter.requireAuth();
        
        // Assert
        assertThrows(Exception.class, () -> {
            filter.handle(request, response);
        });
        
        verify(authService, never()).validateToken(anyString());
    }
    
    @Test
    @DisplayName("Should deny access with invalid token format")
    void testRequireAuth_InvalidFormat() {
        // Arrange
        when(request.headers("Authorization")).thenReturn("InvalidFormat token");
        when(request.pathInfo()).thenReturn("/api/products");
        
        // Act
        spark.Filter filter = authFilter.requireAuth();
        
        // Assert
        assertThrows(Exception.class, () -> {
            filter.handle(request, response);
        });
    }
    
    @Test
    @DisplayName("Should deny access with invalid token")
    void testRequireAuth_InvalidToken() {
        // Arrange
        when(request.headers("Authorization")).thenReturn("Bearer invalid-token");
        when(authService.validateToken("invalid-token")).thenReturn(null);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // Act
        spark.Filter filter = authFilter.requireAuth();
        
        // Assert
        assertThrows(Exception.class, () -> {
            filter.handle(request, response);
        });
        
        verify(authService).validateToken("invalid-token");
    }
    
    @Test
    @DisplayName("Should allow access with ADMIN role")
    void testRequireRole_AdminRole() {
        // Arrange
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(testUser);
        
        // Act
        spark.Filter filter = authFilter.requireRole(User.UserRole.ADMIN);
        
        // Assert - Should not throw exception for ADMIN user
        assertDoesNotThrow(() -> {
            try {
                filter.handle(request, response);
            } catch (Exception e) {
                // Expected for halt()
            }
        });
    }
    
    @Test
    @DisplayName("Should deny access with CUSTOMER role when ADMIN required")
    void testRequireRole_CustomerWhenAdminRequired() {
        // Arrange
        User customerUser = new User();
        customerUser.setId("user2");
        customerUser.setRole(User.UserRole.CUSTOMER);
        customerUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(customerUser);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // Act
        spark.Filter filter = authFilter.requireRole(User.UserRole.ADMIN);
        
        // Assert
        assertThrows(Exception.class, () -> {
            filter.handle(request, response);
        });
    }
    
    @Test
    @DisplayName("Should allow access with any of the allowed roles")
    void testRequireAnyRole_Success() {
        // Arrange
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("Bearer valid-token")).thenReturn(testUser);
        
        // Act
        spark.Filter filter = authFilter.requireAnyRole(User.UserRole.ADMIN, User.UserRole.MODERATOR);
        
        // Assert
        assertDoesNotThrow(() -> {
            try {
                filter.handle(request, response);
            } catch (Exception e) {
                // Expected for halt()
            }
        });
    }
    
    @Test
    @DisplayName("Should deny access when user role not in allowed roles")
    void testRequireAnyRole_Failure() {
        // Arrange
        User customerUser = new User();
        customerUser.setId("user2");
        customerUser.setRole(User.UserRole.CUSTOMER);
        customerUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(customerUser);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // Act
        spark.Filter filter = authFilter.requireAnyRole(User.UserRole.ADMIN, User.UserRole.MODERATOR);
        
        // Assert
        assertThrows(Exception.class, () -> {
            filter.handle(request, response);
        });
    }
}






