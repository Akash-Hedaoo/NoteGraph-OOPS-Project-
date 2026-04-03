package com.notegraph.ui.layout;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.components.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top bar matching the React TopBar.jsx — search field + user avatar.
 */
public class TopBarPanel extends JPanel {

    private JTextField searchField;

    public TopBarPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_SURFACE);
        setPreferredSize(new Dimension(0, ColorScheme.TOPBAR_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));
        buildUI();
    }

    private void buildUI() {
        // Search 
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField = new JTextField(30);
        searchField.setFont(ColorScheme.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setBackground(ColorScheme.BG_APP);
        searchBox.add(searchIcon);
        searchBox.add(searchField);
        add(searchBox, BorderLayout.WEST);

        // User avatar
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        userArea.setOpaque(false);
        JLabel bellIcon = new JLabel("🔔");
        bellIcon.setFont(new Font("SansSerif", Font.PLAIN, 16));
        bellIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String name = ApiClient.get().getUser() != null ? ApiClient.get().getUser().name : "User";
        String initials = getInitials(name);
        JLabel avatar = new JLabel(initials);
        avatar.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setBackground(new Color(55, 65, 81));
        avatar.setOpaque(true);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        userArea.add(bellIcon);
        userArea.add(avatar);
        userArea.add(new JLabel(name));
        add(userArea, BorderLayout.EAST);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "US";
        String[] parts = name.split(" ");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
