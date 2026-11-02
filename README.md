# Enterprise Project Management System (PMS)

A desktop enterprise application engineered with Java Swing and SQLite JDBC, featuring role-based access control (RBAC), recursive Work Breakdown Structure (WBS), automated team workflows, time-in session auditing, and live database persistence.

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VIEW / PRESENTATION LAYER                       │
│  MainFrame (JFrame)                                                    │
│  ├── Executive Dashboard (Real-time analytics, recursive progress)     │
│  ├── Project Management (CRUD, budget tracking, client filters)        │
│  ├── Tasks & WBS Hierarchy (JSplitPane: JTree WBS + JTable)            │
│  ├── Team Members & Roles (Functional squads, user management)         │
│  └── User Time-In & Work Logs (Session auditing & time tracking)       │
│  Dialogs: LoginDialog, ProjectFormDialog, TaskFormDialog, TeamDialog   │
│  Theme: Modern Flat UITheme (vibrant high-contrast button styling)     │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Delegation Event Model / Adapters
┌────────────────────────────────────▼───────────────────────────────────┐
│                    ADAPTER & EVENT DRIVEN LAYER                        │
│  ├── ProjectTableModelAdapter (Adapter Pattern: Entity -> JTable)      │
│  └── TaskTableModelAdapter (Adapter Pattern: Hierarchical WBS)         │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Service Contracts
┌────────────────────────────────────▼───────────────────────────────────┐
│                          SERVICE / BUSINESS LAYER                      │
│  ├── ProjectService (Project workflows, recursive completion rollup)   │
│  └── TaskService (State machine enforcement, recursive hierarchies)    │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Interface Contracts
┌────────────────────────────────────▼───────────────────────────────────┐
│                       DATA ACCESS LAYER (DAO / JDBC)                   │
│  ├── DatabaseManager (Singleton Pattern, SQLite JDBC Dynamic Loader)   │
│  └── Live SQLite tables: users, teams, projects, tasks, sessions, logs │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                          DOMAIN / MODEL LAYER                          │
│  BaseEntity (Abstract, Auditable, Identifiable, Protected members)     │
│  ├── User (SuperAdmin, Manager, Developer with polymorphic dispatch)   │
│  ├── Team (Squad domain name, mission description)                     │
│  ├── Project (Budget, client, status, tags)                            │
│  └── Task (WBS hierarchy, recursive completion percentage)             │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Key Features

1. **Role-Based Access Control & Dynamic UI Visibility**:
   - **Super Administrator**: Root governance, user management, and authority to create/delete administrators.
   - **Project Manager (Admin)**: Project and task lifecycle management, team creation, and developer management.
   - **Software Developer (User)**: Task execution, status transitions, and hour logging. Administrative buttons (e.g., Delete Task, Add/Delete Team, Add/Delete User, Project CRUD) are hidden for developers.
2. **Strict Validation**:
   - **Passwords**: Enforced minimum of 8 characters.
   - **Emails**: Validated against standard format regex.
3. **Interactive Work Breakdown Structure (WBS)**:
   - Synchronized `JTree` hierarchy paired with a real-time `JTable`.
   - Double-clicking tree nodes automatically highlights and focuses corresponding tasks.
4. **Time Tracking & Session Audits**:
   - Automatic time-in recorded on sign-in and duration computed upon logout.
   - Real-time work log records task hours logged per developer.
5. **Persistent SQLite Storage**:
   - Zero-configuration embedded SQLite database (`pms_database.db`).

---

## Default Login Credentials

| Account Type Selected | Username | Password | Role Description |
| :--- | :--- | :--- | :--- |
| **Super Admin (System Administrator)** | `superadmin` | `superadmin123` | Full governance & administrator management |
| **Admin (Project Manager)** | `admin` | `admin123` | Project, team, and task management |
| **User (Software Developer)** | `user` | `user1234` | Task progress execution and hour logging |
| **User (Software Developer)** | `dev_alice` | `alice123` | Execution and hour logging |
| **User (Software Developer)** | `dev_bob` | `bob12345` | Execution and hour logging |

---

## How to Run

### Option 1: Double-click Launcher
Double-click `run.bat` in this folder. It automatically compiles and opens the application.

### Option 2: Command Prompt
```cmd
cd /d "C:\Users\User\Desktop\pms-app"
run.bat
```

Or directly via Java:
```cmd
javac -encoding UTF-8 -cp ".;lib\*" -d bin ProjectManagementSystem.java
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" ProjectManagementSystem
```

---

## KTU OOP Syllabus Topics Mapping (Course Code: PBCST304)

