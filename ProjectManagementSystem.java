import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * =========================================================================================
 * OBJECT-ORIENTED PROGRAMMING USING JAVA - PROJECT MANAGEMENT SYSTEM (PMS)
 * =========================================================================================
 * 
 * EXECUTION INSTRUCTIONS:
 *   javac ProjectManagementSystem.java
 *   java ProjectManagementSystem
 * 
 * DEFAULT DEMO ACCOUNTS:
 *   - Project Manager:   admin     / admin123  (Full administrative privileges)
 *   - Developer 1:       dev_alice / alice123  (Role-based access: execution & logging)
 *   - Developer 2:       dev_bob   / bob123    (Role-based access: execution & logging)
 * =========================================================================================
 */
public class ProjectManagementSystem {

    // =====================================================================================
    // MODULE 1: FUNDAMENTALS, TYPES, LOGGING, VARARGS & UTILITIES
    // =====================================================================================

    public enum LogLevel {
        DEBUG("[DEBUG]"), INFO("[INFO ]"), WARN("[WARN ]"), ERROR("[ERROR]");
        private final String prefix;
        LogLevel(String prefix) { this.prefix = prefix; }
        public String getPrefix() { return prefix; }
    }

    /**
     * Module 1: Logger with Varargs (Object... args), Strings formatting, and Switch statement.
     */
    public static final class AppLogger {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        private static LogLevel currentLevel = LogLevel.INFO;

        private AppLogger() {}

        public static void setLogLevel(LogLevel level) { currentLevel = level; }

        public static void log(LogLevel level, String message, Object... args) {
            if (level.ordinal() < currentLevel.ordinal()) return;
            String formatted = (args != null && args.length > 0) ? String.format(message, args) : message;
            String line = String.format("%s %s [%s] %s",
                    LocalDateTime.now().format(FORMATTER), level.getPrefix(), Thread.currentThread().getName(), formatted);
            switch (level) {
                case ERROR: System.err.println(line); break;
                default:    System.out.println(line); break;
            }
        }

        public static void info(String msg, Object... args) { log(LogLevel.INFO, msg, args); }
        public static void warn(String msg, Object... args) { log(LogLevel.WARN, msg, args); }
        public static void error(String msg, Object... args) { log(LogLevel.ERROR, msg, args); }
    }

    // =====================================================================================
    // MODULE 3: CUSTOM EXCEPTION HIERARCHY
    // =====================================================================================

    public static class PMSException extends Exception {
        private final String errorCode;
        public PMSException(String message) { super(message); this.errorCode = "ERR_GENERIC"; }
        public PMSException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
        public String getErrorCode() { return errorCode; }
    }

    public static class ProjectNotFoundException extends PMSException {
        private final Integer projectId;
        public ProjectNotFoundException(int projectId) {
            super("ERR_PROJECT_NOT_FOUND", "Project with ID #" + projectId + " was not found in the system.");
            this.projectId = projectId;
        }
        public Integer getProjectId() { return projectId; }
    }

    public static class TaskNotFoundException extends PMSException {
        public TaskNotFoundException(int taskId) {
            super("ERR_TASK_NOT_FOUND", "Task with ID #" + taskId + " does not exist.");
        }
    }

    public static class UnauthorizedAccessException extends PMSException {
        private final String username;
        private final String attemptedAction;
        public UnauthorizedAccessException(String username, String attemptedAction) {
            super("ERR_UNAUTHORIZED", String.format("User '%s' is not authorized to perform action: '%s'. Required: Project Manager (Admin) privileges.", username, attemptedAction));
            this.username = username;
            this.attemptedAction = attemptedAction;
        }
        public String getUsername() { return username; }
        public String getAttemptedAction() { return attemptedAction; }
    }

    public static class InvalidTaskStateException extends PMSException {
        public InvalidTaskStateException(String fromState, String toState, String reason) {
            super("ERR_INVALID_STATE", String.format("Illegal state transition from '%s' to '%s': %s", fromState, toState, reason));
        }
    }

    public static class DatabaseOperationException extends PMSException {
        public DatabaseOperationException(String message, Throwable cause) {
            super("ERR_DB_OP", message + (cause != null ? " Reason: " + cause.getMessage() : ""));
        }
    }

    // =====================================================================================
    // MODULE 1 & 2: INTERFACES, ENUMS & CORE DOMAIN ENTITIES
    // =====================================================================================

    public interface Identifiable {
        Integer getId();
        void setId(Integer id);
    }

    public interface Auditable {
        LocalDateTime getCreatedAt();
        LocalDateTime getUpdatedAt();
        void setUpdatedAt(LocalDateTime dt);
    }

    public static abstract class BaseEntity implements Identifiable, Auditable {
        protected Integer id;
        protected LocalDateTime createdAt;
        protected LocalDateTime updatedAt;

        public BaseEntity() { this(null); }

        public BaseEntity(Integer id) {
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] BaseEntity constructor initialized for ID: %s", id);
            this.id = id;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        @Override public Integer getId() { return id; }
        @Override public void setId(Integer id) { this.id = id; }
        @Override public LocalDateTime getCreatedAt() { return createdAt; }
        @Override public LocalDateTime getUpdatedAt() { return updatedAt; }
        @Override public void setUpdatedAt(LocalDateTime dt) { this.updatedAt = dt; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BaseEntity that = (BaseEntity) o;
            return Objects.equals(id, that.id);
        }
        @Override public int hashCode() { return Objects.hashCode(id); }
    }

    public enum UserRole {
        MANAGER("Project Manager"), DEVELOPER("Software Developer");
        private final String label;
        UserRole(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum Permission {
        CREATE_PROJECT, EDIT_PROJECT, DELETE_PROJECT, CREATE_TASK,
        EDIT_TASK, DELETE_TASK, UPDATE_TASK_STATUS, LOG_HOURS, MANAGE_USERS
    }

    /**
     * Module 2: Abstract User Superclass enforcing Dynamic Method Dispatch.
     */
    public static abstract class User extends BaseEntity {
        protected String username;
        protected String password;
        protected String fullName;
        protected String email;
        protected UserRole role;

        public User(Integer id, String username, String password, String fullName, String email, UserRole role) {
            super(id);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] User constructor initialized: %s", username);
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }

        public abstract boolean hasPermission(Permission permission);
        public abstract String getRoleDescription();

        public String getProfileBadgeText() {
            return fullName + " (" + username + ") • " + getRoleDescription();
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public void setFullName(String n) { this.fullName = n; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public UserRole getRole() { return role; }

        @Override public String toString() {
            return String.format("[%s] %s (%s)", role, fullName, username);
        }

        public static class Credentials {
            private final String username;
            private final String rawPassword;
            public Credentials(String u, String p) { this.username = u; this.rawPassword = p; }
            public String getUsername() { return username; }
            public String getRawPassword() { return rawPassword; }
        }
    }

    public static class Manager extends User {
        private String department;
        private double budgetApprovalLimit;

        public Manager(Integer id, String username, String password, String fullName, String email, String dept, double limit) {
            super(id, username, password, fullName, email, UserRole.MANAGER);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] Manager constructor initialized: %s", username);
            this.department = dept;
            this.budgetApprovalLimit = limit;
        }

        @Override public boolean hasPermission(Permission permission) { return true; }
        @Override public String getRoleDescription() { return "Project Manager (Admin Authority)"; }
        public String getDepartment() { return department; }
        public void setDepartment(String d) { this.department = d; }
        public double getBudgetApprovalLimit() { return budgetApprovalLimit; }
        public void setBudgetApprovalLimit(double l) { this.budgetApprovalLimit = l; }
    }

    public static class Developer extends User {
        private String techStack;
        private double hourlyRate;

        public Developer(Integer id, String username, String password, String fullName, String email, String stack, double rate) {
            super(id, username, password, fullName, email, UserRole.DEVELOPER);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] Developer constructor initialized: %s", username);
            this.techStack = stack;
            this.hourlyRate = rate;
        }

        @Override public boolean hasPermission(Permission permission) {
            switch (permission) {
                case UPDATE_TASK_STATUS:
                case LOG_HOURS:
                case CREATE_TASK:
                case EDIT_TASK:
                    return true;
                default:
                    return false;
            }
        }

        @Override public String getRoleDescription() { return "Software Developer (Execution & Logging)"; }
        public String getTechStack() { return techStack; }
        public void setTechStack(String s) { this.techStack = s; }
        public double getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(double r) { this.hourlyRate = r; }
    }

    public enum ProjectStatus {
        PLANNING("Planning"), IN_PROGRESS("In Progress"), ON_HOLD("On Hold"), COMPLETED("Completed"), CANCELLED("Cancelled");
        private final String label;
        ProjectStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum TaskStatus {
        TODO("To Do"), IN_PROGRESS("In Progress"), REVIEW("In Review"), COMPLETED("Completed");
        private final String label;
        TaskStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }

        public boolean canTransitionTo(TaskStatus next) {
            if (this == next) return true;
            switch (this) {
                case TODO:        return next == IN_PROGRESS;
                case IN_PROGRESS: return next == REVIEW || next == TODO;
                case REVIEW:      return next == COMPLETED || next == IN_PROGRESS;
                case COMPLETED:   return next == IN_PROGRESS;
                default:          return false;
            }
        }
    }

