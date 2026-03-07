package com.notegraph.frontend.ui;

import com.notegraph.frontend.api.AuthClient;
import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("NoteGraph - Login");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);

        // Main frame background
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new GridBagLayout()); // Center the card in the frame

        // Central white card
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // Typography
        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.DARK_GRAY);
        cardPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 25, 5); // More space below subtitle
        JLabel subtitleLabel = new JLabel("Sign in to access your NoteGraph", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        cardPanel.add(subtitleLabel, gbc);

        // Inputs
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(Color.DARK_GRAY);
        cardPanel.add(userLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 15, 5);
        JTextField usernameField = new JTextField(20);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        usernameField.setPreferredSize(new Dimension(300, 40));
        cardPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(Color.DARK_GRAY);
        cardPanel.add(passLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 25, 5);
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        passwordField.setPreferredSize(new Dimension(300, 40));
        cardPanel.add(passwordField, gbc);

        // Remember Me Checkbox
        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 15, 5);
        JCheckBox rememberMeBox = new JCheckBox("Remember Me");
        rememberMeBox.setBackground(Color.WHITE);
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeBox.setFocusPainted(false);
        cardPanel.add(rememberMeBox, gbc);

        // Auto-fill logic
        String savedUser = prefs.get("saved_username", "");
        String savedPass = prefs.get("saved_password", "");
        boolean remember = prefs.getBoolean("remember_me", false);

        if (remember) {
            rememberMeBox.setSelected(true);
            usernameField.setText(savedUser);
            passwordField.setText(savedPass);
        }

        // Primary Button
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 15, 5);
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setBackground(new Color(24, 119, 242));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(300, 45));
        cardPanel.add(loginBtn, gbc);

        // Secondary Link
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5);
        JButton registerBtn = new JButton("Don't have an account? Sign up");
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setForeground(new Color(24, 119, 242));
        registerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardPanel.add(registerBtn, gbc);

        // Extracted Actions
        loginBtn.addActionListener(e -> {
            if (usernameField.getText().trim().isEmpty() || new String(passwordField.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean success = AuthClient.login(usernameField.getText(), new String(passwordField.getPassword()));
            if (success) {
                if (rememberMeBox.isSelected()) {
                    prefs.put("saved_username", usernameField.getText());
                    prefs.put("saved_password", new String(passwordField.getPassword()));
                    prefs.putBoolean("remember_me", true);
                } else {
                    prefs.remove("saved_username");
                    prefs.remove("saved_password");
                    prefs.putBoolean("remember_me", false);
                }
                this.dispose();
                new DashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Login failed. Check your credentials.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            new RegisterDialog(LoginFrame.this).setVisible(true);
        });

        // Add card to main frame
        add(cardPanel);
    }
}
