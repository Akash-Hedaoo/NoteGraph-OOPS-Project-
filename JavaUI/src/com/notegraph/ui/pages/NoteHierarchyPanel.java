package com.notegraph.ui.pages;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.ConfirmDialog;
import com.notegraph.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Note hierarchy page with TWO view modes:
 *  - "org" = Visual org chart with painted nodes and connector lines (DEFAULT)
 *  - "tree" = Collapsible tree list
 * Matches React NoteHierarchy.jsx exactly.
 */
public class NoteHierarchyPanel extends JPanel {

    private Consumer<String> onNavigate;
    private List<ApiModels.GraphBranch> branches = new ArrayList<>();
    private ApiModels.GraphBranch selectedBranch;
    private String viewMode = "org"; // "org" or "tree"
    private JPanel detailsSidebar;
    private JPanel detailsContent;
    private JPanel mainContent;
    private CardLayout viewCards;
    private JPanel viewPanel;
    private JButton orgBtn, treeBtn;
    private String workspaceName = "Workspace";
    private java.util.Map<String, Boolean> expandedTags = new java.util.HashMap<>();

    private static final String[] COLORS = {"purple", "blue", "green", "orange", "red"};
    private static final Color[] COLOR_MAIN = {
        new Color(139, 92, 246),  // purple
        new Color(59, 130, 246),  // blue
        new Color(16, 185, 129),  // green
        new Color(249, 115, 22),  // orange
        new Color(239, 68, 68),   // red
    };
    private static final Color[] COLOR_LIGHT = {
        new Color(245, 243, 255),
        new Color(239, 246, 255),
        new Color(209, 250, 229),
        new Color(255, 247, 237),
        new Color(254, 226, 226),
    };

    public NoteHierarchyPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private Color getColorMain(int idx) { return COLOR_MAIN[idx % COLOR_MAIN.length]; }
    private Color getColorMain(String c) {
        if (c == null) return COLOR_MAIN[1];
        return switch (c.toLowerCase()) {
            case "purple" -> COLOR_MAIN[0]; case "blue" -> COLOR_MAIN[1];
            case "green" -> COLOR_MAIN[2]; case "orange" -> COLOR_MAIN[3];
            case "red" -> COLOR_MAIN[4]; default -> COLOR_MAIN[1];
        };
    }
    private Color getColorLight(String c) {
        if (c == null) return COLOR_LIGHT[1];
        return switch (c.toLowerCase()) {
            case "purple" -> COLOR_LIGHT[0]; case "blue" -> COLOR_LIGHT[1];
            case "green" -> COLOR_LIGHT[2]; case "orange" -> COLOR_LIGHT[3];
            case "red" -> COLOR_LIGHT[4]; default -> COLOR_LIGHT[1];
        };
    }

