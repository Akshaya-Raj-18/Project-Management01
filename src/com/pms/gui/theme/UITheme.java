package com.pms.gui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import com.pms.model.TaskPriority;
import com.pms.model.TaskStatus;
import com.pms.util.AppLogger;

/**
 * UI Theme & Design System styling constants and custom Swing renderers.
 * 
 * KTU OOP Syllabus Topics Demonstrated:
 * - Module 2: Static members, Anonymous inner classes (custom cell renderers).
 * - Module 4: Swing controls styling, custom painting, Delegation model.
 */
public final class UITheme {

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);     // Modern Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);    // Emerald Green
    public static final Color COLOR_WARNING = new Color(245, 158, 11);    // Amber
    public static final Color COLOR_DANGER = new Color(239, 68, 68);      // Red
    public static final Color COLOR_INFO = new Color(14, 165, 233);       // Cyan / Sky
    public static final Color COLOR_PURPLE = new Color(139, 92, 246);     // Purple

    public static final Color COLOR_BG_LIGHT = new Color(248, 250, 252);  // Soft slate bg
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);    // Dark slate
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);// Slate gray
    public static final Color COLOR_BORDER = new Color(226, 232, 240);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    private UITheme() {
        // Prevent instantiation
    }

    /**
     * Initializes Swing Look and Feel with system defaults.
     */
    public static void initializeLookAndFeel() {
        try {
            // Attempt System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            AppLogger.info("Initialized System Look and Feel: %s", UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            try {
                // Fallback to Nimbus if System fails
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                AppLogger.warn("Could not set custom Look and Feel, using standard Swing: %s", ex.getMessage());
            }
        }
    }

    /**
     * Creates an elevated card panel with clean border and background.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    /**
     * Creates a styled primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled secondary action button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled danger action button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Module 2: Anonymous Inner Class TableCellRenderer for Task Status badges.
     */
    public static DefaultTableCellRenderer createStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_BADGE);
                label.setOpaque(true);

                if (value instanceof TaskStatus) {
                    TaskStatus status = (TaskStatus) value;
                    switch (status) {
                        case COMPLETED:
                            label.setBackground(new Color(209, 250, 229)); // Light green
                            label.setForeground(new Color(6, 95, 70));     // Dark green
                            label.setText("COMPLETED");
                            break;
                        case REVIEW:
                            label.setBackground(new Color(243, 232, 255)); // Light purple
                            label.setForeground(new Color(107, 33, 168));   // Dark purple
                            label.setText("IN REVIEW");
                            break;
                        case IN_PROGRESS:
                            label.setBackground(new Color(254, 243, 199)); // Light amber
                            label.setForeground(new Color(146, 64, 14));   // Dark amber
                            label.setText("IN PROGRESS");
                            break;
                        case TODO:
                        default:
                            label.setBackground(new Color(224, 231, 255)); // Light indigo
                            label.setForeground(new Color(55, 48, 163));   // Dark indigo
                            label.setText("TO DO");
                            break;
                    }
                }
                return label;
            }
        };
    }

    /**
     * Module 2: Anonymous Inner Class TableCellRenderer for Priority badges.
     */
    public static DefaultTableCellRenderer createPriorityBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_BADGE);
                label.setOpaque(true);

                if (value instanceof TaskPriority) {
                    TaskPriority priority = (TaskPriority) value;
                    switch (priority) {
                        case CRITICAL:
                            label.setBackground(new Color(254, 226, 226)); // Soft red
                            label.setForeground(new Color(153, 27, 27));
                            break;
                        case HIGH:
                            label.setBackground(new Color(254, 243, 199)); // Soft amber
                            label.setForeground(new Color(146, 64, 14));
                            break;
                        case MEDIUM:
                            label.setBackground(new Color(224, 242, 254)); // Soft sky blue
                            label.setForeground(new Color(7, 89, 133));
                            break;
                        case LOW:
                        default:
                            label.setBackground(new Color(241, 245, 249)); // Soft slate
                            label.setForeground(new Color(71, 85, 105));
                            break;
                    }
                }
                return label;
            }
        };
    }

    /**
     * Module 2: Anonymous Inner Class TableCellRenderer for Progress Bar column.
     */
    public static DefaultTableCellRenderer createProgressCellRenderer() {
        final JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(FONT_SMALL);

        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                if (value instanceof Number) {
                    int val = ((Number) value).intValue();
                    progressBar.setValue(val);
                    progressBar.setString(val + "%");
                    if (val >= 100) {
                        progressBar.setForeground(COLOR_SUCCESS);
                    } else if (val >= 50) {
                        progressBar.setForeground(COLOR_PRIMARY);
                    } else {
                        progressBar.setForeground(COLOR_WARNING);
                    }
                    return progressBar;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
    }
}
