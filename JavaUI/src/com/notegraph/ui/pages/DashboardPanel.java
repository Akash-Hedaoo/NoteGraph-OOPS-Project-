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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard overview page matching DashboardOverview.jsx.
 * Shows metric cards, recent notes, and activity feed.
 */
public class DashboardPanel extends JPanel {

    private Consumer<String> onNavigate;
    private List<ApiModels.Note> notes = new ArrayList<>();
    private List<ApiModels.ActivityLog> activities = new ArrayList<>();
    private JPanel metricsPanel;
    private JPanel notesGrid;
    private JPanel activityList;

    public DashboardPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Header
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        String userName = ApiClient.get().getUser() != null ? ApiClient.get().getUser().name.split(" ")[0] : "User";
        JLabel subtitle = new JLabel("Welcome back, " + userName + "! Here's what's happening with your notes today.");
        subtitle.setFont(ColorScheme.FONT_REGULAR);
        subtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        // Metrics
        metricsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        metricsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricsPanel.add(createMetricCard("Total Notes", "...", "📄"));
        metricsPanel.add(createMetricCard("Recent Edits", "...", "✏️"));
        metricsPanel.add(createMetricCard("Favorites", "...", "🔗"));
        metricsPanel.add(createMetricCard("Archived", "0", "📦"));
        content.add(metricsPanel);
        content.add(Box.createVerticalStrut(24));

