/**
 * ProjectManagement entry point alias for ProjectManagementSystem.
 * Allows running with either:
 *   javac ProjectManagement.java && java ProjectManagement
 * or:
 *   javac ProjectManagementSystem.java && java ProjectManagementSystem
 */
public class ProjectManagement {
    public static void main(String[] args) {
        ProjectManagementSystem.main(args);
    }
}
