package com.pms.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.pms.gui.theme.UITheme;
import com.pms.model.Task;

/**
 * Modal Dialog for Logging Work Hours against a Task.
 * (Module 4: Swing Controls and Event Delegation)
 */
public class LogHoursDialog extends JDialog {

    private final Task task;
    private double hoursLogged = 0.0;
    private boolean confirmed = false;

    private JTextField txtHours;

    public LogHoursDialog(Frame parent, Task task) {
        super(parent, "Log Progress Hours - Task #" + task.getId(), true);
        this.task = task;

        initComponents();
        setSize(440, 280);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_BG_LIGHT);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Task Title:"), gbc);

        gbc.gridx = 1;
        JLabel lblTitle = new JLabel("<html><b>" + task.getTitle() + "</b></html>");
        panel.add(lblTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Current Hours Logged:"), gbc);

        gbc.gridx = 1;
        JLabel lblCurrent = new JLabel(String.format("%.2f hrs (Est: %.2f hrs)",
                task.getLoggedHours(), task.getEstimatedHours()));
        panel.add(lblCurrent, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Additional Hours: *"), gbc);

        gbc.gridx = 1;
        txtHours = new JTextField("2.0", 10);
        txtHours.setFont(UITheme.FONT_REGULAR);
        panel.add(txtHours, gbc);

        // Quick add buttons panel
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        quickPanel.setOpaque(false);
        int[] increments = {1, 2, 4, 8};
        for (int inc : increments) {
            JButton btnInc = new JButton("+" + inc + "h");
            btnInc.setFont(UITheme.FONT_SMALL);
            btnInc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    txtHours.setText(String.valueOf((double) inc));
                }
            });
            quickPanel.add(btnInc);
        }
        panel.add(quickPanel, gbc);

        add(panel, BorderLayout.CENTER);

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

        JButton btnSubmit = UITheme.createPrimaryButton("Submit Hours");
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double hrs = Double.parseDouble(txtHours.getText().trim());
                    if (hrs <= 0) {
                        JOptionPane.showMessageDialog(LogHoursDialog.this,
                                "Hours must be greater than zero.",
                                "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    hoursLogged = hrs;
                    confirmed = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LogHoursDialog.this,
                            "Please enter a valid numeric number of hours.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        actionPanel.add(btnCancel);
        actionPanel.add(btnSubmit);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getHoursLogged() {
        return hoursLogged;
    }
}
