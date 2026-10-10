package com.pms.model;

/**
 * Enumeration representing Project lifecycle states.
 * (Module 1: Enum types)
 */
public enum ProjectStatus {
    PLANNING("Planning"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
