package com.pms.exception;

/**
 * Thrown when a task cannot be located in the database or task hierarchy.
 * (Module 3: Custom Exception)
 */
public class TaskNotFoundException extends PMSException {

    private final Integer taskId;

    public TaskNotFoundException(int taskId) {
        super("ERR_TASK_NOT_FOUND", "Task with ID " + taskId + " does not exist.");
        this.taskId = taskId;
    }

    public TaskNotFoundException(String message) {
        super("ERR_TASK_NOT_FOUND", message);
        this.taskId = null;
    }

    public Integer getTaskId() {
        return taskId;
    }
}
