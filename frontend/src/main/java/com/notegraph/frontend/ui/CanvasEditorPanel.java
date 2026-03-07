package com.notegraph.frontend.ui;

import com.notegraph.frontend.canvas.CanvasElement;
import com.notegraph.frontend.canvas.CircleShape;
import com.notegraph.frontend.canvas.CodeBlockElement;
import com.notegraph.frontend.canvas.FreehandShape;
import com.notegraph.frontend.canvas.RectangleShape;
import com.notegraph.frontend.canvas.TextElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class CanvasEditorPanel extends JPanel {

    private List<CanvasElement> elements = new ArrayList<>();
    private String currentMode = "RECTANGLE";
    private FreehandShape currentFreehand = null;
    private CanvasElement currentDynamicShape = null;
    private int startX, startY;
    private Long currentNoteId = null;

    public void setCurrentNoteId(Long id) {
        this.currentNoteId = id;
    }

    private JLabel noteTitleHeader;

    public void loadNote(com.notegraph.frontend.model.NoteDto note) {
        this.currentNoteId = note.getId();
        if (noteTitleHeader != null) {
            noteTitleHeader.setText(note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle()
                    : "Untitled Canvas Note");
        }
        // Additional load logic could go here
    }

    public CanvasEditorPanel() {
        setLayout(new BorderLayout());

        // --- Toolbar area ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton rectBtn = new JButton("Draw Rectangle");
        rectBtn.setBackground(new Color(60, 60, 60));
        rectBtn.setForeground(Color.WHITE);
        rectBtn.setFocusPainted(false);
        rectBtn.addActionListener(e -> currentMode = "RECTANGLE");

        JButton circleBtn = new JButton("Draw Circle");
        circleBtn.setBackground(new Color(60, 60, 60));
        circleBtn.setForeground(Color.WHITE);
        circleBtn.setFocusPainted(false);
        circleBtn.addActionListener(e -> currentMode = "CIRCLE");

        JButton penBtn = new JButton("Pen Tool");
        penBtn.setBackground(new Color(60, 60, 60));
        penBtn.setForeground(Color.WHITE);
        penBtn.setFocusPainted(false);
        penBtn.addActionListener(e -> currentMode = "PEN");

        JButton textBtn = new JButton("Text");
        textBtn.setBackground(new Color(60, 60, 60));
        textBtn.setForeground(Color.WHITE);
        textBtn.setFocusPainted(false);
        textBtn.addActionListener(e -> currentMode = "TEXT");

        JButton codeBtn = new JButton("Code Block");
        codeBtn.setBackground(new Color(60, 60, 60));
        codeBtn.setForeground(Color.WHITE);
        codeBtn.setFocusPainted(false);
        codeBtn.addActionListener(e -> currentMode = "CODE");

        JButton exportBtn = new JButton("Export PNG");
        exportBtn.setBackground(new Color(50, 150, 50));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportToPNG());

        JButton exportPdfBtn = new JButton("Export PDF");
        exportPdfBtn.setBackground(new Color(150, 50, 50));
        exportPdfBtn.setForeground(Color.WHITE);
        exportPdfBtn.setFocusPainted(false);
        exportPdfBtn.addActionListener(e -> exportToPDF());

        JButton exportDocxBtn = new JButton("Export DOCX");
        exportDocxBtn.setBackground(new Color(50, 50, 150));
        exportDocxBtn.setForeground(Color.WHITE);
        exportDocxBtn.setFocusPainted(false);
        exportDocxBtn.addActionListener(e -> exportToDOCX());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setBackground(new Color(150, 50, 50));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            elements.clear();
            repaint();
        });

        JButton deleteBtn = new JButton("Delete Canvas");
        deleteBtn.setBackground(new Color(200, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            if (currentNoteId != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this canvas completely?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    com.notegraph.frontend.api.ApiClient.deleteNote(currentNoteId);
                    elements.clear();
                    repaint();
                    currentNoteId = null;
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window instanceof DashboardFrame) {
                        ((DashboardFrame) window).refreshSidebar();
                    }
                }
            } else {
                elements.clear();
                repaint();
            }
        });

        toolBar.add(rectBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(circleBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(penBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(textBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(codeBtn);
        toolBar.add(Box.createHorizontalGlue()); // Push right
        toolBar.add(exportBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(exportPdfBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(exportDocxBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(clearBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteBtn);

        // --- Note Title Header & Sleek Toolbar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        noteTitleHeader = new JLabel("Untitled Canvas Note");
        noteTitleHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        noteTitleHeader.setForeground(Color.DARK_GRAY);
        headerPanel.add(noteTitleHeader, BorderLayout.WEST);

        JPanel sleekToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        sleekToolbar.setBackground(Color.WHITE);

        JButton shareBtn = new JButton("Share \uD83D\uDD17"); // Placeholder icon
        shareBtn.setForeground(Color.GRAY);
        shareBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        shareBtn.setBorderPainted(false);
        shareBtn.setContentAreaFilled(false);
        shareBtn.setFocusPainted(false);
        shareBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton delHeaderBtn = new JButton("Delete \uD83D\uDDD1"); // Placeholder icon
        delHeaderBtn.setForeground(Color.GRAY);
        delHeaderBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        delHeaderBtn.setBorderPainted(false);
        delHeaderBtn.setContentAreaFilled(false);
        delHeaderBtn.setFocusPainted(false);
        delHeaderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sleekToolbar.add(shareBtn);
        sleekToolbar.add(delHeaderBtn);
        headerPanel.add(sleekToolbar, BorderLayout.EAST);

        // North container to hold both headers
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setBackground(Color.WHITE);
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(toolBar, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);

        // --- Canvas Drawing Surface ---
        JPanel drawingSurface = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Ensure the background is painted correctly

                Graphics2D g2d = (Graphics2D) g;

                // Demonstrates Polymorphism
                for (CanvasElement element : elements) {
                    element.draw(g2d);
                }
            }
        };
        // White background for canvas
        drawingSurface.setBackground(Color.WHITE);

        // Add mouse listener to draw shapes on click
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Create a new random color shape at the click coordinates
                Color randomColor = new Color(
                        (int) (Math.random() * 256),
                        (int) (Math.random() * 256),
                        (int) (Math.random() * 256));

                if ("RECTANGLE".equals(currentMode)) {
                    startX = e.getX();
                    startY = e.getY();
                    currentDynamicShape = new RectangleShape(startX, startY, 0, 0, randomColor);
                    elements.add(currentDynamicShape);
                } else if ("CIRCLE".equals(currentMode)) {
                    startX = e.getX();
                    startY = e.getY();
                    currentDynamicShape = new CircleShape(startX, startY, 0, 0, randomColor);
                    elements.add(currentDynamicShape);
                } else if ("PEN".equals(currentMode)) {
                    currentFreehand = new FreehandShape(e.getX(), e.getY(), Color.BLACK);
                    elements.add(currentFreehand);
                } else if ("TEXT".equals(currentMode)) {
                    String input = JOptionPane.showInputDialog(CanvasEditorPanel.this, "Enter text:");
                    if (input != null && !input.trim().isEmpty()) {
                        elements.add(new TextElement(e.getX(), e.getY(), input,
                                new Font("SansSerif", Font.BOLD, 18), Color.BLACK));
                    }
                } else if ("CODE".equals(currentMode)) {
                    String input = JOptionPane.showInputDialog(CanvasEditorPanel.this, "Enter code snippet:");
                    if (input != null && !input.trim().isEmpty()) {
                        elements.add(new CodeBlockElement(e.getX(), e.getY(), input, "Java"));
                    }
                }

                // Request a UI repaint
                drawingSurface.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (("RECTANGLE".equals(currentMode) || "CIRCLE".equals(currentMode)) && currentDynamicShape != null) {
                    int newX = Math.min(startX, e.getX());
                    int newY = Math.min(startY, e.getY());
                    int newWidth = Math.abs(startX - e.getX());
                    int newHeight = Math.abs(startY - e.getY());

                    currentDynamicShape.setBounds(newX, newY, newWidth, newHeight);
                    drawingSurface.repaint();
                } else if ("PEN".equals(currentMode) && currentFreehand != null) {
                    currentFreehand.addPoint(e.getX(), e.getY());
                    drawingSurface.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentDynamicShape = null;
                currentFreehand = null;
            }
        };

        drawingSurface.addMouseListener(mouseAdapter);
        drawingSurface.addMouseMotionListener(mouseAdapter);

        add(drawingSurface, BorderLayout.CENTER);
    }

    private void exportToPNG() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Canvas as PNG");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Ensure .png extension
            if (!selectedFile.getName().toLowerCase().endsWith(".png")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".png");
            }

            try {
                BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                this.paint(g2d);
                g2d.dispose();
                ImageIO.write(image, "png", selectedFile);
                JOptionPane.showMessageDialog(this, "Canvas exported successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export Canvas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Canvas as PDF");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try {
                BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                this.paint(g2d);
                g2d.dispose();

                com.itextpdf.text.Document document = new com.itextpdf.text.Document(
                        new com.itextpdf.text.Rectangle(getWidth(), getHeight()));
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(baos.toByteArray());
                pdfImage.setAbsolutePosition(0, 0);
                document.add(pdfImage);
                document.close();

                JOptionPane.showMessageDialog(this, "Canvas exported to PDF successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export Canvas to PDF.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToDOCX() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Canvas as DOCX");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".docx")) {
                file = new File(file.getAbsolutePath() + ".docx");
            }
            try {
                BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                this.paint(g2d);
                g2d.dispose();

                org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();
                org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());

                run.addPicture(bais, org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG, "canvas.png",
                        org.apache.poi.util.Units.toEMU(getWidth() > 500 ? 500 : getWidth()), org.apache.poi.util.Units
                                .toEMU(getHeight() > 500 ? (getHeight() * 500 / getWidth()) : getHeight()));

                try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                    document.write(out);
                }
                document.close();

                JOptionPane.showMessageDialog(this, "Canvas exported to DOCX successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export Canvas to DOCX.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
