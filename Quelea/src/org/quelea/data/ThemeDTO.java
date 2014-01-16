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
package org.quelea.data;

import java.io.File;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.quelea.data.db.model.TextShadow;
import org.quelea.data.db.model.Theme;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.Utils;

/**
 * A theme data transfer object used to deliver theme data from DB to view layer
 * <p/>
 * @author Michael
 */
public class ThemeDTO implements Serializable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final SerializableFont DEFAULT_FONT = new SerializableFont(Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, QueleaProperties.get().getMaxFontSize()));
    public static final Color DEFAULT_FONT_COLOR = Color.WHITE;
    public static final SerializableDropShadow DEFAULT_SHADOW = new SerializableDropShadow(DEFAULT_FONT_COLOR, 0, 0);
    public static final ColourBackground DEFAULT_BACKGROUND = new ColourBackground(Color.BLACK);
    public static final ThemeDTO DEFAULT_THEME = new ThemeDTO(DEFAULT_FONT, DEFAULT_FONT_COLOR, DEFAULT_BACKGROUND, DEFAULT_SHADOW, DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("bold"), DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("italic"));
    private final SerializableFont font;
    private final SerializableColor fontColor;
    private final Background background;
    private final SerializableDropShadow textShadow;
    private String themeName;
    private File file;
    private Boolean isFontBold = false;
    private Boolean isFontItalic = false;

    /**
     * Create a new theme with a specified font, font colour and background.
     * <p/>
     * @param font the font to use for the theme.
     * @param fontPaint the font colour to use for the theme.
     * @param background the background to use for the page.
     */
    public ThemeDTO(SerializableFont font, Color fontPaint, Background background,
            SerializableDropShadow shadow, Boolean isFontBold, Boolean isFontItalic) {
        this.font = font;
        this.fontColor = new SerializableColor(fontPaint);
        this.background = background;
        themeName = "";
        this.textShadow = shadow;
        this.isFontBold = isFontBold;
        this.isFontItalic = isFontItalic;
    }

    /**
     * Get the file associated with this theme.
     * <p/>
     * @return the theme file, or null if one hasn't been set.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file associated with this theme.
     * <p/>
     * @param file the file to set as the theme file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the name of the theme.
     * <p/>
     * @return the name of the theme.
     */
    public String getThemeName() {
        return themeName;
    }

    /**
     * Set the theme name.
     * <p/>
     * @param themeName the theme name.
     */
    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    /**
     * Get the background of the theme.
     * <p/>
     * @return the theme background.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Get the font of the theme.
     * <p/>
     * @return the theme font.
     */
    public Font getFont() {
        return font.getFont();
    }

    /**
     * Get the paint of the font.
     * <p/>
     * @return the theme font paint.
     */
    public Color getFontPaint() {
        return fontColor.getColor();
    }

    /**
     * Determine if this theme is equal to another object.
     * <p/>
     * @param obj the other object.
     * @return true if the two objects are meaningfully equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ThemeDTO other = (ThemeDTO) obj;
        if(this.font != other.font && (this.font == null || !this.font.equals(other.font))) {
            return false;
        }
        if(this.fontColor != other.fontColor && (this.fontColor == null || !this.fontColor.equals(other.fontColor))) {
            return false;
        }
        if(this.background != other.background && (this.background == null || !this.background.equals(other.background))) {
            return false;
        }
        return true;
    }

    /**
     * Determine a hashcode for this theme.
     * <p/>
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
     * <p/>
     * @return the string to store in the database.
     */
    public Theme getTheme() {
        String backgroundColor = "";
        String backgroundVideo = "";
        String backgroundImage = "";
        if(background instanceof VideoBackground) {
            backgroundVideo = background.getString();
        }
        else if(background instanceof ImageBackground) {
            backgroundImage = background.getString();
        }
        else if(background instanceof ColourBackground) {
            backgroundColor = background.getString();
        }
        final TextShadow shadow = new TextShadow(textShadow.getColor().toString(),
                textShadow.getOffsetX(), textShadow.getOffsetY());
        final Theme theme = new Theme(themeName, font.getFont().getName(), fontColor.toString(), backgroundColor,
                backgroundVideo, backgroundImage, shadow, isFontBold, isFontItalic);
        return theme;
    }

    /**
     * Get a themeDTO from a Theme which is DB table mapping
     * <p/>
     */
    public static ThemeDTO getDTO(Theme theme) {
        SerializableFont font = new SerializableFont(new Font(theme.getFontname(), QueleaProperties.get().getMaxFontSize()));
        Background background;
        if(!theme.getBackgroundcolour().isEmpty()) {
            background = new ColourBackground(Utils.parseColour(theme.getBackgroundcolour()));
        }
        else if(!theme.getBackgroundimage().isEmpty()) {
            background = new ImageBackground(theme.getBackgroundimage());
        }
        else if(!theme.getBackgroundvid().isEmpty()) {
            background = new VideoBackground(theme.getBackgroundvid());
        }
        else {
            background = new ColourBackground(Color.BLACK);
        }
        TextShadow givenShadow = theme.getTextShadow();
        SerializableDropShadow shadow = new SerializableDropShadow(Utils.parseColour(givenShadow.getShadowColor()),
                givenShadow.getOffsetX(), givenShadow.getOffsetY());
        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(theme.getFontcolour()),
                background, shadow, theme.isFontBold(), theme.isFontItalic());
        ret.themeName = theme.getName();
        return ret;
    }

    public SerializableDropShadow getShadow() {
        return textShadow;
    }

    /**
     * Presents theme as string representation //
     * <p/>
     * @todo move to SimpleXML framework.
     * @return theme as string representation
     */
    public String asString() {
        StringBuilder ret = new StringBuilder();
        ret.append("fontname:").append(font.getFont().getFamily());
        ret.append("$fontcolour:").append(fontColor.toString());
        if(!themeName.isEmpty()) {
            ret.append("$themename:").append(themeName);
        }
        ret.append("$isFontBold:").append(isFontBold);
        ret.append("$isFontItalic:").append(isFontItalic);
        if(background instanceof VideoBackground) {
            ret.append("$backgroundvideo:").append(((VideoBackground) background).getString());
        }
        else if(background instanceof ImageBackground) {
            ret.append("$backgroundimage:").append(((ImageBackground) background).getString());
        }
        else if(background instanceof ColourBackground) {
            ret.append("$backgroundcolour:").append(((ColourBackground) background).getString());
        }
        ret.append("$shadowcolor:").append(textShadow.getColor().toString());
        ret.append("$shadowX:").append(textShadow.getOffsetX());
        ret.append("$shadowY:").append(textShadow.getOffsetY());
        return ret.toString();
    }

    /**
     * Create Theme DTO form its String representation
     * <p/>
     * @param content Theme String representation
     * @return theme DTO
     */
    public static ThemeDTO fromString(String content) {
        if(content == null || content.isEmpty()) {
            return ThemeDTO.DEFAULT_THEME;
        }
        String fontname = "";
        String fontcolour = "";
        String isFontBold = "";
        String isFontItalic = "";
        String backgroundcolour = "";
        String backgroundvid = "";
        String backgroundimage = "";
        String themeName = "";
        String shadowColor = "";
        String shadowOffsetX = "0";
        String shadowOffsetY = "0";
        for(String part : content.split("\\$")) {
            if(!part.contains(":")) {
                continue;
            }
            String[] parts = part.split(":");
            if(parts[0].equalsIgnoreCase("fontname")) {
                fontname = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("fontcolour")) {
                fontcolour = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("isFontBold")) {
                isFontBold = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("isFontItalic")) {
                isFontItalic = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("backgroundcolour")) {
                backgroundcolour = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("backgroundimage")) {
                backgroundimage = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("backgroundvideo")) {
                backgroundvid = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("themename")) {
                themeName = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("shadowcolor")) {
                shadowColor = parts[1];
            }
            else if(parts[0].equalsIgnoreCase("shadowX")) {
                shadowOffsetX = defaultIfEmpty(parts[1], "0");
            }
            else if(parts[0].equalsIgnoreCase("shadowY")) {
                shadowOffsetY = defaultIfEmpty(parts[1], "0");
            }
        }
        Font sysFont = Font.font(fontname,
                Boolean.parseBoolean(isFontBold) ? FontWeight.BOLD : FontWeight.NORMAL,
                Boolean.parseBoolean(isFontItalic) ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        SerializableFont font = new SerializableFont(sysFont);
        Background background;
        if(!backgroundcolour.trim().isEmpty()) {
            background = new ColourBackground(Utils.parseColour(backgroundcolour));
        }
        else if(!backgroundimage.trim().isEmpty()) {
            background = new ImageBackground(backgroundimage);
        }
        else if(!backgroundvid.trim().isEmpty()) {
            background = new VideoBackground(backgroundvid);
        }
        else {
            LOGGER.log(Level.WARNING, "WARNING: Unhandled or empty background, using default background. Raw content: " + content, new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            background = ThemeDTO.DEFAULT_BACKGROUND;
        }
        SerializableDropShadow shadow = new SerializableDropShadow(Utils.parseColour(shadowColor), Double.parseDouble(shadowOffsetX), Double.parseDouble(shadowOffsetY));
        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(fontcolour),
                background, shadow, Boolean.valueOf(isFontBold),
                Boolean.valueOf(isFontItalic));
        ret.themeName = themeName;
        return ret;
    }

    /**
     * Return a set value if the given string is empty.
     * <p/>
     * @param val the string to check.
     * @param defaultVal the value to return if the string is empty.
     * @return val, or defaultVal if the string is empty (or just whitespace.)
     */
    private static String defaultIfEmpty(String val, String defaultVal) {
        if(val == null) {
            return defaultVal;
        }
        if(val.trim().isEmpty()) {
            return defaultVal;
        }
        return val;
    }

    /**
     * @return the italic
     */
    public boolean isItalic() {
        return isFontItalic;
    }

    /**
     * @return the bold
     */
    public boolean isBold() {
        return isFontBold;
    }
}