    private void buildUI() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        mainArea.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Header ──────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel breadcrumb = new JLabel("Workspaces  ▸  " + workspaceName);
        breadcrumb.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 13));
        breadcrumb.setForeground(ColorScheme.PRIMARY_BLUE);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(breadcrumb);
        header.add(Box.createVerticalStrut(8));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Note Connections");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);

        JPanel toolbarBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbarBtns.setOpaque(false);

        // View toggle
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        togglePanel.setBackground(Color.WHITE);
        togglePanel.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true));
        orgBtn = createToggleBtn("📊 Chart", true);
        treeBtn = createToggleBtn("📄 Tree", false);
        orgBtn.addActionListener(e -> switchView("org"));
        treeBtn.addActionListener(e -> switchView("tree"));
        togglePanel.add(orgBtn);
        togglePanel.add(treeBtn);
        toolbarBtns.add(togglePanel);

        JButton filterBtn = new JButton("🔍 Filter by Tag");
        filterBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        filterBtn.setForeground(ColorScheme.TEXT_PRIMARY);
        filterBtn.setBackground(Color.WHITE);
        filterBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        filterBtn.setFocusPainted(false);
        toolbarBtns.add(filterBtn);

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(toolbarBtns, BorderLayout.EAST);
        header.add(titleRow);
        header.add(Box.createVerticalStrut(16));
        mainArea.add(header, BorderLayout.NORTH);

        // ── View Area (CardLayout) ──────────────────
        viewCards = new CardLayout();
        viewPanel = new JPanel(viewCards);
        viewPanel.setOpaque(false);
        viewPanel.add(createOrgChartView(), "org");
        viewPanel.add(createTreeView(), "tree");
        viewCards.show(viewPanel, "org");

        mainArea.add(viewPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // ── Details Sidebar ─────────────────────────
        detailsSidebar = new JPanel();
        detailsSidebar.setLayout(new BoxLayout(detailsSidebar, BoxLayout.Y_AXIS));
        detailsSidebar.setBackground(Color.WHITE);
        detailsSidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(20, 16, 20, 16)
        ));
        detailsSidebar.setPreferredSize(new Dimension(320, 0));

        JPanel detailsHeader = new JPanel(new BorderLayout());
        detailsHeader.setOpaque(false);
        detailsHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        detailsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detTitle = new JLabel("Connection Details");
        detTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 16));
        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { selectedBranch = null; detailsSidebar.setVisible(false); }
        });
        detailsHeader.add(detTitle, BorderLayout.WEST);
        detailsHeader.add(closeBtn, BorderLayout.EAST);
        detailsSidebar.add(detailsHeader);
        detailsSidebar.add(Box.createVerticalStrut(16));

        detailsContent = new JPanel();
        detailsContent.setLayout(new BoxLayout(detailsContent, BoxLayout.Y_AXIS));
        detailsContent.setOpaque(false);
        detailsContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane detScroll = new JScrollPane(detailsContent);
        detScroll.setBorder(null);
        detScroll.setOpaque(false);
        detScroll.getViewport().setOpaque(false);
        detailsSidebar.add(detScroll);
        detailsSidebar.setVisible(false);

        add(detailsSidebar, BorderLayout.EAST);
    }

    // ═══════════════════════════════════════════════
    //  ORG CHART VIEW — Custom painted canvas
    // ═══════════════════════════════════════════════
    private JScrollPane createOrgChartView() {
        mainContent = new OrgChartCanvas();
        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** The custom-painted org chart panel with grid background, nodes, and connecting lines. */
    class OrgChartCanvas extends JPanel {
        private static final int NODE_W = 200, NODE_H_TAG = 64, NODE_H_NOTE = 44;
        private static final int ROOT_W = 280, ROOT_H = 52;
        private static final int H_GAP = 48, V_GAP_ROOT = 40, V_GAP_TAG = 28, V_GAP_LEAF = 16;
        private static final int LEAF_GAP = 12;

        OrgChartCanvas() {
            setBackground(new Color(248, 250, 252));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { handleChartClick(e.getX(), e.getY()); }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            int cols = Math.max(branches.size(), 1);
            int maxLeaves = branches.stream().mapToInt(b -> b.leaves != null ? b.leaves.size() : 0).max().orElse(1);
            int w = Math.max(cols * (NODE_W + H_GAP) + 100, 800);
            int h = ROOT_H + V_GAP_ROOT + NODE_H_TAG + V_GAP_TAG + (maxLeaves * (NODE_H_NOTE + LEAF_GAP)) + 120;
            return new Dimension(w, Math.max(h, 500));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Grid pattern background
            g2.setColor(new Color(10, 102, 240, 12));
            for (int x = 0; x < w; x += 20) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 20) g2.drawLine(0, y, w, y);

            if (branches.isEmpty()) {
                g2.setColor(ColorScheme.TEXT_SECONDARY);
                g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 15));
                String msg = "No tags yet. Create notes and add tags to see connections!";
                int mw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (w - mw) / 2, h / 2);
                g2.dispose();
                return;
            }

            int cols = branches.size();
            int totalW = cols * NODE_W + (cols - 1) * H_GAP;
            int startX = (w - totalW) / 2;
            int rootCX = w / 2;

            // ─── Root Node ──────────────────────────
            int rootX = rootCX - ROOT_W / 2;
            int rootY = 40;
            drawRootNode(g2, rootX, rootY, ROOT_W, ROOT_H, workspaceName + " Workspace Notes");

            int rootBottomY = rootY + ROOT_H;

            // ─── Vertical line down from root ───────
            int lineX = rootCX;
            int lineEndY = rootBottomY + V_GAP_ROOT;
            g2.setColor(ColorScheme.BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(lineX, rootBottomY, lineX, lineEndY);

            // ─── Horizontal connector across tags ───
            int tagY = lineEndY;
            if (cols > 1) {
                int leftTagCX = startX + NODE_W / 2;
                int rightTagCX = startX + (cols - 1) * (NODE_W + H_GAP) + NODE_W / 2;
                g2.drawLine(leftTagCX, tagY, rightTagCX, tagY);
            }

            // ─── Tag nodes + their leaves ───────────
            for (int i = 0; i < cols; i++) {
                ApiModels.GraphBranch branch = branches.get(i);
                String color = branch.tagColor != null ? branch.tagColor : COLORS[i % COLORS.length];
                Color colorMain = getColorMain(color);
                Color colorLight = getColorLight(color);

                int tagX = startX + i * (NODE_W + H_GAP);
                int tagCX = tagX + NODE_W / 2;

                // Vertical stub down to tag
                g2.setColor(ColorScheme.BORDER);
                g2.drawLine(tagCX, tagY, tagCX, tagY + V_GAP_TAG / 2);

                int tagNodeY = tagY + V_GAP_TAG / 2;
                drawTagNode(g2, tagX, tagNodeY, NODE_W, NODE_H_TAG, branch.tagTitle, branch.noteCount, colorMain);

                int tagBottomY = tagNodeY + NODE_H_TAG;

                // Leaves
                if (branch.leaves != null && !branch.leaves.isEmpty()) {
                    g2.setColor(ColorScheme.BORDER);
                    g2.drawLine(tagCX, tagBottomY, tagCX, tagBottomY + V_GAP_TAG / 2);

                    int leafY = tagBottomY + V_GAP_TAG / 2;
                    for (int j = 0; j < branch.leaves.size(); j++) {
                        ApiModels.GraphLeaf leaf = branch.leaves.get(j);
                        drawNoteNode(g2, tagX, leafY, NODE_W, NODE_H_NOTE, 
                                     leaf.title != null ? leaf.title : "Untitled Note", colorMain);
                        leafY += NODE_H_NOTE + LEAF_GAP;

                        // Connector between leaves
                        if (j < branch.leaves.size() - 1) {
                            g2.setColor(ColorScheme.BORDER);
                            g2.setStroke(new BasicStroke(2));
                            g2.drawLine(tagCX, leafY - LEAF_GAP, tagCX, leafY);
                        }
                    }
                }
            }

            g2.dispose();
        }

        private void drawRootNode(Graphics2D g2, int x, int y, int w, int h, String text) {
            // Blue glow
            g2.setColor(new Color(10, 102, 240, 25));
            g2.fillRoundRect(x - 4, y - 4, w + 8, h + 8, 14, 14);
            // White body
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, w, h, 10, 10);
            // Blue border
            g2.setColor(ColorScheme.PRIMARY_BLUE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w, h, 10, 10);
            // Icon + text
            g2.setColor(ColorScheme.PRIMARY_BLUE);
            g2.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String icon = "📁 ";
            String full = icon + text;
            if (fm.stringWidth(full) > w - 24) text = text.substring(0, Math.max(0, 18)) + "...";
            full = icon + text;
            int tw = fm.stringWidth(full);
            g2.drawString(full, x + (w - tw) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
        }

        private void drawTagNode(Graphics2D g2, int x, int y, int w, int h, String tagTitle, int count, Color color) {
            // Shadow
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(x + 2, y + 3, w, h, 10, 10);
            // White body
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, w, h, 10, 10);
            // Color border left
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, w, h, 10, 10);
            // Color header bar
            int headerH = h / 2;
            g2.setColor(color);
            g2.fillRoundRect(x, y, w, headerH, 10, 10);
            g2.fillRect(x, y + headerH - 6, w, 6); // fill the bottom corners
            // Header text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String header = "🏷 #" + tagTitle;
            int tw = fm.stringWidth(header);
            g2.drawString(header, x + (w - tw) / 2, y + (headerH + fm.getAscent() - fm.getDescent()) / 2);
            // Body text
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
            fm = g2.getFontMetrics();
            String body = count + " connected notes";
            tw = fm.stringWidth(body);
            g2.drawString(body, x + (w - tw) / 2, y + headerH + (h - headerH + fm.getAscent() - fm.getDescent()) / 2);
        }

        private void drawNoteNode(Graphics2D g2, int x, int y, int w, int h, String title, Color color) {
            // Shadow
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(x + 1, y + 2, w, h, 8, 8);
            // White body
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(new Color(229, 231, 235));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, w, h, 8, 8);
            // Icon + Title
            g2.setColor(color);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("📄", x + 12, y + h / 2 + 5);
            g2.setColor(ColorScheme.TEXT_PRIMARY);
            g2.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            String t = title;
            if (fm.stringWidth(t) > w - 48) t = t.substring(0, Math.max(0, 16)) + "...";
            g2.drawString(t, x + 34, y + (h + fm.getAscent() - fm.getDescent()) / 2);
        }

        private void handleChartClick(int mx, int my) {
            if (branches.isEmpty()) return;
            int cols = branches.size();
            int totalW = cols * NODE_W + (cols - 1) * H_GAP;
            int startX = (getWidth() - totalW) / 2;
            int tagY = 40 + ROOT_H + V_GAP_ROOT + V_GAP_TAG / 2;

            for (int i = 0; i < cols; i++) {
                int tagX = startX + i * (NODE_W + H_GAP);
                // Check tag node hit
                if (mx >= tagX && mx <= tagX + NODE_W && my >= tagY && my <= tagY + NODE_H_TAG) {
                    selectedBranch = branches.get(i);
                    updateDetailsSidebar();
                    return;
                }
                // Check leaf hits
                if (branches.get(i).leaves != null) {
                    int leafY = tagY + NODE_H_TAG + V_GAP_TAG / 2;
                    for (ApiModels.GraphLeaf leaf : branches.get(i).leaves) {
                        if (mx >= tagX && mx <= tagX + NODE_W && my >= leafY && my <= leafY + NODE_H_NOTE) {
                            onNavigate.accept("editor:" + leaf.id);
                            return;
                        }
                        leafY += NODE_H_NOTE + LEAF_GAP;
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  TREE LIST VIEW — Collapsible cards
    // ═══════════════════════════════════════════════
    private JScrollPane createTreeView() {
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.Y_AXIS));
        treePanel.setOpaque(false);
        treePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        treePanel.setName("treeContent");
        JScrollPane scroll = new JScrollPane(treePanel);
        scroll.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    private void rebuildTreeView() {
        // Find the tree content panel from the card layout
        for (Component c : viewPanel.getComponents()) {
            if (c instanceof JScrollPane sp) {
                if (sp.getViewport().getView() instanceof JPanel p && "treeContent".equals(p.getName())) {
                    p.removeAll();
                    buildTreeContent(p);
                    p.revalidate();
                    p.repaint();
                    return;
                }
            }
        }
    }

    private void buildTreeContent(JPanel treePanel) {
        // Root workspace card
        RoundedPanel rootCard = new RoundedPanel(10, ColorScheme.BORDER, false);
        rootCard.setLayout(new BorderLayout());
        rootCard.setBorder(new EmptyBorder(14, 18, 14, 18));
        rootCard.setMaximumSize(new Dimension(800, 60));
        rootCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rootLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rootLeft.setOpaque(false);
        JLabel wIcon = new JLabel("W");
        wIcon.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 18));
        wIcon.setForeground(ColorScheme.PRIMARY_BLUE);
        wIcon.setBackground(ColorScheme.TAG_BLUE_BG);
        wIcon.setOpaque(true);
        wIcon.setBorder(new EmptyBorder(6, 10, 6, 10));
        JPanel wText = new JPanel();
        wText.setLayout(new BoxLayout(wText, BoxLayout.Y_AXIS));
        wText.setOpaque(false);
        JLabel wName = new JLabel(workspaceName + " Workspace");
        wName.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
        JLabel wDesc = new JLabel("Tag hierarchy and note organization");
        wDesc.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        wDesc.setForeground(ColorScheme.TEXT_SECONDARY);
        wText.add(wName);
        wText.add(wDesc);
        rootLeft.add(wIcon);
        rootLeft.add(wText);
        rootCard.add(rootLeft, BorderLayout.WEST);
        treePanel.add(rootCard);
        treePanel.add(Box.createVerticalStrut(20));

        if (branches.isEmpty()) {
            JLabel empty = new JLabel("No tags yet. Create notes and add tags!");
            empty.setFont(ColorScheme.FONT_REGULAR);
            empty.setForeground(ColorScheme.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(0, 24, 0, 0));
            treePanel.add(empty);
            return;
        }

        // Branches container with left border
        JPanel branchesBox = new JPanel();
        branchesBox.setLayout(new BoxLayout(branchesBox, BoxLayout.Y_AXIS));
        branchesBox.setOpaque(false);
        branchesBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, ColorScheme.BORDER),
                new EmptyBorder(0, 24, 0, 0)
        ));
        branchesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        branchesBox.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        for (int i = 0; i < branches.size(); i++) {
            ApiModels.GraphBranch branch = branches.get(i);
            String color = branch.tagColor != null ? branch.tagColor : COLORS[i % COLORS.length];
            boolean isExpanded = expandedTags.getOrDefault(branch.tagId, true);

            // Tag header card
            RoundedPanel tagCard = new RoundedPanel(12, ColorScheme.BORDER, false);
            tagCard.setLayout(new BorderLayout());
            tagCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            tagCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            tagCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            tagCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel tagLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            tagLeft.setOpaque(false);
            JLabel dot = new JLabel("●");
            dot.setForeground(getColorMain(color));
            JLabel tagName = new JLabel(branch.tagTitle);
            tagName.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
            JLabel tagCount = new JLabel("  " + branch.noteCount + " sub-notes");
            tagCount.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
            tagCount.setForeground(ColorScheme.TEXT_SECONDARY);
            tagLeft.add(dot);
            tagLeft.add(tagName);
            tagLeft.add(tagCount);
            tagCard.add(tagLeft, BorderLayout.WEST);

            JPanel tagRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            tagRight.setOpaque(false);
            JLabel chevron = new JLabel(isExpanded ? "▲" : "▼");
            chevron.setFont(new Font("SansSerif", Font.PLAIN, 12));
            chevron.setForeground(ColorScheme.TEXT_TERTIARY);
            tagRight.add(chevron);
            tagCard.add(tagRight, BorderLayout.EAST);

            final int idx = i;
            tagCard.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedBranch = branches.get(idx);
                    expandedTags.put(branch.tagId, !expandedTags.getOrDefault(branch.tagId, true));
                    updateDetailsSidebar();
                    rebuildTreeView();
                }
                public void mouseEntered(MouseEvent e) { tagCard.setBackground(new Color(252, 253, 255)); }
                public void mouseExited(MouseEvent e) { tagCard.setBackground(Color.WHITE); }
            });

            branchesBox.add(tagCard);
            branchesBox.add(Box.createVerticalStrut(8));

            // Leaves (if expanded)
            if (isExpanded && branch.leaves != null) {
                JPanel leavesBox = new JPanel();
                leavesBox.setLayout(new BoxLayout(leavesBox, BoxLayout.Y_AXIS));
                leavesBox.setOpaque(false);
                leavesBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, ColorScheme.BORDER),
                        new EmptyBorder(0, 24, 12, 0)
                ));
                leavesBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                for (ApiModels.GraphLeaf leaf : branch.leaves) {
                    RoundedPanel leafCard = new RoundedPanel(8, ColorScheme.BORDER, false);
                    leafCard.setLayout(new BorderLayout());
                    leafCard.setBorder(new EmptyBorder(12, 16, 12, 16));
                    leafCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                    leafCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                    leafCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    JPanel leafLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                    leafLeft.setOpaque(false);
                    JLabel fileIcon = new JLabel("📄");
                    JLabel leafTitle = new JLabel(leaf.title != null ? leaf.title : "Untitled Note");
                    leafTitle.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 14));
                    leafLeft.add(fileIcon);
                    leafLeft.add(leafTitle);
                    leafCard.add(leafLeft, BorderLayout.WEST);

                    JLabel linkedBadge = new JLabel("Linked");
                    linkedBadge.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 11));
                    linkedBadge.setForeground(ColorScheme.TEXT_SECONDARY);
                    linkedBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                            new EmptyBorder(2, 8, 2, 8)));
                    leafCard.add(linkedBadge, BorderLayout.EAST);

                    leafCard.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) { onNavigate.accept("editor:" + leaf.id); }
                        public void mouseEntered(MouseEvent e) { leafCard.setBackground(new Color(252, 253, 255)); }
                        public void mouseExited(MouseEvent e) { leafCard.setBackground(Color.WHITE); }
                    });

                    leavesBox.add(leafCard);
                    leavesBox.add(Box.createVerticalStrut(6));
                }
                branchesBox.add(leavesBox);
                branchesBox.add(Box.createVerticalStrut(8));
            }
        }

        treePanel.add(branchesBox);
    }

    // ═══════════════════════════════════════════════
    //  DETAILS SIDEBAR
    // ═══════════════════════════════════════════════
    private void updateDetailsSidebar() {
        if (selectedBranch == null) { detailsSidebar.setVisible(false); return; }
        detailsSidebar.setVisible(true);
        detailsContent.removeAll();

        String color = selectedBranch.tagColor != null ? selectedBranch.tagColor : "purple";

        // Preview card
        RoundedPanel preview = new RoundedPanel(10, null, false);
        preview.setBackground(getColorLight(color));
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
        preview.setBorder(new EmptyBorder(16, 16, 16, 16));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagIcon = new JLabel("🏷");
        tagIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        tagIcon.setBackground(getColorMain(color));
        tagIcon.setOpaque(true);
        tagIcon.setBorder(new EmptyBorder(6, 10, 6, 10));
        tagIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        preview.add(tagIcon);
        preview.add(Box.createVerticalStrut(8));

        JLabel tagLabel = new JLabel("#" + selectedBranch.tagTitle);
        tagLabel.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 20));
        tagLabel.setForeground(getColorMain(color).darker());
        tagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        preview.add(tagLabel);

        JLabel countLbl = new JLabel(selectedBranch.noteCount + " Notes Connected");
        countLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        countLbl.setForeground(getColorMain(color));
        countLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        preview.add(countLbl);
        detailsContent.add(preview);
        detailsContent.add(Box.createVerticalStrut(20));

        // Stats
        JLabel statsTitle = new JLabel("GRAPH STATS");
        statsTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 11));
        statsTitle.setForeground(ColorScheme.TEXT_TERTIARY);
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsContent.add(statsTitle);
        detailsContent.add(Box.createVerticalStrut(8));

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 8, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.add(createStatBox(String.valueOf(selectedBranch.noteCount), "Notes"));
        statsGrid.add(createStatBox(String.valueOf(selectedBranch.noteCount * 2), "Links"));
        statsGrid.add(createStatBox("High", "Density"));
        detailsContent.add(statsGrid);
        detailsContent.add(Box.createVerticalStrut(20));

        // Connected Notes
        JLabel connTitle = new JLabel("CONNECTED NOTES (#" + selectedBranch.tagTitle + ")");
        connTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 11));
        connTitle.setForeground(ColorScheme.TEXT_TERTIARY);
        connTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsContent.add(connTitle);
        detailsContent.add(Box.createVerticalStrut(10));

        if (selectedBranch.leaves != null) {
            for (ApiModels.GraphLeaf leaf : selectedBranch.leaves) {
                RoundedPanel noteItem = new RoundedPanel(8, ColorScheme.BORDER, false);
                noteItem.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
                noteItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                noteItem.setAlignmentX(Component.LEFT_ALIGNMENT);
                noteItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel icon = new JLabel("📄");
                icon.setBackground(getColorLight(color));
                icon.setOpaque(true);
                icon.setBorder(new EmptyBorder(4, 6, 4, 6));
                JLabel name = new JLabel(leaf.title != null ? leaf.title : "Untitled");
                name.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
                noteItem.add(icon);
                noteItem.add(name);

                noteItem.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { onNavigate.accept("editor:" + leaf.id); }
                    public void mouseEntered(MouseEvent e) { noteItem.setBackground(getColorLight(color)); }
                    public void mouseExited(MouseEvent e) { noteItem.setBackground(Color.WHITE); }
                });
                detailsContent.add(noteItem);
                detailsContent.add(Box.createVerticalStrut(8));
            }
        }

        detailsContent.revalidate();
        detailsContent.repaint();
    }

    private RoundedPanel createStatBox(String value, String label) {
        RoundedPanel box = new RoundedPanel(6, ColorScheme.BORDER, false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 8, 10, 8));
        box.setBackground(ColorScheme.BG_APP);
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 16));
        valLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        valLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 10));
        lblLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        lblLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(valLabel);
        box.add(Box.createVerticalStrut(2));
        box.add(lblLabel);
        return box;
    }

    // ═══════════════════════════════════════════════
    //  VIEW TOGGLE + DATA REFRESH
    // ═══════════════════════════════════════════════
    private JButton createToggleBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        btn.setForeground(active ? ColorScheme.PRIMARY_BLUE : ColorScheme.TEXT_SECONDARY);
        btn.setBackground(active ? ColorScheme.BG_APP : Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(80, 28));
        return btn;
    }

    private void switchView(String mode) {
        viewMode = mode;
        orgBtn.setBackground(mode.equals("org") ? ColorScheme.BG_APP : Color.WHITE);
        orgBtn.setForeground(mode.equals("org") ? ColorScheme.PRIMARY_BLUE : ColorScheme.TEXT_SECONDARY);
        treeBtn.setBackground(mode.equals("tree") ? ColorScheme.BG_APP : Color.WHITE);
        treeBtn.setForeground(mode.equals("tree") ? ColorScheme.PRIMARY_BLUE : ColorScheme.TEXT_SECONDARY);
        if (mode.equals("tree")) rebuildTreeView();
        viewCards.show(viewPanel, mode);
    }

    public void refreshData() {
        new SwingWorker<ApiModels.GraphResponse, Void>() {
            @Override
            protected ApiModels.GraphResponse doInBackground() throws Exception {
                String wsId = ApiClient.get().getWorkspaceId();
                if (wsId == null) return null;
                return ApiClient.get().getGraph(wsId);
            }
            @Override
            protected void done() {
                try {
                    // Get workspace name
                    try {
                        List<ApiModels.WorkspaceDto> wss = ApiClient.get().getWorkspaces();
                        String wsId = ApiClient.get().getWorkspaceId();
                        workspaceName = wss.stream().filter(w -> w.id.equals(wsId)).findFirst().map(w -> w.name).orElse("My");
                    } catch (Exception ignored) {}

                    ApiModels.GraphResponse resp = get();
                    if (resp == null) { branches = new ArrayList<>(); } 
                    else { branches = resp.branches != null ? resp.branches : new ArrayList<>(); }

                    // Auto-expand all tags
                    for (ApiModels.GraphBranch b : branches) expandedTags.putIfAbsent(b.tagId, true);

                    // Refresh both views
                    mainContent.revalidate();
                    mainContent.repaint();
                    if (viewMode.equals("tree")) rebuildTreeView();
                    if (!branches.isEmpty() && selectedBranch == null) {
                        selectedBranch = branches.get(0);
                        updateDetailsSidebar();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }
}
