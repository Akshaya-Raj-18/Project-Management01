package com.pms.model;

/**
 * Enumeration representing Task priority levels.
 * (Module 1: Enum types)
 */
public enum TaskPriority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);

    private final String label;
    private final int weight;

    TaskPriority(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return label;
    }
}
