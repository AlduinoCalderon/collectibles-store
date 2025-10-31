package com.spark.collectibles.database;

import com.spark.collectibles.config.EnvironmentConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pool
 * 
 * This class follows the Singleton pattern and manages database connections
 * using HikariCP for optimal performance and resource management.
 */
public class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static DatabaseConnectionManager instance;
    private HikariDataSource dataSource;
    
    private DatabaseConnectionManager() {
        initializeDataSource();
    }
    
    /**
     * Get singleton instance
     * @return DatabaseConnectionManager instance
     */
    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            logger.info("Creating new DatabaseConnectionManager instance");
            instance = new DatabaseConnectionManager();
        } else {
            // Check if dataSource is still valid
            if (instance.dataSource == null || instance.dataSource.isClosed()) {
                logger.warn("Existing instance found but dataSource is closed. Cleaning up and creating new instance.");
                // Clean up the old instance first (if not already closed)
                if (instance.dataSource != null) {
                    try {
                        if (!instance.dataSource.isClosed()) {
                            instance.dataSource.close();
                        }
                    } catch (Exception e) {
                        logger.error("Error closing old dataSource", e);
                    }
                }
                instance = new DatabaseConnectionManager();
            } else {
                // Verify only one pool exists
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning existing DatabaseConnectionManager instance. Pool: {}", 
                        instance.dataSource.getPoolName());
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize HikariCP data source
     */
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Database connection settings
            config.setJdbcUrl(EnvironmentConfig.getDbUrl());
            config.setUsername(EnvironmentConfig.getDbUsername());
            config.setPassword(EnvironmentConfig.getDbPassword());
            
            // Connection pool settings
            int maxPoolSize = EnvironmentConfig.getDbMaxConnections();
            int minIdle = EnvironmentConfig.getDbMinConnections();
            
            // Ensure max pool size doesn't exceed 3 for free MySQL instances
            if (maxPoolSize > 3) {
                logger.warn("Max pool size {} exceeds recommended limit of 3 for free MySQL. Reducing to 3.", maxPoolSize);
                maxPoolSize = 3;
            }
            // Ensure min idle doesn't exceed max pool size
            if (minIdle > maxPoolSize) {
                logger.warn("Min idle {} exceeds max pool size {}. Reducing min idle to {}.", minIdle, maxPoolSize, maxPoolSize);
                minIdle = maxPoolSize;
            }
            
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minIdle);
            config.setConnectionTimeout(EnvironmentConfig.getDbConnectionTimeout());
            
            // Connection pool properties - aggressive cleanup for limited connections
            config.setPoolName("CollectiblesStorePool");
            config.setConnectionTestQuery("SELECT 1");
            config.setLeakDetectionThreshold(30000); // 30 seconds - reduced for faster leak detection
            
            // Close connections that are idle for more than 10 minutes
            config.setIdleTimeout(600000); // 10 minutes in milliseconds
            // Close connections that have been open for more than 30 minutes
            config.setMaxLifetime(1800000); // 30 minutes in milliseconds
            
            // Validate connections before use
            config.setValidationTimeout(5000); // 5 seconds
            config.setKeepaliveTime(300000); // 5 minutes - send keepalive packets
            
            // MySQL specific settings for performance optimization
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                logger.info("Database connection established successfully");
                logger.info("Connection pool configured: Max={}, MinIdle={}, IdleTimeout={}ms, MaxLifetime={}ms", 
                    maxPoolSize, minIdle, config.getIdleTimeout(), config.getMaxLifetime());
            }
            
        } catch (SQLException e) {
            logger.error("Failed to initialize database connection", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get database connection from pool
     * @return Connection instance
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not available");
        }
        
        // Log pool stats periodically for debugging
        if (logger.isDebugEnabled()) {
            logger.debug("Getting connection. Pool stats: {}", getPoolStats());
        }
        
        return dataSource.getConnection();
    }
    
    /**
     * Get data source
     * @return DataSource instance
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Check if data source is available
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Get connection pool statistics
     * @return Connection pool stats as string
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "DataSource not initialized";
        }
        
        return String.format(
            "Pool: %s, Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getPoolName(),
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
    
    /**
     * Close data source and cleanup resources
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database connection pool");
            dataSource.close();
        }
    }
    
    /**
     * Shutdown hook for graceful cleanup
     */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down database connection manager");
            close();
        }));
    }
}
