# KTU PBCST304: Object-Oriented Programming (Java)
## Comprehensive Project Viva & Syllabus Reference Guide
**Course Code:** PBCST304  
**Project:** Enterprise Project Management System (PMS)  
**Author:** APJ Abdul Kalam Technological University (KTU) Student  

---

## 📑 Table of Contents
1. [Overview & Project Architecture](#1-overview--project-architecture)
2. [Module 1: Fundamentals, Flow Control, Structure & Types](#2-module-1-fundamentals-flow-control-structure--types)
3. [Module 2: Core OOP Principles & Architecture](#3-module-2-core-oop-principles--architecture)
4. [Module 3: Advanced Structure, Exceptions & Design Patterns](#4-module-3-advanced-structure-exceptions--design-patterns)
5. [Module 4: Event-Driven GUI (Java Swing) & Workflow State Machine](#5-module-4-event-driven-gui-java-swing--workflow-state-machine)
6. [Top 15 Examiner Viva Questions & Model Answers](#6-top-15-examiner-viva-questions--model-answers)
7. [Code Navigation Quick Index](#7-code-navigation-quick-index)

---

## 1. Overview & Project Architecture

The **Enterprise Project Management System (PMS)** is an interactive desktop application engineered using standard Java SE (JDK) and Swing. It implements the enterprise **Model-View-Controller (MVC)** architectural pattern alongside **SOLID** design principles.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VIEW / PRESENTATION LAYER                       │
│  MainFrame (JFrame)                                                    │
│  ├── DashboardPanel (Real-time analytics, recursive progress rollups)  │
│  ├── ProjectManagementPanel (JTable, full-field modal dialogs, search) │
│  ├── TaskManagementPanel (JSplitPane: JTree WBS + JTable with badges)  │
│  └── TeamManagementPanel (Polymorphic user roster & role switching)    │
│  Dialogs: LoginDialog, ProjectFormDialog, TaskFormDialog, AddTeamMember│
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Delegation Event Model / Adapters
┌────────────────────────────────────▼───────────────────────────────────┐
│                    ADAPTER & EVENT-DRIVEN BUS                          │
│  ├── ProjectTableModelAdapter (Adapter Pattern: List/Vector -> Table)   │
│  ├── TaskTableModelAdapter (Adapter Pattern: Recursive Progress)       │
│  └── TaskStatusChangeEvent & TaskStatusChangeListener (Custom Event)   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Dependency Inversion
┌────────────────────────────────────▼───────────────────────────────────┐
│                          SERVICE / BUSINESS LAYER                      │
│  ├── ProjectService (RBAC validation, recursive completion rollups)    │
│  └── TaskService (State machine enforcement, recursive tree builder)   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Interface Contracts
┌────────────────────────────────────▼───────────────────────────────────┐
│                       DATA ACCESS LAYER (DAO)                          │
│  ├── DatabaseManager (Singleton Pattern with Double-Checked Locking)   │
│  └── Repository<T, ID> extends Searchable<T> (Interface Inheritance)   │
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

## 2. Module 1: Fundamentals, Flow Control, Structure & Types

### 1.1 Classes, Objects, and Object References
* **Syllabus Topic:** Defining classes, creating instances using `new`, memory allocation on heap vs. stack reference variables.
* **In Code:** Classes `Project`, `Task`, `Manager`, `Developer`.
  ```java
  Manager admin = new Manager(1, "admin", "admin123", "Dr. Arun Kumar", "arun@ktu.edu", "Engineering", 1000000.0);
  ```
* **Viva Explanation:** A class is a blueprint; an object is a runtime instance. `Manager` is the class, `admin` is the reference variable on the stack, and `new Manager(...)` dynamically allocates memory on the heap.

### 1.2 Wrapper Classes & Primitive Type Casting
* **Syllabus Topic:** Wrapping primitives (`int`, `double`) into objects (`Integer`, `Double`), autoboxing, unboxing, explicit type casting.
* **In Code:**
  ```java
  // Wrapper class parsing (String to double)
  Double budget = Double.parseDouble(txtBudget.getText().trim());

  // Primitive Type Casting (explicit casting in mathematical division)
  double avg = sum / (double) subTasks.size();

  // Primitive casting from double to int for JProgressBar
  bar.setValue((int) Math.round(pct));
  ```
* **Viva Explanation:** Wrapper classes allow primitive types to be treated as objects (such as permitting a top-level task to hold a `null` parent ID). Primitive casting `(double)` avoids integer division truncation.

### 1.3 Primitive Arrays vs. Vectors
* **Syllabus Topic:** Static arrays vs. synchronized legacy collections (`Vector`).
* **In Code:**
  ```java
  // Primitive 1D Array in Project.java
  private String[] tags;
  String[] newTags = Arrays.copyOf(tags, tags.length + 1);

  // Vector for Swing Table interoperability
  Vector<String> cols = new Vector<>(Arrays.asList("ID", "Full Name", "Username", "Role"));
  Vector<Vector<Object>> data = new Vector<>();
  ```
* **Viva Explanation:** `String[]` is fixed in size and used for domain entity tags. `Vector` is a synchronized, dynamic list utilized for Swing `DefaultTableModel` rows and column headers.

### 1.4 Strings & StringBuilder (Immutability)
* **Syllabus Topic:** String immutability in the String Pool vs. mutable `StringBuilder` for efficient loops.
* **In Code:** In `Project.getTagsFormatted()`:
  ```java
  StringBuilder sb = new StringBuilder();
  for (int i = 0; i < tags.length; i++) {
      sb.append(tags[i]);
      if (i < tags.length - 1) sb.append(", ");
  }
  return sb.toString();
  ```
* **Viva Explanation:** Standard `String` concatenation in loops creates excessive temporary objects. `StringBuilder` provides mutable buffer appending for optimal memory utilization.

### 1.5 Variable-Length Arguments (Varargs)
* **Syllabus Topic:** Methods accepting zero or more parameters using `Type... name`.
* **In Code:** In `AppLogger.java`:
  ```java
  public static void log(LogLevel level, String message, Object... args) {
      String formatted = (args != null && args.length > 0) ? String.format(message, args) : message;
      // ...
  }
  ```
* **Viva Explanation:** Varargs allows passing an arbitrary number of arguments packed into an array, eliminating the need to write dozens of overloaded logging functions.

### 1.6 Control Flow (Switch & Loops)
* **Syllabus Topic:** Branching evaluation with `switch` on Enums and Strings.
* **In Code:** In `Developer.hasPermission()`:
  ```java
  switch (permission) {
      case UPDATE_TASK_STATUS:
      case LOG_HOURS:
      case CREATE_TASK:
      case EDIT_TASK:
          return true;
      default:
          return false;
  }
  ```

---

## 3. Module 2: Core OOP Principles & Architecture

### 2.1 Access Modifiers & Encapsulation
* **Syllabus Topic:** `public`, `protected`, default (package-private), `private` data hiding.
* **In Code:**
  * `private`: Internal collections in `DatabaseManager` (`private final List<User> userTable;`).
  * `protected`: State variables in `BaseEntity` and `User` (`protected Integer id;`, `protected String username;`) enabling direct access by subclasses.
  * `public`: Service interfaces and public business methods.

### 2.2 Dynamic use of `this` Keyword
* **Syllabus Topic:** Variable disambiguation, constructor chaining, method chaining in Builder pattern.
* **In Code:**
  * Disambiguation: `this.title = title;`
  * Constructor Chaining:
    ```java
    public Project(String title, String description) {
        this(null, title, description, 0.0, ProjectStatus.PLANNING, "Internal Client", new String[0]);
    }
    ```
  * Method Chaining in Builder:
    ```java
    public Builder priority(TaskPriority p) {
        this.priority = p;
        return this; // Returns current instance
    }
    ```

### 2.3 Constructor Overloading & Method Overloading
* **Syllabus Topic:** Compile-time polymorphism (same name, differing parameter signatures).
* **In Code:**
  * Constructor Overloading: `Project(String, String)` vs. `Project(Integer, String, String, double, ProjectStatus, String, String[])`.
  * Method Overloading in `Task.java`:
    ```java
    public void logHours(double hours) { ... }
    public void logHours(double hours, String note) { ... }
    ```

### 2.4 Inheritance Hierarchy & `super` Keyword
* **Syllabus Topic:** IS-A relationship, extending superclasses, calling parent constructors and methods via `super`.
* **In Code:**
  ```java
  public static class Manager extends User {
      private String department;
      private double budgetApprovalLimit;

      public Manager(Integer id, String username, String password, String fullName, String email, String dept, double limit) {
          super(id, username, password, fullName, email, UserRole.MANAGER); // Chaining to parent User
          this.department = dept;
          this.budgetApprovalLimit = limit;
      }
  }
  ```
* **Viva Explanation:** `Manager` inherits common fields from `User` and `BaseEntity`. When instantiated, constructor chaining executes in order: `BaseEntity` ➔ `User` ➔ `Manager`.

### 2.5 Abstract Classes vs. Interfaces
* **Syllabus Topic:** Abstract classes with partial implementations vs. pure abstract contracts in interfaces.
* **In Code:**
  * Interfaces: `Identifiable`, `Auditable`, `Searchable<T>`, `Repository<T, ID>`, `TaskStatusChangeListener`.
  * Abstract Classes: `BaseEntity` and `User`.
* **Viva Explanation:** `User` is abstract because every system user must be a specific concrete role (`Manager` or `Developer`). Interfaces define pure behavioral contracts without state.

### 2.6 Dynamic Method Dispatch (Runtime Polymorphism)
* **Syllabus Topic:** A superclass reference invoking an overridden method resolved at runtime based on the actual object instance.
* **In Code:**
  ```java
  User currentUser = ...; // Superclass reference!

  // Dynamic Method Dispatch:
  boolean permitted = currentUser.hasPermission(Permission.CREATE_PROJECT);
  String roleDesc = currentUser.getRoleDescription();
  ```
* **Viva Explanation:** Although `currentUser` is typed as `User`, Java evaluates at runtime whether the object is a `Manager` or `Developer` and executes the corresponding subclass implementation.

### 2.7 Recursive Algorithms
* **Syllabus Topic:** Methods invoking themselves with a base case to terminate and a recursive step to traverse self-similar hierarchical data.
* **In Code:** In `Task.java`:
  ```java
  public double calculateCompletionPercentage() {
      // 1. BASE CASE: Leaf task (no children)
      if (subTasks.isEmpty()) {
          switch (this.status) {
              case COMPLETED:   return 100.0;
              case REVIEW:      return 80.0;
              case IN_PROGRESS: return estimatedHours > 0 ? Math.min(90.0, Math.max(25.0, (loggedHours / estimatedHours) * 100.0)) : 50.0;
              case TODO:
              default:          return 0.0;
          }
      }

      // 2. RECURSIVE STEP: Traverses all nested child sub-tasks
      double sum = 0.0;
      for (Task child : subTasks) {
          sum += child.calculateCompletionPercentage(); // Recursive invocation!
      }
      return sum / (double) subTasks.size();
  }
  ```
  And in `calculateDependencyDepth()`:
  ```java
  public int calculateDependencyDepth() {
      if (subTasks.isEmpty()) return 0; // Base case
      int maxDepth = 0;
      for (Task child : subTasks) {
          int d = child.calculateDependencyDepth(); // Recursive invocation!
          if (d > maxDepth) maxDepth = d;
      }
      return 1 + maxDepth;
  }
  ```
* **Viva Explanation:** Tasks can be nested to arbitrary depths. Recursion computes completion percentages and tree depths without requiring prior knowledge of how many nesting levels exist.

### 2.8 Inner Classes (All 3 Types Implemented)
* **Syllabus Topic:** Member Inner Classes, Static Nested Classes, and Anonymous Inner Classes.
* **In Code:**
  1. **Member Inner Class:** `public class ProjectSummary` inside `class Project`:
     * Directly accesses outer class fields via `Project.this.title` and `Project.this.budget`.
  2. **Static Nested Class:** `public static class Builder` inside `class Task` and `public static class Credentials` inside `class User`:
     * Exists independently of outer class instances, enabling clean object construction.
  3. **Anonymous Inner Class:** Used in event listeners and table renderers:
     ```java
     btnNew.addActionListener(new ActionListener() { // Anonymous Inner Class
         @Override
         public void actionPerformed(ActionEvent e) { handleNewProject(); }
     });
     ```

---

## 4. Module 3: Advanced Structure, Exceptions & Design Patterns

### 3.1 Custom Interface Inheritance
* **Syllabus Topic:** An interface extending another interface using `extends`.
* **In Code:**
  ```java
  public interface Searchable<T> {
      List<T> search(String keyword) throws DatabaseOperationException;
  }

  // Custom Interface Inheritance:
  public interface Repository<T, ID> extends Searchable<T> {
      T save(T entity) throws DatabaseOperationException;
      Optional<T> findById(ID id) throws DatabaseOperationException;
  }
  ```

### 3.2 Custom Exception Hierarchy & Nested Try Blocks
* **Syllabus Topic:** Checked custom exceptions extending `Exception`, `throw`, `throws`, and nested `try-catch` blocks.
* **In Code:**
  * Base: `PMSException extends Exception`
  * Subclasses: `ProjectNotFoundException`, `TaskNotFoundException`, `UnauthorizedAccessException`, `InvalidTaskStateException`, `DatabaseOperationException`.
  * **Nested Try Block in Transaction Rollback (`DatabaseManager.commitTransaction()`):**
    ```java
    try {
        if (!inTransaction) return;
        this.inTransaction = false;
    } catch (Exception e) {
        // NESTED TRY BLOCK: Ensures rollback safety if commit fails
        try {
            rollbackTransaction();
        } catch (Exception rollbackEx) {
            AppLogger.error("Rollback failed: %s", rollbackEx.getMessage());
        }
        throw new DatabaseOperationException("Failed to commit transaction", e);
    }
    ```

### 3.3 Design Patterns
* **A. Singleton Pattern (`DatabaseManager`):**
  * Private constructor: `private DatabaseManager() { ... }`
  * Double-Checked Locking:
    ```java
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
    ```
* **B. Adapter Pattern (`ProjectTableModelAdapter` & `TaskTableModelAdapter`):**
  * Bridges domain collections (`List<Project>`, `List<Task>`) with Swing's `JTable` by extending `AbstractTableModel` and overriding `getValueAt()`, `getRowCount()`, and `getColumnCount()`.
* **C. Builder Pattern (`Task.Builder`):**
  * Facilitates readable, multi-parameter task creation through method chaining.

### 3.4 SOLID Principles
* **SRP (Single Responsibility):** Separate classes for data storage (`DatabaseManager`), business validation (`ProjectService`), and UI presentation (`MainFrame`).
* **OCP (Open/Closed):** New roles can be introduced by subclassing `User` without modifying existing services.
* **LSP (Liskov Substitution):** Subclasses `Manager` and `Developer` seamlessly substitute for `User`.
* **ISP (Interface Segregation):** Focused interfaces (`Identifiable`, `Auditable`, `Searchable`) instead of monolithic interfaces.
* **DIP (Dependency Inversion):** High-level services depend on abstract contracts rather than concrete GUI components.

---

## 5. Module 4: Event-Driven GUI (Java Swing) & Workflow State Machine

### 5.1 Swing Controls & Layout Managers
* **Swing Controls:**
  * Containers: `JFrame`, `JDialog`, `JPanel`, `JSplitPane`, `JTabbedPane`, `JScrollPane`.
  * Controls: `JLabel`, `JButton`, `JTextField`, `JPasswordField`, `JTextArea`, `JComboBox`, `JProgressBar`.
  * Complex Data Views: `JTable` (tabular view) and `JTree` (interactive Work Breakdown Structure).
* **Layout Managers:**
  * `BorderLayout`: Main application frame (Header NORTH, Tabs CENTER, Status Bar SOUTH).
  * `GridBagLayout`: Dynamic form dialogs (`ProjectFormDialog`, `TaskFormDialog`, `LoginDialog`).
  * `FlowLayout`: Header toolbars and button panels.
  * `GridLayout`: 4-column KPI cards and workflow status breakdown.
  * `BoxLayout`: Vertical progress rollups on the Dashboard.

### 5.2 Delegation Event Model & Custom Events
* **Syllabus Topic:** Event Sources generating Event Objects listened to by Event Listeners.
* **In Code:**
  * Standard Listeners: `ActionListener`, `ItemListener`, `KeyAdapter`, `MouseAdapter`, `TreeSelectionListener`.
  * **Custom Event:** `TaskStatusChangeEvent extends java.util.EventObject`.
  * **Custom Listener:** `TaskStatusChangeListener extends java.util.EventListener`.

### 5.3 Workflow State Machine Validation
* **Syllabus Topic:** Enforcing lifecycle state transitions via business logic.
* **In Code:** In `TaskStatus.canTransitionTo()`:
  $$\text{TODO} \longrightarrow \text{IN\_PROGRESS} \longrightarrow \text{REVIEW} \longrightarrow \text{COMPLETED}$$
  Direct jumps (e.g., `TODO` straight to `COMPLETED`) throw `InvalidTaskStateException`.

---

## 6. Top 15 Examiner Viva Questions & Model Answers

### Q1: What architecture does this application follow?
> **Answer:** It implements the **Model-View-Controller (MVC)** architectural pattern adhering to **SOLID** principles. The domain entities form the Model, Java Swing forms the View, and the Service layer acts as the Controller.

### Q2: Why is `User` declared as an abstract class instead of an interface?
> **Answer:** `User` contains shared state attributes (`id`, `username`, `password`, `fullName`, `email`) and common concrete methods (`getProfileBadgeText()`), while delegating role-specific authorization (`hasPermission()`) to subclasses. An interface cannot hold mutable instance state.

### Q3: Show me where Dynamic Method Dispatch is demonstrated.
> **Answer:** In `currentUser.hasPermission(Permission)`. The reference variable is of superclass type `User`. At runtime, Java dynamically binds the call to `Manager.hasPermission()` or `Developer.hasPermission()` based on the logged-in user instance.

### Q4: Why did you use Recursion in `Task.java`?
> **Answer:** Tasks in a Work Breakdown Structure (WBS) can have child sub-tasks nested to arbitrary depths. `calculateCompletionPercentage()` recursively aggregates progress across all descendant tasks. The base case evaluates leaf tasks, while the recursive step averages child completion values.

### Q5: What is the purpose of the Singleton Pattern in `DatabaseManager`?
> **Answer:** It ensures that only one shared instance of the database manager exists throughout the application lifecycle. It uses a private constructor and thread-safe Double-Checked Locking in `getInstance()`.

### Q6: How does the Adapter Pattern work with `JTable`?
> **Answer:** Swing's `JTable` cannot directly display domain entity lists. `ProjectTableModelAdapter` and `TaskTableModelAdapter` extend `AbstractTableModel` to adapt `List<Project>` and `List<Task>` into rows and columns using `getValueAt(row, col)`.

### Q7: What happens when a Developer attempts to delete a project?
> **Answer:** The service layer evaluates `currentUser.hasPermission(DELETE_PROJECT)`. For a `Developer`, this returns `false`, causing the system to throw an `UnauthorizedAccessException`, which is caught to display an "Access Denied" dialog.

### Q8: What are the different types of inner classes implemented in your project?
> **Answer:**
> 1. **Member Inner Class:** `Project.ProjectSummary` (accesses outer fields via `Project.this.title`).
> 2. **Static Nested Class:** `Task.Builder` and `User.Credentials`.
> 3. **Anonymous Inner Class:** Event listeners and custom table renderers.

### Q9: What is constructor call chaining order in Java?
> **Answer:** When an object of a derived class is created, Java invokes constructors starting from the highest superclass down to the derived class: `BaseEntity` ➔ `User` ➔ `Manager`.

### Q10: Where did you use Varargs?
> **Answer:** In `AppLogger.log(LogLevel level, String message, Object... args)` to allow logging with an arbitrary number of parameters.

### Q11: What is the difference between `throw` and `throws`?
> **Answer:** `throw` explicitly raises an exception object (`throw new UnauthorizedAccessException(...)`), whereas `throws` is part of a method signature declaring that the method may propagate an exception (`public void deleteProject(...) throws UnauthorizedAccessException`).

### Q12: How does the JTree synchronize with the JTable?
> **Answer:** A `TreeSelectionListener` is attached to the `JTree`. When a tree node is selected, its user object provides the task ID, and the application programmatically selects and scrolls to the corresponding row in the `JTable`.

### Q13: Why is `StringBuilder` used instead of `String` in `getTagsFormatted()`?
> **Answer:** `String` is immutable, so repeated concatenation inside loops creates unnecessary objects in the String Pool. `StringBuilder` provides a mutable buffer for efficient string construction.

### Q14: How does the state machine prevent illegal task transitions?
> **Answer:** In `TaskStatus.canTransitionTo()`, allowed transitions are enforced (`TODO` ➔ `IN_PROGRESS` ➔ `REVIEW` ➔ `COMPLETED`). If an illegal transition is attempted, the system throws an `InvalidTaskStateException`.

### Q15: What is the purpose of `super` in the `Manager` constructor?
> **Answer:** `super(id, username, password, fullName, email, UserRole.MANAGER)` explicitly calls the parameterized constructor of the superclass `User` to initialize inherited state.

---

## 7. Code Navigation Quick Index

When opening [`ProjectManagementSystem.java`](file:///C:/Users/User/Desktop/ProjectManagementSystem.java) during your viva, search for these keywords:

| Syllabus Keyword | Search Keyword in Code | Class / Location |
| :--- | :--- | :--- |
| **Varargs** | `Object... args` | `AppLogger.java` |
| **Custom Exceptions** | `class UnauthorizedAccessException` | Exception Hierarchy |
| **Abstract Superclass** | `abstract class User` | Domain Entities |
| **Inheritance & `super`** | `class Manager extends User` | `Manager.java` |
| **Dynamic Method Dispatch** | `currentUser.hasPermission` | Services & Role checks |
| **Method Overloading** | `logHours(double hours, String note)` | `Task.java` |
| **Recursive Percentage** | `calculateCompletionPercentage()` | `Task.java` |
| **Recursive Depth** | `calculateDependencyDepth()` | `Task.java` |
| **Member Inner Class** | `class ProjectSummary` | `Project.java` |
| **Builder Pattern** | `class Builder` | `Task.java` |
| **Singleton Pattern** | `getInstance()` | `DatabaseManager.java` |
| **Adapter Pattern** | `class ProjectTableModelAdapter` | GUI Adapters |
| **WBS Hierarchy Tree** | `buildTreeNodesRecursively` | `MainFrame.java` |
| **State Machine Check** | `canTransitionTo` | `TaskStatus.java` |
