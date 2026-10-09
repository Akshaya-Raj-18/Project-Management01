package com.pms.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import com.pms.model.ProjectStatus;
import com.pms.model.User;

/**
 * Modal Dialog for Project Creation and Modification.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Wrapper types (Double parsing), Arrays (tags processing), Strings.
 * - Module 2: Anonymous inner classes, Method overriding.
 * - Module 4: Swing controls (JDialog, JTextField, JTextArea, JComboBox, JScrollPane),
 *   GridBagLayout, BorderLayout, Delegation Event model.
 */
public class ProjectDialog extends JDialog {

    private final Project projectToEdit;
    private final User currentUser;
    private boolean saved = false;

    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JTextField txtClient;
    private JTextField txtBudget;
    private JComboBox<ProjectStatus> cmbStatus;
    private JTextField txtTags;

    public ProjectDialog(Frame parent, Project projectToEdit, User currentUser) {
        super(parent, projectToEdit == null ? "Create New Project" : "Edit Project #" + projectToEdit.getId(), true);
        this.projectToEdit = projectToEdit;
        this.currentUser = currentUser;

        initComponents();
        setSize(520, 480);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG_LIGHT);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.COLOR_CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Project Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Project Title: *"), gbc);

        gbc.gridx = 1;
        txtTitle = new JTextField(20);
        txtTitle.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtTitle, gbc);

        // Client Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Client Name:"), gbc);

        gbc.gridx = 1;
        txtClient = new JTextField(20);
        txtClient.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtClient, gbc);

        // Budget
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Budget ($):"), gbc);

        gbc.gridx = 1;
        txtBudget = new JTextField("50000.0", 20);
        txtBudget.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtBudget, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Project Status:"), gbc);

        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(ProjectStatus.values());
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        formPanel.add(cmbStatus, gbc);

        // Tags
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Tags (comma separated):"), gbc);

        gbc.gridx = 1;
        txtTags = new JTextField(20);
        txtTags.setFont(UITheme.FONT_REGULAR);
        formPanel.add(txtTags, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 5;
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

        // If editing, pre-fill values
        if (projectToEdit != null) {
            txtTitle.setText(projectToEdit.getTitle());
            txtClient.setText(projectToEdit.getClientName());
            txtBudget.setText(String.valueOf(projectToEdit.getBudget()));
            cmbStatus.setSelectedItem(projectToEdit.getStatus());
            txtTags.setText(projectToEdit.getTagsFormatted());
            txtDescription.setText(projectToEdit.getDescription());
        }

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(UITheme.COLOR_BG_LIGHT);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton btnSave = UITheme.createPrimaryButton("Save Project");
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
            JOptionPane.showMessageDialog(this, "Project Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double budget = 0.0;
        try {
            // Module 1: Wrapper Class Double parsing
            Double budgetWrapper = Double.valueOf(txtBudget.getText().trim());
            budget = budgetWrapper.doubleValue();
            if (budget < 0) {
                throw new NumberFormatException("Negative budget");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric budget.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String client = txtClient.getText().trim();
        ProjectStatus status = (ProjectStatus) cmbStatus.getSelectedItem();
        String description = txtDescription.getText().trim();
        
        // Parse tags array
        String[] tags = txtTags.getText().trim().isEmpty() ? 
                new String[0] : txtTags.getText().trim().split(",\\s*");

        if (projectToEdit == null) {
            // New project
            Project newProject = new Project(null, title, description, budget, status,
                    currentUser != null ? currentUser.getId() : null,
                    client.isEmpty() ? "Internal" : client, tags);
            this.saved = true;
            this.createdProject = newProject;
        } else {
            // Edit existing
            projectToEdit.setTitle(title);
            projectToEdit.setDescription(description);
            projectToEdit.setClientName(client.isEmpty() ? "Internal" : client);
            projectToEdit.setBudget(budget);
            projectToEdit.setStatus(status);
            projectToEdit.setTags(tags);
            this.saved = true;
        }
        dispose();
    }

    private Project createdProject;

    public boolean isSaved() {
        return saved;
    }

    public Project getProject() {
        return projectToEdit != null ? projectToEdit : createdProject;
    }
}
