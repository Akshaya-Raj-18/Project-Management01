package com.pms.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import com.pms.dao.IUserDao;
import com.pms.exception.InvalidTaskStateException;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.gui.adapter.TaskTableModelAdapter;
import com.pms.gui.dialog.LogHoursDialog;
import com.pms.gui.dialog.TaskDialog;
import com.pms.gui.event.TaskStatusChangeEvent;
import com.pms.gui.event.TaskStatusChangeListener;
import com.pms.gui.theme.UITheme;
import com.pms.model.Permission;
import com.pms.model.Project;
import com.pms.model.Task;
import com.pms.model.TaskStatus;
import com.pms.model.User;
import com.pms.service.IProjectService;
import com.pms.service.ITaskService;
import com.pms.util.AppLogger;

/**
 * Task & Sub-task Management Panel featuring JTree hierarchy and JTable with custom renderers.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Vectors, Primitive casting, Strings.
 * - Module 2: Recursion (building dynamic JTree hierarchy and rendering subtask completion),
 *   Anonymous inner classes (listeners).
 * - Module 3: Adapter Pattern, Custom Exceptions (InvalidTaskStateException, UnauthorizedAccessException).
 * - Module 4: Swing controls (JSplitPane, JTree, JTable), Delegation event model,
 *   Custom Event Dispatching (TaskStatusChangeListener).
 */
public class TaskManagementPanel extends JPanel {

    private final ITaskService taskService;
    private final IProjectService projectService;
    private final IUserDao userDao;
    private User currentUser;

    // Custom Event Listener list
    private final List<TaskStatusChangeListener> statusListeners = new ArrayList<>();

    // GUI Components
    private JComboBox<ProjectComboItem> cmbProjectFilter;
    private JComboBox<String> cmbStatusFilter;
    private JTextField txtSearch;
    private JTable taskTable;
    private TaskTableModelAdapter tableAdapter;
    private JTree hierarchyTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode treeRoot;

    private JButton btnNewTask;
    private JButton btnUpdateStatus;
    private JButton btnLogHours;
    private JButton btnDeleteTask;

    public TaskManagementPanel(ITaskService taskService, IProjectService projectService,
                               IUserDao userDao, User currentUser) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userDao = userDao;
        this.currentUser = currentUser;

