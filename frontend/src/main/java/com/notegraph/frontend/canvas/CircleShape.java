package com.notegraph.frontend.canvas;

import java.awt.Color;
import java.awt.Graphics2D;

public class CircleShape extends CanvasElement {

    public CircleShape(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Set the color and draw the filled oval (circle)
        g2d.setColor(this.color);
        g2d.fillOval(this.x, this.y, this.width, this.height);
    }
}
