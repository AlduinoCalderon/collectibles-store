package com.spark.collectibles.routes;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spark.collectibles.model.User;
import com.spark.collectibles.service.UserService;
import com.spark.collectibles.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

/**
 * Route definitions for user management API endpoints
 * 
 * This class defines all the RESTful routes for user CRUD operations
 * following the requirements specified in the project documentation.
 */
public class UserRoutes {
    private static final Logger logger = LoggerFactory.getLogger(UserRoutes.class);
    private static final Gson gson = new Gson();
    
    /**
     * Initialize all user-related routes
     * @param userService UserService instance for business logic
     */
    public static void initialize(UserService userService) {
        // GET /users — Retrieve the list of all users
        get("/users", (request, response) -> {
            logger.info("GET /users - Retrieving all users");
            try {
                return userService.getAllUsers();
            } catch (Exception e) {
                logger.error("Error retrieving users", e);
                response.status(500);
                return new ErrorResponse("Failed to retrieve users");
            }
        }, JsonUtil::toJson);
        
        // GET /users/:id — Retrieve a user by the given ID
        get("/users/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("GET /users/{} - Retrieving user by ID", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("User ID is required");
            }
            
            User user = userService.getUserById(id);
            if (user == null) {
                response.status(404);
                return new ErrorResponse("User not found");
            }
            
            return user;
        }, JsonUtil::toJson);
        
        // POST /users/:id — Add a user
        post("/users/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("POST /users/{} - Creating user", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("User ID is required");
            }
            
            try {
                User user = gson.fromJson(request.body(), User.class);
                if (user == null) {
                    response.status(400);
                    return new ErrorResponse("Invalid user data");
                }
                
                // Set the ID from the URL parameter
                user.setId(id);
                
                User createdUser = userService.createUser(user);
                if (createdUser == null) {
                    response.status(409);
                    return new ErrorResponse("User already exists or invalid data");
                }
                
                response.status(201);
                return createdUser;
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("Error creating user", e);
                response.status(500);
                return new ErrorResponse("Failed to create user");
            }
        }, JsonUtil::toJson);
        
        // PUT /users/:id — Edit a specific user
        put("/users/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("PUT /users/{} - Updating user", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("User ID is required");
            }
            
            try {
                User user = gson.fromJson(request.body(), User.class);
                if (user == null) {
                    response.status(400);
                    return new ErrorResponse("Invalid user data");
                }
                
                // Set the ID from the URL parameter
                user.setId(id);
                
                User updatedUser = userService.updateUser(id, user);
                if (updatedUser == null) {
                    response.status(404);
                    return new ErrorResponse("User not found or invalid data");
                }
                
                return updatedUser;
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in request body", e);
                response.status(400);
                return new ErrorResponse("Invalid JSON format");
            } catch (Exception e) {
                logger.error("Error updating user", e);
                response.status(500);
                return new ErrorResponse("Failed to update user");
            }
        }, JsonUtil::toJson);
        
        // OPTIONS /users/:id — Check whether a user with the given ID exists
        options("/users/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("OPTIONS /users/{} - Checking if user exists", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("User ID is required");
            }
            
            boolean exists = userService.userExists(id);
            response.status(exists ? 200 : 404);
            return new StatusResponse(exists ? "User exists" : "User not found", exists);
        }, JsonUtil::toJson);
        
        // DELETE /users/:id — Delete a specific user
        delete("/users/:id", (request, response) -> {
            String id = request.params(":id");
            logger.info("DELETE /users/{} - Deleting user", id);
            
            if (id == null || id.trim().isEmpty()) {
                response.status(400);
                return new ErrorResponse("User ID is required");
            }
            
            boolean deleted = userService.deleteUser(id);
            if (!deleted) {
                response.status(404);
                return new ErrorResponse("User not found");
            }
            
            response.status(200);
            return new StatusResponse("User deleted successfully", true);
        }, JsonUtil::toJson);
        
        // Additional helpful endpoints
        
        // GET /users/search?q=query — Search users
        get("/users/search", (request, response) -> {
            String query = request.queryParams("q");
            logger.info("GET /users/search?q={} - Searching users", query);
            
            try {
                return userService.searchUsers(query);
            } catch (Exception e) {
                logger.error("Error searching users", e);
                response.status(500);
                return new ErrorResponse("Failed to search users");
            }
        }, JsonUtil::toJson);
        
        // GET /users/role/:role — Get users by role
        get("/users/role/:role", (request, response) -> {
            String roleParam = request.params(":role");
            logger.info("GET /users/role/{} - Getting users by role", roleParam);
            
            try {
                User.UserRole role = User.UserRole.valueOf(roleParam.toUpperCase());
                return userService.getUsersByRole(role);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return new ErrorResponse("Invalid role: " + roleParam);
            } catch (Exception e) {
                logger.error("Error getting users by role", e);
                response.status(500);
                return new ErrorResponse("Failed to get users by role");
            }
        }, JsonUtil::toJson);
        
        // GET /users/stats — Get user statistics
        get("/users/stats", (request, response) -> {
            logger.info("GET /users/stats - Getting user statistics");
            
            try {
                int totalUsers = userService.getUserCount();
                int activeUsers = userService.getActiveUsers().size();
                
                return new UserStatsResponse(totalUsers, activeUsers);
            } catch (Exception e) {
                logger.error("Error getting user statistics", e);
                response.status(500);
                return new ErrorResponse("Failed to get user statistics");
            }
        }, JsonUtil::toJson);
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
    
    /**
     * User statistics response class
     */
    public static class UserStatsResponse {
        private int totalUsers;
        private int activeUsers;
        
        public UserStatsResponse(int totalUsers, int activeUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
        }
        
        public int getTotalUsers() {
            return totalUsers;
        }
        
        public int getActiveUsers() {
            return activeUsers;
        }
    }
}
