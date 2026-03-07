package com.notegraph.frontend.ui;

import com.notegraph.frontend.api.ApiClient;
import com.notegraph.frontend.model.NoteDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AllNotesPanel extends JPanel {
    private DashboardFrame parentFrame;
    private JPanel listContainer;

    public AllNotesPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header (North)
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("All Documents");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));

        JLabel subtitleLabel = new JLabel("Browse and manage your complete workspace");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Content (Center)
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        listContainer.removeAll();
        try {
            List<NoteDto> notes = ApiClient.fetchAllNotes();
            if (notes != null) {
                // To keep the cards compact even if there are few
                for (NoteDto note : notes) {
                    listContainer.add(createRowPanel(note));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add glue to push items strictly to top
        listContainer.add(Box.createVerticalGlue());

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createRowPanel(NoteDto note) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(UIManager.getColor("Panel.background"));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Row Left
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setBackground(UIManager.getColor("Panel.background"));

        String title = note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle() : "Untitled Note";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));

        String content = note.getContent() != null ? note.getContent().replace("\n", " ") : "";
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        } else if (content.isEmpty()) {
            content = "No content.";
        }
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentLabel.setForeground(Color.GRAY);

        leftPanel.add(titleLabel);
        leftPanel.add(contentLabel);
        rowPanel.add(leftPanel, BorderLayout.CENTER);

        // Row Right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton openButton = new JButton("Open Note");
        openButton.setForeground(new Color(24, 119, 242));
        openButton.setBackground(UIManager.getColor("Panel.background"));
        openButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        openButton.setFocusPainted(false);
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openButton.addActionListener(e -> openNoteAction(note));

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(new Color(220, 53, 69));
        deleteBtn.setBackground(UIManager.getColor("Panel.background"));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this note?",
                    "Confirm Delete",
                    javax.swing.JOptionPane.YES_NO_OPTION);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                com.notegraph.frontend.api.ApiClient.deleteNote(note.getId());
                // Refresh the overview stats panel via the parent dashboard
                for (java.awt.Component c : parentFrame.getMainCardPanel().getComponents()) {
                    if (c instanceof DashboardOverviewPanel) {
                        ((DashboardOverviewPanel) c).loadRealData();
                        break;
                    }
                }
                this.refreshData();
            }
        });

        Color base = UIManager.getColor("Panel.background");
        Color hover = base.brighter();
        rowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openNoteAction(note);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rowPanel.setBackground(hover);
                leftPanel.setBackground(hover);
                rightPanel.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rowPanel.setBackground(base);
                leftPanel.setBackground(base);
                rightPanel.setBackground(base);
            }
        });

        rightPanel.add(deleteBtn);
        rightPanel.add(openButton);
        rowPanel.add(rightPanel, BorderLayout.EAST);

        return rowPanel;
    }

    private void openNoteAction(NoteDto note) {
        TextNoteEditorPanel editor = parentFrame.getTextNoteEditorPanel();
        if (editor != null) {
            editor.loadNote(note);
            parentFrame.showEditor(editor);
        }
    }
}
