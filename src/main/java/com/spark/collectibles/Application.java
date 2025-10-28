package com.spark.collectibles;

import com.spark.collectibles.config.EnvironmentConfig;
import com.spark.collectibles.database.DatabaseConnectionManager;
import com.spark.collectibles.database.DatabaseMigrationManager;
import com.spark.collectibles.routes.ProductRoutes;
import com.spark.collectibles.service.ProductService;
import com.spark.collectibles.util.JsonUtil;
import com.spark.collectibles.util.ErrorHandler;
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
            
            // Set port from configuration
            port(EnvironmentConfig.getAppPort());
            
            // Enable CORS for all routes
            enableCORS();
            
            // Set up JSON response type
            after((request, response) -> {
                response.type("application/json");
            });
            
            // Initialize routes
            ProductRoutes.initialize(productService);
            
            // API Documentation routes
            setupApiDocumentation();
            
            // Health check endpoint
            setupHealthEndpoint();
            
            // Global exception handler
            exception(Exception.class, (exception, request, response) -> {
                ErrorHandler.ErrorResponse errorResponse = ErrorHandler.handleException(exception, request, response);
                response.status(errorResponse.getStatusCode());
                response.body(JsonUtil.toJson(errorResponse));
            });
            
            // 404 handler
            notFound((request, response) -> {
                ErrorHandler.ErrorResponse errorResponse = ErrorHandler.createNotFoundError("Endpoint");
                response.status(errorResponse.getStatusCode());
                return JsonUtil.toJson(errorResponse);
            });
            
            logger.info("Collectibles Store API started on port {}", EnvironmentConfig.getAppPort());
            logger.info("API Base Path: {}", EnvironmentConfig.getApiBasePath());
            logger.info("Environment: {}", EnvironmentConfig.getAppEnv());
            
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
            response.type("text/html");
            try {
                // Read the Scalar HTML from resources
                java.io.InputStream inputStream = Application.class.getClassLoader()
                    .getResourceAsStream("static/scalar.html");
                if (inputStream != null) {
                    return new String(inputStream.readAllBytes());
                } else {
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
