package com.notegraph.ui.pages;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.ConfirmDialog;
import com.notegraph.ui.components.VectorIcon;
import com.notegraph.ui.components.RoundedPanel;
import com.notegraph.ui.export.NoteExporter;
import com.notegraph.ui.export.PlainTextExporter;
import com.notegraph.ui.export.MarkdownExporter;
import com.notegraph.ui.export.HtmlExporter;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Rich text editor panel matching Editor.jsx.
 * Uses JTextPane with StyledDocument for formatting.
 */
public class EditorPanel extends JPanel {

    private Consumer<String> onNavigate;
    private String noteId;
    private JTextPane editorPane;
    private JTextField titleField;
    private JLabel saveStatusLabel;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel authorLabel;
    private JPanel tagsRow;
    private JPanel relatedNotesPanel;
    private boolean isFavorite = false;
    private JButton favBtn;
    private Timer saveTimer;
    private List<ApiModels.Tag> noteTags = new ArrayList<>();
    private List<ApiModels.Tag> availableTags = new ArrayList<>();
    private ApiModels.Note currentNote;

    // AI Fields
    private JPanel aiChatPanel;
    private JTextField aiInput;
    private List<ApiModels.AiChatMessage> aiChatHistory = new ArrayList<>();

    public EditorPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        // ── Top Action Bar ──────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ColorScheme.BG_SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Breadcrumbs
        JLabel breadcrumb = new JLabel("Workspace / Personal / New Draft");
        breadcrumb.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        breadcrumb.setForeground(ColorScheme.TEXT_SECONDARY);
        topBar.add(breadcrumb, BorderLayout.WEST);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        saveStatusLabel = new JLabel("Auto-saved");
        saveStatusLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        saveStatusLabel.setForeground(ColorScheme.SUCCESS_TEXT);
        actions.add(saveStatusLabel);

