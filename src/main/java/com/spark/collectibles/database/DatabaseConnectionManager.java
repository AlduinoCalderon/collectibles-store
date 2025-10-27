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
            instance = new DatabaseConnectionManager();
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
            config.setMaximumPoolSize(EnvironmentConfig.getDbMaxConnections());
            config.setMinimumIdle(EnvironmentConfig.getDbMinConnections());
            config.setConnectionTimeout(EnvironmentConfig.getDbConnectionTimeout());
            
            // Connection pool properties
            config.setPoolName("CollectiblesStorePool");
            config.setConnectionTestQuery("SELECT 1");
            config.setLeakDetectionThreshold(60000); // 60 seconds
            
            // PostgreSQL specific settings
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
