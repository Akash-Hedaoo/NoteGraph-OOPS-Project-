package com.notegraph.ui.pages;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.ConfirmDialog;
import com.notegraph.ui.components.RoundedPanel;
import com.notegraph.ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tags management page matching TagsPage.jsx.
 */
public class TagsPanel extends JPanel {

    private JPanel tagsGrid;
    private JTextField searchField;
    private JLabel countLabel;
    private List<ApiModels.Tag> allTags = new ArrayList<>();
    private static final String[] COLORS = {"blue", "purple", "green", "orange", "gray", "red"};

    public TagsPanel() {
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
        JLabel title = new JLabel("Tags Management");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Organize and manage your note tags across all workspaces.");
        subtitle.setFont(ColorScheme.FONT_REGULAR);
        subtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        titleGroup.add(title);
        titleGroup.add(subtitle);
        headerRow.add(titleGroup, BorderLayout.WEST);

        JButton createBtn = new JButton("+ Create New Tag");
        createBtn.setFont(ColorScheme.FONT_SEMIBOLD);
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(ColorScheme.PRIMARY_BLUE);
        createBtn.setBorderPainted(false);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> showCreateTagDialog());
        createBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { createBtn.setBackground(ColorScheme.PRIMARY_BLUE_HOVER); }
            public void mouseExited(MouseEvent e) { createBtn.setBackground(ColorScheme.PRIMARY_BLUE); }
        });
        headerRow.add(createBtn, BorderLayout.EAST);
        content.add(headerRow);
        content.add(Box.createVerticalStrut(20));

        // Tags card
        RoundedPanel tagsCard = new RoundedPanel(12, ColorScheme.BORDER, false);
        tagsCard.setLayout(new BoxLayout(tagsCard, BoxLayout.Y_AXIS));
        tagsCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        tagsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel(new VectorIcon(VectorIcon.Type.SEARCH, 18, ColorScheme.TEXT_SECONDARY));
        searchField = new JTextField(20);
        searchField.setFont(ColorScheme.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTags(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTags(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTags(); }
        });
        searchBox.add(searchIcon);
        searchBox.add(searchField);
        toolbar.add(searchBox, BorderLayout.WEST);

        countLabel = new JLabel("0 Total Tags");
        countLabel.setFont(ColorScheme.FONT_REGULAR);
        countLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        toolbar.add(countLabel, BorderLayout.EAST);
        tagsCard.add(toolbar);
        tagsCard.add(Box.createVerticalStrut(16));

        // Grid
        tagsGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        tagsGrid.setOpaque(false);
        tagsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagsCard.add(tagsGrid);

        content.add(tagsCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private RoundedPanel createTagCard(ApiModels.Tag tag, int index) {
        String color = tag.color != null ? tag.color : COLORS[index % COLORS.length];
        RoundedPanel card = new RoundedPanel(10, ColorScheme.BORDER, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header: tag badge + menu
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pillPanel.setOpaque(false);
        JLabel dot = new JLabel(new VectorIcon(VectorIcon.Type.DOT, 10, ColorScheme.tagFg(color)));
        JLabel nameLabel = new JLabel("#" + tag.name);
        nameLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        nameLabel.setForeground(ColorScheme.tagFg(color));
        pillPanel.add(dot);
        pillPanel.add(nameLabel);
        headerRow.add(pillPanel, BorderLayout.WEST);

        card.add(headerRow);
        card.add(Box.createVerticalStrut(12));

        // Stats
        JLabel statLabel = new JLabel("System managed tag");
        statLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        statLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        statLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(12));

        // Actions
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsRow.setOpaque(false);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton editBtn = new JButton("Edit", new VectorIcon(VectorIcon.Type.EDIT, 14, ColorScheme.TEXT_SECONDARY));
        editBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        editBtn.setForeground(ColorScheme.TEXT_SECONDARY);
        editBtn.setBorderPainted(false);
        editBtn.setFocusPainted(false);
        editBtn.setBackground(null);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> showEditTagDialog(tag));
        editBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { editBtn.setForeground(ColorScheme.PRIMARY_BLUE); }
            public void mouseExited(MouseEvent e) { editBtn.setForeground(ColorScheme.TEXT_SECONDARY); }
        });

        JButton deleteBtn = new JButton("Delete", new VectorIcon(VectorIcon.Type.TRASH, 14, ColorScheme.TEXT_SECONDARY));
        deleteBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        deleteBtn.setForeground(ColorScheme.TEXT_SECONDARY);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBackground(null);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteTag(tag));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { deleteBtn.setForeground(ColorScheme.ERROR_TEXT); }
            public void mouseExited(MouseEvent e) { deleteBtn.setForeground(ColorScheme.TEXT_SECONDARY); }
        });

        actionsRow.add(editBtn);
        actionsRow.add(deleteBtn);
        card.add(actionsRow);

        return card;
    }

    private void showCreateTagDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Tag Name:");
        label.setFont(ColorScheme.FONT_SEMIBOLD);
        JTextField nameField = new JTextField(20);
        nameField.setFont(ColorScheme.FONT_REGULAR);
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setFont(ColorScheme.FONT_SEMIBOLD);
        JComboBox<String> colorBox = new JComboBox<>(COLORS);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(colorLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(colorBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Tag", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ApiModels.Tag tag = new ApiModels.Tag();
                    tag.name = nameField.getText().trim();
                    tag.color = (String) colorBox.getSelectedItem();
                    tag.workspace = new ApiModels.IdRef(ApiClient.get().getWorkspaceId());
                    ApiClient.get().createTag(tag);
                    return null;
                }
                @Override protected void done() { refreshData(); }
            }.execute();
        }
    }

    private void showEditTagDialog(ApiModels.Tag tag) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Tag Name:");
        label.setFont(ColorScheme.FONT_SEMIBOLD);
        JTextField nameField = new JTextField(tag.name, 20);
        nameField.setFont(ColorScheme.FONT_REGULAR);
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setFont(ColorScheme.FONT_SEMIBOLD);
        JComboBox<String> colorBox = new JComboBox<>(COLORS);
        colorBox.setSelectedItem(tag.color != null ? tag.color : "blue");
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(colorLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(colorBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Tag", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    tag.name = nameField.getText().trim();
                    tag.color = (String) colorBox.getSelectedItem();
                    ApiClient.get().updateTag(tag.id, tag);
                    return null;
                }
                @Override protected void done() { refreshData(); }
            }.execute();
        }
    }

    private void deleteTag(ApiModels.Tag tag) {
        if (ConfirmDialog.show(this, "Delete Tag", "Are you sure? Connections to notes will be lost.")) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ApiClient.get().deleteTag(tag.id); return null;
                }
                @Override protected void done() { refreshData(); }
            }.execute();
        }
    }

    public void refreshData() {
        new SwingWorker<List<ApiModels.Tag>, Void>() {
            @Override protected List<ApiModels.Tag> doInBackground() throws Exception {
                String wsId = ApiClient.get().getWorkspaceId();
                if (wsId == null) return new ArrayList<>();
                return ApiClient.get().getWorkspaceTags(wsId);
            }
            @Override protected void done() {
                try {
                    allTags = get();
                    countLabel.setText(allTags.size() + " Total Tags");
                    filterTags();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void filterTags() {
        String query = searchField.getText().toLowerCase();
        tagsGrid.removeAll();
        int idx = 0;
        for (ApiModels.Tag tag : allTags) {
            if (query.isEmpty() || tag.name.toLowerCase().contains(query)) {
                tagsGrid.add(createTagCard(tag, idx));
                idx++;
            }
        }
        if (idx == 0) {
            JLabel empty = new JLabel("No tags found. Create one to get started!");
            empty.setFont(ColorScheme.FONT_REGULAR);
            empty.setForeground(ColorScheme.TEXT_SECONDARY);
            tagsGrid.add(empty);
        }
        tagsGrid.revalidate();
        tagsGrid.repaint();
    }
}
