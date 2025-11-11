package com.spark.collectibles;

import com.spark.collectibles.config.EnvironmentConfig;
import com.spark.collectibles.database.DatabaseConnectionManager;
import com.spark.collectibles.database.DatabaseMigrationManager;
import com.spark.collectibles.routes.AuthRoutes;
import com.spark.collectibles.routes.ProductRoutes;
import com.spark.collectibles.routes.UserRoutes;
import com.spark.collectibles.routes.ViewRoutes;
import com.spark.collectibles.service.AuthService;
import com.spark.collectibles.service.ProductService;
import com.spark.collectibles.service.UserService;
import com.spark.collectibles.util.AuthFilter;
import com.spark.collectibles.util.JsonUtil;
import com.spark.collectibles.util.ErrorHandler;
import com.spark.collectibles.exception.ExceptionHandler;
import com.spark.collectibles.model.User;
import com.spark.collectibles.websocket.PriceWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

/**
 * Main application class for the Collectibles Store API
 * 
 * This class initializes the Spark web framework, database connections,
 * and sets up all the routes for the RESTful API endpoints.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            // Initialize database
            initializeDatabase();
            
            // Initialize services
            ProductService productService = new ProductService();
            UserService userService = new UserService();
            AuthService authService = new AuthService();
            AuthFilter authFilter = new AuthFilter(authService);
            
            // Set port from configuration
            port(EnvironmentConfig.getAppPort());
            
            // Configure WebSocket endpoint for real-time price updates
            // Set AuthService for WebSocket authentication
            PriceWebSocketHandler.setAuthService(authService);
            webSocket("/ws/prices", PriceWebSocketHandler.class);
            
            // Configure static files (MUST be before any route mapping)
            // Spark serves files from resources/static when using location("/static")
            spark.Spark.staticFiles.location("/static");
            logger.info("Static files configured at /static");
            
            // Explicitly serve auth.js to ensure it's accessible
            get("/static/auth.js", (request, response) -> {
                response.type("application/javascript");
                response.header("Cache-Control", "no-cache, no-store, must-revalidate");
                try {
                    java.io.InputStream inputStream = Application.class.getClassLoader()
                        .getResourceAsStream("static/auth.js");
                    if (inputStream != null) {
                        String js = new String(inputStream.readAllBytes());
                        logger.debug("Serving auth.js (length: {})", js.length());
                        return js;
                    } else {
                        logger.error("auth.js file not found in resources/static");
                        response.status(404);
                        return "console.error('auth.js not found');";
                    }
                } catch (Exception e) {
                    logger.error("Error serving auth.js", e);
                    response.status(500);
                    return "console.error('Error loading auth.js');";
                }
            });
            
            // Add logging for static file requests
            before("/static/*", (request, response) -> {
                logger.debug("Static file request: {}", request.pathInfo());
            });
            
            // Enable CORS for all routes
            enableCORS();
            
            // Set up JSON response type only for API routes (exclude /api/docs which serves HTML)
            after("/api/*", (request, response) -> {
                // Don't override Content-Type for /api/docs (it serves HTML)
                if (!request.pathInfo().equals("/api/docs")) {
                    response.type("application/json");
                }
            });
            
            // Initialize authentication routes (public)
            AuthRoutes.initialize(authService);
            
            // Initialize API routes
            ProductRoutes.initialize(productService);
            
            // Initialize user management routes (protected - admin only)
            setupUserRoutes(userService, authFilter);
            
            // Protect admin product management routes (POST, PUT, DELETE require ADMIN role)
            setupProtectedProductRoutes(authFilter);
            
            // Initialize view routes (must come after API routes)
            ViewRoutes.initialize(productService);
            
            // API Documentation routes
            setupApiDocumentation();
            
            // Health check endpoint
            setupHealthEndpoint();
            
            // Global exception handler
            exception(Exception.class, (exception, request, response) -> {
                // Check if it's an API route
                if (request.pathInfo().startsWith("/api")) {
                    ErrorHandler.ErrorResponse errorResponse = ExceptionHandler.handleGeneric(exception, request, response);
                    response.status(errorResponse.getStatusCode());
                    response.type("application/json");
                    response.body(JsonUtil.toJson(errorResponse));
                } else {
                    // For view routes, redirect to error page
                    ErrorHandler.ErrorResponse errorResponse = ExceptionHandler.handleGeneric(exception, request, response);
                    response.status(errorResponse.getStatusCode());
                    response.redirect("/error/" + errorResponse.getStatusCode());
                }
            });
            
            // 404 handler
            notFound((request, response) -> {
                if (request.pathInfo().startsWith("/api")) {
                    // API route - return JSON
                    ErrorHandler.ErrorResponse errorResponse = ErrorHandler.createNotFoundError("Endpoint");
                    response.status(errorResponse.getStatusCode());
                    response.type("application/json");
                    return JsonUtil.toJson(errorResponse);
                } else {
                    // View route - redirect to error page
                    response.status(404);
                    response.redirect("/error/404");
                    return null;
                }
            });
            
            // Initialize Spark (required for WebSocket to work)
            init();
            
            logger.info("Collectibles Store API started on port {}", EnvironmentConfig.getAppPort());
            logger.info("API Base Path: {}", EnvironmentConfig.getApiBasePath());
            logger.info("Environment: {}", EnvironmentConfig.getAppEnv());
            logger.info("WebSocket endpoint available at /ws/prices");
            logger.info("Authentication enabled - JWT-based authentication");
            logger.info("Protected routes require authentication with ADMIN role");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }
    
    /**
     * Initialize database connection and run migrations
     */
    private static void initializeDatabase() {
        try {
            logger.info("Initializing database connection...");
            
            // Initialize database connection manager
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            connectionManager.registerShutdownHook();
            
            // Run database migrations
            DatabaseMigrationManager migrationManager = DatabaseMigrationManager.getInstance();
            int migrationsApplied = migrationManager.migrate();
            
            if (migrationsApplied > 0) {
                logger.info("Applied {} database migrations", migrationsApplied);
            } else {
                logger.info("Database is up to date");
            }
            
            // Validate migrations
            if (!migrationManager.validate()) {
                throw new RuntimeException("Database migration validation failed");
            }
            
            logger.info("Database initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Setup API documentation routes
     */
    private static void setupApiDocumentation() {
        // Serve OpenAPI specification
        get("/api/openapi.json", (request, response) -> {
            response.type("application/json");
            try {
                // Read the OpenAPI spec from resources
                java.io.InputStream inputStream = Application.class.getClassLoader()
                    .getResourceAsStream("openapi.json");
                if (inputStream != null) {
                    return new String(inputStream.readAllBytes());
                } else {
                    response.status(404);
                    return JsonUtil.toJson(new ErrorResponse("OpenAPI specification not found"));
                }
            } catch (Exception e) {
                logger.error("Error serving OpenAPI specification", e);
                response.status(500);
                return JsonUtil.toJson(new ErrorResponse("Failed to load OpenAPI specification"));
            }
        });
        
        // Serve Scalar API documentation
        get("/api/docs", (request, response) -> {
            response.type("text/html; charset=utf-8");
            response.header("Cache-Control", "no-cache, no-store, must-revalidate");
            response.header("Pragma", "no-cache");
            response.header("Expires", "0");
            try {
                // Read the Scalar HTML from resources
                java.io.InputStream inputStream = Application.class.getClassLoader()
                    .getResourceAsStream("static/scalar.html");
                if (inputStream != null) {
                    String html = new String(inputStream.readAllBytes());
                    logger.info("Serving Scalar API documentation HTML (length: {})", html.length());
                    return html;
                } else {
                    logger.error("Scalar HTML file not found in resources");
                    response.status(404);
                    return "<h1>API Documentation not found</h1>";
                }
            } catch (Exception e) {
                logger.error("Error serving API documentation", e);
                response.status(500);
                return "<h1>Error loading API documentation</h1>";
            }
        });
        
        logger.info("API Documentation available at /api/docs");
        logger.info("OpenAPI Specification available at /api/openapi.json");
    }
    
    /**
     * Setup health check endpoint
     */
    private static void setupHealthEndpoint() {
        get("/api/health", (request, response) -> {
            response.type("application/json");
            try {
                DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
                boolean dbHealthy = dbManager.isAvailable();
                
                java.util.Map<String, Object> health = new java.util.HashMap<>();
                health.put("status", dbHealthy ? "UP" : "DOWN");
                health.put("timestamp", java.time.LocalDateTime.now().toString());
                health.put("database", dbHealthy ? "connected" : "disconnected");
                health.put("environment", EnvironmentConfig.getAppEnv());
                
                if (dbHealthy) {
                    health.put("poolStats", dbManager.getPoolStats());
                }
                
                response.status(dbHealthy ? 200 : 503);
                return JsonUtil.toJson(health);
            } catch (Exception e) {
                logger.error("Health check failed", e);
                response.status(503);
                java.util.Map<String, Object> health = new java.util.HashMap<>();
                health.put("status", "DOWN");
                health.put("timestamp", java.time.LocalDateTime.now().toString());
                health.put("error", e.getMessage());
                return JsonUtil.toJson(health);
            }
        });
        
        logger.info("Health check endpoint available at /api/health");
    }
    
    /**
     * Setup user management routes with authentication
     */
    private static void setupUserRoutes(UserService userService, AuthFilter authFilter) {
        // Protect all user management routes with ADMIN role requirement
        before("/api/users/*", authFilter.requireRole(User.UserRole.ADMIN));
        before("/api/users", authFilter.requireRole(User.UserRole.ADMIN));
        
        // Initialize user routes
        UserRoutes.initialize(userService);
        
        logger.info("User management routes initialized (ADMIN access required)");
    }
    
    /**
     * Setup protected product management routes
     * Public routes (GET) remain accessible, but admin routes (POST, PUT, DELETE) require authentication
     */
    private static void setupProtectedProductRoutes(AuthFilter authFilter) {
        // Protect product creation, update, and deletion with ADMIN role
        before("/api/products", (request, response) -> {
            // Only require auth for POST, PUT, DELETE methods
            if ("POST".equals(request.requestMethod())) {
                authFilter.requireRole(User.UserRole.ADMIN).handle(request, response);
            }
        });
        
        before("/api/products/:id", (request, response) -> {
            // Only require auth for PUT, DELETE methods (GET is public)
            if ("PUT".equals(request.requestMethod()) || "DELETE".equals(request.requestMethod())) {
                authFilter.requireRole(User.UserRole.ADMIN).handle(request, response);
            }
        });
        
        // Protect restore and hard delete routes
        before("/api/products/:id/restore", authFilter.requireRole(User.UserRole.ADMIN));
        before("/api/products/:id/hard", authFilter.requireRole(User.UserRole.ADMIN));
        
        logger.info("Product management routes protected (ADMIN access required for modifications)");
    }
    
    /**
     * Enable CORS (Cross-Origin Resource Sharing) for all routes
     */
    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            
            return "OK";
        });
        
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        });
    }
    
    /**
     * Simple error response class
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
