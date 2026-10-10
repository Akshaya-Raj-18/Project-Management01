package com.pms.model;

/**
 * Enumeration representing user access roles within the Project Management System.
 * (Module 1: Enum types, Module 1: Switch expression/statement)
 */
public enum UserRole {
    MANAGER("Project Manager"),
    DEVELOPER("Software Developer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isManager() {
        return this == MANAGER;
    }

    public boolean isDeveloper() {
        return this == DEVELOPER;
    }
}
