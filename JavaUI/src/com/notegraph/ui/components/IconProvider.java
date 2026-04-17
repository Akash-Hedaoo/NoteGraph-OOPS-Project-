package com.notegraph.ui.components;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class IconProvider {
    private static Font iconFont;

    // Material Icon Unicodes
    public static final String TRASH = "\ue872";
    public static final String STAR_BORDER = "\ue83a";
    public static final String STAR_FILLED = "\ue838";
    public static final String AUTO_AWESOME = "\ue65f";
    public static final String PERSON = "\ue7fd";
    public static final String ROBOT = "\ue8ae";
    public static final String SEND = "\ue163";

    static {
        try {
            File fontFile = new File("src/resources/fonts/MaterialIcons-Regular.ttf");
            if (fontFile.exists()) {
                iconFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(iconFont);
            } else {
                System.err.println("Material Icons font not found at " + fontFile.getAbsolutePath());
                iconFont = new Font("SansSerif", Font.PLAIN, 18); // Fallback
            }
        } catch (Exception e) {
            e.printStackTrace();
            iconFont = new Font("SansSerif", Font.PLAIN, 18); // Fallback
        }
    }

    public static Font getFont(float size) {
        return iconFont.deriveFont(size);
    }

    public static JLabel getIconLabel(String unicode, float size, Color color) {
        JLabel lbl = new JLabel(unicode);
        lbl.setFont(getFont(size));
        lbl.setForeground(color);
        return lbl;
    }
}
