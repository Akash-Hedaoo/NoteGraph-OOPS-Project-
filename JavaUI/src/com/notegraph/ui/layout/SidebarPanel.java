package com.notegraph.ui.layout;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.ConfirmDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Navigation sidebar matching the React Sidebar.jsx.
 * Proper spacing, rounded nav items, clean proportions.
 */
public class SidebarPanel extends JPanel {

    private Consumer<String> onNavigate;
    private Runnable onLogout;
    private String activePage = "dashboard";
    private List<ApiModels.WorkspaceDto> workspaces = new ArrayList<>();
    private JLabel workspaceLabel;
    private JPanel navContainer;
    private JLabel userNameLabel;

    private static final int SIDEBAR_W = 260;

    public SidebarPanel(Consumer<String> onNavigate, Runnable onLogout) {
        this.onNavigate = onNavigate;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(SIDEBAR_W, 0));
        setMinimumSize(new Dimension(SIDEBAR_W, 0));
        setMaximumSize(new Dimension(SIDEBAR_W, Integer.MAX_VALUE));
        setBackground(ColorScheme.SIDEBAR_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorScheme.BORDER));
        buildUI();
    }

    private void buildUI() {
        // ════════════════════════════════════════════
        //  TOP: Brand + User + Create Button
        // ════════════════════════════════════════════
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(20, 20, 12, 20));

        // Brand
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        brandRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel logoIcon = new JLabel("◆");
        logoIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoIcon.setForeground(ColorScheme.PRIMARY_BLUE);
        JLabel logoText = new JLabel("NoteGraph");
        logoText.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 18));
        logoText.setForeground(ColorScheme.TEXT_PRIMARY);
        brandRow.add(logoIcon);
        brandRow.add(logoText);
        top.add(brandRow);
        top.add(Box.createVerticalStrut(18));

        // User badge
        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userBadge.setOpaque(false);
        userBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        userBadge.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar circle
        JLabel avatar = new JLabel("A") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(55, 65, 81));
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (32 - fm.stringWidth(t)) / 2, (32 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setText("");

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);
        userNameLabel = new JLabel("User");
        userNameLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        userNameLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel planLabel = new JLabel("Pro Plan");
        planLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        planLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        userInfo.add(userNameLabel);
        userInfo.add(planLabel);
        userBadge.add(avatar);
        userBadge.add(userInfo);
        top.add(userBadge);
        top.add(Box.createVerticalStrut(18));

        // Create New Note button
        JButton createBtn = new JButton("📄  Create New Note") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        createBtn.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(ColorScheme.PRIMARY_BLUE);
        createBtn.setBorderPainted(false);
        createBtn.setFocusPainted(false);
        createBtn.setContentAreaFilled(false);
        createBtn.setOpaque(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        createBtn.setPreferredSize(new Dimension(0, 42));
        createBtn.setAlignmentX(LEFT_ALIGNMENT);
        createBtn.addActionListener(e -> onNavigate.accept("editor"));
        createBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { createBtn.setBackground(ColorScheme.PRIMARY_BLUE_HOVER); createBtn.repaint(); }
            public void mouseExited(MouseEvent e) { createBtn.setBackground(ColorScheme.PRIMARY_BLUE); createBtn.repaint(); }
        });
        top.add(createBtn);

        add(top, BorderLayout.NORTH);

        // ════════════════════════════════════════════
        //  MIDDLE: Workspace + Menu + Pinned
        // ════════════════════════════════════════════
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setOpaque(false);
        middle.setBorder(new EmptyBorder(8, 20, 8, 20));

        // ── MY WORKSPACE ─────────────────────
        middle.add(createSectionTitle("MY WORKSPACE"));
        middle.add(Box.createVerticalStrut(8));

        JPanel wsSelector = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ColorScheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        wsSelector.setOpaque(false);
        wsSelector.setBackground(Color.WHITE);
        wsSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wsSelector.setAlignmentX(LEFT_ALIGNMENT);
        wsSelector.setBorder(new EmptyBorder(0, 14, 0, 14));
        wsSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        workspaceLabel = new JLabel("Loading...");
        workspaceLabel.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 14));
        workspaceLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel wsArrow = new JLabel("▾");
        wsArrow.setFont(new Font("SansSerif", Font.PLAIN, 14));
        wsArrow.setForeground(ColorScheme.TEXT_TERTIARY);
        wsSelector.add(workspaceLabel, BorderLayout.CENTER);
        wsSelector.add(wsArrow, BorderLayout.EAST);
        wsSelector.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showWorkspaceMenu(wsSelector); }
            public void mouseEntered(MouseEvent e) { wsSelector.setBackground(ColorScheme.BG_APP); wsSelector.repaint(); }
            public void mouseExited(MouseEvent e) { wsSelector.setBackground(Color.WHITE); wsSelector.repaint(); }
        });
        middle.add(wsSelector);
        middle.add(Box.createVerticalStrut(24));

        // ── MENU ─────────────────────────────
        middle.add(createSectionTitle("MENU"));
        middle.add(Box.createVerticalStrut(8));

        navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setAlignmentX(LEFT_ALIGNMENT);
        rebuildNav();
        middle.add(navContainer);
        middle.add(Box.createVerticalStrut(24));

        // ── PINNED ───────────────────────────
        JPanel pinnedHeader = new JPanel(new BorderLayout());
        pinnedHeader.setOpaque(false);
        pinnedHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        pinnedHeader.setAlignmentX(LEFT_ALIGNMENT);
        pinnedHeader.add(createSectionTitle("PINNED"), BorderLayout.WEST);
        JLabel addPinLabel = new JLabel("+");
        addPinLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        addPinLabel.setForeground(ColorScheme.TEXT_TERTIARY);
        addPinLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pinnedHeader.add(addPinLabel, BorderLayout.EAST);
        middle.add(pinnedHeader);
        middle.add(Box.createVerticalStrut(8));

        middle.add(createPinnedItem("●", "Scratchpad", new Color(59, 130, 246)));
        middle.add(Box.createVerticalStrut(4));
        middle.add(createPinnedItem("●", "Daily Log", new Color(139, 92, 246)));
        middle.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(middle);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // ════════════════════════════════════════════
        //  FOOTER: Settings + Logout
        // ════════════════════════════════════════════
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JButton settingsBtn = new JButton("⚙  Settings");
        settingsBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        settingsBtn.setForeground(ColorScheme.TEXT_SECONDARY);
        settingsBtn.setBackground(null);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsBtn.setHorizontalAlignment(SwingConstants.LEFT);
        footer.add(settingsBtn, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        logoutBtn.setForeground(ColorScheme.ERROR_TEXT);
        logoutBtn.setBackground(null);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            ApiClient.get().logout();
            if (onLogout != null) onLogout.run();
        });
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setForeground(new Color(220, 38, 38)); }
            public void mouseExited(MouseEvent e) { logoutBtn.setForeground(ColorScheme.ERROR_TEXT); }
        });
        footer.add(logoutBtn, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // ─── Helpers ────────────────────────────────────

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 11));
        label.setForeground(ColorScheme.TEXT_TERTIARY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createNavItem(String page, String icon, String label) {
        boolean active = page.equals(activePage);

        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBackground(active ? ColorScheme.SIDEBAR_ACTIVE_BG : ColorScheme.SIDEBAR_BG);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setPreferredSize(new Dimension(0, 40));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), active ? Font.BOLD : Font.PLAIN, 14));
        textLbl.setForeground(active ? ColorScheme.PRIMARY_BLUE : ColorScheme.TEXT_PRIMARY);
        left.add(iconLbl);
        left.add(textLbl);
        item.add(left, BorderLayout.CENTER);

        // Active indicator dot
        if (active) {
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("SansSerif", Font.PLAIN, 8));
            dot.setForeground(ColorScheme.PRIMARY_BLUE);
            item.add(dot, BorderLayout.EAST);
        }

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!page.equals(activePage)) { item.setBackground(ColorScheme.SIDEBAR_HOVER); item.repaint(); }
            }
            public void mouseExited(MouseEvent e) {
                item.setBackground(page.equals(activePage) ? ColorScheme.SIDEBAR_ACTIVE_BG : ColorScheme.SIDEBAR_BG);
                item.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                setActivePage(page);
                onNavigate.accept(page);
            }
        });

        return item;
    }

    private JPanel createPinnedItem(String dot, String label, Color dotColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        item.setPreferredSize(new Dimension(0, 34));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel dotLbl = new JLabel(dot);
        dotLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dotLbl.setForeground(dotColor);
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
        textLbl.setForeground(ColorScheme.TEXT_SECONDARY);
        item.add(dotLbl);
        item.add(textLbl);

        item.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onNavigate.accept("editor"); }
            public void mouseEntered(MouseEvent e) { textLbl.setForeground(ColorScheme.TEXT_PRIMARY); }
            public void mouseExited(MouseEvent e) { textLbl.setForeground(ColorScheme.TEXT_SECONDARY); }
        });

        return item;
    }

    private void rebuildNav() {
        navContainer.removeAll();
        navContainer.add(createNavItem("dashboard", "📊", "Dashboard"));
        navContainer.add(Box.createVerticalStrut(4));
        navContainer.add(createNavItem("hierarchy", "📁", "Note Graph"));
        navContainer.add(Box.createVerticalStrut(4));
        navContainer.add(createNavItem("knowledgegraph", "🧠", "Knowledge Graph"));
        navContainer.add(Box.createVerticalStrut(4));
        navContainer.add(createNavItem("tags", "🏷", "Tags"));
        navContainer.add(Box.createVerticalStrut(4));
        navContainer.add(createNavItem("favorites", "⭐", "Favorites"));
        navContainer.add(Box.createVerticalStrut(4));
        navContainer.add(createNavItem("guide", "📖", "Guide"));
    }

    public void setActivePage(String page) {
        this.activePage = page;
        rebuildNav();
        navContainer.revalidate();
        navContainer.repaint();
    }

    public void refreshData() {
        ApiModels.UserDto user = ApiClient.get().getUser();
        if (user != null) userNameLabel.setText(user.name);

        new SwingWorker<List<ApiModels.WorkspaceDto>, Void>() {
            @Override
            protected List<ApiModels.WorkspaceDto> doInBackground() throws Exception {
                return ApiClient.get().getWorkspaces();
            }
            @Override
            protected void done() {
                try {
                    workspaces = get();
                    String wsId = ApiClient.get().getWorkspaceId();
                    ApiModels.WorkspaceDto active = workspaces.stream()
                            .filter(w -> w.id.equals(wsId))
                            .findFirst().orElse(workspaces.isEmpty() ? null : workspaces.get(0));
                    workspaceLabel.setText(active != null ? active.name : "No workspace");
                } catch (Exception e) {
                    workspaceLabel.setText("Error");
                }
            }
        }.execute();
    }

    private void showWorkspaceMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER),
                new EmptyBorder(4, 0, 4, 0)));

        String currentWsId = ApiClient.get().getWorkspaceId();
        for (ApiModels.WorkspaceDto ws : workspaces) {
            JMenuItem item = new JMenuItem(ws.name + (ws.id.equals(currentWsId) ? "  ✓" : ""));
            item.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
            item.setBorder(new EmptyBorder(6, 12, 6, 12));
            item.addActionListener(e -> {
                ApiClient.get().setWorkspaceId(ws.id);
                workspaceLabel.setText(ws.name);
                onNavigate.accept(activePage);
            });
            menu.add(item);
        }

        menu.addSeparator();

        JMenuItem createItem = new JMenuItem("＋  Create new workspace");
        createItem.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
        createItem.setForeground(ColorScheme.TEXT_SECONDARY);
        createItem.setBorder(new EmptyBorder(6, 12, 6, 12));
        createItem.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Workspace Name:", "Create Workspace", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        ApiModels.WorkspaceDto ws = ApiClient.get().createWorkspace(name.trim());
                        ApiClient.get().setWorkspaceId(ws.id);
                        return null;
                    }
                    @Override
                    protected void done() { refreshData(); }
                }.execute();
            }
        });
        menu.add(createItem);

        menu.show(anchor, 0, anchor.getHeight() + 4);
    }
}
