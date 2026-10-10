package com.pms.exception;

/**
 * Thrown when a project with the requested ID or title cannot be located.
 * (Module 3: Custom Exception, throw and throws handling)
 */
public class ProjectNotFoundException extends PMSException {

    private final Integer projectId;

    public ProjectNotFoundException(int projectId) {
        // Module 2: Using super keyword to invoke parent constructor
        super("ERR_PROJECT_NOT_FOUND", "Project with ID " + projectId + " was not found.");
        this.projectId = projectId;
    }

    public ProjectNotFoundException(String message) {
        super("ERR_PROJECT_NOT_FOUND", message);
        this.projectId = null;
    }

    public Integer getProjectId() {
        return projectId;
    }
}
