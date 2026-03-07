package com.notegraph.frontend.canvas;

import java.awt.Color;
import java.awt.Graphics2D;

public class RectangleShape extends CanvasElement {

    public RectangleShape(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Set the color and draw the filled rectangle
        g2d.setColor(this.color);
        g2d.fillRect(this.x, this.y, this.width, this.height);
    }
}