    public enum TaskPriority {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL("Critical");
        private final String label;
        TaskPriority(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public static class Project extends BaseEntity {
        private static long projectCount = 0;
        private String title;
        private String description;
        private double budget;
        private ProjectStatus status;
        private String clientName;
        private String[] tags;

        public Project(String title, String description) {
            this(null, title, description, 0.0, ProjectStatus.PLANNING, "Internal Client", new String[0]);
        }

        public Project(Integer id, String title, String description, double budget, ProjectStatus status, String client, String[] tags) {
            super(id);
            this.title = title != null ? title.trim() : "";
            this.description = description != null ? description.trim() : "";
            this.budget = budget;
            this.status = status != null ? status : ProjectStatus.PLANNING;
            this.clientName = client != null ? client.trim() : "Internal Client";
            this.tags = tags != null ? Arrays.copyOf(tags, tags.length) : new String[0];
            projectCount++;
        }

        public static long getProjectCount() { return projectCount; }

        public void addTag(String tag) {
            if (tag == null || tag.trim().isEmpty()) return;
            String[] newTags = Arrays.copyOf(tags, tags.length + 1);
            newTags[newTags.length - 1] = tag.trim();
            this.tags = newTags;
        }

        public Vector<String> getTagsAsVector() {
            Vector<String> vec = new Vector<>();
            if (tags != null) for (String t : tags) vec.add(t);
            return vec;
        }

        public String getTagsFormatted() {
            if (tags == null || tags.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tags.length; i++) {
                sb.append(tags[i]);
                if (i < tags.length - 1) sb.append(", ");
            }
            return sb.toString();
        }

        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public double getBudget() { return budget; }
        public void setBudget(double b) { this.budget = b; }
        public ProjectStatus getStatus() { return status; }
        public void setStatus(ProjectStatus s) { this.status = s; }
        public String getClientName() { return clientName; }
        public void setClientName(String c) { this.clientName = c; }
        public String[] getTags() { return Arrays.copyOf(tags, tags.length); }
        public void setTags(String[] t) { this.tags = t != null ? Arrays.copyOf(t, t.length) : new String[0]; }

        public class ProjectSummary {
            private final int taskCount;
            private final double progress;
            public ProjectSummary(int taskCount, double progress) {
                this.taskCount = taskCount;
                this.progress = progress;
            }
            public String getFormattedSummary() {
                return String.format("Project '%s' [%s] - %d tasks, %.1f%% complete. Budget: $%,.2f",
                        Project.this.title, Project.this.status, taskCount, progress, Project.this.budget);
            }
        }
    }

    public static class Task extends BaseEntity {
        private int projectId;
        private String title;
        private String description;
        private Integer assignedUserId;
        private Integer parentTaskId;
        private TaskStatus status;
        private TaskPriority priority;
        private double estimatedHours;
        private double loggedHours;

        private final List<Task> subTasks = new ArrayList<>();

        public Task(String title, int projectId) {
            this(null, projectId, title, "", null, null, TaskStatus.TODO, TaskPriority.MEDIUM, 0.0, 0.0);
        }

        public Task(Integer id, int projectId, String title, String description, Integer userId,
                    Integer parentId, TaskStatus status, TaskPriority priority, double estHours, double loggedHours) {
            super(id);
            this.projectId = projectId;
            this.title = title != null ? title.trim() : "";
            this.description = description != null ? description.trim() : "";
            this.assignedUserId = userId;
            this.parentTaskId = parentId;
            this.status = status != null ? status : TaskStatus.TODO;
            this.priority = priority != null ? priority : TaskPriority.MEDIUM;
            this.estimatedHours = estHours;
            this.loggedHours = loggedHours;
        }

        public void addSubTask(Task child) {
            if (child != null && !subTasks.contains(child)) {
                child.setParentTaskId(this.id);
                child.setProjectId(this.projectId);
                subTasks.add(child);
            }
        }

        public List<Task> getSubTasks() { return Collections.unmodifiableList(subTasks); }
        public void clearSubTasks() { subTasks.clear(); }

        // =====================================================================
        // MODULE 2: RECURSIVE ALGORITHMS
        // =====================================================================

        public double calculateCompletionPercentage() {
            if (subTasks.isEmpty()) {
                switch (this.status) {
                    case COMPLETED:   return 100.0;
                    case REVIEW:      return 80.0;
                    case IN_PROGRESS: return estimatedHours > 0 ? Math.min(90.0, Math.max(25.0, (loggedHours / estimatedHours) * 100.0)) : 50.0;
                    case TODO:
                    default:          return 0.0;
                }
            }
            double sum = 0.0;
            for (Task child : subTasks) {
                sum += child.calculateCompletionPercentage(); // Recursive call
            }
            double avg = sum / (double) subTasks.size();
            return Math.round(avg * 10.0) / 10.0;
        }

        public int calculateDependencyDepth() {
            if (subTasks.isEmpty()) return 0;
            int maxDepth = 0;
            for (Task child : subTasks) {
                int d = child.calculateDependencyDepth(); // Recursive call
                if (d > maxDepth) maxDepth = d;
            }
            return 1 + maxDepth;
        }

        public int countTotalSubTasksRecursive() {
            int count = subTasks.size();
            for (Task child : subTasks) {
                count += child.countTotalSubTasksRecursive(); // Recursive call
            }
            return count;
        }

        public void logHours(double hours) { logHours(hours, "Work in progress"); }
        public void logHours(double hours, String note) {
            if (hours > 0) {
                this.loggedHours += hours;
                this.updatedAt = LocalDateTime.now();
                AppLogger.info("Logged %.2f hours on task #%d '%s' [%s]", hours, id != null ? id : 0, title, note);
            }
        }

        public int getProjectId() { return projectId; }
        public void setProjectId(int pid) { this.projectId = pid; }
        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public Integer getAssignedUserId() { return assignedUserId; }
        public void setAssignedUserId(Integer uid) { this.assignedUserId = uid; }
        public Integer getParentTaskId() { return parentTaskId; }
        public void setParentTaskId(Integer pid) { this.parentTaskId = pid; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus s) { this.status = s; this.updatedAt = LocalDateTime.now(); }
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority p) { this.priority = p; }
        public double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(double h) { this.estimatedHours = h; }
        public double getLoggedHours() { return loggedHours; }
        public void setLoggedHours(double h) { this.loggedHours = h; }

        public static class Builder {
            private Integer id;
            private final int projectId;
            private final String title;
            private String description = "";
            private Integer assignedUserId;
            private Integer parentTaskId;
            private TaskStatus status = TaskStatus.TODO;
            private TaskPriority priority = TaskPriority.MEDIUM;
            private double estimatedHours = 0.0;
            private double loggedHours = 0.0;

            public Builder(int projectId, String title) { this.projectId = projectId; this.title = title; }
            public Builder id(Integer id) { this.id = id; return this; }
            public Builder description(String d) { this.description = d; return this; }
            public Builder assignedUser(Integer u) { this.assignedUserId = u; return this; }
            public Builder parentTask(Integer p) { this.parentTaskId = p; return this; }
            public Builder status(TaskStatus s) { this.status = s; return this; }
            public Builder priority(TaskPriority p) { this.priority = p; return this; }
            public Builder estimatedHours(double h) { this.estimatedHours = h; return this; }
            public Builder loggedHours(double h) { this.loggedHours = h; return this; }
            public Task build() {
                return new Task(id, projectId, title, description, assignedUserId, parentTaskId, status, priority, estimatedHours, loggedHours);
            }
        }
    }

    // =====================================================================================
    // MODULE 3: DATA ACCESS LAYER & THREAD-SAFE SINGLETON DATABASE MANAGER
    // =====================================================================================

    public static class DatabaseManager {
        private static volatile DatabaseManager instance;

        private final List<User> userTable = new ArrayList<>();
        private final List<Project> projectTable = new ArrayList<>();
        private final List<Task> taskTable = new ArrayList<>();
        private int userIdSequence = 1;
        private int projectIdSequence = 1;
        private int taskIdSequence = 1;

        private DatabaseManager() {
            seedDemoData();
        }

        public static DatabaseManager getInstance() {
            if (instance == null) {
                synchronized (DatabaseManager.class) {
                    if (instance == null) {
                        instance = new DatabaseManager();
                    }
                }
            }
            return instance;
        }

        private void seedDemoData() {
            // Seed Users
            Manager admin = new Manager(userIdSequence++, "admin", "admin123", "Dr. Arun Kumar (Manager)", "arun@ktu.edu", "Engineering Directorate", 1000000.0);
            Developer alice = new Developer(userIdSequence++, "dev_alice", "alice123", "Alice George (Developer)", "alice@tech.io", "Java 21, Swing, Spring Boot", 75.0);
            Developer bob = new Developer(userIdSequence++, "dev_bob", "bob123", "Bob Varghese (Developer)", "bob@cloud.io", "Java, Microservices, SQLite", 65.0);
            userTable.add(admin);
            userTable.add(alice);
            userTable.add(bob);

            // Seed Projects
            Project p1 = new Project(projectIdSequence++, "AI-Powered Smart Campus Portal", "Next-gen portal for student attendance and IoT campus alerts.", 85000.0, ProjectStatus.IN_PROGRESS, "KTU Directorate", new String[]{"AI", "Campus", "Java"});
            Project p2 = new Project(projectIdSequence++, "KTU ERP Examination Automation", "Centralized evaluation, hall ticket generation, and result ledger.", 130000.0, ProjectStatus.PLANNING, "Controller of Exams", new String[]{"ERP", "Security"});
            Project p3 = new Project(projectIdSequence++, "Intensive Care Telemetry Dashboard", "High-reliability patient telemetry visualization and alert gateway.", 98000.0, ProjectStatus.COMPLETED, "Regional Hospital", new String[]{"Healthcare", "IoT"});
            projectTable.add(p1);
            projectTable.add(p2);
            projectTable.add(p3);

            // Seed Tasks with 3-Level Recursive Tree
            Task t1 = new Task(taskIdSequence++, p1.getId(), "Architecture Design & Schema Specification", "Define relational schemas and component diagrams.", alice.getId(), null, TaskStatus.COMPLETED, TaskPriority.HIGH, 25.0, 25.0);
            Task t2 = new Task(taskIdSequence++, p1.getId(), "Authentication & RBAC Security Module", "Core security module supporting role permissions.", alice.getId(), null, TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL, 40.0, 18.0);
            
            // Sub-tasks under t2
            Task sub21 = new Task(taskIdSequence++, p1.getId(), "Password Hashing & PBKDF2 Salt Provider", "Cryptographic hashing module with salt generation.", alice.getId(), t2.getId(), TaskStatus.COMPLETED, TaskPriority.HIGH, 12.0, 12.0);
            Task sub22 = new Task(taskIdSequence++, p1.getId(), "Session & RBAC Token Interceptor", "Interceptor verifying user permissions per request.", bob.getId(), t2.getId(), TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 18.0, 6.0);
            
            // Sub-sub-task under sub22 (Nesting Depth = 2)
            Task sub221 = new Task(taskIdSequence++, p1.getId(), "Expiry & Refresh Token Rotator", "Worker task to invalidate expired session tokens.", bob.getId(), sub22.getId(), TaskStatus.TODO, TaskPriority.MEDIUM, 10.0, 0.0);

            Task t3 = new Task(taskIdSequence++, p1.getId(), "Interactive Swing GUI Dashboard", "Modern MVC GUI with Table Adapters and JTree WBS.", bob.getId(), null, TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL, 50.0, 30.0);

            taskTable.add(t1);
            taskTable.add(t2);
            taskTable.add(sub21);
            taskTable.add(sub22);
            taskTable.add(sub221);
            taskTable.add(t3);
        }

