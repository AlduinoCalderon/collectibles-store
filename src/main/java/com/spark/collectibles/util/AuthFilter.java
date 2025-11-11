package com.spark.collectibles.util;

import com.spark.collectibles.model.User;
import com.spark.collectibles.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

/**
 * Authentication filter for protecting routes
 * 
 * This filter validates JWT tokens and sets the current user in request attributes.
 * It can be used to protect routes that require authentication.
 */
public class AuthFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final AuthService authService;
    
    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Create a filter that requires authentication
     * @return Filter that validates JWT token
     */
    public Filter requireAuth() {
        return (Request request, Response response) -> {
            String authHeader = request.headers("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Unauthorized access attempt to: {}", request.pathInfo());
                halt(401, JsonUtil.toJson(new ErrorResponse("Authentication required")));
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            User user = authService.validateToken(token);
            
            if (user == null) {
                logger.warn("Invalid token for path: {}", request.pathInfo());
                halt(401, JsonUtil.toJson(new ErrorResponse("Invalid or expired token")));
            }
            
            // Set user in request attributes for use in route handlers
            request.attribute("currentUser", user);
            logger.debug("User authenticated: {} for path: {}", user.getUsername(), request.pathInfo());
        };
    }
    
    /**
     * Create a filter that requires a specific role
     * @param requiredRole Required user role
     * @return Filter that validates JWT token and role
     */
    public Filter requireRole(User.UserRole requiredRole) {
        return (Request request, Response response) -> {
            // First check authentication
            requireAuth().handle(request, response);
            
            // Then check role
            User user = request.attribute("currentUser");
            if (user == null || user.getRole() != requiredRole) {
                logger.warn("Access denied for user: {} to path: {} (required role: {})", 
                    user != null ? user.getUsername() : "unknown", 
                    request.pathInfo(), 
                    requiredRole);
                halt(403, JsonUtil.toJson(new ErrorResponse("Insufficient permissions")));
            }
            
            logger.debug("Role check passed for user: {} with role: {}", user.getUsername(), user.getRole());
        };
    }
    
    /**
     * Create a filter that requires one of the specified roles
     * @param allowedRoles Allowed user roles
     * @return Filter that validates JWT token and role
     */
    public Filter requireAnyRole(User.UserRole... allowedRoles) {
        return (Request request, Response response) -> {
            // First check authentication
            requireAuth().handle(request, response);
            
            // Then check role
            User user = request.attribute("currentUser");
            if (user == null) {
                halt(401, JsonUtil.toJson(new ErrorResponse("Authentication required")));
            }
            
            boolean hasAllowedRole = false;
            for (User.UserRole role : allowedRoles) {
                if (user.getRole() == role) {
                    hasAllowedRole = true;
                    break;
                }
            }
            
            if (!hasAllowedRole) {
                logger.warn("Access denied for user: {} to path: {} (required roles: {})", 
                    user.getUsername(), 
                    request.pathInfo(), 
                    java.util.Arrays.toString(allowedRoles));
                halt(403, JsonUtil.toJson(new ErrorResponse("Insufficient permissions")));
            }
            
            logger.debug("Role check passed for user: {} with role: {}", user.getUsername(), user.getRole());
        };
    }
    
    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

