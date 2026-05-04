package com.notegraph.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A styled confirmation dialog matching the React ConfirmModal component.
 */
public class ConfirmDialog {

    public static boolean show(Component parent, String title, String message) {
        // Use a custom JOptionPane for better styling
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.BG_SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JLabel msgLabel = new JLabel("<html><body style='width: 280px; font-size: 13px; color: #6B7280;'>" 
                + message + "</body></html>");
        msgLabel.setFont(ColorScheme.FONT_REGULAR);
        panel.add(msgLabel);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.OK_OPTION;
    }
}
