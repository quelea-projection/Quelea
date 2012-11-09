/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.quelea.data.db.model.TextShadow;
import org.quelea.data.db.model.Theme;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * A theme data transfer object used to deliver theme data from DB to view layer 
 *
 * @author Michael
 */
public class ThemeDTO {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final Font DEFAULT_FONT = new Font("Arial", 72);
    public static final Color DEFAULT_FONT_COLOR = Color.WHITE;
    public static final DropShadow DEFAULT_SHADOW = new DropShadow();
    public static final Background DEFAULT_BACKGROUND = new ColourBackground(Color.BLACK);
    public static final ThemeDTO DEFAULT_THEME = new ThemeDTO(DEFAULT_FONT, DEFAULT_FONT_COLOR,
            DEFAULT_BACKGROUND, DEFAULT_SHADOW);
    private final Font font;
    private final Paint fontColor;
    private final Background background;
    private final DropShadow textShadow;
    private String themeName;
    private File file;

    /**
     * Create a new theme with a specified font, font colour and background.
     *
     * @param font the font to use for the theme.
     * @param fontPaint the font colour to use for the theme.
     * @param background the background to use for the page.
     */
    public ThemeDTO(Font font, Paint fontPaint, Background background, DropShadow shadow) {
        this.font = font;
        this.fontColor = fontPaint;
        this.background = background;
        themeName = "";
        this.textShadow = shadow;
    }

    /**
     * Get the file associated with this theme.
     *
     * @return the theme file, or null if one hasn't been set.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file associated with this theme.
     *
     * @param file the file to set as the theme file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the name of the theme.
     *
     * @return the name of the theme.
     */
    public String getThemeName() {
        return themeName;
    }

    /**
     * Set the theme name.
     *
     * @param themeName the theme name.
     */
    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    /**
     * Get the background of the theme.
     *
     * @return the theme background.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Get the font of the theme.
     *
     * @return the theme font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get the paint of the font.
     *
     * @return the theme font paint.
     */
    public Paint getFontPaint() {
        return fontColor;
    }

    /**
     * Determine if this theme is equal to another object.
     *
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
        final ThemeDTO other = (ThemeDTO) obj;
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
     *
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
     *
     * @return the string to store in the database.
     */
    public Theme getTheme() {
        String backgroundColor = "";
        String backgroundVideo = "";
        String backgroundImage = "";
        if (background instanceof VideoBackground) {
            backgroundVideo = background.getString();
        } else if (background instanceof ImageBackground) {
            backgroundImage = background.getString();
        } else if (background instanceof ColourBackground) {
            backgroundColor = background.getString();
        }
        final TextShadow shadow = new TextShadow(textShadow.getColor().toString(),
                textShadow.getOffsetX(), textShadow.getOffsetY());
        final Theme theme = new Theme(themeName, font.getName(), fontColor.toString(), backgroundColor,
                backgroundVideo, backgroundImage, shadow);
        return theme;
    }

    /**
     * Get a themeDTO from a Theme which is DB table mapping
     *
     * @param s the string to parse.
     * @return the theme parsed from the string, or null if a parsing error
     * occurs.
     */
    public static ThemeDTO getDTO(Theme theme) {
        Font font = new Font(theme.getFontname(), 72);
        Background background;
        if (!theme.getBackgroundcolour().isEmpty()) {
            background = new ColourBackground(Utils.parseColour(theme.getBackgroundcolour()));
        } else if (!theme.getBackgroundimage().isEmpty()) {
            background = new ImageBackground(theme.getBackgroundimage());
        } else if (!theme.getBackgroundvid().isEmpty()) {
            background = new VideoBackground(theme.getBackgroundvid());
        } else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled background");
            background = null;
        }
        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(theme.getFontcolour()), background, DEFAULT_SHADOW);//@todo tp get shadow from db
        ret.themeName = theme.getName();
        return ret;
    }

    public DropShadow getShadow() {
        return textShadow;
    }
    
    /**
     * Presents theme as string representation //@todo move to SimpleXML framework.
     * @return theme as string representation
     */
    public String asString(){
         StringBuilder ret = new StringBuilder();
        ret.append("fontname:").append(font.getName());
        ret.append("$fontcolour:").append(fontColor.toString());
        if (!themeName.isEmpty()) {
            ret.append("$themename:").append(themeName);
        }
        ret.append(background.getString());
        return ret.toString();
    }
    
    /**
     * Create Theme DTO form its String representation
     * @param content Theme String representation
     * @return theme DTO
     */
    public static ThemeDTO fromString(String content){
         if (content == null || content.isEmpty()) {
            return ThemeDTO.DEFAULT_THEME;
        }
        String fontname = "";
        String fontcolour = "";
        String backgroundcolour = "";
        String backgroundvid = "";
        String backgroundimage = "";
        String themeName = "";

        for (String part : content.split("\\$")) {
            if (!part.contains(":")) {
                continue;
            }
            String[] parts = part.split(":");
            if (parts[0].equalsIgnoreCase("fontname")) {
                fontname = parts[1];
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
            else if (parts[0].equalsIgnoreCase("backgroundvideo")) {
                backgroundvid = parts[1];
            }
            else if (parts[0].equalsIgnoreCase("themename")) {
                themeName = parts[1];
            }
        }
        Font font = new Font(fontname, 72);
        Background background;
        if (!backgroundcolour.isEmpty()) {
            background = new ColourBackground(Utils.parseColour(backgroundcolour));
        }
        else if (!backgroundimage.isEmpty()) {
            background = new ImageBackground(backgroundimage);
        }
        else if (!backgroundvid.isEmpty()) {
            background = new VideoBackground(backgroundvid);
        }
        else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled background");
            background = null;
        }
        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(fontcolour), background, DEFAULT_SHADOW);
        ret.themeName = themeName;
        return ret;
    }
}
