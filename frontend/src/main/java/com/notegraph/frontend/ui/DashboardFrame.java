package com.notegraph.frontend.ui;

import javax.swing.*;
import java.awt.*;

// Demonstrates Inheritance: DashboardFrame IS-A JFrame
public class DashboardFrame extends JFrame {

    // Demonstrates Composition: DashboardFrame HAS-A JSplitPane and JPanels
    private JPanel sidebarPanel;
    private JSplitPane mainSplitPane;
    private JPanel mainContentContainer;
    private CardLayout cardLayout;

    private TextNoteEditorPanel textEditorPanel;
    private CanvasEditorPanel canvasEditorPanel;
    private DashboardOverviewPanel overviewPanel;
    private AllNotesPanel allNotesPanel;
    private GraphViewPanel graphViewPanel;

    public DashboardFrame() {
        // Configure basic frame properties
        setTitle("NoteGraph - Dashboard Overview");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Build the User Interface
        initUI();
    }

    private void initUI() {
        // --- Left Column (Sidebar) ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(UIManager.getColor("Panel.background"));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebarPanel.setPreferredSize(new Dimension(250, 0));

        JPanel userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        userProfilePanel.setBackground(UIManager.getColor("Panel.background"));
        userProfilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Mock user profile
        JLabel avatar = new JLabel("\uD83D\uDC68\u200D\uD83D\uDCBC"); // Man office worker emoji
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(UIManager.getColor("Panel.background"));
        JLabel nameLbl = new JLabel("John Doe");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(UIManager.getColor("Label.foreground"));
        JLabel planLbl = new JLabel("Pro Plan");
        planLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        planLbl.setForeground(new Color(24, 119, 242));
        textPanel.add(nameLbl);
        textPanel.add(planLbl);
        userProfilePanel.add(avatar);
        userProfilePanel.add(textPanel);

        JButton newNoteBtn = new JButton("+ Create New Note");
        newNoteBtn.setBackground(new Color(24, 119, 242));
        newNoteBtn.setForeground(Color.WHITE);
        newNoteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newNoteBtn.setFocusPainted(false);
        newNoteBtn.setBorderPainted(false);
        newNoteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newNoteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        newNoteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newNoteBtn.addActionListener(e -> {
            textEditorPanel.clearEditor();
            showEditor(textEditorPanel);
        });

        JLabel menuSection = new JLabel("MENU");
        menuSection.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuSection.setForeground(Color.GRAY);
        menuSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dashboardBtn = createNavButton("Dashboard");
        // Highlight active dashboard button
        dashboardBtn.setBackground(new Color(230, 240, 255));
        dashboardBtn.setForeground(new Color(24, 119, 242));
        dashboardBtn.addActionListener(e -> {
            showDashboard();
        });

        JButton allNotesBtn = createNavButton("All Notes");
        allNotesBtn.addActionListener(e -> {
            showAllNotes();
        });

        JButton foldersBtn = createNavButton("Folders");
        JButton tagsBtn = createNavButton("Tags");
        JButton favBtn = createNavButton("Favorites");
        JButton graphBtn = createNavButton("Graph View");
        graphBtn.addActionListener(e -> {
            graphViewPanel.refreshData();
            cardLayout.show(mainContentContainer, "GRAPH");
        });

        JLabel recentSection = new JLabel("RECENT FOLDERS         +");
        recentSection.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentSection.setForeground(Color.GRAY);
        recentSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton designBtn = createNavButton("\u2022  Design System");
        JButton alphaBtn = createNavButton("\u2022  Project Alpha");
        JButton mktgBtn = createNavButton("\u2022  Marketing Assets");

        sidebarPanel.add(userProfilePanel);
        sidebarPanel.add(Box.createVerticalStrut(20));
        sidebarPanel.add(newNoteBtn);
        sidebarPanel.add(Box.createVerticalStrut(30));

        sidebarPanel.add(menuSection);
        sidebarPanel.add(Box.createVerticalStrut(15));
        sidebarPanel.add(dashboardBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(allNotesBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(foldersBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(tagsBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(favBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(graphBtn);

        sidebarPanel.add(Box.createVerticalStrut(30));
        sidebarPanel.add(recentSection);
        sidebarPanel.add(Box.createVerticalStrut(15));
        sidebarPanel.add(designBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(alphaBtn);
        sidebarPanel.add(Box.createVerticalStrut(5));
        sidebarPanel.add(mktgBtn);

        sidebarPanel.add(Box.createVerticalGlue()); // Push bottom actions down

        JButton newWorkspaceBtn = new JButton("+ New Workspace");
        newWorkspaceBtn.setBackground(Color.WHITE);
        newWorkspaceBtn.setForeground(Color.DARK_GRAY);
        newWorkspaceBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newWorkspaceBtn.setFocusPainted(false);
        newWorkspaceBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        newWorkspaceBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newWorkspaceBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        newWorkspaceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sidebarPanel.add(newWorkspaceBtn);

        // --- Split Panes Setup ---
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());

        // Default Main View
        overviewPanel = new DashboardOverviewPanel(this);

        textEditorPanel = new TextNoteEditorPanel();
        canvasEditorPanel = new CanvasEditorPanel();
        allNotesPanel = new AllNotesPanel(this);
        graphViewPanel = new GraphViewPanel();

        cardLayout = new CardLayout();
        mainContentContainer = new JPanel(cardLayout);
        mainContentContainer.add(overviewPanel, "DASHBOARD");
        mainContentContainer.add(graphViewPanel, "GRAPH");
        mainContentContainer.add(textEditorPanel, "TEXT_EDITOR");
        mainContentContainer.add(canvasEditorPanel, "CANVAS_EDITOR");
        mainContentContainer.add(allNotesPanel, "ALL_NOTES");

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainContentContainer);
        mainSplitPane.setDividerLocation(240); // Sidebar width
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerSize(2);

        // A trick to create the top navbar without messing up the main layout
        JPanel topNavBar = new JPanel(new BorderLayout());
        topNavBar.setBackground(UIManager.getColor("Panel.background"));
        topNavBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        topNavBar.setPreferredSize(new Dimension(1200, 60));

        // Left logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        logoPanel.setBackground(UIManager.getColor("Panel.background"));
        JLabel logo = new JLabel("SmartNotes");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(UIManager.getColor("Label.foreground"));
        logoPanel.add(logo);

        // Center search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        searchPanel.setBackground(UIManager.getColor("Panel.background"));
        JTextField searchField = new JTextField("Search notes, tags, or folders...", 40);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                overviewPanel.filterNotes(query);
            }
        });
        searchPanel.add(searchField);

        // Right icons
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        iconsPanel.setBackground(UIManager.getColor("Panel.background"));

        // Dark Mode toggle
        JButton toggleThemeBtn = new JButton("Toggle Theme");
        toggleThemeBtn.setBackground(new Color(100, 100, 100)); // Darker flat look
        toggleThemeBtn.setForeground(Color.WHITE);
        toggleThemeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleThemeBtn.setFocusPainted(false);
        toggleThemeBtn.setBorderPainted(false);
        toggleThemeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleThemeBtn.addActionListener(e -> {
            boolean isDark = UIManager.getLookAndFeel().getClass().getName().toLowerCase().contains("dark");
            try {
                if (isDark) {
                    com.formdev.flatlaf.FlatLightLaf.setup();
                } else {
                    com.formdev.flatlaf.FlatDarkLaf.setup();
                }
                SwingUtilities.updateComponentTreeUI(DashboardFrame.this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Export dropdown button
        JButton exportBtn = new JButton("Export All");
        exportBtn.setBackground(new Color(24, 119, 242));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exportBtn.setFocusPainted(false);
        exportBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> {
            JPopupMenu exportMenu = new JPopupMenu();

            JMenuItem pdfItem = new JMenuItem("Export as PDF");
            pdfItem.addActionListener(ev -> exportAllNotes());

            JMenuItem docItem = new JMenuItem("Export as DOC");
            docItem.addActionListener(ev -> JOptionPane.showMessageDialog(DashboardFrame.this,
                    "Format export coming soon!", "DOC Export", JOptionPane.INFORMATION_MESSAGE));

            JMenuItem jsonItem = new JMenuItem("Export as JSON");
            jsonItem.addActionListener(ev -> JOptionPane.showMessageDialog(DashboardFrame.this,
                    "Format export coming soon!", "JSON Export", JOptionPane.INFORMATION_MESSAGE));

            JMenuItem imgItem = new JMenuItem("Export as Image");
            imgItem.addActionListener(ev -> JOptionPane.showMessageDialog(DashboardFrame.this,
                    "Format export coming soon!", "Image Export", JOptionPane.INFORMATION_MESSAGE));

            exportMenu.add(pdfItem);
            exportMenu.add(docItem);
            exportMenu.add(jsonItem);
            exportMenu.add(imgItem);

            exportMenu.show(exportBtn, 0, exportBtn.getHeight());
        });

        JLabel helpIcon = new JLabel("?");
        helpIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        helpIcon.setForeground(Color.GRAY);
        JLabel settingsIcon = new JLabel("\u2699");
        settingsIcon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        settingsIcon.setForeground(Color.GRAY);
        JLabel bellIcon = new JLabel("\uD83D\uDD14");
        bellIcon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        bellIcon.setForeground(Color.GRAY);

        iconsPanel.add(toggleThemeBtn);
        iconsPanel.add(exportBtn);
        iconsPanel.add(helpIcon);
        iconsPanel.add(settingsIcon);
        iconsPanel.add(bellIcon);

        topNavBar.add(logoPanel, BorderLayout.WEST);
        topNavBar.add(searchPanel, BorderLayout.CENTER);
        topNavBar.add(iconsPanel, BorderLayout.EAST);

        getContentPane().add(topNavBar, BorderLayout.NORTH);
        getContentPane().add(mainSplitPane, BorderLayout.CENTER);
    }

    public void showEditor(JPanel editorPanel) {
        if (editorPanel instanceof CanvasEditorPanel) {
            cardLayout.show(mainContentContainer, "CANVAS_EDITOR");
        } else {
            cardLayout.show(mainContentContainer, "TEXT_EDITOR");
        }
    }

    public TextNoteEditorPanel getTextNoteEditorPanel() {
        return textEditorPanel;
    }

    public CanvasEditorPanel getCanvasEditorPanel() {
        return canvasEditorPanel;
    }

    public void showDashboard() {
        cardLayout.show(mainContentContainer, "DASHBOARD");
    }

    public JPanel getMainCardPanel() {
        return mainContentContainer;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public void showAllNotes() {
        allNotesPanel.refreshData();
        cardLayout.show(mainContentContainer, "ALL_NOTES");
    }

    public AllNotesPanel getAllNotesPanel() {
        return allNotesPanel;
    }

    // Unchanged helper for refreshing sidebar in older contexts if called
    public void refreshSidebar() {
        // Now unused in new UI but kept for compatibility with Editor Panels that
        // trigger it
        if (mainSplitPane.getRightComponent() instanceof DashboardOverviewPanel) {
            showDashboard(); // Reload data
        }
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.GRAY);
        button.setBackground(new Color(248, 248, 248));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getForeground().equals(new Color(24, 119, 242))) {
                    button.setBackground(new Color(240, 240, 240));
                }
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getForeground().equals(new Color(24, 119, 242))) { // If not active
                    button.setBackground(new Color(248, 248, 248));
                }
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        return button;
    }

    private void exportAllNotes() {
        try {
            java.util.List<com.notegraph.frontend.model.NoteDto> notes = com.notegraph.frontend.api.ApiClient
                    .fetchAllNotes();
            if (notes == null || notes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No notes found to export.", "Export",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Workspace Summary");
            fileChooser.setSelectedFile(new java.io.File("Workspace_Summary.pdf"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();

                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fileToSave));
                document.open();

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);

                document.add(new com.itextpdf.text.Paragraph("NoteGraph Workspace Summary", new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD)));
                document.add(new com.itextpdf.text.Paragraph(" "));

                for (com.notegraph.frontend.model.NoteDto note : notes) {
                    String title = note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle()
                            : "Untitled Note";
                    document.add(new com.itextpdf.text.Paragraph(title, titleFont));

                    String content = note.getContent() != null ? note.getContent() : "";
                    if (content.length() > 200) {
                        content = content.substring(0, 200).replace("\n", " ") + "...";
                    } else if (content.isEmpty()) {
                        content = "No content.";
                    }
                    document.add(new com.itextpdf.text.Paragraph(content, normalFont));
                    document.add(new com.itextpdf.text.Paragraph(" "));
                    document.add(new com.itextpdf.text.Paragraph("--------------------------------------------------"));
                    document.add(new com.itextpdf.text.Paragraph(" "));
                }

                document.close();
                JOptionPane.showMessageDialog(this, "Workspace exported successfully!", "Export Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export notes: " + ex.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void openNoteInEditor(Long noteId) {
        try {
            com.notegraph.frontend.model.NoteDto note = com.notegraph.frontend.api.ApiClient.fetchNoteById(noteId);
            if (note != null) {
                textEditorPanel.clearEditor();
                textEditorPanel.loadNote(note);
                cardLayout.show(mainContentContainer, "TEXT_EDITOR");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch note details.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening note: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
