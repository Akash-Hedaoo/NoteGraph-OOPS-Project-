package com.notegraph.frontend.ui;

import com.notegraph.frontend.model.NoteDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphViewPanel extends JPanel {

    private List<GraphNode> graphNodes = new ArrayList<>();
    private List<GraphEdge> edges = new ArrayList<>();

    private GraphNode draggedNode = null;
    private GraphNode hoveredNode = null;
    private int mouseX = -1;
    private int mouseY = -1;
    private Timer physicsTimer;

    public GraphViewPanel() {
        // Theme-aware background
        setBackground(UIManager.getColor("Panel.background"));

        // Physics Simulation Loop
        physicsTimer = new Timer(30, e -> performPhysicsStep());
        physicsTimer.start();

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Normal left click drag logic
                for (int i = graphNodes.size() - 1; i >= 0; i--) {
                    GraphNode node = graphNodes.get(i);
                    if (node.contains(e.getX(), e.getY())) {
                        draggedNode = node;
                        break;
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = graphNodes.size() - 1; i >= 0; i--) {
                    GraphNode node = graphNodes.get(i);
                    if (Math.hypot(node.x - e.getX(), node.y - e.getY()) <= node.RADIUS) {
                        Window window = SwingUtilities.getWindowAncestor(GraphViewPanel.this);
                        if (window instanceof DashboardFrame) {
                            ((DashboardFrame) window).openNoteInEditor(node.id);
                        }
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.x = e.getX();
                    draggedNode.y = e.getY();
                    draggedNode.dx = 0;
                    draggedNode.dy = 0;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                GraphNode previousHover = hoveredNode;
                hoveredNode = null;
                for (int i = graphNodes.size() - 1; i >= 0; i--) {
                    GraphNode node = graphNodes.get(i);
                    if (node.isHovered(e.getX(), e.getY())) {
                        hoveredNode = node;
                        break;
                    }
                }
                if (previousHover != hoveredNode) {
                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void loadGraphData(List<NoteDto> noteList) {
        graphNodes.clear();
        edges.clear();

        if (noteList != null) {
            java.util.Random random = new java.util.Random(42);
            int padding = 50;
            // Best effort static dimensions since panel might not be rendered yet
            int maxX = getWidth() > 0 ? getWidth() - padding * 2 : 800 - padding * 2;
            int maxY = getHeight() > 0 ? getHeight() - padding * 2 : 600 - padding * 2;

            for (NoteDto note : noteList) {
                int x = padding + random.nextInt(Math.max(1, maxX));
                int y = padding + random.nextInt(Math.max(1, maxY));
                graphNodes.add(new GraphNode(note, x, y));
            }

            // Fetch automatic wikilink connections from backend
            try {
                java.util.List<com.notegraph.frontend.model.ConnectionDto> connections = com.notegraph.frontend.api.ApiClient
                        .fetchAllConnections();
                if (connections != null) {
                    for (com.notegraph.frontend.model.ConnectionDto conn : connections) {
                        GraphNode sourceNode = null;
                        GraphNode targetNode = null;

                        for (GraphNode node : graphNodes) {
                            if (node.note.getId().equals(conn.getSourceId())) {
                                sourceNode = node;
                            }
                            if (node.note.getId().equals(conn.getTargetId())) {
                                targetNode = node;
                            }
                        }

                        if (sourceNode != null && targetNode != null) {
                            edges.add(new GraphEdge(sourceNode, targetNode));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        repaint();
    }

    public void refreshData() {
        try {
            List<NoteDto> freshNotes = com.notegraph.frontend.api.ApiClient.fetchAllNotes();
            loadGraphData(freshNotes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void performPhysicsStep() {
        if (graphNodes.isEmpty())
            return;

        double repulsionConstant = 2000.0;
        double springConstant = 0.05;
        double optimalLength = 100.0;
        double friction = 0.85;

        // Apply Repulsion
        for (int i = 0; i < graphNodes.size(); i++) {
            GraphNode n1 = graphNodes.get(i);
            for (int j = i + 1; j < graphNodes.size(); j++) {
                GraphNode n2 = graphNodes.get(j);
                double dX = n1.x - n2.x;
                double dY = n1.y - n2.y;
                double distanceSq = dX * dX + dY * dY;
                if (distanceSq < 1)
                    distanceSq = 1; // prevent div by zero
                double distance = Math.sqrt(distanceSq);

                double force = repulsionConstant / distanceSq;
                double fx = force * (dX / distance);
                double fy = force * (dY / distance);

                if (n1 != draggedNode) {
                    n1.dx += fx;
                    n1.dy += fy;
                }
                if (n2 != draggedNode) {
                    n2.dx -= fx;
                    n2.dy -= fy;
                }
            }
        }

        // Apply Spring Attraction
        for (GraphEdge edge : edges) {
            GraphNode n1 = edge.source;
            GraphNode n2 = edge.target;
            double dX = n1.x - n2.x;
            double dY = n1.y - n2.y;
            double distance = Math.sqrt(dX * dX + dY * dY);
            if (distance < 1)
                distance = 1;

            double force = springConstant * (distance - optimalLength);
            double fx = force * (dX / distance);
            double fy = force * (dY / distance);

            if (n1 != draggedNode) {
                n1.dx -= fx;
                n1.dy -= fy;
            }
            if (n2 != draggedNode) {
                n2.dx += fx;
                n2.dy += fy;
            }
        }

        // Update positions & friction
        boolean moved = false;
        int width = getWidth();
        int height = getHeight();
        if (width == 0)
            width = 800; // default bounds if not visible
        if (height == 0)
            height = 600;

        for (GraphNode node : graphNodes) {
            if (node == draggedNode)
                continue;

            node.dx *= friction;
            node.dy *= friction;

            node.x += node.dx;
            node.y += node.dy;

            // Bounds checking
            if (node.x < 20) {
                node.x = 20;
                node.dx *= -0.5;
            }
            if (node.x > width - 20) {
                node.x = width - 20;
                node.dx *= -0.5;
            }
            if (node.y < 20) {
                node.y = 20;
                node.dy *= -0.5;
            }
            if (node.y > height - 20) {
                node.y = height - 20;
                node.dy *= -0.5;
            }

            if (Math.abs(node.dx) > 0.1 || Math.abs(node.dy) > 0.1)
                moved = true;
        }

        if (moved)
            repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Turn on antialiasing for smooth circles and text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Identify active nodes
        Set<GraphNode> activeNodes = new HashSet<>();
        if (hoveredNode != null) {
            activeNodes.add(hoveredNode);
            for (GraphEdge edge : edges) {
                if (edge.source == hoveredNode)
                    activeNodes.add(edge.target);
                if (edge.target == hoveredNode)
                    activeNodes.add(edge.source);
            }
        }

        // Draw Saved Edges
        for (GraphEdge edge : edges) {
            if (hoveredNode == null) {
                g2d.setColor(new Color(200, 200, 200));
            } else if (edge.source == hoveredNode || edge.target == hoveredNode) {
                g2d.setColor(new Color(255, 122, 89)); // Orange Accent
            } else {
                g2d.setColor(new Color(230, 230, 230));
            }
            g2d.drawLine((int) edge.source.x, (int) edge.source.y, (int) edge.target.x, (int) edge.target.y);
        }

        for (GraphNode node : graphNodes) {
            if (hoveredNode == null) {
                g2d.setColor(new Color(59, 130, 246));
                g2d.fillOval((int) node.x - node.RADIUS, (int) node.y - node.RADIUS, node.RADIUS * 2, node.RADIUS * 2);

                // Only draw title if mouse is near or on the exact node bounds
                if (mouseX != -1 && mouseY != -1) {
                    double dist = Math.hypot(node.x - mouseX, node.y - mouseY);
                    if (dist <= node.RADIUS) {
                        g2d.setColor(UIManager.getColor("Label.foreground"));
                        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        String title = (node.note.getTitle() != null && !node.note.getTitle().isEmpty())
                                ? node.note.getTitle()
                                : "Untitled";
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(title);
                        g2d.drawString(title, (int) node.x - (textWidth / 2), (int) node.y + 20);
                    }
                }
            } else if (activeNodes.contains(node)) {
                g2d.setColor(new Color(255, 122, 89)); // Keep active nodes orange
                g2d.fillOval((int) node.x - node.RADIUS, (int) node.y - node.RADIUS, node.RADIUS * 2, node.RADIUS * 2);

                g2d.setColor(UIManager.getColor("Label.foreground"));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                String title = (node.note.getTitle() != null && !node.note.getTitle().isEmpty()) ? node.note.getTitle()
                        : "Untitled";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(title);
                g2d.drawString(title, (int) node.x - (textWidth / 2), (int) node.y + 20);
            } else {
                g2d.setColor(new Color(200, 200, 200)); // Faded
                g2d.fillOval((int) node.x - node.RADIUS, (int) node.y - node.RADIUS, node.RADIUS * 2, node.RADIUS * 2);
            }
        }
    }

    private class GraphNode {
        public Long id;
        public NoteDto note;
        public double x;
        public double y;
        public double dx;
        public double dy;
        public final int RADIUS = 6;

        public GraphNode(NoteDto note, int x, int y) {
            this.id = note.getId();
            this.note = note;
            this.x = x;
            this.y = y;
        }

        public boolean contains(int px, int py) {
            // Distance formula
            double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
            return distance <= RADIUS;
        }

        public boolean isHovered(int px, int py) {
            double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
            return distance <= 15;
        }
    }

    private class GraphEdge {
        public GraphNode source;
        public GraphNode target;

        public GraphEdge(GraphNode source, GraphNode target) {
            this.source = source;
            this.target = target;
        }
    }
}
