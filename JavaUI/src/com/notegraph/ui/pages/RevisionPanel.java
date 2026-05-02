package com.notegraph.ui.pages;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;
import com.notegraph.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RevisionPanel extends JPanel {

    private Consumer<String> onNavigate;
    private List<ApiModels.RevisionPlanItem> planItems = new ArrayList<>();
    private List<ApiModels.RevisionScheduleDto> scheduledRevisions = new ArrayList<>();
    private JPanel planGrid;
    private JPanel scheduledList;
    private JLabel statusLabel;

    public RevisionPanel(Consumer<String> onNavigate) {
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
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        header.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        JLabel title = new JLabel("Revision Checklist");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);
        headerText.add(title);
        JLabel subtitle = new JLabel("AI-powered spaced repetition based on the Ebbinghaus forgetting curve");
        subtitle.setFont(ColorScheme.FONT_REGULAR);
        subtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(subtitle);
        header.add(headerText, BorderLayout.CENTER);

        JButton generateBtn = createPrimaryButton("✦ Generate AI Plan");
        generateBtn.addActionListener(e -> generatePlan());
        header.add(generateBtn, BorderLayout.EAST);

        content.add(header);
        content.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.ITALIC, 13));
        statusLabel.setForeground(ColorScheme.PRIMARY_BLUE);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(16));

        // AI Plan Section
        JLabel planTitle = new JLabel("AI Revision Plan");
        planTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 18));
        planTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        planTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(planTitle);
        content.add(Box.createVerticalStrut(12));

        planGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        planGrid.setOpaque(false);
        planGrid.setAlignmentX(LEFT_ALIGNMENT);
        JLabel emptyPlan = new JLabel("Click 'Generate AI Plan' to analyze your notes.");
        emptyPlan.setFont(ColorScheme.FONT_REGULAR);
        emptyPlan.setForeground(ColorScheme.TEXT_TERTIARY);
        planGrid.add(emptyPlan);
        content.add(planGrid);
        content.add(Box.createVerticalStrut(28));

        // Scheduled Revisions Section
        JLabel schedTitle = new JLabel("Scheduled Revisions");
        schedTitle.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 18));
        schedTitle.setForeground(ColorScheme.TEXT_PRIMARY);
        schedTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(schedTitle);
        content.add(Box.createVerticalStrut(12));

        scheduledList = new JPanel();
        scheduledList.setLayout(new BoxLayout(scheduledList, BoxLayout.Y_AXIS));
        scheduledList.setOpaque(false);
        scheduledList.setAlignmentX(LEFT_ALIGNMENT);
        JLabel emptySchedule = new JLabel("No revisions scheduled yet.");
        emptySchedule.setFont(ColorScheme.FONT_REGULAR);
        emptySchedule.setForeground(ColorScheme.TEXT_TERTIARY);
        scheduledList.add(emptySchedule);
        content.add(scheduledList);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void generatePlan() {
        statusLabel.setText("⏳ Generating AI revision plan... This may take a moment.");
        statusLabel.setForeground(ColorScheme.PRIMARY_BLUE);
        new SwingWorker<List<ApiModels.RevisionPlanItem>, Void>() {
            @Override
            protected List<ApiModels.RevisionPlanItem> doInBackground() throws Exception {
                return ApiClient.get().generateRevisionPlan(ApiClient.get().getWorkspaceId());
            }
            @Override
            protected void done() {
                try {
                    planItems = get();
                    statusLabel.setText("✓ Plan generated for " + planItems.size() + " notes");
                    statusLabel.setForeground(ColorScheme.SUCCESS_TEXT);
                    rebuildPlanGrid();
                } catch (Exception e) {
                    statusLabel.setText("✗ " + e.getMessage());
                    statusLabel.setForeground(ColorScheme.ERROR_TEXT);
                }
            }
        }.execute();
    }

    private void rebuildPlanGrid() {
        planGrid.removeAll();
        if (planItems.isEmpty()) {
            JLabel empty = new JLabel("No notes found in this workspace.");
            empty.setFont(ColorScheme.FONT_REGULAR);
            empty.setForeground(ColorScheme.TEXT_TERTIARY);
            planGrid.add(empty);
        } else {
            for (ApiModels.RevisionPlanItem item : planItems) {
                planGrid.add(createPlanCard(item));
            }
        }
        planGrid.revalidate();
        planGrid.repaint();
    }

    private RoundedPanel createPlanCard(ApiModels.RevisionPlanItem item) {
        RoundedPanel card = new RoundedPanel(12, ColorScheme.BORDER, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Title row
        JLabel titleLbl = new JLabel(item.title != null ? item.title : "Untitled");
        titleLbl.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 15));
        titleLbl.setForeground(ColorScheme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));

        // Complexity + Urgency row
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(LEFT_ALIGNMENT);
        badges.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        Color compColor = item.complexity <= 3 ? ColorScheme.SUCCESS_TEXT :
                           item.complexity <= 6 ? ColorScheme.TAG_ORANGE_TEXT : ColorScheme.ERROR_TEXT;
        Color compBg = item.complexity <= 3 ? ColorScheme.SUCCESS_BG :
                        item.complexity <= 6 ? ColorScheme.TAG_ORANGE_BG : ColorScheme.ERROR_BG;
        JLabel compBadge = createBadge("Complexity: " + item.complexity + "/10", compColor, compBg);
        badges.add(compBadge);

        Color urgColor = "HIGH".equals(item.urgency) ? ColorScheme.ERROR_TEXT :
                          "MEDIUM".equals(item.urgency) ? ColorScheme.TAG_ORANGE_TEXT : ColorScheme.SUCCESS_TEXT;
        Color urgBg = "HIGH".equals(item.urgency) ? ColorScheme.ERROR_BG :
                       "MEDIUM".equals(item.urgency) ? ColorScheme.TAG_ORANGE_BG : ColorScheme.SUCCESS_BG;
        JLabel urgBadge = createBadge(item.urgency, urgColor, urgBg);
        badges.add(urgBadge);
        card.add(badges);
        card.add(Box.createVerticalStrut(10));

        // Complexity bar
        JPanel barContainer = new JPanel(new BorderLayout());
        barContainer.setOpaque(false);
        barContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        barContainer.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(229, 231, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int fillWidth = (int) (getWidth() * (item.complexity / 10.0));
                g2.setColor(compColor);
                g2.fillRoundRect(0, 0, fillWidth, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 6));
        barContainer.add(bar);
        card.add(barContainer);
        card.add(Box.createVerticalStrut(10));

        // Reason
        if (item.reason != null && !item.reason.isEmpty()) {
            JLabel reasonLbl = new JLabel("<html><body style='width:220px'>" + item.reason + "</body></html>");
            reasonLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
            reasonLbl.setForeground(ColorScheme.TEXT_SECONDARY);
            reasonLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(reasonLbl);
            card.add(Box.createVerticalStrut(10));
        }

        // Suggested dates
        if (item.suggestedDates != null && !item.suggestedDates.isEmpty()) {
            JLabel datesHeader = new JLabel("Suggested dates:");
            datesHeader.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.BOLD, 11));
            datesHeader.setForeground(ColorScheme.TEXT_TERTIARY);
            datesHeader.setAlignmentX(LEFT_ALIGNMENT);
            card.add(datesHeader);
            card.add(Box.createVerticalStrut(4));
            for (String date : item.suggestedDates) {
                String formatted = formatIsoDate(date);
                JLabel dateLbl = new JLabel("  • " + formatted);
                dateLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
                dateLbl.setForeground(ColorScheme.TEXT_SECONDARY);
                dateLbl.setAlignmentX(LEFT_ALIGNMENT);
                card.add(dateLbl);
            }
            card.add(Box.createVerticalStrut(10));
        }

        // Schedule button
        JButton scheduleBtn = createPrimaryButton("Schedule Revision");
        scheduleBtn.setAlignmentX(LEFT_ALIGNMENT);
        scheduleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        scheduleBtn.addActionListener(e -> showScheduleDialog(item));
        card.add(scheduleBtn);

        return card;
    }

    private void showScheduleDialog(ApiModels.RevisionPlanItem item) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        String[] years = {"2025", "2026", "2027"};
        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) hours[i] = String.format("%02d", i);
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) minutes[i] = String.format("%02d", i);

        LocalDateTime now = LocalDateTime.now();
        JComboBox<String> yearBox = new JComboBox<>(years);
        JComboBox<String> monthBox = new JComboBox<>(months);
        monthBox.setSelectedItem(String.format("%02d", now.getMonthValue()));
        JComboBox<String> dayBox = new JComboBox<>(days);
        dayBox.setSelectedItem(String.format("%02d", Math.min(now.getDayOfMonth() + 1, 28)));
        JComboBox<String> hourBox = new JComboBox<>(hours);
        hourBox.setSelectedItem(String.format("%02d", now.getHour()));
        JComboBox<String> minuteBox = new JComboBox<>(minutes);

        panel.add(new JLabel("Year:")); panel.add(yearBox);
        panel.add(new JLabel("Month:")); panel.add(monthBox);
        panel.add(new JLabel("Day:")); panel.add(dayBox);
        panel.add(new JLabel("Hour:")); panel.add(hourBox);
        panel.add(new JLabel("Minute:")); panel.add(minuteBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Schedule Revision — " + item.title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String dateStr = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-"
                    + dayBox.getSelectedItem() + "T" + hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + ":00";
            scheduleRevision(item, dateStr);
        }
    }

    private void scheduleRevision(ApiModels.RevisionPlanItem item, String dateStr) {
        String userId = ApiClient.get().getUser() != null ? ApiClient.get().getUser().id : null;
        if (userId == null) return;
        ApiModels.RevisionRequest req = new ApiModels.RevisionRequest(
                item.noteId, userId, dateStr, item.complexity, item.reason);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.get().scheduleRevision(req);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("✓ Revision scheduled for " + item.title);
                    statusLabel.setForeground(ColorScheme.SUCCESS_TEXT);
                    // Remove scheduled note from plan
                    planItems.removeIf(p -> p.noteId.equals(item.noteId));
                    rebuildPlanGrid();
                    loadScheduledRevisions();
                } catch (Exception e) {
                    statusLabel.setText("✗ Failed to schedule: " + e.getMessage());
                    statusLabel.setForeground(ColorScheme.ERROR_TEXT);
                }
            }
        }.execute();
    }

    private void loadScheduledRevisions() {
        String wsId = ApiClient.get().getWorkspaceId();
        String userId = ApiClient.get().getUser() != null ? ApiClient.get().getUser().id : null;
        if (wsId == null || userId == null) return;
        new SwingWorker<List<ApiModels.RevisionScheduleDto>, Void>() {
            @Override
            protected List<ApiModels.RevisionScheduleDto> doInBackground() throws Exception {
                return ApiClient.get().getRevisions(wsId, userId);
            }
            @Override
            protected void done() {
                try {
                    scheduledRevisions = get();
                    rebuildScheduledList();
                } catch (Exception e) { /* ignore */ }
            }
        }.execute();
    }

    private void rebuildScheduledList() {
        scheduledList.removeAll();
        if (scheduledRevisions.isEmpty()) {
            JLabel empty = new JLabel("No revisions scheduled yet.");
            empty.setFont(ColorScheme.FONT_REGULAR);
            empty.setForeground(ColorScheme.TEXT_TERTIARY);
            scheduledList.add(empty);
        } else {
            for (ApiModels.RevisionScheduleDto rev : scheduledRevisions) {
                scheduledList.add(createScheduledRow(rev));
                scheduledList.add(Box.createVerticalStrut(8));
            }
        }
        scheduledList.revalidate();
        scheduledList.repaint();
    }

    private RoundedPanel createScheduledRow(ApiModels.RevisionScheduleDto rev) {
        RoundedPanel row = new RoundedPanel(10, ColorScheme.BORDER, false);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Status icon
        String statusIcon = switch (rev.status != null ? rev.status : "") {
            case "SENT" -> "✉️";
            case "COMPLETED" -> "✅";
            case "SKIPPED" -> "⏭";
            default -> "⏳";
        };
        JLabel iconLbl = new JLabel(statusIcon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        row.add(iconLbl, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        String noteTitle = rev.note != null ? rev.note.title : "Note";
        JLabel nameLbl = new JLabel(noteTitle);
        nameLbl.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 14));
        nameLbl.setForeground(ColorScheme.TEXT_PRIMARY);
        info.add(nameLbl);
        JLabel timeLbl = new JLabel("Scheduled: " + formatIsoDate(rev.scheduledAt) + " • " + (rev.status != null ? rev.status : "PENDING"));
        timeLbl.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
        timeLbl.setForeground(ColorScheme.TEXT_SECONDARY);
        info.add(timeLbl);
        row.add(info, BorderLayout.CENTER);

        // Actions
        if ("PENDING".equals(rev.status) || "SENT".equals(rev.status)) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            actions.setOpaque(false);
            JButton completeBtn = createSmallButton("✓ Done", ColorScheme.SUCCESS_TEXT);
            completeBtn.addActionListener(e -> updateStatus(rev.id, "COMPLETED"));
            JButton skipBtn = createSmallButton("Skip", ColorScheme.TEXT_TERTIARY);
            skipBtn.addActionListener(e -> updateStatus(rev.id, "SKIPPED"));
            JButton deleteBtn = createSmallButton("✗", ColorScheme.ERROR_TEXT);
            deleteBtn.addActionListener(e -> deleteRevision(rev.id));
            actions.add(completeBtn);
            actions.add(skipBtn);
            actions.add(deleteBtn);
            row.add(actions, BorderLayout.EAST);
        }

        return row;
    }

    private void updateStatus(String id, String status) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.get().updateRevisionStatus(id, status);
                return null;
            }
            @Override protected void done() { loadScheduledRevisions(); }
        }.execute();
    }

    private void deleteRevision(String id) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.get().deleteRevision(id);
                return null;
            }
            @Override protected void done() { loadScheduledRevisions(); }
        }.execute();
    }

    public void refreshData() {
        loadScheduledRevisions();
    }

    // ── Helpers ──────────────────────────────────
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ColorScheme.PRIMARY_BLUE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 38));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ColorScheme.PRIMARY_BLUE_HOVER); btn.repaint(); }
            public void mouseExited(MouseEvent e) { btn.setBackground(ColorScheme.PRIMARY_BLUE); btn.repaint(); }
        });
        return btn;
    }

    private JButton createSmallButton(String text, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 12));
        btn.setForeground(fg);
        btn.setBackground(null);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createBadge(String text, Color fg, Color bg) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        return badge;
    }

    private String formatIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "N/A";
        try {
            String clean = dateStr.length() > 19 ? dateStr.substring(0, 19) : dateStr;
            LocalDateTime dt = LocalDateTime.parse(clean);
            return dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}
