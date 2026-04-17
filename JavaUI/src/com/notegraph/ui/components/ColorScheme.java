package com.notegraph.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Central color and font constants matching the React CSS variables.
 */
public final class ColorScheme {
    // Brand
    public static final Color PRIMARY_BLUE       = new Color(10, 102, 240);
    public static final Color PRIMARY_BLUE_HOVER  = new Color(8, 90, 212);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(17, 24, 39);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    public static final Color TEXT_TERTIARY  = new Color(156, 163, 175);

    // Backgrounds & Borders
    public static final Color BG_APP     = new Color(249, 250, 251);
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color BORDER     = new Color(229, 231, 235);

    // Status colors
    public static final Color ERROR_TEXT   = new Color(239, 68, 68);
    public static final Color ERROR_BG    = new Color(254, 226, 226);
    public static final Color SUCCESS_TEXT = new Color(16, 185, 129);
    public static final Color SUCCESS_BG  = new Color(209, 250, 229);

    // Tag colors
    public static final Color TAG_BLUE_BG   = new Color(239, 246, 255);
    public static final Color TAG_BLUE_TEXT  = new Color(37, 99, 235);
    public static final Color TAG_PURPLE_BG  = new Color(245, 243, 255);
    public static final Color TAG_PURPLE_TEXT = new Color(124, 58, 237);
    public static final Color TAG_GREEN_BG   = SUCCESS_BG;
    public static final Color TAG_GREEN_TEXT  = SUCCESS_TEXT;
    public static final Color TAG_ORANGE_BG  = new Color(255, 247, 237);
    public static final Color TAG_ORANGE_TEXT = new Color(234, 88, 12);
    public static final Color TAG_GRAY_BG    = new Color(243, 244, 246);
    public static final Color TAG_GRAY_TEXT   = new Color(75, 85, 99);
    public static final Color TAG_RED_BG     = ERROR_BG;
    public static final Color TAG_RED_TEXT    = ERROR_TEXT;

    public static final Color STAR_COLOR = new Color(245, 158, 11);

    // Sidebar
    public static final Color SIDEBAR_BG       = BG_SURFACE;
    public static final Color SIDEBAR_HOVER     = new Color(243, 244, 246);
    public static final Color SIDEBAR_ACTIVE_BG = new Color(239, 246, 255);

    // Layout
    public static final int SIDEBAR_WIDTH = 260;
    public static final int TOPBAR_HEIGHT = 56;

    // Fonts
    public static Font FONT_REGULAR;
    public static Font FONT_MEDIUM;
    public static Font FONT_SEMIBOLD;
    public static Font FONT_BOLD;

    static {
        String[] preferred = {"Inter", "Segoe UI", "Roboto", "Arial"};
        String family = "SansSerif";
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        outer:
        for (String p : preferred) {
            for (String a : available) {
                if (a.equalsIgnoreCase(p)) {
                    family = a;
                    break outer;
                }
            }
        }
        FONT_REGULAR  = new Font(family, Font.PLAIN, 15);
        FONT_MEDIUM   = new Font(family, Font.PLAIN, 15);
        FONT_SEMIBOLD = new Font(family, Font.BOLD, 15);
        FONT_BOLD     = new Font(family, Font.BOLD, 18);
    }

    /** Get a tag's background color by name. */
    public static Color tagBg(String color) {
        if (color == null) return TAG_BLUE_BG;
        return switch (color.toLowerCase()) {
            case "purple" -> TAG_PURPLE_BG;
            case "green"  -> TAG_GREEN_BG;
            case "orange" -> TAG_ORANGE_BG;
            case "gray"   -> TAG_GRAY_BG;
            case "red"    -> TAG_RED_BG;
            default       -> TAG_BLUE_BG;
        };
    }

    /** Get a tag's foreground text color by name. */
    public static Color tagFg(String color) {
        if (color == null) return TAG_BLUE_TEXT;
        return switch (color.toLowerCase()) {
            case "purple" -> TAG_PURPLE_TEXT;
            case "green"  -> TAG_GREEN_TEXT;
            case "orange" -> TAG_ORANGE_TEXT;
            case "gray"   -> TAG_GRAY_TEXT;
            case "red"    -> TAG_RED_TEXT;
            default       -> TAG_BLUE_TEXT;
        };
    }

    private ColorScheme() {}
}
