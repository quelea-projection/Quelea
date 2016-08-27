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
package org.quelea.windows.lyrics;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.quelea.services.utils.LyricLine;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Responsible for drawing lyrics and their background.
 * <p/>
 * @author Ben Goodwin, tomaszpio@gmail.com, Michael
 */
public class LyricDrawer extends WordDrawer {

    
    private String[] text;
    private String[] translations;
    private Group textGroup;
    private Group smallTextGroup;
    private ThemeDTO theme;
    private TextDisplayable curDisplayable;
    private boolean capitaliseFirst;
    private String[] smallText;

    public LyricDrawer() {
        text = new String[]{};
        theme = ThemeDTO.DEFAULT_THEME;
        textGroup = new Group();
        smallTextGroup = new Group();
        lastClearedState = new HashMap<>();
    }

    protected void drawText(double defaultFontSize, boolean dumbWrap) {
        Utils.checkFXThread();
        if (defaultFontSize < 1) {
            defaultFontSize = QueleaProperties.get().getMaxFontSize();
        }
        if (getCanvas().getCanvasBackground() != null) {
            if (!getCanvas().getChildren().contains(getCanvas().getCanvasBackground())
                    && !getCanvas().getChildren().contains(textGroup) && !getCanvas().getChildren().contains(smallTextGroup)) {
                getCanvas().getChildren().add(0, getCanvas().getCanvasBackground());
                getCanvas().getChildren().add(textGroup);
                getCanvas().getChildren().add(smallTextGroup);
            }
        }
        Font font = Font.font(theme.getFont().getFamily(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                defaultFontSize);
        String translateFamily = theme.getFont().getFamily();
        if (theme.getTranslateFont() != null) {
            translateFamily = theme.getTranslateFont().getFamily();
        }
        Font translateFont = Font.font(translateFamily,
                theme.isTranslateBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isTranslateItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                defaultFontSize);

        if (font == null) {
            font = ThemeDTO.DEFAULT_FONT.getFont();
        }
        DropShadow shadow = new DropShadow();
        if (theme.getShadow() != null) {
            shadow = theme.getShadow().getDropShadow();
        }
        if (shadow == null) {
            shadow = ThemeDTO.DEFAULT_SHADOW.getDropShadow();
        }

        List<LyricLine> newText;
        if (dumbWrap) {
            newText = new ArrayList<>();
            for (String str : text) {
                for (String line : str.split("\n")) {
                    newText.add(new LyricLine(line));
                }
            }
//            newText = dumbWrapText(text);
        } else {
            newText = sanctifyText(text, translations);
        }
        double fontSize;
//        if (QueleaProperties.get().getUseUniformFontSize()) {
//            fontSize = defaultFontSize;
//        } else {
        fontSize = pickFontSize(font, newText, getCanvas().getWidth() * QueleaProperties.get().getLyricWidthBounds(), getCanvas().getHeight() * QueleaProperties.get().getLyricHeightBounds());
//        }
        font = Font.font(font.getFamily(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                fontSize);
        translateFont = Font.font(translateFont.getFamily(),
                theme.isTranslateBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isTranslateItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                fontSize - QueleaProperties.get().getTranslationFontSizeOffset());
        double smallFontSize;
        Font smallTextFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 500);

        if (curDisplayable instanceof BiblePassage) {
            smallFontSize = pickSmallFontSize(smallTextFont, smallText, getCanvas().getWidth() * 0.8,
                    (getCanvas().getHeight() * (QueleaProperties.get().getSmallBibleTextSize())) - 5); //-5 for insets
        } else {
            smallFontSize = pickSmallFontSize(smallTextFont, smallText, getCanvas().getWidth() * 0.8,
                    (getCanvas().getHeight() * (QueleaProperties.get().getSmallSongTextSize())) - 5); //-5 for insets
        }
        smallTextFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, smallFontSize);

        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        FontMetrics translateMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(translateFont);
        FontMetrics smallTextMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(smallTextFont);
        final Group newTextGroup = new Group();
        shadow.setOffsetX(metrics.getLineHeight() * shadow.getOffsetX() * 0.003);
        shadow.setOffsetY(metrics.getLineHeight() * shadow.getOffsetY() * 0.003);
        shadow.setRadius(shadow.getRadius() * metrics.getLineHeight() * 0.0015);
        newTextGroup.setEffect(shadow);
        StackPane.setAlignment(newTextGroup, Pos.CENTER);
        smallTextGroup = new Group();
        DropShadow smallshadow = theme.getShadow().getDropShadow();
        if (smallshadow == null) {
            smallshadow = new DropShadow();
        }
        smallshadow.setOffsetX(smallTextMetrics.getLineHeight() * shadow.getOffsetX() * 0.03);
        smallshadow.setOffsetY(smallTextMetrics.getLineHeight() * shadow.getOffsetY() * 0.03);
        smallshadow.setRadius(shadow.getRadius() * smallTextMetrics.getLineHeight() * 0.015);
        smallTextGroup.setEffect(smallshadow);

        if (curDisplayable instanceof BiblePassage) {
            if (QueleaProperties.get().getSmallBibleTextPositionV().toLowerCase().equals("top")) {
                if (QueleaProperties.get().getSmallBibleTextPositionH().toLowerCase().equals("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_RIGHT);
                }
            } else {
                if (QueleaProperties.get().getSmallBibleTextPositionH().toLowerCase().equals("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_RIGHT);
                }
            }
        } else {
            if (QueleaProperties.get().getSmallSongTextPositionV().toLowerCase().equals("top")) {
                if (QueleaProperties.get().getSmallSongTextPositionH().toLowerCase().equals("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_RIGHT);
                }
            } else {
                if (QueleaProperties.get().getSmallSongTextPositionH().toLowerCase().equals("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_RIGHT);
                }
            }
        }

        for (Iterator< Node> it = getCanvas().getChildren().iterator(); it.hasNext();) {
            Node node = it.next();
            if (node instanceof Group) {
                it.remove();
            }
        }

        getCanvas().getChildren().add(newTextGroup);
        if (curDisplayable instanceof BiblePassage && QueleaProperties.get().getSmallBibleTextShow()) {
            getCanvas().getChildren().add(smallTextGroup);
        } else if (curDisplayable instanceof SongDisplayable && QueleaProperties.get().getSmallSongTextShow()) {
            getCanvas().getChildren().add(smallTextGroup);
        }
        getCanvas().pushLogoNoticeToFront();

        int y = 0;
        ParallelTransition paintTransition = new ParallelTransition();
        for (LyricLine line : newText) {
            FontMetrics loopMetrics;
            if (line.isTranslateLine()) {
                loopMetrics = translateMetrics;
            } else {
                loopMetrics = metrics;
            }
            FormattedText t;
            t = new FormattedText(line.getLine());

            if (line.isTranslateLine()) {
                t.setFont(translateFont);
            } else {
                t.setFont(font);
            }

            setPositionX(t, loopMetrics, line.getLine(), curDisplayable instanceof BiblePassage);
            t.setLayoutY(y);

            Color lineColor;
            if (line.isTranslateLine()) {
                lineColor = theme.getTranslateFontPaint();
            } else {
                lineColor = theme.getFontPaint();
            }
            if (lineColor == null) {
                LOGGER.log(Level.WARNING, "Warning: Font Color not initialised correctly. Using default font colour.");
                lineColor = ThemeDTO.DEFAULT_FONT_COLOR;
            }
            t.setFill(lineColor);
            y += loopMetrics.getLineHeight() + getLineSpacing();

            newTextGroup.getChildren().add(t);
        }

        int sy = 0;
        for (String stext : smallText) {
            stext = stext.trim();
            FormattedText ft = new FormattedText(stext);
            ft.setFont(smallTextFont);
            ft.setFill(theme.getFontPaint());
            if (curDisplayable instanceof BiblePassage) {
                if (QueleaProperties.get().getSmallBibleTextPositionH().equalsIgnoreCase("right")) {
                    ft.setLayoutX(getCanvas().getWidth() - smallTextMetrics.computeStringWidth(stext));
                }
                if (QueleaProperties.get().getSmallBibleTextPositionV().equalsIgnoreCase("top")) {
                    ft.setLayoutY(getCanvas().getHeight() - sy);
                } else {
                    ft.setLayoutY(sy);
                }
            } else {
                if (QueleaProperties.get().getSmallSongTextPositionH().equalsIgnoreCase("right")) {
                    ft.setLayoutX(getCanvas().getWidth() - smallTextMetrics.computeStringWidth(stext));
                }
                if (QueleaProperties.get().getSmallSongTextPositionV().equalsIgnoreCase("top")) {
                    ft.setLayoutY(getCanvas().getHeight() - sy);
                } else {
                    ft.setLayoutY(sy);
                }
            }
            smallTextGroup.getChildren().add(ft);
            sy += smallTextMetrics.getLineHeight() + 2;
        }
        if (!paintTransition.getChildren().isEmpty()) {
            paintTransition.play();
        }
        textGroup = newTextGroup;
        StackPane.setMargin(textGroup, new Insets(10));
        StackPane.setAlignment(textGroup, DisplayPositionSelector.getPosFromIndex(theme.getTextPosition()));

        StackPane.setMargin(smallTextGroup, new Insets(5));

        if (getCanvas().isCleared() && !getLastClearedState()) {
            setLastClearedState(true);
            FadeTransition t = new FadeTransition(Duration.millis(QueleaProperties.get().getClearFadeDuration()), textGroup);
            FadeTransition t2 = new FadeTransition(Duration.millis(QueleaProperties.get().getClearFadeDuration()), smallTextGroup);
            t.setToValue(0);
            t.play();
            t2.setToValue(0);
            t2.play();
        } else if (getCanvas().isCleared()) {
            textGroup.setOpacity(0);
            smallTextGroup.setOpacity(0);
        } else if (!getCanvas().isCleared() && getLastClearedState()) {
            setLastClearedState(false);
            FadeTransition t = new FadeTransition(Duration.millis(QueleaProperties.get().getClearFadeDuration()), textGroup);
            FadeTransition t2 = new FadeTransition(Duration.millis(QueleaProperties.get().getClearFadeDuration()), smallTextGroup);
            t.setFromValue(0);
            t.setToValue(1);
            t.play();
            t2.setFromValue(0);
            t2.setToValue(1);
            t2.play();
        }
    }

    private void setPositionX(FormattedText t, FontMetrics metrics, String line, boolean biblePassage) {
        Utils.checkFXThread();
        String strippedLine = line.replaceAll("\\<\\/?sup\\>", "");
        double width = metrics.computeStringWidth(strippedLine);
        double leftOffset = 0;
        double centreOffset = (getCanvas().getWidth() - width) / 2;
        double rightOffset = (getCanvas().getWidth() - width);
        if (theme.getTextAlignment() == -1) {
            t.setLayoutX(leftOffset);
        } else if (theme.getTextAlignment() == 0) {
            t.setLayoutX(centreOffset);
        } else if (theme.getTextAlignment() == 1) {
            t.setLayoutX(rightOffset);
        }
    }

    /**
     * Set the theme of this getCanvas().
     * <p/>
     * @param theme the theme to place on the getCanvas().
     */
    @Override
    public void setTheme(ThemeDTO theme) {
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        boolean sameVid = false;
        if (theme.getBackground() instanceof VideoBackground && VLCWindow.INSTANCE.getLastLocation() != null) {
            String newLocation = ((VideoBackground) theme.getBackground()).getVLCVidString();
            String[] locationParts = newLocation.split("[\\r\\n]+");
            if (locationParts.length > 1) {
                newLocation = locationParts[0];
            }
            String oldLocation = VLCWindow.INSTANCE.getLastLocation();
            if (newLocation.equals(oldLocation)) {
                sameVid = true;
            }
        }
        this.theme = theme;
        Image image;
        ColorAdjust colourAdjust = null;
        if (theme.getBackground() instanceof ImageBackground) {
            image = ((ImageBackground) theme.getBackground()).getImage();
        } else if (theme.getBackground() instanceof ColourBackground) {
            Color color = ((ColourBackground) theme.getBackground()).getColour();
            image = Utils.getImageFromColour(color);
        } else if (theme.getBackground() instanceof VideoBackground && getCanvas().getPlayVideo()) {
            image = null;
        } else if (theme.getBackground() instanceof VideoBackground) {
            VideoBackground vidBack = (VideoBackground) theme.getBackground();
            image = Utils.getVidBlankImage(vidBack.getVideoFile());
            colourAdjust = new ColorAdjust();
            double hue = vidBack.getHue() * 2;
            if (hue > 1) {
                hue -= 2;
            }
            hue *= -1;
            colourAdjust.setHue(hue);
        } else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled theme background case, trying to use default background: " + theme.getBackground(), new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            image = Utils.getImageFromColour(ThemeDTO.DEFAULT_BACKGROUND.getColour());
        }

        Node newBackground;
        if (image == null) {
            final VideoBackground vidBackground = (VideoBackground) theme.getBackground();
            if (!sameVid || !VLCWindow.INSTANCE.isPlaying()) {
                final String location = vidBackground.getVLCVidString();
                final boolean stretch = vidBackground.getStretch();
                String[] locationParts = location.split("[\\r\\n]+");
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
                VLCWindow.INSTANCE.setRepeat(true);
                if (locationParts.length == 1) {
                    VLCWindow.INSTANCE.play(locationParts[0], null, stretch);
                } else {
                    VLCWindow.INSTANCE.play(locationParts[0], locationParts[1], stretch);
                }
                VLCWindow.INSTANCE.setHue(vidBackground.getHue());
            }
            if (sameVid && VLCWindow.INSTANCE.getHue() != ((VideoBackground) theme.getBackground()).getHue()) {
                VLCWindow.INSTANCE.fadeHue(vidBackground.getHue());
            }
            newBackground = null; //transparent
        } else {
            if (getCanvas().getPlayVideo() && !(theme.getBackground() instanceof VideoBackground)) {
                VLCWindow.INSTANCE.stop();
            }
            final ImageView newImageView = getCanvas().getNewImageView();
            newImageView.setFitHeight(getCanvas().getHeight());
            newImageView.setFitWidth(getCanvas().getWidth());
            newImageView.setImage(image);
            if (colourAdjust != null) {
                newImageView.setEffect(colourAdjust);
            }
            getCanvas().getChildren().add(newImageView);
            newBackground = newImageView;
        }
        getCanvas().getChildren().remove(getCanvas().getCanvasBackground());
        getCanvas().setOpacity(1);
        getCanvas().setCanvasBackground(newBackground);
    }

    /**
     * Get the theme currently in use on the getCanvas().
     * <p/>
     * @return the current theme
     */
    @Override
    public ThemeDTO getTheme() {
        return theme;
    }

    @Override
    public void requestFocus() {
    }

    /**
     * Set whether the first of each line should be capitalised.
     * <p/>
     * @param val true if the first character should be, false otherwise.
     */
    @Override
    public void setCapitaliseFirst(boolean val) {
        this.capitaliseFirst = val;
    }

    /**
     * Take the raw text and format it into a number of lines nicely, where the
     * lines aren't more than the maximum length.
     * <p/>
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<LyricLine> sanctifyText(String[] linesArr, String[] translationArr) {
        List<LyricLine> finalLines = new ArrayList<>();
        int translationOffset = 0;
        for (int i = 0; i < linesArr.length; i++) {
            finalLines.add(new LyricLine(linesArr[i]));
            if(new LineTypeChecker(linesArr[i]).getLineType() == Type.NONBREAK) {
                continue;
            }
            if (translationArr != null && i < translationArr.length) {
                while (i + translationOffset < translationArr.length && new LineTypeChecker(translationArr[i + translationOffset]).getLineType() != Type.NORMAL) {
                    translationOffset++;
                }
                if (i + translationOffset < translationArr.length && new LineTypeChecker(translationArr[i + translationOffset]).getLineType() == Type.NORMAL) {
                    finalLines.add(new LyricLine(true, translationArr[i + translationOffset]));
                }
            }
        }

        List<LyricLine> ret = new ArrayList<>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for (LyricLine line : finalLines) {
            if ((translationArr != null && translationArr.length > 0)) {
                ret.add(line);
            } else {
                List<String> splits = splitLine(line.getLine(), maxLength);
                for (String split : splits) {
                    ret.add(new LyricLine(split));
                }
            }
        }
        return ret;
    }

    /**
     * Given a line of any length, sensibly split it up into several lines.
     * <p/>
     * @param line the line to split.
     * @return the split line (or the unaltered line if it is less than or equal
     * to the allowed length.
     */
    private List<String> splitLine(String line, int maxLength) {
        List<String> sections = new ArrayList<>();
        if (line.length() > maxLength) {
            if (containsNotAtEnd(line, ";")) {
                for (String s : splitMiddle(line, ';')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            } else if (containsNotAtEnd(line, ",")) {
                for (String s : splitMiddle(line, ',')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            } else if (containsNotAtEnd(line, " ")) {
                for (String s : splitMiddle(line, ' ')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            } else {
                sections.addAll(splitLine(new StringBuilder(line).insert(line.length() / 2, " ").toString(), maxLength));
            }
        } else {
            line = line.trim();
            if (capitaliseFirst && QueleaProperties.get().checkCapitalFirst()) {
                line = Utils.capitaliseFirst(line);
            }
            sections.add(line);
        }
        return sections;
    }

    /**
     * Determine the largest font size we can safely use for every section of a
     * text displayable.
     * <p>
     * @param displayable the displayable to check.
     * @return the font size to use
     */
    private double getUniformFontSize(TextDisplayable displayable) {
        if (!QueleaProperties.get().getUseUniformFontSize()) {
            return -1;
        }
        Font font = theme.getFont();
        font = Font.font(font.getName(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        double fontSize = Double.POSITIVE_INFINITY;
        for (int i = 0; i < displayable.getSections().length; i++) {
            TextSection section = displayable.getSections()[i];
            String[] textArr;
            textArr = section.getText(false, false);
            List<LyricLine> processedText;
            double newSize;
            if (displayable instanceof BiblePassage) {
                processedText = new ArrayList<>();
                for (String str : displayable.getSections()[i].getText(false, false)) {
                    for (String line : str.split("\n")) {
                        processedText.add(new LyricLine(line));
                    }
                }
//                processedText = dumbWrapText(textArr);
            } else {
                String[] translationArr = null;
                if (displayable instanceof SongDisplayable) {
                    String translationLyrics = ((SongDisplayable) displayable).getCurrentTranslationSection(i);
                    if (translationLyrics != null) {
                        translationArr = translationLyrics.split("\n");
                    }
                }
                processedText = sanctifyText(textArr, translationArr);
            }
            newSize = pickFontSize(font, processedText, getCanvas().getWidth() * QueleaProperties.get().getLyricWidthBounds(), getCanvas().getHeight() * QueleaProperties.get().getLyricHeightBounds());
            if (newSize < fontSize) {
                fontSize = newSize;
            }
        }
        if (fontSize == Double.POSITIVE_INFINITY) {
            fontSize = -1;
        }
        return fontSize;
    }

    @Override
    public void setText(TextDisplayable displayable, int index) {
        boolean fade = curDisplayable != displayable;
        double uniformFontSize = getUniformFontSize(displayable);
        curDisplayable = displayable;
        String[] bigText;
        bigText = displayable.getSections()[index].getText(false, false);
        String[] translationArr = null;
        if (displayable instanceof SongDisplayable) {
            String translationText = ((SongDisplayable) displayable).getCurrentTranslationSection(index);
            if (translationText != null) {
                translationArr = translationText.split("\n");
            }
        }
        setText(bigText, translationArr, displayable.getSections()[index].getSmallText(), fade, uniformFontSize);
    }

    /**
     * Set the text to appear on the getCanvas(). The lines will be
     * automatically wrapped and if the text is too large to fit on the screen
     * in the current font, the size will be decreased until all the text fits.
     * <p/>
     * @param text an array of the lines to display on the canvas, one entry in
     * the array is one line.
     * @param translations the translation to use for the current section, or
     * null if none should be used.
     * @param smallText an array of the small lines to be displayed on the
     * getCanvas().
     * @param fade true if the text should fade, false otherwise.
     * @param fontSize the font size to use to draw this text.
     */
    @Override
    public void setText(String[] text, String[] translations, String[] smallText, boolean fade, double fontSize) {
        if (text == null) {
            text = new String[0];
        }
        if (smallText == null) {
            smallText = new String[0];
        }
        if (translations == null) {
            translations = new String[0];
        }
        this.text = Arrays.copyOf(text, text.length);
        this.translations = Arrays.copyOf(translations, translations.length);
        this.smallText = Arrays.copyOf(smallText, smallText.length);
        draw(curDisplayable, fontSize);
    }

    /**
     * Get the text currently set to appear on the getCanvas(). The text may or
     * may not be shown depending on whether the canvas is blacked or cleared.
     * <p/>
     * @return the current text.
     */
    public String[] getText() {
        return Arrays.copyOf(text, text.length);
    }

    @Override
    public void draw(Displayable displayable) {
        draw(displayable, -1);
    }

    public void draw(Displayable displayable, double fontSize) {
        drawText(fontSize, displayable instanceof BiblePassage);
        if (getCanvas().getCanvasBackground() instanceof ImageView) {
            ImageView imgBackground = (ImageView) getCanvas().getCanvasBackground();
            imgBackground.setFitHeight(getCanvas().getHeight());
            imgBackground.setFitWidth(getCanvas().getWidth());
        } else if (getCanvas().getCanvasBackground() != null) {
            LOGGER.log(Level.WARNING, "BUG: Unrecognised image background - " + getCanvas().getCanvasBackground().getClass(), new RuntimeException("DEBUG EXCEPTION"));
        }
    }

    @Override
    public void clear() {
        if (getCanvas().getChildren() != null) {
            getCanvas().clearNonPermanentChildren();
        }
        setTheme(ThemeDTO.DEFAULT_THEME);
        eraseText();
    }
}
