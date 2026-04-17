package com.notegraph.ui.components;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class VectorIcon implements Icon {
    public enum Type { TRASH, STAR_BORDER, STAR_FILLED, SPARKLE, AVATAR_USER, AVATAR_AI }

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
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }
}
