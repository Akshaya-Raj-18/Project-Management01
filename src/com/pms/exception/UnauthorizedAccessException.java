package com.pms.exception;

/**
 * Thrown when a user attempts an action disallowed by their role permissions.
 * (Module 3: Custom Exception, Role-based security validation)
 */
public class UnauthorizedAccessException extends PMSException {

    private final String username;
    private final String attemptedAction;

    public UnauthorizedAccessException(String username, String attemptedAction) {
        super("ERR_UNAUTHORIZED", 
              String.format("User '%s' is not authorized to perform action: '%s'. Required: Manager Role.",
                      username, attemptedAction));
        this.username = username;
        this.attemptedAction = attemptedAction;
    }

    public UnauthorizedAccessException(String message) {
        super("ERR_UNAUTHORIZED", message);
        this.username = null;
        this.attemptedAction = null;
    }

    public String getUsername() {
        return username;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
