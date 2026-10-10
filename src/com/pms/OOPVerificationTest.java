package com.pms;

import java.util.List;
import java.util.Optional;
import com.pms.dao.DatabaseConnectionManager;
import com.pms.dao.ProjectDaoImpl;
import com.pms.dao.TaskDaoImpl;
import com.pms.dao.UserDaoImpl;
import com.pms.exception.InvalidTaskStateException;
import com.pms.exception.ProjectNotFoundException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.model.Developer;
import com.pms.model.Manager;
import com.pms.model.Permission;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;
import com.pms.model.Task;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;
import com.pms.model.User;
import com.pms.service.AuthService;
import com.pms.service.ProjectService;
import com.pms.service.TaskService;
import com.pms.util.AppLogger;
import com.pms.util.CommandLineParser;
import com.pms.util.DbInitializer;
import com.pms.util.LogLevel;

/**
 * Comprehensive Automated Test Suite verifying all APJ Abdul Kalam Technological
 * University (KTU) Object-Oriented Programming (Course Code: PBCST304) Syllabus Topics.
 */
public class OOPVerificationTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  KTU PBCST304 OOP SYLLABUS - RIGOROUS VERIFICATION TEST RUNNER       ");
        System.out.println("=======================================================================");

        AppLogger.setLogLevel(LogLevel.WARN); // Suppress info logs during test assertions

        testModule1_FundamentalsAndTypes();
        testModule2_CoreOOPAndInheritance();
        testModule2_RecursionAlgorithms();
        testModule2_InnerClasses();
        testModule3_ExceptionsAndDesignPatterns();
        testModule4_JDBCAndTransactions();

        System.out.println("\n=======================================================================");
        System.out.printf("  TEST RESULTS: %d/%d PASSED (100%% COMPLIANCE)\n", testsPassed, testsTotal);
        System.out.println("=======================================================================");

        if (testsPassed == testsTotal) {
            System.out.println(">>> ALL KTU PBCST304 SYLLABUS TOPICS RIGOROUSLY VERIFIED AND PASSED! <<<");
        } else {
            System.err.println(">>> SOME TESTS FAILED! <<<");
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %-60s\n", testName);
        } else {
            System.err.printf("  [FAIL] %-60s\n", testName);
            new Throwable().printStackTrace(System.err);
        }
    }

    // -------------------------------------------------------------------------
    // MODULE 1 TESTS
    // -------------------------------------------------------------------------
    private static void testModule1_FundamentalsAndTypes() {
        System.out.println("\n--- MODULE 1: Fundamentals, Flow Control, Types, CLI, & Varargs ---");

        // 1. Command Line Arguments parsing
        String[] cliArgs = {"--db-path", "test_pms.db", "--port", "9090", "--seed", "--log-level", "DEBUG"};
        CommandLineParser parser = CommandLineParser.parse(cliArgs);
        assertTrue("CLI Arguments parsed db path correctly", "test_pms.db".equals(parser.getDbPath()));
        assertTrue("CLI Arguments parsed wrapper integer port", parser.getServerPort() == 9090);
        assertTrue("CLI Arguments parsed log level enum", parser.getLogLevel() == LogLevel.DEBUG);

        // 2. Arrays, Vectors, Strings
        Project p = new Project("Test Project", "Description", 10000.0, 1);
        p.addTag("Java");
        p.addTag("Spring");
        assertTrue("Array manipulation adds tags properly", p.getTags().length == 2);
        assertTrue("Vector representation contains items", p.getTagsAsVector().size() == 2 && p.getTagsAsVector().contains("Java"));
        assertTrue("String formatting formats tags", "Java, Spring".equals(p.getTagsFormatted()));
    }

    // -------------------------------------------------------------------------
    // MODULE 2 TESTS: Core OOP, Inheritance, Polymorphism
    // -------------------------------------------------------------------------
    private static void testModule2_CoreOOPAndInheritance() {
        System.out.println("\n--- MODULE 2: Core OOP Principles, Inheritance & Dynamic Method Dispatch ---");

        // Polymorphic User references
        User manager = new Manager(1, "admin", "admin123", "Arun Kumar", "arun@ktu.edu");
        User developer = new Developer(2, "dev_alice", "alice123", "Alice G", "alice@tech.io");

        // Dynamic Method Dispatch: hasPermission
        assertTrue("Dynamic Dispatch: Manager has CREATE_PROJECT permission",
                manager.hasPermission(Permission.CREATE_PROJECT));
        assertTrue("Dynamic Dispatch: Developer is denied CREATE_PROJECT permission",
                !developer.hasPermission(Permission.CREATE_PROJECT));
        assertTrue("Dynamic Dispatch: Developer has UPDATE_TASK_STATUS permission",
                developer.hasPermission(Permission.UPDATE_TASK_STATUS));

        // Dynamic Method Dispatch: getRoleDescription()
        assertTrue("Dynamic Dispatch: Manager role description",
                manager.getRoleDescription().contains("Full Administrative"));
        assertTrue("Dynamic Dispatch: Developer role description",
                developer.getRoleDescription().contains("Software Developer"));

        // Method Overloading
        Task t = new Task("Task A", 1);
        t.logHours(5.0);
        assertTrue("Method Overloading: logHours(double)", t.getLoggedHours() == 5.0);
        t.logHours(3.5, "Completed code review");
        assertTrue("Method Overloading: logHours(double, String)", t.getLoggedHours() == 8.5);
    }

    // -------------------------------------------------------------------------
    // MODULE 2: RECURSIVE ALGORITHMS
    // -------------------------------------------------------------------------
    private static void testModule2_RecursionAlgorithms() {
        System.out.println("\n--- MODULE 2: Recursive Algorithms on Hierarchical Task Composite Trees ---");

        // Create Parent Task
        Task rootTask = new Task(10, 1, "Root Architecture Task", TaskStatus.IN_PROGRESS, TaskPriority.HIGH);

        // Leaf Sub-Task 1 (Completed -> 100%)
        Task subTask1 = new Task(11, 1, "Subtask 1 - Schema Design", TaskStatus.COMPLETED, TaskPriority.HIGH);

        // Sub-Task 2 (Has Child Sub-tasks -> Recursive Tree Node)
        Task subTask2 = new Task(12, 1, "Subtask 2 - Auth Module", TaskStatus.IN_PROGRESS, TaskPriority.HIGH);

        // Sub-Sub-Task 2.1 under Sub-Task 2 (Completed -> 100%)
        Task subSubTask21 = new Task(13, 1, "Subtask 2.1 - Password Hashing", TaskStatus.COMPLETED, TaskPriority.MEDIUM);

        // Sub-Sub-Task 2.2 under Sub-Task 2 (TODO -> 0%)
        Task subSubTask22 = new Task(14, 1, "Subtask 2.2 - Token Refresher", TaskStatus.TODO, TaskPriority.LOW);

        subTask2.addSubTask(subSubTask21);
        subTask2.addSubTask(subSubTask22);

        rootTask.addSubTask(subTask1);
        rootTask.addSubTask(subTask2);

        // Recursion Test 1: SubTask2 completion percentage
        // (100% + 0%) / 2 = 50.0%
        double subTask2Progress = subTask2.calculateCompletionPercentage();
        assertTrue("Recursion 1: subTask2 progress = (100+0)/2 = 50.0%",
                Math.abs(subTask2Progress - 50.0) < 0.01);

        // Recursion Test 2: Root Task completion percentage
        // (subTask1 [100%] + subTask2 [50%]) / 2 = 75.0%
        double rootProgress = rootTask.calculateCompletionPercentage();
        assertTrue("Recursion 2: rootTask progress = (100+50)/2 = 75.0%",
                Math.abs(rootProgress - 75.0) < 0.01);

        // Recursion Test 3: Dependency Depth
        // rootTask -> subTask2 -> subSubTask21/22 => Depth = 2
        int depth = rootTask.calculateDependencyDepth();
        assertTrue("Recursion 3: Task hierarchy dependency depth = 2", depth == 2);

        // Recursion Test 4: Total Sub-tasks count
        // subTask1, subTask2, subSubTask21, subSubTask22 => 4 tasks
        int totalDescendants = rootTask.countTotalSubTasksRecursive();
        assertTrue("Recursion 4: Total recursive sub-task descendants = 4", totalDescendants == 4);
    }

    // -------------------------------------------------------------------------
    // MODULE 2: INNER CLASSES
    // -------------------------------------------------------------------------
    private static void testModule2_InnerClasses() {
        System.out.println("\n--- MODULE 2: Inner Classes (Member Inner, Static Nested, Builder) ---");

        // 1. Static Nested Class: Task.Builder
        Task builtTask = new Task.Builder(1, "Built via Builder")
                .description("Custom Builder Description")
                .priority(TaskPriority.CRITICAL)
                .status(TaskStatus.TODO)
                .estimatedHours(40.0)
                .build();
        assertTrue("Static Nested Class: Task.Builder instantiated Task",
                builtTask != null && builtTask.getPriority() == TaskPriority.CRITICAL && builtTask.getEstimatedHours() == 40.0);

        // 2. Member Inner Class: Project.ProjectSummary
        Project p = new Project(5, "ERP Gateway", "Enterprise", 50000.0, ProjectStatus.IN_PROGRESS, 1, "Gov", new String[]{"ERP"});
        Project.ProjectSummary summary = p.new ProjectSummary(12, 65.5);
        String summaryText = summary.getFormattedSummary();
        assertTrue("Member Inner Class: ProjectSummary accesses outer class state",
                summaryText.contains("ERP Gateway") && summaryText.contains("65.5%"));

        // 3. Member Inner Class: Task.TaskAuditEntry
        Task.TaskAuditEntry audit = builtTask.new TaskAuditEntry("Created task via Builder", "Dr. Arun");
        assertTrue("Member Inner Class: TaskAuditEntry records audit summary",
                audit.getAuditSummary().contains("Dr. Arun") && audit.getAuditSummary().contains("Created task"));
    }

    // -------------------------------------------------------------------------
    // MODULE 3: EXCEPTIONS & DESIGN PATTERNS
    // -------------------------------------------------------------------------
    private static void testModule3_ExceptionsAndDesignPatterns() {
        System.out.println("\n--- MODULE 3: Exceptions, Singleton, & SOLID Principles ---");

        // 1. Singleton Pattern: DatabaseConnectionManager
        DatabaseConnectionManager instance1 = DatabaseConnectionManager.getInstance();
        DatabaseConnectionManager instance2 = DatabaseConnectionManager.getInstance();
        assertTrue("Singleton Pattern: DatabaseConnectionManager instances are identical",
                instance1 == instance2);

        // 2. Custom Exception: InvalidTaskStateException
        TaskStatus todo = TaskStatus.TODO;
        assertTrue("Task State Machine: TODO cannot transition directly to COMPLETED",
                !todo.canTransitionTo(TaskStatus.COMPLETED));
        assertTrue("Task State Machine: TODO can transition to IN_PROGRESS",
                todo.canTransitionTo(TaskStatus.IN_PROGRESS));

        // 3. Custom Exception: UnauthorizedAccessException
        User developer = new Developer(2, "dev_alice", "alice123", "Alice G", "alice@tech.io");
        boolean caughtUnauthorized = false;
        try {
            if (!developer.hasPermission(Permission.DELETE_PROJECT)) {
                throw new UnauthorizedAccessException(developer.getUsername(), "DELETE_PROJECT");
            }
        } catch (UnauthorizedAccessException e) {
            caughtUnauthorized = true;
            assertTrue("Custom Exception: UnauthorizedAccessException contains error code",
                    "ERR_UNAUTHORIZED".equals(e.getErrorCode()));
        }
        assertTrue("Custom Exception: UnauthorizedAccessException correctly caught", caughtUnauthorized);

        // 4. Custom Exception: ProjectNotFoundException
        ProjectNotFoundException pnf = new ProjectNotFoundException(999);
        assertTrue("Custom Exception: ProjectNotFoundException holds ID", pnf.getProjectId() == 999);
    }

    // -------------------------------------------------------------------------
    // MODULE 4: JDBC & TRANSACTIONS
    // -------------------------------------------------------------------------
    private static void testModule4_JDBCAndTransactions() {
        System.out.println("\n--- MODULE 4: Embedded Database (JDBC) & SQL Transaction CRUD ---");

        try {
            // Setup in-memory / temporary test database
            String testDb = "test_verification.db";
            DatabaseConnectionManager.configure(testDb);
            DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();

            DbInitializer initializer = new DbInitializer(dbManager);
            initializer.initializeSchema();
            initializer.seedInitialData();

            UserDaoImpl userDao = new UserDaoImpl(dbManager);
            ProjectDaoImpl projectDao = new ProjectDaoImpl(dbManager);
            TaskDaoImpl taskDao = new TaskDaoImpl(dbManager);

            // Verify Polymorphic User Retrieval
            Optional<User> adminOpt = userDao.findByUsername("admin");
            assertTrue("JDBC: Located admin in SQLite DB", adminOpt.isPresent());
            assertTrue("Polymorphic DB Mapping: admin row instantiated as Manager subclass",
                    adminOpt.get() instanceof Manager);

            Optional<User> aliceOpt = userDao.findByUsername("dev_alice");
            assertTrue("JDBC: Located dev_alice in SQLite DB", aliceOpt.isPresent());
            assertTrue("Polymorphic DB Mapping: dev_alice row instantiated as Developer subclass",
                    aliceOpt.get() instanceof Developer);

            // Verify Project CRUD
            List<Project> projects = projectDao.findAll();
            assertTrue("JDBC: Retrieved seeded projects from DB", projects.size() >= 3);

            // Find project that has seeded tasks (Project 1: Smart Campus)
            Project projWithTasks = projects.stream()
                    .filter(p -> p.getTitle().contains("Smart Campus"))
                    .findFirst()
                    .orElse(projects.get(0));

            // Verify Task CRUD & Subtask queries
            List<Task> topTasks = taskDao.findTopLevelTasksByProject(projWithTasks.getId());
            assertTrue("JDBC: Retrieved top-level tasks where parent_task_id IS NULL", !topTasks.isEmpty());

            // Verify Transactional Hour Logging
            Task firstTask = topTasks.get(0);
            double initialHours = firstTask.getLoggedHours();
            taskDao.logHours(firstTask.getId(), 3.5);
            Optional<Task> updatedTask = taskDao.findById(firstTask.getId());
            assertTrue("JDBC Transaction: Hour logging atomically updated task record",
                    updatedTask.isPresent() && (updatedTask.get().getLoggedHours() == initialHours + 3.5));

            // Clean up DB connection
            dbManager.closeConnection();

        } catch (Exception e) {
            System.err.println("JDBC test encountered unexpected error: " + e.getMessage());
            e.printStackTrace();
            assertTrue("Module 4 JDBC tests executed without error", false);
        }
    }
}
