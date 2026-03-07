package com.notegraph.frontend.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

public class FreehandShape extends CanvasElement {

    private Path2D.Float path;

    public FreehandShape(int startX, int startY, Color color) {
        super(startX, startY, 0, 0, color);
        path = new Path2D.Float();
        path.moveTo(startX, startY);
    }

    public void addPoint(int x, int y) {
        path.lineTo(x, y);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(path);
    }
}
