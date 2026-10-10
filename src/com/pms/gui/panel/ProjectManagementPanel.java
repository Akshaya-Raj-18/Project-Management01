package com.pms.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import com.pms.exception.UnauthorizedAccessException;
import com.pms.gui.adapter.ProjectTableModelAdapter;
import com.pms.gui.dialog.ProjectDialog;
import com.pms.gui.theme.UITheme;
import com.pms.model.Permission;
import com.pms.model.Project;
import com.pms.model.ProjectStatus;
import com.pms.model.User;
import com.pms.service.IProjectService;
import com.pms.util.AppLogger;

/**
 * Project Management Panel providing search, creation, editing, and soft-delete operations.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Anonymous inner classes (ActionListeners, MouseAdapter, KeyAdapter).
 * - Module 3: Adapter Pattern (ProjectTableModelAdapter), Exception Handling
 *   (try-catch for UnauthorizedAccessException).
 * - Module 4: Swing Controls (JTable, JScrollPane, JComboBox, JTextField), Layout Managers.
 */
public class ProjectManagementPanel extends JPanel {

    private final IProjectService projectService;
    private User currentUser;
    private final Consumer<Integer> onSelectProjectForTasks;

    private JTable projectTable;
    private ProjectTableModelAdapter tableModelAdapter;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JButton btnNewProject;
    private JButton btnEditProject;
    private JButton btnDeleteProject;
    private JButton btnViewTasks;

    public ProjectManagementPanel(IProjectService projectService, User currentUser, Consumer<Integer> onSelectProjectForTasks) {
        this.projectService = projectService;
        this.currentUser = currentUser;
        this.onSelectProjectForTasks = onSelectProjectForTasks;

        initComponents();
        loadProjects();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateButtonPermissions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Toolbar & Search Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Search and Filter controls (Left)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(16);
        txtSearch.setFont(UITheme.FONT_REGULAR);
        // Key listener for quick searching
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applySearch();
            }
        });
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "PLANNING", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "CANCELLED"});
        cmbStatusFilter.setFont(UITheme.FONT_REGULAR);
        cmbStatusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySearch();
            }
        });
        filterPanel.add(cmbStatusFilter);

        topPanel.add(filterPanel, BorderLayout.WEST);

        // Action Buttons (Right)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnNewProject = UITheme.createPrimaryButton("+ New Project");
        btnNewProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewProject();
            }
        });

        btnEditProject = UITheme.createSecondaryButton("Edit");
        btnEditProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEditProject();
            }
        });

        btnViewTasks = UITheme.createSecondaryButton("View Tasks");
        btnViewTasks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleViewTasks();
            }
        });

        btnDeleteProject = UITheme.createDangerButton("Delete");
        btnDeleteProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteProject();
            }
        });

        actionPanel.add(btnNewProject);
        actionPanel.add(btnEditProject);
        actionPanel.add(btnViewTasks);
        actionPanel.add(btnDeleteProject);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Table with Adapter Pattern
        tableModelAdapter = new ProjectTableModelAdapter();
        projectTable = new JTable(tableModelAdapter);
        projectTable.setRowHeight(32);
        projectTable.setFont(UITheme.FONT_REGULAR);
        projectTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Mouse double click to edit
        projectTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleEditProject();
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(projectTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));
        add(scrollTable, BorderLayout.CENTER);

        updateButtonPermissions();
    }

    private void updateButtonPermissions() {
        if (currentUser != null) {
            boolean canCreate = currentUser.hasPermission(Permission.CREATE_PROJECT);
            boolean canDelete = currentUser.hasPermission(Permission.DELETE_PROJECT);
            btnNewProject.setEnabled(canCreate);
            btnDeleteProject.setEnabled(canDelete);
        }
    }

    public void loadProjects() {
        try {
            List<Project> list = projectService.getAllProjects();
            tableModelAdapter.setProjects(list);
        } catch (Exception e) {
            AppLogger.error("Failed to load projects", e);
        }
    }

    private void applySearch() {
        try {
            String query = txtSearch.getText().trim();
            String statusFilter = (String) cmbStatusFilter.getSelectedItem();

            List<Project> results = projectService.searchProjects(query);
            if (statusFilter != null && !"ALL".equals(statusFilter)) {
                ProjectStatus status = ProjectStatus.valueOf(statusFilter);
                results.removeIf(p -> p.getStatus() != status);
            }
            tableModelAdapter.setProjects(results);
        } catch (Exception e) {
            AppLogger.error("Error during project search", e);
        }
    }

    private void handleNewProject() {
        try {
            if (!currentUser.hasPermission(Permission.CREATE_PROJECT)) {
                throw new UnauthorizedAccessException(currentUser.getUsername(), "Create Project");
            }

            Frame parentFrame = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ProjectDialog dialog = new ProjectDialog(parentFrame, null, currentUser);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                Project newProj = dialog.getProject();
                projectService.createProject(newProj, currentUser);
                loadProjects();
                JOptionPane.showMessageDialog(this,
                        "Project '" + newProj.getTitle() + "' created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (UnauthorizedAccessException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\n(Software Developers have view-only rights for projects)",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            AppLogger.error("Error saving new project", ex);
            JOptionPane.showMessageDialog(this, "Failed to create project: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a project to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!currentUser.hasPermission(Permission.EDIT_PROJECT)) {
                throw new UnauthorizedAccessException(currentUser.getUsername(), "Edit Project");
            }

            Project project = tableModelAdapter.getProjectAt(selectedRow);
            Frame parentFrame = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ProjectDialog dialog = new ProjectDialog(parentFrame, project, currentUser);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                projectService.updateProject(project, currentUser);
                loadProjects();
                JOptionPane.showMessageDialog(this, "Project updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (UnauthorizedAccessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            AppLogger.error("Error editing project", ex);
            JOptionPane.showMessageDialog(this, "Failed to update project: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a project to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Project project = tableModelAdapter.getProjectAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Are you sure you want to soft-delete project #%d '%s'?",
                        project.getId(), project.getTitle()),
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Throws UnauthorizedAccessException if Developer attempts this
                projectService.deleteProject(project.getId(), currentUser);
                loadProjects();
                JOptionPane.showMessageDialog(this, "Project soft-deleted successfully.",
                        "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                AppLogger.error("Error deleting project", ex);
                JOptionPane.showMessageDialog(this, "Failed to delete project: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewTasks() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow >= 0 && onSelectProjectForTasks != null) {
            Project project = tableModelAdapter.getProjectAt(selectedRow);
            onSelectProjectForTasks.accept(project.getId());
        } else {
            JOptionPane.showMessageDialog(this, "Please select a project first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
