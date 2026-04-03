package com.notegraph.ui.pages;

import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * User guide page matching GuidePage.jsx.
 * Accordion-based expandable guide sections.
 */
public class GuidePanel extends JPanel {

    private String expandedSection = "workspaces";

    private static final String[][] SECTIONS = {
        {"workspaces", "📁", "Workspaces", "Organize your projects", "#3B82F6"},
        {"notes", "📄", "Creating & Editing Notes", "Write beautiful notes", "#10B981"},
        {"tags", "🏷", "Tags & Sections", "Categorize your notes", "#8B5CF6"},
        {"graph", "🔗", "Note Graph & Connections", "Visualize your knowledge", "#F59E0B"},
        {"favorites", "⭐", "Favorites", "Quick access to important notes", "#EF4444"},
        {"tips", "⚡", "Pro Tips", "Get the most out of NoteGraph", "#EC4899"},
    };

    private static final String[][][] CONTENT = {
        { // Workspaces
            {"What are Workspaces?", "Workspaces are the top-level containers for your notes. Think of them as separate projects or areas of focus."},
            {"Create a Workspace", "Click the workspace dropdown in the sidebar, then click 'Create new workspace'. Give it a meaningful name."},
            {"Switch Workspaces", "Use the workspace dropdown in the sidebar to switch between workspaces."},
            {"Rename a Workspace", "Click the pencil icon next to a workspace in the dropdown menu."},
            {"Delete a Workspace", "Click the trash icon next to a workspace. Warning: this permanently deletes all notes and tags inside it."},
        },
        { // Notes
            {"Create a New Note", "Click the '+ New Note' button in the sidebar. This opens the Editor with a blank note."},
            {"Rich Text Editor", "Use the formatting toolbar — Bold, Italic, Underline, Headings, Lists."},
            {"Choose Workspace & Section", "Use the Workspace selector and Section selector below the title."},
            {"Auto-Save", "Your notes are automatically saved as you type. Look for the 'Saved' indicator."},
            {"Delete a Note", "Click the trash icon in the editor toolbar."},
        },
        { // Tags
            {"What are Tags?", "Tags (Sections) are labels you add to notes. They create connections in the Note Graph."},
            {"Create a Tag", "Go to the Tags page, then click '+ Create New Tag'."},
            {"Add Tags to Notes", "In the Editor, click '+ Add tag' to attach tags to your note."},
            {"Rename a Section", "On the Note Graph page, click the pencil icon next to any section header."},
            {"Delete a Tag", "Use the trash icon on the Tags page. Notes will be unlinked but not deleted."},
        },
        { // Graph
            {"The Note Graph", "Navigate to 'Note Graph' in the sidebar to see how your notes are connected."},
            {"Tree View", "See a collapsible list layout. Click on any section to expand/collapse it."},
            {"Connection Details", "Click on any tag node to open the Connection Details sidebar."},
        },
        { // Favorites
            {"Mark as Favorite", "Click the star icon on any note to toggle it as a favorite."},
            {"Favorites Page", "Navigate to 'Favorites' in the sidebar to see all starred notes."},
        },
        { // Tips
            {"Keyboard Shortcuts", "Ctrl+B for bold, Ctrl+I for italic, Ctrl+U for underline."},
            {"Use Tags Strategically", "Create tags for topics, not just categories. This creates richer connections."},
            {"Multiple Sections per Note", "A note can belong to multiple sections, creating cross-connections."},
            {"Search & Filter", "Use the search bar on the Dashboard and Tags pages to quickly find notes."},
        },
    };

    public GuidePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headerIcon = new JLabel("📖");
        headerIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        JLabel guideTitle = new JLabel("User Guide");
        guideTitle.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        guideTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel guideSubtitle = new JLabel("Everything you need to know to master NoteGraph");
        guideSubtitle.setFont(ColorScheme.FONT_REGULAR);
        guideSubtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        headerText.add(guideTitle);
        headerText.add(guideSubtitle);
        headerPanel.add(headerIcon);
        headerPanel.add(headerText);
        content.add(headerPanel);
        content.add(Box.createVerticalStrut(20));

