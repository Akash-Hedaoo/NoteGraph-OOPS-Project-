package com.notegraph.frontend.ui;

import com.notegraph.frontend.model.NoteDto;
import com.notegraph.frontend.api.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DashboardOverviewPanel extends JPanel {
    private DashboardFrame parentFrame;
    private JPanel statsPanel;
    private JPanel cardsGrid;
    private JPanel activityCard;

    public DashboardOverviewPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Top Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        JLabel subtitleLabel = new JLabel("Welcome back! Here's what's happening with your notes today.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // Stats Row
        statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(UIManager.getColor("Panel.background"));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        // Will be populated in loadRealData()

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(UIManager.getColor("Panel.background"));
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // Main Content (Recent notes + Activity Sidebar)
        JPanel contentPanel = new JPanel(new BorderLayout(30, 0));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // "Recent Notes" Grid
        JPanel recentNotesContainer = new JPanel(new BorderLayout());
        recentNotesContainer.setBackground(UIManager.getColor("Panel.background"));

        JPanel recentNotesHeader = new JPanel(new BorderLayout());
        recentNotesHeader.setBackground(UIManager.getColor("Panel.background"));
        recentNotesHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        JLabel recentNotesTitle = new JLabel("Recent Notes");
        recentNotesTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentNotesTitle.setForeground(UIManager.getColor("Label.foreground"));
        JLabel viewAllLabel = new JLabel("View All");
        viewAllLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        viewAllLabel.setForeground(new Color(24, 119, 242));
        viewAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.showAllNotes();
            }
        });
        recentNotesHeader.add(recentNotesTitle, BorderLayout.WEST);
        recentNotesHeader.add(viewAllLabel, BorderLayout.EAST);

        recentNotesContainer.add(recentNotesHeader, BorderLayout.NORTH);

        cardsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsGrid.setBackground(UIManager.getColor("Panel.background"));

        // Will be populated in loadRealData()

        recentNotesContainer.add(cardsGrid, BorderLayout.CENTER);

        // Activity Summary (East)
        JPanel rightSidebar = new JPanel();
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setBackground(UIManager.getColor("Panel.background"));
        rightSidebar.setPreferredSize(new Dimension(320, 0));

        activityCard = new JPanel();
        activityCard.setLayout(new BoxLayout(activityCard, BoxLayout.Y_AXIS));
        activityCard.setBackground(UIManager.getColor("Panel.background"));
        activityCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Will be populated in loadRealData()

        rightSidebar.add(activityCard);
        rightSidebar.add(Box.createVerticalStrut(20));

        // Quick Access
        JPanel quickAccessCard = new JPanel();
        quickAccessCard.setLayout(new BoxLayout(quickAccessCard, BoxLayout.Y_AXIS));
        quickAccessCard.setBackground(UIManager.getColor("Panel.background"));
        quickAccessCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel qaHeader = new JPanel(new BorderLayout());
        qaHeader.setBackground(UIManager.getColor("Panel.background"));
        JLabel qaTitle = new JLabel("Quick Access");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        qaTitle.setForeground(UIManager.getColor("Label.foreground"));
        qaHeader.add(qaTitle, BorderLayout.WEST);
        quickAccessCard.add(qaHeader);
        quickAccessCard.add(Box.createVerticalStrut(15));

        quickAccessCard.add(createQuickAccessItem("\uD83D\uDCC1", "Project Beta Docs"));
        quickAccessCard.add(Box.createVerticalStrut(10));
        quickAccessCard.add(createQuickAccessItem("#\uFE0F\u20E3", "#Ideas"));

        rightSidebar.add(quickAccessCard);

        contentPanel.add(recentNotesContainer, BorderLayout.CENTER);
        contentPanel.add(rightSidebar, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);

        loadRealData();
    }

    public void loadRealData() {
        List<NoteDto> notes = null;
        try {
            notes = ApiClient.fetchAllNotes();
        } catch (Exception e) {
        }

        if (notes == null)
            notes = new java.util.ArrayList<>();

        // UPDATE STATS
        statsPanel.removeAll();
        statsPanel.add(createStatCard("Total Notes", String.valueOf(notes.size()), "+5%", new Color(24, 119, 242)));
        statsPanel.add(createStatCard("Recent Edits", String.valueOf(Math.min(notes.size(), 12)), "+2%",
                new Color(153, 50, 204)));
        statsPanel.add(createStatCard("Shared Notes", "5", "0%", new Color(255, 122, 89)));
        statsPanel.add(createStatCard("Archived", "142", null, Color.GRAY));
        statsPanel.revalidate();
        statsPanel.repaint();

        // UPDATE RECENT NOTES GRID
        updateCardsGrid(notes);

        // UPDATE ACTIVITY SUMMARY
        activityCard.removeAll();
        JLabel activityTitle = new JLabel("Activity Summary");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activityTitle.setForeground(UIManager.getColor("Label.foreground"));
        activityCard.add(activityTitle);
        activityCard.add(Box.createVerticalStrut(20));

        int maxActivities = Math.min(4, notes.size());
        if (maxActivities == 0) {
            JLabel emptyLbl = new JLabel("No recent activity.");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLbl.setForeground(Color.GRAY);
            activityCard.add(emptyLbl);
        } else {
            for (int i = 0; i < maxActivities; i++) {
                NoteDto note = notes.get(notes.size() - 1 - i);
                String title = note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle()
                        : "Untitled Note";
                activityCard.add(createActivityItem("You updated", title, (i * 10 + 5) + " mins ago"));
                if (i < maxActivities - 1) {
                    activityCard.add(Box.createVerticalStrut(15));
                }
            }
        }

        JButton viewLogBtn = new JButton("View Activity Log");
        viewLogBtn.setBackground(UIManager.getColor("Panel.background"));
        viewLogBtn.setForeground(UIManager.getColor("Label.foreground"));
        viewLogBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewLogBtn.setFocusPainted(false);
        viewLogBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        viewLogBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        viewLogBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityCard.add(Box.createVerticalStrut(25));
        activityCard.add(viewLogBtn);

        activityCard.revalidate();
        activityCard.repaint();
    }

    private void updateCardsGrid(List<NoteDto> notes) {
        cardsGrid.removeAll();
        Color[] tagColors = { new Color(24, 119, 242), new Color(153, 50, 204), new Color(50, 180, 100),
                new Color(255, 122, 89) };
        String[] tags = { "Work", "Personal", "Project Alpha", "Docs" };

        int maxCards = Math.min(3, notes.size());
        for (int i = 0; i < maxCards; i++) {
            NoteDto note = notes.get(notes.size() - 1 - i);
            cardsGrid.add(createRealNoteCard(note, tags[i % tags.length], tagColors[i % tagColors.length]));
        }

        JPanel createCard = new JPanel(new GridBagLayout());
        createCard.setBackground(UIManager.getColor("Panel.background"));
        createCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        createCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel createLbl = new JLabel("+ Create New Note");
        createLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createLbl.setForeground(Color.GRAY);
        createCard.add(createLbl);
        createCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextNoteEditorPanel editor = parentFrame.getTextNoteEditorPanel();
                if (editor != null)
                    editor.clearEditor();
                parentFrame.showEditor(editor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                createCard.setBackground(UIManager.getColor("Panel.background").brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                createCard.setBackground(UIManager.getColor("Panel.background"));
            }
        });
        cardsGrid.add(createCard);

        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    public void filterNotes(String query) {
        List<NoteDto> notes = null;
        try {
            notes = ApiClient.fetchAllNotes();
        } catch (Exception e) {
        }

        if (notes == null)
            notes = new java.util.ArrayList<>();

        if (query == null || query.trim().isEmpty() || query.equals("search notes, tags, or folders...")) {
            updateCardsGrid(notes);
        } else {
            List<NoteDto> filtered = notes.stream().filter(n -> {
                boolean mt = n.getTitle() != null && n.getTitle().toLowerCase().contains(query);
                boolean mc = n.getContent() != null && n.getContent().toLowerCase().contains(query);
                return mt || mc;
            }).collect(java.util.stream.Collectors.toList());
            updateCardsGrid(filtered);
        }
    }

    private JPanel createStatCard(String title, String value, String change, Color iconColor) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLbl.setForeground(Color.GRAY);
        card.add(titleLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel valPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valPanel.setBackground(UIManager.getColor("Panel.background"));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(UIManager.getColor("Label.foreground"));
        valPanel.add(valLbl);

        if (change != null) {
            JLabel changeLbl = new JLabel("  " + change);
            changeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            changeLbl.setForeground(new Color(50, 180, 100));
            valPanel.add(changeLbl);
        }
        card.add(valPanel, gbc);

        return card;
    }

    private JPanel createRealNoteCard(NoteDto note, String tag, Color tagColor) {
        String title = note.getTitle() != null && !note.getTitle().isEmpty() ? note.getTitle() : "Untitled Note";
        String content = note.getContent() != null ? note.getContent() : "";
        if (content.length() > 80)
            content = content.substring(0, 80).replace("\n", " ") + "...";
        else
            content = content.replace("\n", " ");

        JPanel card = createNoteCard(tag, title, content, "Recently Updated", tagColor);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextNoteEditorPanel editor = parentFrame.getTextNoteEditorPanel();
                editor.loadNote(note);
                parentFrame.showEditor(editor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(UIManager.getColor("Panel.background").brighter());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 122, 89), 1),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(UIManager.getColor("Panel.background"));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }
        });
        return card;
    }

    private JPanel createNoteCard(String tag, String title, String preview, String date, Color tagColor) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tag Pill
        JLabel tagPill = new JLabel(tag);
        tagPill.setOpaque(true);
        tagPill.setBackground(new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), 30));
        tagPill.setForeground(tagColor);
        tagPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagPill.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel pillWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pillWrapper.setOpaque(false);
        pillWrapper.add(tagPill);
        card.add(pillWrapper, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 5, 0);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(UIManager.getColor("Label.foreground"));
        card.add(titleLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        JTextArea previewArea = new JTextArea(preview);
        previewArea.setWrapStyleWord(true);
        previewArea.setLineWrap(true);
        previewArea.setEditable(false);
        previewArea.setFocusable(false);
        previewArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewArea.setForeground(Color.GRAY);
        previewArea.setBackground(UIManager.getColor("Panel.background"));
        card.add(previewArea, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(Color.LIGHT_GRAY);
        card.add(dateLbl, gbc);

        return card;
    }

    private JPanel createActivityItem(String action, String target, String time) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dot = new JLabel("\u2022 "); // bullet
        dot.setFont(new Font("Segoe UI", Font.BOLD, 24));
        dot.setForeground(new Color(24, 119, 242));
        panel.add(dot, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(UIManager.getColor("Panel.background"));
        String htmlDesc = "<html><font color='gray'>" + action + " </font><font color='#1877f2'>" + target
                + "</font></html>";
        JLabel desc = new JLabel(htmlDesc);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textPanel.add(desc);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(Color.LIGHT_GRAY);
        textPanel.add(timeLbl);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createQuickAccessItem(String icon, String title) {
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setBackground(UIManager.getColor("Panel.background"));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        wrapper.add(iconLbl, BorderLayout.WEST);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLbl.setForeground(UIManager.getColor("Label.foreground"));
        wrapper.add(titleLbl, BorderLayout.CENTER);

        JLabel arrowLbl = new JLabel("\u2192");
        arrowLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        arrowLbl.setForeground(Color.LIGHT_GRAY);
        wrapper.add(arrowLbl, BorderLayout.EAST);

        wrapper.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return wrapper;
    }
}
