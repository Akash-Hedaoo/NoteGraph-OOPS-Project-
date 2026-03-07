package com.notegraph.frontend;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Initialize the light theme globally BEFORE creating the UI
        com.formdev.flatlaf.FlatLightLaf.setup();
        javax.swing.UIManager.put("Component.accentColor", new java.awt.Color(255, 122, 89));
        javax.swing.UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));

        // Optional globally seamless tuning for FlatLaf
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("ScrollBar.showButtons", false);

        // Schedule a job for the event-dispatching thread
        // Creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            com.notegraph.frontend.ui.LoginFrame frame = new com.notegraph.frontend.ui.LoginFrame();
            frame.setVisible(true);
        });
    }
}
