package com.pms.exception;

/**
 * Thrown when a database or JDBC level failure occurs.
 * (Module 3: Custom Exception with cause chaining)
 */
public class DatabaseOperationException extends PMSException {

    private final String sqlQuery;

    public DatabaseOperationException(String message, Throwable cause) {
        super("ERR_DATABASE_OP", message, cause);
        this.sqlQuery = null;
    }

    public DatabaseOperationException(String message, String sqlQuery, Throwable cause) {
        super("ERR_DATABASE_OP", message + (sqlQuery != null ? " [SQL: " + sqlQuery + "]" : ""), cause);
        this.sqlQuery = sqlQuery;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }
}
