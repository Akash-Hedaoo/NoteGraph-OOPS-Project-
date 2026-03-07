package com.notegraph.frontend.canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class TextElement extends CanvasElement {

    private String text;
    private Font font;

    public TextElement(int x, int y, String text, Font font, Color color) {
        super(x, y, 0, 0, color);
        this.text = text;
        this.font = font;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (text != null && !text.isEmpty()) {
            g2d.setFont(font);
            g2d.setColor(this.color);
            g2d.drawString(text, this.x, this.y);
        }
    }
}
