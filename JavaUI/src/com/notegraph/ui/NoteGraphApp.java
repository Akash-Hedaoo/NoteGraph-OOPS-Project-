package com.notegraph.ui;

import com.notegraph.ui.auth.LoginPanel;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.layout.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * NoteGraph Desktop Client — Main entry point.
 * Sets up FlatLaf look-and-feel, creates the JFrame,
 * and manages transitions between Login and Dashboard views.
 */
public class NoteGraphApp {

    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private LoginPanel loginPanel;
    private MainFrame mainFrame;

    public NoteGraphApp() {
        frame = new JFrame("NoteGraph — Desktop Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 800);
        frame.setMinimumSize(new Dimension(960, 600));
        frame.setLocationRelativeTo(null);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);

        loginPanel = new LoginPanel(this::onLoginSuccess);
        mainFrame = new MainFrame(this::onLogout);

        rootPanel.add(loginPanel, "login");
        rootPanel.add(mainFrame, "main");

        frame.setContentPane(rootPanel);
        rootLayout.show(rootPanel, "login");
        frame.setVisible(true);
    }

    private void onLoginSuccess() {
        rootLayout.show(rootPanel, "main");
        mainFrame.initialLoad();
    }

    private void onLogout() {
        rootLayout.show(rootPanel, "login");
    }

    public static void main(String[] args) {
        // Try FlatLaf first, fallback to system L&F
        try {
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((LookAndFeel) flatLaf.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Fallback to default
            }
        }

        // Global UI tweaks
        UIManager.put("defaultFont", ColorScheme.FONT_REGULAR);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("TitlePane.unifiedBackground", true);

        SwingUtilities.invokeLater(NoteGraphApp::new);
    }
}