        public synchronized List<User> getUsers() { return new ArrayList<>(userTable); }
        public synchronized Optional<User> authenticate(String u, String p) {
            return userTable.stream().filter(usr -> usr.getUsername().equalsIgnoreCase(u) && usr.getPassword().equals(p)).findFirst();
        }
        public synchronized boolean isUsernameTaken(String username) {
            return userTable.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        }
        public synchronized User saveUser(User u) {
            if (u.getId() == null) u.setId(userIdSequence++);
            userTable.removeIf(existing -> existing.getId().equals(u.getId()));
            userTable.add(u);
            return u;
        }

        public synchronized List<Project> getProjects() { return new ArrayList<>(projectTable); }
        public synchronized Project saveProject(Project p) {
            if (p.getId() == null) p.setId(projectIdSequence++);
            projectTable.removeIf(existing -> existing.getId().equals(p.getId()));
            projectTable.add(p);
            return p;
        }
        public synchronized boolean deleteProject(int id) {
            // Delete associated tasks
            taskTable.removeIf(t -> t.getProjectId() == id);
            return projectTable.removeIf(p -> p.getId() == id);
        }

        public synchronized List<Task> getTasks() { return new ArrayList<>(taskTable); }
        public synchronized Task saveTask(Task t) {
            if (t.getId() == null) t.setId(taskIdSequence++);
            taskTable.removeIf(existing -> existing.getId().equals(t.getId()));
            taskTable.add(t);
            return t;
        }
        public synchronized boolean deleteTask(int id) {
            // Orphaned children or recursive deletes
            return taskTable.removeIf(t -> t.getId() == id || (t.getParentTaskId() != null && t.getParentTaskId().equals(id)));
        }
    }

    // =====================================================================================
    // MODULE 3: SERVICE LAYER (SOLID PRINCIPLES: SRP, OCP, LSP, ISP, DIP)
    // =====================================================================================

    public static class ProjectService {
        private final DatabaseManager db;
        public ProjectService(DatabaseManager db) { this.db = db; }

        public Project saveProject(Project p, User initiator) throws UnauthorizedAccessException {
            if (initiator == null || !initiator.hasPermission(Permission.CREATE_PROJECT)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", "CREATE/EDIT PROJECT");
            }
            return db.saveProject(p);
        }

        public void deleteProject(int id, User initiator) throws UnauthorizedAccessException, ProjectNotFoundException {
            if (initiator == null || !initiator.hasPermission(Permission.DELETE_PROJECT)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", "DELETE PROJECT");
            }
            boolean removed = db.deleteProject(id);
            if (!removed) throw new ProjectNotFoundException(id);
        }

        public List<Project> getAllProjects() { return db.getProjects(); }

        public double calculateProjectCompletion(int projectId) {
            List<Task> topTasks = buildTaskHierarchy(projectId);
            if (topTasks.isEmpty()) return 0.0;
            double sum = 0.0;
            for (Task t : topTasks) {
                sum += t.calculateCompletionPercentage(); // Recursive aggregation
            }
            return Math.round((sum / (double) topTasks.size()) * 10.0) / 10.0;
        }

        /**
         * Robust Work Breakdown Structure (WBS) builder:
         * Accurately groups all top-level tasks and assigns child tasks recursively.
         */
        public List<Task> buildTaskHierarchy(int projectId) {
            List<Task> all = db.getTasks();
            List<Task> projectTasks = new ArrayList<>();
            Map<Integer, Task> taskMap = new HashMap<>();

            for (Task t : all) {
                if (t.getProjectId() == projectId) {
                    t.clearSubTasks();
                    projectTasks.add(t);
                    taskMap.put(t.getId(), t);
                }
            }

            List<Task> roots = new ArrayList<>();
            for (Task t : projectTasks) {
                Integer pid = t.getParentTaskId();
                if (pid == null || pid <= 0 || !taskMap.containsKey(pid) || pid.equals(t.getId())) {
                    // Top-level task
                    roots.add(t);
                } else {
                    // Child task
                    Task parent = taskMap.get(pid);
                    parent.addSubTask(t);
                }
            }
            return roots;
        }
    }

    public static class TaskService {
        private final DatabaseManager db;
        public TaskService(DatabaseManager db) { this.db = db; }

        public Task saveTask(Task t, User initiator) throws UnauthorizedAccessException {
            boolean isEdit = t.getId() != null;
            Permission perm = isEdit ? Permission.EDIT_TASK : Permission.CREATE_TASK;
            if (initiator == null || !initiator.hasPermission(perm)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", perm.name());
            }
            return db.saveTask(t);
        }

        public void updateTaskStatus(int taskId, TaskStatus newStatus, User initiator)
                throws UnauthorizedAccessException, InvalidTaskStateException, TaskNotFoundException {
            if (initiator == null || !initiator.hasPermission(Permission.UPDATE_TASK_STATUS)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", "UPDATE_TASK_STATUS");
            }
            Task task = db.getTasks().stream().filter(t -> t.getId() == taskId).findFirst().orElseThrow(() -> new TaskNotFoundException(taskId));
            if (!task.getStatus().canTransitionTo(newStatus)) {
                throw new InvalidTaskStateException(task.getStatus().getLabel(), newStatus.getLabel(), "Illegal status transition according to engineering workflow.");
            }
            task.setStatus(newStatus);
            db.saveTask(task);
        }

        public void logHours(int taskId, double hours, User initiator) throws UnauthorizedAccessException, TaskNotFoundException {
            if (initiator == null || !initiator.hasPermission(Permission.LOG_HOURS)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", "LOG_HOURS");
            }
            Task task = db.getTasks().stream().filter(t -> t.getId() == taskId).findFirst().orElseThrow(() -> new TaskNotFoundException(taskId));
            task.logHours(hours);
            db.saveTask(task);
        }

        public void deleteTask(int taskId, User initiator) throws UnauthorizedAccessException, TaskNotFoundException {
            if (initiator == null || !initiator.hasPermission(Permission.DELETE_TASK)) {
                throw new UnauthorizedAccessException(initiator != null ? initiator.getUsername() : "GUEST", "DELETE_TASK");
            }
            boolean removed = db.deleteTask(taskId);
            if (!removed) throw new TaskNotFoundException(taskId);
        }

        public List<Task> getAllTasks() { return db.getTasks(); }
    }

    // =====================================================================================
    // MODULE 4: ADAPTER DESIGN PATTERNS FOR JTABLE
    // =====================================================================================

    public static class ProjectTableModelAdapter extends AbstractTableModel {
        private final String[] columns = {"ID", "Project Title", "Client Name", "Budget ($)", "Status", "Tags"};
        private final List<Project> projects = new ArrayList<>();

        public void setProjects(List<Project> list) {
            projects.clear();
            if (list != null) projects.addAll(list);
            fireTableDataChanged();
        }
        public Project getProjectAt(int row) { return (row >= 0 && row < projects.size()) ? projects.get(row) : null; }
        @Override public int getRowCount() { return projects.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }
        @Override public Object getValueAt(int row, int col) {
            Project p = projects.get(row);
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getTitle();
                case 2: return p.getClientName();
                case 3: return String.format("$%,.2f", p.getBudget());
                case 4: return p.getStatus();
                case 5: return p.getTagsFormatted();
                default: return null;
            }
        }
    }

    public static class TaskTableModelAdapter extends AbstractTableModel {
        private final String[] columns = {"ID", "Task Title", "Project ID", "Priority", "Status", "Est. Hrs", "Logged Hrs", "Progress", "Depth"};
        private final List<Task> tasks = new ArrayList<>();

        public void setTasks(List<Task> list) {
            tasks.clear();
            if (list != null) tasks.addAll(list);
            fireTableDataChanged();
        }
        public Task getTaskAt(int row) { return (row >= 0 && row < tasks.size()) ? tasks.get(row) : null; }
        @Override public int getRowCount() { return tasks.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }
        @Override public Object getValueAt(int row, int col) {
            Task t = tasks.get(row);
            switch (col) {
                case 0: return t.getId();
                case 1: return t.getTitle();
                case 2: return "#" + t.getProjectId();
                case 3: return t.getPriority();
                case 4: return t.getStatus();
                case 5: return t.getEstimatedHours() + "h";
                case 6: return t.getLoggedHours() + "h";
                case 7: return t.calculateCompletionPercentage(); // Recursive
                case 8: return "L" + t.calculateDependencyDepth(); // Recursive
                default: return null;
            }
        }
    }

    // =====================================================================================
    // UI THEME, PALETTE & CUSTOM RENDERERS
    // =====================================================================================

    public static final class UITheme {
        public static final Color PRIMARY = new Color(30, 64, 175);       // Deep Enterprise Royal Blue
        public static final Color PRIMARY_ACCENT = new Color(59, 130, 246);
        public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald Green
        public static final Color WARNING = new Color(245, 158, 11);       // Amber
        public static final Color DANGER = new Color(220, 38, 38);         // Crimson Red
        public static final Color BG_LIGHT = new Color(241, 245, 249);     // Slate 100
        public static final Color CARD_BG = Color.WHITE;
        public static final Color BORDER = new Color(203, 213, 225);       // Slate 300
        public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
        public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500

