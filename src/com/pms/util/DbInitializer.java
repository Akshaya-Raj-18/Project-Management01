package com.pms.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.pms.dao.DatabaseConnectionManager;
import com.pms.dao.ProjectDaoImpl;
import com.pms.dao.TaskDaoImpl;
import com.pms.dao.UserDaoImpl;
import com.pms.exception.DatabaseOperationException;
import com.pms.model.Developer;
import com.pms.model.Manager;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;
import com.pms.model.Task;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;
import com.pms.model.User;

/**
 * Embedded SQLite Schema Initializer and Demo Seed Data Generator.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Control statements, Loops, Primitive casting.
 * - Module 3: Exception handling, nested try-catch.
 * - Module 4: Dynamic JDBC DDL/DML execution and transaction seeding.
 */
public class DbInitializer {

    private final DatabaseConnectionManager connectionManager;

    public DbInitializer() {
        this(DatabaseConnectionManager.getInstance());
    }

    public DbInitializer(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Creates database tables if they do not already exist.
     */
    public void initializeSchema() throws DatabaseOperationException {
        final String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "department TEXT, " +
                "tech_stack TEXT, " +
                "hourly_rate REAL, " +
                "max_hours INTEGER, " +
                "is_active INTEGER DEFAULT 1);";

        final String createProjectsTable = "CREATE TABLE IF NOT EXISTS projects (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "budget REAL, " +
                "status TEXT NOT NULL, " +
                "manager_id INTEGER, " +
                "client_name TEXT, " +
                "tags TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY (manager_id) REFERENCES users(id));";

        final String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "assigned_user_id INTEGER, " +
                "parent_task_id INTEGER, " +
                "status TEXT NOT NULL, " +
                "priority TEXT NOT NULL, " +
                "estimated_hours REAL DEFAULT 0, " +
                "logged_hours REAL DEFAULT 0, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (project_id) REFERENCES projects(id), " +
                "FOREIGN KEY (assigned_user_id) REFERENCES users(id), " +
                "FOREIGN KEY (parent_task_id) REFERENCES tasks(id));";

        final String createTaskLogsTable = "CREATE TABLE IF NOT EXISTS task_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_id INTEGER NOT NULL, " +
                "hours_logged REAL NOT NULL, " +
                "log_date TEXT NOT NULL, " +
                "notes TEXT, " +
                "FOREIGN KEY (task_id) REFERENCES tasks(id));";

        try {
            Connection conn = connectionManager.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createUsersTable);
                stmt.execute(createProjectsTable);
                stmt.execute(createTasksTable);
                stmt.execute(createTaskLogsTable);
                AppLogger.info("Database schema verification and table creation completed successfully.");
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to initialize database schema", e);
        }
    }

