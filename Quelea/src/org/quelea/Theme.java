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
    public static final Color DEFAULT_FONT_COLOR = Color.WHITE;
    public static final Background DEFAULT_BACKGROUND = new Background(Color.BLACK);
    public static final Theme DEFAULT_THEME = new Theme(DEFAULT_FONT, DEFAULT_FONT_COLOR, DEFAULT_BACKGROUND);

    private Font font;
    private Color fontColor;
    private Background background;

    /**
     * Create a new theme with a specified font, font colour and background.
     * @param font the font to use for the theme.
     * @param fontColor the font colour to use for the theme.
     * @param background the background to use for the page.
     */
    public Theme(Font font, Color fontColor, Background background) {
        this.font = font;
        this.fontColor = fontColor;
        this.background = background;
    }

    /**
     * Get the background of the theme.
     * @return the theme background.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Get the font of the theme.
     * @return the theme font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get the colour of the font.
     * @return the theme font colour.
     */
    public Color getFontColor() {
        return fontColor;
    }

}
