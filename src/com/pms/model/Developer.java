package com.pms.model;

import com.pms.util.AppLogger;

/**
 * Developer subclass representing a Software Engineer / Developer.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Inheritance (Subclass), super keyword for constructor chaining,
 *   Method Overriding (@Override), Dynamic Method Dispatch, Protected member access.
 * - Module 1: Switch statement over enums.
 */
public class Developer extends User {

    private String primaryTechStack;
    private double hourlyBillingRate;
    private int maxWeeklyHours;

    // Module 2: Overloaded constructors
    public Developer() {
        super();
        this.role = UserRole.DEVELOPER;
        this.primaryTechStack = "Java / Swing / JDBC";
        this.hourlyBillingRate = 65.0;
        this.maxWeeklyHours = 40;
    }

    public Developer(Integer id, String username, String passwordHash, String fullName, String email) {
        this(id, username, passwordHash, fullName, email, "Java / Spring / SQLite", 65.0, 40);
    }

    public Developer(Integer id, String username, String passwordHash, String fullName, String email,
                     String primaryTechStack, double hourlyBillingRate, int maxWeeklyHours) {
        // Module 2: super() constructor call chaining to superclass (User -> BaseEntity)
        super(id, username, passwordHash, fullName, email, UserRole.DEVELOPER);
        AppLogger.debug("[Constructor Order] Developer constructor executing for user: %s", username);

        this.primaryTechStack = primaryTechStack;
        this.hourlyBillingRate = hourlyBillingRate;
        this.maxWeeklyHours = maxWeeklyHours;
    }

    /**
     * Module 2: Method Overriding and Dynamic Method Dispatch.
     * Enforces Role-Based Access Control (RBAC): Developers can only update task statuses,
     * log hours, and create subtasks assigned to their domain.
     */
    @Override
    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }

        // Module 1: Switch statement over Enum
        switch (permission) {
            case UPDATE_TASK_STATUS:
            case LOG_HOURS:
                return true;
            case CREATE_TASK:
                // Developers can break tasks into subtasks
                return true;
            case CREATE_PROJECT:
            case EDIT_PROJECT:
            case DELETE_PROJECT:
            case DELETE_TASK:
            case MANAGE_USERS:
            case VIEW_ANALYTICS:
            default:
                // Administrative actions denied to Developers
                return false;
        }
    }

    /**
     * Module 2: Polymorphic method implementation.
     */
    @Override
    public String getRoleDescription() {
        return "Software Developer (Task Execution, Status Updates & Hour Logging)";
    }

    public String getPrimaryTechStack() {
        return primaryTechStack;
    }

    public void setPrimaryTechStack(String primaryTechStack) {
        this.primaryTechStack = primaryTechStack;
    }

    public double getHourlyBillingRate() {
        return hourlyBillingRate;
    }

    public void setHourlyBillingRate(double hourlyBillingRate) {
        this.hourlyBillingRate = hourlyBillingRate;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(int maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    @Override
    public String toString() {
        return super.toString() + " [Tech Stack: " + primaryTechStack + ", Max " + maxWeeklyHours + "h/wk]";
    }
}
