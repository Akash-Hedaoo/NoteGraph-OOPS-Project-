package com.notegraph.ui.auth;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login / Register panel matching the React Login.jsx design.
 */
public class LoginPanel extends JPanel {

    private boolean isLogin = true;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton submitBtn;
    private JLabel toggleLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JPanel nameRow;
    private JLabel promptLabel;
    private Runnable onLoginSuccess;

    public LoginPanel(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_APP);
        buildUI();
    }

    private void buildUI() {
        // ── Top Header ──────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.BG_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER),
                new EmptyBorder(16, 32, 16, 32)
        ));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoPanel.setOpaque(false);
        JLabel logoIcon = new JLabel("◆");
        logoIcon.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoIcon.setForeground(ColorScheme.PRIMARY_BLUE);
        JLabel logoText = new JLabel("NoteGraph");
        logoText.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 18));
        logoText.setForeground(ColorScheme.TEXT_PRIMARY);
        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0));
        links.setOpaque(false);
        JLabel helpLink = makeLink("Help Center", ColorScheme.TEXT_SECONDARY);
        JLabel supportLink = makeLink("Contact Support", ColorScheme.TEXT_PRIMARY);
        supportLink.setFont(ColorScheme.FONT_SEMIBOLD);
        links.add(helpLink);
        links.add(supportLink);

        header.add(logoPanel, BorderLayout.WEST);
        header.add(links, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Center Card ─────────────────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Use a non-BoxLayout approach for better control
        JPanel card = new RoundedPanel(16, ColorScheme.BORDER, true);
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(460, 580));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 40, 0, 40);
        gbc.weightx = 1.0;
        int row = 0;

        // Title
        gbc.gridy = row++;
        gbc.insets = new Insets(36, 40, 0, 40);
        titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 26));
        titleLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        card.add(titleLabel, gbc);

        // Subtitle
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 40, 0, 40);
        subtitleLabel = new JLabel("Sign in to sync your notes across devices", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
        subtitleLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        card.add(subtitleLabel, gbc);

        // Spacer
        gbc.gridy = row++;
        gbc.insets = new Insets(24, 40, 0, 40);
        card.add(new JLabel(""), gbc);

        // Name field (hidden for login)
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 40, 0, 40);
        nameRow = new JPanel(new BorderLayout(0, 8));
        nameRow.setOpaque(false);
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        nameLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        nameField = new JTextField();
        nameField.setFont(ColorScheme.FONT_REGULAR);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        nameRow.add(nameLabel, BorderLayout.NORTH);
        nameRow.add(nameField, BorderLayout.CENTER);
        nameRow.setVisible(false);
        card.add(nameRow, gbc);

        // Email label
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 40, 0, 40);
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        emailLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        card.add(emailLabel, gbc);

        // Email field
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 40, 0, 40);
        emailField = new JTextField();
        emailField.setFont(ColorScheme.FONT_REGULAR);
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        card.add(emailField, gbc);

        // Password header (label + forgot password)
        gbc.gridy = row++;
        gbc.insets = new Insets(18, 40, 0, 40);
        JPanel passHeader = new JPanel(new BorderLayout());
        passHeader.setOpaque(false);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        passLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel forgotLink = makeLink("Forgot password?", ColorScheme.PRIMARY_BLUE);
        forgotLink.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        passHeader.add(passLabel, BorderLayout.WEST);
        passHeader.add(forgotLink, BorderLayout.EAST);
        card.add(passHeader, gbc);

        // Password field
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 40, 0, 40);
        passwordField = new JPasswordField();
        passwordField.setFont(ColorScheme.FONT_REGULAR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        passwordField.addActionListener(e -> doSubmit());
        card.add(passwordField, gbc);

        // Error label
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 40, 0, 40);
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        errorLabel.setForeground(ColorScheme.ERROR_TEXT);
        card.add(errorLabel, gbc);

        // Submit button
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 40, 0, 40);
        submitBtn = new JButton("Sign In  →");
        submitBtn.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(ColorScheme.PRIMARY_BLUE);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.setPreferredSize(new Dimension(0, 46));
        submitBtn.addActionListener(e -> doSubmit());
        submitBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { submitBtn.setBackground(ColorScheme.PRIMARY_BLUE_HOVER); }
            public void mouseExited(MouseEvent e) { submitBtn.setBackground(ColorScheme.PRIMARY_BLUE); }
        });
        card.add(submitBtn, gbc);

        // Divider — custom painted "─── Or continue with ───"
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 40, 0, 40);
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                String text = "Or continue with";
                g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int cx = w / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                // Lines
                g2.setColor(ColorScheme.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(0, h / 2, cx - tw / 2 - 12, h / 2);
                g2.drawLine(cx + tw / 2 + 12, h / 2, w, h / 2);
                // Text
                g2.setColor(ColorScheme.TEXT_TERTIARY);
                g2.drawString(text, cx - tw / 2, ty);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 24));
        card.add(divider, gbc);

        // Social buttons
        gbc.gridy = row++;
        gbc.insets = new Insets(16, 40, 0, 40);
        JPanel socialRow = new JPanel(new GridLayout(1, 2, 14, 0));
        socialRow.setOpaque(false);
        socialRow.add(createSocialBtn("Google"));
        socialRow.add(createSocialBtn("GitHub"));
        card.add(socialRow, gbc);

        // Toggle login/register
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 40, 0, 40);
        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        toggleRow.setOpaque(false);
        promptLabel = new JLabel("Don't have an account?");
        promptLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
        promptLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        toggleLabel = new JLabel("Sign up");
        toggleLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        toggleLabel.setForeground(ColorScheme.PRIMARY_BLUE);
        toggleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { toggleMode(); }
        });
        toggleRow.add(promptLabel);
        toggleRow.add(toggleLabel);
        card.add(toggleRow, gbc);

        // Footer links
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 40, 32, 40);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        footer.setOpaque(false);
        footer.add(makeLink("Privacy Policy", ColorScheme.TEXT_SECONDARY));
        footer.add(makeLink("Terms of Service", ColorScheme.TEXT_SECONDARY));
        card.add(footer, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JLabel makeLink(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        lbl.setForeground(color);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private JButton createSocialBtn(String name) {
        JButton btn = new JButton(name);
        btn.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 14));
        btn.setForeground(ColorScheme.TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(10, 0, 10, 0)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ColorScheme.BG_APP); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private void toggleMode() {
        isLogin = !isLogin;
        nameRow.setVisible(!isLogin);
        titleLabel.setText(isLogin ? "Welcome Back" : "Create an Account");
        subtitleLabel.setText(isLogin ? "Sign in to sync your notes across devices" : "Get started with your digital brain");
        submitBtn.setText(isLogin ? "Sign In  →" : "Sign Up  →");
        toggleLabel.setText(isLogin ? "Sign up" : "Sign in");
        promptLabel.setText(isLogin ? "Don't have an account?" : "Already have an account?");
        errorLabel.setText(" ");
        revalidate();
        repaint();
    }

    private void doSubmit() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || (!isLogin && name.isEmpty())) {
            errorLabel.setText("⚠ Please fill in all required fields.");
            return;
        }

        submitBtn.setEnabled(false);
        submitBtn.setText("Connecting...");
        errorLabel.setText(" ");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String errorMsg;
            @Override
            protected Void doInBackground() {
                try {
                    if (isLogin) {
                        ApiClient.get().login(email, password);
                    } else {
                        ApiClient.get().register(email, password, name);
                    }
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                }
                return null;
            }
            @Override
            protected void done() {
                submitBtn.setEnabled(true);
                submitBtn.setText(isLogin ? "Sign In  →" : "Sign Up  →");
                if (errorMsg != null) {
                    errorLabel.setText("⚠ " + errorMsg);
                } else {
                    onLoginSuccess.run();
                }
            }
        };
        worker.execute();
    }
}
