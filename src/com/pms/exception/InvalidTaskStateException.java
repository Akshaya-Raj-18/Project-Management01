package com.pms.exception;

/**
 * Thrown when an illegal state transition occurs for a task
 * (e.g., trying to transition directly from TODO to COMPLETED without review, etc.)
 * (Module 3: Custom Exception)
 */
public class InvalidTaskStateException extends PMSException {

    private final String currentState;
    private final String attemptedState;

    public InvalidTaskStateException(String currentState, String attemptedState, String reason) {
        super("ERR_INVALID_TASK_STATE", 
              String.format("Invalid state transition from '%s' to '%s': %s",
                      currentState, attemptedState, reason));
        this.currentState = currentState;
        this.attemptedState = attemptedState;
    }

    public InvalidTaskStateException(String message) {
        super("ERR_INVALID_TASK_STATE", message);
        this.currentState = null;
        this.attemptedState = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedState() {
        return attemptedState;
    }
}
