package com.spark.collectibles.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Environment configuration manager
 * 
 * This class handles loading configuration from environment variables
 * and provides a centralized way to access configuration values.
 */
public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    private static final Properties properties = new Properties();
    
    static {
        loadConfiguration();
    }
    
    /**
     * Load configuration from environment variables and properties file
     */
    private static void loadConfiguration() {
        // Load from properties file if exists
        try (InputStream input = EnvironmentConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from application.properties");
            }
        } catch (IOException e) {
            logger.warn("Could not load application.properties, using environment variables only");
        }
        
        // Override with environment variables
        loadFromEnvironment();
    }
    
    /**
     * Load configuration from environment variables
     */
    private static void loadFromEnvironment() {
        // Database configuration
    setProperty("db.host", "DB_HOST", "localhost");
    setProperty("db.port", "DB_PORT", "3306");
    setProperty("db.name", "DB_NAME", "collectibles_store");
    setProperty("db.username", "DB_USERNAME", "root");
    setProperty("db.password", "DB_PASSWORD", "password");
        
        // Application configuration
        setProperty("app.port", "APP_PORT", "4567");
        setProperty("app.env", "APP_ENV", "development");
        setProperty("app.log.level", "LOG_LEVEL", "INFO");
        
        // Database connection pool
        // Reduced defaults for free MySQL instances (max 5 connections total)
        setProperty("db.max.connections", "DB_MAX_CONNECTIONS", "3");
        setProperty("db.min.connections", "DB_MIN_CONNECTIONS", "1");
        setProperty("db.connection.timeout", "DB_CONNECTION_TIMEOUT", "30000");
        
        // API configuration
        setProperty("api.base.path", "API_BASE_PATH", "/api");
        
        logger.info("Configuration loaded successfully");
    }
    
    /**
     * Set property from environment variable with fallback to default value
     */
    private static void setProperty(String key, String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            properties.setProperty(key, value);
        } else {
            properties.setProperty(key, defaultValue);
        }
    }
    
    // Database configuration getters
    public static String getDbHost() {
        return properties.getProperty("db.host");
    }
    
    public static int getDbPort() {
        return Integer.parseInt(properties.getProperty("db.port"));
    }
    
    public static String getDbName() {
        return properties.getProperty("db.name");
    }
    
    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }
    
    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }
    
    public static String getDbUrl() {
    return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", getDbHost(), getDbPort(), getDbName());
    }
    
    // Application configuration getters
    public static int getAppPort() {
        return Integer.parseInt(properties.getProperty("app.port"));
    }
    
    public static String getAppEnv() {
        return properties.getProperty("app.env");
    }
    
    public static String getLogLevel() {
        return properties.getProperty("app.log.level");
    }
    
    // Database connection pool getters
    public static int getDbMaxConnections() {
        return Integer.parseInt(properties.getProperty("db.max.connections"));
    }
    
    public static int getDbMinConnections() {
        return Integer.parseInt(properties.getProperty("db.min.connections"));
    }
    
    public static int getDbConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("db.connection.timeout"));
    }
    
    // API configuration getters
    public static String getApiBasePath() {
        return properties.getProperty("api.base.path");
    }
    
    /**
     * Check if running in development mode
     */
    public static boolean isDevelopment() {
        return "development".equalsIgnoreCase(getAppEnv());
    }
    
    /**
     * Check if running in production mode
     */
    public static boolean isProduction() {
        return "production".equalsIgnoreCase(getAppEnv());
    }
    
    /**
     * Get all configuration as properties
     */
    public static Properties getAllProperties() {
        return new Properties(properties);
    }
}
