package com.notegraph.frontend.canvas;

import java.awt.Color;
import java.awt.Graphics2D;

// Demonstrates Abstraction: Template for all drawable canvas items
public abstract class CanvasElement {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Color color;

    public CanvasElement(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Abstract method that subclasses MUST implement
    public abstract void draw(Graphics2D g2d);
}
