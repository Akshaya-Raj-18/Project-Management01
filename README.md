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