        JButton deleteBtn = new JButton(new VectorIcon(VectorIcon.Type.TRASH, 18, ColorScheme.TEXT_PRIMARY));
        deleteBtn.setToolTipText("Delete Note");
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBackground(null);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> handleDelete());
        actions.add(deleteBtn);

        favBtn = new JButton(new VectorIcon(VectorIcon.Type.STAR_BORDER, 20, ColorScheme.TEXT_SECONDARY));
        favBtn.setToolTipText("Add to favorites");
        favBtn.setBorderPainted(false);
        favBtn.setFocusPainted(false);
        favBtn.setBackground(null);
        favBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        favBtn.addActionListener(e -> handleToggleFavorite());
        actions.add(favBtn);

        JButton exportBtn = new JButton("Export v");
        exportBtn.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 14));
        exportBtn.setForeground(ColorScheme.TEXT_PRIMARY);
        exportBtn.setBackground(Color.WHITE);
        exportBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> showExportMenu(exportBtn));
        actions.add(exportBtn);

        JButton shareBtn = new JButton("Share");
        shareBtn.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        shareBtn.setForeground(Color.WHITE);
        shareBtn.setBackground(ColorScheme.PRIMARY_BLUE);
        shareBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.PRIMARY_BLUE, 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        shareBtn.setFocusPainted(false);
        shareBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actions.add(shareBtn);

        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Main Layout ─────────────────────────────
        JPanel mainLayout = new JPanel(new BorderLayout(0, 0));
        mainLayout.setOpaque(false);

        // Editor canvas
        JPanel editorWrapper = new JPanel(new BorderLayout());
        editorWrapper.setOpaque(false);
        editorWrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        RoundedPanel canvas = new RoundedPanel(12, ColorScheme.BORDER, false);
        canvas.setLayout(new BoxLayout(canvas, BoxLayout.Y_AXIS));
        canvas.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Title
        titleField = new JTextField("Untitled Note");
        titleField.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 26));
        titleField.setForeground(ColorScheme.TEXT_PRIMARY);
        titleField.setBorder(null);
        titleField.setBackground(Color.WHITE);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
        });
        canvas.add(titleField);
        canvas.add(Box.createVerticalStrut(10));

        // Metadata row
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel = new JLabel("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        dateLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        timeLabel = new JLabel("Time: Just now");
        timeLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        timeLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        metaRow.add(dateLabel);
        metaRow.add(timeLabel);
        canvas.add(metaRow);
        canvas.add(Box.createVerticalStrut(12));

        // Tags row
        tagsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tagsRow.setOpaque(false);
        tagsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tagsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton addTagBtn = new JButton("+ Add tag");
        addTagBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        addTagBtn.setForeground(ColorScheme.PRIMARY_BLUE);
        addTagBtn.setBorderPainted(false);
        addTagBtn.setFocusPainted(false);
        addTagBtn.setBackground(null);
        addTagBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTagBtn.addActionListener(e -> showAddTagDialog());
        tagsRow.add(addTagBtn);
        
        JButton suggestTagsBtn = new JButton("Suggest tags", new VectorIcon(VectorIcon.Type.SPARKLE, 14, ColorScheme.PRIMARY_BLUE));
        suggestTagsBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        suggestTagsBtn.setForeground(ColorScheme.PRIMARY_BLUE);
        suggestTagsBtn.setBorderPainted(false);
        suggestTagsBtn.setFocusPainted(false);
        suggestTagsBtn.setBackground(null);
        suggestTagsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        suggestTagsBtn.addActionListener(e -> applyAiTagSuggestions());
        tagsRow.add(suggestTagsBtn);

        canvas.add(tagsRow);
        canvas.add(Box.createVerticalStrut(8));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        toolbar.setOpaque(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, ColorScheme.BORDER),
                new EmptyBorder(4, 0, 4, 0)
        ));

        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.FORMAT_BOLD, 18, ColorScheme.TEXT_SECONDARY), () -> applyStyle("bold")));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.FORMAT_ITALIC, 18, ColorScheme.TEXT_SECONDARY), () -> applyStyle("italic")));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.FORMAT_UNDERLINE, 18, ColorScheme.TEXT_SECONDARY), () -> applyStyle("underline")));
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.HEADING_1, 18, ColorScheme.TEXT_SECONDARY), () -> applyHeading(24)));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.HEADING_2, 18, ColorScheme.TEXT_SECONDARY), () -> applyHeading(18)));
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.LIST_BULLET, 18, ColorScheme.TEXT_SECONDARY), () -> insertText("• ")));
        toolbar.add(createToolbarBtn(new VectorIcon(VectorIcon.Type.LIST_NUMBER, 18, ColorScheme.TEXT_SECONDARY), () -> insertText("1. ")));
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        JButton formatBtn = createToolbarBtn(new VectorIcon(VectorIcon.Type.SPARKLE, 16, ColorScheme.PRIMARY_BLUE), this::formatEntireContent);
        formatBtn.setToolTipText("AI Format Content");
        toolbar.add(formatBtn);
        canvas.add(toolbar);
        canvas.add(Box.createVerticalStrut(8));

        // Editor content
        editorPane = new JTextPane();
        editorPane.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 15));
        editorPane.setForeground(ColorScheme.TEXT_PRIMARY);
        editorPane.setBorder(new EmptyBorder(8, 0, 8, 0));
        editorPane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleSave(); }
        });

        // Keyboard shortcuts
        editorPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK), "bold");
        editorPane.getActionMap().put("bold", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { applyStyle("bold"); }
        });
        editorPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "italic");
        editorPane.getActionMap().put("italic", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { applyStyle("italic"); }
        });
        editorPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), "underline");
        editorPane.getActionMap().put("underline", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { applyStyle("underline"); }
        });

        JScrollPane editorScroll = new JScrollPane(editorPane);
        editorScroll.setBorder(null);
        editorScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        editorScroll.setPreferredSize(new Dimension(0, 400));
        canvas.add(editorScroll);

        editorWrapper.add(canvas, BorderLayout.CENTER);

        // ── Right Sidebar ───────────────────────────
        RoundedPanel rightSidebar = new RoundedPanel(0, null, false);
        rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
        rightSidebar.setBackground(ColorScheme.BG_SURFACE);
        rightSidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, ColorScheme.BORDER),
                new EmptyBorder(20, 16, 20, 16)
        ));
        rightSidebar.setPreferredSize(new Dimension(260, 0));

        // Note details section
        JLabel detailsTitle = new JLabel("NOTE DETAILS");
        detailsTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 11));
        detailsTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightSidebar.add(detailsTitle);
        rightSidebar.add(Box.createVerticalStrut(12));

        rightSidebar.add(createDetailRow("Created", "Today"));
        rightSidebar.add(createDetailRow("Last Edited", "Just now"));

        JPanel authorRow = new JPanel(new BorderLayout());
        authorRow.setOpaque(false);
        authorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        authorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel authorLabelTitle = new JLabel("Author");
        authorLabelTitle.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        authorLabelTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        authorLabel = new JLabel(ApiClient.get().getUser() != null ? ApiClient.get().getUser().name : "Unknown");
        authorLabel.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 12));
        authorLabel.setForeground(ColorScheme.TEXT_PRIMARY);
        authorRow.add(authorLabelTitle, BorderLayout.WEST);
        authorRow.add(authorLabel, BorderLayout.EAST);
        rightSidebar.add(authorRow);
        rightSidebar.add(Box.createVerticalStrut(6));
        rightSidebar.add(createDetailRow("Status", "Synced to Cloud"));
        rightSidebar.add(Box.createVerticalStrut(20));

        // Related notes section
        JLabel relatedTitle = new JLabel("RELATED NOTES");
        relatedTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 11));
        relatedTitle.setForeground(ColorScheme.TEXT_SECONDARY);
        relatedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightSidebar.add(relatedTitle);
        rightSidebar.add(Box.createVerticalStrut(8));

        relatedNotesPanel = new JPanel();
        relatedNotesPanel.setLayout(new BoxLayout(relatedNotesPanel, BoxLayout.Y_AXIS));
        relatedNotesPanel.setOpaque(false);
        relatedNotesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noRelated = new JLabel("No related notes found.");
        noRelated.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        noRelated.setForeground(ColorScheme.TEXT_SECONDARY);
        relatedNotesPanel.add(noRelated);
        rightSidebar.add(relatedNotesPanel);
        
        // AI Assistant
        rightSidebar.add(Box.createVerticalStrut(20));
        JLabel aiTitle = new JLabel("AI ASSISTANT", new VectorIcon(VectorIcon.Type.SPARKLE, 14, ColorScheme.PRIMARY_BLUE), SwingConstants.LEFT);
        aiTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 12));
        aiTitle.setForeground(ColorScheme.PRIMARY_BLUE);
        aiTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightSidebar.add(aiTitle);
        rightSidebar.add(Box.createVerticalStrut(8));

        aiChatPanel = new JPanel();
        aiChatPanel.setLayout(new BoxLayout(aiChatPanel, BoxLayout.Y_AXIS));
        aiChatPanel.setOpaque(false);
        JScrollPane aiScroll = new JScrollPane(aiChatPanel);
        aiScroll.setBorder(null);
        aiScroll.setOpaque(false);
        aiScroll.getViewport().setOpaque(false);
        aiScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightSidebar.add(aiScroll);

        rightSidebar.add(Box.createVerticalStrut(8));
        aiInput = new JTextField();
        aiInput.putClientProperty("JTextField.placeholderText", "Ask about this note...");
        aiInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        aiInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiInput.addActionListener(e -> handleAiChatSubmit());
        rightSidebar.add(aiInput);

        rightSidebar.add(Box.createVerticalStrut(8));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorWrapper, rightSidebar);
        splitPane.setDividerLocation(1280 - 320); // Default width approx
        splitPane.setResizeWeight(1.0); // Give extra space to editor
        splitPane.setBorder(null);
        // Clean divider
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    public void paint(Graphics g) {
                        g.setColor(ColorScheme.BORDER);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                    }
                };
            }
        });
        splitPane.setDividerSize(2);
        
        mainLayout.add(splitPane, BorderLayout.CENTER);
        add(mainLayout, BorderLayout.CENTER);

        // Save timer
        saveTimer = new Timer(1500, e -> doSave());
        saveTimer.setRepeats(false);
    }

    private JButton createToolbarBtn(Icon icon, Runnable action) {
        JButton btn = new JButton(icon);
        btn.setBackground(null);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 28));
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ColorScheme.BG_APP); btn.setOpaque(true); }
            public void mouseExited(MouseEvent e) { btn.setOpaque(false); btn.setBackground(null); }
        });
        return btn;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        lbl.setForeground(ColorScheme.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 12));
        val.setForeground(ColorScheme.TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    // ── Formatting ──────────────────────────────────
    private void applyStyle(String style) {
        StyledDocument doc = editorPane.getStyledDocument();
        int start = editorPane.getSelectionStart();
        int end = editorPane.getSelectionEnd();
        if (start == end) return;

        MutableAttributeSet attrs = new SimpleAttributeSet();
        switch (style) {
            case "bold" -> StyleConstants.setBold(attrs, !StyleConstants.isBold(doc.getCharacterElement(start).getAttributes()));
            case "italic" -> StyleConstants.setItalic(attrs, !StyleConstants.isItalic(doc.getCharacterElement(start).getAttributes()));
            case "underline" -> StyleConstants.setUnderline(attrs, !StyleConstants.isUnderline(doc.getCharacterElement(start).getAttributes()));
        }
        doc.setCharacterAttributes(start, end - start, attrs, false);
    }

    private void applyHeading(int size) {
        StyledDocument doc = editorPane.getStyledDocument();
        int start = editorPane.getSelectionStart();
        int end = editorPane.getSelectionEnd();
        if (start == end) return;
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, size);
        StyleConstants.setBold(attrs, true);
        doc.setCharacterAttributes(start, end - start, attrs, false);
    }

    private void insertText(String text) {
        try {
            int pos = editorPane.getCaretPosition();
            editorPane.getDocument().insertString(pos, text, null);
        } catch (BadLocationException ex) { /* ignore */ }
    }

    // ── Save Logic ──────────────────────────────────
    private void scheduleSave() {
        saveStatusLabel.setText("Saving...");
        saveStatusLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        if (saveTimer.isRunning()) saveTimer.restart();
        else saveTimer.start();
    }

    private void doSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) title = "Untitled Note";
        String content = getEditorContent();
        String wsId = ApiClient.get().getWorkspaceId();
        String userId = ApiClient.get().getUser() != null ? ApiClient.get().getUser().id : null;
        if (wsId == null || userId == null) return;

        String finalTitle = title;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiModels.Note note = new ApiModels.Note();
                note.title = finalTitle;
                note.content = content;
                note.workspace = new ApiModels.IdRef(wsId);
                note.owner = new ApiModels.IdRef(userId);

                if (noteId != null) {
                    ApiClient.get().updateNote(noteId, note);
                } else {
                    ApiModels.Note created = ApiClient.get().createNote(note);
                    noteId = created.id;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    saveStatusLabel.setText("Auto-saved");
                    saveStatusLabel.setForeground(ColorScheme.SUCCESS_TEXT);
                } catch (Exception e) {
                    saveStatusLabel.setText("✗ Save failed");
                    saveStatusLabel.setForeground(ColorScheme.ERROR_TEXT);
                }
            }
        }.execute();
    }

    private String getEditorContent() {
        // Get plain text since JTextPane styled content is complex
        // For the backend which stores HTML, we'll wrap in basic HTML
        try {
            StyledDocument doc = editorPane.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            return "<p>" + text.replace("\n", "</p><p>") + "</p>";
        } catch (BadLocationException e) {
            return "";
        }
    }

    private void formatEntireContent() {
        String content = getEditorContent();
        if (content == null || content.isEmpty()) return;

        saveStatusLabel.setText("✨ AI Formatting...");
        saveStatusLabel.setForeground(ColorScheme.PRIMARY_BLUE);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // To get better results from AI, pass raw text instead of HTML paragraphs
                String rawText = editorPane.getStyledDocument().getText(0, editorPane.getStyledDocument().getLength());
                return ApiClient.get().formatContent(rawText);
            }
            @Override
            protected void done() {
                try {
                    String formatted = get();
                    editorPane.setText(formatted);
                    saveTimer.stop(); // Prevent delayed save
                    doSave(); // Save instantly
                } catch (Exception e) {
                    saveStatusLabel.setText("✗ Formatting failed");
                    saveStatusLabel.setForeground(ColorScheme.ERROR_TEXT);
                }
            }
        }.execute();
    }

    // ── Actions ─────────────────────────────────────
    private void handleDelete() {
        if (noteId == null) return;
        if (ConfirmDialog.show(this, "Delete Note", "Are you sure you want to delete this note? This action cannot be undone.")) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ApiClient.get().deleteNote(noteId); return null;
                }
                @Override protected void done() { onNavigate.accept("dashboard"); }
            }.execute();
        }
    }

    private void handleToggleFavorite() {
        if (noteId == null) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.get().toggleFavorite(noteId); return null;
            }
            @Override protected void done() {
                isFavorite = !isFavorite;
                favBtn.setIcon(new VectorIcon(isFavorite ? VectorIcon.Type.STAR_FILLED : VectorIcon.Type.STAR_BORDER, 20, isFavorite ? ColorScheme.STAR_COLOR : ColorScheme.TEXT_SECONDARY));
            }
        }.execute();
    }

    private void showExportMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER),
                new EmptyBorder(4, 0, 4, 0)));

        NoteExporter[] exporters = {
                new PlainTextExporter(),
                new MarkdownExporter(),
                new HtmlExporter()
        };

        for (NoteExporter exporter : exporters) {
            JMenuItem item = new JMenuItem("Export as " + exporter.getFormatName());
            item.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
            item.setBorder(new EmptyBorder(6, 12, 6, 12));
            item.addActionListener(e -> handleExport(exporter));
            menu.add(item);
        }

        menu.show(source, 0, source.getHeight() + 4);
    }

    private void handleExport(NoteExporter exporter) {
        String content = getEditorContent();
        String title = titleField.getText().trim();
        if (title.isEmpty()) title = "Untitled Note";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export " + exporter.getFormatName());
        fileChooser.setSelectedFile(new File(title + "." + exporter.getExtension()));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            // Ensure proper extension
            if (!target.getName().toLowerCase().endsWith("." + exporter.getExtension())) {
                target = new File(target.getParentFile(), target.getName() + "." + exporter.getExtension());
            }

            try {
                exporter.export(title, content, target);
                JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + target.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export:\n" + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddTagDialog() {
        if (noteId == null) {
            JOptionPane.showMessageDialog(this, "Please type something first to save the note.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Fetch available tags
        new SwingWorker<List<ApiModels.Tag>, Void>() {
            @Override protected List<ApiModels.Tag> doInBackground() throws Exception {
                return ApiClient.get().getWorkspaceTags(ApiClient.get().getWorkspaceId());
            }
            @Override protected void done() {
                try {
                    availableTags = get();
                    String[] options = availableTags.stream().map(t -> "#" + t.name).toArray(String[]::new);
                    if (options.length == 0) {
                        String name = JOptionPane.showInputDialog(EditorPanel.this, "Enter new tag name:");
                        if (name != null && !name.trim().isEmpty()) createAndAddTag(name.trim());
                        return;
                    }
                    String selected = (String) JOptionPane.showInputDialog(EditorPanel.this, "Select tag:", "Add Tag",
                            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                    if (selected != null) {
                        String tagName = selected.replace("#", "");
                        ApiModels.Tag tag = availableTags.stream().filter(t -> t.name.equals(tagName)).findFirst().orElse(null);
                        if (tag != null) addTagToNote(tag);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void createAndAddTag(String name) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ApiModels.Tag tag = new ApiModels.Tag();
                tag.name = name;
                tag.color = "blue";
                tag.workspace = new ApiModels.IdRef(ApiClient.get().getWorkspaceId());
                ApiModels.Tag created = ApiClient.get().createTag(tag);
                ApiClient.get().addTagToNote(noteId, created.id);
                noteTags.add(created);
                return null;
            }
            @Override protected void done() { refreshTagsUI(); }
        }.execute();
    }

    private void addTagToNote(ApiModels.Tag tag) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.get().addTagToNote(noteId, tag.id);
                noteTags.add(tag);
                return null;
            }
            @Override protected void done() { refreshTagsUI(); }
        }.execute();
    }

    private void refreshTagsUI() {
        tagsRow.removeAll();
        for (ApiModels.Tag tag : noteTags) {
            JLabel pill = new JLabel("# " + tag.name + " ✕");
            pill.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 11));
            pill.setForeground(ColorScheme.tagFg(tag.color));
            pill.setBackground(ColorScheme.tagBg(tag.color));
            pill.setOpaque(true);
            pill.setBorder(new EmptyBorder(3, 8, 3, 8));
            pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pill.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            ApiClient.get().removeTagFromNote(noteId, tag.id);
                            noteTags.remove(tag); return null;
                        }
                        @Override protected void done() { refreshTagsUI(); }
                    }.execute();
                }
            });
            tagsRow.add(pill);
        }
        JButton addTagBtn = new JButton("+ Add tag");
        addTagBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        addTagBtn.setForeground(ColorScheme.PRIMARY_BLUE);
        addTagBtn.setBorderPainted(false);
        addTagBtn.setFocusPainted(false);
        addTagBtn.setBackground(null);
        addTagBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTagBtn.addActionListener(e -> showAddTagDialog());
        tagsRow.add(addTagBtn);
        
        JButton suggestTagsBtn = new JButton("Suggest tags", new VectorIcon(VectorIcon.Type.SPARKLE, 14, ColorScheme.PRIMARY_BLUE));
        suggestTagsBtn.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        suggestTagsBtn.setForeground(ColorScheme.PRIMARY_BLUE);
        suggestTagsBtn.setBorderPainted(false);
        suggestTagsBtn.setFocusPainted(false);
        suggestTagsBtn.setBackground(null);
        suggestTagsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        suggestTagsBtn.addActionListener(e -> applyAiTagSuggestions());
        tagsRow.add(suggestTagsBtn);

        tagsRow.revalidate();
        tagsRow.repaint();
    }

    // ── Load / Reset ────────────────────────────────
    public void loadNote(String id) {
        this.noteId = id;
        if (id == null) {
            titleField.setText("Untitled Note");
            editorPane.setText("");
            isFavorite = false;
            favBtn.setText("☆");
            favBtn.setForeground(ColorScheme.TEXT_SECONDARY);
            noteTags.clear();
            refreshTagsUI();
            saveStatusLabel.setText("✓ Auto-saved");
            saveStatusLabel.setForeground(ColorScheme.SUCCESS_TEXT);
            return;
        }
        new SwingWorker<ApiModels.Note, Void>() {
            @Override protected ApiModels.Note doInBackground() throws Exception {
                return ApiClient.get().getNoteById(id);
            }
            @Override protected void done() {
                try {
                    currentNote = get();
                    titleField.setText(currentNote.title != null ? currentNote.title : "Untitled Note");
                    String content = currentNote.content != null ? currentNote.content : "";
                    // Strip HTML for JTextPane
                    String plain = content.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&");
                    editorPane.setText(plain);
                    isFavorite = Boolean.TRUE.equals(currentNote.favorite);
                    favBtn.setIcon(new VectorIcon(isFavorite ? VectorIcon.Type.STAR_FILLED : VectorIcon.Type.STAR_BORDER, 20, isFavorite ? ColorScheme.STAR_COLOR : ColorScheme.TEXT_SECONDARY));
                    noteTags = currentNote.tags != null ? new ArrayList<>(currentNote.tags) : new ArrayList<>();
                    refreshTagsUI();
                    if (currentNote.createdAt != null) {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(currentNote.createdAt.substring(0, 19));
                            dateLabel.setText("Date: " + dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                            timeLabel.setText("Time: " + dt.format(DateTimeFormatter.ofPattern("HH:mm")));
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    titleField.setText("Error loading note");
                }
            }
        }.execute();
    }

    // ── AI Methods ──────────────────────────────────
    private void appendAiMessage(String role, String text) {
        boolean isUser = "user".equals(role);
        
        JPanel row = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 4, 12, 4));

        // Avatar
        JLabel avatar = new JLabel(new VectorIcon(isUser ? VectorIcon.Type.AVATAR_USER : VectorIcon.Type.AVATAR_AI, 24, isUser ? ColorScheme.TEXT_SECONDARY : ColorScheme.PRIMARY_BLUE));
        avatar.setVerticalAlignment(SwingConstants.TOP);
        avatar.setBorder(new EmptyBorder(2, 4, 0, 4));

        // Chat Bubble
        RoundedPanel bubble = new RoundedPanel(16, null, false);
        bubble.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bubble.setBackground(isUser ? ColorScheme.PRIMARY_BLUE : ColorScheme.BG_APP);
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));

        JTextArea textArea = new JTextArea(text.replaceAll("\\*\\*", "")) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int maxW = 200;
                if (d.width > maxW) {
                    setSize(new Dimension(maxW, Short.MAX_VALUE));
                    return new Dimension(maxW, super.getPreferredSize().height);
                }
                return d;
            }
        };
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        textArea.setForeground(isUser ? Color.WHITE : ColorScheme.TEXT_PRIMARY);
        textArea.setBackground(isUser ? ColorScheme.PRIMARY_BLUE : ColorScheme.BG_APP);
        textArea.setEditable(false);
        textArea.setOpaque(true);
        textArea.setBorder(null);

        bubble.add(textArea);

        if (isUser) {
            row.add(Box.createHorizontalGlue());
            row.add(bubble);
            row.add(avatar);
        } else {
            row.add(avatar);
            row.add(bubble);
            row.add(Box.createHorizontalGlue());
        }

        aiChatPanel.add(row);
        aiChatPanel.revalidate();
        aiChatPanel.repaint();
        
        // Auto scroll to bottom
        SwingUtilities.invokeLater(() -> {
            Container parent = SwingUtilities.getAncestorOfClass(JScrollPane.class, aiChatPanel);
            if (parent != null) {
                JScrollPane scroll = (JScrollPane) parent;
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private void handleAiChatSubmit() {
        String query = aiInput.getText().trim();
        if (query.isEmpty() || noteId == null) return;
        aiInput.setText("");

        aiChatHistory.add(new ApiModels.AiChatMessage("user", query));
        appendAiMessage("user", query);

        JLabel loadingLabel = new JLabel("Thinking...");
        loadingLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.ITALIC, 11));
        loadingLabel.setForeground(ColorScheme.TEXT_SECONDARY);
        aiChatPanel.add(loadingLabel);
        aiChatPanel.revalidate();
        aiChatPanel.repaint();

        ApiModels.AiChatRequest req = new ApiModels.AiChatRequest();
        req.currentNoteId = noteId;
        req.currentNoteTitle = titleField.getText();
        req.currentNoteContent = getEditorContent();
        req.currentTags = noteTags.stream().map(t -> t.id).toList();
        req.history = new ArrayList<>(aiChatHistory);

        new SwingWorker<ApiModels.AiChatResponse, Void>() {
            @Override protected ApiModels.AiChatResponse doInBackground() throws Exception {
                return ApiClient.get().chatWithAi(ApiClient.get().getWorkspaceId(), req);
            }
            @Override protected void done() {
                aiChatPanel.remove(loadingLabel);
                try {
                    ApiModels.AiChatResponse res = get();
                    aiChatHistory.add(new ApiModels.AiChatMessage("model", res.answer));
                    appendAiMessage("model", res.answer);
                } catch (Exception e) {
                    appendAiMessage("model", "Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void applyAiTagSuggestions() {
        if (noteId == null) return;
        String content = getEditorContent();
        
        JLabel loading = new JLabel(" ✨...");
        loading.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        tagsRow.add(loading);
        tagsRow.revalidate();
        tagsRow.repaint();

        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() throws Exception {
                if(availableTags.isEmpty()){
                    availableTags = ApiClient.get().getWorkspaceTags(ApiClient.get().getWorkspaceId());
                }
                return ApiClient.get().suggestTags(ApiClient.get().getWorkspaceId(), content);
            }
            @Override protected void done() {
                try {
                    List<String> suggested = get();
                    for(String s : suggested) {
                        ApiModels.Tag existing = availableTags.stream()
                                .filter(t -> t.name.equalsIgnoreCase(s))
                                .findFirst().orElse(null);
                        
                        if (existing != null) {
                            if (noteTags.stream().noneMatch(t -> t.id.equals(existing.id))) {
                                addTagToNote(existing);
                            }
                        } else {
                            createAndAddTag(s);
                        }
                    }
                } catch (Exception ignored) {}
                refreshTagsUI();
            }
        }.execute();
    }
}
