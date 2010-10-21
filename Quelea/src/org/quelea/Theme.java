package org.quelea;

import java.awt.Color;
import java.awt.Font;

/**
 * A theme for displaying some lyrics on screen. Currently consists of a font
 * and a background.
 * @author Michael
 */
public class Theme {

    public static final Font DEFAULT_FONT = new Font("Sans serif", Font.BOLD, 72);
    public static final Color DEFAULT_FONT_COLOR = Color.BLACK;
    public static final Background DEFAULT_BACKGROUND = new Background(Color.BLACK);
    public static final Theme DEFAULT_THEME = new Theme(DEFAULT_FONT, DEFAULT_FONT_COLOR, DEFAULT_BACKGROUND);

    private Font font;
    private Color fontColor;
    private Background background;

    public Theme(Font font, Color fontColor, Background background) {
        this.font = font;
        this.background = background;
    }

    public Background getBackground() {
        return background;
    }

    public Font getFont() {
        return font;
    }

    public Color getFontColor() {
        return fontColor;
    }

}