The codebase strictly contains only the core Object-Oriented Programming topics defined in the **KTU PBCST304 OOP Syllabus**:

### Module 1: Fundamentals, Types, Flow Control & Varargs
* **Standard Java Structure & Types**: Explicit primitive type casting, wrapper types (`Integer`, `Double`).
* **Arrays, Strings & Vectors**: Primitive arrays (`String[] tags`), `StringBuilder` for session audits, `Vector<Vector<Object>>` for Swing table models.
* **Control Statements**: Multi-branch `switch` statements (in logging & badge rendering), conditional branching (`if-else`), iterative loops (`for`, `while`).
* **Command Line Arguments**: `public static void main(String[] args)` supporting runtime CLI execution.
* **Variable-Length Arguments (Varargs)**: `AppLogger.info(String format, Object... args)`, `AppLogger.debug(...)`, and `AppLogger.error(...)`.
* **Abstract Classes & Interfaces**: `BaseEntity`, `User`, `Identifiable`, `Auditable`, `ProjectOperations`, `TaskOperations`.

### Module 2: Core OOP Principles & Architecture
* **Classes & Objects**: Domain modeling for `Project`, `Task`, `User`, `Team`, `TimeInSession`, and `TaskWorkLog`.
* **Access Modifiers**: Strict encapsulation via `private`, package-level, and `protected` fields.
* **Dynamic Use of `this`**: Instance variable resolution and method chaining in `Task.Builder`.
* **Overloaded Constructors**: Default and parameterized constructor chains across all models.
* **Method Overloading**: Overloaded utility methods and constructors with varying signatures.
* **Recursion**:
  * `calculateProjectCompletion(int projectId)`: Recursively aggregates progress across nested subtask trees.
  * `buildTreeNodesRecursively(Task t)`: Recursively traverses and constructs the JTree Work Breakdown Structure (WBS).
* **Static Members & Final Variables**: Thread-safe `static DatabaseManager getInstance()`, immutable constants in `UITheme`.
* **Inner Classes & Anonymous Inner Classes**:
  * Static inner classes: `Task.Builder`, `User.Credentials`.
  * Anonymous inner classes: Custom `DefaultTableCellRenderer` widgets and Swing event listeners (`MouseAdapter`, `TreeSelectionListener`).
* **Inheritance Hierarchy**: Abstract `User` superclass inherited by `SuperAdmin`, `Manager`, and `Developer`.
* **Protected Members & `super` Keyword**: `super(...)` constructor chaining up the inheritance tree (`Developer` -> `User` -> `BaseEntity`).
* **Method Overriding & Dynamic Method Dispatch**: Polymorphic invocation of `user.hasPermission(Permission)` and `user.getRoleDescription()`.

### Module 3: Advanced Structure, Exceptions & Design Patterns
* **Architectural Separation**: Clean multi-layer design (Model, Service, DAO, Adapter, View).
* **Interface Inheritance**: `BaseEntity implements Identifiable, Auditable`.
* **Exception Handling**: Robust `try-catch-finally` blocks and nested exception handling.
* **Custom Exception Hierarchy**: `PMSException` base class extended by `ProjectNotFoundException`, `TaskNotFoundException`, `UnauthorizedAccessException`, `InvalidTaskStateException`, and `DatabaseOperationException`.
* **Design Patterns**:
  * **Singleton Pattern**: Synchronized, thread-safe `DatabaseManager.getInstance()`.
  * **Adapter Pattern**: `ProjectTableModelAdapter` and `TaskTableModelAdapter` adapt domain collections to Swing `AbstractTableModel`.
* **SOLID Principles**: Single Responsibility Principle (SRP), Open/Closed Principle (OCP), Liskov Substitution Principle (LSP), Interface Segregation Principle (ISP), and Dependency Inversion Principle (DIP).

### Module 4: Event-Driven GUI (Java Swing) & Database Connectivity (JDBC)
* **Swing Controls**: `JFrame`, `JTabbedPane`, `JPanel`, `JTable`, `JTree`, `JButton`, `JTextField`, `JPasswordField`, `JComboBox`, `JProgressBar`, `JScrollPane`, `JSplitPane`, `JOptionPane`.
* **Layout Managers**: Dynamic responsive layouts using `BorderLayout`, `GridBagLayout`, `FlowLayout`, `GridLayout`, and `BoxLayout`.
* **Delegation Event Model**: `ActionListener`, `MouseListener`, `TreeSelectionListener`, `WindowAdapter`, `KeyAdapter`.
* **JDBC Database Connectivity**: Full CRUD operations executed using `Connection`, `PreparedStatement`, `ResultSet`, and transactional SQL statements with SQLite.

