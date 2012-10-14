/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea;

import java.io.File;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.quelea.utils.Utils;

/**
 * A theme for displaying some lyrics on screen. Currently consists of a font and a background.
 * @author Michael
 */
public class Theme {

    public static final Font DEFAULT_FONT = new Font("Arial", 72);
    public static final Color DEFAULT_FONT_COLOR = Color.WHITE;
    public static final Background DEFAULT_BACKGROUND = new Background(Color.BLACK);
    public static final Theme DEFAULT_THEME = new Theme(DEFAULT_FONT, DEFAULT_FONT_COLOR, DEFAULT_BACKGROUND);
    private final Font font;
    private final Paint fontColor;
    private final Background background;
    private String themeName;
    private File file;

    /**
     * Create a new theme with a specified font, font colour and background.
     * @param font       the font to use for the theme.
     * @param fontPaint  the font colour to use for the theme.
     * @param background the background to use for the page.
     */
    public Theme(Font font, Paint fontPaint, Background background) {
        this.font = font;
        this.fontColor = fontPaint;
        this.background = background;
        themeName = "";
    }

    /**
     * Get the file associated with this theme.
     * @return the theme file, or null if one hasn't been set.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file associated with this theme.
     * @param file the file to set as the theme file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the name of the theme.
     * @return the name of the theme.
     */
    public String getThemeName() {
        return themeName;
    }

    /**
     * Set the theme name.
     * @param themeName the theme name.
     */
    public void setThemeName(String themeName) {
        this.themeName = themeName;
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
     * Get the paint of the font.
     * @return the theme font paint.
     */
    public Paint getFontPaint() {
        return fontColor;
    }

    /**
     * Determine if this theme is equal to another object.
     * @param obj the other object.
     * @return true if the two objects are meaningfully equal, false otherwise.
     */
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

    /**
     * Determine a hashcode for this theme.
     * @return the theme's hashcode.
     */
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
//        ret.append("$fontbold:").append(font.isBold());
//        ret.append("$fontitalic:").append(font.isItalic());
        ret.append("$fontcolour:").append(fontColor.toString());
        if (!themeName.isEmpty()) {
            ret.append("$themename:").append(themeName);
        }
        if (background.isColour()) {
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
     * @return the theme parsed from the string, or null if a parsing error occurs.
     */
    public static Theme parseDBString(String s) {
        if (s == null || s.isEmpty()) {
            return Theme.DEFAULT_THEME;
        }
        String fontname = "";
        boolean fontbold = false;
        boolean fontitalic = false;
        String fontcolour = "";
        String backgroundcolour = "";
        String backgroundimage = "";
        String themeName = "";

        for (String part : s.split("\\$")) {
            if (!part.contains(":")) {
                continue;
            }
            String[] parts = part.split(":");
            if (parts[0].equalsIgnoreCase("fontname")) {
                fontname = parts[1];
            }
            else if (parts[0].equalsIgnoreCase("fontbold")) {
                fontbold = Boolean.parseBoolean(parts[1]);
            }
            else if (parts[0].equalsIgnoreCase("fontitalic")) {
                fontitalic = Boolean.parseBoolean(parts[1]);
            }
            else if (parts[0].equalsIgnoreCase("fontcolour")) {
                fontcolour = parts[1];
            }
            else if (parts[0].equalsIgnoreCase("backgroundcolour")) {
                backgroundcolour = parts[1];
            }
            else if (parts[0].equalsIgnoreCase("backgroundimage")) {
                backgroundimage = parts[1];
            }
            else if (parts[0].equalsIgnoreCase("themename")) {
                themeName = parts[1];
            }
        }
//        int fontstyle = 0;
//        if (fontbold) {
//            fontstyle |= Font.BOLD;
//        }
//        if (fontitalic) {
//            fontstyle |= Font.ITALIC;
//        }
        Font font = new Font(fontname, 72);
        Background background;
        if (backgroundcolour.isEmpty()) {
            background = new Background(backgroundimage);
        }
        else {
            background = new Background(Utils.parseColour(backgroundcolour));
        }
        Theme ret = new Theme(font, Utils.parseColour(fontcolour), background);
        ret.themeName = themeName;
        return ret;
    }
}
