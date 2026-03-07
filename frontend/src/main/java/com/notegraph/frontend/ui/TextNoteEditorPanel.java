package com.notegraph.frontend.ui;

import javax.swing.*;
import java.awt.*;
import com.notegraph.frontend.api.ApiClient;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

// Demonstrates Encapsulation: fields are private with public getters
public class TextNoteEditorPanel extends JPanel {

    private Long currentNoteId = null;
    private JTextField titleField;
    private JTextArea contentArea;
    private JEditorPane previewPane;
    private JTextField tagField;
    private JTextField connectField;

    public TextNoteEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        // --- Metadata Panel (Right Side) ---
        JPanel metadataPanel = new JPanel();
        metadataPanel.setLayout(new BoxLayout(metadataPanel, BoxLayout.Y_AXIS));
        metadataPanel.setBackground(UIManager.getColor("Panel.background"));
        metadataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        metadataPanel.setPreferredSize(new Dimension(280, 0)); // Fixed width, flexible height

        JLabel tagLabel = new JLabel("Note Tag:");
        tagLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tagLabel.setForeground(UIManager.getColor("Label.foreground"));
        tagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tagField = new JTextField();
        tagField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tagField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        tagField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel connectionsLabel = new JLabel("Connections:");
        connectionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        connectionsLabel.setForeground(UIManager.getColor("Label.foreground"));
        connectionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        connectField = new JTextField();
        connectField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        connectField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        connectField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        connectField.putClientProperty("JTextField.placeholderText", "Enter note titles to link");
        connectField.setAlignmentX(Component.LEFT_ALIGNMENT);

        metadataPanel.add(tagLabel);
        metadataPanel.add(Box.createVerticalStrut(5));
        metadataPanel.add(tagField);
        metadataPanel.add(Box.createVerticalStrut(25));
        metadataPanel.add(connectionsLabel);
        metadataPanel.add(Box.createVerticalStrut(5));
        metadataPanel.add(connectField);
        metadataPanel.add(Box.createVerticalGlue());

        // --- Title Area ---
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setBackground(UIManager.getColor("Panel.background"));

