# Enterprise Project Management System (PMS)
**Course Code:** PBCST304 (Object-Oriented Programming using Java)  
**Curriculum:** APJ Abdul Kalam Technological University (KTU)  
**Architecture:** Enterprise Model-View-Controller (MVC) + SOLID Principles + SQLite JDBC  

---

## 🏛️ System Architectural Overview

This Project Management System is engineered as a production-grade, modular, role-based desktop enterprise application. It demonstrates comprehensive mastery of core and advanced Java Object-Oriented Programming (OOP) concepts prescribed in the KTU PBCST304 syllabus.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VIEW / PRESENTATION LAYER                       │
│  MainFrame (JFrame)                                                    │
│  ├── DashboardPanel (Real-time analytics, progress bars)               │
│  ├── ProjectManagementPanel (JTable, Search, CRUD filters)             │
│  ├── TaskManagementPanel (JSplitPane: JTree WBS + JTable with pills)   │
│  └── TeamManagementPanel (Polymorphic user roster & role switching)    │
│  Dialogs: LoginDialog, ProjectDialog, TaskDialog, LogHoursDialog       │
│  Theme & Renderers: UITheme (Custom Status/Priority Pills, Progress)   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Delegation Event Model / Adapters
┌────────────────────────────────────▼───────────────────────────────────┐
│                    ADAPTER & EVENT DRIVEN BUS                          │
│  ├── ProjectTableModelAdapter (Adapter Pattern: List/Vector -> Table)   │
│  ├── TaskTableModelAdapter (Adapter Pattern: Recursive Progress)       │
│  └── TaskStatusChangeEvent & TaskStatusChangeListener (Custom Event)   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Dependency Inversion
┌────────────────────────────────────▼───────────────────────────────────┐
│                          SERVICE / BUSINESS LAYER                      │
│  ├── AuthService (Session management, RBAC verification)              │
│  ├── ProjectService (Project workflows, recursive completion rollup)   │
│  └── TaskService (State machine enforcement, recursive hierarchies)    │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Interface Contracts
┌────────────────────────────────────▼───────────────────────────────────┐
│                       DATA ACCESS LAYER (DAO / JDBC)                   │
│  ├── DatabaseConnectionManager (Singleton Pattern, Transactions)       │
│  ├── Repository<T, ID> extends Searchable<T> (Interface Inheritance)   │
│  ├── UserDaoImpl, ProjectDaoImpl, TaskDaoImpl (PreparedStatement, SQL)  │
│  └── DbInitializer (Embedded SQLite DDL tables & recursive seed data)  │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                          DOMAIN / MODEL LAYER                          │
│  BaseEntity (Abstract, Auditable, Identifiable, Protected members)     │
│  ├── User (Abstract, Dynamic Method Dispatch, Credentials inner class) │
│  │   ├── Manager (Inheritance, full admin permissions)                 │
│  │   └── Developer (Inheritance, RBAC restricted permissions)          │
│  ├── Project (Overloaded constructors, ProjectSummary inner class)     │
│  └── Task (Recursion: progress %, depth, descendants; Task.Builder)    │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Comprehensive KTU PBCST304 Syllabus Compliance Matrix

