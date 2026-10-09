package com.pms.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.pms.gui.theme.UITheme;
import com.pms.model.Project;
import com.pms.model.Task;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;
import com.pms.model.User;

/**
 * Modal Dialog for Creating and Editing Tasks and Hierarchical Sub-tasks.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Wrapper classes (Double, Integer), Strings, Primitive casting.
 * - Module 2: Static nested classes / Helper wrappers, Dynamic use of 'this'.
 * - Module 4: Swing controls (JComboBox, JTextArea, JScrollPane), GridBagLayout, Event Delegation.
 */
public class TaskDialog extends JDialog {

    private final Task taskToEdit;
    private final List<Project> availableProjects;
    private final List<Task> availableParentTasks;
    private final List<User> availableUsers;
    private boolean saved = false;

    private JComboBox<ProjectItem> cmbProject;
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<TaskPriority> cmbPriority;
    private JComboBox<TaskStatus> cmbStatus;
    private JComboBox<TaskItem> cmbParentTask;
    private JComboBox<UserItem> cmbAssignee;
    private JTextField txtEstHours;

    public TaskDialog(Frame parent, Task taskToEdit, List<Project> projects,
                      List<Task> potentialParents, List<User> users, Integer defaultProjectId) {
        super(parent, taskToEdit == null ? "Create New Task / Sub-task" : "Edit Task #" + taskToEdit.getId(), true);
        this.taskToEdit = taskToEdit;
        this.availableProjects = projects;
        this.availableParentTasks = potentialParents;
        this.availableUsers = users;

        initComponents(defaultProjectId);
        setSize(540, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents(Integer defaultProjectId) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG_LIGHT);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.COLOR_CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Project Combo
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Project: *"), gbc);

        gbc.gridx = 1;
        cmbProject = new JComboBox<>();
        cmbProject.setFont(UITheme.FONT_REGULAR);
        for (Project p : availableProjects) {
            ProjectItem item = new ProjectItem(p);
            cmbProject.addItem(item);
            if (defaultProjectId != null && p.getId().equals(defaultProjectId)) {
                cmbProject.setSelectedItem(item);
            }
        }
        formPanel.add(cmbProject, gbc);

        // Parent Task (For Hierarchical Sub-tasks)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Parent Task (Optional):"), gbc);

        gbc.gridx = 1;
        cmbParentTask = new JComboBox<>();
        cmbParentTask.setFont(UITheme.FONT_REGULAR);
        cmbParentTask.addItem(new TaskItem(null, "-- None (Top-Level Task) --"));
        for (Task t : availableParentTasks) {
            if (taskToEdit == null || !t.getId().equals(taskToEdit.getId())) {
                cmbParentTask.addItem(new TaskItem(t.getId(), "#" + t.getId() + ": " + t.getTitle()));
            }
        }
        formPanel.add(cmbParentTask, gbc);

        // Task Title
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Task Title: *"), gbc);

        gbc.gridx = 1;
        txtTitle = new JTextField(20);
        txtTitle.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtTitle, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 1;
        cmbPriority = new JComboBox<>(TaskPriority.values());
        cmbPriority.setFont(UITheme.FONT_REGULAR);
        cmbPriority.setSelectedItem(TaskPriority.MEDIUM);
        formPanel.add(cmbPriority, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(TaskStatus.values());
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        formPanel.add(cmbStatus, gbc);

        // Assignee
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Assigned Developer:"), gbc);

        gbc.gridx = 1;
        cmbAssignee = new JComboBox<>();
        cmbAssignee.setFont(UITheme.FONT_REGULAR);
        cmbAssignee.addItem(new UserItem(null, "-- Unassigned --"));
        for (User u : availableUsers) {
            cmbAssignee.addItem(new UserItem(u.getId(), u.getFullName() + " (" + u.getUsername() + ")"));
        }
        formPanel.add(cmbAssignee, gbc);

        // Estimated Hours
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Estimated Hours:"), gbc);

        gbc.gridx = 1;
        txtEstHours = new JTextField("10.0", 20);
        txtEstHours.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtEstHours, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(UITheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);

        add(formPanel, BorderLayout.CENTER);

        // If editing, preload data
        if (taskToEdit != null) {
            txtTitle.setText(taskToEdit.getTitle());
            txtDescription.setText(taskToEdit.getDescription());
            cmbPriority.setSelectedItem(taskToEdit.getPriority());
            cmbStatus.setSelectedItem(taskToEdit.getStatus());
            txtEstHours.setText(String.valueOf(taskToEdit.getEstimatedHours()));

            // Select project
            for (int i = 0; i < cmbProject.getItemCount(); i++) {
                if (cmbProject.getItemAt(i).project.getId().equals(taskToEdit.getProjectId())) {
                    cmbProject.setSelectedIndex(i);
                    break;
                }
            }

            // Select parent task
            if (taskToEdit.getParentTaskId() != null) {
                for (int i = 0; i < cmbParentTask.getItemCount(); i++) {
                    if (taskToEdit.getParentTaskId().equals(cmbParentTask.getItemAt(i).taskId)) {
                        cmbParentTask.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Select assignee
            if (taskToEdit.getAssignedUserId() != null) {
                for (int i = 0; i < cmbAssignee.getItemCount(); i++) {
                    if (taskToEdit.getAssignedUserId().equals(cmbAssignee.getItemAt(i).userId)) {
                        cmbAssignee.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(UITheme.COLOR_BG_LIGHT);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton btnSave = UITheme.createPrimaryButton("Save Task");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProjectItem projItem = (ProjectItem) cmbProject.getSelectedItem();
        if (projItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a Project.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double estHours = 0.0;
        try {
            // Module 1: Wrapper Class Double parsing
            estHours = Double.parseDouble(txtEstHours.getText().trim());
            if (estHours < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric estimated hours.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaskItem parentItem = (TaskItem) cmbParentTask.getSelectedItem();
        Integer parentId = (parentItem != null) ? parentItem.taskId : null;

        UserItem userItem = (UserItem) cmbAssignee.getSelectedItem();
        Integer assigneeId = (userItem != null) ? userItem.userId : null;

        TaskPriority priority = (TaskPriority) cmbPriority.getSelectedItem();
        TaskStatus status = (TaskStatus) cmbStatus.getSelectedItem();
        String desc = txtDescription.getText().trim();

        if (taskToEdit == null) {
            // Module 2: Using Task.Builder pattern
            this.createdTask = new Task.Builder(projItem.project.getId(), title)
                    .description(desc)
                    .assignedUser(assigneeId)
                    .parentTask(parentId)
                    .priority(priority)
                    .status(status)
                    .estimatedHours(estHours)
                    .loggedHours(0.0)
                    .build();
            this.saved = true;
        } else {
            taskToEdit.setProjectId(projItem.project.getId());
            taskToEdit.setTitle(title);
            taskToEdit.setDescription(desc);
            taskToEdit.setPriority(priority);
            taskToEdit.setStatus(status);
            taskToEdit.setParentTaskId(parentId);
            taskToEdit.setAssignedUserId(assigneeId);
            taskToEdit.setEstimatedHours(estHours);
            this.saved = true;
        }
        dispose();
    }

    private Task createdTask;

    public boolean isSaved() {
        return saved;
    }

    public Task getTask() {
        return taskToEdit != null ? taskToEdit : createdTask;
    }

    // Helper wrapper classes for ComboBox rendering
    private static class ProjectItem {
        final Project project;
        ProjectItem(Project project) { this.project = project; }
        @Override public String toString() { return "#" + project.getId() + " - " + project.getTitle(); }
    }

    private static class TaskItem {
        final Integer taskId;
        final String label;
        TaskItem(Integer taskId, String label) { this.taskId = taskId; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class UserItem {
        final Integer userId;
        final String label;
        UserItem(Integer userId, String label) { this.userId = userId; this.label = label; }
        @Override public String toString() { return label; }
    }
}
