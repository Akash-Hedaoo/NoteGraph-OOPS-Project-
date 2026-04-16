package com.notegraph.ui.pages;

import com.notegraph.ui.api.ApiClient;
import com.notegraph.ui.api.ApiModels;
import com.notegraph.ui.components.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Obsidian-style Knowledge Graph — force-directed interactive visualization.
 * Notes are circular nodes, tags are hub nodes; edges connect notes to their tags.
 * Light theme with colored glows and smooth physics simulation.
 */
public class KnowledgeGraphPanel extends JPanel {

    private Consumer<String> onNavigate;
    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    private Timer physicsTimer;
    private GraphNode hoveredNode = null;
    private GraphNode draggedNode = null;

    // Camera / viewport
    private double offsetX = 0, offsetY = 0;
    private double zoom = 1.0;
    private boolean isPanning = false;
    private Point panStart;

    // Physics constants
    private static final double REPULSION = 8000.0;
    private static final double ATTRACTION = 0.005;
    private static final double SPRING_LENGTH = 180.0;
    private static final double GRAVITY = 0.02;
    private static final double DAMPING = 0.85;
    private static final double MIN_VELOCITY = 0.01;
    private int settleCountdown = 300; // frames before pausing physics

    // Node styling
    private static final int NOTE_RADIUS = 18;
    private static final int TAG_RADIUS = 28;

    private static final Color[] TAG_COLORS = {
        new Color(99, 102, 241),   // indigo
        new Color(16, 185, 129),   // emerald
        new Color(245, 158, 11),   // amber
        new Color(239, 68, 68),    // red
        new Color(139, 92, 246),   // violet
        new Color(59, 130, 246),   // blue
        new Color(236, 72, 153),   // pink
        new Color(20, 184, 166),   // teal
    };

    // Legend & stats
    private int noteCount = 0;
    private int tagCount = 0;
    private int edgeCount = 0;

