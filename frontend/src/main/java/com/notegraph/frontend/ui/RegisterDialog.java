package com.notegraph.frontend.ui;

import com.notegraph.frontend.api.AuthClient;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {

    public RegisterDialog(JFrame parent) {
        super(parent, "NoteGraph - Create Account", true); // true = modal
        setSize(500, 550);
        setLocationRelativeTo(parent);

        // Main background
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new GridBagLayout());

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
        JLabel titleLabel = new JLabel("Create an Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.DARK_GRAY);
        cardPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 25, 5);
        JLabel subtitleLabel = new JLabel("Sign up to start connecting your notes", SwingConstants.CENTER);
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
        usernameField.putClientProperty("JTextField.placeholderText", "Choose a unique username");
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
        passwordField.putClientProperty("JTextField.placeholderText", "Create a secure password");
        passwordField.setPreferredSize(new Dimension(300, 40));
        cardPanel.add(passwordField, gbc);

        // Primary Sign Up Button
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 15, 5);
        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.setBackground(new Color(24, 119, 242));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpBtn.setFocusPainted(false);
        signUpBtn.setPreferredSize(new Dimension(300, 45));
        cardPanel.add(signUpBtn, gbc);

        // Cancel / Back Link
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5);
        JButton cancelBtn = new JButton("Already have an account? Log in");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(new Color(24, 119, 242));
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardPanel.add(cancelBtn, gbc);

        // Actions
        signUpBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = AuthClient.register(username, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Username might already exist.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> this.dispose());

        // Add to dialog
        add(cardPanel);
    }
}
