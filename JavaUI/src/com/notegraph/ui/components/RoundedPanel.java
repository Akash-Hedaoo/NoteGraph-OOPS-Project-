package com.notegraph.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel with rounded corners and an optional border/shadow, matching the React ".card" class.
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color borderColor;
    private boolean drawShadow;

    public RoundedPanel(int radius) {
        this(radius, ColorScheme.BORDER, false);
    }

    public RoundedPanel(int radius, Color borderColor, boolean drawShadow) {
        super();
        this.cornerRadius = radius;
        this.borderColor = borderColor;
        this.drawShadow = drawShadow;
        setOpaque(false);
        setBackground(ColorScheme.BG_SURFACE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius);
        }

        // Background fill
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Border
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