        titleField = new JTextField("Untitled Note");
        titleField.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleField.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 10));
        topToolbar.add(titleField, BorderLayout.WEST);

        JButton saveButton = new JButton("Save Note");
        saveButton.setFocusPainted(false);
        saveButton.setBackground(new Color(24, 119, 242));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setBorderPainted(false);
        // Demonstrates Client-Server Architecture and event-driven API calls
        saveButton.addActionListener(e -> {
            Long returnedId = ApiClient.saveTextNote(currentNoteId, titleField.getText(), contentArea.getText(),
                    tagField.getText());
            if (returnedId != null) {
                currentNoteId = returnedId;

                // Handle Links
                if (currentNoteId != null) {
                    processLinks(connectField.getText());
                } else {
                    // Refetch all to find our newly created note id to link against
                    try {
                        java.util.List<com.notegraph.frontend.model.NoteDto> allNotes = ApiClient.fetchAllNotes();
                        if (allNotes != null) {
                            for (com.notegraph.frontend.model.NoteDto n : allNotes) {
                                if (n.getTitle() != null && n.getTitle().equals(titleField.getText())) {
                                    currentNoteId = n.getId();
                                    break;
                                }
                            }
                            if (currentNoteId != null) {
                                processLinks(connectField.getText());
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                connectField.setText("");

                JOptionPane.showMessageDialog(this, "Note saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof DashboardFrame) {
                    DashboardFrame parent = (DashboardFrame) window;
                    parent.refreshSidebar();
                    // Force refresh of dashboard content
                    // Access overviewPanel via parent
                    DashboardOverviewPanel overview = null;
                    Component[] comps = parent.getMainCardPanel().getComponents();
                    for (Component c : comps) {
                        if (c instanceof DashboardOverviewPanel) {
                            overview = (DashboardOverviewPanel) c;
                            break;
                        }
                    }
                    if (overview != null) {
                        overview.loadRealData();
                    }
                    parent.getCardLayout().show(parent.getMainCardPanel(), "DASHBOARD");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save note.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonWrapper.setBackground(UIManager.getColor("Panel.background"));
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 30));

        JButton optionsBtn = new JButton("Options");
        optionsBtn.setFocusPainted(false);
        optionsBtn.setBackground(UIManager.getColor("Panel.background"));
        optionsBtn.setForeground(UIManager.getColor("Label.foreground"));
        optionsBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        optionsBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        optionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optionsBtn.addActionListener(e -> {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem renameItem = new JMenuItem("Rename...");
            renameItem.addActionListener(ev -> {
                String newTitle = JOptionPane.showInputDialog(this, "Enter new title:", titleField.getText());
                if (newTitle != null && !newTitle.trim().isEmpty()) {
                    titleField.setText(newTitle.trim());
                }
            });
            popupMenu.add(renameItem);

            JMenuItem findReplaceItem = new JMenuItem("Find / Replace...");
            findReplaceItem.addActionListener(ev -> {
                String target = JOptionPane.showInputDialog(this, "Find what:");
                if (target != null && !target.isEmpty()) {
                    String replacement = JOptionPane.showInputDialog(this, "Replace with:");
                    if (replacement != null) {
                        contentArea.setText(contentArea.getText().replace(target, replacement));
                    }
                }
            });
            popupMenu.add(findReplaceItem);

            JMenuItem copyItem = new JMenuItem("Copy note text");
            copyItem.addActionListener(ev -> {
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(contentArea.getText()), null);
                JOptionPane.showMessageDialog(this, "Copied!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });
            popupMenu.add(copyItem);

            JMenuItem exportPdfItem = new JMenuItem("Export to PDF...");
            exportPdfItem.addActionListener(ev -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new java.io.File(titleField.getText() + ".pdf"));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document,
                                new java.io.FileOutputStream(fileChooser.getSelectedFile()));
                        document.open();
                        document.add(new com.itextpdf.text.Paragraph(titleField.getText(),
                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24,
                                        com.itextpdf.text.Font.BOLD)));
                        document.add(new com.itextpdf.text.Paragraph("\n\n"));
                        document.add(new com.itextpdf.text.Paragraph(contentArea.getText()));
                        document.close();
                        JOptionPane.showMessageDialog(this, "PDF Exported successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to export PDF: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            popupMenu.add(exportPdfItem);

            popupMenu.addSeparator();

            JMenuItem deleteItem = new JMenuItem("Delete file");
            deleteItem.setForeground(Color.RED);
            deleteItem.addActionListener(ev -> {
                if (currentNoteId == null) {
                    JOptionPane.showMessageDialog(this, "Note not saved yet.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this note?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = ApiClient.deleteNote(currentNoteId);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Note deleted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        clearEditor();
                        Window window = SwingUtilities.getWindowAncestor(this);
                        if (window instanceof DashboardFrame) {
                            DashboardFrame parent = (DashboardFrame) window;
                            parent.refreshSidebar();
                            DashboardOverviewPanel overview = null;
                            Component[] comps = parent.getMainCardPanel().getComponents();
                            for (Component c : comps) {
                                if (c instanceof DashboardOverviewPanel) {
                                    overview = (DashboardOverviewPanel) c;
                                    break;
                                }
                            }
                            if (overview != null) {
                                overview.loadRealData();
                            }
                            parent.getCardLayout().show(parent.getMainCardPanel(), "DASHBOARD");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete note.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            popupMenu.add(deleteItem);

            popupMenu.show(optionsBtn, 0, optionsBtn.getHeight());
        });

        buttonWrapper.add(saveButton);
        buttonWrapper.add(optionsBtn);

        topToolbar.add(buttonWrapper, BorderLayout.EAST);

        // Add to North
        add(topToolbar, BorderLayout.NORTH);

        // --- Content Area ---
        contentArea = new JTextArea();
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        contentArea.setBackground(UIManager.getColor("TextArea.background"));
        contentArea.setForeground(UIManager.getColor("TextArea.foreground"));
        contentArea.setCaretColor(UIManager.getColor("TextArea.foreground"));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JScrollPane editorScrollPane = new JScrollPane(contentArea);
        editorScrollPane.setBorder(BorderFactory.createEmptyBorder());
        editorScrollPane.getViewport().setBackground(UIManager.getColor("TextArea.background"));

        // --- Live Preview Area ---
        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setBorder(null);
        previewPane.setBackground(UIManager.getColor("TextArea.background"));
        // Prevent the HTML renderer from resetting the background to white
        previewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane previewScrollPane = new JScrollPane(previewPane);
        previewScrollPane.setBorder(BorderFactory.createEmptyBorder());
        previewScrollPane.getViewport().setBackground(UIManager.getColor("TextArea.background"));

        // --- Split Pane Setup ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScrollPane, previewScrollPane);
        splitPane.setResizeWeight(0.5); // 50% split
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // Layout Center Wrapper for Editor and Metadata
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UIManager.getColor("Panel.background"));
        centerWrapper.add(splitPane, BorderLayout.CENTER);
        centerWrapper.add(metadataPanel, BorderLayout.EAST);

        // Add to main panel Center
        add(centerWrapper, BorderLayout.CENTER);

        // --- Live Syncing ---
        contentArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMarkdownPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMarkdownPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateMarkdownPreview();
            }
        });
    }

    private void updateMarkdownPreview() {
        String markdownText = contentArea.getText();
        Node document = Parser.builder().build().parse(markdownText);
        String htmlBody = HtmlRenderer.builder().build().render(document);

        Color bgColor = UIManager.getColor("TextArea.background");
        Color fgColor = UIManager.getColor("TextArea.foreground");
        String bg = bgColor != null
                ? String.format("#%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue())
                : "white";
        String fg = fgColor != null
                ? String.format("#%02x%02x%02x", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue())
                : "black";
        String fullHtml = "<html><body style='background-color:" + bg + "; color:" + fg
                + "; font-family:sans-serif; padding:10px;'>"
                + htmlBody + "</body></html>";
        previewPane.setText(fullHtml);
    }

    // Public Getters for Encapsulation
    public JTextField getTitleField() {
        return titleField;
    }

    public JTextArea getContentArea() {
        return contentArea;
    }

    public void loadNote(com.notegraph.frontend.model.NoteDto note) {
        currentNoteId = note.getId();
        titleField.setText(note.getTitle());
        contentArea.setText(note.getContent());

        String tagString = "";
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            tagString = note.getTags().stream()
                    .map(t -> (String) t.get("name"))
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        tagField.setText(tagString);
        connectField.setText("");

        updateMarkdownPreview();
    }

    public void clearEditor() {
        currentNoteId = null;
        titleField.setText("Untitled Note");
        contentArea.setText("");
        tagField.setText("");
        connectField.setText("");
        updateMarkdownPreview();
    }

    private void processLinks(String linksText) {
        if (linksText == null || linksText.trim().isEmpty())
            return;

        String[] titles = linksText.split(",");
        try {
            java.util.List<com.notegraph.frontend.model.NoteDto> allNotes = ApiClient.fetchAllNotes();
            if (allNotes == null)
                return;

            for (String title : titles) {
                String cleanTitle = title.trim();
                if (cleanTitle.isEmpty())
                    continue;

                for (com.notegraph.frontend.model.NoteDto n : allNotes) {
                    if (n.getTitle() != null && n.getTitle().equalsIgnoreCase(cleanTitle)) {
                        if (!n.getId().equals(currentNoteId)) {
                            ApiClient.createConnection(currentNoteId, n.getId());
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
