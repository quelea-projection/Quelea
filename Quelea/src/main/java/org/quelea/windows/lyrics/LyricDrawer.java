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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.utils.FXFontMetrics;
import org.quelea.utils.WrapTextResult;
import org.quelea.windows.video.VidDisplay;

/**
 * Responsible for drawing lyrics and their background.
 * <p/>
 *
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
    private Group oldTextGroup;
    private String[] oldText;
    private boolean newItem;
    private VidDisplay vidDisplay;

    public LyricDrawer() {
        text = new String[]{};
        theme = ThemeDTO.DEFAULT_THEME;
        textGroup = new Group();
        smallTextGroup = new Group();
        lastClearedState = new HashMap<>();
        vidDisplay = new VidDisplay();
        vidDisplay.setLoop(true);
    }

    protected void drawText(double defaultFontSize, boolean dumbWrap) {
        Utils.checkFXThread();
        if (defaultFontSize < 1) {
            defaultFontSize = QueleaProperties.get().getMaxFontSize();

            // Scale the default font size for this canvas
            defaultFontSize *= canvasScalingFactor();
        }
        if (getCanvas().getCanvasBackground() != null) {
            if (!getCanvas().getChildren().contains(getCanvas().getCanvasBackground())
                    && !getCanvas().getChildren().contains(textGroup) && !getCanvas().getChildren().contains(smallTextGroup)) {
                getCanvas().getChildren().add(0, getCanvas().getCanvasBackground());
                getCanvas().getChildren().add(textGroup);
                getCanvas().getChildren().add(smallTextGroup);
            }
        }

        if (getCanvas().equals(QueleaApp.get().getProjectionWindow().getCanvas())) {
            newItem = oldText != null && !Arrays.deepEquals(getText(), oldText) && textGroup != null;
            oldText = getText();
        } else {
            oldTextGroup = textGroup;
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
        double fontSize = -1;
        if (dumbWrap) {
            if (text.length == 0) {
                fontSize = 1;
                newText = new ArrayList<>();
            } else {
                WrapTextResult result = normalWrapText(font, text[0], getCanvas().getWidth() * QueleaProperties.get().getLyricWidthBounds(), getCanvas().getHeight() * QueleaProperties.get().getLyricHeightBounds());
                newText = result.getNewText();
                fontSize = result.getFontSize();
            }
        } else {
            newText = sanctifyText(text, translations);
        }
        if (fontSize == -1) {
            fontSize = pickFontSize(font, newText, getCanvas().getWidth() * QueleaProperties.get().getLyricWidthBounds(), getCanvas().getHeight() * QueleaProperties.get().getLyricHeightBounds());
        }
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

        FXFontMetrics metrics = new FXFontMetrics(font);
        FXFontMetrics translateMetrics = new FXFontMetrics(translateFont);
        FXFontMetrics smallTextMetrics = new FXFontMetrics(smallTextFont);
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
            if (QueleaProperties.get().getSmallBibleTextPositionV().equalsIgnoreCase("top")) {
                if (QueleaProperties.get().getSmallBibleTextPositionH().equalsIgnoreCase("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_RIGHT);
                }
            } else {
                if (QueleaProperties.get().getSmallBibleTextPositionH().equalsIgnoreCase("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_RIGHT);
                }
            }
        } else {
            if (QueleaProperties.get().getSmallSongTextPositionV().equalsIgnoreCase("top")) {
                if (QueleaProperties.get().getSmallSongTextPositionH().equalsIgnoreCase("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.TOP_RIGHT);
                }
            } else {
                if (QueleaProperties.get().getSmallSongTextPositionH().equalsIgnoreCase("left")) {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_LEFT);
                } else {
                    StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_RIGHT);
                }
            }
        }
        getCanvas().getChildren().removeIf(node -> node instanceof Group);

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
            FXFontMetrics loopMetrics;
            if (line.isTranslateLine()) {
                loopMetrics = translateMetrics;
            } else {
                loopMetrics = metrics;
            }
            FormattedText t;
            if (QueleaProperties.get().getTextBackgroundEnable()) {
                t = new FormattedText(" " + line.getLine() + " ");
                t.setBackground(new Background(new BackgroundFill(QueleaProperties.get().getTextBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                t = new FormattedText(line.getLine());
            }
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
        } else if (QueleaProperties.get().getUseSlideTransition()
                && !getCanvas().isBlacked() && !getCanvas().isCleared()
                && !getCanvas().isShowingLogo()
                && getCanvas().equals(QueleaApp.get().getProjectionWindow().getCanvas())) {
            if (oldTextGroup != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(QueleaProperties.get().getSlideTransitionOutDuration()), oldTextGroup);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    if (getCanvas().getChildren().contains(oldTextGroup)) {
                        getCanvas().getChildren().remove(oldTextGroup);
                        oldTextGroup = null;
                    }
                });
                getCanvas().getChildren().add(oldTextGroup);
                fadeOut.play();
            }
            if (oldTextGroup == null && Arrays.deepToString(oldText).equals("[]") || newItem) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(QueleaProperties.get().getSlideTransitionInDuration()), textGroup);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        }
    }

    private void setPositionX(FormattedText t, FXFontMetrics metrics, String line, boolean biblePassage) {
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

    static int iter = 0;

    /**
     * Set the theme of this getCanvas().
     * <p/>
     *
     * @param theme the theme to place on the getCanvas().
     */
    @Override
    public void setTheme(ThemeDTO theme) {
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        this.theme = theme;
        Image image = null;
        ColorAdjust colourAdjust = null;
        final ImageView newImageView = getCanvas().getNewImageView();
        if (theme.getBackground() instanceof ImageBackground) {
            image = ((ImageBackground) theme.getBackground()).getImage();
        } else if (theme.getBackground() instanceof ColourBackground) {
            Color color = ((ColourBackground) theme.getBackground()).getColour();
            image = Utils.getImageFromColour(color);
        } else if (theme.getBackground() instanceof VideoBackground) {
            var uri = ((VideoBackground) theme.getBackground()).getVideoFile().toURI();
            if(!Objects.equals(vidDisplay.getUri(), uri)) {
                vidDisplay.stop();
                vidDisplay.setURI(uri);
                vidDisplay.play();
            }
            newImageView.imageProperty().bind(vidDisplay.imageProperty());

        } else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled theme background case, trying to use default background: " + theme.getBackground(), new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            image = Utils.getImageFromColour(ThemeDTO.DEFAULT_BACKGROUND.getColour());
        }

        newImageView.setFitHeight(getCanvas().getHeight());
        newImageView.setFitWidth(getCanvas().getWidth());
        if (image != null) {
            newImageView.setImage(image);
        }

