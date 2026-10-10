package com.pms.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import com.pms.gui.theme.UITheme;
import com.pms.model.Project;
import com.pms.model.Task;
import com.pms.model.TaskStatus;
import com.pms.service.IProjectService;
import com.pms.service.ITaskService;
import com.pms.util.AppLogger;

/**
 * Interactive Analytics & Executive Overview Dashboard.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Primitive casting, Wrapper types, Strings.
 * - Module 2: Recursion integration (renders project completion percentages calculated recursively).
 * - Module 4: Swing Layout Managers (BorderLayout, GridLayout, GridBagLayout, BoxLayout),
 *   Swing Controls (JProgressBar, JLabel, JScrollPane, JButton).
 */
public class DashboardPanel extends JPanel {

    private final IProjectService projectService;
    private final ITaskService taskService;

    private JLabel lblTotalProjects;
    private JLabel lblTotalTasks;
    private JLabel lblCompletedTasks;
    private JLabel lblTotalHours;
    private JPanel projectProgressContainer;

    public DashboardPanel(IProjectService projectService, ITaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;

        initComponents();
        refreshMetrics();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Overview & Real-Time Analytics");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MAIN);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Dashboard");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshMetrics();
            }
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Cards on top, Project Progress on bottom
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Top Metrics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsGrid.setOpaque(false);

        lblTotalProjects = new JLabel("0", SwingConstants.CENTER);
        lblTotalTasks = new JLabel("0", SwingConstants.CENTER);
        lblCompletedTasks = new JLabel("0", SwingConstants.CENTER);
        lblTotalHours = new JLabel("0.0", SwingConstants.CENTER);

        metricsGrid.add(createMetricCard("TOTAL PROJECTS", lblTotalProjects, UITheme.COLOR_PRIMARY));
        metricsGrid.add(createMetricCard("TOTAL TASKS", lblTotalTasks, UITheme.COLOR_INFO));
        metricsGrid.add(createMetricCard("COMPLETED TASKS", lblCompletedTasks, UITheme.COLOR_SUCCESS));
        metricsGrid.add(createMetricCard("HOURS LOGGED", lblTotalHours, UITheme.COLOR_PURPLE));

        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Projects Progress Panel
        JPanel progressSection = UITheme.createCardPanel();
        progressSection.setLayout(new BorderLayout(0, 10));

        JLabel lblSectionTitle = new JLabel("Project Recursive Progress Tracking");
        lblSectionTitle.setFont(UITheme.FONT_SUBTITLE);
        lblSectionTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        progressSection.add(lblSectionTitle, BorderLayout.NORTH);

        projectProgressContainer = new JPanel();
        projectProgressContainer.setLayout(new BoxLayout(projectProgressContainer, BoxLayout.Y_AXIS));
        projectProgressContainer.setOpaque(false);

        JScrollPane scrollProjects = new JScrollPane(projectProgressContainer);
        scrollProjects.setBorder(BorderFactory.createEmptyBorder());
        scrollProjects.setOpaque(false);
        scrollProjects.getViewport().setOpaque(false);
        progressSection.add(scrollProjects, BorderLayout.CENTER);

        centerPanel.add(progressSection, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes metrics dynamically via database services and recursive calculations.
     */
    public void refreshMetrics() {
        try {
            long totalProjects = projectService.getTotalProjectCount();
            long totalTasks = taskService.getTotalTaskCount();
            long completedTasks = taskService.getCompletedTaskCount();

            List<Task> allTasks = taskService.getAllTasks();
            double totalHours = 0.0;
            for (Task t : allTasks) {
                totalHours += t.getLoggedHours();
            }

            lblTotalProjects.setText(String.valueOf(totalProjects));
            lblTotalTasks.setText(String.valueOf(totalTasks));
            lblCompletedTasks.setText(String.valueOf(completedTasks));
            lblTotalHours.setText(String.format("%.1f hrs", totalHours));

            // Refresh Project Progress Rows
            projectProgressContainer.removeAll();
            List<Project> projects = projectService.getAllProjects();

            for (Project project : projects) {
                // Module 2: Recursively calculated project completion percentage!
                double completion = projectService.calculateProjectCompletion(project.getId());

                JPanel row = new JPanel(new BorderLayout(15, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

                JLabel lblName = new JLabel(String.format("<html><b>#%d: %s</b> <font color='#64748B'>[%s]</font></html>",
                        project.getId(), project.getTitle(), project.getStatus()));
                lblName.setPreferredSize(new Dimension(280, 24));
                lblName.setFont(UITheme.FONT_REGULAR);

                JProgressBar bar = new JProgressBar(0, 100);
                // Module 1: Primitive integer casting
                bar.setValue((int) Math.round(completion));
                bar.setStringPainted(true);
                bar.setString(String.format("%.1f%%", completion));
                bar.setPreferredSize(new Dimension(200, 20));

                if (completion >= 100.0) {
                    bar.setForeground(UITheme.COLOR_SUCCESS);
                } else if (completion >= 50.0) {
                    bar.setForeground(UITheme.COLOR_PRIMARY);
                } else {
                    bar.setForeground(UITheme.COLOR_WARNING);
                }

                row.add(lblName, BorderLayout.WEST);
                row.add(bar, BorderLayout.CENTER);

                projectProgressContainer.add(row);
            }

            projectProgressContainer.revalidate();
            projectProgressContainer.repaint();
        } catch (Exception e) {
            AppLogger.error("Failed to refresh dashboard metrics", e);
        }
    }
}
