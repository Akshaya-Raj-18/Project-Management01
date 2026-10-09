package com.pms.exception;

/**
 * Base custom application exception.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 3: Exception Handling, Custom Exceptions, Superclass-Subclass
 *   exception hierarchy, constructor call chaining with super().
 */
public class PMSException extends Exception {

    private final String errorCode;

    public PMSException(String message) {
        super(message);
        this.errorCode = "PMS_GENERIC_ERROR";
    }

    public PMSException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PMS_GENERIC_ERROR";
    }

    public PMSException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PMSException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