        public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 19);
        public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
        public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
        public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
        public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
        public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

        public static JPanel createCard() {
            JPanel p = new JPanel();
            p.setBackground(CARD_BG);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    new EmptyBorder(14, 16, 14, 16)
            ));
            return p;
        }

        public static JButton createPrimaryButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_BOLD);
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            return btn;
        }

        public static JButton createSecondaryButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_REGULAR);
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)
            ));
            return btn;
        }

        public static JButton createDangerButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_BOLD);
            btn.setBackground(DANGER);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            return btn;
        }

        public static DefaultTableCellRenderer createStatusRenderer() {
            return new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(FONT_BADGE);
                    lbl.setOpaque(true);

                    if (val instanceof TaskStatus) {
                        TaskStatus s = (TaskStatus) val;
                        switch (s) {
                            case COMPLETED:   lbl.setBackground(new Color(209, 250, 229)); lbl.setForeground(new Color(6, 95, 70)); break;
                            case REVIEW:      lbl.setBackground(new Color(243, 232, 255)); lbl.setForeground(new Color(107, 33, 168)); break;
                            case IN_PROGRESS: lbl.setBackground(new Color(254, 243, 199)); lbl.setForeground(new Color(146, 64, 14)); break;
                            default:          lbl.setBackground(new Color(224, 231, 255)); lbl.setForeground(new Color(30, 64, 175)); break;
                        }
                    } else if (val instanceof ProjectStatus) {
                        ProjectStatus s = (ProjectStatus) val;
                        switch (s) {
                            case COMPLETED:   lbl.setBackground(new Color(209, 250, 229)); lbl.setForeground(new Color(6, 95, 70)); break;
                            case IN_PROGRESS: lbl.setBackground(new Color(224, 242, 254)); lbl.setForeground(new Color(3, 105, 161)); break;
                            case ON_HOLD:     lbl.setBackground(new Color(254, 243, 199)); lbl.setForeground(new Color(146, 64, 14)); break;
                            case CANCELLED:   lbl.setBackground(new Color(254, 226, 226)); lbl.setForeground(new Color(185, 28, 28)); break;
                            default:          lbl.setBackground(new Color(241, 245, 249)); lbl.setForeground(new Color(71, 85, 105)); break;
                        }
                    }
                    return lbl;
                }
            };
        }

        public static DefaultTableCellRenderer createProgressRenderer() {
            final JProgressBar pb = new JProgressBar(0, 100);
            pb.setStringPainted(true);
            pb.setFont(FONT_SMALL);
            return new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                    if (val instanceof Number) {
                        int v = ((Number) val).intValue();
                        pb.setValue(v);
                        pb.setString(v + "%");
                        pb.setForeground(v >= 100 ? SUCCESS : (v >= 50 ? PRIMARY_ACCENT : WARNING));
                        return pb;
                    }
                    return super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                }
            };
        }
    }

    // =====================================================================================
    // COMPREHENSIVE MODAL DIALOGS (ALL FIELDS ENTERABLE)
    // =====================================================================================

    /**
     * Comprehensive Project Dialog with all fields enterable.
     */
    public static class ProjectFormDialog extends JDialog {
        private boolean saved = false;
        private final Project project;

        private JTextField txtTitle = new JTextField(22);
        private JTextField txtClient = new JTextField(22);
        private JTextField txtBudget = new JTextField(22);
        private JComboBox<ProjectStatus> cmbStatus = new JComboBox<>(ProjectStatus.values());
        private JTextField txtTags = new JTextField(22);
        private JTextArea txtDesc = new JTextArea(4, 22);

        public ProjectFormDialog(Frame parent, Project projectToEdit) {
            super(parent, projectToEdit == null ? "Create New Project" : "Edit Project #" + projectToEdit.getId(), true);
            this.project = projectToEdit;

            initComponents();
            if (project != null) {
                txtTitle.setText(project.getTitle());
                txtClient.setText(project.getClientName());
                txtBudget.setText(String.valueOf(project.getBudget()));
                cmbStatus.setSelectedItem(project.getStatus());
                txtTags.setText(project.getTagsFormatted());
                txtDesc.setText(project.getDescription());
            } else {
                txtBudget.setText("50000.0");
                txtClient.setText("Internal Directorate");
            }
            pack();
            setLocationRelativeTo(parent);
            setResizable(false);
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            header.setBackground(UITheme.PRIMARY);
            JLabel lblH = new JLabel(project == null ? "📋 Enter New Project Details" : "✏️ Update Project Information");
            lblH.setFont(UITheme.FONT_SECTION);
            lblH.setForeground(Color.WHITE);
            header.add(lblH);
            add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;
            addRow(form, g, row++, "Project Title: *", txtTitle);
            addRow(form, g, row++, "Client / Sponsor Name:", txtClient);
            addRow(form, g, row++, "Total Budget ($): *", txtBudget);
            addRow(form, g, row++, "Lifecycle Status:", cmbStatus);
            addRow(form, g, row++, "Domain Tags (comma separated):", txtTags);

            g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.NORTHWEST;
            form.add(new JLabel("Description & Objectives:"), g);
            g.gridx = 1;
            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            form.add(new JScrollPane(txtDesc), g);

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(UITheme.BG_LIGHT);

            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = UITheme.createPrimaryButton("Save Project");
            btnSave.addActionListener(e -> {
                String title = txtTitle.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Project Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double budget = 0.0;
                try {
                    budget = Double.parseDouble(txtBudget.getText().trim());
                    if (budget < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid numeric budget.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String[] tags = txtTags.getText().trim().isEmpty() ? new String[0] : txtTags.getText().trim().split(",\\s*");
                if (project == null) {
                    createdProject = new Project(null, title, txtDesc.getText().trim(), budget, (ProjectStatus) cmbStatus.getSelectedItem(), txtClient.getText().trim(), tags);
                } else {
                    project.setTitle(title);
                    project.setClientName(txtClient.getText().trim());
                    project.setBudget(budget);
                    project.setStatus((ProjectStatus) cmbStatus.getSelectedItem());
                    project.setTags(tags);
                    project.setDescription(txtDesc.getText().trim());
                }
                saved = true;
                dispose();
            });

            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
            g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST;
            p.add(new JLabel(label), g);
            g.gridx = 1;
            p.add(comp, g);
        }

        private Project createdProject;
        public boolean isSaved() { return saved; }
        public Project getProject() { return project != null ? project : createdProject; }
    }

    /**
     * Comprehensive Task & Sub-task Dialog with all fields enterable.
     */
    public static class TaskFormDialog extends JDialog {
        private boolean saved = false;
        private final Task task;

        private JComboBox<ProjectItem> cmbProj = new JComboBox<>();
        private JComboBox<TaskItem> cmbParent = new JComboBox<>();
        private JTextField txtTitle = new JTextField(22);
        private JComboBox<UserItem> cmbUser = new JComboBox<>();
        private JComboBox<TaskPriority> cmbPriority = new JComboBox<>(TaskPriority.values());
        private JComboBox<TaskStatus> cmbStatus = new JComboBox<>(TaskStatus.values());
        private JTextField txtEstHours = new JTextField("10.0", 22);
        private JTextField txtLoggedHours = new JTextField("0.0", 22);
        private JTextArea txtDesc = new JTextArea(4, 22);

        public TaskFormDialog(Frame parent, Task taskToEdit, List<Project> projects, List<Task> tasks, List<User> users, Integer defaultProjectId) {
            super(parent, taskToEdit == null ? "Create New Task / Sub-task" : "Edit Task #" + taskToEdit.getId(), true);
            this.task = taskToEdit;

            for (Project p : projects) cmbProj.addItem(new ProjectItem(p));
            cmbParent.addItem(new TaskItem(null, "── None (Root / Top-Level Task) ──"));
            for (Task t : tasks) {
                if (taskToEdit == null || !t.getId().equals(taskToEdit.getId())) {
                    cmbParent.addItem(new TaskItem(t.getId(), "#" + t.getId() + " • " + t.getTitle()));
                }
            }
            cmbUser.addItem(new UserItem(null, "── Unassigned ──"));
            for (User u : users) cmbUser.addItem(new UserItem(u.getId(), u.getFullName() + " (" + u.getRole().getLabel() + ")"));

            // Project default
            if (defaultProjectId != null) {
                for (int i = 0; i < cmbProj.getItemCount(); i++) {
                    if (cmbProj.getItemAt(i).p.getId().equals(defaultProjectId)) {
                        cmbProj.setSelectedIndex(i);
                        break;
                    }
                }
            }

            initComponents();
            if (task != null) {
                txtTitle.setText(task.getTitle());
                txtDesc.setText(task.getDescription());
                cmbPriority.setSelectedItem(task.getPriority());
                cmbStatus.setSelectedItem(task.getStatus());
                txtEstHours.setText(String.valueOf(task.getEstimatedHours()));
                txtLoggedHours.setText(String.valueOf(task.getLoggedHours()));

                // Select project
                for (int i = 0; i < cmbProj.getItemCount(); i++) {
                    if (cmbProj.getItemAt(i).p.getId().equals(task.getProjectId())) {
                        cmbProj.setSelectedIndex(i);
                        break;
                    }
                }
                // Select parent
                if (task.getParentTaskId() != null) {
                    for (int i = 0; i < cmbParent.getItemCount(); i++) {
                        if (task.getParentTaskId().equals(cmbParent.getItemAt(i).id)) {
                            cmbParent.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                // Select user
                if (task.getAssignedUserId() != null) {
                    for (int i = 0; i < cmbUser.getItemCount(); i++) {
                        if (task.getAssignedUserId().equals(cmbUser.getItemAt(i).id)) {
                            cmbUser.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            pack();
            setLocationRelativeTo(parent);
            setResizable(false);
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            header.setBackground(UITheme.PRIMARY);
            JLabel lblH = new JLabel(task == null ? "📌 Create New Task / Sub-task" : "✏️ Update Task #" + task.getId());
            lblH.setFont(UITheme.FONT_SECTION);
            lblH.setForeground(Color.WHITE);
            header.add(lblH);
            add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;
            addRow(form, g, row++, "Target Project: *", cmbProj);
            addRow(form, g, row++, "Parent Task (For Nested Sub-tasks):", cmbParent);
            addRow(form, g, row++, "Task Title: *", txtTitle);
            addRow(form, g, row++, "Assigned Team Member:", cmbUser);
            addRow(form, g, row++, "Priority Level:", cmbPriority);
            addRow(form, g, row++, "Execution Status:", cmbStatus);
            addRow(form, g, row++, "Estimated Hours (Est):", txtEstHours);
            if (task != null) addRow(form, g, row++, "Logged Hours (Actual):", txtLoggedHours);

            g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.NORTHWEST;
            form.add(new JLabel("Task Description & Scope:"), g);
            g.gridx = 1;
            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            form.add(new JScrollPane(txtDesc), g);

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(UITheme.BG_LIGHT);

            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = UITheme.createPrimaryButton("Save Task");
            btnSave.addActionListener(e -> {
                String title = txtTitle.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Task Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double est = 0.0, logged = 0.0;
                try {
                    est = Double.parseDouble(txtEstHours.getText().trim());
                    if (est < 0) throw new NumberFormatException();
                    if (task != null) logged = Double.parseDouble(txtLoggedHours.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid numeric hours.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ProjectItem projItem = (ProjectItem) cmbProj.getSelectedItem();
                int pid = projItem != null ? projItem.p.getId() : 1;
                TaskItem parentItem = (TaskItem) cmbParent.getSelectedItem();
                Integer parentId = parentItem != null ? parentItem.id : null;
                UserItem userItem = (UserItem) cmbUser.getSelectedItem();
                Integer uid = userItem != null ? userItem.id : null;

                if (task == null) {
                    createdTask = new Task.Builder(pid, title)
                            .description(txtDesc.getText().trim())
                            .parentTask(parentId)
                            .assignedUser(uid)
                            .priority((TaskPriority) cmbPriority.getSelectedItem())
                            .status((TaskStatus) cmbStatus.getSelectedItem())
                            .estimatedHours(est)
                            .loggedHours(0.0)
                            .build();
                } else {
                    task.setProjectId(pid);
                    task.setParentTaskId(parentId);
                    task.setTitle(title);
                    task.setDescription(txtDesc.getText().trim());
                    task.setAssignedUserId(uid);
                    task.setPriority((TaskPriority) cmbPriority.getSelectedItem());
                    task.setStatus((TaskStatus) cmbStatus.getSelectedItem());
                    task.setEstimatedHours(est);
                    task.setLoggedHours(logged);
                }
                saved = true;
                dispose();
            });

            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
            g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST;
            p.add(new JLabel(label), g);
            g.gridx = 1;
            p.add(comp, g);
        }

        private Task createdTask;
        public boolean isSaved() { return saved; }
        public Task getTask() { return task != null ? task : createdTask; }

        private static class ProjectItem {
            final Project p;
            ProjectItem(Project p) { this.p = p; }
            @Override public String toString() { return "#" + p.getId() + " • " + p.getTitle(); }
        }
        private static class TaskItem {
            final Integer id;
            final String text;
            TaskItem(Integer id, String text) { this.id = id; this.text = text; }
            @Override public String toString() { return text; }
        }
        private static class UserItem {
            final Integer id;
            final String text;
            UserItem(Integer id, String text) { this.id = id; this.text = text; }
            @Override public String toString() { return text; }
        }
    }

    /**
     * Modal Dialog to Add New Team Members (Manager / Admin Only).
     */
    public static class AddTeamMemberDialog extends JDialog {
        private boolean saved = false;
        private User createdUser;

        private JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Software Developer", "Project Manager"});
        private JTextField txtFullName = new JTextField(20);
        private JTextField txtUsername = new JTextField(20);
        private JPasswordField txtPassword = new JPasswordField(20);
        private JTextField txtEmail = new JTextField(20);
        private JTextField txtSpec = new JTextField("Java, Swing, JDBC", 20); // Dept or TechStack
        private JTextField txtMetric = new JTextField("60.0", 20); // Hourly Rate or Budget Limit

        private JLabel lblSpec = new JLabel("Primary Tech Stack: *");
        private JLabel lblMetric = new JLabel("Hourly Billing Rate ($): *");

        public AddTeamMemberDialog(Frame parent, DatabaseManager db) {
            super(parent, "Add New Team Member (Admin Only)", true);
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            header.setBackground(UITheme.PRIMARY);
            JLabel lblH = new JLabel("👥 Register New Team Member");
            lblH.setFont(UITheme.FONT_SECTION);
            lblH.setForeground(Color.WHITE);
            header.add(lblH);
            add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.fill = GridBagConstraints.HORIZONTAL;

            cmbRole.addActionListener(e -> {
                if (cmbRole.getSelectedIndex() == 1) { // Manager
                    lblSpec.setText("Assigned Department: *");
                    txtSpec.setText("Engineering Management");
                    lblMetric.setText("Budget Approval Limit ($): *");
                    txtMetric.setText("500000.0");
                } else {
                    lblSpec.setText("Primary Tech Stack: *");
                    txtSpec.setText("Java, Microservices, SQLite");
                    lblMetric.setText("Hourly Billing Rate ($): *");
                    txtMetric.setText("60.0");
                }
            });

            int row = 0;
            addRow(form, g, row++, "Team Role: *", cmbRole);
            addRow(form, g, row++, "Full Name: *", txtFullName);
            addRow(form, g, row++, "Unique Username: *", txtUsername);
            addRow(form, g, row++, "Initial Password: *", txtPassword);
            addRow(form, g, row++, "Email Address: *", txtEmail);
            addRow(form, g, row++, lblSpec.getText(), txtSpec);
            addRow(form, g, row++, lblMetric.getText(), txtMetric);

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(UITheme.BG_LIGHT);

            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dispose());

            JButton btnAdd = UITheme.createPrimaryButton("Add Team Member");
            btnAdd.addActionListener(e -> {
                String fn = txtFullName.getText().trim();
                String un = txtUsername.getText().trim();
                String pw = new String(txtPassword.getPassword());
                String em = txtEmail.getText().trim();
                String spec = txtSpec.getText().trim();

                if (fn.isEmpty() || un.isEmpty() || pw.isEmpty() || em.isEmpty() || spec.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (db.isUsernameTaken(un)) {
                    JOptionPane.showMessageDialog(this, "Username '" + un + "' is already taken! Choose another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double metricVal = 0.0;
                try {
                    metricVal = Double.parseDouble(txtMetric.getText().trim());
                    if (metricVal < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric rate or budget.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cmbRole.getSelectedIndex() == 1) { // Manager
                    createdUser = new Manager(null, un, pw, fn, em, spec, metricVal);
                } else { // Developer
                    createdUser = new Developer(null, un, pw, fn, em, spec, metricVal);
                }
                saved = true;
                dispose();
            });

            footer.add(btnCancel);
            footer.add(btnAdd);
            add(footer, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
            setResizable(false);
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
            g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST;
            p.add(new JLabel(label), g);
            g.gridx = 1;
            p.add(comp, g);
        }

        public boolean isSaved() { return saved; }
        public User getCreatedUser() { return createdUser; }
    }

    // =====================================================================================
    // MAIN APPLICATION GUI (JFRAME) WITH ENHANCED DASHBOARD & WBS TREE
    // =====================================================================================

    public static class MainFrame extends JFrame {
        private final DatabaseManager db;
        private final ProjectService projectService;
        private final TaskService taskService;
        private User currentUser;

        private JLabel lblUserBadge;
        private JTabbedPane tabbedPane;

        // Dashboard Components
        private JLabel lblTotalProj, lblTotalTasks, lblCompletedTasks, lblActiveUsers;
        private JPanel progressCardsPanel;
        private JLabel lblTodoCount, lblProgCount, lblReviewCount, lblDoneCount;

        // Project Panel Components
        private ProjectTableModelAdapter projectAdapter;
        private JTable projectTable;

        // Task Panel Components
        private TaskTableModelAdapter taskAdapter;
        private DefaultTreeModel treeModel;
        private DefaultMutableTreeNode treeRoot;
        private JTree hierarchyTree;
        private JTable taskTable;
        private JComboBox<String> cmbProjectFilter;

        // Team Panel Components
        private DefaultTableModel teamModel;
        private JTable teamTable;

        public MainFrame(DatabaseManager db, User initialUser) {
            super("KTU PBCST304 - Enterprise Project Management System");
            this.db = db;
            this.projectService = new ProjectService(db);
            this.taskService = new TaskService(db);
            this.currentUser = initialUser;

            initComponents();
            setSize(1220, 800);
            setMinimumSize(new Dimension(1040, 680));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            // Enterprise Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(UITheme.PRIMARY);
            header.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));

            JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            leftHeader.setOpaque(false);
            JLabel logo = new JLabel("🚀 PMS");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logo.setForeground(Color.WHITE);
            JLabel title = new JLabel("APJ Abdul Kalam Technological University | PBCST304 OOP");
            title.setFont(UITheme.FONT_SMALL);
            title.setForeground(new Color(224, 231, 255));
            leftHeader.add(logo);
            leftHeader.add(title);
            header.add(leftHeader, BorderLayout.WEST);

            JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
            rightHeader.setOpaque(false);

            // Dynamic Method Dispatch
            lblUserBadge = new JLabel(currentUser.getProfileBadgeText());
            lblUserBadge.setFont(UITheme.FONT_BOLD);
            lblUserBadge.setForeground(Color.WHITE);

            JButton btnSwitch = new JButton("🔑 Switch Active User");
            btnSwitch.setFont(UITheme.FONT_SMALL);
            btnSwitch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnSwitch.addActionListener(e -> showSwitchUserDialog());

            rightHeader.add(lblUserBadge);
            rightHeader.add(btnSwitch);
            header.add(rightHeader, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            // Tabs
            tabbedPane = new JTabbedPane();
            tabbedPane.setFont(UITheme.FONT_BOLD);
            tabbedPane.addTab("📊 Executive Dashboard", createDashboardPanel());
            tabbedPane.addTab("📁 Project Management", createProjectsPanel());
            tabbedPane.addTab("📌 Tasks & WBS Hierarchy", createTasksPanel());
            tabbedPane.addTab("👥 Team Members & Roles", createTeamPanel());

            tabbedPane.addChangeListener(e -> refreshAllViews());
            add(tabbedPane, BorderLayout.CENTER);

            // Status Bar
            JPanel status = new JPanel(new BorderLayout());
            status.setBackground(Color.WHITE);
            status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER), new EmptyBorder(7, 20, 7, 20)));
            JLabel lblStat = new JLabel("● Persistence Engine: Thread-Safe Singleton Active | MVC Architecture | Recursive Composite Pattern");
            lblStat.setFont(UITheme.FONT_SMALL);
            lblStat.setForeground(UITheme.SUCCESS);
            status.add(lblStat, BorderLayout.WEST);
            add(status, BorderLayout.SOUTH);

            refreshAllViews();
        }

        // ---------------------------------------------------------------------
        // 1. ENHANCED PROFESSIONAL DASHBOARD
        // ---------------------------------------------------------------------
        private JPanel createDashboardPanel() {
            JPanel panel = new JPanel(new BorderLayout(16, 16));
            panel.setBackground(UITheme.BG_LIGHT);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

            // Top Stat Cards (4 columns)
            JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
            metricsGrid.setOpaque(false);

            lblTotalProj = new JLabel("0", SwingConstants.CENTER);
            lblTotalTasks = new JLabel("0", SwingConstants.CENTER);
            lblCompletedTasks = new JLabel("0", SwingConstants.CENTER);
            lblActiveUsers = new JLabel("0", SwingConstants.CENTER);

            metricsGrid.add(createStatCard("ACTIVE PROJECTS", lblTotalProj, UITheme.PRIMARY, "Portfolio Overview"));
            metricsGrid.add(createStatCard("TOTAL TASKS", lblTotalTasks, UITheme.PRIMARY_ACCENT, "Work Breakdown Units"));
            metricsGrid.add(createStatCard("COMPLETED TASKS", lblCompletedTasks, UITheme.SUCCESS, "Delivered Milestones"));
            metricsGrid.add(createStatCard("TEAM MEMBERS", lblActiveUsers, new Color(139, 92, 246), "Engineers & Leads"));
            panel.add(metricsGrid, BorderLayout.NORTH);

            // Center: Split between Project Progress Rollups and Task Status Breakdown
            JPanel splitCenter = new JPanel(new GridLayout(1, 2, 16, 0));
            splitCenter.setOpaque(false);

            // Left Card: Project Rollups
            JPanel progressSection = UITheme.createCard();
            progressSection.setLayout(new BorderLayout(0, 12));
            JLabel lblPTitle = new JLabel("📈 Project Recursive Progress Tracking (Rollup from Sub-tasks)");
            lblPTitle.setFont(UITheme.FONT_SECTION);
            lblPTitle.setForeground(UITheme.TEXT_DARK);
            progressSection.add(lblPTitle, BorderLayout.NORTH);

            progressCardsPanel = new JPanel();
            progressCardsPanel.setLayout(new BoxLayout(progressCardsPanel, BoxLayout.Y_AXIS));
            progressCardsPanel.setOpaque(false);
            JScrollPane scrollProj = new JScrollPane(progressCardsPanel);
            scrollProj.setBorder(BorderFactory.createEmptyBorder());
            scrollProj.setOpaque(false);
            scrollProj.getViewport().setOpaque(false);
            progressSection.add(scrollProj, BorderLayout.CENTER);
            splitCenter.add(progressSection);

            // Right Card: Workflow Distribution & System Overview
            JPanel summarySection = UITheme.createCard();
            summarySection.setLayout(new BorderLayout(0, 15));
            JLabel lblSTitle = new JLabel("⚡ Task Workflow Distribution & System Health");
            lblSTitle.setFont(UITheme.FONT_SECTION);
            lblSTitle.setForeground(UITheme.TEXT_DARK);
            summarySection.add(lblSTitle, BorderLayout.NORTH);

            JPanel distroPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            distroPanel.setOpaque(false);

            lblTodoCount = new JLabel("To Do: 0");
            lblProgCount = new JLabel("In Progress: 0");
            lblReviewCount = new JLabel("In Review: 0");
            lblDoneCount = new JLabel("Completed: 0");

            distroPanel.add(createWorkflowItem("📝 To Do (Pending Execution)", lblTodoCount, new Color(224, 231, 255), new Color(30, 64, 175)));
            distroPanel.add(createWorkflowItem("⚙️ In Progress (Active Sprints)", lblProgCount, new Color(254, 243, 199), new Color(146, 64, 14)));
            distroPanel.add(createWorkflowItem("🔍 In Review (Quality Assurance)", lblReviewCount, new Color(243, 232, 255), new Color(107, 33, 168)));
            distroPanel.add(createWorkflowItem("✅ Completed (Delivered)", lblDoneCount, new Color(209, 250, 229), new Color(6, 95, 70)));
            summarySection.add(distroPanel, BorderLayout.CENTER);

            splitCenter.add(summarySection);
            panel.add(splitCenter, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createStatCard(String title, JLabel val, Color color, String sub) {
            JPanel card = UITheme.createCard();
            card.setLayout(new BorderLayout(0, 6));

            JLabel t = new JLabel(title);
            t.setFont(UITheme.FONT_BADGE);
            t.setForeground(UITheme.TEXT_MUTED);

            val.setFont(new Font("Segoe UI", Font.BOLD, 30));
            val.setForeground(color);

            JLabel s = new JLabel(sub);
            s.setFont(UITheme.FONT_SMALL);
            s.setForeground(UITheme.TEXT_MUTED);

            card.add(t, BorderLayout.NORTH);
            card.add(val, BorderLayout.CENTER);
            card.add(s, BorderLayout.SOUTH);
            return card;
        }

        private JPanel createWorkflowItem(String title, JLabel countLabel, Color bg, Color fg) {
            JPanel item = new JPanel(new BorderLayout());
            item.setBackground(bg);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(UITheme.FONT_BOLD);
            titleLbl.setForeground(fg);

            countLabel.setFont(UITheme.FONT_BOLD);
            countLabel.setForeground(fg);

            item.add(titleLbl, BorderLayout.WEST);
            item.add(countLabel, BorderLayout.EAST);
            return item;
        }

        // ---------------------------------------------------------------------
        // 2. ENHANCED PROJECT MANAGEMENT PANEL (FULL FIELDS & WARNINGS)
        // ---------------------------------------------------------------------
        private JPanel createProjectsPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setBackground(UITheme.BG_LIGHT);
            panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            JLabel heading = new JLabel("Enterprise Projects Directory");
            heading.setFont(UITheme.FONT_HEADER);
            heading.setForeground(UITheme.TEXT_DARK);
            topBar.add(heading, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton btnNew = UITheme.createPrimaryButton("+ New Project");
            btnNew.addActionListener(e -> handleNewProject());

            JButton btnEdit = UITheme.createSecondaryButton("✏️ Edit Project");
            btnEdit.addActionListener(e -> handleEditProject());

            JButton btnDel = UITheme.createDangerButton("🗑️ Delete Project");
            btnDel.addActionListener(e -> handleDeleteProject());

            actions.add(btnNew);
            actions.add(btnEdit);
            actions.add(btnDel);
            topBar.add(actions, BorderLayout.EAST);

            panel.add(topBar, BorderLayout.NORTH);

            projectAdapter = new ProjectTableModelAdapter();
            projectTable = new JTable(projectAdapter);
            projectTable.setRowHeight(34);
            projectTable.setFont(UITheme.FONT_REGULAR);
            projectTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            projectTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());

            // Double click to edit
            projectTable.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) handleEditProject();
                }
            });

            JScrollPane scroll = new JScrollPane(projectTable);
            scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
            panel.add(scroll, BorderLayout.CENTER);
            return panel;
        }

        private void handleNewProject() {
            try {
                if (!currentUser.hasPermission(Permission.CREATE_PROJECT)) {
                    throw new UnauthorizedAccessException(currentUser.getUsername(), "CREATE_PROJECT");
                }
                ProjectFormDialog dialog = new ProjectFormDialog(this, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    projectService.saveProject(dialog.getProject(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Project '" + dialog.getProject().getTitle() + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage() + "\n(Software Developers have view-only rights for projects)", "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void handleEditProject() {
            int row = projectTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a project from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                if (!currentUser.hasPermission(Permission.EDIT_PROJECT)) {
                    throw new UnauthorizedAccessException(currentUser.getUsername(), "EDIT_PROJECT");
                }
                Project p = projectAdapter.getProjectAt(row);
                ProjectFormDialog dialog = new ProjectFormDialog(this, p);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    projectService.saveProject(dialog.getProject(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Project updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void handleDeleteProject() {
            int row = projectTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a project from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Project p = projectAdapter.getProjectAt(row);

            // Mandatory Warning Message before Deleting
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("⚠️ WARNING: Are you sure you want to permanently delete Project #%d '%s'?\n" +
                            "This action will also delete all associated nested tasks and work logs!",
                            p.getId(), p.getTitle()),
                    "Confirm Project Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    projectService.deleteProject(p.getId(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Project #" + p.getId() + " and its tasks were deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                } catch (UnauthorizedAccessException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
                } catch (ProjectNotFoundException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. ENHANCED TASKS & PERFECT WBS TREE PANEL
        // ---------------------------------------------------------------------
        private JPanel createTasksPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setBackground(UITheme.BG_LIGHT);
            panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

            JPanel topBar = new JPanel(new BorderLayout(10, 0));
            topBar.setOpaque(false);

            JPanel filterArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            filterArea.setOpaque(false);
            filterArea.add(new JLabel("Filter Project:"));
            cmbProjectFilter = new JComboBox<>();
            cmbProjectFilter.setFont(UITheme.FONT_REGULAR);
            cmbProjectFilter.addActionListener(e -> applyTaskFilter());
            filterArea.add(cmbProjectFilter);
            topBar.add(filterArea, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton btnNewTask = UITheme.createPrimaryButton("+ New Task / Sub-task");
            btnNewTask.addActionListener(e -> handleNewTask());

            JButton btnEditTask = UITheme.createSecondaryButton("✏️ Edit Task");
            btnEditTask.addActionListener(e -> handleEditTask());

            JButton btnStatus = UITheme.createSecondaryButton("⚡ Transition Status");
            btnStatus.addActionListener(e -> handleTransitionStatus());

            JButton btnHours = UITheme.createSecondaryButton("⏱️ Log Hours");
            btnHours.addActionListener(e -> handleLogHours());

            JButton btnDelTask = UITheme.createDangerButton("🗑️ Delete Task");
            btnDelTask.addActionListener(e -> handleDeleteTask());

            actions.add(btnNewTask);
            actions.add(btnEditTask);
            actions.add(btnStatus);
            actions.add(btnHours);
            actions.add(btnDelTask);
            topBar.add(actions, BorderLayout.EAST);

            panel.add(topBar, BorderLayout.NORTH);

            // JTree WBS on Left, JTable on Right
            treeRoot = new DefaultMutableTreeNode("Work Breakdown Structure (WBS)");
            treeModel = new DefaultTreeModel(treeRoot);
            hierarchyTree = new JTree(treeModel);
            hierarchyTree.setFont(UITheme.FONT_REGULAR);
            hierarchyTree.setRowHeight(24);

            // Clicking any tree node synchronizes and selects that task in the table!
            hierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) hierarchyTree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject() instanceof TaskNodeItem) {
                        TaskNodeItem item = (TaskNodeItem) node.getUserObject();
                        highlightTaskInTable(item.task.getId());
                    }
                }
            });

            taskAdapter = new TaskTableModelAdapter();
            taskTable = new JTable(taskAdapter);
            taskTable.setRowHeight(34);
            taskTable.setFont(UITheme.FONT_REGULAR);
            taskTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            taskTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());
            taskTable.getColumnModel().getColumn(7).setCellRenderer(UITheme.createProgressRenderer());

            taskTable.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) handleEditTask();
                }
            });

            JScrollPane scrollTree = new JScrollPane(hierarchyTree);
            scrollTree.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UITheme.BORDER), "Interactive WBS Tree", TitledBorder.LEFT, TitledBorder.TOP, UITheme.FONT_BOLD, UITheme.PRIMARY),
                    new EmptyBorder(6, 6, 6, 6)
            ));
            scrollTree.setPreferredSize(new Dimension(380, 400));

            JScrollPane scrollTable = new JScrollPane(taskTable);
            scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, scrollTable);
            split.setDividerLocation(380);
            split.setOpaque(false);
            panel.add(split, BorderLayout.CENTER);
            return panel;
        }

        private void highlightTaskInTable(int taskId) {
            for (int i = 0; i < taskAdapter.getRowCount(); i++) {
                Task t = taskAdapter.getTaskAt(i);
                if (t != null && t.getId().equals(taskId)) {
                    taskTable.setRowSelectionInterval(i, i);
                    taskTable.scrollRectToVisible(taskTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }

        private void handleNewTask() {
            try {
                if (!currentUser.hasPermission(Permission.CREATE_TASK)) {
                    throw new UnauthorizedAccessException(currentUser.getUsername(), "CREATE_TASK");
                }
                List<Project> projs = projectService.getAllProjects();
                if (projs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please create a project first.", "No Projects", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                TaskFormDialog dialog = new TaskFormDialog(this, null, projs, taskService.getAllTasks(), db.getUsers(), projs.get(0).getId());
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    taskService.saveTask(dialog.getTask(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Task created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void handleEditTask() {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a task from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                if (!currentUser.hasPermission(Permission.EDIT_TASK)) {
                    throw new UnauthorizedAccessException(currentUser.getUsername(), "EDIT_TASK");
                }
                Task t = taskAdapter.getTaskAt(row);
                TaskFormDialog dialog = new TaskFormDialog(this, t, projectService.getAllProjects(), taskService.getAllTasks(), db.getUsers(), t.getProjectId());
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    taskService.saveTask(dialog.getTask(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Task updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void handleTransitionStatus() {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task to transition status.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Task t = taskAdapter.getTaskAt(row);
            TaskStatus chosen = (TaskStatus) JOptionPane.showInputDialog(
                    this, String.format("Current Status: %s\nSelect target status for Task #%d '%s':", t.getStatus(), t.getId(), t.getTitle()),
                    "State Machine Workflow Transition", JOptionPane.QUESTION_MESSAGE,
                    null, TaskStatus.values(), t.getStatus());
            if (chosen != null && chosen != t.getStatus()) {
                try {
                    taskService.updateTaskStatus(t.getId(), chosen, currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Status successfully changed to: " + chosen.getLabel(), "Workflow Updated", JOptionPane.INFORMATION_MESSAGE);
                } catch (InvalidTaskStateException ex) {
                    JOptionPane.showMessageDialog(this, "State Machine Violation:\n" + ex.getMessage(), "Illegal Transition", JOptionPane.ERROR_MESSAGE);
                } catch (UnauthorizedAccessException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void handleLogHours() {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task to log hours against.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Task t = taskAdapter.getTaskAt(row);
            String input = JOptionPane.showInputDialog(this,
                    String.format("Task: #%d - %s\nCurrent Logged: %.2fh (Est: %.2fh)\n\nEnter additional hours to log:",
                            t.getId(), t.getTitle(), t.getLoggedHours(), t.getEstimatedHours()), "2.0");
            if (input != null) {
                try {
                    double hrs = Double.parseDouble(input.trim());
                    if (hrs <= 0) throw new NumberFormatException();
                    taskService.logHours(t.getId(), hrs, currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, String.format("Logged %.2f hours on task #%d successfully!", hrs, t.getId()), "Hours Recorded", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid numeric number of hours (> 0).", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        private void handleDeleteTask() {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Task t = taskAdapter.getTaskAt(row);

            // Mandatory Warning Message before Deleting
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("⚠️ WARNING: Are you sure you want to delete Task #%d '%s'?\n" +
                            "Any associated nested sub-tasks will also be removed!",
                            t.getId(), t.getTitle()),
                    "Confirm Task Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    taskService.deleteTask(t.getId(), currentUser);
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "Task #" + t.getId() + " was deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                } catch (UnauthorizedAccessException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
                } catch (TaskNotFoundException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. ENHANCED TEAM PANEL (ADD TEAM MEMBER - ADMIN ONLY)
        // ---------------------------------------------------------------------
        private JPanel createTeamPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setBackground(UITheme.BG_LIGHT);
            panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            JLabel heading = new JLabel("Engineering Team Roster & Polymorphic Profiles");
            heading.setFont(UITheme.FONT_HEADER);
            heading.setForeground(UITheme.TEXT_DARK);
            topBar.add(heading, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton btnAddMember = UITheme.createPrimaryButton("+ Add Team Member");
            btnAddMember.addActionListener(e -> handleAddTeamMember());

            JButton btnSwitch = UITheme.createSecondaryButton("🔑 Switch Active Session to Selected");
            btnSwitch.addActionListener(e -> {
                int row = teamTable.getSelectedRow();
                if (row >= 0) {
                    List<User> users = db.getUsers();
                    if (row < users.size()) {
                        switchSessionTo(users.get(row));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Select a user from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            actions.add(btnAddMember);
            actions.add(btnSwitch);
            topBar.add(actions, BorderLayout.EAST);

            panel.add(topBar, BorderLayout.NORTH);

            Vector<String> cols = new Vector<>(Arrays.asList("ID", "Full Name", "Username", "Role", "Department / Stack", "Rate / Limit", "Polymorphic Role Description"));
            teamModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            teamTable = new JTable(teamModel);
            teamTable.setRowHeight(34);
            teamTable.setFont(UITheme.FONT_REGULAR);
            teamTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            teamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scroll = new JScrollPane(teamTable);
            scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
            panel.add(scroll, BorderLayout.CENTER);
            return panel;
        }

        private void handleAddTeamMember() {
            // Admin only access restriction
            if (currentUser.getRole() != UserRole.MANAGER) {
                JOptionPane.showMessageDialog(this,
                        "Access Denied: Only Project Managers (Administrators) have permission to recruit and add new team members.\n" +
                                "Current Role: " + currentUser.getRole().getLabel(),
                        "Unauthorized", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AddTeamMemberDialog dialog = new AddTeamMemberDialog(this, db);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                User u = dialog.getCreatedUser();
                db.saveUser(u);
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Team member '" + u.getFullName() + "' (" + u.getRole().getLabel() + ") added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void switchSessionTo(User u) {
            this.currentUser = u;
            lblUserBadge.setText(currentUser.getProfileBadgeText()); // Dynamic dispatch
            refreshAllViews();
            JOptionPane.showMessageDialog(this, String.format("Active session switched to: %s\nRole: %s\nDescription: %s",
                    u.getFullName(), u.getRole().getLabel(), u.getRoleDescription()), "Session Switched", JOptionPane.INFORMATION_MESSAGE);
        }

        // ---------------------------------------------------------------------
        // REFRESH & WBS TREE BUILDER
        // ---------------------------------------------------------------------
        private void refreshAllViews() {
            List<Project> projs = projectService.getAllProjects();
            List<Task> tasks = taskService.getAllTasks();
            List<User> users = db.getUsers();

            // 1. Dashboard Metrics
            lblTotalProj.setText(String.valueOf(projs.size()));
            lblTotalTasks.setText(String.valueOf(tasks.size()));
            lblCompletedTasks.setText(String.valueOf(tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count()));
            lblActiveUsers.setText(String.valueOf(users.size()));

            long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
            long prog = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
            long rev = tasks.stream().filter(t -> t.getStatus() == TaskStatus.REVIEW).count();
            long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

            lblTodoCount.setText(todo + " Tasks (" + (tasks.isEmpty() ? 0 : (todo * 100 / tasks.size())) + "%)");
            lblProgCount.setText(prog + " Tasks (" + (tasks.isEmpty() ? 0 : (prog * 100 / tasks.size())) + "%)");
            lblReviewCount.setText(rev + " Tasks (" + (tasks.isEmpty() ? 0 : (rev * 100 / tasks.size())) + "%)");
            lblDoneCount.setText(done + " Tasks (" + (tasks.isEmpty() ? 0 : (done * 100 / tasks.size())) + "%)");

            // 2. Dashboard Project Progress Rollup Cards
            progressCardsPanel.removeAll();
            for (Project p : projs) {
                double pct = projectService.calculateProjectCompletion(p.getId()); // Recursive rollup
                JPanel r = new JPanel(new BorderLayout(14, 0));
                r.setOpaque(false);
                r.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

                JLabel l = new JLabel(String.format("<html><b>#%d: %s</b> <font color='#64748B'>[%s • $%,.0f]</font></html>",
                        p.getId(), p.getTitle(), p.getStatus(), p.getBudget()));
                l.setPreferredSize(new Dimension(320, 24));
                l.setFont(UITheme.FONT_REGULAR);

                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue((int) Math.round(pct));
                bar.setStringPainted(true);
                bar.setString(String.format("%.1f%%", pct));
                bar.setPreferredSize(new Dimension(240, 22));
                bar.setForeground(pct >= 100 ? UITheme.SUCCESS : (pct >= 50 ? UITheme.PRIMARY_ACCENT : UITheme.WARNING));

                r.add(l, BorderLayout.WEST);
                r.add(bar, BorderLayout.CENTER);
                progressCardsPanel.add(r);
            }
            progressCardsPanel.revalidate();
            progressCardsPanel.repaint();

            // 3. Project Table
            projectAdapter.setProjects(projs);

            // 4. Update Filter Combo
            String prevSelected = (String) cmbProjectFilter.getSelectedItem();
            cmbProjectFilter.removeAllItems();
            cmbProjectFilter.addItem("All Projects (" + projs.size() + ")");
            for (Project p : projs) cmbProjectFilter.addItem("#" + p.getId() + " • " + p.getTitle());
            if (prevSelected != null) cmbProjectFilter.setSelectedItem(prevSelected);

            applyTaskFilter();

            // 5. Build Perfect WBS Tree
            treeRoot.removeAllChildren();
            for (Project p : projs) {
                DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(String.format("📁 Project #%d: %s [%s]", p.getId(), p.getTitle(), p.getStatus()));
                List<Task> roots = projectService.buildTaskHierarchy(p.getId());
                for (Task rootTask : roots) {
                    pNode.add(buildTreeNodesRecursively(rootTask));
                }
                treeRoot.add(pNode);
            }
            treeModel.reload();
            for (int i = 0; i < hierarchyTree.getRowCount(); i++) hierarchyTree.expandRow(i);

            // 6. Team Table
            Vector<Vector<Object>> data = new Vector<>();
            for (User u : users) {
                Vector<Object> row = new Vector<>();
                row.add(u.getId());
                row.add(u.getFullName());
                row.add(u.getUsername());
                row.add(u.getRole().getLabel());
                if (u instanceof Manager) {
                    Manager m = (Manager) u;
                    row.add(m.getDepartment());
                    row.add(String.format("Limit: $%,.2f", m.getBudgetApprovalLimit()));
                } else if (u instanceof Developer) {
                    Developer d = (Developer) u;
                    row.add(d.getTechStack());
                    row.add(String.format("$%.2f/hr", d.getHourlyRate()));
                } else {
                    row.add("-");
                    row.add("-");
                }
                row.add(u.getRoleDescription()); // Dynamic method dispatch
                data.add(row);
            }
            teamModel.setDataVector(data, new Vector<>(Arrays.asList("ID", "Full Name", "Username", "Role", "Department / Stack", "Rate / Limit", "Polymorphic Role Description")));
        }

        private void applyTaskFilter() {
            List<Task> all = taskService.getAllTasks();
            int idx = cmbProjectFilter.getSelectedIndex();
            if (idx > 0) {
                List<Project> projs = projectService.getAllProjects();
                if (idx - 1 < projs.size()) {
                    int pid = projs.get(idx - 1).getId();
                    all.removeIf(t -> t.getProjectId() != pid);
                }
            }
            taskAdapter.setTasks(all);
        }

        private DefaultMutableTreeNode buildTreeNodesRecursively(Task t) {
            String label = String.format("📋 #%d: %s [%s] (%.0f%% • Depth: L%d)",
                    t.getId(), t.getTitle(), t.getStatus().getLabel(), t.calculateCompletionPercentage(), t.calculateDependencyDepth());
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TaskNodeItem(t, label));
            for (Task child : t.getSubTasks()) {
                node.add(buildTreeNodesRecursively(child)); // Recursive descent
            }
            return node;
        }

        private void showSwitchUserDialog() {
            List<User> users = db.getUsers();
            String[] names = users.stream().map(u -> u.getFullName() + " [" + u.getRole().getLabel() + "]").toArray(String[]::new);
            String pick = (String) JOptionPane.showInputDialog(this, "Select active user profile for session:", "Role Switcher",
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (pick != null) {
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals(pick)) {
                        switchSessionTo(users.get(i));
                        break;
                    }
                }
            }
        }

        private static class TaskNodeItem {
            final Task task;
            final String label;
            TaskNodeItem(Task task, String label) { this.task = task; this.label = label; }
            @Override public String toString() { return label; }
        }
    }

    // =====================================================================================
    // LOGIN DIALOG
    // =====================================================================================

    public static class LoginDialog extends JDialog {
        private final DatabaseManager db;
        private User authenticatedUser;

        public LoginDialog(Frame parent, DatabaseManager db) {
            super(parent, "KTU PBCST304 - Login", true);
            this.db = db;
            initComponents();
            setSize(480, 340);
            setLocationRelativeTo(parent);
            setResizable(false);
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel banner = new JPanel(new BorderLayout());
            banner.setBackground(UITheme.PRIMARY);
            banner.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
            JLabel t = new JLabel("Enterprise Project Management System");
            t.setFont(UITheme.FONT_HEADER);
            t.setForeground(Color.WHITE);
            JLabel s = new JLabel("APJ Abdul Kalam Technological University | PBCST304 OOP");
            s.setFont(UITheme.FONT_SMALL);
            s.setForeground(new Color(224, 231, 255));
            banner.add(t, BorderLayout.NORTH);
            banner.add(s, BorderLayout.SOUTH);
            add(banner, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.fill = GridBagConstraints.HORIZONTAL;

            g.gridx = 0; g.gridy = 0;
            form.add(new JLabel("Quick Demo Profile:"), g);

            g.gridx = 1;
            JComboBox<String> cmbQuick = new JComboBox<>(new String[]{
                    "Select Demo Profile...",
                    "Dr. Arun Kumar (Manager: admin)",
                    "Alice George (Developer: dev_alice)",
                    "Bob Varghese (Developer: dev_bob)"
            });
            form.add(cmbQuick, g);

            g.gridx = 0; g.gridy = 1;
            form.add(new JLabel("Username:"), g);

            g.gridx = 1;
            JTextField txtUser = new JTextField("admin", 16);
            form.add(txtUser, g);

            g.gridx = 0; g.gridy = 2;
            form.add(new JLabel("Password:"), g);

            g.gridx = 1;
            JPasswordField txtPass = new JPasswordField("admin123", 16);
            form.add(txtPass, g);

            cmbQuick.addActionListener(e -> {
                int idx = cmbQuick.getSelectedIndex();
                if (idx == 1) { txtUser.setText("admin"); txtPass.setText("admin123"); }
                else if (idx == 2) { txtUser.setText("dev_alice"); txtPass.setText("alice123"); }
                else if (idx == 3) { txtUser.setText("dev_bob"); txtPass.setText("bob123"); }
            });

            add(form, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            actions.setBackground(UITheme.BG_LIGHT);

            JButton btnExit = UITheme.createSecondaryButton("Exit");
            btnExit.addActionListener(e -> System.exit(0));

            JButton btnLogin = UITheme.createPrimaryButton("Sign In");
            btnLogin.addActionListener(e -> {
                String u = txtUser.getText().trim();
                String p = new String(txtPass.getPassword());
                Optional<User> auth = db.authenticate(u, p);
                if (auth.isPresent()) {
                    this.authenticatedUser = auth.get();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials! Use the Quick Demo dropdown.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnExit);
            actions.add(btnLogin);
            add(actions, BorderLayout.SOUTH);
        }

        public User getAuthenticatedUser() { return authenticatedUser; }
    }

    // =====================================================================================
    // APPLICATION ENTRY POINT: MAIN METHOD
    // =====================================================================================

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  KTU PBCST304 - Project Management System (Production Standard)       ");
        System.out.println("=======================================================================");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        DatabaseManager db = DatabaseManager.getInstance();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginDialog login = new LoginDialog(null, db);
                login.setVisible(true);

                User user = login.getAuthenticatedUser();
                if (user != null) {
                    AppLogger.info("Authenticated successfully as: %s [%s]", user.getFullName(), user.getRole().getLabel());
                    MainFrame frame = new MainFrame(db, user);
                    frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
