package com.notegraph.frontend.canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class CodeBlockElement extends CanvasElement {

    private String code;
    private String language;
    private Font font;

    public CodeBlockElement(int x, int y, String code, String language) {
        // Initialize super with a hardcoded dark background color
        super(x, y, 300, 150, new Color(40, 40, 40));
        this.code = code;
        this.language = language;
        this.font = new Font("Monospaced", Font.PLAIN, 14);
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Draw background
        g2d.setColor(this.color);
        g2d.fillRect(this.x, this.y, this.width, this.height);

        // Draw border
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(this.x, this.y, this.width, this.height);

        // Draw language header
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(Color.YELLOW);
        g2d.drawString(language != null ? language.toUpperCase() : "CODE", this.x + 5, this.y + 15);

        // Draw code content
        g2d.setFont(font);
        g2d.setColor(Color.WHITE); // or subtle green

        if (code != null) {
            String[] lines = code.split("\n");
            int lineY = this.y + 35;
            for (String line : lines) {
                g2d.drawString(line, this.x + 10, lineY);
                lineY += g2d.getFontMetrics().getHeight();
            }
        }
    }
}