    public KnowledgeGraphPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 24, 0, 24));

        JLabel title = new JLabel("Knowledge Graph");
        title.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, 24));
        title.setForeground(ColorScheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Visualize how your notes connect — drag, zoom, and explore your second brain.");
        subtitle.setFont(ColorScheme.FONT_REGULAR);
        subtitle.setForeground(ColorScheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(subtitle);
        header.add(Box.createVerticalStrut(16));

        add(header, BorderLayout.NORTH);

        // Canvas
        GraphCanvas canvas = new GraphCanvas();
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBorder(new EmptyBorder(0, 24, 20, 24));
        canvasWrapper.setOpaque(false);
        canvasWrapper.add(canvas, BorderLayout.CENTER);
        add(canvasWrapper, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════
    //  GRAPH DATA MODEL
    // ═══════════════════════════════════════════════
    static class GraphNode {
        String id;
        String label;
        boolean isTag;
        Color color;
        double x, y;
        double vx, vy;
        int radius;

        GraphNode(String id, String label, boolean isTag, Color color, int radius) {
            this.id = id;
            this.label = label;
            this.isTag = isTag;
            this.color = color;
            this.radius = radius;
            // Random initial position
            this.x = (Math.random() - 0.5) * 600;
            this.y = (Math.random() - 0.5) * 400;
        }
    }

    static class GraphEdge {
        GraphNode from;
        GraphNode to;
        Color color;

        GraphEdge(GraphNode from, GraphNode to, Color color) {
            this.from = from;
            this.to = to;
            this.color = color;
        }
    }

    // ═══════════════════════════════════════════════
    //  DATA LOADING
    // ═══════════════════════════════════════════════
    public void refreshData() {
        new SwingWorker<Void, Void>() {
            List<ApiModels.Note> notesList = new ArrayList<>();
            List<ApiModels.Tag> tagsList = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                String wsId = ApiClient.get().getWorkspaceId();
                if (wsId == null) return null;
                try { notesList = ApiClient.get().getWorkspaceNotes(wsId); } catch (Exception e) { e.printStackTrace(); }
                try { tagsList = ApiClient.get().getWorkspaceTags(wsId); } catch (Exception e) { e.printStackTrace(); }
                return null;
            }

            @Override
            protected void done() {
                buildGraph(notesList, tagsList);
            }
        }.execute();
    }

    private void buildGraph(List<ApiModels.Note> notesList, List<ApiModels.Tag> tagsList) {
        nodes.clear();
        edges.clear();

        // Create tag hub nodes
        for (int i = 0; i < tagsList.size(); i++) {
            ApiModels.Tag tag = tagsList.get(i);
            Color c = TAG_COLORS[i % TAG_COLORS.length];
            nodes.add(new GraphNode("tag:" + tag.id, "#" + tag.name, true, c, TAG_RADIUS));
        }

        // Create note nodes
        for (ApiModels.Note note : notesList) {
            // Determine primary color from first tag
            Color noteColor = new Color(100, 116, 139); // slate default
            if (note.tags != null && !note.tags.isEmpty()) {
                // Find matching tag node color
                for (GraphNode n : nodes) {
                    if (n.isTag && n.id.equals("tag:" + note.tags.get(0).id)) {
                        noteColor = n.color;
                        break;
                    }
                }
            }
            String label = (note.title != null && !note.title.isEmpty()) ? note.title : "Untitled";
            if (label.length() > 20) label = label.substring(0, 18) + "…";
            nodes.add(new GraphNode("note:" + note.id, label, false, noteColor, NOTE_RADIUS));
        }

        // Create edges: note → tag
        for (ApiModels.Note note : notesList) {
            if (note.tags == null) continue;
            GraphNode noteNode = findNode("note:" + note.id);
            if (noteNode == null) continue;

            for (ApiModels.Tag tag : note.tags) {
                GraphNode tagNode = findNode("tag:" + tag.id);
                if (tagNode != null) {
                    edges.add(new GraphEdge(noteNode, tagNode, tagNode.color));
                }
            }
        }

        // Also connect notes that share no tags to a virtual "Untagged" hub
        List<GraphNode> untaggedNotes = new ArrayList<>();
        for (ApiModels.Note note : notesList) {
            if (note.tags == null || note.tags.isEmpty()) {
                GraphNode n = findNode("note:" + note.id);
                if (n != null) untaggedNotes.add(n);
            }
        }
        if (!untaggedNotes.isEmpty()) {
            GraphNode untaggedHub = new GraphNode("tag:__untagged__", "Untagged", true,
                    new Color(148, 163, 184), TAG_RADIUS);
            nodes.add(untaggedHub);
            for (GraphNode n : untaggedNotes) {
                edges.add(new GraphEdge(n, untaggedHub, untaggedHub.color));
            }
        }

        noteCount = (int) nodes.stream().filter(n -> !n.isTag).count();
        tagCount = (int) nodes.stream().filter(n -> n.isTag).count();
        edgeCount = edges.size();

        // Reset physics
        settleCountdown = 300;
        startPhysics();
        repaint();
    }

    private GraphNode findNode(String id) {
        for (GraphNode n : nodes) if (n.id.equals(id)) return n;
        return null;
    }

    // ═══════════════════════════════════════════════
    //  PHYSICS SIMULATION
    // ═══════════════════════════════════════════════
    private void startPhysics() {
        if (physicsTimer != null && physicsTimer.isRunning()) return;
        physicsTimer = new Timer(16, e -> {
            if (nodes.isEmpty()) return;
            stepPhysics();
            repaint();
            settleCountdown--;
            if (settleCountdown <= 0) {
                // Check if settled
                double maxV = 0;
                for (GraphNode n : nodes) maxV = Math.max(maxV, Math.abs(n.vx) + Math.abs(n.vy));
                if (maxV < MIN_VELOCITY) physicsTimer.stop();
            }
        });
        physicsTimer.start();
    }

    private void stepPhysics() {
        // Repulsion between all pairs
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                GraphNode a = nodes.get(i);
                GraphNode b = nodes.get(j);
                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 1) dist = 1;
                double force = REPULSION / (dist * dist);
                double fx = (dx / dist) * force;
                double fy = (dy / dist) * force;
                a.vx -= fx;
                a.vy -= fy;
                b.vx += fx;
                b.vy += fy;
            }
        }

        // Attraction along edges
        for (GraphEdge edge : edges) {
            double dx = edge.to.x - edge.from.x;
            double dy = edge.to.y - edge.from.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 1) dist = 1;
            double force = (dist - SPRING_LENGTH) * ATTRACTION;
            double fx = (dx / dist) * force;
            double fy = (dy / dist) * force;
            edge.from.vx += fx;
            edge.from.vy += fy;
            edge.to.vx -= fx;
            edge.to.vy -= fy;
        }

        // Center gravity
        for (GraphNode n : nodes) {
            n.vx -= n.x * GRAVITY;
            n.vy -= n.y * GRAVITY;
        }

        // Apply velocity + damping
        for (GraphNode n : nodes) {
            if (n == draggedNode) continue; // skip dragged node
            n.vx *= DAMPING;
            n.vy *= DAMPING;
            n.x += n.vx;
            n.y += n.vy;
        }
    }

    // ═══════════════════════════════════════════════
    //  GRAPH CANVAS (Custom painting)
    // ═══════════════════════════════════════════════
    class GraphCanvas extends JPanel {

        GraphCanvas() {
            setBackground(new Color(248, 250, 252)); // light slate bg
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    handleMouseMove(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggedNode != null) {
                        // Drag node
                        Point2D world = screenToWorld(e.getPoint());
                        draggedNode.x = world.getX();
                        draggedNode.y = world.getY();
                        draggedNode.vx = 0;
                        draggedNode.vy = 0;
                        repaint();
                    } else if (isPanning) {
                        // Pan canvas
                        int dx = e.getX() - panStart.x;
                        int dy = e.getY() - panStart.y;
                        offsetX += dx;
                        offsetY += dy;
                        panStart = e.getPoint();
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    GraphNode hit = hitTest(e.getPoint());
                    if (hit != null) {
                        draggedNode = hit;
                        settleCountdown = 120;
                        if (physicsTimer != null && !physicsTimer.isRunning()) startPhysics();
                    } else {
                        isPanning = true;
                        panStart = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggedNode != null) {
                        draggedNode = null;
                    }
                    if (isPanning) {
                        isPanning = false;
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    GraphNode hit = hitTest(e.getPoint());
                    if (hit != null && !hit.isTag) {
                        String noteId = hit.id.replace("note:", "");
                        onNavigate.accept("editor:" + noteId);
                    }
                }
            });

            addMouseWheelListener(e -> {
                double scaleFactor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                double newZoom = zoom * scaleFactor;
                if (newZoom < 0.2 || newZoom > 5.0) return;

                // Zoom toward mouse position
                double mx = e.getX();
                double my = e.getY();
                offsetX = mx - (mx - offsetX) * (newZoom / zoom);
                offsetY = my - (my - offsetY) * (newZoom / zoom);
                zoom = newZoom;
                repaint();
            });
        }

        private void handleMouseMove(MouseEvent e) {
            GraphNode prev = hoveredNode;
            hoveredNode = hitTest(e.getPoint());
            if (prev != hoveredNode) {
                if (hoveredNode != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setToolTipText(hoveredNode.isTag ? "Tag: " + hoveredNode.label : hoveredNode.label + " (click to open)");
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    setToolTipText(null);
                }
                repaint();
            }
        }

        private GraphNode hitTest(Point screenPoint) {
            Point2D world = screenToWorld(screenPoint);
            for (GraphNode n : nodes) {
                double dx = n.x - world.getX();
                double dy = n.y - world.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist <= n.radius + 4) return n;
            }
            return null;
        }

        private Point2D screenToWorld(Point screen) {
            double wx = (screen.x - offsetX - getWidth() / 2.0) / zoom;
            double wy = (screen.y - offsetY - getHeight() / 2.0) / zoom;
            return new Point2D.Double(wx, wy);
        }

        private double worldToScreenX(double wx) {
            return wx * zoom + getWidth() / 2.0 + offsetX;
        }

        private double worldToScreenY(double wy) {
            return wy * zoom + getHeight() / 2.0 + offsetY;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // ── Dot grid background ──
            g2.setColor(new Color(10, 102, 240, 15));
            for (int x = 0; x < w; x += 24) {
                for (int y = 0; y < h; y += 24) {
                    g2.fillOval(x, y, 2, 2);
                }
            }

            if (nodes.isEmpty()) {
                drawEmptyState(g2, w, h);
                g2.dispose();
                return;
            }

            // Determine connected set for hover highlighting
            java.util.Set<String> connectedIds = new java.util.HashSet<>();
            if (hoveredNode != null) {
                connectedIds.add(hoveredNode.id);
                for (GraphEdge edge : edges) {
                    if (edge.from == hoveredNode) connectedIds.add(edge.to.id);
                    if (edge.to == hoveredNode) connectedIds.add(edge.from.id);
                }
            }

            // ── Draw edges ──
            for (GraphEdge edge : edges) {
                double x1 = worldToScreenX(edge.from.x);
                double y1 = worldToScreenY(edge.from.y);
                double x2 = worldToScreenX(edge.to.x);
                double y2 = worldToScreenY(edge.to.y);

                boolean highlighted = hoveredNode != null &&
                        (edge.from == hoveredNode || edge.to == hoveredNode);
                boolean dimmed = hoveredNode != null && !highlighted;

                Color edgeColor = edge.color;
                if (highlighted) {
                    g2.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 200));
                    g2.setStroke(new BasicStroke((float)(2.5 * zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else if (dimmed) {
                    g2.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 30));
                    g2.setStroke(new BasicStroke((float)(1.0 * zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else {
                    g2.setColor(new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 80));
                    g2.setStroke(new BasicStroke((float)(1.5 * zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
                g2.draw(new Line2D.Double(x1, y1, x2, y2));
            }

            // ── Draw nodes ──
            for (GraphNode node : nodes) {
                drawNode(g2, node, connectedIds);
            }

            // ── Legend overlay ──
            drawLegend(g2, w, h);

            g2.dispose();
        }

        private void drawNode(Graphics2D g2, GraphNode node, java.util.Set<String> connectedIds) {
            double sx = worldToScreenX(node.x);
            double sy = worldToScreenY(node.y);
            double r = node.radius * zoom;

            boolean isHovered = node == hoveredNode;
            boolean isConnected = connectedIds.contains(node.id);
            boolean isDimmed = hoveredNode != null && !isConnected;

            if (isDimmed) {
                // Very faint
                Color faint = new Color(node.color.getRed(), node.color.getGreen(), node.color.getBlue(), 40);
                g2.setColor(faint);
                g2.fill(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
                // Faint label
                if (zoom > 0.5) {
                    g2.setColor(new Color(100, 116, 139, 50));
                    g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, (int) Math.max(9, 11 * zoom)));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(node.label, (float)(sx - fm.stringWidth(node.label) / 2.0), (float)(sy + r + 14 * zoom));
                }
                return;
            }

            // Glow
            if (isHovered || isConnected) {
                Color glow = new Color(node.color.getRed(), node.color.getGreen(), node.color.getBlue(), isHovered ? 50 : 25);
                double glowR = r * (isHovered ? 2.2 : 1.6);
                g2.setColor(glow);
                g2.fill(new Ellipse2D.Double(sx - glowR, sy - glowR, glowR * 2, glowR * 2));
            }

            if (node.isTag) {
                // Tag hub: solid colored circle
                g2.setColor(node.color);
                g2.fill(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
                // White border
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke((float)(2 * zoom)));
                g2.draw(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
                // Tag icon text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(ColorScheme.FONT_BOLD.getFamily(), Font.BOLD, (int) Math.max(10, 13 * zoom)));
                FontMetrics fm = g2.getFontMetrics();
                String label = node.label;
                if (fm.stringWidth(label) > r * 2 - 4) label = label.substring(0, Math.min(label.length(), 4)) + "…";
                g2.drawString(label, (float)(sx - fm.stringWidth(label) / 2.0), (float)(sy + fm.getAscent() / 2.0 - 1));
            } else {
                // Note node: white circle with colored border
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
                g2.setColor(node.color);
                g2.setStroke(new BasicStroke((float)(2.0 * zoom)));
                g2.draw(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));

                // Small inner dot
                double dotR = 3 * zoom;
                g2.setColor(node.color);
                g2.fill(new Ellipse2D.Double(sx - dotR, sy - dotR, dotR * 2, dotR * 2));
            }

            // Label below node
            if (zoom > 0.4) {
                g2.setColor(isHovered ? ColorScheme.TEXT_PRIMARY : ColorScheme.TEXT_SECONDARY);
                int fontSize = (int) Math.max(9, (node.isTag ? 12 : 11) * zoom);
                g2.setFont(new Font(
                        (isHovered ? ColorScheme.FONT_SEMIBOLD : ColorScheme.FONT_REGULAR).getFamily(),
                        isHovered ? Font.BOLD : Font.PLAIN,
                        fontSize));
                FontMetrics fm = g2.getFontMetrics();
                String label = node.label;
                float lx = (float)(sx - fm.stringWidth(label) / 2.0);
                float ly = (float)(sy + r + 14 * zoom);
                // White background for readability
                g2.setColor(new Color(248, 250, 252, 200));
                g2.fillRect((int) lx - 2, (int) ly - fm.getAscent(), fm.stringWidth(label) + 4, fm.getHeight());
                g2.setColor(isHovered ? ColorScheme.TEXT_PRIMARY : ColorScheme.TEXT_SECONDARY);
                g2.drawString(label, lx, ly);
            }
        }

        private void drawEmptyState(Graphics2D g2, int w, int h) {
            // Empty state
            g2.setColor(ColorScheme.TEXT_TERTIARY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 48));
            String icon = "🧠";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, (w - fm.stringWidth(icon)) / 2, h / 2 - 30);

            g2.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 18));
            fm = g2.getFontMetrics();
            String msg = "Your Knowledge Graph is Empty";
            g2.setColor(ColorScheme.TEXT_PRIMARY);
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2 + 20);

            g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 14));
            fm = g2.getFontMetrics();
            String sub = "Create notes and add tags to see your neural knowledge network come alive!";
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 + 46);
        }

        private void drawLegend(Graphics2D g2, int w, int h) {
            // Bottom-left stats box
            int boxW = 200, boxH = 80;
            int bx = 16, by = h - boxH - 16;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);
            g2.setColor(ColorScheme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(bx, by, boxW, boxH, 12, 12);

            g2.setColor(ColorScheme.TEXT_TERTIARY);
            g2.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 10));
            g2.drawString("GRAPH STATS", bx + 12, by + 18);

            g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 12));
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.drawString("📄 " + noteCount + " Notes", bx + 12, by + 38);
            g2.drawString("🏷 " + tagCount + " Tags", bx + 12, by + 56);
            g2.drawString("🔗 " + edgeCount + " Connections", bx + 12, by + 74);

            // Top-right legend
            int lx = w - 180, ly = 16;
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(lx, ly, 164, 68, 12, 12);
            g2.setColor(ColorScheme.BORDER);
            g2.drawRoundRect(lx, ly, 164, 68, 12, 12);

            g2.setColor(ColorScheme.TEXT_TERTIARY);
            g2.setFont(new Font(ColorScheme.FONT_SEMIBOLD.getFamily(), Font.BOLD, 10));
            g2.drawString("LEGEND", lx + 12, ly + 18);

            // Tag circle
            g2.setColor(new Color(99, 102, 241));
            g2.fillOval(lx + 12, ly + 26, 12, 12);
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 11));
            g2.drawString("Tag Hub", lx + 30, ly + 37);

            // Note circle
            g2.setColor(Color.WHITE);
            g2.fillOval(lx + 12, ly + 46, 12, 12);
            g2.setColor(new Color(99, 102, 241));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(lx + 12, ly + 46, 12, 12);
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.setFont(new Font(ColorScheme.FONT_REGULAR.getFamily(), Font.PLAIN, 11));
            g2.drawString("Note", lx + 30, ly + 57);

            // Zoom level indicator (bottom-right)
            String zoomText = String.format("%.0f%%", zoom * 100);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(w - 70, h - 36, 54, 24, 8, 8);
            g2.setColor(ColorScheme.BORDER);
            g2.drawRoundRect(w - 70, h - 36, 54, 24, 8, 8);
            g2.setColor(ColorScheme.TEXT_SECONDARY);
            g2.setFont(new Font(ColorScheme.FONT_MEDIUM.getFamily(), Font.PLAIN, 11));
            FontMetrics fmZoom = g2.getFontMetrics();
            g2.drawString(zoomText, w - 70 + (54 - fmZoom.stringWidth(zoomText)) / 2, h - 36 + 16);
        }
    }
}
