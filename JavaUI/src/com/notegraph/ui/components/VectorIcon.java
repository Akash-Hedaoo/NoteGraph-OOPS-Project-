package com.notegraph.ui.components;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

public class VectorIcon implements Icon {
    public enum Type { 
        TRASH, STAR_BORDER, STAR_FILLED, SPARKLE, AVATAR_USER, AVATAR_AI,
        FORMAT_BOLD, FORMAT_ITALIC, FORMAT_UNDERLINE, HEADING_1, HEADING_2, LIST_BULLET, LIST_NUMBER,
        NAV_DASHBOARD, NAV_NOTE_GRAPH, NAV_KNOWLEDGE_GRAPH, NAV_TAGS, NAV_FAVORITES, NAV_GUIDE, NAV_REVISION,
        SEARCH, BELL, EDIT, PLUS, FILTER, CLOSE, DOT, CHEVRON_UP, CHEVRON_DOWN, CHEVRON_RIGHT, CHECK, CROSS, FOLDER, CHART, TAG, BOOK, CLOCK, MAIL, PLAY, REFRESH, LINK
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
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float dw = s*0.28f;
                float doff = s*0.16f;
                float dspace = s*0.12f;
                g2.draw(new RoundRectangle2D.Float(doff, doff, dw, dw, s*0.1f, s*0.1f));
                g2.draw(new RoundRectangle2D.Float(doff + dw + dspace, doff, dw, dw, s*0.1f, s*0.1f));
                g2.draw(new RoundRectangle2D.Float(doff, doff + dw + dspace, dw, dw, s*0.1f, s*0.1f));
                g2.draw(new RoundRectangle2D.Float(doff + dw + dspace, doff + dw + dspace, dw, dw, s*0.1f, s*0.1f));
                break;
            case NAV_NOTE_GRAPH:
            case FOLDER:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D folder = new Path2D.Float();
                folder.moveTo(s*0.15f, s*0.3f);
                folder.lineTo(s*0.35f, s*0.3f);
                folder.lineTo(s*0.45f, s*0.4f);
                folder.lineTo(s*0.85f, s*0.4f);
                folder.lineTo(s*0.85f, s*0.75f);
                folder.lineTo(s*0.15f, s*0.75f);
                folder.closePath();
                g2.draw(folder);
                break;
            case NAV_KNOWLEDGE_GRAPH:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float radius = s*0.16f;
                float cx1 = s*0.5f, cy1 = s*0.25f;
                float cx2 = s*0.25f, cy2 = s*0.7f;
                float cx3 = s*0.75f, cy3 = s*0.7f;
                g2.drawLine((int)cx1, (int)cy1, (int)cx2, (int)cy2);
                g2.drawLine((int)cx1, (int)cy1, (int)cx3, (int)cy3);
                
                g2.setColor(c.getBackground() != null ? c.getBackground() : Color.WHITE);
                g2.fill(new Ellipse2D.Float(cx1-radius, cy1-radius, 2*radius, 2*radius));
                g2.fill(new Ellipse2D.Float(cx2-radius, cy2-radius, 2*radius, 2*radius));
                g2.fill(new Ellipse2D.Float(cx3-radius, cy3-radius, 2*radius, 2*radius));
                
