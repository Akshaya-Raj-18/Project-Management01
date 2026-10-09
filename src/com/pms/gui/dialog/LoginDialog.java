package com.pms.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import com.pms.gui.theme.UITheme;
import com.pms.model.User;
import com.pms.service.IAuthService;
import com.pms.util.AppLogger;

/**
 * Authentication Dialog supporting direct credential input or Quick Demo user profiles.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 1: Wrapper types, Arrays, Strings.
 * - Module 2: Anonymous inner classes (ActionListeners, KeyAdapter).
 * - Module 4: Swing controls (JDialog, JTextField, JPasswordField, JComboBox, JButton),
 *   Layout Managers (BorderLayout, GridBagLayout, FlowLayout), Delegation Event Model.
 */
public class LoginDialog extends JDialog {

    private final IAuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbQuickSelect;
    private boolean authenticated = false;

    // Quick demo credentials definition [Display, Username, Password]
    private static final String[][] DEMO_ACCOUNTS = {
            {"Select Demo Account...", "", ""},
            {"Dr. Arun Kumar (Manager) - admin", "admin", "admin123"},
            {"Alice George (Developer) - dev_alice", "dev_alice", "alice123"},
            {"Bob Varghese (Developer) - dev_bob", "dev_bob", "bob123"}
    };

    public LoginDialog(Frame parent, IAuthService authService) {
        super(parent, "KTU PBCST304 - Project Management System Login", true);
        this.authService = authService;

        initComponents();
        setSize(480, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG_LIGHT);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitle = new JLabel("Enterprise Project Management System");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("APJ Abdul Kalam Technological University (PBCST304)");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(new Color(224, 231, 255));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Center Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.COLOR_CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Quick Select Demo Combo
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Quick Demo User:"), gbc);

        gbc.gridx = 1;
        String[] demoLabels = new String[DEMO_ACCOUNTS.length];
        for (int i = 0; i < DEMO_ACCOUNTS.length; i++) {
            demoLabels[i] = DEMO_ACCOUNTS[i][0];
        }
        cmbQuickSelect = new JComboBox<>(demoLabels);
        cmbQuickSelect.setFont(UITheme.FONT_REGULAR);
        
        // Item listener delegation
        cmbQuickSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = cmbQuickSelect.getSelectedIndex();
                if (idx > 0 && idx < DEMO_ACCOUNTS.length) {
                    txtUsername.setText(DEMO_ACCOUNTS[idx][1]);
                    txtPassword.setText(DEMO_ACCOUNTS[idx][2]);
                }
            }
        });
        formPanel.add(cmbQuickSelect, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.setText("admin"); // Default
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setText("admin123");
        
        // Key listener for Enter key to trigger login
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        formPanel.add(txtPassword, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(UITheme.COLOR_BG_LIGHT);

        JButton btnExit = UITheme.createSecondaryButton("Exit Application");
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });

        JButton btnLogin = UITheme.createPrimaryButton("Sign In");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        actionPanel.add(btnExit);
        actionPanel.add(btnLogin);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Optional<User> userOpt = authService.authenticate(username, password);
            if (userOpt.isPresent()) {
                authenticated = true;
                User user = userOpt.get();
                AppLogger.info("LoginDialog: User '%s' successfully signed in.", user.getUsername());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials. Please verify your username and password.\nTip: You can use the Quick Demo dropdown.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
        } catch (Exception ex) {
            AppLogger.error("Error during authentication attempt", ex);
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
