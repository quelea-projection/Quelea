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
    public static final Color DEFAULT_TRANSLATE_FONT_COLOR = Color.WHITESMOKE;
    public static final SerializableDropShadow DEFAULT_SHADOW = new SerializableDropShadow(DEFAULT_FONT_COLOR, 0, 0);
    public static final ColourBackground DEFAULT_BACKGROUND = new ColourBackground(Color.BLACK);
    public static final SerializableFont BIBLE_DEFAULT_FONT = new SerializableFont(Font.font("Liberation Sans", FontWeight.BOLD, FontPosture.REGULAR, QueleaProperties.get().getMaxFontSize()));
    public static final Color BIBLE_DEFAULT_FONT_COLOR = Color.WHITE;
    public static final SerializableDropShadow BIBLE_DEFAULT_SHADOW = new SerializableDropShadow(BIBLE_DEFAULT_FONT_COLOR, 0, 0);
    public static final ColourBackground BIBLE_DEFAULT_BACKGROUND = new ColourBackground(Color.BLACK);
    public static final ThemeDTO DEFAULT_THEME = new ThemeDTO(DEFAULT_FONT, DEFAULT_FONT_COLOR, DEFAULT_FONT, DEFAULT_TRANSLATE_FONT_COLOR, DEFAULT_BACKGROUND, DEFAULT_SHADOW, DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("bold"), DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("italic"), DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("bold"), true, -1, 0,
            BIBLE_DEFAULT_FONT, BIBLE_DEFAULT_FONT_COLOR, BIBLE_DEFAULT_BACKGROUND, BIBLE_DEFAULT_SHADOW, BIBLE_DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("bold"), BIBLE_DEFAULT_FONT.getFont().getStyle().toLowerCase().contains("italic"), -1, -1);
    private final SerializableFont font;
    private final SerializableColor fontColor;
    private final SerializableFont translateFont;
    private final SerializableColor translateFontColor;
    private final Background background;
    private final SerializableDropShadow textShadow;
    private String themeName;
    private File file;
    private Boolean isFontBold = false;
    private Boolean isFontItalic = false;
    private Boolean isTranslateFontBold = false;
    private Boolean isTranslateFontItalic = false;
    private int textPosition = -1;
    private int textAlignment = 0;
    //Bible
    private final SerializableFont biblefont;
    private final SerializableColor biblefontcolour;
    private final Background biblebackground;
    private Boolean biblefontBold = false;
    private Boolean biblefontItalic = false;
    private Integer bibletextPosition = -1;
    private Integer bibletextAlignment = -1;
    private final SerializableDropShadow bibletextShadow;

    /**
     * Create a new theme with a specified font, font colour and background.
     * <p/>
     * @param font the font to use for the theme.
     * @param fontPaint the font colour to use for the theme.
     * @param background the background to use for the page.
     */
    public ThemeDTO(SerializableFont font, Color fontPaint, SerializableFont translatefont, Color translatefontPaint, Background background,
            SerializableDropShadow shadow, Boolean isFontBold, Boolean isFontItalic, Boolean isTranslateFontBold, Boolean isTranslateFontItalic,
            Integer textPosition, Integer textAlignment, SerializableFont biblefont, Color biblefontcolour, Background biblebackground, SerializableDropShadow bibletextShadow,
            Boolean biblefontBold, Boolean biblefontItalic, Integer bibletextPosition, Integer bibletextAlignment) {
        this.font = font;
        this.translateFont = translatefont;
        this.fontColor = new SerializableColor(fontPaint);
        this.translateFontColor = new SerializableColor(translatefontPaint);
        this.background = background;
        themeName = "";
        this.textShadow = shadow;
        this.isFontBold = isFontBold;
        this.isFontItalic = isFontItalic;
        this.isTranslateFontBold = isTranslateFontBold;
        this.isTranslateFontItalic = isTranslateFontItalic;
        this.textPosition = textPosition;
        this.textAlignment = textAlignment;
        this.biblefont = biblefont;
        this.biblefontcolour = new SerializableColor(biblefontcolour);
        this.biblebackground = biblebackground;
        this.bibletextShadow = bibletextShadow;
        this.biblefontBold = biblefontBold;
        this.biblefontItalic = biblefontItalic;
        this.bibletextPosition = bibletextPosition;
        this.bibletextAlignment = bibletextAlignment;
    }

    public int getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int textPosition) {
        this.textPosition = textPosition;
    }

    public int getTextAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(int textAlignment) {
        this.textAlignment = textAlignment;
    }
    
    public int getBibleTextPosition() {
        return bibletextPosition;
    }

    public void setBibleTextPosition(int textPosition) {
        this.bibletextPosition = textPosition;
    }

    public int getBibleTextAlignment() {
        return bibletextAlignment;
    }

    public void setBibleTextAlignment(int textAlignment) {
        this.bibletextAlignment = textAlignment;
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
     * Get the background of the theme.
     * <p/>
     * @return the theme background.
     */
    public Background getBibleBackground() {
        return biblebackground;
    }

    /**
     * Get the font of the theme.
     * <p/>
     * @return the theme font.
     */
    public Font getFont() {
        if (font == null) {
            return null;
        }
        return font.getFont();
    }
    
    /**
     * Get the font of the theme.
     * <p/>
     * @return the theme font.
     */
    public Font getBibleFont() {
        if (biblefont == null) {
            return null;
        }
        return biblefont.getFont();
    }

    /**
     * Get the translate font of the theme.
     * <p/>
     * @return the translate theme font.
     */
    public Font getTranslateFont() {
        if (translateFont == null) {
            return null;
        }
        return translateFont.getFont();
    }

    /**
     * Get the font in its raw serializable form.
     * <p>
     * @return the serializable font.
     */
    public SerializableFont getSerializableFont() {
        return font;
    }

    /**
     * Get the translate font in its raw serializable form.
     * <p>
     * @return the translate serializable font.
     */
    public SerializableFont getTranslateSerializableFont() {
        return translateFont;
    }
    
    /**
     * Get the translate font in its raw serializable form.
     * <p>
     * @return the translate serializable font.
     */
    public SerializableFont getBibleSerializableFont() {
        return biblefont;
    }

    /**
     * Get the paint of the font.
     * <p/>
     * @return the theme font paint.
     */
    public Color getFontPaint() {
        if (fontColor == null) {
            return null;
        }
        return fontColor.getColor();
    }
    
    /**
     * Get the paint of the bible font.
     * <p/>
     * @return the bible theme font paint.
     */
    public Color getBibleFontPaint() {
        if (biblefontcolour == null) {
            return null;
        }
        return biblefontcolour.getColor();
    }

    /**
     * Get the paint of the translate font.
     * <p/>
     * @return the theme translate font paint.
     */
    public Color getTranslateFontPaint() {
        if (translateFontColor == null) {
            return null;
        }
        return translateFontColor.getColor();
    }

    /**
     * Determine if this theme is equal to another object.
     * <p/>
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
        if (this.biblefont != other.biblefont && (this.biblefont == null || !this.biblefont.equals(other.biblefont))) {
            return false;
        }
        if (this.biblefontcolour != other.biblefontcolour && (this.biblefontcolour == null || !this.biblefontcolour.equals(other.biblefontcolour))) {
            return false;
        }
        if (this.biblebackground != other.biblebackground && (this.biblebackground == null || !this.biblebackground.equals(other.biblebackground))) {
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
        hash = 67 * hash + (this.biblefont != null ? this.biblefont.hashCode() : 0);
        hash = 67 * hash + (this.biblefontcolour != null ? this.biblefontcolour.hashCode() : 0);
        hash = 67 * hash + (this.biblebackground != null ? this.biblebackground.hashCode() : 0);
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
        String biblebackgroundColor = "";
        String biblebackgroundVideo = "";
        String biblebackgroundImage = "";
        double vidHue = 0;
        double biblevidHue = 0;
        if (background instanceof VideoBackground) {
            backgroundVideo = background.getString();
            vidHue = ((VideoBackground) background).getHue();
        } else if (background instanceof ImageBackground) {
            backgroundImage = background.getString();
        } else if (background instanceof ColourBackground) {
            backgroundColor = background.getString();
        }
        if (biblebackground instanceof VideoBackground) {
            biblebackgroundVideo = biblebackground.getString();
            biblevidHue = ((VideoBackground) biblebackground).getHue();
        } else if (biblebackground instanceof ImageBackground) {
            biblebackgroundImage = biblebackground.getString();
        } else if (biblebackground instanceof ColourBackground) {
            biblebackgroundColor = biblebackground.getString();
        }
        final TextShadow shadow = new TextShadow(textShadow.getColor().toString(),
                textShadow.getOffsetX(), textShadow.getOffsetY());
        final TextShadow bibleshadow = new TextShadow(bibletextShadow.getColor().toString(),
                bibletextShadow.getOffsetX(), bibletextShadow.getOffsetY());
        final Theme theme = new Theme(themeName, font.getFont().getName(), fontColor.toString(), translateFont.getFont().getName(), translateFontColor.toString(), backgroundColor,
                backgroundVideo, backgroundImage, shadow, isFontBold, isFontItalic, isTranslateFontBold, isTranslateFontItalic, vidHue, textPosition, textAlignment,
                biblefont.getFont().getName(), biblefontcolour.toString(), biblebackgroundColor, biblebackgroundVideo, biblebackgroundImage,
                bibleshadow, biblefontBold, biblefontItalic, biblevidHue, bibletextPosition, bibletextAlignment);
        return theme;
    }

    /**
     * Get a themeDTO from a Theme which is DB table mapping
     * <p/>
     */
    public static ThemeDTO getDTO(Theme theme) {
        SerializableFont font = new SerializableFont(new Font(theme.getFontname(), QueleaProperties.get().getMaxFontSize()));
        SerializableFont translateFont = new SerializableFont(new Font(theme.getTranslateFontname(), QueleaProperties.get().getMaxFontSize()));
        Background background;
        if (!theme.getBackgroundcolour().isEmpty()) {
            background = new ColourBackground(Utils.parseColour(theme.getBackgroundcolour()));
        } else if (!theme.getBackgroundimage().isEmpty()) {
            background = new ImageBackground(theme.getBackgroundimage());
        } else if (!theme.getBackgroundvid().isEmpty()) {
            background = new VideoBackground(theme.getBackgroundvid(), theme.getVideoHue());
        } else {
            background = new ColourBackground(Color.BLACK);
        }
        TextShadow givenShadow = theme.getTextShadow();
        SerializableDropShadow shadow = new SerializableDropShadow(Utils.parseColour(givenShadow.getShadowColor()),
                givenShadow.getOffsetX(), givenShadow.getOffsetY());

        //Bible
        SerializableFont biblefont = new SerializableFont(new Font(theme.getBibleFontname(), QueleaProperties.get().getMaxFontSize()));
        Background biblebackground;
        if (theme.getBibleBackgroundcolour() == null) {
            biblebackground = BIBLE_DEFAULT_BACKGROUND;
        } else if (!theme.getBibleBackgroundcolour().isEmpty()) {
            biblebackground = new ColourBackground(Utils.parseColour(theme.getBibleBackgroundcolour()));
        } else if (!theme.getBibleBackgroundimage().isEmpty()) {
            biblebackground = new ImageBackground(theme.getBibleBackgroundimage());
        } else if (!theme.getBibleBackgroundvid().isEmpty()) {
            biblebackground = new VideoBackground(theme.getBibleBackgroundvid(), theme.getBibleVideoHue());
        } else {
            biblebackground = new ColourBackground(Color.BLACK);
        }
        TextShadow biblegivenShadow = theme.getBibleTextShadow();
        SerializableDropShadow bibleshadow = new SerializableDropShadow(Utils.parseColour(givenShadow.getShadowColor()),
                givenShadow.getOffsetX(), givenShadow.getOffsetY());

        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(theme.getFontcolour()), translateFont, Utils.parseColour(theme.getTranslateFontcolour()),
                background, shadow, theme.isFontBold(), theme.isFontItalic(), theme.isTranslateFontBold(), theme.isTranslateFontItalic(), theme.getTextPosition(), theme.getTextAlignment(),
                biblefont, Utils.parseColour(theme.getBibleFontcolour()), biblebackground, bibleshadow, theme.isBibleFontBold(), theme.isBibleFontItalic(), theme.getBibleTextPosition(), theme.getBibleTextAlignment());
        ret.themeName = theme.getName();
        return ret;
    }

    public SerializableDropShadow getShadow() {
        return textShadow;
    }
    
    public SerializableDropShadow getBibleShadow() {
        return bibletextShadow;
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
        ret.append("$translatefontname:").append(translateFont.getFont().getFamily());
        ret.append("$fontcolour:").append(fontColor.toString());
        ret.append("$translatefontcolour:").append(translateFontColor.toString());
        if (!themeName.isEmpty()) {
            ret.append("$themename:").append(themeName);
        }
        ret.append("$isFontBold:").append(isFontBold);
        ret.append("$isFontItalic:").append(isFontItalic);
        ret.append("$isTranslateFontBold:").append(isTranslateFontBold);
        ret.append("$isTranslateFontItalic:").append(isTranslateFontItalic);
        if (background instanceof VideoBackground) {
            ret.append("$backgroundvideo:").append(((VideoBackground) background).getString());
            ret.append("$vidhue:").append(((VideoBackground) background).getHue());
        } else if (background instanceof ImageBackground) {
            ret.append("$backgroundimage:").append(((ImageBackground) background).getString());
        } else if (background instanceof ColourBackground) {
            ret.append("$backgroundcolour:").append(((ColourBackground) background).getString());
        }
        ret.append("$shadowcolor:").append(textShadow.getColor().toString());
        ret.append("$shadowX:").append(textShadow.getOffsetX());
        ret.append("$shadowY:").append(textShadow.getOffsetY());
        ret.append("$textposition:").append(textPosition);
        ret.append("$textalignment:").append(textAlignment);
        ret.append("$biblefontname:").append(biblefont.getFont().getFamily());
        ret.append("$biblefontcolour:").append(biblefontcolour.toString());
        ret.append("$bibleisFontBold:").append(biblefontBold);
        ret.append("$bibleisFontItalic:").append(biblefontItalic);
        if (biblebackground instanceof VideoBackground) {
            ret.append("$biblebackgroundvideo:").append(((VideoBackground) biblebackground).getString());
            ret.append("$biblevidhue:").append(((VideoBackground) biblebackground).getHue());
        } else if (biblebackground instanceof ImageBackground) {
            ret.append("$biblebackgroundimage:").append(((ImageBackground) biblebackground).getString());
        } else if (biblebackground instanceof ColourBackground) {
            ret.append("$biblebackgroundcolour:").append(((ColourBackground) biblebackground).getString());
        }
        ret.append("$bibleshadowcolor:").append(bibletextShadow.getColor().toString());
        ret.append("$bibleshadowX:").append(bibletextShadow.getOffsetX());
        ret.append("$bibleshadowY:").append(bibletextShadow.getOffsetY());
        ret.append("$bibletextposition:").append(bibletextPosition);
        ret.append("$bibletextalignment:").append(bibletextAlignment);
        return ret.toString();
    }

    /**
     * Create Theme DTO form its String representation
     * <p/>
     * @param content Theme String representation
     * @return theme DTO
     */
    public static ThemeDTO fromString(String content) {
        if (content == null || content.isEmpty()) {
            return ThemeDTO.DEFAULT_THEME;
        }
        String fontname = "";
        String fontcolour = "";
        String translatefontname = "";
        String translatefontcolour = "";
        String isFontBold = "";
        String isFontItalic = "";
        String isTranslateFontBold = "";
        String isTranslateFontItalic = "";
        String backgroundcolour = "";
        String backgroundvid = "";
        String backgroundimage = "";
        String themeName = "";
        String shadowColor = "";
        String shadowOffsetX = "0";
        String shadowOffsetY = "0";
        String vidHue = "0";
        String textPosition = "-1";
        String textAlignment = "0";
        //Bible
        String biblefontname = "";
        String biblefontcolour = "";
        String bibleisFontBold = "";
        String bibleisFontItalic = "";
        String biblebackgroundcolour = "";
        String biblebackgroundvid = "";
        String biblebackgroundimage = "";
        String bibleshadowColor = "";
        String bibleshadowOffsetX = "0";
        String bibleshadowOffsetY = "0";
        String biblevidHue = "0";
        String bibletextPosition = "-1";
        String bibletextAlignment = "0";

        for (String part : content.split("\\$")) {
            if (!part.contains(":")) {
                continue;
            }
            String[] parts = part.split(":");
            if (parts[0].equalsIgnoreCase("fontname")) {
                fontname = parts[1];
            } else if (parts[0].equalsIgnoreCase("fontcolour")) {
                fontcolour = parts[1];
            } else if (parts[0].equalsIgnoreCase("translatefontname")) {
                translatefontname = parts[1];
            } else if (parts[0].equalsIgnoreCase("translatefontcolour")) {
                translatefontcolour = parts[1];
            } else if (parts[0].equalsIgnoreCase("isFontBold")) {
                isFontBold = parts[1];
            } else if (parts[0].equalsIgnoreCase("isTranslateFontBold")) {
                isTranslateFontBold = parts[1];
            } else if (parts[0].equalsIgnoreCase("isTranslateFontItalic")) {
                isTranslateFontItalic = parts[1];
            } else if (parts[0].equalsIgnoreCase("isFontItalic")) {
                isFontItalic = parts[1];
            } else if (parts[0].equalsIgnoreCase("backgroundcolour")) {
                backgroundcolour = parts[1];
            } else if (parts[0].equalsIgnoreCase("backgroundimage")) {
                backgroundimage = parts[1];
            } else if (parts[0].equalsIgnoreCase("backgroundvideo")) {
                backgroundvid = parts[1];
            } else if (parts[0].equalsIgnoreCase("themename")) {
                themeName = parts[1];
            } else if (parts[0].equalsIgnoreCase("shadowcolor")) {
                shadowColor = parts[1];
            } else if (parts[0].equalsIgnoreCase("vidhue")) {
                vidHue = parts[1];
            } else if (parts[0].equalsIgnoreCase("textposition")) {
                textPosition = parts[1];
            } else if (parts[0].equalsIgnoreCase("textalignment")) {
                textAlignment = parts[1];
            } else if (parts[0].equalsIgnoreCase("shadowX")) {
                shadowOffsetX = defaultIfEmpty(parts[1], "0");
            } else if (parts[0].equalsIgnoreCase("shadowY")) {
                shadowOffsetY = defaultIfEmpty(parts[1], "0");
            } else if (parts[0].equalsIgnoreCase("biblefontname")) {
                biblefontname = parts[1];
            } else if (parts[0].equalsIgnoreCase("biblefontcolour")) {
                biblefontcolour = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibleisFontBold")) {
                bibleisFontBold = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibleisFontItalic")) {
                bibleisFontItalic = parts[1];
            } else if (parts[0].equalsIgnoreCase("biblebackgroundcolour")) {
                biblebackgroundcolour = parts[1];
            } else if (parts[0].equalsIgnoreCase("biblebackgroundimage")) {
                biblebackgroundimage = parts[1];
            } else if (parts[0].equalsIgnoreCase("biblebackgroundvideo")) {
                biblebackgroundvid = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibleshadowcolor")) {
                bibleshadowColor = parts[1];
            } else if (parts[0].equalsIgnoreCase("biblevidhue")) {
                biblevidHue = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibletextposition")) {
                bibletextPosition = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibletextalignment")) {
                bibletextAlignment = parts[1];
            } else if (parts[0].equalsIgnoreCase("bibleshadowX")) {
                bibleshadowOffsetX = defaultIfEmpty(parts[1], "0");
            } else if (parts[0].equalsIgnoreCase("bibleshadowY")) {
                bibleshadowOffsetY = defaultIfEmpty(parts[1], "0");
            }
        }
        Font sysFont = Font.font(fontname,
                Boolean.parseBoolean(isFontBold) ? FontWeight.BOLD : FontWeight.NORMAL,
                Boolean.parseBoolean(isFontItalic) ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        SerializableFont font = new SerializableFont(sysFont);
        Font sysTranslateFont = Font.font(translatefontname,
                Boolean.parseBoolean(isTranslateFontBold) ? FontWeight.BOLD : FontWeight.NORMAL,
                Boolean.parseBoolean(isTranslateFontItalic) ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        SerializableFont translateFont = new SerializableFont(sysTranslateFont);
        Font biblesysFont = Font.font(biblefontname,
                Boolean.parseBoolean(bibleisFontBold) ? FontWeight.BOLD : FontWeight.NORMAL,
                Boolean.parseBoolean(bibleisFontItalic) ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        SerializableFont biblefont = new SerializableFont(biblesysFont);

        Background background;
        if (!backgroundcolour.trim().isEmpty()) {
            background = new ColourBackground(Utils.parseColour(backgroundcolour));
        } else if (!backgroundimage.trim().isEmpty()) {
            background = new ImageBackground(backgroundimage);
        } else if (!backgroundvid.trim().isEmpty()) {
            background = new VideoBackground(backgroundvid, Double.parseDouble(vidHue));
        } else {
            LOGGER.log(Level.WARNING, "WARNING: Unhandled or empty background, using default background. Raw content: " + content, new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            background = ThemeDTO.DEFAULT_BACKGROUND;
        }
        SerializableDropShadow shadow = new SerializableDropShadow(Utils.parseColour(shadowColor), Double.parseDouble(shadowOffsetX), Double.parseDouble(shadowOffsetY));

        Background biblebackground;
        if (biblebackgroundcolour == null) {
            biblebackground = BIBLE_DEFAULT_BACKGROUND;
        } else if (!biblebackgroundcolour.trim().isEmpty()) {
            biblebackground = new ColourBackground(Utils.parseColour(biblebackgroundcolour));
        } else if (!biblebackgroundimage.trim().isEmpty()) {
            biblebackground = new ImageBackground(biblebackgroundimage);
        } else if (!backgroundvid.trim().isEmpty()) {
            biblebackground = new VideoBackground(biblebackgroundvid, Double.parseDouble(biblevidHue));
        } else {
            LOGGER.log(Level.WARNING, "WARNING: Unhandled or empty bible background, using default background. Raw content: " + content, new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            biblebackground = ThemeDTO.BIBLE_DEFAULT_BACKGROUND;
        }
        SerializableDropShadow bibleshadow = new SerializableDropShadow(Utils.parseColour(bibleshadowColor), Double.parseDouble(bibleshadowOffsetX), Double.parseDouble(bibleshadowOffsetY));

        ThemeDTO ret = new ThemeDTO(font, Utils.parseColour(fontcolour), translateFont, Utils.parseColour(translatefontcolour),
                background, shadow, Boolean.valueOf(isFontBold), Boolean.valueOf(isFontItalic), Boolean.valueOf(isTranslateFontBold),
                Boolean.valueOf(isTranslateFontItalic), Integer.parseInt(textPosition), Integer.parseInt(textAlignment.trim()),
                biblefont, Utils.parseColour(biblefontcolour), biblebackground, bibleshadow,
                Boolean.valueOf(bibleisFontBold), Boolean.valueOf(bibleisFontItalic), Integer.parseInt(bibletextPosition), Integer.parseInt(bibletextAlignment.trim()));
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
        if (val == null) {
            return defaultVal;
        }
        if (val.trim().isEmpty()) {
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

    /**
     * @return the italic
     */
    public boolean isTranslateItalic() {
        return isTranslateFontItalic;
    }

    /**
     * @return the bold
     */
    public boolean isTranslateBold() {
        return isTranslateFontBold;
    }
    
    /**
     * @return the bible italic
     */
    public boolean isBibleItalic() {
        return biblefontItalic;
    }

    /**
     * @return the bible bold
     */
    public boolean isBibleBold() {
        return biblefontBold;
    }
}
