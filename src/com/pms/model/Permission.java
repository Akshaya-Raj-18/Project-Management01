package com.pms.model;

/**
 * Fine-grained permissions matrix.
 * (Module 1: Enums and Role-Based Access Control)
 */
public enum Permission {
    CREATE_PROJECT("Create new projects"),
    EDIT_PROJECT("Modify existing projects"),
    DELETE_PROJECT("Delete/Archive projects"),
    ASSIGN_TASK("Assign tasks to developers"),
    CREATE_TASK("Create new tasks or subtasks"),
    DELETE_TASK("Delete or cancel tasks"),
    UPDATE_TASK_STATUS("Change task status (TODO -> IN_PROGRESS -> REVIEW -> COMPLETED)"),
    LOG_HOURS("Log work hours against tasks"),
    MANAGE_USERS("Manage team members and roles"),
    VIEW_ANALYTICS("View management dashboards and project budgets");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
