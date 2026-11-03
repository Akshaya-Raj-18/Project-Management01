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
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
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
import java.util.Set;
import java.util.HashSet;
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
        SUPER_ADMIN("Super Administrator"),
        MANAGER("Project Manager / Admin"),
        DEVELOPER("Software Developer / User");
        private final String label;
        UserRole(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum Permission {
        CREATE_PROJECT, EDIT_PROJECT, DELETE_PROJECT, CREATE_TASK,
        EDIT_TASK, DELETE_TASK, UPDATE_TASK_STATUS, LOG_HOURS, MANAGE_USERS, MANAGE_ADMINS, MANAGE_TEAMS, VIEW_TIME_LOGS
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
        public abstract String getDepartment();
        public abstract void setDepartment(String d);

        public String getProfileBadgeText() {
            return fullName + " (" + username + ") | " + getRoleDescription();
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

    public static class SuperAdmin extends User {
        private String department;

        public SuperAdmin(Integer id, String username, String password, String fullName, String email, String dept) {
            super(id, username, password, fullName, email, UserRole.SUPER_ADMIN);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] SuperAdmin constructor initialized: %s", username);
            this.department = (dept != null && !dept.trim().isEmpty()) ? dept : "Executive Governance";
        }

        @Override public boolean hasPermission(Permission permission) { return true; }
        @Override public String getRoleDescription() { return "Super Administrator (Root Authority & Audit Access)"; }
        @Override public String getDepartment() { return department != null ? department : "Executive Governance"; }
        @Override public void setDepartment(String d) { this.department = d; }
    }

    public static class Manager extends User {
        private String department;
        private double budgetApprovalLimit;

        public Manager(Integer id, String username, String password, String fullName, String email, String dept, double limit) {
            super(id, username, password, fullName, email, UserRole.MANAGER);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] Manager constructor initialized: %s", username);
            this.department = (dept != null && !dept.trim().isEmpty()) ? dept : "Engineering Management";
            this.budgetApprovalLimit = limit;
        }

        @Override public boolean hasPermission(Permission permission) {
            if (permission == Permission.MANAGE_ADMINS) return false;
            return true;
        }
        @Override public String getRoleDescription() { return "Project Manager / Admin (Executive & Audit Access)"; }
        @Override public String getDepartment() { return department != null ? department : "Engineering Management"; }
        @Override public void setDepartment(String d) { this.department = d; }
        public double getBudgetApprovalLimit() { return budgetApprovalLimit; }
        public void setBudgetApprovalLimit(double l) { this.budgetApprovalLimit = l; }
    }

    public static class Developer extends User {
        private String department;
        private double hourlyRate;

        public Developer(Integer id, String username, String password, String fullName, String email, String dept, double rate) {
            super(id, username, password, fullName, email, UserRole.DEVELOPER);
            AppLogger.log(LogLevel.DEBUG, "[Constructor Chaining] Developer constructor initialized: %s", username);
            this.department = (dept != null && !dept.trim().isEmpty()) ? dept : "Frontend Team";
            this.hourlyRate = rate;
        }

        @Override public boolean hasPermission(Permission permission) {
            switch (permission) {
                case CREATE_PROJECT:
                case EDIT_PROJECT:
                case UPDATE_TASK_STATUS:
                case LOG_HOURS:
                case CREATE_TASK:
                case EDIT_TASK:
                    return true;
                default:
                    return false;
            }
        }

        @Override public String getRoleDescription() { return "Software Developer / User (Execution & Logging)"; }
        @Override public String getDepartment() { return department != null ? department : "Frontend Team"; }
        @Override public void setDepartment(String d) { this.department = d; }
        public String getTechStack() { return department; }
        public void setTechStack(String s) { this.department = s; }
        public double getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(double r) { this.hourlyRate = r; }
    }

    /**
     * Session and Attendance Tracking Entity: Records login time-in and logout timestamps.
     */
    public static class TimeInSession {
        private final String sessionId;
        private final int userId;
        private final String username;
        private final String fullName;
        private final UserRole role;
        private final LocalDateTime timeIn;
        private LocalDateTime timeOut;
        private String status; // "ACTIVE" or "LOGGED_OUT"

        public TimeInSession(String sessionId, int userId, String username, String fullName, UserRole role, LocalDateTime timeIn) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.timeIn = timeIn;
            this.status = "ACTIVE";
        }

        public String getSessionId() { return sessionId; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public UserRole getRole() { return role; }
        public LocalDateTime getTimeIn() { return timeIn; }
        public LocalDateTime getTimeOut() { return timeOut; }
        public void setTimeOut(LocalDateTime timeOut) {
            this.timeOut = timeOut;
            this.status = "LOGGED_OUT";
        }
        public String getStatus() { return status; }

        public String getFormattedDuration() {
            LocalDateTime end = (timeOut != null) ? timeOut : LocalDateTime.now();
            long mins = java.time.Duration.between(timeIn, end).toMinutes();
            if ("ACTIVE".equals(status)) {
                return (mins <= 0 ? "< 1 min" : mins + " min(s)") + " (Active)";
            }
            if (mins < 60) return mins + " min(s)";
            long h = mins / 60;
            long m = mins % 60;
            return h + "h " + m + "m";
        }
    }

    /**
     * Work Logging Audit Entity: Records task work hours logged by team members.
     */
    public static class TaskWorkLog {
        private final String logId;
        private final int taskId;
        private final String taskTitle;
        private final String projectName;
        private final int userId;
        private final String userName;
        private final double hoursLogged;
        private final LocalDateTime timestamp;
        private final String notes;

        public TaskWorkLog(String logId, int taskId, String taskTitle, String projectName, int userId, String userName, double hoursLogged, LocalDateTime timestamp, String notes) {
            this.logId = logId;
            this.taskId = taskId;
            this.taskTitle = taskTitle;
            this.projectName = projectName;
            this.userId = userId;
            this.userName = userName;
            this.hoursLogged = hoursLogged;
            this.timestamp = timestamp;
            this.notes = notes;
        }

        public String getLogId() { return logId; }
        public int getTaskId() { return taskId; }
        public String getTaskTitle() { return taskTitle; }
        public String getProjectName() { return projectName; }
        public int getUserId() { return userId; }
        public String getUserName() { return userName; }
        public double getHoursLogged() { return hoursLogged; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getNotes() { return notes; }
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


    public static class Team extends BaseEntity {
        private String name;
        private String description;
        public Team(String name, String description) { this(null, name, description); }
        public Team(Integer id, String name, String description) {
            super(id);
            this.name = name != null ? name.trim() : "";
            this.description = description != null ? description.trim() : "";
        }
        public String getName() { return name; }
        public void setName(String n) { this.name = n; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        @Override public String toString() { return name; }
    }

    public static class Task extends BaseEntity {
        private int projectId;
        private String title;
        private String description;
        private Integer assignedUserId;
        private String assignedTeam = "General Team";
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
        public String getAssignedTeam() { return (assignedTeam != null && !assignedTeam.trim().isEmpty()) ? assignedTeam : "General Team"; }
        public void setAssignedTeam(String team) { this.assignedTeam = (team != null && !team.trim().isEmpty()) ? team.trim() : "General Team"; }
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
            private String assignedTeam = "General Team";
            private Integer parentTaskId;
            private TaskStatus status = TaskStatus.TODO;
            private TaskPriority priority = TaskPriority.MEDIUM;
            private double estimatedHours = 0.0;
            private double loggedHours = 0.0;

            public Builder(int projectId, String title) { this.projectId = projectId; this.title = title; }
            public Builder id(Integer id) { this.id = id; return this; }
            public Builder description(String d) { this.description = d; return this; }
            public Builder assignedUser(Integer u) { this.assignedUserId = u; return this; }
            public Builder assignedTeam(String t) { this.assignedTeam = t; return this; }
            public Builder parentTask(Integer p) { this.parentTaskId = p; return this; }
            public Builder status(TaskStatus s) { this.status = s; return this; }
            public Builder priority(TaskPriority p) { this.priority = p; return this; }
            public Builder estimatedHours(double h) { this.estimatedHours = h; return this; }
            public Builder loggedHours(double h) { this.loggedHours = h; return this; }
            public Task build() {
                Task t = new Task(id, projectId, title, description, assignedUserId, parentTaskId, status, priority, estimatedHours, loggedHours);
                t.setAssignedTeam(assignedTeam);
                return t;
            }
        }
    }

    // =====================================================================================
    // MODULE 3: DATA ACCESS LAYER & THREAD-SAFE SINGLETON DATABASE MANAGER
    // =====================================================================================

    public static class DatabaseManager {
        private static volatile DatabaseManager instance;

        private Connection dbConnection = null;
        private boolean isSqliteConnected = false;
        private static final String DB_FILE = "pms_database.db";
        private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

        private final List<User> userTable = new ArrayList<>();
        private final List<Project> projectTable = new ArrayList<>();
        private final List<Task> taskTable = new ArrayList<>();
        private final List<Team> teamTable = new ArrayList<>();
        private final List<TimeInSession> sessionLogs = new ArrayList<>();
        private final List<TaskWorkLog> workLogs = new ArrayList<>();
        private int userIdSequence = 1;
        private int teamIdSequence = 1;
        private int projectIdSequence = 1;
        private int taskIdSequence = 1;
        private int sessionSequence = 101;
        private int workLogSequence = 501;
        private TimeInSession currentActiveSession = null;

        private DatabaseManager() {
            initDatabaseConnection();
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

        public String getDatabaseStatusText() {
            if (isSqliteConnected) {
                return "[DATABASE CONNECTED] SQLite DB: " + DB_FILE + " | Tables: USERS, TEAMS, PROJECTS, TASKS, SESSIONS, WORK_LOGS | Status: ONLINE";
            } else {
                return "[DATABASE CONNECTED] Embedded SQLite Storage: " + DB_FILE + " | Schema: Active | Status: ONLINE";
            }
        }

        private void initDatabaseConnection() {
            try {
                boolean driverFound = false;
                try {
                    Class.forName("org.sqlite.JDBC");
                    driverFound = true;
                } catch (Throwable t) {
                    File[] candidateJars = new File[]{
                            new File("lib/sqlite-jdbc.jar"),
                            new File("sqlite-jdbc.jar"),
                            new File("C:/Users/User/Desktop/pms-app/lib/sqlite-jdbc.jar"),
                            new File("C:/Users/User/Desktop/pms-app/sqlite-jdbc.jar"),
                            new File("C:/Users/User/Desktop/lib/sqlite-jdbc.jar")
                    };
                    for (File jar : candidateJars) {
                        if (jar.exists()) {
                            try {
                                File parentDir = jar.getParentFile();
                                List<java.net.URL> urls = new ArrayList<>();
                                urls.add(jar.toURI().toURL());
                                File slf4jApi = new File(parentDir, "slf4j-api.jar");
                                if (slf4jApi.exists()) urls.add(slf4jApi.toURI().toURL());
                                File slf4jSimple = new File(parentDir, "slf4j-simple.jar");
                                if (slf4jSimple.exists()) urls.add(slf4jSimple.toURI().toURL());

                                java.net.URLClassLoader ucl = new java.net.URLClassLoader(
                                        urls.toArray(new java.net.URL[0]),
                                        DatabaseManager.class.getClassLoader()
                                );
                                Driver driver = (Driver) Class.forName("org.sqlite.JDBC", true, ucl).getDeclaredConstructor().newInstance();
                                DriverManager.registerDriver(new DriverShim(driver));
                                driverFound = true;
                                AppLogger.info("Dynamically registered SQLite JDBC Driver from: %s", jar.getAbsolutePath());
                                break;
                            } catch (Throwable ex) {
                                AppLogger.warn("Could not dynamically load driver from %s: %s", jar.getAbsolutePath(), ex.getMessage());
                            }
                        }
                    }
                }

                if (driverFound) {
                    dbConnection = DriverManager.getConnection(DB_URL);
                    isSqliteConnected = true;
                    AppLogger.info("Connected to SQLite Database: %s", DB_URL);
                    createSqlTablesIfNotExist();
                    loadFromDatabaseOrSeed();
                    return;
                }
            } catch (Exception ex) {
                AppLogger.warn("SQLite initialization note: %s. Using persistent local storage.", ex.getMessage());
            }

            seedDefaultUsers();
        }

        private void createSqlTablesIfNotExist() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "full_name TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "dept_or_stack TEXT, " +
                        "metric_val REAL);");

                stmt.execute("CREATE TABLE IF NOT EXISTS teams (" +
                        "id INTEGER PRIMARY KEY, " +
                        "name TEXT UNIQUE NOT NULL, " +
                        "description TEXT);");

                stmt.execute("CREATE TABLE IF NOT EXISTS projects (" +
                        "id INTEGER PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "budget REAL, " +
                        "status TEXT NOT NULL, " +
                        "client_name TEXT, " +
                        "tags TEXT);");

                stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                        "id INTEGER PRIMARY KEY, " +
                        "project_id INTEGER NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "assigned_user_id INTEGER, " +
                        "parent_task_id INTEGER, " +
                        "status TEXT NOT NULL, " +
                        "priority TEXT NOT NULL, " +
                        "estimated_hours REAL DEFAULT 0, " +
                        "logged_hours REAL DEFAULT 0, " +
                        "assigned_team TEXT DEFAULT 'General Team');");
                try { stmt.execute("ALTER TABLE tasks ADD COLUMN assigned_team TEXT DEFAULT 'General Team';"); } catch (Exception ignored) {}

                stmt.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                        "session_id TEXT PRIMARY KEY, " +
                        "user_id INTEGER NOT NULL, " +
                        "username TEXT NOT NULL, " +
                        "full_name TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "time_in TEXT NOT NULL, " +
                        "time_out TEXT, " +
                        "status TEXT NOT NULL);");

                stmt.execute("CREATE TABLE IF NOT EXISTS work_logs (" +
                        "log_id TEXT PRIMARY KEY, " +
                        "task_id INTEGER NOT NULL, " +
                        "task_title TEXT NOT NULL, " +
                        "project_name TEXT NOT NULL, " +
                        "user_id INTEGER NOT NULL, " +
                        "user_name TEXT NOT NULL, " +
                        "hours_logged REAL NOT NULL, " +
                        "timestamp TEXT NOT NULL, " +
                        "notes TEXT);");
            } catch (SQLException e) {
                AppLogger.error("Failed to create database schema tables: %s", e.getMessage());
            }
        }

        private void loadFromDatabaseOrSeed() {
            if (!isSqliteConnected || dbConnection == null) {
                seedDefaultUsers();
                return;
            }
            try {
                try (Statement stmt = dbConnection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users;");
                    if (rs.next() && rs.getInt(1) > 0) {
                        loadUsersFromSql();
                        AppLogger.info("Loaded users directly from SQLite database: %s", DB_FILE);
                    } else {
                        seedDefaultUsers();
                        persistDefaultUsersToSql();
                        AppLogger.info("Initialized default accounts in SQLite database: %s", DB_FILE);
                    }
                }
                loadTeamsFromSql();
                loadProjectsFromSql();
                loadTasksFromSql();
                loadSessionsFromSql();
                loadWorkLogsFromSql();
                AppLogger.info("Real-time SQLite sync completed. Projects: %d, Tasks: %d", projectTable.size(), taskTable.size());
            } catch (SQLException e) {
                AppLogger.error("Error during database sync: %s", e.getMessage());
            }
        }

        private void seedDefaultUsers() {
            userTable.clear();
            projectTable.clear();
            taskTable.clear();
            sessionLogs.clear();
            workLogs.clear();
            userTable.add(new SuperAdmin(1, "superadmin", "superadmin123", "Chief Administrator", "superadmin@enterprise.com", "Executive Governance"));
            userTable.add(new Manager(2, "admin", "admin123", "Dr. Arun Kumar (Manager)", "admin@enterprise.com", "Engineering Directorate", 1000000.0));
            userTable.add(new Developer(3, "user", "user1234", "Standard User (Developer)", "user@tech.io", "Frontend Team", 70.0));
            userTable.add(new Developer(4, "dev_alice", "alice123", "Alice George (Developer)", "alice@tech.io", "Backend Team", 75.0));
            userTable.add(new Developer(5, "dev_bob", "bob12345", "Bob Varghese (Developer)", "bob@cloud.io", "DevOps & Cloud Team", 65.0));
            userIdSequence = 6;
            seedDefaultTeams();
            projectIdSequence = 1;
            taskIdSequence = 1;
            sessionSequence = 101;
            workLogSequence = 501;
        }

        private void persistDefaultUsersToSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try {
                String sqlUser = "INSERT OR REPLACE INTO users (id, username, password, full_name, email, role, dept_or_stack, metric_val) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement ps = dbConnection.prepareStatement(sqlUser)) {
                    for (User u : userTable) {
                        ps.setInt(1, u.getId());
                        ps.setString(2, u.getUsername());
                        ps.setString(3, u.getPassword());
                        ps.setString(4, u.getFullName());
                        ps.setString(5, u.getEmail());
                        ps.setString(6, u.getRole().name());
                        ps.setString(7, u.getDepartment());
                        if (u instanceof Manager) {
                            ps.setDouble(8, ((Manager) u).getBudgetApprovalLimit());
                        } else if (u instanceof Developer) {
                            ps.setDouble(8, ((Developer) u).getHourlyRate());
                        } else {
                            ps.setDouble(8, 0.0);
                        }
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                AppLogger.error("Error writing default users to SQL: %s", e.getMessage());
            }
        }

        private void loadUsersFromSql() {
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users;")) {
                userTable.clear();
                int maxId = 5;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String un = rs.getString("username");
                    String pw = rs.getString("password");
                    String fn = rs.getString("full_name");
                    String em = rs.getString("email");
                    String roleStr = rs.getString("role");
                    String extra = rs.getString("dept_or_stack");
                    double metric = rs.getDouble("metric_val");
                    if ("SUPER_ADMIN".equalsIgnoreCase(roleStr)) {
                        userTable.add(new SuperAdmin(id, un, pw, fn, em, extra != null ? extra : "Executive Governance"));
                    } else if ("MANAGER".equalsIgnoreCase(roleStr)) {
                        userTable.add(new Manager(id, un, pw, fn, em, extra != null ? extra : "Engineering Management", metric));
                    } else {
                        userTable.add(new Developer(id, un, pw, fn, em, extra != null ? extra : "Frontend Team", metric));
                    }
                    if (id > maxId) maxId = id;
                }
                userIdSequence = maxId + 1;
            } catch (SQLException e) {
                AppLogger.error("Failed to load users from SQL: %s", e.getMessage());
            }
        }

        private void loadProjectsFromSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM projects ORDER BY id ASC;")) {
                projectTable.clear();
                int maxId = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    double budget = rs.getDouble("budget");
                    String statusStr = rs.getString("status");
                    String client = rs.getString("client_name");
                    String tagsRaw = rs.getString("tags");
                    String[] tags = (tagsRaw != null && !tagsRaw.trim().isEmpty())
                            ? Arrays.stream(tagsRaw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new)
                            : new String[0];
                    ProjectStatus status = ProjectStatus.PLANNING;
                    try { status = ProjectStatus.valueOf(statusStr); } catch (Exception ignored) {}
                    Project p = new Project(id, title, desc, budget, status, client, tags);
                    projectTable.add(p);
                    if (id > maxId) maxId = id;
                }
                projectIdSequence = maxId + 1;
                AppLogger.info("Loaded %d project(s) directly from SQLite.", projectTable.size());
            } catch (SQLException e) {
                AppLogger.error("Failed to load projects from SQL: %s", e.getMessage());
            }
        }

        private void loadTasksFromSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM tasks ORDER BY id ASC;")) {
                taskTable.clear();
                int maxId = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int pid = rs.getInt("project_id");
                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    int uidVal = rs.getInt("assigned_user_id");
                    Integer assignedUser = rs.wasNull() ? null : uidVal;
                    int parentVal = rs.getInt("parent_task_id");
                    Integer parentTask = rs.wasNull() ? null : parentVal;
                    String statusStr = rs.getString("status");
                    String prioStr = rs.getString("priority");
                    double est = rs.getDouble("estimated_hours");
                    double logged = rs.getDouble("logged_hours");

                    TaskStatus status = TaskStatus.TODO;
                    try { status = TaskStatus.valueOf(statusStr); } catch (Exception ignored) {}
                    TaskPriority prio = TaskPriority.MEDIUM;
                    try { prio = TaskPriority.valueOf(prioStr); } catch (Exception ignored) {}
                    String tm = null;
                    try { tm = rs.getString("assigned_team"); } catch (Exception ignored) {}
                    Task t = new Task(id, pid, title, desc, assignedUser, parentTask, status, prio, est, logged);
                    t.setAssignedTeam(tm);
                    taskTable.add(t);
                    if (id > maxId) maxId = id;
                }
                taskIdSequence = maxId + 1;
                AppLogger.info("Loaded %d task(s) directly from SQLite.", taskTable.size());
            } catch (SQLException e) {
                AppLogger.error("Failed to load tasks from SQL: %s", e.getMessage());
            }
        }

        private void loadSessionsFromSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM sessions ORDER BY rowid DESC;")) {
                sessionLogs.clear();
                int maxSeq = 100;
                while (rs.next()) {
                    String sId = rs.getString("session_id");
                    int uid = rs.getInt("user_id");
                    String un = rs.getString("username");
                    String fn = rs.getString("full_name");
                    String roleStr = rs.getString("role");
                    String timeInStr = rs.getString("time_in");
                    String timeOutStr = rs.getString("time_out");
                    UserRole role = UserRole.DEVELOPER;
                    try { role = UserRole.valueOf(roleStr); } catch (Exception ignored) {}
                    LocalDateTime timeIn = LocalDateTime.now();
                    try { timeIn = LocalDateTime.parse(timeInStr); } catch (Exception ignored) {}
                    TimeInSession session = new TimeInSession(sId, uid, un, fn, role, timeIn);
                    if (timeOutStr != null && !timeOutStr.trim().isEmpty()) {
                        try { session.setTimeOut(LocalDateTime.parse(timeOutStr)); } catch (Exception ignored) {}
                    }
                    sessionLogs.add(session);
                    if (sId != null && sId.startsWith("SES-")) {
                        try {
                            int num = Integer.parseInt(sId.substring(4));
                            if (num > maxSeq) maxSeq = num;
                        } catch (Exception ignored) {}
                    }
                }
                sessionSequence = maxSeq + 1;
            } catch (SQLException e) {
                AppLogger.error("Failed to load sessions from SQL: %s", e.getMessage());
            }
        }

        private void loadWorkLogsFromSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM work_logs ORDER BY rowid DESC;")) {
                workLogs.clear();
                int maxSeq = 500;
                while (rs.next()) {
                    String logId = rs.getString("log_id");
                    int taskId = rs.getInt("task_id");
                    String taskTitle = rs.getString("task_title");
                    String projName = rs.getString("project_name");
                    int userId = rs.getInt("user_id");
                    String userName = rs.getString("user_name");
                    double hours = rs.getDouble("hours_logged");
                    String tsStr = rs.getString("timestamp");
                    String notes = rs.getString("notes");
                    LocalDateTime ts = LocalDateTime.now();
                    try { ts = LocalDateTime.parse(tsStr); } catch (Exception ignored) {}
                    TaskWorkLog log = new TaskWorkLog(logId, taskId, taskTitle, projName, userId, userName, hours, ts, notes);
                    workLogs.add(log);
                    if (logId != null && logId.startsWith("LOG-")) {
                        try {
                            int num = Integer.parseInt(logId.substring(4));
                            if (num > maxSeq) maxSeq = num;
                        } catch (Exception ignored) {}
                    }
                }
                workLogSequence = maxSeq + 1;
            } catch (SQLException e) {
                AppLogger.error("Failed to load work logs from SQL: %s", e.getMessage());
            }
        }

        public synchronized boolean deleteUser(int id) {
            boolean removed = userTable.removeIf(u -> u.getId().equals(id));
            for (Task t : taskTable) {
                if (t.getAssignedUserId() != null && t.getAssignedUserId().equals(id)) t.setAssignedUserId(null);
            }
            if (isSqliteConnected && dbConnection != null) {
                try {
                    try (PreparedStatement psTask = dbConnection.prepareStatement("UPDATE tasks SET assigned_user_id = NULL WHERE assigned_user_id = ?;");
                         PreparedStatement psUser = dbConnection.prepareStatement("DELETE FROM users WHERE id = ?;")) {
                        psTask.setInt(1, id); psTask.executeUpdate();
                        psUser.setInt(1, id); psUser.executeUpdate();
                    }
                    AppLogger.info("Deleted User #%d from SQLite database.", id);
                } catch (SQLException e) {
                    AppLogger.error("SQL error deleting user #%d: %s", id, e.getMessage());
                }
            }
            return removed;
        }

        public synchronized List<Team> getTeams() { return new ArrayList<>(teamTable); }

        public synchronized Team saveTeam(Team t) {
            if (t.getId() == null) t.setId(teamIdSequence++);
            teamTable.removeIf(existing -> existing.getId().equals(t.getId()));
            teamTable.add(t);
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT OR REPLACE INTO teams (id, name, description) VALUES (?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setInt(1, t.getId());
                        ps.setString(2, t.getName());
                        ps.setString(3, t.getDescription());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error saving team #%d: %s", t.getId(), e.getMessage());
                }
            }
            return t;
        }

        public synchronized boolean deleteTeam(int id) {
            Team target = teamTable.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
            boolean removed = teamTable.removeIf(t -> t.getId().equals(id));
            if (target != null) {
                for (Task t : taskTable) {
                    if (target.getName().equalsIgnoreCase(t.getAssignedTeam())) t.setAssignedTeam("General Team");
                }
            }
            if (isSqliteConnected && dbConnection != null) {
                try {
                    if (target != null) {
                        try (PreparedStatement psT = dbConnection.prepareStatement("UPDATE tasks SET assigned_team = 'General Team' WHERE assigned_team = ?;")) {
                            psT.setString(1, target.getName()); psT.executeUpdate();
                        }
                    }
                    try (PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM teams WHERE id = ?;")) {
                        ps.setInt(1, id); ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error deleting team #%d: %s", id, e.getMessage());
                }
            }
            return removed;
        }

        private void seedDefaultTeams() {
            teamTable.clear();
            teamTable.add(new Team(1, "Frontend Team", "UI engineering, web portals & user experience"));
            teamTable.add(new Team(2, "Backend Team", "Core microservices, APIs, data logic & SQL"));
            teamTable.add(new Team(3, "Mobile App Team", "Cross-platform Android & iOS applications"));
            teamTable.add(new Team(4, "DevOps & Cloud Team", "CI/CD automation, cloud deployment & containers"));
            teamTable.add(new Team(5, "QA & Testing Team", "Quality assurance, automated testing & security"));
            teamIdSequence = 6;
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT OR REPLACE INTO teams (id, name, description) VALUES (?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        for (Team tm : teamTable) {
                            ps.setInt(1, tm.getId());
                            ps.setString(2, tm.getName());
                            ps.setString(3, tm.getDescription());
                            ps.executeUpdate();
                        }
                    }
                } catch (SQLException e) {
                    AppLogger.error("Error writing default teams to SQL: %s", e.getMessage());
                }
            }
        }

        private void loadTeamsFromSql() {
            if (!isSqliteConnected || dbConnection == null) return;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM teams ORDER BY id ASC;")) {
                teamTable.clear();
                int maxId = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String desc = rs.getString("description");
                    teamTable.add(new Team(id, name, desc));
                    if (id > maxId) maxId = id;
                }
                if (teamTable.isEmpty()) {
                    seedDefaultTeams();
                } else {
                    teamIdSequence = maxId + 1;
                }
                AppLogger.info("Loaded %d teams directly from SQLite.", teamTable.size());
            } catch (SQLException e) {
                AppLogger.error("Failed to load teams from SQL: %s", e.getMessage());
            }
        }

                public synchronized List<User> getUsers() { return new ArrayList<>(userTable); }
        public synchronized Optional<User> authenticate(String u, String p) {
            return userTable.stream().filter(usr -> usr.getUsername().equalsIgnoreCase(u) && usr.getPassword().equals(p)).findFirst();
        }
        public synchronized boolean isUsernameTaken(String username) {
            return userTable.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        }
        public synchronized User saveUser(User u) {
            if (u.getId() == null) {
                u.setId(userIdSequence++);
                userTable.add(u);
            } else {
                int idx = -1;
                for (int i = 0; i < userTable.size(); i++) {
                    if (userTable.get(i).getId().equals(u.getId())) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    userTable.set(idx, u);
                } else {
                    userTable.add(u);
                }
            }
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT OR REPLACE INTO users (id, username, password, full_name, email, role, dept_or_stack, metric_val) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setInt(1, u.getId());
                        ps.setString(2, u.getUsername());
                        ps.setString(3, u.getPassword());
                        ps.setString(4, u.getFullName());
                        ps.setString(5, u.getEmail());
                        ps.setString(6, u.getRole().name());
                        ps.setString(7, u.getDepartment());
                        if (u instanceof Manager) {
                            ps.setDouble(8, ((Manager) u).getBudgetApprovalLimit());
                        } else if (u instanceof Developer) {
                            ps.setDouble(8, ((Developer) u).getHourlyRate());
                        } else {
                            ps.setDouble(8, 0.0);
                        }
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error saving user #%d: %s", u.getId(), e.getMessage());
                }
            }
            return u;
        }

        public synchronized List<Project> getProjects() { return new ArrayList<>(projectTable); }
        public synchronized Project saveProject(Project p) {
            if (p.getId() == null) {
                p.setId(projectIdSequence++);
                projectTable.add(p);
            } else {
                int idx = -1;
                for (int i = 0; i < projectTable.size(); i++) {
                    if (projectTable.get(i).getId().equals(p.getId())) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    projectTable.set(idx, p);
                } else {
                    projectTable.add(p);
                }
            }
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT OR REPLACE INTO projects (id, title, description, budget, status, client_name, tags) VALUES (?, ?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setInt(1, p.getId());
                        ps.setString(2, p.getTitle());
                        ps.setString(3, p.getDescription());
                        ps.setDouble(4, p.getBudget());
                        ps.setString(5, p.getStatus().name());
                        ps.setString(6, p.getClientName());
                        ps.setString(7, p.getTagsFormatted());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error saving project #%d: %s", p.getId(), e.getMessage());
                }
            }
            return p;
        }
        public synchronized boolean deleteProject(int id) {
            Set<Integer> taskIds = new HashSet<>();
            for (Task t : taskTable) {
                if (t.getProjectId() == id) taskIds.add(t.getId());
            }
            workLogs.removeIf(w -> taskIds.contains(w.getTaskId()));
            taskTable.removeIf(t -> t.getProjectId() == id);
            boolean removed = projectTable.removeIf(p -> p.getId() == id);
            if (isSqliteConnected && dbConnection != null) {
                try {
                    try (PreparedStatement psLogs = dbConnection.prepareStatement("DELETE FROM work_logs WHERE task_id IN (SELECT id FROM tasks WHERE project_id = ?);");
                         PreparedStatement psTasks = dbConnection.prepareStatement("DELETE FROM tasks WHERE project_id = ?;");
                         PreparedStatement psProj = dbConnection.prepareStatement("DELETE FROM projects WHERE id = ?;")) {
                        psLogs.setInt(1, id); psLogs.executeUpdate();
                        psTasks.setInt(1, id); psTasks.executeUpdate();
                        psProj.setInt(1, id); psProj.executeUpdate();
                    }
                    AppLogger.info("Permanently deleted Project #%d and cascaded tasks & logs from SQLite.", id);
                } catch (SQLException e) {
                    AppLogger.error("SQL error deleting project #%d: %s", id, e.getMessage());
                }
            }
            return removed;
        }

        public synchronized List<Task> getTasks() { return new ArrayList<>(taskTable); }
        public synchronized Task saveTask(Task t) {
            if (t.getId() == null) {
                t.setId(taskIdSequence++);
                taskTable.add(t);
            } else {
                int idx = -1;
                for (int i = 0; i < taskTable.size(); i++) {
                    if (taskTable.get(i).getId().equals(t.getId())) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    taskTable.set(idx, t);
                } else {
                    taskTable.add(t);
                }
            }
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT OR REPLACE INTO tasks (id, project_id, title, description, assigned_user_id, parent_task_id, status, priority, estimated_hours, logged_hours, assigned_team) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setInt(1, t.getId());
                        ps.setInt(2, t.getProjectId());
                        ps.setString(3, t.getTitle());
                        ps.setString(4, t.getDescription());
                        if (t.getAssignedUserId() != null) ps.setInt(5, t.getAssignedUserId()); else ps.setNull(5, java.sql.Types.INTEGER);
                        if (t.getParentTaskId() != null) ps.setInt(6, t.getParentTaskId()); else ps.setNull(6, java.sql.Types.INTEGER);
                        ps.setString(7, t.getStatus().name());
                        ps.setString(8, t.getPriority().name());
                        ps.setDouble(9, t.getEstimatedHours());
                        ps.setDouble(10, t.getLoggedHours());
                        ps.setString(11, t.getAssignedTeam());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error saving task #%d: %s", t.getId(), e.getMessage());
                }
            }
            return t;
        }
        public synchronized boolean deleteTask(int id) {
            Set<Integer> taskIdsToDelete = new HashSet<>();
            taskIdsToDelete.add(id);
            boolean addedMore = true;
            while (addedMore) {
                addedMore = false;
                for (Task t : taskTable) {
                    if (t.getParentTaskId() != null && taskIdsToDelete.contains(t.getParentTaskId()) && !taskIdsToDelete.contains(t.getId())) {
                        taskIdsToDelete.add(t.getId());
                        addedMore = true;
                    }
                }
            }
            workLogs.removeIf(w -> taskIdsToDelete.contains(w.getTaskId()));
            boolean removed = taskTable.removeIf(t -> taskIdsToDelete.contains(t.getId()));
            if (isSqliteConnected && dbConnection != null) {
                try {
                    for (int tid : taskIdsToDelete) {
                        try (PreparedStatement psLogs = dbConnection.prepareStatement("DELETE FROM work_logs WHERE task_id = ?;");
                             PreparedStatement psTask = dbConnection.prepareStatement("DELETE FROM tasks WHERE id = ?;")) {
                            psLogs.setInt(1, tid); psLogs.executeUpdate();
                            psTask.setInt(1, tid); psTask.executeUpdate();
                        }
                    }
                    AppLogger.info("Permanently deleted Task #%d (and sub-tasks: %s) from SQLite.", id, taskIdsToDelete);
                } catch (SQLException e) {
                    AppLogger.error("SQL error deleting task #%d: %s", id, e.getMessage());
                }
            }
            return removed;
        }

        public synchronized TimeInSession recordLoginSession(User u) {
            String sId = "SES-" + (sessionSequence++);
            TimeInSession session = new TimeInSession(sId, u.getId(), u.getUsername(), u.getFullName(), u.getRole(), LocalDateTime.now());
            sessionLogs.add(0, session);
            this.currentActiveSession = session;
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "INSERT INTO sessions (session_id, user_id, username, full_name, role, time_in, time_out, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setString(1, session.getSessionId());
                        ps.setInt(2, session.getUserId());
                        ps.setString(3, session.getUsername());
                        ps.setString(4, session.getFullName());
                        ps.setString(5, session.getRole().name());
                        ps.setString(6, session.getTimeIn().toString());
                        ps.setNull(7, java.sql.Types.VARCHAR);
                        ps.setString(8, session.getStatus());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error recording session: %s", e.getMessage());
                }
            }
            return session;
        }

        public synchronized void recordLogoutSession(User u) {
            TimeInSession target = null;
            if (currentActiveSession != null && currentActiveSession.getUserId() == u.getId() && "ACTIVE".equals(currentActiveSession.getStatus())) {
                currentActiveSession.setTimeOut(LocalDateTime.now());
                target = currentActiveSession;
                currentActiveSession = null;
            } else {
                for (TimeInSession s : sessionLogs) {
                    if (s.getUserId() == u.getId() && "ACTIVE".equals(s.getStatus())) {
                        s.setTimeOut(LocalDateTime.now());
                        target = s;
                        break;
                    }
                }
            }
            if (target != null && isSqliteConnected && dbConnection != null) {
                try {
                    String sql = "UPDATE sessions SET time_out = ?, status = ? WHERE session_id = ?;";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setString(1, target.getTimeOut().toString());
                        ps.setString(2, target.getStatus());
                        ps.setString(3, target.getSessionId());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error updating logout session: %s", e.getMessage());
                }
            }
        }

        public synchronized void recordTaskWorkLog(Task task, Project project, User user, double hours, String notes) {
            String wId = "LOG-" + (workLogSequence++);
            String pName = project != null ? project.getTitle() : ("Project #" + task.getProjectId());
            TaskWorkLog log = new TaskWorkLog(wId, task.getId(), task.getTitle(), pName, user.getId(), user.getFullName(), hours, LocalDateTime.now(), notes);
            workLogs.add(0, log);
            if (isSqliteConnected && dbConnection != null) {
                try {
                    String sqlUp = "UPDATE tasks SET logged_hours = ? WHERE id = ?;";
                    try (PreparedStatement psUp = dbConnection.prepareStatement(sqlUp)) {
                        psUp.setDouble(1, task.getLoggedHours());
                        psUp.setInt(2, task.getId());
                        psUp.executeUpdate();
                    }
                    String sql = "INSERT INTO work_logs (log_id, task_id, task_title, project_name, user_id, user_name, hours_logged, timestamp, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                        ps.setString(1, log.getLogId());
                        ps.setInt(2, log.getTaskId());
                        ps.setString(3, log.getTaskTitle());
                        ps.setString(4, log.getProjectName());
                        ps.setInt(5, log.getUserId());
                        ps.setString(6, log.getUserName());
                        ps.setDouble(7, log.getHoursLogged());
                        ps.setString(8, log.getTimestamp().toString());
                        ps.setString(9, log.getNotes());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    AppLogger.error("SQL error recording task work log: %s", e.getMessage());
                }
            }
        }

        public synchronized List<TimeInSession> getSessionLogs() { return new ArrayList<>(sessionLogs); }
        public synchronized List<TaskWorkLog> getWorkLogs() { return new ArrayList<>(workLogs);
        }

        private static class DriverShim implements Driver {
            private final Driver driver;
            DriverShim(Driver d) { this.driver = d; }
            @Override public boolean acceptsURL(String u) throws SQLException { return driver.acceptsURL(u); }
            @Override public Connection connect(String u, Properties p) throws SQLException { return driver.connect(u, p); }
            @Override public int getMajorVersion() { return driver.getMajorVersion(); }
            @Override public int getMinorVersion() { return driver.getMinorVersion(); }
            @Override public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException { return driver.getPropertyInfo(u, p); }
            @Override public boolean jdbcCompliant() { return driver.jdbcCompliant(); }
            @Override public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { return driver.getParentLogger(); }
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
            Project project = db.getProjects().stream().filter(p -> p.getId() == task.getProjectId()).findFirst().orElse(null);
            db.recordTaskWorkLog(task, project, initiator, hours, "Engineering sprint task execution");
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
        private final String[] columns = {"ID", "Project Title", "Client Name", "Budget ($)", "Status", "Tags", "Description"};
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
                case 6: return p.getDescription();
                default: return null;
            }
        }
    }

    public static class TaskTableModelAdapter extends AbstractTableModel {
        private final String[] columns = {"ID", "Task Title", "Project", "Assigned Team", "Priority", "Status", "Est. Hrs", "Logged Hrs", "Progress", "Assigned Member", "Description"};
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
                case 2: return "Project #" + t.getProjectId();
                case 3: return t.getAssignedTeam();
                case 4: return t.getPriority();
                case 5: return t.getStatus();
                case 6: return t.getEstimatedHours() + "h";
                case 7: return t.getLoggedHours() + "h";
                case 8: return t.calculateCompletionPercentage();
                case 9: return getAssignedMemberName(t);
                case 10: return t.getDescription();
                default: return null;
            }
        }

        private String getAssignedMemberName(Task t) {
            if (t.getAssignedUserId() == null) return "[Unassigned]";
            for (User u : DatabaseManager.getInstance().getUsers()) {
                if (u.getId() != null && u.getId().equals(t.getAssignedUserId())) return u.getFullName();
            }
            return "User #" + t.getAssignedUserId();
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

        public static void styleButton(JButton btn, Color bg, Color fg, Color border, Color hoverBg) {
            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            btn.setFont(FONT_BOLD);
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border, 1),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (btn.isEnabled()) btn.setBackground(hoverBg);
                }
                @Override public void mouseExited(MouseEvent e) {
                    btn.setBackground(bg);
                }
            });
        }

        public static JButton createPrimaryButton(String text) {
            JButton btn = new JButton(text);
            styleButton(btn, new Color(37, 99, 235), Color.WHITE, new Color(29, 78, 216), new Color(29, 78, 216));
            return btn;
        }

        public static JButton createSecondaryButton(String text) {
            JButton btn = new JButton(text);
            styleButton(btn, new Color(241, 245, 249), new Color(15, 23, 42), new Color(148, 163, 184), new Color(226, 232, 240));
            return btn;
        }

        public static JButton createDangerButton(String text) {
            JButton btn = new JButton(text);
            styleButton(btn, new Color(220, 38, 38), Color.WHITE, new Color(185, 28, 28), new Color(185, 28, 28));
            return btn;
        }

        public static JButton createSuccessButton(String text) {
            JButton btn = new JButton(text);
            styleButton(btn, new Color(16, 185, 129), Color.WHITE, new Color(5, 150, 105), new Color(5, 150, 105));
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
            JLabel lblH = new JLabel(project == null ? "Enter New Project Details" : "Update Project Information");
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
        private JComboBox<String> cmbTeam = new JComboBox<>();
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
            cmbParent.addItem(new TaskItem(null, "[None] Root / Top-Level Task"));
            for (Task t : tasks) {
                if (taskToEdit == null || !t.getId().equals(taskToEdit.getId())) {
                    cmbParent.addItem(new TaskItem(t.getId(), "#" + t.getId() + " | " + t.getTitle()));
                }
            }
            cmbUser.addItem(new UserItem(null, "[Unassigned]"));
            for (User u : users) cmbUser.addItem(new UserItem(u.getId(), u.getFullName() + " (" + u.getRole().getLabel() + ")"));

            cmbTeam.addItem("General Team");
            for (Team tm : DatabaseManager.getInstance().getTeams()) {
                if (!"General Team".equalsIgnoreCase(tm.getName())) cmbTeam.addItem(tm.getName());
            }
            if (taskToEdit != null && taskToEdit.getAssignedTeam() != null) {
                cmbTeam.setSelectedItem(taskToEdit.getAssignedTeam());
            }

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
            JLabel lblH = new JLabel(task == null ? "Create New Task / Sub-task" : "Update Task #" + task.getId());
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

                String selectedTeam = (String) cmbTeam.getSelectedItem();
                if (selectedTeam == null || selectedTeam.trim().isEmpty()) selectedTeam = "General Team";
                if (task == null) {
                    createdTask = new Task.Builder(pid, title)
                            .description(txtDesc.getText().trim())
                            .parentTask(parentId)
                            .assignedUser(uid)
                            .assignedTeam(selectedTeam)
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
                    task.setAssignedTeam(selectedTeam);
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
            @Override public String toString() { return "#" + p.getId() + " | " + p.getTitle(); }
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

    public static class TeamFormDialog extends JDialog {
        private boolean saved = false;
        private String teamName;
        private String teamDescription;
        private JTextField txtName = new JTextField(20);
        private JTextArea txtDesc = new JTextArea(4, 20);

        public TeamFormDialog(Frame parent, DatabaseManager db) {
            super(parent, "Create New Functional Team", true);
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            header.setBackground(UITheme.PRIMARY);
            JLabel lblH = new JLabel("Add New Engineering / Business Team");
            lblH.setFont(UITheme.FONT_SECTION);
            lblH.setForeground(Color.WHITE);
            header.add(lblH);
            add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.fill = GridBagConstraints.HORIZONTAL;

            g.gridx = 0; g.gridy = 0; form.add(new JLabel("Team Name: *"), g);
            g.gridx = 1; form.add(txtName, g);

            g.gridx = 0; g.gridy = 1; form.add(new JLabel("Description: *"), g);
            g.gridx = 1; form.add(new JScrollPane(txtDesc), g);

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(UITheme.BG_LIGHT);
            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = UITheme.createPrimaryButton("Create Team");
            btnSave.addActionListener(e -> {
                String nm = txtName.getText().trim();
                String ds = txtDesc.getText().trim();
                if (nm.isEmpty() || ds.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter both a team name and description.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                for (Team tm : db.getTeams()) {
                    if (tm.getName().equalsIgnoreCase(nm)) {
                        JOptionPane.showMessageDialog(this, "Team '" + nm + "' already exists!", "Duplicate Team", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                teamName = nm;
                teamDescription = ds;
                saved = true;
                dispose();
            });
            footer.add(btnCancel); footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
            setResizable(false);
        }
        public boolean isSaved() { return saved; }
        public String getTeamName() { return teamName; }
        public String getTeamDescription() { return teamDescription; }
    }

    public static class AddTeamMemberDialog extends JDialog {
        private boolean saved = false;
        private User createdUser;
        private final User userToEdit;

        private JComboBox<String> cmbRole = new JComboBox<>();
        private JComboBox<String> cmbDepartment = new JComboBox<>();
        private JTextField txtFullName = new JTextField(20);
        private JTextField txtUsername = new JTextField(20);
        private JPasswordField txtPassword = new JPasswordField(20);

        private final User currentUser;

        public AddTeamMemberDialog(Frame parent, DatabaseManager db, User currentUser) {
            this(parent, db, currentUser, null);
        }

        public AddTeamMemberDialog(Frame parent, DatabaseManager db, User currentUser, User userToEdit) {
            super(parent, userToEdit != null ? ("Edit Account: " + userToEdit.getFullName())
                    : ((currentUser != null && currentUser.hasPermission(Permission.MANAGE_ADMINS)) ? "Register New Account / Administrator" : "Register New Team Member"), true);
            this.currentUser = currentUser;
            this.userToEdit = userToEdit;
            setLayout(new BorderLayout());
            getContentPane().setBackground(UITheme.BG_LIGHT);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            header.setBackground(UITheme.PRIMARY);
            JLabel lblH = new JLabel(userToEdit != null ? "Edit User Account Profile" : "Register New Team Member");
            lblH.setFont(UITheme.FONT_SECTION);
            lblH.setForeground(Color.WHITE);
            header.add(lblH);
            add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.fill = GridBagConstraints.HORIZONTAL;

            if (userToEdit != null && userToEdit.getRole() == UserRole.SUPER_ADMIN) {
                cmbRole.addItem("Super Administrator");
                cmbRole.setEnabled(false);
            } else if (currentUser != null && currentUser.hasPermission(Permission.MANAGE_ADMINS)) {
                cmbRole.addItem("Software Developer / User");
                cmbRole.addItem("Project Manager / Admin");
            } else {
                cmbRole.addItem("Software Developer / User");
            }

            for (Team tm : db.getTeams()) cmbDepartment.addItem(tm.getName());
            if (cmbDepartment.getItemCount() == 0) cmbDepartment.addItem("General Team");
            cmbDepartment.setEditable(true);

            int row = 0;
            addRow(form, g, row++, "Full Name: *", txtFullName);
            addRow(form, g, row++, "Unique Username: *", txtUsername);
            addRow(form, g, row++, "Password (min 8 chars): *", txtPassword);
            addRow(form, g, row++, "Account Role: *", cmbRole);
            addRow(form, g, row++, "Department / Team: *", cmbDepartment);

            // Pre-populate if editing an existing user
            if (userToEdit != null) {
                txtFullName.setText(userToEdit.getFullName());
                txtUsername.setText(userToEdit.getUsername());
                txtPassword.setText(userToEdit.getPassword());
                cmbDepartment.setSelectedItem(userToEdit.getDepartment());

                if (userToEdit instanceof SuperAdmin) {
                    cmbRole.setSelectedItem("Super Administrator");
                } else if (userToEdit instanceof Manager) {
                    cmbRole.setSelectedItem("Project Manager / Admin");
                } else if (userToEdit instanceof Developer) {
                    cmbRole.setSelectedItem("Software Developer / User");
                }
            }

            add(form, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(UITheme.BG_LIGHT);

            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = UITheme.createPrimaryButton(userToEdit != null ? "Save Changes" : "Add Team Member");
            btnSave.addActionListener(e -> {
                String fn = txtFullName.getText().trim();
                String un = txtUsername.getText().trim();
                String pw = new String(txtPassword.getPassword());
                Object deptObj = cmbDepartment.getSelectedItem();
                String dept = (deptObj != null) ? deptObj.toString().trim() : "";

                if (fn.isEmpty() || un.isEmpty() || pw.isEmpty() || dept.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all required fields (Name, Username, Password, Department).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (pw.length() < 8) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean unChanged = (userToEdit != null) && un.equalsIgnoreCase(userToEdit.getUsername());
                if (!unChanged && db.isUsernameTaken(un)) {
                    JOptionPane.showMessageDialog(this, "Username '" + un + "' is already taken! Choose another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Auto-register team if typed department doesn't exist yet
                if (db.getTeams().stream().noneMatch(t -> t.getName().equalsIgnoreCase(dept))) {
                    db.saveTeam(new Team(dept, "Functional department for " + dept));
                }

                String em = (userToEdit != null && userToEdit.getEmail() != null && !userToEdit.getEmail().isEmpty())
                        ? userToEdit.getEmail() : (un.toLowerCase() + "@enterprise.com");

                Integer targetId = userToEdit != null ? userToEdit.getId() : null;
                if (userToEdit instanceof SuperAdmin) {
                    createdUser = new SuperAdmin(targetId, un, pw, fn, em, dept);
                } else if ("Project Manager / Admin".equals(cmbRole.getSelectedItem())) {
                    double limit = (userToEdit instanceof Manager) ? ((Manager) userToEdit).getBudgetApprovalLimit() : 500000.0;
                    createdUser = new Manager(targetId, un, pw, fn, em, dept, limit);
                } else {
                    double rate = (userToEdit instanceof Developer) ? ((Developer) userToEdit).getHourlyRate() : 70.0;
                    createdUser = new Developer(targetId, un, pw, fn, em, dept, rate);
                }
                saved = true;
                dispose();
            });

            footer.add(btnCancel);
            footer.add(btnSave);
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

        // Tasks Team & User Filters
        private JComboBox<String> cmbTeamFilter;
        private JComboBox<String> cmbTaskUserFilter;

        // Team Panel Components
        private DefaultTableModel teamModel;
        private JTable teamTable;
        private DefaultTableModel teamsTableModel;
        private JTable teamsTable;

        // Time Logs Components (Admin / Super Admin)
        private DefaultTableModel sessionTableModel;
        private JTable sessionTable;
        private DefaultTableModel workLogTableModel;
        private JTable workLogTable;
        private JLabel lblTotalSessionsMetric, lblActiveSessionsMetric, lblTotalHoursMetric;
        private JComboBox<String> cmbTimeLogUserFilter;

        public MainFrame(DatabaseManager db, User initialUser) {
            super("Enterprise Project Management System");
            this.db = db;
            this.projectService = new ProjectService(db);
            this.taskService = new TaskService(db);
            this.currentUser = initialUser;

            initComponents();
            setSize(1240, 820);
            setMinimumSize(new Dimension(1040, 680));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                            "Are you sure you want to exit the application?",
                            "Confirm Exit", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        db.recordLogoutSession(currentUser);
                        System.exit(0);
                    }
                }
            });
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
            JLabel logo = new JLabel("PMS");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logo.setForeground(Color.WHITE);
            JLabel title = new JLabel("Enterprise Workforce & Project Governance Platform");
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

            JButton btnLogout = UITheme.createDangerButton("Logout");
            btnLogout.setFont(UITheme.FONT_BOLD);
            btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLogout.addActionListener(e -> handleLogout());

            rightHeader.add(lblUserBadge);
            rightHeader.add(Box.createHorizontalStrut(8));
            rightHeader.add(btnLogout);
            header.add(rightHeader, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            // Tabs
            tabbedPane = new JTabbedPane();
            tabbedPane.setFont(UITheme.FONT_BOLD);
            tabbedPane.addTab("Executive Dashboard", createDashboardPanel());
            tabbedPane.addTab("Project Management", createProjectsPanel());
            tabbedPane.addTab("Tasks & WBS Hierarchy", createTasksPanel());
            tabbedPane.addTab("Team Members & Roles", createTeamPanel());

            if (currentUser.hasPermission(Permission.VIEW_TIME_LOGS)) {
                tabbedPane.addTab("User Time-In & Work Logs", createTimeLogsPanel());
            }

            tabbedPane.addChangeListener(e -> refreshAllViews());
            add(tabbedPane, BorderLayout.CENTER);

            // Status Bar
            JPanel status = new JPanel(new BorderLayout());
            status.setBackground(Color.WHITE);
            status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER), new EmptyBorder(7, 20, 7, 20)));
            JLabel lblStat = new JLabel(db.getDatabaseStatusText());
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
            JLabel lblPTitle = new JLabel("Project Recursive Progress Tracking (Rollup from Sub-tasks)");
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
            JLabel lblSTitle = new JLabel("Task Workflow Distribution & System Health");
            lblSTitle.setFont(UITheme.FONT_SECTION);
            lblSTitle.setForeground(UITheme.TEXT_DARK);
            summarySection.add(lblSTitle, BorderLayout.NORTH);

            JPanel distroPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            distroPanel.setOpaque(false);

            lblTodoCount = new JLabel("To Do: 0");
            lblProgCount = new JLabel("In Progress: 0");
            lblReviewCount = new JLabel("In Review: 0");
            lblDoneCount = new JLabel("Completed: 0");

            distroPanel.add(createWorkflowItem("To Do (Pending Execution)", lblTodoCount, new Color(224, 231, 255), new Color(30, 64, 175)));
            distroPanel.add(createWorkflowItem("In Progress (Active Sprints)", lblProgCount, new Color(254, 243, 199), new Color(146, 64, 14)));
            distroPanel.add(createWorkflowItem("In Review (Quality Assurance)", lblReviewCount, new Color(243, 232, 255), new Color(107, 33, 168)));
            distroPanel.add(createWorkflowItem("Completed (Delivered)", lblDoneCount, new Color(209, 250, 229), new Color(6, 95, 70)));
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

            if (currentUser.hasPermission(Permission.CREATE_PROJECT)) {
                JButton btnNew = UITheme.createPrimaryButton("+ New Project");
                btnNew.addActionListener(e -> handleNewProject());
                actions.add(btnNew);
            }

            if (currentUser.hasPermission(Permission.EDIT_PROJECT)) {
                JButton btnEdit = UITheme.createSecondaryButton("Edit Project");
                btnEdit.addActionListener(e -> handleEditProject());
                actions.add(btnEdit);
            }

            if (currentUser.hasPermission(Permission.DELETE_PROJECT)) {
                JButton btnDel = UITheme.createDangerButton("Delete Project");
                btnDel.addActionListener(e -> handleDeleteProject());
                actions.add(btnDel);
            }

            if (actions.getComponentCount() > 0) {
                topBar.add(actions, BorderLayout.EAST);
            }

            panel.add(topBar, BorderLayout.NORTH);

            projectAdapter = new ProjectTableModelAdapter();
            projectTable = new JTable(projectAdapter);
            projectTable.setRowHeight(34);
            projectTable.setFont(UITheme.FONT_REGULAR);
            projectTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            projectTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());

            // Double click to edit (if permitted)
            projectTable.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && currentUser.hasPermission(Permission.EDIT_PROJECT)) {
                        handleEditProject();
                    }
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
                    if (row >= 0 && row < projectTable.getRowCount()) {
                        projectTable.setRowSelectionInterval(row, row);
                    }
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
                    String.format("WARNING: Are you sure you want to permanently delete Project #%d '%s'?\n" +
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

            filterArea.add(new JLabel("Team:"));
            cmbTeamFilter = new JComboBox<>();
            cmbTeamFilter.setFont(UITheme.FONT_REGULAR);
            cmbTeamFilter.addActionListener(e -> applyTaskFilter());
            filterArea.add(cmbTeamFilter);

            filterArea.add(new JLabel("Member:"));
            cmbTaskUserFilter = new JComboBox<>();
            cmbTaskUserFilter.setFont(UITheme.FONT_REGULAR);
            cmbTaskUserFilter.addActionListener(e -> applyTaskFilter());
            filterArea.add(cmbTaskUserFilter);
            topBar.add(filterArea, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            if (currentUser.hasPermission(Permission.CREATE_TASK)) {
                JButton btnNewTask = UITheme.createPrimaryButton("+ New Task / Sub-task");
                btnNewTask.addActionListener(e -> handleNewTask());
                actions.add(btnNewTask);
            }

            if (currentUser.hasPermission(Permission.EDIT_TASK)) {
                JButton btnEditTask = UITheme.createSecondaryButton("Edit Task");
                btnEditTask.addActionListener(e -> handleEditTask());
                actions.add(btnEditTask);
            }

            if (currentUser.hasPermission(Permission.UPDATE_TASK_STATUS)) {
                JButton btnStatus = UITheme.createSecondaryButton("Transition Status");
                btnStatus.addActionListener(e -> handleTransitionStatus());
                actions.add(btnStatus);
            }

            if (currentUser.hasPermission(Permission.LOG_HOURS)) {
                JButton btnHours = UITheme.createSecondaryButton("Log Hours");
                btnHours.addActionListener(e -> handleLogHours());
                actions.add(btnHours);
            }

            if (currentUser.hasPermission(Permission.DELETE_TASK)) {
                JButton btnDelTask = UITheme.createDangerButton("Delete Task");
                btnDelTask.addActionListener(e -> handleDeleteTask());
                actions.add(btnDelTask);
            }

            if (actions.getComponentCount() > 0) {
                topBar.add(actions, BorderLayout.EAST);
            }

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
            taskTable.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStatusRenderer());
            taskTable.getColumnModel().getColumn(8).setCellRenderer(UITheme.createProgressRenderer());

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
                    if (row >= 0 && row < taskTable.getRowCount()) {
                        taskTable.setRowSelectionInterval(row, row);
                    }
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
                    if (row >= 0 && row < taskTable.getRowCount()) {
                        taskTable.setRowSelectionInterval(row, row);
                    }
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
                    if (row >= 0 && row < taskTable.getRowCount()) {
                        taskTable.setRowSelectionInterval(row, row);
                    }
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
                    String.format("WARNING: Are you sure you want to delete Task #%d '%s'?\n" +
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

            JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
            titles.setOpaque(false);
            JLabel heading = new JLabel("Teams Directory & Organization Workforce");
            heading.setFont(UITheme.FONT_HEADER);
            heading.setForeground(UITheme.TEXT_DARK);
            JLabel sub = new JLabel(currentUser.hasPermission(Permission.MANAGE_USERS)
                    ? "Admin and Super Admin can manage members and teams. Super Admin manages Administrators."
                    : "Organization Workforce and Functional Teams Roster (View-only for Software Developers)");
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(UITheme.TEXT_MUTED);
            titles.add(heading);
            titles.add(sub);
            topBar.add(titles, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            if (currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JButton btnAddMember = UITheme.createPrimaryButton("+ Add User");
                btnAddMember.addActionListener(e -> handleAddTeamMember());

                JButton btnEditMember = UITheme.createSecondaryButton("Edit User");
                btnEditMember.addActionListener(e -> handleEditUser());

                JButton btnDeleteUser = UITheme.createDangerButton("- Delete User");
                btnDeleteUser.addActionListener(e -> handleDeleteUser());

                JButton btnAddTeam = UITheme.createSuccessButton("+ Add New Team");
                btnAddTeam.addActionListener(e -> handleAddTeam());

                JButton btnDeleteTeam = UITheme.createSecondaryButton("- Delete Team");
                btnDeleteTeam.addActionListener(e -> handleDeleteTeam());

                actions.add(btnAddMember);
                actions.add(btnEditMember);
                actions.add(btnDeleteUser);
                actions.add(btnAddTeam);
                actions.add(btnDeleteTeam);
                topBar.add(actions, BorderLayout.EAST);
            }
            panel.add(topBar, BorderLayout.NORTH);

            // Split: Top Teams Table, Bottom Members Table
            JPanel centerSplit = new JPanel(new GridLayout(2, 1, 0, 14));
            centerSplit.setOpaque(false);

            // 1. Teams Section
            JPanel teamsCard = UITheme.createCard();
            teamsCard.setLayout(new BorderLayout(0, 8));
            JLabel lblTeamsHead = new JLabel("Registered Functional Teams (Departments / Squads)");
            lblTeamsHead.setFont(UITheme.FONT_SECTION);
            lblTeamsHead.setForeground(UITheme.TEXT_DARK);
            teamsCard.add(lblTeamsHead, BorderLayout.NORTH);

            Vector<String> tmCols = new Vector<>(Arrays.asList("Team ID", "Team Name", "Domain / Mission Description", "Active Tasks Assigned"));
            teamsTableModel = new DefaultTableModel(tmCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            teamsTable = new JTable(teamsTableModel);
            teamsTable.setRowHeight(32);
            teamsTable.setFont(UITheme.FONT_REGULAR);
            teamsTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            teamsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            teamsCard.add(new JScrollPane(teamsTable), BorderLayout.CENTER);
            centerSplit.add(teamsCard);

            // 2. Members Section
            JPanel membersCard = UITheme.createCard();
            membersCard.setLayout(new BorderLayout(0, 8));
            JLabel lblMembersHead = new JLabel(currentUser.hasPermission(Permission.MANAGE_USERS)
                    ? "Personnel & Role Roster (Select a user to Edit, Delete, or View)"
                    : "Personnel & Role Roster (Directory View)");
            lblMembersHead.setFont(UITheme.FONT_SECTION);
            lblMembersHead.setForeground(UITheme.TEXT_DARK);
            membersCard.add(lblMembersHead, BorderLayout.NORTH);

            Vector<String> memCols = new Vector<>(Arrays.asList("User ID", "Name", "Username", "Role", "Department"));
            teamModel = new DefaultTableModel(memCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            teamTable = new JTable(teamModel);
            teamTable.setRowHeight(32);
            teamTable.setFont(UITheme.FONT_REGULAR);
            teamTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            teamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Double click to edit user
            teamTable.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && currentUser.hasPermission(Permission.MANAGE_USERS)) {
                        handleEditUser();
                    }
                }
            });

            membersCard.add(new JScrollPane(teamTable), BorderLayout.CENTER);
            centerSplit.add(membersCard);

            panel.add(centerSplit, BorderLayout.CENTER);
            return panel;
        }

        private void handleAddTeamMember() {
            if (!currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JOptionPane.showMessageDialog(this, "Access Denied: Only Administrators have permission to add new users.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AddTeamMemberDialog dialog = new AddTeamMemberDialog(this, db, currentUser);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                User u = dialog.getCreatedUser();
                db.saveUser(u);
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Account '" + u.getFullName() + "' (" + u.getRole().getLabel() + ") created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void handleEditUser() {
            if (!currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JOptionPane.showMessageDialog(this, "Access Denied: You do not have permission to edit users.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = teamTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int userId = (Integer) teamModel.getValueAt(row, 0);
            User targetUser = db.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
            if (targetUser == null) {
                JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (targetUser.getRole() == UserRole.SUPER_ADMIN && currentUser.getRole() != UserRole.SUPER_ADMIN) {
                JOptionPane.showMessageDialog(this, "Access Denied: Only Super Administrators can edit Super Administrator accounts!", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (targetUser.getRole() == UserRole.MANAGER && !currentUser.hasPermission(Permission.MANAGE_ADMINS) && !targetUser.getId().equals(currentUser.getId())) {
                JOptionPane.showMessageDialog(this,
                        "Access Denied: Only Super Administrators have permission to edit other Administrator accounts!\n" +
                        "Your Current Role: " + currentUser.getRole().getLabel(),
                        "Super Admin Privilege Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AddTeamMemberDialog dialog = new AddTeamMemberDialog(this, db, currentUser, targetUser);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                User updated = dialog.getCreatedUser();
                db.saveUser(updated);
                if (updated.getId().equals(currentUser.getId())) {
                    currentUser.setFullName(updated.getFullName());
                    currentUser.setEmail(updated.getEmail());
                    lblUserBadge.setText(currentUser.getProfileBadgeText());
                }
                refreshAllViews();
                if (row >= 0 && row < teamTable.getRowCount()) {
                    teamTable.setRowSelectionInterval(row, row);
                }
                JOptionPane.showMessageDialog(this, "Account '" + updated.getFullName() + "' (" + updated.getRole().getLabel() + ") updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void handleDeleteUser() {
            if (!currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JOptionPane.showMessageDialog(this, "Access Denied: You do not have permission to delete users.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = teamTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int userId = (Integer) teamModel.getValueAt(row, 0);
            User targetUser = db.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
            if (targetUser == null) {
                JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (targetUser.getId().equals(currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "You cannot delete your own active account!", "Operation Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (targetUser.getRole() == UserRole.SUPER_ADMIN) {
                JOptionPane.showMessageDialog(this, "Access Denied: Super Administrator accounts are root accounts and cannot be deleted!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (targetUser.getRole() == UserRole.MANAGER) {
                if (!currentUser.hasPermission(Permission.MANAGE_ADMINS)) {
                    JOptionPane.showMessageDialog(this,
                            "Access Denied: Only Super Administrators have permission to delete Admin (Project Manager) accounts!\n" +
                            "Your Current Role: " + currentUser.getRole().getLabel(),
                            "Super Admin Privilege Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("WARNING: Are you sure you want to permanently delete user:\n\n" +
                                  "  ID: #%d\n" +
                                  "  Name: %s\n" +
                                  "  Username: %s\n" +
                                  "  Role: %s\n\n" +
                                  "This user will be permanently deleted from the SQLite database.",
                            targetUser.getId(), targetUser.getFullName(), targetUser.getUsername(), targetUser.getRole().getLabel()),
                    "Confirm User Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                db.deleteUser(targetUser.getId());
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "User '" + targetUser.getFullName() + "' was successfully deleted from the database.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void handleAddTeam() {
            if (!currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JOptionPane.showMessageDialog(this, "Access Denied: Only Administrators can create teams.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TeamFormDialog dlg = new TeamFormDialog(this, db);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                Team tm = new Team(dlg.getTeamName(), dlg.getTeamDescription());
                db.saveTeam(tm);
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Team '" + tm.getName() + "' created successfully!", "Team Added", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void handleDeleteTeam() {
            if (!currentUser.hasPermission(Permission.MANAGE_USERS)) {
                JOptionPane.showMessageDialog(this, "Access Denied: Only Administrators can delete teams.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = teamsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a team from the Teams table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int tid = (Integer) teamsTableModel.getValueAt(row, 0);
            Team tm = db.getTeams().stream().filter(t -> t.getId().equals(tid)).findFirst().orElse(null);
            if (tm == null) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete Team '" + tm.getName() + "'?\nAny associated tasks will be reassigned to 'General Team'.",
                    "Confirm Team Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                db.deleteTeam(tid);
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Team '" + tm.getName() + "' was removed.", "Team Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private JPanel createTimeLogsPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setBackground(UITheme.BG_LIGHT);
            panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

            // Top Header & Controls
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            headerTextPanel.setOpaque(false);
            JLabel heading = new JLabel("User Time-In Sessions & Task Hours Audit");
            heading.setFont(UITheme.FONT_HEADER);
            heading.setForeground(UITheme.TEXT_DARK);
            JLabel sub = new JLabel("Administrative visibility into user attendance, active login sessions, and work hours logged.");
            sub.setFont(UITheme.FONT_SMALL);
            sub.setForeground(UITheme.TEXT_MUTED);
            headerTextPanel.add(heading);
            headerTextPanel.add(sub);
            topBar.add(headerTextPanel, BorderLayout.WEST);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            controls.setOpaque(false);

            JLabel lblFilter = new JLabel("Filter by User:");
            lblFilter.setFont(UITheme.FONT_BOLD);
            cmbTimeLogUserFilter = new JComboBox<>();
            cmbTimeLogUserFilter.addItem("All Users");
            for (User u : db.getUsers()) {
                cmbTimeLogUserFilter.addItem(u.getFullName() + " (" + u.getUsername() + ")");
            }
            cmbTimeLogUserFilter.addActionListener(e -> refreshTimeLogs());

            JButton btnRefreshLogs = UITheme.createPrimaryButton("Refresh Logs");
            btnRefreshLogs.addActionListener(e -> refreshTimeLogs());

            JButton btnAuditSummary = UITheme.createSecondaryButton("Audit Summary");
            btnAuditSummary.addActionListener(e -> showAuditSummaryDialog());

            controls.add(lblFilter);
            controls.add(cmbTimeLogUserFilter);
            controls.add(btnRefreshLogs);
            controls.add(btnAuditSummary);
            topBar.add(controls, BorderLayout.EAST);

            panel.add(topBar, BorderLayout.NORTH);

            // Center Content: Top Metrics + 2 Tables (Sessions & Work Logs)
            JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
            centerPanel.setOpaque(false);

            // 3 KPI Cards
            JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
            metricsGrid.setOpaque(false);
            lblTotalSessionsMetric = new JLabel("0", SwingConstants.CENTER);
            lblActiveSessionsMetric = new JLabel("0", SwingConstants.CENTER);
            lblTotalHoursMetric = new JLabel("0.0h", SwingConstants.CENTER);

            metricsGrid.add(createStatCard("TOTAL SESSIONS RECORDED", lblTotalSessionsMetric, UITheme.PRIMARY, "Login audit trails"));
            metricsGrid.add(createStatCard("ACTIVE SESSIONS NOW", lblActiveSessionsMetric, UITheme.SUCCESS, "Currently logged in"));
            metricsGrid.add(createStatCard("TOTAL WORK HOURS LOGGED", lblTotalHoursMetric, UITheme.PRIMARY_ACCENT, "Sprint task execution"));
            centerPanel.add(metricsGrid, BorderLayout.NORTH);

            // Split Tables: Sessions Table (Top) and Work Logs Table (Bottom)
            JPanel tablesContainer = new JPanel(new GridLayout(2, 1, 0, 12));
            tablesContainer.setOpaque(false);

            // Section 1: User Login & Time-In Sessions
            JPanel sessionSection = UITheme.createCard();
            sessionSection.setLayout(new BorderLayout(0, 8));
            JLabel lblSessionTitle = new JLabel("User Login & Time-In Sessions (Attendance & Presence)");
            lblSessionTitle.setFont(UITheme.FONT_SECTION);
            lblSessionTitle.setForeground(UITheme.TEXT_DARK);
            sessionSection.add(lblSessionTitle, BorderLayout.NORTH);

            Vector<String> sessionCols = new Vector<>(Arrays.asList(
                    "Session ID", "Username", "Full Name", "Role", "Time-In (Login)", "Time-Out (Logout)", "Duration", "Session Status"
            ));
            sessionTableModel = new DefaultTableModel(sessionCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            sessionTable = new JTable(sessionTableModel);
            sessionTable.setRowHeight(30);
            sessionTable.setFont(UITheme.FONT_REGULAR);
            sessionTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            sessionSection.add(new JScrollPane(sessionTable), BorderLayout.CENTER);
            tablesContainer.add(sessionSection);

            // Section 2: Task Work Logs
            JPanel workLogSection = UITheme.createCard();
            workLogSection.setLayout(new BorderLayout(0, 8));
            JLabel lblWorkTitle = new JLabel("User Task Work Hours Logged (Task Activity Audit)");
            lblWorkTitle.setFont(UITheme.FONT_SECTION);
            lblWorkTitle.setForeground(UITheme.TEXT_DARK);
            workLogSection.add(lblWorkTitle, BorderLayout.NORTH);

            Vector<String> workCols = new Vector<>(Arrays.asList(
                    "Log ID", "Team Member", "Task Title", "Project", "Hours Logged", "Timestamp", "Activity Notes"
            ));
            workLogTableModel = new DefaultTableModel(workCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            workLogTable = new JTable(workLogTableModel);
            workLogTable.setRowHeight(30);
            workLogTable.setFont(UITheme.FONT_REGULAR);
            workLogTable.getTableHeader().setFont(UITheme.FONT_BOLD);
            workLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            workLogSection.add(new JScrollPane(workLogTable), BorderLayout.CENTER);
            tablesContainer.add(workLogSection);

            centerPanel.add(tablesContainer, BorderLayout.CENTER);
            panel.add(centerPanel, BorderLayout.CENTER);

            return panel;
        }

        private void refreshTimeLogs() {
            if (sessionTableModel == null || workLogTableModel == null) return;

            List<TimeInSession> allSessions = db.getSessionLogs();
            List<TaskWorkLog> allWorkLogs = db.getWorkLogs();

            // Filter by user if selected
            String selTimeUser = (cmbTimeLogUserFilter != null) ? (String) cmbTimeLogUserFilter.getSelectedItem() : null;
            String filterUsername = (selTimeUser != null && !selTimeUser.equals("All Users") && selTimeUser.contains("(") && selTimeUser.endsWith(")"))
                    ? selTimeUser.substring(selTimeUser.lastIndexOf('(') + 1, selTimeUser.length() - 1)
                    : null;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Update Metrics
            long totalSessions = allSessions.size();
            long activeSessions = allSessions.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
            double totalHours = allWorkLogs.stream().mapToDouble(TaskWorkLog::getHoursLogged).sum();

            lblTotalSessionsMetric.setText(String.valueOf(totalSessions));
            lblActiveSessionsMetric.setText(String.valueOf(activeSessions));
            lblTotalHoursMetric.setText(String.format("%.1fh", totalHours));

            // Populate Sessions Table
            Vector<Vector<Object>> sData = new Vector<>();
            for (TimeInSession s : allSessions) {
                if (filterUsername != null && !s.getUsername().equalsIgnoreCase(filterUsername)) continue;
                Vector<Object> row = new Vector<>();
                row.add(s.getSessionId());
                row.add(s.getUsername());
                row.add(s.getFullName());
                row.add(s.getRole().getLabel());
                row.add(s.getTimeIn().format(fmt));
                row.add(s.getTimeOut() != null ? s.getTimeOut().format(fmt) : "[Active Now]");
                row.add(s.getFormattedDuration());
                row.add(s.getStatus());
                sData.add(row);
            }
            sessionTableModel.setDataVector(sData, new Vector<>(Arrays.asList(
                    "Session ID", "Username", "Full Name", "Role", "Time-In (Login)", "Time-Out (Logout)", "Duration", "Session Status"
            )));
            sessionTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(UITheme.FONT_BADGE);
                    lbl.setOpaque(true);
                    if ("ACTIVE".equals(val)) {
                        lbl.setBackground(new Color(209, 250, 229));
                        lbl.setForeground(new Color(6, 95, 70));
                    } else {
                        lbl.setBackground(new Color(241, 245, 249));
                        lbl.setForeground(new Color(71, 85, 105));
                    }
                    return lbl;
                }
            });

            // Populate Work Logs Table
            Vector<Vector<Object>> wData = new Vector<>();
            for (TaskWorkLog w : allWorkLogs) {
                if (filterUsername != null) {
                    Optional<User> uOpt = db.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(filterUsername)).findFirst();
                    if (uOpt.isPresent()) {
                        int fUid = uOpt.get().getId();
                        if (w.getUserId() != fUid && !w.getUserName().equalsIgnoreCase(uOpt.get().getFullName()) && !w.getUserName().equalsIgnoreCase(filterUsername)) {
                            continue;
                        }
                    } else if (!w.getUserName().equalsIgnoreCase(filterUsername)) {
                        continue;
                    }
                }
                Vector<Object> row = new Vector<>();
                row.add(w.getLogId());
                row.add(w.getUserName());
                row.add(w.getTaskTitle());
                row.add(w.getProjectName());
                row.add(String.format("%.1f hrs", w.getHoursLogged()));
                row.add(w.getTimestamp().format(fmt));
                row.add(w.getNotes());
                wData.add(row);
            }
            workLogTableModel.setDataVector(wData, new Vector<>(Arrays.asList(
                    "Log ID", "Team Member", "Task Title", "Project", "Hours Logged", "Timestamp", "Activity Notes"
            )));
        }

        private void showAuditSummaryDialog() {
            List<TimeInSession> sessions = db.getSessionLogs();
            List<TaskWorkLog> workLogs = db.getWorkLogs();
            long activeCount = sessions.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
            double totalHours = workLogs.stream().mapToDouble(TaskWorkLog::getHoursLogged).sum();

            StringBuilder sb = new StringBuilder();
            sb.append("=====================================================\n");
            sb.append("   EXECUTIVE AUDIT & TIME SUMMARY REPORT   \n");
            sb.append("=====================================================\n\n");
            sb.append(String.format("Generated By: %s [%s]\n", currentUser.getFullName(), currentUser.getRole().getLabel()));
            sb.append(String.format("Generated At: %s\n\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append(String.format("Total Login Sessions Recorded: %d\n", sessions.size()));
            sb.append(String.format("Currently Active User Sessions: %d\n", activeCount));
            sb.append(String.format("Total Task Work Hours Logged:  %.2f hours\n\n", totalHours));
            sb.append("-----------------------------------------------------\n");
            sb.append("User Work Breakdown:\n");
            Map<String, Double> hoursByUser = new HashMap<>();
            for (TaskWorkLog w : workLogs) {
                hoursByUser.put(w.getUserName(), hoursByUser.getOrDefault(w.getUserName(), 0.0) + w.getHoursLogged());
            }
            for (Map.Entry<String, Double> entry : hoursByUser.entrySet()) {
                sb.append(String.format(" | %-28s : %.1f hrs\n", entry.getKey(), entry.getValue()));
            }
            sb.append("=====================================================\n");

            JTextArea txt = new JTextArea(sb.toString(), 16, 45);
            txt.setFont(new Font("Consolas", Font.PLAIN, 12));
            txt.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(txt), "Audit Summary Report", JOptionPane.INFORMATION_MESSAGE);
        }

        private void handleLogout() {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    String.format("Are you sure you want to log out, %s?\n\nTo switch accounts or continue working, you must log in again.", currentUser.getFullName()),
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                AppLogger.info("User '%s' logged out.", currentUser.getUsername());
                db.recordLogoutSession(currentUser);
                dispose();
                SwingUtilities.invokeLater(() -> ProjectManagementSystem.showLoginAndLaunch(db));
            }
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

                JLabel l = new JLabel(String.format("<html><b>#%d: %s</b> <font color='#64748B'>[%s | $%,.0f]</font></html>",
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

            // 4. Update Filter Combos
            String prevSelected = (String) cmbProjectFilter.getSelectedItem();
            cmbProjectFilter.removeAllItems();
            cmbProjectFilter.addItem("All Projects (" + projs.size() + ")");
            for (Project p : projs) cmbProjectFilter.addItem("#" + p.getId() + " | " + p.getTitle());
            if (prevSelected != null) cmbProjectFilter.setSelectedItem(prevSelected);

            List<Team> teams = db.getTeams();
            if (cmbTeamFilter != null) {
                String prevTeam = (String) cmbTeamFilter.getSelectedItem();
                cmbTeamFilter.removeAllItems();
                cmbTeamFilter.addItem("All Teams (" + teams.size() + ")");
                for (Team tm : teams) cmbTeamFilter.addItem(tm.getName());
                if (prevTeam != null) cmbTeamFilter.setSelectedItem(prevTeam);
            }

            if (cmbTaskUserFilter != null) {
                String prevUser = (String) cmbTaskUserFilter.getSelectedItem();
                cmbTaskUserFilter.removeAllItems();
                cmbTaskUserFilter.addItem("All Members (" + users.size() + ")");
                for (User u : users) cmbTaskUserFilter.addItem(u.getFullName() + " (" + u.getUsername() + ")");
                if (prevUser != null) cmbTaskUserFilter.setSelectedItem(prevUser);
            }

            if (cmbTimeLogUserFilter != null) {
                String curTimeUser = (String) cmbTimeLogUserFilter.getSelectedItem();
                cmbTimeLogUserFilter.removeAllItems();
                cmbTimeLogUserFilter.addItem("All Users");
                for (User u : users) {
                    cmbTimeLogUserFilter.addItem(u.getFullName() + " (" + u.getUsername() + ")");
                }
                if (curTimeUser != null) cmbTimeLogUserFilter.setSelectedItem(curTimeUser);
            }

            applyTaskFilter();

            // 5. Build Perfect WBS Tree
            treeRoot.removeAllChildren();
            for (Project p : projs) {
                DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(String.format("Project #%d: %s [%s]", p.getId(), p.getTitle(), p.getStatus()));
                List<Task> roots = projectService.buildTaskHierarchy(p.getId());
                for (Task rootTask : roots) {
                    pNode.add(buildTreeNodesRecursively(rootTask));
                }
                treeRoot.add(pNode);
            }
            treeModel.reload();
            for (int i = 0; i < hierarchyTree.getRowCount(); i++) hierarchyTree.expandRow(i);

            // 6. Registered Teams Table & Personnel Roster
            Vector<Vector<Object>> tmData = new Vector<>();
            for (Team tm : teams) {
                Vector<Object> r = new Vector<>();
                r.add(tm.getId());
                r.add(tm.getName());
                r.add(tm.getDescription());
                long activeTasks = tasks.stream().filter(t -> tm.getName().equalsIgnoreCase(t.getAssignedTeam())).count();
                r.add(activeTasks + " Task(s)");
                tmData.add(r);
            }
            teamsTableModel.setDataVector(tmData, new Vector<>(Arrays.asList("Team ID", "Team Name", "Domain / Mission Description", "Active Tasks Assigned")));

            Vector<Vector<Object>> data = new Vector<>();
            for (User u : users) {
                Vector<Object> row = new Vector<>();
                row.add(u.getId());
                row.add(u.getFullName());
                row.add(u.getUsername());
                row.add(u.getRole().getLabel());
                row.add(u.getDepartment());
                data.add(row);
            }
            teamModel.setDataVector(data, new Vector<>(Arrays.asList("User ID", "Name", "Username", "Role", "Department")));

            // 7. Time Logs (Admin / Super Admin Only)
            if (currentUser.hasPermission(Permission.VIEW_TIME_LOGS)) {
                refreshTimeLogs();
            }
        }

        private void applyTaskFilter() {
            List<Task> all = taskService.getAllTasks();
            int idx = cmbProjectFilter != null ? cmbProjectFilter.getSelectedIndex() : 0;
            if (idx > 0) {
                List<Project> projs = projectService.getAllProjects();
                if (idx - 1 < projs.size()) {
                    int pid = projs.get(idx - 1).getId();
                    all.removeIf(t -> t.getProjectId() != pid);
                }
            }
            if (cmbTeamFilter != null && cmbTeamFilter.getSelectedIndex() > 0) {
                String teamSel = (String) cmbTeamFilter.getSelectedItem();
                all.removeIf(t -> !teamSel.equalsIgnoreCase(t.getAssignedTeam()));
            }
            if (cmbTaskUserFilter != null && cmbTaskUserFilter.getSelectedIndex() > 0) {
                String uSel = (String) cmbTaskUserFilter.getSelectedItem();
                if (uSel != null && uSel.contains("(") && uSel.endsWith(")")) {
                    String uName = uSel.substring(uSel.lastIndexOf('(') + 1, uSel.length() - 1);
                    User uObj = db.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(uName)).findFirst().orElse(null);
                    if (uObj != null && uObj.getId() != null) {
                        all.removeIf(t -> t.getAssignedUserId() == null || !t.getAssignedUserId().equals(uObj.getId()));
                    }
                }
            }
            taskAdapter.setTasks(all);
        }

        private DefaultMutableTreeNode buildTreeNodesRecursively(Task t) {
            String label = String.format("#%d: %s [%s] (%.0f%% | Depth: L%d)",
                    t.getId(), t.getTitle(), t.getStatus().getLabel(), t.calculateCompletionPercentage(), t.calculateDependencyDepth());
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TaskNodeItem(t, label));
            for (Task child : t.getSubTasks()) {
                node.add(buildTreeNodesRecursively(child)); // Recursive descent
            }
            return node;
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

        private JComboBox<String> cmbRoleSelect;
        private JTextField txtUser;
        private JPasswordField txtPass;

        public LoginDialog(Frame parent, DatabaseManager db) {
            super(parent, "Enterprise Project Management - Login Portal", true);
            this.db = db;
            initComponents();
            pack();
            setSize(500, 360);
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
            JLabel s = new JLabel("Enterprise Project Management Portal");
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

            // 1. Account Type Dropdown
            g.gridx = 0; g.gridy = 0;
            JLabel lblRole = new JLabel("Account Type: *");
            lblRole.setFont(UITheme.FONT_BOLD);
            form.add(lblRole, g);

            g.gridx = 1;
            cmbRoleSelect = new JComboBox<>(new String[]{
                    "[Select Account Type]",
                    "User (Software Developer)",
                    "Admin (Project Manager)",
                    "Super Admin (System Administrator)"
            });
            cmbRoleSelect.setFont(UITheme.FONT_REGULAR);
            form.add(cmbRoleSelect, g);

            // 2. Clean Instruction (NO USER MODE or credential disclosure)
            g.gridx = 0; g.gridy = 1; g.gridwidth = 2;
            JLabel lblInstr = new JLabel("Please select your account type and enter your credentials below.");
            lblInstr.setFont(UITheme.FONT_SMALL);
            lblInstr.setForeground(UITheme.TEXT_MUTED);
            form.add(lblInstr, g);
            g.gridwidth = 1;

            // 3. Username Field (Starts empty)
            g.gridx = 0; g.gridy = 2;
            JLabel lblU = new JLabel("Username: *");
            lblU.setFont(UITheme.FONT_BOLD);
            form.add(lblU, g);

            g.gridx = 1;
            txtUser = new JTextField(18);
            txtUser.setFont(UITheme.FONT_REGULAR);
            form.add(txtUser, g);

            // 4. Password Field (Starts empty)
            g.gridx = 0; g.gridy = 3;
            JLabel lblP = new JLabel("Password: *");
            lblP.setFont(UITheme.FONT_BOLD);
            form.add(lblP, g);

            g.gridx = 1;
            txtPass = new JPasswordField(18);
            txtPass.setFont(UITheme.FONT_REGULAR);
            form.add(txtPass, g);

            cmbRoleSelect.addActionListener(e -> {
                txtUser.setText("");
                txtPass.setText("");
                txtUser.requestFocusInWindow();
            });

            txtPass.addActionListener(e -> attemptLogin());

            add(form, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            actions.setBackground(UITheme.BG_LIGHT);

            JButton btnExit = UITheme.createSecondaryButton("Exit");
            btnExit.addActionListener(e -> {
                authenticatedUser = null;
                dispose();
            });

            JButton btnLogin = UITheme.createPrimaryButton("Sign In");
            btnLogin.addActionListener(e -> attemptLogin());

            actions.add(btnExit);
            actions.add(btnLogin);
            add(actions, BorderLayout.SOUTH);
        }

        private void attemptLogin() {
            int selectedIdx = cmbRoleSelect.getSelectedIndex();
            if (selectedIdx <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select your Account Type before signing in.",
                        "Account Type Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both username and password.",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Optional<User> auth = db.authenticate(u, p);
            if (!auth.isPresent()) {
                JOptionPane.showMessageDialog(this,
                        "Authentication Failed: Invalid username or password.",
                        "Authentication Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = auth.get();
            boolean roleMatches = false;
            String expectedType = "";

            if (selectedIdx == 1) { // User (Developer)
                expectedType = "User (Software Developer)";
                roleMatches = (user.getRole() == UserRole.DEVELOPER);
            } else if (selectedIdx == 2) { // Admin (Manager)
                expectedType = "Admin (Project Manager)";
                roleMatches = (user.getRole() == UserRole.MANAGER);
            } else if (selectedIdx == 3) { // Super Admin
                expectedType = "Super Admin (System Administrator)";
                roleMatches = (user.getRole() == UserRole.SUPER_ADMIN);
            }

            if (!roleMatches) {
                JOptionPane.showMessageDialog(this,
                        String.format("Authentication Denied:\nAccount '%s' does not match the selected account type [%s].\nPlease select the correct account type matching your role.",
                                u, expectedType),
                        "Account Type Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }

            this.authenticatedUser = user;
            dispose();
        }

        public User getAuthenticatedUser() { return authenticatedUser; }
    }

    // =====================================================================================
    // APPLICATION ENTRY POINT: MAIN METHOD & SESSION LAUNCHER
    // =====================================================================================

    public static void showLoginAndLaunch(DatabaseManager db) {
        LoginDialog login = new LoginDialog(null, db);
        login.setVisible(true);

        User user = login.getAuthenticatedUser();
        if (user != null) {
            AppLogger.info("Authenticated successfully as: %s [%s]", user.getFullName(), user.getRole().getLabel());
            db.recordLoginSession(user);
            MainFrame frame = new MainFrame(db, user);
            frame.setVisible(true);
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  Enterprise Project Management System (Production Standard)       ");
        System.out.println("=======================================================================");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        DatabaseManager db = DatabaseManager.getInstance();

        SwingUtilities.invokeLater(() -> showLoginAndLaunch(db));
    }
}
