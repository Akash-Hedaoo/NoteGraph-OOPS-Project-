package com.notegraph.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A JTextField that shows placeholder text when empty and unfocused.
 */
public class PlaceholderTextField extends JTextField {

    private String placeholder;
    private boolean isPassword;

    public PlaceholderTextField(String placeholder) {
        this(placeholder, false);
    }

    public PlaceholderTextField(String placeholder, boolean isPassword) {
        this.placeholder = placeholder;
        this.isPassword = isPassword;
        setFont(ColorScheme.FONT_REGULAR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !hasFocus()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(ColorScheme.TEXT_TERTIARY);
            g2.setFont(getFont());
            Insets insets = getInsets();
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, insets.left + 2, y);
            g2.dispose();
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }
}
