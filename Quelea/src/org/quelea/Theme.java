package org.quelea;

import org.quelea.utils.Utils;
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

    private final Font font;
    private final Color fontColor;
    private final Background background;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Theme other = (Theme) obj;
        if (this.font != other.font && (this.font == null || !this.font.equals(other.font))) {
            return false;
        }
        if (this.fontColor != other.fontColor && (this.fontColor == null || !this.fontColor.equals(other.fontColor))) {
            return false;
        }
        if (this.background != other.background && (this.background == null || !this.background.equals(other.background))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.font != null ? this.font.hashCode() : 0);
        hash = 67 * hash + (this.fontColor != null ? this.fontColor.hashCode() : 0);
        hash = 67 * hash + (this.background != null ? this.background.hashCode() : 0);
        return hash;
    }

    /**
     * Get a string representation of this theme for storing in the database.
     * @return the string to store in the database.
     */
    public String toDBString() {
        StringBuilder ret = new StringBuilder();
        ret.append("fontname:").append(font.getName());
        ret.append("$fontbold:").append(font.isBold());
        ret.append("$fontitalic:").append(font.isItalic());
        ret.append("$fontcolour:").append(fontColor.toString());
        if(background.isColour()) {
            ret.append("$backgroundcolour:").append(background.getColour());
        }
        else {
            ret.append("$backgroundimage:").append(background.getImageLocation());
        }
        return ret.toString();
    }

    /**
     * Get a theme from a string.
     * @param s the string to parse.
     * @return the theme parsed from the string, or null if a parsing error
     * occurs.
     */
    public static Theme parseDBString(String s) {
        if(s==null || s.isEmpty()) {
            return Theme.DEFAULT_THEME;
        }
        String fontname = "";
        boolean fontbold = false;
        boolean fontitalic = false;
        String fontcolour = "";
        String backgroundcolour = "";
        String backgroundimage = "";

        for(String part : s.split("\\$")) {
            if(!part.contains(":")) {
                continue;
            }
            String[] parts = part.split(":");
            if(parts[0].equalsIgnoreCase("fontname")) {
                fontname = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("fontbold")) {
                fontbold = Boolean.parseBoolean(parts[1]);
            }
            else if(parts[0].equalsIgnoreCase("fontitalic")) {
                fontitalic = Boolean.parseBoolean(parts[1]);
            }
            else if(parts[0].equalsIgnoreCase("fontcolour")) {
                fontcolour = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("backgroundcolour")) {
                backgroundcolour = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("backgroundimage")) {
                backgroundimage = parts[1];
            }
        }
        int fontstyle = 0;
        if(fontbold) {
            fontstyle |= Font.BOLD;
        }
        if(fontitalic) {
            fontstyle |= Font.ITALIC;
        }
        Font font = new Font(fontname, fontstyle, 72);
        Background background;
        if(backgroundcolour.isEmpty()) {
            background = new Background(backgroundimage);
        }
        else {
            background = new Background(Utils.parseColour(backgroundcolour));
        }
        return new Theme(font, Utils.parseColour(fontcolour), background);
    }

}
