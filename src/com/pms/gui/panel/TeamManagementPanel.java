package com.pms.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import com.pms.dao.IUserDao;
import com.pms.gui.theme.UITheme;
import com.pms.model.Developer;
import com.pms.model.Manager;
import com.pms.model.Permission;
import com.pms.model.User;
import com.pms.util.AppLogger;

/**
 * Team Management Panel displaying polymorphic User details and allowing active user switching.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Vectors (Vector<Vector<Object>> for DefaultTableModel), Wrapper types.
 * - Module 2: Inheritance (User -> Manager/Developer), Dynamic Method Dispatch
 *   (user.getRoleDescription()), Polymorphism.
 * - Module 4: Swing controls (JTable, JButton, JLabel), Layout Managers.
 */
public class TeamManagementPanel extends JPanel {

    private final IUserDao userDao;
    private User currentUser;
    private final Consumer<User> onUserSwitched;

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private List<User> userList;

    public TeamManagementPanel(IUserDao userDao, User currentUser, Consumer<User> onUserSwitched) {
        this.userDao = userDao;
        this.currentUser = currentUser;
        this.onUserSwitched = onUserSwitched;

        initComponents();
        loadUsers();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Team Roster & Role-Based Permissions (Polymorphic Model)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnSwitchUser = UITheme.createPrimaryButton("🔑 Switch Active Session to Selected User");
        btnSwitchUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSwitchUser();
            }
        });

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadUsers();
            }
        });

        actionPanel.add(btnRefresh);
        actionPanel.add(btnSwitchUser);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        // Module 1: Vector-based column headers
        Vector<String> columnHeaders = new Vector<>();
        columnHeaders.add("ID");
        columnHeaders.add("Full Name");
        columnHeaders.add("Username");
        columnHeaders.add("Role");
        columnHeaders.add("Department / Tech Stack");
        columnHeaders.add("Rate / Budget Limit");
        columnHeaders.add("Polymorphic Role Description");

        tableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usersTable = new JTable(tableModel);
        usersTable.setRowHeight(32);
        usersTable.setFont(UITheme.FONT_REGULAR);
        usersTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadUsers() {
        try {
            userList = userDao.findAll();
            // Module 1: Vector<Vector<Object>>
            Vector<Vector<Object>> data = new Vector<>();

            for (User u : userList) {
                Vector<Object> row = new Vector<>();
                row.add(u.getId());
                row.add(u.getFullName());
                row.add(u.getUsername());
                row.add(u.getRole().getDisplayName());

                // Dynamic method dispatch and instance checking
                if (u instanceof Manager) {
                    Manager m = (Manager) u;
                    row.add("Dept: " + m.getDepartment());
                    row.add(String.format("Limit: $%,.2f", m.getBudgetApprovalLimit()));
                } else if (u instanceof Developer) {
                    Developer d = (Developer) u;
                    row.add("Stack: " + d.getPrimaryTechStack());
                    row.add(String.format("$%.2f/hr (Max %dh/wk)", d.getHourlyBillingRate(), d.getMaxWeeklyHours()));
                } else {
                    row.add("-");
                    row.add("-");
                }

                // Module 2: Dynamic Method Dispatch in action!
                row.add(u.getRoleDescription());
                data.add(row);
            }

            tableModel.setDataVector(data, new Vector<>(List.of(
                    "ID", "Full Name", "Username", "Role", "Department / Tech Stack", "Rate / Budget Limit", "Polymorphic Role Description"
            )));
        } catch (Exception e) {
            AppLogger.error("Failed to load users", e);
        }
    }

    private void handleSwitchUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to switch the session to.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User selectedUser = userList.get(selectedRow);
        if (selectedUser != null && onUserSwitched != null) {
            onUserSwitched.accept(selectedUser);
            JOptionPane.showMessageDialog(this,
                    String.format("Active session switched to: %s\nRole: %s\nDescription: %s",
                            selectedUser.getFullName(), selectedUser.getRole(), selectedUser.getRoleDescription()),
                    "Session Switched", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