        initComponents();
        loadProjectsIntoFilter();
        refreshAllData();
    }

    public void addStatusChangeListener(TaskStatusChangeListener listener) {
        if (listener != null) {
            statusListeners.add(listener);
        }
    }

    private void fireStatusChangeEvent(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        TaskStatusChangeEvent evt = new TaskStatusChangeEvent(this, task, oldStatus, newStatus);
        for (TaskStatusChangeListener listener : statusListeners) {
            listener.onTaskStatusChanged(evt);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateRolePermissions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Left Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Project:"));
        cmbProjectFilter = new JComboBox<>();
        cmbProjectFilter.setFont(UITheme.FONT_REGULAR);
        cmbProjectFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(cmbProjectFilter);

        filterPanel.add(new JLabel("Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "TODO", "IN_PROGRESS", "REVIEW", "COMPLETED"});
        cmbStatusFilter.setFont(UITheme.FONT_REGULAR);
        cmbStatusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(cmbStatusFilter);

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_REGULAR);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(txtSearch);

        topPanel.add(filterPanel, BorderLayout.WEST);

        // Right Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnNewTask = UITheme.createPrimaryButton("+ New Task / Sub-task");
        btnNewTask.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewTask();
            }
        });

        btnUpdateStatus = UITheme.createSecondaryButton("Transition Status");
        btnUpdateStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTransitionStatus();
            }
        });

        btnLogHours = UITheme.createSecondaryButton("Log Hours");
        btnLogHours.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogHours();
            }
        });

        btnDeleteTask = UITheme.createDangerButton("Delete");
        btnDeleteTask.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteTask();
            }
        });

        actionPanel.add(btnNewTask);
        actionPanel.add(btnUpdateStatus);
        actionPanel.add(btnLogHours);
        actionPanel.add(btnDeleteTask);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center SplitPane: JTree (Hierarchy) on Left, JTable on Right
        tableAdapter = new TaskTableModelAdapter();
        taskTable = new JTable(tableAdapter);
        taskTable.setRowHeight(32);
        taskTable.setFont(UITheme.FONT_REGULAR);
        taskTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Cell Renderers (Status Pill, Priority Pill, Progress Bar)
        taskTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.createPriorityBadgeRenderer());
        taskTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusBadgeRenderer());
        taskTable.getColumnModel().getColumn(7).setCellRenderer(UITheme.createProgressCellRenderer());

        JScrollPane scrollTable = new JScrollPane(taskTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // JTree for Recursive Sub-task Hierarchy
        treeRoot = new DefaultMutableTreeNode("Project Work Breakdown Structure (WBS)");
        treeModel = new DefaultTreeModel(treeRoot);
        hierarchyTree = new JTree(treeModel);
        hierarchyTree.setFont(UITheme.FONT_REGULAR);
        hierarchyTree.setRowHeight(24);

        hierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) hierarchyTree.getLastSelectedPathComponent();
                if (node != null && node.getUserObject() instanceof TaskTreeNode) {
                    TaskTreeNode tNode = (TaskTreeNode) node.getUserObject();
                    selectTaskInTable(tNode.task.getId());
                }
            }
        });

        JScrollPane scrollTree = new JScrollPane(hierarchyTree);
        scrollTree.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));
        scrollTree.setPreferredSize(new Dimension(300, 400));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, scrollTable);
        splitPane.setDividerLocation(320);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);

        updateRolePermissions();
    }

    private void updateRolePermissions() {
        if (currentUser != null) {
            btnDeleteTask.setEnabled(currentUser.hasPermission(Permission.DELETE_TASK));
        }
    }

    public void selectProject(int projectId) {
        for (int i = 0; i < cmbProjectFilter.getItemCount(); i++) {
            ProjectComboItem item = cmbProjectFilter.getItemAt(i);
            if (item.project != null && item.project.getId().equals(projectId)) {
                cmbProjectFilter.setSelectedIndex(i);
                break;
            }
        }
    }

    private void selectTaskInTable(int taskId) {
        for (int i = 0; i < tableAdapter.getRowCount(); i++) {
            Task t = tableAdapter.getTaskAt(i);
            if (t != null && t.getId().equals(taskId)) {
                taskTable.setRowSelectionInterval(i, i);
                taskTable.scrollRectToVisible(taskTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    public void loadProjectsIntoFilter() {
        try {
            cmbProjectFilter.removeAllItems();
            cmbProjectFilter.addItem(new ProjectComboItem(null, "All Projects"));
            List<Project> projects = projectService.getAllProjects();
            for (Project p : projects) {
                cmbProjectFilter.addItem(new ProjectComboItem(p, "#" + p.getId() + " - " + p.getTitle()));
            }
        } catch (Exception e) {
            AppLogger.error("Failed to load projects into filter", e);
        }
    }

    public void refreshAllData() {
        applyFilters();
        refreshTreeHierarchy();
    }

    private void applyFilters() {
        try {
            ProjectComboItem projItem = (ProjectComboItem) cmbProjectFilter.getSelectedItem();
            String statusStr = (String) cmbStatusFilter.getSelectedItem();
            String search = txtSearch.getText().trim();

            List<Task> tasks;
            if (projItem != null && projItem.project != null) {
                tasks = taskService.getTasksByProject(projItem.project.getId());
            } else {
                tasks = taskService.getAllTasks();
            }

            if (statusStr != null && !"ALL".equals(statusStr)) {
                TaskStatus target = TaskStatus.valueOf(statusStr);
                tasks.removeIf(t -> t.getStatus() != target);
            }

            if (!search.isEmpty()) {
                String lower = search.toLowerCase();
                tasks.removeIf(t -> !t.getTitle().toLowerCase().contains(lower) &&
                                    !t.getDescription().toLowerCase().contains(lower));
            }

            tableAdapter.setTasks(tasks);
        } catch (Exception e) {
            AppLogger.error("Error applying task filters", e);
        }
    }

    /**
     * Rebuilds the JTree by querying hierarchical sub-tasks recursively.
     * Demonstrates Module 2: Recursion.
     */
    private void refreshTreeHierarchy() {
        try {
            treeRoot.removeAllChildren();
            List<Project> projects = projectService.getAllProjects();

            for (Project proj : projects) {
                DefaultMutableTreeNode projNode = new DefaultMutableTreeNode(
                        String.format("📁 Project #%d: %s [%s]", proj.getId(), proj.getTitle(), proj.getStatus()));

                // Fetch top-level task trees populated recursively
                List<Task> hierarchy = taskService.buildTaskHierarchy(proj.getId());
                for (Task rootTask : hierarchy) {
                    // Recursively build tree nodes
                    DefaultMutableTreeNode taskNode = buildTreeNodeRecursively(rootTask);
                    projNode.add(taskNode);
                }

                treeRoot.add(projNode);
            }

            treeModel.reload();
            // Expand all rows for immediate visual clarity
            for (int i = 0; i < hierarchyTree.getRowCount(); i++) {
                hierarchyTree.expandRow(i);
            }
        } catch (Exception e) {
            AppLogger.error("Error refreshing task tree hierarchy", e);
        }
    }

    /**
     * RECURSIVE ALGORITHM: Populates DefaultMutableTreeNode with nested child sub-tasks.
     * Evaluates task.calculateCompletionPercentage() recursively at each level.
     */
    private DefaultMutableTreeNode buildTreeNodeRecursively(Task task) {
        // Module 2: Recursively evaluates sub-task completion percentage
        double completion = task.calculateCompletionPercentage();
        int depth = task.calculateDependencyDepth();

        String label = String.format("📌 #%d: %s [%s] (%.0f%% - Depth: %d)",
                task.getId(), task.getTitle(), task.getStatus(), completion, depth);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TaskTreeNode(task, label));

        for (Task child : task.getSubTasks()) {
            // Recursive descent
            node.add(buildTreeNodeRecursively(child));
        }
        return node;
    }

    private void handleNewTask() {
        try {
            List<Project> projects = projectService.getAllProjects();
            List<Task> allTasks = taskService.getAllTasks();
            List<User> users = userDao.findAll();

            ProjectComboItem currentProjItem = (ProjectComboItem) cmbProjectFilter.getSelectedItem();
            Integer defaultProjId = (currentProjItem != null && currentProjItem.project != null) ?
                    currentProjItem.project.getId() : (projects.isEmpty() ? null : projects.get(0).getId());

            Frame parent = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            TaskDialog dialog = new TaskDialog(parent, null, projects, allTasks, users, defaultProjId);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                Task newTask = dialog.getTask();
                taskService.createTask(newTask, currentUser);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Task created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (UnauthorizedAccessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            AppLogger.error("Failed to create task", ex);
            JOptionPane.showMessageDialog(this, "Error creating task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTransitionStatus() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task from the table to transition.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableAdapter.getTaskAt(selectedRow);
        TaskStatus currentStatus = task.getStatus();

        // Status selection options
        TaskStatus[] allStatuses = TaskStatus.values();
        TaskStatus chosenStatus = (TaskStatus) JOptionPane.showInputDialog(
                this,
                String.format("Current Status: %s\nSelect target status for Task #%d '%s':",
                        currentStatus, task.getId(), task.getTitle()),
                "State Transition",
                JOptionPane.QUESTION_MESSAGE,
                null,
                allStatuses,
                currentStatus);

        if (chosenStatus != null && chosenStatus != currentStatus) {
            try {
                // Throws InvalidTaskStateException if transition violates state rules!
                taskService.updateTaskStatus(task.getId(), chosenStatus, currentUser);
                fireStatusChangeEvent(task, currentStatus, chosenStatus);
                refreshAllData();
                JOptionPane.showMessageDialog(this,
                        String.format("Status successfully transitioned to: %s", chosenStatus.getLabel()),
                        "Transition Succeeded", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidTaskStateException ex) {
                // Catch custom exception and display clean dialog explaining rule
                JOptionPane.showMessageDialog(this,
                        "State Machine Violation:\n" + ex.getMessage(),
                        "Illegal Transition", JOptionPane.ERROR_MESSAGE);
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                AppLogger.error("Error transitioning task status", ex);
                JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLogHours() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to log hours against.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableAdapter.getTaskAt(selectedRow);
        Frame parent = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        LogHoursDialog dialog = new LogHoursDialog(parent, task);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                // Atomically logs hours in database transaction
                taskService.logHours(task.getId(), dialog.getHoursLogged(), currentUser);
                refreshAllData();
                JOptionPane.showMessageDialog(this,
                        String.format("Logged %.2f hours on Task #%d successfully!",
                                dialog.getHoursLogged(), task.getId()),
                        "Hours Recorded", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                AppLogger.error("Failed to log hours", ex);
                JOptionPane.showMessageDialog(this, "Error logging hours: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableAdapter.getTaskAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Are you sure you want to delete Task #%d '%s'?", task.getId(), task.getTitle()),
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskService.deleteTask(task.getId(), currentUser);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Task deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                AppLogger.error("Error deleting task", ex);
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper item classes
    private static class ProjectComboItem {
        final Project project;
        final String label;
        ProjectComboItem(Project project, String label) { this.project = project; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class TaskTreeNode {
        final Task task;
        final String label;
        TaskTreeNode(Task task, String label) { this.task = task; this.label = label; }
        @Override public String toString() { return label; }
    }
}
