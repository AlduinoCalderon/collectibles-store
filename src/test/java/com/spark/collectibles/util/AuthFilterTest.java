package com.spark.collectibles.util;

import com.spark.collectibles.model.User;
import com.spark.collectibles.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthFilter
 * 
 * Tests authentication and authorization filters for route protection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter Tests")
class AuthFilterTest {
    
    @Mock
    private AuthService authService;
    
    @Mock
    private Request request;
    
    @Mock
    private Response response;
    
    private AuthFilter authFilter;
    
    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(authService);
    }
    
    @Test
    @DisplayName("Should allow request with valid token")
    void testRequireAuthValidToken() throws Exception {
        // Given
        User user = new User("user1", "testuser", "test@example.com", "Test", "User");
        user.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(user);
        
        // When
        authFilter.requireAuth().handle(request, response);
        
        // Then
        verify(request).attribute("currentUser", user);
        verify(response, never()).status(anyInt());
    }
    
    @Test
    @DisplayName("Should reject request without Authorization header")
    void testRequireAuthNoHeader() throws Exception {
        // Given
        when(request.headers("Authorization")).thenReturn(null);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // When & Then - halt() throws RuntimeException
        assertThrows(RuntimeException.class, () -> {
            authFilter.requireAuth().handle(request, response);
        });
        
        verify(response).status(401);
    }
    
    @Test
    @DisplayName("Should reject request with invalid token")
    void testRequireAuthInvalidToken() throws Exception {
        // Given
        when(request.headers("Authorization")).thenReturn("Bearer invalid-token");
        when(request.pathInfo()).thenReturn("/api/products");
        when(authService.validateToken("invalid-token")).thenReturn(null);
        
        // When & Then - halt() throws RuntimeException
        assertThrows(RuntimeException.class, () -> {
            authFilter.requireAuth().handle(request, response);
        });
        
        verify(response).status(401);
    }
    
    @Test
    @DisplayName("Should allow ADMIN user to access ADMIN routes")
    void testRequireRoleAdminSuccess() throws Exception {
        // Given
        User adminUser = new User("user1", "admin", "admin@example.com", "Admin", "User");
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(adminUser);
        
        // When
        authFilter.requireRole(User.UserRole.ADMIN).handle(request, response);
        
        // Then
        verify(request).attribute("currentUser", adminUser);
        verify(response, never()).status(403);
    }
    
    @Test
    @DisplayName("Should reject CUSTOMER user from ADMIN routes")
    void testRequireRoleAdminFailure() throws Exception {
        // Given
        User customerUser = new User("user1", "customer", "customer@example.com", "Customer", "User");
        customerUser.setRole(User.UserRole.CUSTOMER);
        customerUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(customerUser);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // When & Then - halt() throws RuntimeException
        assertThrows(RuntimeException.class, () -> {
            authFilter.requireRole(User.UserRole.ADMIN).handle(request, response);
        });
        
        verify(response).status(403);
    }
    
    @Test
    @DisplayName("Should allow user with any of the allowed roles")
    void testRequireAnyRoleSuccess() throws Exception {
        // Given
        User adminUser = new User("user1", "admin", "admin@example.com", "Admin", "User");
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(adminUser);
        
        // When
        authFilter.requireAnyRole(User.UserRole.ADMIN, User.UserRole.MODERATOR).handle(request, response);
        
        // Then
        verify(request).attribute("currentUser", adminUser);
        verify(response, never()).status(403);
    }
    
    @Test
    @DisplayName("Should reject user without any of the allowed roles")
    void testRequireAnyRoleFailure() throws Exception {
        // Given
        User customerUser = new User("user1", "customer", "customer@example.com", "Customer", "User");
        customerUser.setRole(User.UserRole.CUSTOMER);
        customerUser.setActive(true);
        
        when(request.headers("Authorization")).thenReturn("Bearer valid-token");
        when(authService.validateToken("valid-token")).thenReturn(customerUser);
        when(request.pathInfo()).thenReturn("/api/products");
        
        // When & Then - halt() throws RuntimeException
        assertThrows(RuntimeException.class, () -> {
            authFilter.requireAnyRole(User.UserRole.ADMIN, User.UserRole.MODERATOR).handle(request, response);
        });
        
        verify(response).status(403);
    }
}

