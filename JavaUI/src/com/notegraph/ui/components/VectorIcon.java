package com.notegraph.ui.components;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class VectorIcon implements Icon {
    public enum Type { 
        TRASH, STAR_BORDER, STAR_FILLED, SPARKLE, AVATAR_USER, AVATAR_AI,
        FORMAT_BOLD, FORMAT_ITALIC, FORMAT_UNDERLINE, HEADING_1, HEADING_2, LIST_BULLET, LIST_NUMBER,
        NAV_DASHBOARD, NAV_NOTE_GRAPH, NAV_KNOWLEDGE_GRAPH, NAV_TAGS, NAV_FAVORITES, NAV_GUIDE, NAV_REVISION
    }

    private Type type;
    private int size;
    private Color color;

    public VectorIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);

        float s = size;

        switch (type) {
            case TRASH:
                g2.setStroke(new BasicStroke(Math.max(1, s / 10f)));
                g2.draw(new RoundRectangle2D.Float(s*0.2f, s*0.3f, s*0.6f, s*0.6f, s*0.1f, s*0.1f));
                g2.drawLine((int)(s*0.15f), (int)(s*0.2f), (int)(s*0.85f), (int)(s*0.2f));
                g2.drawRect((int)(s*0.4f), (int)(s*0.05f), (int)(s*0.2f), (int)(s*0.15f));
                break;
            case STAR_BORDER:
            case STAR_FILLED:
                Path2D star = new Path2D.Float();
                double angleOffset = Math.PI / 2.0;
                for (int i = 0; i < 10; i++) {
                    double r = (i % 2 == 0) ? s * 0.45 : s * 0.2;
                    double a = i * (Math.PI / 5) - angleOffset;
                    double px = s/2 + r * Math.cos(a);
                    double py = s/2 + r * Math.sin(a);
                    if (i == 0) star.moveTo(px, py);
                    else star.lineTo(px, py);
                }
                star.closePath();
                if (type == Type.STAR_FILLED) {
                    g2.fill(star);
                } else {
                    g2.setStroke(new BasicStroke(Math.max(1f, s/10f)));
                    g2.draw(star);
                }
                break;
            case SPARKLE:
                Path2D spark = new Path2D.Float();
                spark.moveTo(s/2, s*0.1);
                spark.curveTo(s/2, s*0.4, s*0.4, s/2, s*0.1, s/2);
                spark.curveTo(s*0.4, s/2, s/2, s*0.6, s/2, s*0.9);
                spark.curveTo(s/2, s*0.6, s*0.6, s/2, s*0.9, s/2);
                spark.curveTo(s*0.6, s/2, s/2, s*0.4, s/2, s*0.1);
                spark.closePath();
                g2.fill(spark);
                break;
            case AVATAR_USER:
                g2.fillOval((int)(s*0.25f), (int)(s*0.1f), (int)(s*0.5f), (int)(s*0.5f));
                g2.fillArc((int)(s*0.1f), (int)(s*0.65f), (int)(s*0.8f), (int)(s*0.8f), 0, 180);
                break;
            case AVATAR_AI:
                g2.fillRoundRect((int)(s*0.2f), (int)(s*0.3f), (int)(s*0.6f), (int)(s*0.5f), (int)(s*0.2f), (int)(s*0.2f));
                g2.fillOval((int)(s*0.3f), (int)(s*0.45f), (int)(s*0.15f), (int)(s*0.15f));
                g2.fillOval((int)(s*0.55f), (int)(s*0.45f), (int)(s*0.15f), (int)(s*0.15f));
                g2.fillRect((int)(s*0.45f), (int)(s*0.1f), (int)(s*0.1f), (int)(s*0.2f));
                g2.fillOval((int)(s*0.4f), (int)(s*0.05f), (int)(s*0.2f), (int)(s*0.2f));
                break;
            case FORMAT_BOLD:
                g2.setFont(new Font("Serif", Font.BOLD, (int)(s*0.7)));
                FontMetrics fmB = g2.getFontMetrics();
                g2.drawString("B", (int)((s - fmB.stringWidth("B"))/2.0), (int)(s*0.75));
                break;
            case FORMAT_ITALIC:
                g2.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, (int)(s*0.7)));
                FontMetrics fmI = g2.getFontMetrics();
                g2.drawString("I", (int)((s - fmI.stringWidth("I"))/2.0), (int)(s*0.75));
                break;
            case FORMAT_UNDERLINE:
                g2.setFont(new Font("Serif", Font.BOLD, (int)(s*0.7)));
                FontMetrics fmU = g2.getFontMetrics();
                g2.drawString("U", (int)((s - fmU.stringWidth("U"))/2.0), (int)(s*0.75));
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f)));
                g2.drawLine((int)(s*0.25), (int)(s*0.9), (int)(s*0.75), (int)(s*0.9));
                break;
            case HEADING_1:
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(s*0.5)));
                FontMetrics fmH1 = g2.getFontMetrics();
                g2.drawString("H1", (int)((s - fmH1.stringWidth("H1"))/2.0), (int)(s*0.65));
                break;
            case HEADING_2:
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(s*0.5)));
                FontMetrics fmH2 = g2.getFontMetrics();
                g2.drawString("H2", (int)((s - fmH2.stringWidth("H2"))/2.0), (int)(s*0.65));
                break;
            case LIST_BULLET:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for(int i=0; i<3; i++) {
                    int yPos = (int)(s*0.3 + i*s*0.25);
                    g2.fillOval((int)(s*0.15), yPos - (int)(s*0.05), (int)(s*0.1), (int)(s*0.1));
                    g2.drawLine((int)(s*0.4), yPos, (int)(s*0.85), yPos);
                }
                break;
            case LIST_NUMBER:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(s*0.3)));
                for(int i=0; i<3; i++) {
                    int yPos = (int)(s*0.3 + i*s*0.25);
                    g2.drawString(String.valueOf(i+1), (int)(s*0.05), yPos + (int)(s*0.1));
                    g2.drawLine((int)(s*0.4), yPos, (int)(s*0.85), yPos);
                }
                break;
            case NAV_DASHBOARD:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f)));
                g2.drawRoundRect((int)(s*0.15), (int)(s*0.15), (int)(s*0.3), (int)(s*0.3), 2, 2);
                g2.drawRoundRect((int)(s*0.55), (int)(s*0.15), (int)(s*0.3), (int)(s*0.3), 2, 2);
                g2.drawRoundRect((int)(s*0.15), (int)(s*0.55), (int)(s*0.3), (int)(s*0.3), 2, 2);
                g2.drawRoundRect((int)(s*0.55), (int)(s*0.55), (int)(s*0.3), (int)(s*0.3), 2, 2);
                break;
            case NAV_NOTE_GRAPH:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D folder = new Path2D.Float();
                folder.moveTo(s*0.1, s*0.25);
                folder.lineTo(s*0.35, s*0.25);
                folder.lineTo(s*0.45, s*0.4);
                folder.lineTo(s*0.9, s*0.4);
                folder.lineTo(s*0.9, s*0.8);
                folder.lineTo(s*0.1, s*0.8);
                folder.closePath();
                g2.draw(folder);
                break;
            case NAV_KNOWLEDGE_GRAPH:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f)));
                g2.drawOval((int)(s*0.4), (int)(s*0.1), (int)(s*0.2), (int)(s*0.2));
                g2.drawOval((int)(s*0.15), (int)(s*0.65), (int)(s*0.2), (int)(s*0.2));
                g2.drawOval((int)(s*0.65), (int)(s*0.65), (int)(s*0.2), (int)(s*0.2));
                g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.25), (int)(s*0.65));
                g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.75), (int)(s*0.65));
                break;
            case NAV_TAGS:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D tag = new Path2D.Float();
                tag.moveTo(s*0.15, s*0.5);
                tag.lineTo(s*0.5, s*0.15);
                tag.lineTo(s*0.85, s*0.15);
                tag.lineTo(s*0.85, s*0.5);
                tag.lineTo(s*0.5, s*0.85);
                tag.closePath();
                g2.draw(tag);
                g2.drawOval((int)(s*0.65), (int)(s*0.3), (int)(s*0.05), (int)(s*0.05));
                break;
            case NAV_FAVORITES:
                Path2D sStar = new Path2D.Float();
                double sAngleOffset = Math.PI / 2.0;
                for (int i = 0; i < 10; i++) {
                    double r = (i % 2 == 0) ? s * 0.45 : s * 0.2;
                    double a = i * (Math.PI / 5) - sAngleOffset;
                    double px = s/2 + r * Math.cos(a);
                    double py = s/2 + r * Math.sin(a);
                    if (i == 0) sStar.moveTo(px, py);
                    else sStar.lineTo(px, py);
                }
                sStar.closePath();
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f)));
                g2.draw(sStar);
                break;
            case NAV_GUIDE:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRect((int)(s*0.1), (int)(s*0.2), (int)(s*0.4), (int)(s*0.6));
                g2.drawRect((int)(s*0.5), (int)(s*0.2), (int)(s*0.4), (int)(s*0.6));
                break;
            case NAV_REVISION:
                // Checklist with clock icon
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Clipboard outline
                g2.drawRoundRect((int)(s*0.15), (int)(s*0.2), (int)(s*0.7), (int)(s*0.7), 3, 3);
                // Clipboard top tab
                g2.fillRoundRect((int)(s*0.35), (int)(s*0.1), (int)(s*0.3), (int)(s*0.15), 2, 2);
                // Checkmark lines
                g2.drawLine((int)(s*0.25), (int)(s*0.45), (int)(s*0.35), (int)(s*0.55));
                g2.drawLine((int)(s*0.35), (int)(s*0.55), (int)(s*0.5), (int)(s*0.4));
                // List line
                g2.drawLine((int)(s*0.55), (int)(s*0.48), (int)(s*0.75), (int)(s*0.48));
                // Second row dot + line
                g2.fillOval((int)(s*0.25), (int)(s*0.65), (int)(s*0.08), (int)(s*0.08));
                g2.drawLine((int)(s*0.38), (int)(s*0.69), (int)(s*0.75), (int)(s*0.69));
                break;
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }
}
