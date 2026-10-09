package com.pms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.pms.util.AppLogger;

/**
 * Task Entity representing an actionable item or nested sub-task.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Wrapper types, Primitive casting ((double), (int)), Strings,
 *   Control Statements (switch/if-else/loops).
 * - Module 2: Inheritance (extends BaseEntity), Overloaded Constructors, Dynamic use of 'this',
 *   Method Overloading, Recursion (calculating sub-task completion percentage, depth, and tree count),
 *   Static Nested Class (Task.Builder), Member Inner Class (TaskAuditEntry).
 */
public class Task extends BaseEntity {

    private int projectId;
    private String title;
    private String description;
    private Integer assignedUserId;
    private Integer parentTaskId; // null if top-level task
    private TaskStatus status;
    private TaskPriority priority;
    private double estimatedHours;
    private double loggedHours;
    private boolean deleted;

    // Sub-tasks composite relationship
    private final List<Task> subTasks = new ArrayList<>();

    // Module 2: Overloaded Constructor 1 (Minimal)
    public Task(String title, int projectId) {
        this(null, projectId, title, "", null, null, TaskStatus.TODO, TaskPriority.MEDIUM, 0.0, 0.0);
    }

    // Module 2: Overloaded Constructor 2 (Standard)
    public Task(Integer id, int projectId, String title, TaskStatus status, TaskPriority priority) {
        this(id, projectId, title, "", null, null, status, priority, 0.0, 0.0);
    }

    // Module 2: Overloaded Constructor 3 (Full Parameter Set)
    public Task(Integer id, int projectId, String title, String description,
                Integer assignedUserId, Integer parentTaskId,
                TaskStatus status, TaskPriority priority,
                double estimatedHours, double loggedHours) {
        // Module 2: super(id) invoking BaseEntity constructor
        super(id);
        AppLogger.debug("[Constructor Order] Task constructor executing for task: %s", title);

        // Module 2: Dynamic use of 'this'
        this.projectId = projectId;
        this.title = title != null ? title.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.assignedUserId = assignedUserId;
        this.parentTaskId = parentTaskId;
        this.status = status != null ? status : TaskStatus.TODO;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.estimatedHours = estimatedHours;
        this.loggedHours = loggedHours;
        this.deleted = false;
    }

    // Sub-task tree management
    public void addSubTask(Task child) {
        if (child != null && !subTasks.contains(child)) {
            child.setParentTaskId(this.id);
            child.setProjectId(this.projectId);
            subTasks.add(child);
        }
    }