        // Quick Start Banner
        RoundedPanel quickStart = new RoundedPanel(12, null, false);
        quickStart.setBackground(new Color(239, 246, 255));
        quickStart.setLayout(new BoxLayout(quickStart, BoxLayout.Y_AXIS));
        quickStart.setBorder(new EmptyBorder(16, 20, 16, 20));
        quickStart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        quickStart.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel qsTitle = new JLabel("✨ Quick Start");
        qsTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        qsTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel qsSteps = new JLabel("📁 Workspace  →  📄 Notes  →  🏷 Tags  →  🔗 Graph");
        qsSteps.setFont(ColorScheme.FONT_REGULAR);
        qsSteps.setForeground(ColorScheme.TEXT_SECONDARY);
        quickStart.add(qsTitle);
        quickStart.add(Box.createVerticalStrut(6));
        quickStart.add(qsSteps);
        content.add(quickStart);
        content.add(Box.createVerticalStrut(20));

        // Guide Sections (Accordion)
        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setOpaque(false);
        sectionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < SECTIONS.length; i++) {
            sectionsPanel.add(createSection(i));
            sectionsPanel.add(Box.createVerticalStrut(10));
        }
        content.add(sectionsPanel);
        content.add(Box.createVerticalStrut(24));

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel footerIcon = new JLabel("◆");
        footerIcon.setForeground(ColorScheme.TEXT_TERTIARY);
        JLabel footerText = new JLabel("NoteGraph — A connected note-taking experience");
        footerText.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
        footerText.setForeground(ColorScheme.TEXT_TERTIARY);
        footer.add(footerIcon);
        footer.add(footerText);
        content.add(footer);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private RoundedPanel createSection(int idx) {
        String[] sec = SECTIONS[idx];
        String sectionId = sec[0];
        String icon = sec[1];
        String title = sec[2];
        String subtitle = sec[3];
        Color color = Color.decode(sec[4]);
        boolean isExpanded = sectionId.equals(expandedSection);

        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel iconBox = new JLabel(icon);
        iconBox.setFont(new Font("SansSerif", Font.PLAIN, 20));

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
        titleLbl.setForeground(ColorScheme.TEXT_PRIMARY);
        JLabel subtitleLbl = new JLabel(subtitle);
        subtitleLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        subtitleLbl.setForeground(ColorScheme.TEXT_SECONDARY);
        textGroup.add(titleLbl);
        textGroup.add(subtitleLbl);

        left.add(iconBox);
        left.add(textGroup);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel stepsLabel = new JLabel(CONTENT[idx].length + " steps");
        stepsLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        stepsLabel.setForeground(ColorScheme.TEXT_TERTIARY);
        JLabel chevron = new JLabel(isExpanded ? "▲" : "▼");
        chevron.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chevron.setForeground(ColorScheme.TEXT_TERTIARY);
        right.add(stepsLabel);
        right.add(chevron);
        header.add(right, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                expandedSection = sectionId.equals(expandedSection) ? null : sectionId;
                removeAll();
                buildUI();
                revalidate();
                repaint();
            }
        });
        card.add(header);

        // Body (if expanded)
        if (isExpanded) {
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);
            body.setBorder(new EmptyBorder(0, 24, 16, 24));

            for (int j = 0; j < CONTENT[idx].length; j++) {
                JPanel step = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
                step.setOpaque(false);
                step.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                step.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel numLabel = new JLabel(String.valueOf(j + 1));
                numLabel.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 12));
                numLabel.setForeground(Color.WHITE);
                numLabel.setOpaque(true);
                numLabel.setBackground(color);
                numLabel.setHorizontalAlignment(SwingConstants.CENTER);
                numLabel.setPreferredSize(new Dimension(24, 24));

                JPanel stepText = new JPanel();
                stepText.setLayout(new BoxLayout(stepText, BoxLayout.Y_AXIS));
                stepText.setOpaque(false);
                JLabel heading = new JLabel(CONTENT[idx][j][0]);
                heading.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
                heading.setForeground(ColorScheme.TEXT_PRIMARY);
                JLabel desc = new JLabel("<html><body style='width: 500px'>" + CONTENT[idx][j][1] + "</body></html>");
                desc.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 13));
                desc.setForeground(ColorScheme.TEXT_SECONDARY);
                stepText.add(heading);
                stepText.add(desc);

                step.add(numLabel);
                step.add(stepText);
                body.add(step);
                body.add(Box.createVerticalStrut(8));
            }
            card.add(body);
        }

        return card;
    }
}
