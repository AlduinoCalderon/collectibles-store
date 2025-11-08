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
            logger.info("POST /api/auth/register - Registering new user");
            try {
                RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
                
                if (registerRequest == null || 
                    registerRequest.username == null || registerRequest.username.trim().isEmpty() ||
                    registerRequest.email == null || registerRequest.email.trim().isEmpty() ||
                    registerRequest.password == null || registerRequest.password.isEmpty()) {
                    response.status(400);
                    return new ErrorResponse("Username, email, and password are required");
                }
                
                // Validate password strength
                if (registerRequest.password.length() < 6) {
                    response.status(400);
                    return new ErrorResponse("Password must be at least 6 characters long");
                }
                
                User.UserRole role = registerRequest.role != null ? 
                    User.UserRole.valueOf(registerRequest.role.toUpperCase()) : 
                    User.UserRole.CUSTOMER;
                
                AuthService.AuthResult result = authService.register(
                    registerRequest.username,
                    registerRequest.email,
                    registerRequest.password,
                    registerRequest.firstName != null ? registerRequest.firstName : "",
                    registerRequest.lastName != null ? registerRequest.lastName : "",
                    role
                );
                
                if (result == null) {
                    response.status(409);
                    return new ErrorResponse("User registration failed. Username or email may already exist.");
                }
                
                response.status(201);
                return new AuthResponse(result.getUser(), result.getToken());
                
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (IllegalArgumentException e) {
                logger.error("Invalid role in request", e);
                response.status(400);
                return new ErrorResponse("Invalid role: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error during registration", e);
                response.status(500);
                return new ErrorResponse("Failed to register user");
            }
        }, JsonUtil::toJson);
        
        // POST /api/auth/login — Login and get JWT token
        post("/api/auth/login", (request, response) -> {
            logger.info("POST /api/auth/login - User login attempt");
            try {
                LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
                
                if (loginRequest == null || 
                    loginRequest.usernameOrEmail == null || loginRequest.usernameOrEmail.trim().isEmpty() ||
                    loginRequest.password == null || loginRequest.password.isEmpty()) {
                    response.status(400);
                    return new ErrorResponse("Username/email and password are required");
                }
                
                AuthService.AuthResult result = authService.login(
                    loginRequest.usernameOrEmail,
                    loginRequest.password
                );
                
                if (result == null) {
                    response.status(401);
                    return new ErrorResponse("Invalid username/email or password");
                }
                
                response.status(200);
                return new AuthResponse(result.getUser(), result.getToken());
                
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("Error during login", e);
                response.status(500);
                return new ErrorResponse("Failed to login");
            }
        }, JsonUtil::toJson);
        
        // GET /api/auth/me — Get current user info (requires authentication)
        get("/api/auth/me", (request, response) -> {
            logger.info("GET /api/auth/me - Getting current user info");
            
            User currentUser = request.attribute("currentUser");
            if (currentUser == null) {
                response.status(401);
                return new ErrorResponse("Authentication required");
            }
            
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

