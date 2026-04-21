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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Favorites page matching FavoritesPage.jsx.
 */
public class FavoritesPanel extends JPanel {

    private Consumer<String> onNavigate;
    private JPanel favGrid;
    private JTextField searchField;
    private List<ApiModels.Note> favorites = new ArrayList<>();

    public FavoritesPanel(Consumer<String> onNavigate) {
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
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);
        JLabel title = new JLabel("⭐ Favorites");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Your most important notes, one click away.");
        subtitle.setFont(ColorScheme.FONT_REGULAR);
        subtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        titleGroup.add(title);
        titleGroup.add(subtitle);
        headerRow.add(titleGroup, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField(18);
        searchField.setFont(ColorScheme.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterFavs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterFavs(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterFavs(); }
        });
        searchBox.add(searchIcon);
        searchBox.add(searchField);
        headerRow.add(searchBox, BorderLayout.EAST);
        content.add(headerRow);
        content.add(Box.createVerticalStrut(20));

        // Grid
        favGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        favGrid.setOpaque(false);
        favGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(favGrid);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private RoundedPanel createFavCard(ApiModels.Note note) {
        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header: tag + star + delete
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (note.tags != null && !note.tags.isEmpty()) {
            JLabel tagBadge = new JLabel(note.tags.get(0).name);
            tagBadge.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 11));
            tagBadge.setForeground(ColorScheme.TAG_BLUE_TEXT);
            tagBadge.setBackground(ColorScheme.TAG_BLUE_BG);
            tagBadge.setOpaque(true);
            tagBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
            headerRow.add(tagBadge, BorderLayout.WEST);
        } else {
            JLabel uncatBadge = new JLabel("Uncategorized");
            uncatBadge.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 11));
            uncatBadge.setForeground(ColorScheme.TEXT_SECONDARY);
            uncatBadge.setBackground(ColorScheme.TAG_GRAY_BG);
            uncatBadge.setOpaque(true);
            uncatBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
            headerRow.add(uncatBadge, BorderLayout.WEST);
        }

        JPanel btnsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnsPanel.setOpaque(false);

        JLabel deleteBtn = new JLabel("🗑");
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                e.consume();
                if (ConfirmDialog.show(FavoritesPanel.this, "Delete Note", "Delete this note permanently?")) {
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            ApiClient.get().deleteNote(note.id); return null;
                        }
                        @Override protected void done() { refreshData(); }
                    }.execute();
                }
            }
        });

        JLabel starBtn = new JLabel("★");
        starBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        starBtn.setForeground(ColorScheme.STAR_COLOR);
        starBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        starBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                e.consume();
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() throws Exception {
                        ApiClient.get().toggleFavorite(note.id); return null;
                    }
                    @Override protected void done() { refreshData(); }
                }.execute();
            }
        });

        btnsPanel.add(deleteBtn);
        btnsPanel.add(starBtn);
        headerRow.add(btnsPanel, BorderLayout.EAST);
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
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel typeLabel = new JLabel("📄 Note");
        typeLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        typeLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        JLabel dateLabel = new JLabel(formatDate(note.updatedAt));
        dateLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 11));
        dateLabel.setForeground(ColorScheme.TEXT_TERTIARY);
        footer.add(typeLabel, BorderLayout.WEST);
        footer.add(dateLabel, BorderLayout.EAST);
        card.add(footer);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onNavigate.accept("editor:" + note.id); }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(252, 253, 255)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    public void refreshData() {
        new SwingWorker<List<ApiModels.Note>, Void>() {
            @Override protected List<ApiModels.Note> doInBackground() throws Exception {
                String wsId = ApiClient.get().getWorkspaceId();
                if (wsId == null) return new ArrayList<>();
                List<ApiModels.Note> all = ApiClient.get().getWorkspaceNotes(wsId);
                return all.stream().filter(n -> Boolean.TRUE.equals(n.favorite)).toList();
            }
            @Override protected void done() {
                try {
                    favorites = get();
                    filterFavs();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void filterFavs() {
        String query = searchField.getText().toLowerCase();
        favGrid.removeAll();
        int count = 0;
        for (ApiModels.Note fav : favorites) {
            boolean matches = query.isEmpty()
                    || (fav.title != null && fav.title.toLowerCase().contains(query))
                    || (fav.tags != null && fav.tags.stream().anyMatch(t -> t.name.toLowerCase().contains(query)));
            if (matches) {
                favGrid.add(createFavCard(fav));
                count++;
            }
        }
        if (count == 0) {
            JPanel emptyState = new JPanel();
            emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
            emptyState.setOpaque(false);
            emptyState.setBorder(new EmptyBorder(40, 0, 0, 0));
            JLabel emptyIcon = new JLabel("⭐");
            emptyIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel emptyTitle = new JLabel("No Favorites Found");
            emptyTitle.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 18));
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel emptyDesc = new JLabel("Click the star icon on any note to add it here.");
            emptyDesc.setFont(ColorScheme.FONT_REGULAR);
            emptyDesc.setForeground(ColorScheme.TEXT_SECONDARY);
            emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyState.add(emptyIcon);
            emptyState.add(Box.createVerticalStrut(12));
            emptyState.add(emptyTitle);
            emptyState.add(Box.createVerticalStrut(6));
            emptyState.add(emptyDesc);
            favGrid.add(emptyState);
        }
        favGrid.revalidate();
        favGrid.repaint();
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(dateStr.substring(0, Math.min(19, dateStr.length())));
            return dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) { return dateStr; }
    }
}