                g2.setColor(color);
                g2.draw(new Ellipse2D.Float(cx1-radius, cy1-radius, 2*radius, 2*radius));
                g2.draw(new Ellipse2D.Float(cx2-radius, cy2-radius, 2*radius, 2*radius));
                g2.draw(new Ellipse2D.Float(cx3-radius, cy3-radius, 2*radius, 2*radius));
                break;
            case NAV_TAGS:
            case TAG:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D tag = new Path2D.Float();
                tag.moveTo(s*0.2f, s*0.5f);
                tag.lineTo(s*0.5f, s*0.2f);
                tag.lineTo(s*0.8f, s*0.2f);
                tag.lineTo(s*0.8f, s*0.5f);
                tag.lineTo(s*0.5f, s*0.8f);
                tag.closePath();
                g2.draw(tag);
                g2.draw(new Ellipse2D.Float(s*0.6f, s*0.35f, s*0.08f, s*0.08f));
                break;
            case NAV_FAVORITES:
                Path2D sStar = new Path2D.Float();
                double sAngleOffset = Math.PI / 2.0;
                for (int i = 0; i < 10; i++) {
                    double starR = (i % 2 == 0) ? s * 0.45 : s * 0.2;
                    double a = i * (Math.PI / 5) - sAngleOffset;
                    double px = s/2 + starR * Math.cos(a);
                    double py = s/2 + starR * Math.sin(a);
                    if (i == 0) sStar.moveTo(px, py);
                    else sStar.lineTo(px, py);
                }
                sStar.closePath();
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f)));
                g2.draw(sStar);
                break;
            case NAV_GUIDE:
            case BOOK:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new RoundRectangle2D.Float(s*0.15f, s*0.2f, s*0.35f, s*0.6f, s*0.08f, s*0.08f));
                g2.draw(new RoundRectangle2D.Float(s*0.5f, s*0.2f, s*0.35f, s*0.6f, s*0.08f, s*0.08f));
                g2.drawLine((int)(s*0.5f), (int)(s*0.2f), (int)(s*0.5f), (int)(s*0.8f));
                break;
            case NAV_REVISION:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new RoundRectangle2D.Float(s*0.2f, s*0.2f, s*0.6f, s*0.6f, s*0.15f, s*0.15f));
                g2.drawLine((int)(s*0.2f), (int)(s*0.4f), (int)(s*0.8f), (int)(s*0.4f));
                g2.drawLine((int)(s*0.35f), (int)(s*0.6f), (int)(s*0.45f), (int)(s*0.7f));
                g2.drawLine((int)(s*0.45f), (int)(s*0.7f), (int)(s*0.65f), (int)(s*0.5f));
                break;
            case SEARCH:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval((int)(s*0.2), (int)(s*0.2), (int)(s*0.4), (int)(s*0.4));
                g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.8), (int)(s*0.8));
                break;
            case BELL:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D bell = new Path2D.Float();
                bell.moveTo(s*0.5, s*0.1);
                bell.curveTo(s*0.25, s*0.1, s*0.2, s*0.4, s*0.2, s*0.6);
                bell.lineTo(s*0.1, s*0.8);
                bell.lineTo(s*0.9, s*0.8);
                bell.lineTo(s*0.8, s*0.6);
                bell.curveTo(s*0.8, s*0.4, s*0.75, s*0.1, s*0.5, s*0.1);
                bell.closePath();
                g2.draw(bell);
                g2.drawArc((int)(s*0.4), (int)(s*0.8), (int)(s*0.2), (int)(s*0.1), 0, -180);
                break;
            case EDIT:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D pencil = new Path2D.Float();
                pencil.moveTo(s*0.8, s*0.2);
                pencil.lineTo(s*0.6, s*0.4);
                pencil.lineTo(s*0.2, s*0.8);
                pencil.lineTo(s*0.1, s*0.9);
                pencil.lineTo(s*0.2, s*0.8); // Tip
                pencil.lineTo(s*0.4, s*0.6);
                pencil.closePath();
                g2.draw(pencil);
                g2.drawLine((int)(s*0.6), (int)(s*0.4), (int)(s*0.4), (int)(s*0.6));
                break;
            case PLUS:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.5), (int)(s*0.2), (int)(s*0.5), (int)(s*0.8));
                g2.drawLine((int)(s*0.2), (int)(s*0.5), (int)(s*0.8), (int)(s*0.5));
                break;
            case FILTER:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.9), (int)(s*0.25));
                g2.drawLine((int)(s*0.3), (int)(s*0.5), (int)(s*0.7), (int)(s*0.5));
                g2.drawLine((int)(s*0.45), (int)(s*0.75), (int)(s*0.55), (int)(s*0.75));
                break;
            case CLOSE:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.3), (int)(s*0.3), (int)(s*0.7), (int)(s*0.7));
                g2.drawLine((int)(s*0.7), (int)(s*0.3), (int)(s*0.3), (int)(s*0.7));
                break;
            case DOT:
                g2.fill(new Ellipse2D.Float(s*0.35f, s*0.35f, s*0.3f, s*0.3f));
                break;
            case CHEVRON_UP:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.2), (int)(s*0.6), (int)(s*0.5), (int)(s*0.3));
                g2.drawLine((int)(s*0.5), (int)(s*0.3), (int)(s*0.8), (int)(s*0.6));
                break;
            case CHEVRON_DOWN:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.2), (int)(s*0.4), (int)(s*0.5), (int)(s*0.7));
                g2.drawLine((int)(s*0.5), (int)(s*0.7), (int)(s*0.8), (int)(s*0.4));
                break;
            case CHEVRON_RIGHT:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.3), (int)(s*0.2), (int)(s*0.7), (int)(s*0.5));
                g2.drawLine((int)(s*0.7), (int)(s*0.5), (int)(s*0.3), (int)(s*0.8));
                break;
            case CHECK:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.2), (int)(s*0.5), (int)(s*0.45), (int)(s*0.75));
                g2.drawLine((int)(s*0.45), (int)(s*0.75), (int)(s*0.8), (int)(s*0.3));
                break;
            case CROSS:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.2), (int)(s*0.2), (int)(s*0.8), (int)(s*0.8));
                g2.drawLine((int)(s*0.8), (int)(s*0.2), (int)(s*0.2), (int)(s*0.8));
                break;
            case CHART:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(s*0.1), (int)(s*0.1), (int)(s*0.1), (int)(s*0.9));
                g2.drawLine((int)(s*0.1), (int)(s*0.9), (int)(s*0.9), (int)(s*0.9));
                g2.fillRect((int)(s*0.25), (int)(s*0.5), (int)(s*0.15), (int)(s*0.35));
                g2.fillRect((int)(s*0.5), (int)(s*0.3), (int)(s*0.15), (int)(s*0.55));
                g2.fillRect((int)(s*0.75), (int)(s*0.15), (int)(s*0.15), (int)(s*0.7));
                break;
            case CLOCK:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval((int)(s*0.15), (int)(s*0.15), (int)(s*0.7), (int)(s*0.7));
                g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.5), (int)(s*0.25));
                g2.drawLine((int)(s*0.5), (int)(s*0.5), (int)(s*0.7), (int)(s*0.6));
                break;
            case MAIL:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRect((int)(s*0.1), (int)(s*0.25), (int)(s*0.8), (int)(s*0.5));
                g2.drawLine((int)(s*0.1), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                g2.drawLine((int)(s*0.9), (int)(s*0.25), (int)(s*0.5), (int)(s*0.5));
                break;
            case PLAY:
                Path2D play = new Path2D.Float();
                play.moveTo(s*0.3, s*0.2);
                play.lineTo(s*0.8, s*0.5);
                play.lineTo(s*0.3, s*0.8);
                play.closePath();
                g2.fill(play);
                break;
            case REFRESH:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc((int)(s*0.2), (int)(s*0.2), (int)(s*0.6), (int)(s*0.6), 45, 270);
                g2.drawLine((int)(s*0.75), (int)(s*0.25), (int)(s*0.85), (int)(s*0.2));
                g2.drawLine((int)(s*0.85), (int)(s*0.2), (int)(s*0.85), (int)(s*0.3));
                break;
            case LINK:
                g2.setStroke(new BasicStroke(Math.max(1.5f, s/15f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc((int)(s*0.2), (int)(s*0.3), (int)(s*0.3), (int)(s*0.4), 90, 180);
                g2.drawArc((int)(s*0.5), (int)(s*0.3), (int)(s*0.3), (int)(s*0.4), -90, 180);
                g2.drawLine((int)(s*0.4), (int)(s*0.5), (int)(s*0.6), (int)(s*0.5));
                break;
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }
}
