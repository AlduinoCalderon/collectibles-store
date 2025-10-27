package com.spark.collectibles;

import com.spark.collectibles.config.EnvironmentConfig;
import com.spark.collectibles.database.DatabaseConnectionManager;
import com.spark.collectibles.database.DatabaseMigrationManager;
import com.spark.collectibles.routes.ProductRoutes;
import com.spark.collectibles.service.ProductService;
import com.spark.collectibles.util.JsonUtil;
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
            
            // Global exception handler
            exception(Exception.class, (exception, request, response) -> {
                logger.error("Unhandled exception: ", exception);
                response.status(500);
                response.body(JsonUtil.toJson(new ErrorResponse("Internal server error")));
            });
            
            // 404 handler
            notFound((request, response) -> {
                response.status(404);
                return JsonUtil.toJson(new ErrorResponse("Endpoint not found"));
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
