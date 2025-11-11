package com.spark.collectibles.routes;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spark.collectibles.model.User;
import com.spark.collectibles.service.AuthService;
import com.spark.collectibles.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

/**
 * Route definitions for authentication API endpoints
 * 
 * This class defines all the RESTful routes for user authentication
 * including registration, login, and token validation.
 */
public class AuthRoutes {
    private static final Logger logger = LoggerFactory.getLogger(AuthRoutes.class);
    private static final Gson gson = new Gson();
    
    /**
     * Initialize all authentication-related routes
     * @param authService AuthService instance for authentication logic
     */
    public static void initialize(AuthService authService) {
        // POST /api/auth/register — Register a new user
        post("/api/auth/register", (request, response) -> {
            String clientIp = request.ip();
            logger.info("POST /api/auth/register - Registration attempt from IP: {}", clientIp);
            
            try {
                RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
                
                if (registerRequest == null || 
                    registerRequest.username == null || registerRequest.username.trim().isEmpty() ||
                    registerRequest.email == null || registerRequest.email.trim().isEmpty() ||
                    registerRequest.password == null || registerRequest.password.isEmpty()) {
                    logger.warn("POST /api/auth/register - Missing required fields from IP: {}", clientIp);
                    response.status(400);
                    return new ErrorResponse("Username, email, and password are required");
                }
                
                // Validate password strength
                if (registerRequest.password.length() < 6) {
                    logger.warn("POST /api/auth/register - Password too short for username: {} from IP: {}", 
                              registerRequest.username, clientIp);
                    response.status(400);
                    return new ErrorResponse("Password must be at least 6 characters long");
                }
                
                User.UserRole role = registerRequest.role != null ? 
                    User.UserRole.valueOf(registerRequest.role.toUpperCase()) : 
                    User.UserRole.CUSTOMER;
                
                logger.debug("POST /api/auth/register - Attempting registration for username: {}, email: {}, role: {} from IP: {}", 
                           registerRequest.username, registerRequest.email, role, clientIp);
                
                AuthService.AuthResult result = authService.register(
                    registerRequest.username,
                    registerRequest.email,
                    registerRequest.password,
                    registerRequest.firstName != null ? registerRequest.firstName : "",
                    registerRequest.lastName != null ? registerRequest.lastName : "",
                    role
                );
                
                if (result == null) {
                    logger.warn("POST /api/auth/register - Registration failed (duplicate username/email): {} from IP: {}", 
                              registerRequest.username, clientIp);
                    response.status(409);
                    return new ErrorResponse("User registration failed. Username or email may already exist.");
                }
                
                logger.info("POST /api/auth/register - Successful registration for user: {} (ID: {}) from IP: {}", 
                          result.getUser().getUsername(), result.getUser().getId(), clientIp);
                response.status(201);
                return new AuthResponse(result.getUser(), result.getToken());
                
            } catch (JsonSyntaxException e) {
                logger.error("POST /api/auth/register - Invalid JSON in request body from IP: {}", clientIp, e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (IllegalArgumentException e) {
                logger.error("POST /api/auth/register - Invalid role in request from IP: {}", clientIp, e);
                response.status(400);
                return new ErrorResponse("Invalid role: " + e.getMessage());
            } catch (Exception e) {
                logger.error("POST /api/auth/register - Unexpected error during registration from IP: {}", clientIp, e);
                response.status(500);
                return new ErrorResponse("Failed to register user");
            }
        }, JsonUtil::toJson);
        
        // POST /api/auth/login — Login and get JWT token
        post("/api/auth/login", (request, response) -> {
            String clientIp = request.ip();
            logger.info("POST /api/auth/login - Login attempt from IP: {}", clientIp);
            
            try {
                LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
                
                if (loginRequest == null || 
                    loginRequest.usernameOrEmail == null || loginRequest.usernameOrEmail.trim().isEmpty() ||
                    loginRequest.password == null || loginRequest.password.isEmpty()) {
                    logger.warn("POST /api/auth/login - Missing credentials from IP: {}", clientIp);
                    response.status(400);
                    return new ErrorResponse("Username/email and password are required");
                }
                
                logger.debug("POST /api/auth/login - Attempting login for: {} from IP: {}", 
                           loginRequest.usernameOrEmail, clientIp);
                
                AuthService.AuthResult result = authService.login(
                    loginRequest.usernameOrEmail,
                    loginRequest.password
                );
                
                if (result == null) {
                    logger.warn("POST /api/auth/login - Invalid credentials for: {} from IP: {}", 
                              loginRequest.usernameOrEmail, clientIp);
                    response.status(401);
                    return new ErrorResponse("Invalid username/email or password");
                }
                
                logger.info("POST /api/auth/login - Successful login for user: {} (ID: {}) from IP: {}", 
                          result.getUser().getUsername(), result.getUser().getId(), clientIp);
                
                String token = result.getToken();
                logger.debug("POST /api/auth/login - Token generated, length: {}", token != null ? token.length() : 0);
                logger.debug("POST /api/auth/login - Token preview: {}", token != null && token.length() > 20 ? token.substring(0, 20) + "..." : "null");
                
                response.status(200);
                AuthResponse authResponse = new AuthResponse(result.getUser(), token);
                logger.debug("POST /api/auth/login - AuthResponse created, token in response: {}", authResponse.getToken() != null);
                return authResponse;
                
            } catch (JsonSyntaxException e) {
                logger.error("POST /api/auth/login - Invalid JSON in request body from IP: {}", clientIp, e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("POST /api/auth/login - Unexpected error during login from IP: {}", clientIp, e);
                response.status(500);
                return new ErrorResponse("Failed to login");
            }
        }, JsonUtil::toJson);
        
        // GET /api/auth/me — Get current user info (requires authentication)
        get("/api/auth/me", (request, response) -> {
            String clientIp = request.ip();
            String authHeader = request.headers("Authorization");
            
            logger.info("GET /api/auth/me - Token validation request from IP: {}", clientIp);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("GET /api/auth/me - Missing or invalid Authorization header from IP: {}", clientIp);
                response.status(401);
                return new ErrorResponse("Authentication required");
            }
            
            User currentUser = request.attribute("currentUser");
            if (currentUser == null) {
                logger.warn("GET /api/auth/me - Invalid or expired token from IP: {}", clientIp);
                response.status(401);
                return new ErrorResponse("Invalid or expired token");
            }
            
            logger.debug("GET /api/auth/me - Valid token for user: {} (ID: {}) from IP: {}", 
                       currentUser.getUsername(), currentUser.getId(), clientIp);
            
            // Remove password hash if present
            currentUser.setPasswordHash(null);
            
            return currentUser;
        }, JsonUtil::toJson);
        
        // POST /api/auth/logout — Logout (client-side token removal)
        post("/api/auth/logout", (request, response) -> {
            logger.info("POST /api/auth/logout - User logout");
            // Logout is handled client-side by removing the token
            // This endpoint just confirms the action
            response.status(200);
            return new StatusResponse("Logged out successfully", true);
        }, JsonUtil::toJson);
    }
    
    /**
     * Register request DTO
     */
    private static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public String role;
    }
    
    /**
     * Login request DTO
     */
    private static class LoginRequest {
        public String usernameOrEmail;
        public String password;
    }
    
    /**
     * Authentication response DTO
     */
    public static class AuthResponse {
        private User user;
        private String token;
        
        public AuthResponse(User user, String token) {
            this.user = user;
            this.token = token;
            // Ensure password hash is never returned
            if (this.user != null) {
                this.user.setPasswordHash(null);
            }
            
            // Log for debugging
            logger.debug("AuthResponse created - User: {}, Token exists: {}", 
                        user != null ? user.getUsername() : "null", 
                        token != null);
        }
        
        public User getUser() {
            return user;
        }
        
        public String getToken() {
            return token;
        }
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
    
    /**
     * Status response class
     */
    public static class StatusResponse {
        private String message;
        private boolean success;
        
        public StatusResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
}

