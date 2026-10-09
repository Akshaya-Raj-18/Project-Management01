package com.pms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.pms.dao.DatabaseConnectionManager;
import com.pms.dao.IProjectDao;
import com.pms.dao.ITaskDao;
import com.pms.dao.IUserDao;
import com.pms.gui.dialog.LoginDialog;
import com.pms.gui.event.TaskStatusChangeEvent;
import com.pms.gui.event.TaskStatusChangeListener;
import com.pms.gui.panel.DashboardPanel;
import com.pms.gui.panel.ProjectManagementPanel;
import com.pms.gui.panel.TaskManagementPanel;
import com.pms.gui.panel.TeamManagementPanel;
import com.pms.gui.theme.UITheme;
import com.pms.model.User;
import com.pms.service.IAuthService;
import com.pms.service.IProjectService;
import com.pms.service.ITaskService;
import com.pms.util.AppLogger;

/**
 * Main Enterprise Application Window (JFrame).
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Strings, Control statements, Wrapper classes.
 * - Module 2: Anonymous inner classes (WindowAdapter, ActionListeners, ChangeListeners),
 *   Dynamic Method Dispatch (user.getProfileBadgeText()), Object references.
 * - Module 4: Swing controls (JFrame, JTabbedPane, JToolBar, JLabel, JButton),
 *   Dynamic Layout Managers (BorderLayout, FlowLayout), Event Delegation Model,
 *   Custom Event Handling (TaskStatusChangeListener).
 */
public class MainFrame extends JFrame {

    private final IAuthService authService;
    private final IProjectService projectService;
    private final ITaskService taskService;
    private final IUserDao userDao;

    private User currentUser;

    // UI Components
    private JLabel lblUserBadge;
    private JLabel lblStatusLeft;
    private JLabel lblStatusRight;
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private ProjectManagementPanel projectPanel;
    private TaskManagementPanel taskPanel;
    private TeamManagementPanel teamPanel;

    public MainFrame(IAuthService authService, IProjectService projectService,
                     ITaskService taskService, IUserDao userDao, User initialUser) {
        super("Project Management System - KTU PBCST304 Enterprise Architecture");
        this.authService = authService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.userDao = userDao;
        this.currentUser = initialUser;

        initComponents();
        setupEventWiring();

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG_LIGHT);

        // 1. Top Enterprise Header Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_PRIMARY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Brand Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("🚀 PMS");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("APJ Abdul Kalam Technological University | OOP Enterprise Project");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(new Color(224, 231, 255));

        brandPanel.add(lblLogo);
        brandPanel.add(lblSub);
        topBar.add(brandPanel, BorderLayout.WEST);

        // User Profile & Switch Button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        // Module 2: Dynamic Method Dispatch in action via getProfileBadgeText()
        lblUserBadge = new JLabel(currentUser != null ? currentUser.getProfileBadgeText() : "Guest");
        lblUserBadge.setFont(UITheme.FONT_BOLD);
        lblUserBadge.setForeground(Color.WHITE);

        JButton btnSwitchUser = new JButton("Switch Account");
        btnSwitchUser.setFont(UITheme.FONT_SMALL);
        btnSwitchUser.setBackground(Color.WHITE);
        btnSwitchUser.setForeground(UITheme.COLOR_PRIMARY);
        btnSwitchUser.setFocusPainted(false);
        btnSwitchUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUserSwitch();
            }
        });

        userPanel.add(lblUserBadge);
        userPanel.add(btnSwitchUser);
        topBar.add(userPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 2. Center Tabbed Navigation
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.COLOR_BG_LIGHT);

        // Instantiate Panels
        dashboardPanel = new DashboardPanel(projectService, taskService);
        projectPanel = new ProjectManagementPanel(projectService, currentUser, projectId -> {
            // Navigate to Task Panel and select project
            tabbedPane.setSelectedIndex(2);
            taskPanel.selectProject(projectId);
        });
        taskPanel = new TaskManagementPanel(taskService, projectService, userDao, currentUser);
        teamPanel = new TeamManagementPanel(userDao, currentUser, user -> {
            // Switch session to user selected in table
            switchSessionUser(user);
        });

        tabbedPane.addTab("📊 Dashboard & Analytics", dashboardPanel);
        tabbedPane.addTab("📁 Project Management", projectPanel);
        tabbedPane.addTab("📌 Tasks & Subtask WBS", taskPanel);
        tabbedPane.addTab("👥 Team & Permissions", teamPanel);

        // Tab change listener to auto-refresh data
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == 0) {
                    dashboardPanel.refreshMetrics();
                } else if (index == 1) {
                    projectPanel.loadProjects();
                } else if (index == 2) {
                    taskPanel.refreshAllData();
                } else if (index == 3) {
                    teamPanel.loadUsers();
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        lblStatusLeft = new JLabel("● Database: SQLite Embedded (JDBC Active) | Foreign Keys: Enforced");
        lblStatusLeft.setFont(UITheme.FONT_SMALL);
        lblStatusLeft.setForeground(UITheme.COLOR_SUCCESS);

        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        lblStatusRight = new JLabel("Role: " + (currentUser != null ? currentUser.getRole() : "None") + " | " + timeStr);
        lblStatusRight.setFont(UITheme.FONT_SMALL);
        lblStatusRight.setForeground(UITheme.COLOR_TEXT_MUTED);

        statusBar.add(lblStatusLeft, BorderLayout.WEST);
        statusBar.add(lblStatusRight, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupEventWiring() {
        // Module 4: Custom Event Listener Integration
        // When a task status changes in TaskManagementPanel, notify MainFrame to update Dashboard
        taskPanel.addStatusChangeListener(new TaskStatusChangeListener() {
            @Override
            public void onTaskStatusChanged(TaskStatusChangeEvent event) {
                AppLogger.info("MainFrame received TaskStatusChangeEvent for Task #%d: %s -> %s",
                        event.getTask().getId(), event.getOldStatus(), event.getNewStatus());
                dashboardPanel.refreshMetrics();
                projectPanel.loadProjects();
            }
        });

        // Module 2: Anonymous Inner Class for Window Closing Event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AppLogger.info("Application shutting down cleanly. Releasing database locks...");
                DatabaseConnectionManager.getInstance().closeConnection();
                System.exit(0);
            }
        });
    }

    /**
     * Dynamically switches active user session and updates all UI sub-panels.
     */
    public void switchSessionUser(User newUser) {
        if (newUser == null) return;
        this.currentUser = newUser;
        authService.setCurrentUser(newUser);

        // Update top badge using dynamic method dispatch
        lblUserBadge.setText(currentUser.getProfileBadgeText());

        // Update status bar
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        lblStatusRight.setText("Role: " + currentUser.getRole() + " | " + timeStr);

        // Update subpanels
        projectPanel.setCurrentUser(newUser);
        taskPanel.setCurrentUser(newUser);
        teamPanel.setCurrentUser(newUser);

        AppLogger.info("Session switched to: %s [%s]", newUser.getFullName(), newUser.getRole());
    }

    private void handleUserSwitch() {
        LoginDialog dialog = new LoginDialog(this, authService);
        dialog.setVisible(true);

        if (dialog.isAuthenticated()) {
            User newUser = authService.getCurrentUser();
            switchSessionUser(newUser);
        }
    }
}
