package com.notegraph.ui.layout;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Top bar matching the React TopBar.jsx — search field + user avatar.
 */
public class TopBarPanel extends JPanel {

    private JTextField searchField;
    private JPopupMenu searchPopup;
    private Consumer<String> navigateTo;

    public TopBarPanel(Consumer<String> navigateTo) {
        this.navigateTo = navigateTo;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_SURFACE);
        setPreferredSize(new Dimension(0, ColorScheme.TOPBAR_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));
        buildUI();
        setupSearch();
    }

    private void buildUI() {
        // Search 
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel(new VectorIcon(VectorIcon.Type.SEARCH, 18, ColorScheme.TEXT_SECONDARY));
        searchField = new JTextField(30);
        searchField.setFont(ColorScheme.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setBackground(ColorScheme.BG_APP);
        searchBox.add(searchIcon);
        searchBox.add(searchField);
        add(searchBox, BorderLayout.WEST);

        // User avatar
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        userArea.setOpaque(false);
        JLabel bellIcon = new JLabel(new VectorIcon(VectorIcon.Type.BELL, 20, ColorScheme.TEXT_SECONDARY));
        bellIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(TopBarPanel.this, 
                    "You have no new notifications.", 
                    "Notifications", JOptionPane.INFORMATION_MESSAGE);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                bellIcon.setIcon(new VectorIcon(VectorIcon.Type.BELL, 20, ColorScheme.PRIMARY_BLUE));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                bellIcon.setIcon(new VectorIcon(VectorIcon.Type.BELL, 20, ColorScheme.TEXT_SECONDARY));
            }
        });

        String name = ApiClient.get().getUser() != null ? ApiClient.get().getUser().name : "User";
        String initials = getInitials(name);
        
        JLabel avatar = new JLabel(initials) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(55, 65, 81));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String currentName = ApiClient.get().getUser() != null ? ApiClient.get().getUser().name : "User";
                JOptionPane.showMessageDialog(TopBarPanel.this,
                        "Name: " + currentName + "\nRole: Administrator\nPlan: Pro Plan",
                        "User Profile", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        userArea.add(bellIcon);
        userArea.add(avatar);
        add(userArea, BorderLayout.EAST);
    }

    private void setupSearch() {
        searchPopup = new JPopupMenu();
        searchPopup.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER));
        searchPopup.setBackground(ColorScheme.BG_SURFACE);
        searchPopup.setFocusable(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            Timer timer = new Timer(300, e -> performSearch());
            { timer.setRepeats(false); }
            @Override public void insertUpdate(DocumentEvent e) { timer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { timer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { timer.restart(); }
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty() || ApiClient.get().getWorkspaceId() == null) {
            searchPopup.setVisible(false);
            return;
        }

        SwingWorker<List<ApiModels.Note>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ApiModels.Note> doInBackground() throws Exception {
                return ApiClient.get().getWorkspaceNotes(ApiClient.get().getWorkspaceId());
            }

            @Override
            protected void done() {
                try {
                    List<ApiModels.Note> allNotes = get();
                    List<ApiModels.Note> filtered = allNotes.stream()
                            .filter(n -> n.title.toLowerCase().contains(query) || (n.content != null && n.content.toLowerCase().contains(query)))
                            .limit(6)
                            .collect(Collectors.toList());

                    showSearchResults(filtered);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showSearchResults(List<ApiModels.Note> results) {
        searchPopup.removeAll();
        if (results.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("No results found");
            emptyItem.setEnabled(false);
            searchPopup.add(emptyItem);
        } else {
            for (ApiModels.Note note : results) {
                JMenuItem item = new JMenuItem(note.title, new VectorIcon(VectorIcon.Type.BOOK, 14, ColorScheme.TEXT_SECONDARY));
                item.addActionListener(e -> {
                    searchPopup.setVisible(false);
                    searchField.setText("");
                    navigateTo.accept("editor:" + note.id);
                });
                searchPopup.add(item);
            }
        }
        
        if (searchField.isShowing() && !results.isEmpty()) {
            if (!searchPopup.isVisible()) {
                searchPopup.show(searchField, 0, searchField.getHeight());
            } else {
                searchPopup.pack();
            }
            searchField.requestFocus();
        } else if (results.isEmpty()) {
            searchPopup.setVisible(false);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "US";
        String[] parts = name.split(" ");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