| Syllabus Module | Syllabus Requirement | PMS Implementation Details & Class Reference |
| :--- | :--- | :--- |
| **Module 1** | **Java Structure & Naming** | Fully qualified packages (`com.pms.*`), PascalCase classes, camelCase methods. |
| **Module 1** | **Wrapper Types & Casting** | `Integer.valueOf()`, `Double.parseDouble()`, primitive unboxing, `(double) sum / count`, `(int) Math.round()`. |
| **Module 1** | **Arrays & Strings** | Tags as `String[]`, array cloning, `StringBuilder` formatting, String immutability. |
| **Module 1** | **Vectors** | `Vector<Vector<Object>>` and `Vector<String>` in `ProjectTableModelAdapter`, `TaskTableModelAdapter`, `TeamManagementPanel`. |
| **Module 1** | **Control Statements** | Modern `switch` on Enums/Strings, `if-else` cascades, `for`, `for-each`, `while` loops. |
| **Module 1** | **Command-Line Arguments** | `CommandLineParser.parse(String[] args)` supporting `--db-path`, `--seed`, `--port`, `--theme`, `--log-level`. |
| **Module 1** | **Variable-length args (Varargs)** | `AppLogger.log(LogLevel, String, Object... args)` utility logger. |
| **Module 1** | **Abstract Classes & Interfaces** | `BaseEntity`, `User`, `Identifiable`, `Auditable`, `Searchable<T>`, `Repository<T, ID>`. |
| **Module 2** | **Classes, Objects, References** | Clean entity models with encapsulation and bidirectional object graphs. |
| **Module 2** | **Access Modifiers** | Rigorous use of `public`, `protected` (across inheritance tiers), package-private, and `private`. |
| **Module 2** | **Dynamic use of `this`** | Constructor chaining `this(...)`, method chaining in Builder, disambiguation `this.prop = prop`. |
| **Module 2** | **Overloaded Constructors** | Multiple constructors in `Project`, `Task`, `User`, `BaseEntity`, `CommandLineParser`. |
| **Module 2** | **Method Overloading** | Overloaded methods in `Task.logHours(double)` vs `logHours(double, String)`; `TaskService.searchTasks(...)` with multiple signatures. |
| **Module 2** | **Recursive Algorithms** | **3 Recursive Implementations** in `Task.java`: `calculateCompletionPercentage()` (recursive child progress), `calculateDependencyDepth()` (tree depth), `countTotalSubTasksRecursive()`. Recursive tree node generation in `TaskManagementPanel`. |
| **Module 2** | **Static Members & Final** | `static final` constants, static counters `projectInstanceCounter`, static thread-safe instance in Singleton. |
| **Module 2** | **Inner Classes** | **Member Inner:** `Project.ProjectSummary`, `Task.TaskAuditEntry`. **Static Nested:** `Task.Builder`, `User.Credentials`. **Anonymous Inner:** Event listeners, `WindowAdapter`, custom renderers. |
| **Module 2** | **Inheritance & Hierarchy** | `BaseEntity` ➔ `User` ➔ `Manager` & `Developer`. Protected variables accessible to children. |
| **Module 2** | **`super` Keyword & Call Chaining** | `super(id)`, `super.toString()`, constructor execution logging verifying call order chaining. |
| **Module 2** | **Method Overriding & Dynamic Dispatch** | `@Override` in `hasPermission()`, `getRoleDescription()`, `getProfileBadgeText()`. Runtime polymorphic execution based on concrete instance. |
| **Module 3** | **Custom Interface Inheritance** | `Repository<T, ID> extends Searchable<T>` contract inheritance. |
| **Module 3** | **Custom Exception Hierarchy** | `PMSException` ➔ `ProjectNotFoundException`, `UnauthorizedAccessException`, `InvalidTaskStateException`, `DatabaseOperationException`, `TaskNotFoundException`. |
| **Module 3** | **Exception Handling Architecture** | Multi-tier `try-catch-finally`, nested try blocks for rollback recovery in `DatabaseConnectionManager`. |
| **Module 3** | **Design Patterns** | **Singleton Pattern:** Thread-safe Double-Checked Locking in `DatabaseConnectionManager`. **Adapter Pattern:** `ProjectTableModelAdapter`, `TaskTableModelAdapter`. **Builder Pattern:** `Task.Builder`. |
| **Module 3** | **SOLID Principles** | **SRP** (Isolated services & DAOs), **OCP** (Extensible user roles), **LSP** (Polymorphic `User` substitution), **ISP** (Targeted interfaces), **DIP** (Services depend on DAO abstractions). |
| **Module 4** | **Swing Layout Managers** | `BorderLayout`, `GridBagLayout`, `FlowLayout`, `BoxLayout`, `GridLayout`, `JSplitPane`. |
| **Module 4** | **Swing Control Suite** | `JFrame`, `JDialog`, `JLabel`, `JButton`, `JTextField`, `JPasswordField`, `JComboBox`, `JTable`, `JTree`, `JProgressBar`, `JTextArea`, `JScrollPane`, `JOptionPane`, `JTabbedPane`. |
| **Module 4** | **Delegation Event Model** | `ActionListener`, `ItemListener`, `KeyListener`, `MouseListener`, `TreeSelectionListener`, and custom `TaskStatusChangeListener`. |
| **Module 4** | **Database Connectivity (JDBC)** | Complete CRUD with `Connection`, `PreparedStatement`, `ResultSet`, and atomic transactions (`setAutoCommit(false)`, `commit()`, `rollback()`). |
| **Module 4** | **Auto-Seeding & Schema Initializer** | `DbInitializer` creates SQLite tables (`users`, `projects`, `tasks`, `task_logs`) and seeds 3-level recursive demo trees automatically. |