//        if (colourAdjust != null) {
//            newImageView.setEffect(colourAdjust);
//        }
        newImageView.setOpacity(0);
        getCanvas().getChildren().add(newImageView);

        if(QueleaProperties.get().getUseSlideTransition()) {
            FadeTransition ft = new FadeTransition(Duration.millis(QueleaProperties.get().getSlideTransitionInDuration()), newImageView);
            ft.setToValue(1);
            ft.play();

            ft.setOnFinished(actionEvent -> {
                getCanvas().setOpacity(1);
                getCanvas().getChildren().remove(getCanvas().getCanvasBackground());
                getCanvas().setCanvasBackground(newImageView);
            });
        } else {
            newImageView.setOpacity(1);
            getCanvas().setOpacity(1);
            getCanvas().getChildren().remove(getCanvas().getCanvasBackground());
            getCanvas().setCanvasBackground(newImageView);
        }
    }

    /**
     * Get the theme currently in use on the getCanvas().
     * <p/>
     *
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
     *
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
     *
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<LyricLine> sanctifyText(String[] linesArr, String[] translationArr) {
        List<LyricLine> finalLines = new ArrayList<>();
        int translationOffset = 0;
        for (int i = 0; i < linesArr.length; i++) {
            finalLines.add(new LyricLine(linesArr[i]));
            if (new LineTypeChecker(linesArr[i]).getLineType() == Type.NONBREAK) {
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
     *
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
     *
     * @param displayable the displayable to check.
     * @return the font size to use
     */
    private double getUniformFontSize(TextDisplayable displayable) {
        if (!QueleaProperties.get().getUseUniformFontSize()) {
            return -1;
        }

        // Retrieve and scale the max font size for this canvas
        double maxFontSizeForCanvas = QueleaProperties.get().getMaxFontSize();
        maxFontSizeForCanvas *= canvasScalingFactor();

        int width = (int) (getCanvas().getWidth() * QueleaProperties.get().getLyricWidthBounds());
        int height = (int) (getCanvas().getHeight() * QueleaProperties.get().getLyricHeightBounds());

        if (displayable instanceof BiblePassage) {
            height *= 1 - QueleaProperties.get().getSmallBibleTextSize();
        } else {
            height *= 1 - QueleaProperties.get().getSmallSongTextSize();
        }

        Double cachedSize = displayable.getCachedUniformFontSize(new Dimension(width, height));
        if (cachedSize != null) {
            return cachedSize;
        }
        Font font = theme.getFont();
        font = Font.font(font.getName(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                maxFontSizeForCanvas);
        double fontSize = Double.POSITIVE_INFINITY;
        for (int i = 0; i < displayable.getSections().length; i++) {
            TextSection section = displayable.getSections()[i];
            String[] textArr;
            textArr = section.getText(false, false);
            List<LyricLine> processedText;
            double newSize;
            if (displayable instanceof BiblePassage) {
                WrapTextResult result = normalWrapText(font, textArr[0], width, height);
                if (result.getFontSize() < fontSize) {
                    fontSize = result.getFontSize();
                }
            } else {
                String[] translationArr = null;
                String translationLyrics = ((SongDisplayable) displayable).getCurrentTranslationSection(i);
                if (translationLyrics != null) {
                    translationArr = translationLyrics.split("\n");
                }
                processedText = sanctifyText(textArr, translationArr);
                newSize = pickFontSize(font, processedText, width, height);
                if (newSize < fontSize) {
                    fontSize = newSize;
                }
            }
        }
        if (fontSize == Double.POSITIVE_INFINITY) {
            fontSize = -1;
        }
        displayable.setCachedUniformFontSize(new Dimension(width, height), fontSize);
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

        String[] smallText = displayable.getSections()[index].getSmallText();
        if (QueleaProperties.get().getSmallSongTextShowOnSlides().equals("first") && index > 0) {
            smallText = new String[0];
        }
        if (QueleaProperties.get().getSmallSongTextShowOnSlides().equals("last") && index < displayable.getSections().length - 1) {
            smallText = new String[0];
        }

        setText(bigText, translationArr, smallText, fade, uniformFontSize);
    }

    /**
     * Set the text to appear on the getCanvas(). The lines will be
     * automatically wrapped and if the text is too large to fit on the screen
     * in the current font, the size will be decreased until all the text fits.
     * <p/>
     *
     * @param text         an array of the lines to display on the canvas, one entry in
     *                     the array is one line.
     * @param translations the translation to use for the current section, or
     *                     null if none should be used.
     * @param smallText    an array of the small lines to be displayed on the
     *                     getCanvas().
     * @param fade         true if the text should fade, false otherwise.
     * @param fontSize     the font size to use to draw this text.
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
     *
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
