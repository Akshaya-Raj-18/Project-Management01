package com.pms.model;

/**
 * Enumeration representing Task execution workflow states.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Enums, Switch statements, Control logic.
 * - Module 3: State transition validation supporting InvalidTaskStateException.
 */
public enum TaskStatus {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    REVIEW("Under Review"),
    COMPLETED("Completed");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Validates whether transition from 'this' status to 'target' status is semantically legal.
     * (Module 1: Switch control statement and if-else logic)
     *
     * @param target The requested target TaskStatus
     * @return true if permitted, false otherwise
     */
    public boolean canTransitionTo(TaskStatus target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true; // No-op transition
        }

        switch (this) {
            case TODO:
                // From TODO can begin work
                return target == IN_PROGRESS;

            case IN_PROGRESS:
                // From IN_PROGRESS can submit for REVIEW or send back to TODO
                return target == REVIEW || target == TODO;

            case REVIEW:
                // From REVIEW can approve to COMPLETED or reject back to IN_PROGRESS
                return target == COMPLETED || target == IN_PROGRESS;

            case COMPLETED:
                // A completed task can be reopened back to IN_PROGRESS if bugs found
                return target == IN_PROGRESS;

            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