---

## 🚀 Quick Start Guide

### Prerequisites
- Java SE Development Kit (JDK 17 or higher, fully tested on JDK 21 and JDK 26)
- Windows / macOS / Linux

### Default Demo Credentials

| Role | Username | Password | Full Name | Department / Tech Stack |
| :--- | :--- | :--- | :--- | :--- |
| **Project Manager** | `admin` | `admin123` | Dr. Arun Kumar | Cloud & AI Labs (Full Administrative Access) |
| **Software Developer** | `dev_alice` | `alice123` | Alice George | Java 21, Swing, Spring Boot |
| **Software Developer** | `dev_bob` | `bob123` | Bob Varghese | Java, Microservices, SQLite |

---

### Running the Application

#### Option 1: Using the provided Batch Script (Windows)
Double-click `run.bat` or execute in Command Prompt:
```cmd
run.bat
```

#### Option 2: Using PowerShell
```powershell
.\run.ps1
```

#### Option 3: Manual Compilation & Execution
```cmd
REM 1. Compile
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" -d bin src\com\pms\*.java src\com\pms\model\*.java src\com\pms\dao\*.java src\com\pms\service\*.java src\com\pms\gui\*.java src\com\pms\gui\panel\*.java src\com\pms\gui\dialog\*.java src\com\pms\gui\adapter\*.java src\com\pms\gui\event\*.java src\com\pms\gui\theme\*.java src\com\pms\util\*.java src\com\pms\exception\*.java

REM 2. Run
java -cp "bin;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" com.pms.Main
```

---

## 🧪 Automated Syllabus Verification Test Suite

A standalone test runner `com.pms.OOPVerificationTest` executes 33 automated tests validating every single module in the KTU syllabus:

```cmd
test.bat
```
or
```cmd
java -cp "bin;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" com.pms.OOPVerificationTest
```

