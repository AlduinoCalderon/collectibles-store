package com.spark.collectibles.database;

import com.spark.collectibles.config.EnvironmentConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database migration manager using Flyway
 * 
 * This class handles database schema migrations and ensures
 * the database is up to date with the latest schema changes.
 */
public class DatabaseMigrationManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationManager.class);
    private static DatabaseMigrationManager instance;
    private Flyway flyway;
    
    private DatabaseMigrationManager() {
        initializeFlyway();
    }
    
    /**
     * Get singleton instance
     * @return DatabaseMigrationManager instance
     */
    public static synchronized DatabaseMigrationManager getInstance() {
        if (instance == null) {
            instance = new DatabaseMigrationManager();
        }
        return instance;
    }
    
    /**
     * Initialize Flyway configuration
     */
    private void initializeFlyway() {
        try {
            FluentConfiguration config = Flyway.configure()
                .dataSource(
                    EnvironmentConfig.getDbUrl(),
                    EnvironmentConfig.getDbUsername(),
                    EnvironmentConfig.getDbPassword()
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .cleanDisabled(true);
            
            flyway = new Flyway(config);
            logger.info("Flyway migration manager initialized");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Flyway migration manager", e);
            throw new RuntimeException("Database migration initialization failed", e);
        }
    }
    
    /**
     * Run database migrations
     * @return number of migrations applied
     */
    public int migrate() {
        try {
            logger.info("Starting database migration...");
            logger.info("Migration location: classpath:db/migration");
            
            // Check for failed migrations and repair if needed
            org.flywaydb.core.api.MigrationInfo[] allMigrations = flyway.info().all();
            boolean hasFailedMigrations = false;
            for (org.flywaydb.core.api.MigrationInfo info : allMigrations) {
                if (info.getState() == org.flywaydb.core.api.MigrationState.FAILED) {
                    if (!hasFailedMigrations) {
                        logger.warn("Found failed migrations, attempting repair...");
                        hasFailedMigrations = true;
                    }
                    logger.warn("  - Failed migration: {} - {}", info.getVersion(), info.getDescription());
                }
            }
            if (hasFailedMigrations) {
                flyway.repair();
                logger.info("Flyway repair completed successfully");
            }
            
            // Check pending migrations
            org.flywaydb.core.api.MigrationInfo[] pending = flyway.info().pending();
            logger.info("Found {} pending migrations", pending.length);
            for (org.flywaydb.core.api.MigrationInfo info : pending) {
                logger.info("  - Pending migration: {} - {}", info.getVersion(), info.getDescription());
            }
            
            // Execute migrations
            org.flywaydb.core.api.output.MigrateResult result = flyway.migrate();
            int migrationsApplied = result.migrationsExecuted;
            
            if (migrationsApplied > 0) {
                logger.info("Successfully applied {} database migrations", migrationsApplied);
            } else {
                logger.info("Database is up to date, no migrations needed");
            }
            
            // Log current schema version
            org.flywaydb.core.api.MigrationInfo current = flyway.info().current();
            if (current != null) {
                logger.info("Current schema version: {}", current.getVersion());
            }
            
            return migrationsApplied;
        } catch (Exception e) {
            logger.error("Database migration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database migration failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if database is up to date
     * @return true if up to date, false otherwise
     */
    public boolean isUpToDate() {
        try {
            return flyway.info().pending().length == 0;
        } catch (Exception e) {
            logger.error("Failed to check migration status", e);
            return false;
        }
    }
    
    /**
     * Get migration info
     * @return migration info as string
     */
    public String getMigrationInfo() {
        try {
            return flyway.info().toString();
        } catch (Exception e) {
            logger.error("Failed to get migration info", e);
            return "Migration info unavailable";
        }
    }
    
    /**
     * Validate migrations
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        try {
            flyway.validate();
            logger.info("Database migrations are valid");
            return true;
        } catch (Exception e) {
            logger.error("Database migration validation failed", e);
            return false;
        }
    }
    
    /**
     * Clean database (use with caution - only in development)
     */
    public void clean() {
        if (EnvironmentConfig.isDevelopment()) {
            try {
                flyway.clean();
                logger.warn("Database cleaned - this should only be used in development");
            } catch (Exception e) {
                logger.error("Failed to clean database", e);
                throw new RuntimeException("Database clean failed", e);
            }
        } else {
            logger.warn("Database clean is disabled in production environment");
        }
    }
}
