package com.pms.model;

import com.pms.util.AppLogger;

/**
 * Manager subclass representing a Project Manager.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Inheritance (Subclass), super keyword for constructor chaining,
 *   Method Overriding (@Override), Dynamic Method Dispatch, Protected member access,
 *   Constructor call order tracking.
 */
public class Manager extends User {

    private String department;
    private double budgetApprovalLimit;

    // Module 2: Overloaded constructors
    public Manager() {
        super();
        this.role = UserRole.MANAGER;
        this.department = "Engineering Management";
        this.budgetApprovalLimit = 500000.0;
    }

    public Manager(Integer id, String username, String passwordHash, String fullName, String email) {
        this(id, username, passwordHash, fullName, email, "Engineering Management", 500000.0);
    }

    public Manager(Integer id, String username, String passwordHash, String fullName, String email,
                   String department, double budgetApprovalLimit) {
        // Module 2: super() constructor call chaining to superclass (User -> BaseEntity)
        super(id, username, passwordHash, fullName, email, UserRole.MANAGER);
        AppLogger.debug("[Constructor Order] Manager constructor executing for user: %s", username);

        this.department = department;
        this.budgetApprovalLimit = budgetApprovalLimit;
    }

    /**
     * Module 2: Method Overriding and Dynamic Method Dispatch.
     * Managers possess permissions for all operations including project creation, deletion, assignment.
     */
    @Override
    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        // Managers have access to all project lifecycle operations
        return true;
    }

    /**
     * Module 2: Polymorphic method implementation.
     */
    @Override
    public String getRoleDescription() {
        return "Project Manager (Full Administrative & Planning Authority)";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getBudgetApprovalLimit() {
        return budgetApprovalLimit;
    }

    public void setBudgetApprovalLimit(double budgetApprovalLimit) {
        this.budgetApprovalLimit = budgetApprovalLimit;
    }

    @Override
    public String toString() {
        // Module 2: Using super.toString()
        return super.toString() + " [Dept: " + department + ", Approval Limit: $" + String.format("%,.2f", budgetApprovalLimit) + "]";
    }
}