**Verification Output:**
```
=======================================================================
  KTU PBCST304 OOP SYLLABUS - RIGOROUS VERIFICATION TEST RUNNER       
=======================================================================
--- MODULE 1: Fundamentals, Flow Control, Types, CLI, & Varargs ---
  [PASS] CLI Arguments parsed db path correctly                      
  [PASS] CLI Arguments parsed wrapper integer port                   
  [PASS] CLI Arguments parsed log level enum                         
  [PASS] Array manipulation adds tags properly                       
  [PASS] Vector representation contains items                        
  [PASS] String formatting formats tags                              
--- MODULE 2: Core OOP Principles, Inheritance & Dynamic Method Dispatch ---
  [PASS] Dynamic Dispatch: Manager has CREATE_PROJECT permission     
  [PASS] Dynamic Dispatch: Developer is denied CREATE_PROJECT permission
  [PASS] Dynamic Dispatch: Developer has UPDATE_TASK_STATUS permission
  [PASS] Dynamic Dispatch: Manager role description                  
  [PASS] Dynamic Dispatch: Developer role description                
  [PASS] Method Overloading: logHours(double)                        
  [PASS] Method Overloading: logHours(double, String)                
--- MODULE 2: Recursive Algorithms on Hierarchical Task Composite Trees ---
  [PASS] Recursion 1: subTask2 progress = (100+0)/2 = 50.0%          
  [PASS] Recursion 2: rootTask progress = (100+50)/2 = 75.0%         
  [PASS] Recursion 3: Task hierarchy dependency depth = 2            
  [PASS] Recursion 4: Total recursive sub-task descendants = 4       
--- MODULE 2: Inner Classes (Member Inner, Static Nested, Builder) ---
  [PASS] Static Nested Class: Task.Builder instantiated Task         
  [PASS] Member Inner Class: ProjectSummary accesses outer class state
  [PASS] Member Inner Class: TaskAuditEntry records audit summary    
--- MODULE 3: Exceptions, Singleton, & SOLID Principles ---
  [PASS] Singleton Pattern: DatabaseConnectionManager instances are identical
  [PASS] Task State Machine: TODO cannot transition directly to COMPLETED
  [PASS] Task State Machine: TODO can transition to IN_PROGRESS      
  [PASS] Custom Exception: UnauthorizedAccessException contains error code
  [PASS] Custom Exception: UnauthorizedAccessException correctly caught
  [PASS] Custom Exception: ProjectNotFoundException holds ID         
--- MODULE 4: Embedded Database (JDBC) & SQL Transaction CRUD ---
  [PASS] JDBC: Located admin in SQLite DB                            
  [PASS] Polymorphic DB Mapping: admin row instantiated as Manager subclass
  [PASS] JDBC: Located dev_alice in SQLite DB                        
  [PASS] Polymorphic DB Mapping: dev_alice row instantiated as Developer subclass
  [PASS] JDBC: Retrieved seeded projects from DB                     
  [PASS] JDBC: Retrieved top-level tasks where parent_task_id IS NULL
  [PASS] JDBC Transaction: Hour logging atomically updated task record
=======================================================================
  TEST RESULTS: 33/33 PASSED (100% COMPLIANCE)
=======================================================================
```

---

## 🖥️ Interactive GUI Features & User Experience

1. **Quick-Select Demo Switcher:** The login dialog has a dedicated combo box allowing instant switching between Manager (`admin`) and Developer (`dev_alice`, `dev_bob`) profiles with pre-filled passwords.
2. **Dynamic Role-Based Access Control (RBAC):**
   - Log in as **Manager**: full rights to create projects, edit projects, delete records, assign tasks.
   - Log in as **Developer**: administrative actions (`+ New Project`, `Delete Project`, `Delete Task`) are guarded. Attempting unauthorized operations triggers the custom `UnauthorizedAccessException` handled with informative user alerts.
3. **Recursive Work Breakdown Structure (WBS) JTree:**
   - Displays projects and nested task hierarchies.
   - Dynamically evaluates and displays the completion percentage and dependency depth calculated recursively at every level.
4. **State Machine Validation:**
   - Tasks follow strict workflow states: `TODO` ➔ `IN_PROGRESS` ➔ `REVIEW` ➔ `COMPLETED`.
   - Direct illegal jumps (e.g. `TODO` directly to `COMPLETED`) raise `InvalidTaskStateException` and inform the user.
5. **Atomic JDBC Work Logging:**
   - Developers can log hours with quick +1h, +2h, +4h, +8h buttons.
   - Uses SQL transactions with `commitTransaction()` and rollback support.
6. **Live Role Switcher in Team Tab:**
   - The "Team & Permissions" tab displays all users with their polymorphic details.
   - Users can select any team member and click *"Switch Active Session to Selected User"* to instantly test permissions from different viewpoints without restarting!