    /**
     * Seeds initial demo users, projects, and recursive tasks if tables are empty.
     */
    public void seedInitialData() {
        try {
            UserDaoImpl userDao = new UserDaoImpl(connectionManager);
            ProjectDaoImpl projectDao = new ProjectDaoImpl(connectionManager);
            TaskDaoImpl taskDao = new TaskDaoImpl(connectionManager);

            if (userDao.count() > 0) {
                AppLogger.info("Database already populated. Skipping seed.");
                return;
            }

            AppLogger.info("Seeding initial enterprise demo data (Users, Projects, Hierarchical Tasks)...");

            // 1. Seed Users (Manager & Developers)
            Manager manager = new Manager(null, "admin", "admin123", "Dr. Arun Kumar (Manager)",
                    "arun.kumar@ktu.edu", "Cloud & AI Labs", 1000000.0);
            userDao.save(manager);

            Developer devAlice = new Developer(null, "dev_alice", "alice123", "Alice George (Developer)",
                    "alice@tech.io", "Java 21, Swing, Spring Boot", 75.0, 40);
            userDao.save(devAlice);

            Developer devBob = new Developer(null, "dev_bob", "bob123", "Bob Varghese (Developer)",
                    "bob@cloud.io", "Java, Microservices, SQLite", 65.0, 40);
            userDao.save(devBob);

            // 2. Seed Projects
            Project proj1 = new Project(null, "AI-Powered Smart Campus Portal",
                    "Next-generation portal for student attendance, lab allocation, and real-time alerts.",
                    85000.0, ProjectStatus.IN_PROGRESS, manager.getId(), "KTU Directorate",
                    new String[]{"AI", "Campus", "Java", "Cloud"});
            projectDao.save(proj1);

            Project proj2 = new Project(null, "KTU Examination ERP Automation",
                    "Centralized evaluation, hall ticket generation, and result ledger system.",
                    130000.0, ProjectStatus.PLANNING, manager.getId(), "Examination Controller",
                    new String[]{"ERP", "Security", "High-Volume"});
            projectDao.save(proj2);

            Project proj3 = new Project(null, "Intensive Care Telemetry Dashboard",
                    "High-reliability real-time patient telemetry visualization and alert gateway.",
                    98000.0, ProjectStatus.COMPLETED, manager.getId(), "Regional Hospital",
                    new String[]{"IoT", "Healthcare", "Realtime"});
            projectDao.save(proj3);

            // 3. Seed Tasks with Recursive Hierarchies (Project 1)
            // Top-Level Task 1: Completed Leaf
            Task task1 = new Task(null, proj1.getId(), "Architecture Design & Schema Specification",
                    "Define relational schemas, foreign key cascades, and system component diagrams.",
                    devAlice.getId(), null, TaskStatus.COMPLETED, TaskPriority.HIGH, 25.0, 25.0);
            taskDao.save(task1);

            // Top-Level Task 2: Parent with Sub-tasks
            Task task2 = new Task(null, proj1.getId(), "Authentication and RBAC Security Module",
                    "Core security module supporting dynamic method dispatch and role permissions.",
                    devAlice.getId(), null, TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL, 40.0, 18.0);
            taskDao.save(task2);

            // Sub-Task 2.1 under Task 2
            Task subTask21 = new Task(null, proj1.getId(), "Password Hashing & PBKDF2 Salt Provider",
                    "Cryptographic hashing utility with salt generation.",
                    devAlice.getId(), task2.getId(), TaskStatus.COMPLETED, TaskPriority.HIGH, 12.0, 12.0);
            taskDao.save(subTask21);

            // Sub-Task 2.2 under Task 2 (Has deeper sub-task -> Depth 2)
            Task subTask22 = new Task(null, proj1.getId(), "Session & RBAC Token Interceptor",
                    "Interceptor verifying user permissions per request.",
                    devBob.getId(), task2.getId(), TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 18.0, 6.0);
            taskDao.save(subTask22);

            // Sub-Sub-Task 2.2.1 under Sub-Task 2.2 (Depth 3)
            Task subSubTask221 = new Task(null, proj1.getId(), "Expiry & Refresh Token Rotator",
                    "Background cron/thread worker to invalidate expired tokens.",
                    devBob.getId(), subTask22.getId(), TaskStatus.TODO, TaskPriority.MEDIUM, 10.0, 0.0);
            taskDao.save(subSubTask221);

            // Top-Level Task 3: In Progress
            Task task3 = new Task(null, proj1.getId(), "Interactive Swing GUI Dashboard",
                    "Modular MVC architecture with Custom Table Adapters, Charts, and JTree views.",
                    devBob.getId(), null, TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL, 50.0, 30.0);
            taskDao.save(task3);

            // Seed sample logs
            taskDao.logHours(task1.getId(), 5.0);
            taskDao.logHours(task2.getId(), 4.0);

            AppLogger.info("Demo database successfully initialized with KTU OOP seed entities.");
        } catch (Exception e) {
            AppLogger.error("Failed during seed data insertion", e);
        }
    }
}