        // Main content: notes + activity
        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);
        main.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Recent Notes
        JPanel notesSection = new JPanel();
        notesSection.setLayout(new BoxLayout(notesSection, BoxLayout.Y_AXIS));
        notesSection.setOpaque(false);

        JPanel notesHeader = new JPanel(new BorderLayout());
        notesHeader.setOpaque(false);
        notesHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel notesTitle = new JLabel("Recent Notes");
        notesTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 18));
        JLabel viewAll = new JLabel("View All");
        viewAll.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 13));
        viewAll.setForeground(ColorScheme.PRIMARY_BLUE);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onNavigate.accept("hierarchy"); }
        });
        notesHeader.add(notesTitle, BorderLayout.WEST);
        notesHeader.add(viewAll, BorderLayout.EAST);
        notesSection.add(notesHeader);
        notesSection.add(Box.createVerticalStrut(12));

        notesGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        notesGrid.setOpaque(false);
        notesSection.add(notesGrid);
        main.add(notesSection, BorderLayout.CENTER);

        // Activity sidebar
        RoundedPanel activityPanel = new RoundedPanel(12, ColorScheme.BORDER, false);
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        activityPanel.setPreferredSize(new Dimension(320, 0));

        JLabel actTitle = new JLabel("Activity Summary");
        actTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 16));
        actTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityPanel.add(actTitle);
        activityPanel.add(Box.createVerticalStrut(16));

        activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setOpaque(false);
        JLabel loadingAct = new JLabel("No recent activity.");
        loadingAct.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        loadingAct.setForeground(ColorScheme.TEXT_SECONDARY);
        activityList.add(loadingAct);
        activityPanel.add(activityList);
        activityPanel.add(Box.createVerticalGlue());

        JButton viewHistBtn = new JButton("View full history");
        viewHistBtn.setFont(ColorScheme.FONT_REGULAR);
        viewHistBtn.setForeground(ColorScheme.TEXT_SECONDARY);
        viewHistBtn.setBackground(ColorScheme.BG_APP);
        viewHistBtn.setBorderPainted(false);
        viewHistBtn.setFocusPainted(false);
        viewHistBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewHistBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        activityPanel.add(viewHistBtn);

        main.add(activityPanel, BorderLayout.EAST);
        content.add(main);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private RoundedPanel createMetricCard(String title, String value, String icon) {
        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 13));
        titleLbl.setForeground(ColorScheme.TEXT_SECONDARY);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl, BorderLayout.EAST);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 28));
        valueLbl.setForeground(ColorScheme.TEXT_PRIMARY);
        valueLbl.setBorder(new EmptyBorder(8, 0, 0, 0));

        card.add(top, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createNoteCard(ApiModels.Note note) {
        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tag + Delete
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        if (note.tags != null && !note.tags.isEmpty()) {
            JLabel tagBadge = new JLabel(note.tags.get(0).name);
            tagBadge.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 11));
            tagBadge.setForeground(ColorScheme.TAG_BLUE_TEXT);
            tagBadge.setBackground(ColorScheme.TAG_BLUE_BG);
            tagBadge.setOpaque(true);
            tagBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
            headerRow.add(tagBadge, BorderLayout.WEST);
        }
        JLabel deleteBtn = new JLabel("🗑");
        deleteBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                e.consume();
                if (ConfirmDialog.show(DashboardPanel.this, "Delete Note", "Are you sure you want to delete this note?")) {
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            ApiClient.get().deleteNote(note.id); return null;
                        }
                        @Override protected void done() { refreshData(); }
                    }.execute();
                }
            }
        });
        headerRow.add(deleteBtn, BorderLayout.EAST);
        card.add(headerRow);
        card.add(Box.createVerticalStrut(10));

        // Title
        JLabel titleLbl = new JLabel(note.title != null && !note.title.isEmpty() ? note.title : "Untitled Note");
        titleLbl.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
        titleLbl.setForeground(ColorScheme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));

        // Snippet
        String snippet = stripHtml(note.content);
        if (snippet.length() > 80) snippet = snippet.substring(0, 80) + "...";
        JLabel snippetLbl = new JLabel("<html><body style='width: 180px'>" + snippet + "</body></html>");
        snippetLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        snippetLbl.setForeground(ColorScheme.TEXT_SECONDARY);
        snippetLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(snippetLbl);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(10));

        // Footer
        JLabel timeLabel = new JLabel("Updated " + formatDate(note.updatedAt));
        timeLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 11));
        timeLabel.setForeground(ColorScheme.TEXT_TERTIARY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(timeLabel);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() != deleteBtn) onNavigate.accept("editor:" + note.id);
            }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(252, 253, 255)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    private RoundedPanel createNewNoteCard() {
        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createDashedBorder(ColorScheme.BORDER, 4.0f, 4.0f));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        JLabel plusIcon = new JLabel("＋");
        plusIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        plusIcon.setForeground(ColorScheme.PRIMARY_BLUE);
        plusIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel createLabel = new JLabel("Create New Note");
        createLabel.setFont(ColorScheme.FONT_MEDIUM);
        createLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        createLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(plusIcon);
        center.add(Box.createVerticalStrut(10));
        center.add(createLabel);
        card.add(center);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onNavigate.accept("editor"); }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(ColorScheme.TAG_BLUE_BG);
            }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    public void refreshData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String wsId = ApiClient.get().getWorkspaceId();
                String userId = ApiClient.get().getUser() != null ? ApiClient.get().getUser().id : null;
                if (wsId == null || userId == null) return null;
                notes = ApiClient.get().getWorkspaceNotes(wsId);
                try { activities = ApiClient.get().getUserActivity(userId); } catch (Exception e) { /* ok */ }
                return null;
            }
            @Override
            protected void done() {
                // Update metrics
                metricsPanel.removeAll();
                metricsPanel.add(createMetricCard("Total Notes", String.valueOf(notes.size()), "📄"));
                long edits = activities.stream().filter(a -> "edited".equals(a.action)).count();
                metricsPanel.add(createMetricCard("Recent Edits", String.valueOf(edits), "✏️"));
                long favs = notes.stream().filter(n -> Boolean.TRUE.equals(n.favorite)).count();
                metricsPanel.add(createMetricCard("Favorites", String.valueOf(favs), "🔗"));
                metricsPanel.add(createMetricCard("Archived", "0", "📦"));
                metricsPanel.revalidate();

                // Update notes grid
                notesGrid.removeAll();
                List<ApiModels.Note> displayNotes = notes.size() > 3 ? notes.subList(0, 3) : notes;
                for (ApiModels.Note n : displayNotes) {
                    notesGrid.add(createNoteCard(n));
                }
                notesGrid.add(createNewNoteCard());
                notesGrid.revalidate();
                notesGrid.repaint();

                // Update activity
                activityList.removeAll();
                if (activities.isEmpty()) {
                    JLabel empty = new JLabel("No recent activity.");
                    empty.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
                    empty.setForeground(ColorScheme.TEXT_SECONDARY);
                    activityList.add(empty);
                } else {
                    for (ApiModels.ActivityLog log : activities) {
                        activityList.add(createActivityItem(log));
                        activityList.add(Box.createVerticalStrut(12));
                    }
                }
                activityList.revalidate();
                activityList.repaint();
            }
        }.execute();
    }

    private JPanel createActivityItem(ApiModels.ActivityLog log) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 8));
        dot.setForeground("edited".equals(log.action) ? ColorScheme.SUCCESS_TEXT : ColorScheme.PRIMARY_BLUE);
        JLabel text = new JLabel("<html><b>" + (log.user != null ? log.user.name : "You") + "</b> " 
                + log.action + " <span style='color:#0A66F0'>" + (log.targetName != null ? log.targetName : "") + "</span></html>");
        text.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        text.setForeground(ColorScheme.TEXT_SECONDARY);
        item.add(dot);
        item.add(text);
        return item;
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&");
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "Just now";
        try {
            LocalDateTime dt = LocalDateTime.parse(dateStr.substring(0, Math.min(19, dateStr.length())));
            long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
            if (days == 0) return "Today";
            if (days == 1) return "Yesterday";
            if (days < 7) return days + " days ago";
            return dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}
