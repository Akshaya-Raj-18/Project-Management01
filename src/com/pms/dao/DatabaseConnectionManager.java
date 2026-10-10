package com.pms.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.pms.exception.DatabaseOperationException;
import com.pms.util.AppLogger;

/**
 * Thread-Safe Singleton Database Connection Manager.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 3: Singleton Design Pattern (Double-Checked Locking, private constructor).
 * - Module 3: Exception Handling (try-catch-finally, nested try blocks, throw/throws).
 * - Module 4: Database Connectivity (JDBC: Connection, PreparedStatement, SQL transactions).
 */
public class DatabaseConnectionManager {

    // Module 3: Singleton Pattern - Volatile instance reference for Double-Checked Locking
    private static volatile DatabaseConnectionManager instance;

    // Database configuration
    private String databaseUrl;
    private Connection sharedConnection;

    // Module 3: Private constructor enforcing Singleton pattern
    private DatabaseConnectionManager() {
        this("pms_database.db");
    }

    private DatabaseConnectionManager(String dbFilePath) {
        this.databaseUrl = "jdbc:sqlite:" + dbFilePath;
        AppLogger.info("DatabaseConnectionManager initialized with URL: %s", this.databaseUrl);
    }

    /**
     * Module 3: Singleton Pattern Global Access Point with Double-Checked Locking.
     *
     * @return Single instance of DatabaseConnectionManager
     */
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Parameterized singleton configuration method (called during bootstrap with custom path).
     */
    public static synchronized void configure(String dbFilePath) {
        if (instance == null) {
            instance = new DatabaseConnectionManager(dbFilePath);
        } else {
            instance.setDatabasePath(dbFilePath);
        }
    }

    public synchronized void setDatabasePath(String dbFilePath) {
        closeConnection();
        this.databaseUrl = "jdbc:sqlite:" + dbFilePath;
    }

    /**
     * Obtains or reuses an active JDBC Connection.
     *
     * @return Active java.sql.Connection
     * @throws DatabaseOperationException if connection fails
     */
    public synchronized Connection getConnection() throws DatabaseOperationException {
        try {
            if (sharedConnection == null || sharedConnection.isClosed()) {
                // Ensure SQLite JDBC driver is loaded
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    AppLogger.warn("SQLite JDBC driver class not explicitly found, attempting DriverManager directly");
                }
                sharedConnection = DriverManager.getConnection(databaseUrl);
                
                // Enable foreign keys in SQLite
                try (Statement stmt = sharedConnection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                
                AppLogger.debug("Established new JDBC Connection to %s", databaseUrl);
            }
            return sharedConnection;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to acquire JDBC Connection to " + databaseUrl, e);
        }
    }

    /**
     * Module 4: Dynamic SQL Transaction Handling.
     * Begins an atomic transaction by disabling auto-commit.
     */
    public void beginTransaction() throws DatabaseOperationException {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            AppLogger.debug("Transaction begun: setAutoCommit(false)");
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to begin transaction", e);
        }
    }

    /**
     * Module 4: Dynamic SQL Transaction Handling.
     * Commits the active transaction and restores auto-commit mode.
     */
    public void commitTransaction() throws DatabaseOperationException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.commit();
            AppLogger.debug("Transaction committed successfully");
        } catch (SQLException e) {
            // Module 3: Nested try block inside catch to handle rollback failure safely
            AppLogger.error("Failed to commit transaction. Attempting rollback...", e);
            if (conn != null) {
                try {
                    conn.rollback();
                    AppLogger.warn("Transaction successfully rolled back after commit failure");
                } catch (SQLException rollbackEx) {
                    AppLogger.error("Nested rollback also failed", rollbackEx);
                }
            }
            throw new DatabaseOperationException("Transaction commit failed", e);
        } finally {
            // Module 3: Finally block ensuring auto-commit is safely reset
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    AppLogger.warn("Failed to reset auto-commit in finally block: %s", ex.getMessage());
                }
            }
        }
    }

    /**
     * Module 4: Dynamic SQL Transaction Handling.
     * Explicit transaction rollback.
     */
    public void rollbackTransaction() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.rollback();
                sharedConnection.setAutoCommit(true);
                AppLogger.info("Explicit transaction rollback completed");
            }
        } catch (SQLException e) {
            AppLogger.error("Error during transaction rollback: %s", e.getMessage());
        }
    }

    /**
     * Closes current connection safely.
     */
    public synchronized void closeConnection() {
        if (sharedConnection != null) {
            try {
                if (!sharedConnection.isClosed()) {
                    sharedConnection.close();
                    AppLogger.debug("Database connection closed cleanly");
                }
            } catch (SQLException e) {
                AppLogger.warn("Error closing database connection: %s", e.getMessage());
            } finally {
                sharedConnection = null;
            }
        }
    }
}