    public List<Task> getSubTasks() {
        return Collections.unmodifiableList(subTasks);
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    // =========================================================================
    // MODULE 2: RECURSIVE METHODS (Core Syllabus Requirement)
    // =========================================================================

    /**
     * RECURSIVE ALGORITHM 1: Dynamically calculates task completion percentage.
     * 
     * If the task is a parent with sub-tasks, its completion percentage is computed
     * recursively by evaluating each child sub-task's completion percentage and averaging.
     * If it is a leaf task, it evaluates based on its current lifecycle state.
     * 
     * Demonstrates Module 1: Primitive casting ((double) sum / count) and switch statement.
     * Demonstrates Module 2: Recursion.
     *
     * @return Completion percentage in range [0.0, 100.0]
     */
    public double calculateCompletionPercentage() {
        // Base case: Leaf task with no child sub-tasks
        if (subTasks.isEmpty()) {
            switch (this.status) {
                case COMPLETED:
                    return 100.0;
                case REVIEW:
                    return 80.0;
                case IN_PROGRESS:
                    // If hours are logged against estimate, calculate proportional progress capped at 90%
                    if (estimatedHours > 0) {
                        // Module 1: Primitive double casting
                        double hourRatio = (this.loggedHours / this.estimatedHours) * 100.0;
                        return Math.min(90.0, Math.max(25.0, hourRatio));
                    }
                    return 50.0;
                case TODO:
                default:
                    return 0.0;
            }
        }

        // Recursive case: Compute average of all child sub-tasks recursively
        double sumPercentage = 0.0;
        for (Task child : subTasks) {
            // Recursive invocation
            sumPercentage += child.calculateCompletionPercentage();
        }

        // Module 1: Primitive double division casting
        double aggregate = sumPercentage / (double) subTasks.size();
        return Math.round(aggregate * 10.0) / 10.0; // Round to 1 decimal place
    }

    /**
     * RECURSIVE ALGORITHM 2: Calculates task hierarchy / dependency depth.
     * Leaf tasks return 0. Tasks with sub-tasks return 1 + max(children depth).
     *
     * @return Maximum depth level of nested sub-tasks
     */
    public int calculateDependencyDepth() {
        if (subTasks.isEmpty()) {
            return 0; // Base case
        }

        int maxChildDepth = 0;
        for (Task child : subTasks) {
            // Recursive step
            int depth = child.calculateDependencyDepth();
            if (depth > maxChildDepth) {
                maxChildDepth = depth;
            }
        }
        return 1 + maxChildDepth;
    }

    /**
     * RECURSIVE ALGORITHM 3: Calculates total count of all descendant sub-tasks.
     *
     * @return Total descendant task count
     */
    public int countTotalSubTasksRecursive() {
        int count = subTasks.size();
        for (Task child : subTasks) {
            // Recursive step
            count += child.countTotalSubTasksRecursive();
        }
        return count;
    }

    // =========================================================================
    // MODULE 2: METHOD OVERLOADING
    // =========================================================================

    /**
     * Overloaded method 1: Log hours with numeric value only.
     */
    public void logHours(double additionalHours) {
        logHours(additionalHours, "Work in progress");
    }

    /**
     * Overloaded method 2: Log hours with numeric value and descriptive note.
     */
    public void logHours(double additionalHours, String workNote) {
        if (additionalHours > 0) {
            this.loggedHours += additionalHours;
            this.updatedAt = LocalDateTime.now();
            AppLogger.info("Logged %.2f hours on task #%d '%s' [%s]. Total logged: %.2f hrs",
                    additionalHours, getId() != null ? getId() : 0, this.title, workNote, this.loggedHours);
        }
    }

    // Getters and Setters
    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Integer assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public Integer getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public double getLoggedHours() {
        return loggedHours;
    }

    public void setLoggedHours(double loggedHours) {
        this.loggedHours = loggedHours;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return String.format("[%s] #%d: %s (Priority: %s, Progress: %.1f%%)",
                status, id != null ? id : 0, title, priority, calculateCompletionPercentage());
    }

    /**
     * Module 2: Member Inner Class.
     * Records historical audit events tied specifically to this Task instance.
     */
    public class TaskAuditEntry {
        private final LocalDateTime timestamp;
        private final String actionDescription;
        private final String performedBy;

        public TaskAuditEntry(String actionDescription, String performedBy) {
            this.timestamp = LocalDateTime.now();
            this.actionDescription = actionDescription;
            this.performedBy = performedBy;
        }

        public String getAuditSummary() {
            // Accessing outer class state using Task.this
            return String.format("[%s] Task #%d '%s' by %s: %s",
                    timestamp, Task.this.id, Task.this.title, performedBy, actionDescription);
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getActionDescription() {
            return actionDescription;
        }

        public String getPerformedBy() {
            return performedBy;
        }
    }

    /**
     * Module 2: Static Nested Class (Builder Design Pattern).
     * Demonstrates dynamic method chaining using 'return this'.
     */
    public static class Builder {
        private Integer id;
        private int projectId;
        private String title = "";
        private String description = "";
        private Integer assignedUserId;
        private Integer parentTaskId;
        private TaskStatus status = TaskStatus.TODO;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private double estimatedHours = 0.0;
        private double loggedHours = 0.0;

        public Builder(int projectId, String title) {
            this.projectId = projectId;
            this.title = title;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this; // Dynamic use of 'this' in method chaining
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder assignedUser(Integer userId) {
            this.assignedUserId = userId;
            return this;
        }

        public Builder parentTask(Integer parentTaskId) {
            this.parentTaskId = parentTaskId;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder estimatedHours(double hours) {
            this.estimatedHours = hours;
            return this;
        }

        public Builder loggedHours(double hours) {
            this.loggedHours = hours;
            return this;
        }

        public Task build() {
            return new Task(id, projectId, title, description, assignedUserId,
                    parentTaskId, status, priority, estimatedHours, loggedHours);
        }
    }
}
