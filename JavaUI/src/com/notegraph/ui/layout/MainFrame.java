package com.notegraph.ui.layout;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.pages.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with sidebar + content area.
 * Uses CardLayout to swap between pages.
 */
public class MainFrame extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentArea;
    private SidebarPanel sidebar;
    private DashboardPanel dashboardPanel;
    private EditorPanel editorPanel;
    private NoteHierarchyPanel hierarchyPanel;
    private KnowledgeGraphPanel knowledgeGraphPanel;
    private TagsPanel tagsPanel;
    private FavoritesPanel favoritesPanel;
    private GuidePanel guidePanel;
    private RevisionPanel revisionPanel;
    private Runnable onLogout;

    public MainFrame(Runnable onLogout) {
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BG_APP);
        buildUI();
    }

    private void buildUI() {
        // Sidebar
        sidebar = new SidebarPanel(this::navigateTo, onLogout);
        add(sidebar, BorderLayout.WEST);

        // Right side: top bar + pages
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ColorScheme.BG_APP);

        // Top bar
        TopBarPanel topBar = new TopBarPanel();
        rightPanel.add(topBar, BorderLayout.NORTH);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(ColorScheme.BG_APP);

        dashboardPanel = new DashboardPanel(this::navigateTo);
        editorPanel = new EditorPanel(this::navigateTo);
        hierarchyPanel = new NoteHierarchyPanel(this::navigateTo);
        knowledgeGraphPanel = new KnowledgeGraphPanel(this::navigateTo);
        tagsPanel = new TagsPanel();
        favoritesPanel = new FavoritesPanel(this::navigateTo);
        guidePanel = new GuidePanel();
        revisionPanel = new RevisionPanel(this::navigateTo);

        contentArea.add(dashboardPanel, "dashboard");
        contentArea.add(editorPanel, "editor");
        contentArea.add(hierarchyPanel, "hierarchy");
        contentArea.add(knowledgeGraphPanel, "knowledgegraph");
        contentArea.add(tagsPanel, "tags");
        contentArea.add(favoritesPanel, "favorites");
        contentArea.add(guidePanel, "guide");
        contentArea.add(revisionPanel, "revision");

        rightPanel.add(contentArea, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);
    }

    public void navigateTo(String page) {
        // Handle "editor:<noteId>" format
        String noteId = null;
        if (page.startsWith("editor:")) {
            noteId = page.substring("editor:".length());
            page = "editor";
        }

        sidebar.setActivePage(page);
        cardLayout.show(contentArea, page);

        // Refresh data for the page
        switch (page) {
            case "dashboard" -> dashboardPanel.refreshData();
            case "editor" -> editorPanel.loadNote(noteId);
            case "hierarchy" -> hierarchyPanel.refreshData();
            case "knowledgegraph" -> knowledgeGraphPanel.refreshData();
            case "tags" -> tagsPanel.refreshData();
            case "favorites" -> favoritesPanel.refreshData();
            case "revision" -> revisionPanel.refreshData();
        }
    }

    public void initialLoad() {
        sidebar.refreshData();
        navigateTo("dashboard");
    }
}
