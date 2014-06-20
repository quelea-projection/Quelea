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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.quelea.data.Background;
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
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.LyricLine;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Responsible for drawing lyrics and their background.
 * <p/>
 * @author Ben Goodwin, tomaszpio@gmail.com, Michael
 */
public class LyricDrawer extends DisplayableDrawer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String[] text;
    private String[] translations;
    private Group textGroup;
    private Group smallTextGroup;
    private ThemeDTO theme;
    private TextDisplayable curDisplayable;
    private boolean capitaliseFirst;
    private final Map<DisplayCanvas, Boolean> lastClearedState;
    private String[] smallText;
    private boolean bible;

    public LyricDrawer() {
        text = new String[]{};
        theme = ThemeDTO.DEFAULT_THEME;
        textGroup = new Group();
        smallTextGroup = new Group();
        lastClearedState = new HashMap<>();
    }

    private void drawText(double defaultFontSize, boolean dumbWrap, boolean bible) {
        Utils.checkFXThread();
        this.bible = bible;
        boolean stageView = getCanvas().isStageView();
        if (getCanvas().getCanvasBackground() != null) {
            if (!getCanvas().getChildren().contains(getCanvas().getCanvasBackground())
                    && !getCanvas().getChildren().contains(textGroup) && !getCanvas().getChildren().contains(smallTextGroup)) {
                getCanvas().getChildren().add(0, getCanvas().getCanvasBackground());
                getCanvas().getChildren().add(textGroup);
                getCanvas().getChildren().add(smallTextGroup);
            }
        }
        Font font;
        DropShadow shadow;
        Color lineColor;
        if (bible) {
            font = Font.font(theme.getBibleFont().getFamily(),
                    theme.isBibleBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isBibleItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    QueleaProperties.get().getMaxFontSize());

            shadow = theme.getShadow().getDropShadow();

            lineColor = theme.getBibleFontPaint();

            StackPane.setAlignment(textGroup, DisplayPositionSelector.getPosFromIndex(theme.getBibleTextPosition()));
        } else if (stageView) {
            font = Font.font(QueleaProperties.get().getStageTextFont(), QueleaProperties.get().getMaxFontSize());
            shadow = new DropShadow();

            lineColor = QueleaProperties.get().getStageLyricsColor();

            StackPane.setAlignment(textGroup, Pos.CENTER);

        } else {
            font = Font.font(theme.getFont().getFamily(),
                    theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    QueleaProperties.get().getMaxFontSize());

            shadow = theme.getShadow().getDropShadow();

            lineColor = theme.getFontPaint();

            StackPane.setAlignment(textGroup, DisplayPositionSelector.getPosFromIndex(theme.getTextPosition()));

        }

        if (font == null) {
            font = ThemeDTO.DEFAULT_FONT.getFont();
        }
        if (shadow == null) {
            shadow = ThemeDTO.DEFAULT_SHADOW.getDropShadow();
        }
        shadow.setHeight(5);
        shadow.setWidth(5);
        if (lineColor == null) {
            LOGGER.log(Level.WARNING, "Warning: Font Color not initialised correctly. Using default font colour.");
            lineColor = ThemeDTO.DEFAULT_FONT_COLOR;
        }

        Font translateFont = Font.font(theme.getTranslateFont().getFamily(),
                theme.isTranslateBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isTranslateItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());

        List<LyricLine> newText;
        if (dumbWrap) {
            newText = dumbWrapText(text);
        } else {
            newText = sanctifyText(text, translations);
        }
        double fontSize;
        if (defaultFontSize > 0) {
            fontSize = defaultFontSize;
        } else {
            fontSize = pickFontSize(font, newText, getCanvas().getWidth() * 0.9, getCanvas().getHeight() * 0.9);
        }
        if (bible) {
            font = Font.font(font.getFamily(),
                    theme.isBibleBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isBibleItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSize);
        } else if (stageView) {
            font = Font.font(font.getFamily(), FontWeight.NORMAL,
                    FontPosture.REGULAR, fontSize);
        } else {
            font = Font.font(font.getFamily(),
                    theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSize);
            translateFont = Font.font(translateFont.getFamily(),
                    theme.isTranslateBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isTranslateItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSize - 3);
        }
        double smallFontSize;
        Font smallTextFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 500);
        smallFontSize = pickSmallFontSize(smallTextFont, smallText, getCanvas().getWidth() * 0.5, (getCanvas().getHeight() * 0.07) - 5); //-5 for insets
        smallTextFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, smallFontSize);

        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        FontMetrics translateMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(translateFont);
        FontMetrics smallTextMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(smallTextFont);
        final Group newTextGroup = new Group();
        StackPane.setAlignment(newTextGroup, Pos.CENTER);
        smallTextGroup = new Group();
        if (QueleaProperties.get().getSmallTextPosition().toLowerCase().equals("left")) {
            StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_LEFT);
        } else {
            StackPane.setAlignment(smallTextGroup, Pos.BOTTOM_RIGHT);
        }

        for (Iterator< Node> it = getCanvas().getChildren().iterator(); it.hasNext();) {
            Node node = it.next();
            if (node instanceof Group) {
                it.remove();
            }
        }

        getCanvas().getChildren().add(newTextGroup);
        if (bible && QueleaProperties.get().getSmallBibleTextShow()) {
            getCanvas().getChildren().add(smallTextGroup);
        }
        if (curDisplayable instanceof SongDisplayable && QueleaProperties.get().getSmallSongTextShow()) {
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
            t.setEffect(shadow);

            setPositionX(t, loopMetrics, line.getLine(), stageView, bible);
            t.setLayoutY(y);

            if (stageView && new LineTypeChecker(line.getLine()).getLineType() == LineTypeChecker.Type.CHORDS) {
                lineColor = QueleaProperties.get().getStageChordColor();
            } else if (stageView) {
                lineColor = QueleaProperties.get().getStageLyricsColor();
            } else if (line.isTranslateLine()) {
                lineColor = theme.getTranslateFontPaint();
            } else {
                lineColor = theme.getFontPaint();
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
            if (bible) {
                ft.setFill(theme.getBibleFontPaint());
            } else {
                ft.setFill(theme.getFontPaint());
            }
            ft.setLayoutY(sy);
            if (QueleaProperties.get().getSmallTextPosition().equalsIgnoreCase("right")) {
                ft.setLayoutX(getCanvas().getWidth() - smallTextMetrics.computeStringWidth(stext));
            }
            smallTextGroup.getChildren().add(ft);
            sy += smallTextMetrics.getLineHeight() + 2;
        }
        if (!paintTransition.getChildren().isEmpty()) {
            paintTransition.play();
        }
        textGroup = newTextGroup;
        StackPane.setMargin(textGroup, new Insets(10));

        StackPane.setMargin(smallTextGroup, new Insets(5));

        if (getCanvas().isCleared() && !getLastClearedState()) {
            setLastClearedState(true);
            FadeTransition t = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), textGroup);
            FadeTransition t2 = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), smallTextGroup);
            t.setToValue(0);
            t.play();
            t2.setToValue(0);
            t2.play();
        } else if (getCanvas().isCleared()) {
            textGroup.setOpacity(0);
            smallTextGroup.setOpacity(0);
        } else if (!getCanvas().isCleared() && getLastClearedState()) {
            setLastClearedState(false);
            FadeTransition t = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), textGroup);
            FadeTransition t2 = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), smallTextGroup);
            t.setFromValue(0);
            t.setToValue(1);
            t.play();
            t2.setFromValue(0);
            t2.setToValue(1);
            t2.play();
        }
    }

    private void setPositionX(FormattedText t, FontMetrics metrics, String line, boolean stageView, boolean bible) {
        Utils.checkFXThread();
        String strippedLine = line.replaceAll("\\<\\/?sup\\>", "");
        double width = metrics.computeStringWidth(strippedLine);
        double leftOffset = 0;
        double centreOffset = (getCanvas().getWidth() - width) / 2;
        double rightOffset = (getCanvas().getWidth() - width);
        if (stageView) {
            if (QueleaProperties.get().getStageTextAlignment().equalsIgnoreCase("Left")) {
                t.setLayoutX(getCanvas().getWidth());
            } else {
                t.setLayoutX(centreOffset);
            }
        } else if (bible && theme.getBibleTextAlignment() == -1) {
            t.setLayoutX(leftOffset);
        } else if (bible && theme.getBibleTextAlignment() == 0) {
            t.setLayoutX(centreOffset);
        } else if (bible && theme.getBibleTextAlignment() == 1) {
            t.setLayoutX(rightOffset);
        } else if (theme.getTextAlignment() == -1) {
            t.setLayoutX(leftOffset);
        } else if (theme.getTextAlignment() == 0) {
            t.setLayoutX(centreOffset);
        } else if (theme.getTextAlignment() == 1) {
            t.setLayoutX(rightOffset);
        }
    }

    private boolean getLastClearedState() {
        Boolean val = lastClearedState.get(getCanvas());
        if (val == null) {
            return false;
        }
        return val;
    }

    private void setLastClearedState(boolean val) {
        lastClearedState.put(getCanvas(), val);
    }

    /**
     * Set the theme of this getCanvas().
     * <p/>
     * @param theme the theme to place on the getCanvas().
     */
    public void setTheme(ThemeDTO theme, boolean bible) {
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        boolean sameVid = false;
        Background background;
        if (bible) {
            background = theme.getBibleBackground();
        } else {
            background = theme.getBackground();
        }
        if (background instanceof VideoBackground && VLCWindow.INSTANCE.getLastLocation() != null) {
            String newLocation = ((VideoBackground) background).getVideoFile().getAbsolutePath();
            String oldLocation = VLCWindow.INSTANCE.getLastLocation();
            if (newLocation.equals(oldLocation)) {
                sameVid = true;
            }
        }
        this.theme = theme;
        Image image;
        ColorAdjust colourAdjust = null;
        if (getCanvas().isStageView()) {
            image = Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor());
        }

        if (background instanceof ImageBackground) {
            image = ((ImageBackground) background).getImage();
        } else if (background instanceof ColourBackground) {
            Color color = ((ColourBackground) background).getColour();
            image = Utils.getImageFromColour(color);
        } else if (background instanceof VideoBackground && getCanvas().getPlayVideo()) {
            image = null;
        } else if (background instanceof VideoBackground) {
            VideoBackground vidBack = (VideoBackground) background;
            image = Utils.getVidBlankImage(vidBack.getVideoFile());
            colourAdjust = new ColorAdjust();
            double hue = vidBack.getHue() * 2;
            if (hue > 1) {
                hue -= 2;
            }
            hue *= -1;
            colourAdjust.setHue(hue);
        } else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled theme background case, trying to use default background: " + background, new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            if(bible) {
                image = Utils.getImageFromColour(ThemeDTO.BIBLE_DEFAULT_BACKGROUND.getColour());
            }
            else {
                image = Utils.getImageFromColour(ThemeDTO.DEFAULT_BACKGROUND.getColour());
            }
        }

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), getCanvas().getCanvasBackground());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        Node newBackground;
        if (image == null) {
            final VideoBackground vidBackground = (VideoBackground) background;
            if (!sameVid || !VLCWindow.INSTANCE.isPlaying()) {
                final String location = vidBackground.getVideoFile().getAbsolutePath();
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
                VLCWindow.INSTANCE.setRepeat(true);
                VLCWindow.INSTANCE.play(location);
                VLCWindow.INSTANCE.setHue(vidBackground.getHue());
            }
            if (sameVid && VLCWindow.INSTANCE.getHue() != ((VideoBackground) background).getHue()) {
                VLCWindow.INSTANCE.fadeHue(vidBackground.getHue());
            }
            newBackground = null; //transparent
        } else {
            if (getCanvas().getPlayVideo() && !(background instanceof VideoBackground)) {
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
        final Node oldBackground = getCanvas().getCanvasBackground();

        fadeOut.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                getCanvas().getChildren().remove(oldBackground);
            }
        });
        getCanvas().setOpacity(1);
        getCanvas().setCanvasBackground(newBackground);
        fadeOut.play();
    }

    /**
     * Get the theme currently in use on the getCanvas().
     * <p/>
     * @return the current theme
     */
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
    public void setCapitaliseFirst(boolean val) {
        this.capitaliseFirst = val;
    }

    /**
     * Pick a font size for the specified font that fits the given text into the
     * width and height provided.
     * <p>
     * @param font the font to use for calculations.
     * @param text the text to fit.
     * @param width the fit width.
     * @param height the fit height.
     * @return a font size for the specified font that fits the text into the
     * width and height provided.
     */
    private double pickFontSize(Font font, List<LyricLine> text, double width, double height) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double totalHeight = ((metrics.getLineHeight() + getLineSpacing()) * text.size());
        while (totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + getLineSpacing()) * text.size();
        }

        String longestLine = longestLine(font, text);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > width) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalWidth = metrics.computeStringWidth(longestLine);
        }

        return font.getSize();
    }

    private double getLineSpacing() {
        double space = QueleaProperties.get().getAdditionalLineSpacing();
        double factor = getCanvas().getHeight() / 1000.0;
        return space * factor;
    }

    private String longestLine(Font font, List<LyricLine> text) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double longestWidth = -1;
        String longestStr = null;
        for (LyricLine line : text) {
            line = new LyricLine(line.isTranslateLine(), FormattedText.stripFormatTags(line.getLine()));
            double width = metrics.computeStringWidth(line.getLine());
            if (width > longestWidth) {
                longestWidth = width;
                longestStr = line.getLine();
            }
        }
        return longestStr;
    }

    private String longestLine(Font font, ArrayList<String> text) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double longestWidth = -1;
        String longestStr = null;
        for (String line : text) {
            double width = metrics.computeStringWidth(line);
            if (width > longestWidth) {
                longestWidth = width;
                longestStr = line;
            }
        }
        return longestStr;
    }

    /**
     * Wrap the text in a "dumb" way, not worrying about dividing up lines
     * nicely. This works better for bible passages.
     * <p>
     * @param lines the lines to divide up.
     * @return the divided lines.
     */
    private List<LyricLine> dumbWrapText(String[] lines) {
        List<LyricLine> ret = new ArrayList<>();
        int maxLength = 60;
        StringBuilder currentBuilder = new StringBuilder(maxLength);
        for (String line : lines) {
            for (String word : line.split(" ")) {
                currentBuilder.append(' ');
                if (currentBuilder.length() + word.length() < maxLength) {
                    currentBuilder.append(word);
                } else {
                    ret.add(new LyricLine(currentBuilder.toString()));
                    currentBuilder = new StringBuilder(word);
                }
            }
        }
        if (currentBuilder.length() > 0) {
            ret.add(new LyricLine(currentBuilder.toString()));
        }
        return ret;
    }

    /**
     * Take the raw text and format it into a number of lines nicely, where the
     * lines aren't more than the maximum length.
     * <p/>
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<LyricLine> sanctifyText(String[] linesArr, String[] translationArr) {
        List<LyricLine> finalLines = new ArrayList<>();
        for (int i = 0; i < linesArr.length; i++) {
            finalLines.add(new LyricLine(linesArr[i]));
            if (translationArr != null && i < translationArr.length && new LineTypeChecker(translationArr[i]).getLineType() == Type.NORMAL) {
                finalLines.add(new LyricLine(true, translationArr[i]));
            }
        }

        List<LyricLine> ret = new ArrayList<>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for (LyricLine line : finalLines) {
            if (getCanvas().isStageView() || (translationArr != null && translationArr.length > 0)) {
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
            if (!getCanvas().isStageView()) {
                line = line.trim();
            }
            if (capitaliseFirst && QueleaProperties.get().checkCapitalFirst()) {
                line = Utils.capitaliseFirst(line);
            }
            sections.add(line);
        }
        return sections;
    }

    /**
     * Determine if the given line contains the given string in the middle 80%
     * of the line.
     * <p/>
     * @param line the line to check.
     * @param str the string to use.
     * @return true if the line contains the delimiter, false otherwise.
     */
    private static boolean containsNotAtEnd(String line, String str) {
        final int percentage = 80;
        int removeChars = (int) ((double) line.length() * ((double) (100 - percentage) / 100));
        return line.substring(removeChars, line.length() - removeChars).contains(str);
    }

    /**
     * Split a string with the given delimiter into two parts, using the
     * delimiter closest to the middle of the string.
     * <p/>
     * @param line the line to split.
     * @param delimiter the delimiter.
     * @return an array containing two strings split in the middle by the
     * delimiter.
     */
    private static String[] splitMiddle(String line, char delimiter) {
        final int middle = (int) (((double) line.length() / 2) + 0.5);
        int nearestIndex = -1;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == delimiter) {
                int curDistance = Math.abs(nearestIndex - middle);
                int newDistance = Math.abs(i - middle);
                if (newDistance < curDistance || nearestIndex < 0) {
                    nearestIndex = i;
                }
            }
        }
        return new String[]{line.substring(0, nearestIndex + 1), line.substring(nearestIndex + 1, line.length())};
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
            if (getCanvas().isStageView() && QueleaProperties.get().getShowChords()) {
                textArr = section.getText(true, false);
            } else {
                textArr = section.getText(false, false);
            }
            List<LyricLine> processedText;
            if (displayable instanceof BiblePassage) {
                processedText = dumbWrapText(textArr);
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
            double newSize = pickFontSize(font, processedText, getCanvas().getWidth() * 0.9, getCanvas().getHeight() * 0.9);
            if (newSize < fontSize) {
                fontSize = newSize;
            }
        }
        if (fontSize == Double.POSITIVE_INFINITY) {
            fontSize = -1;
        }
        return fontSize;
    }

    public void setText(TextDisplayable displayable, int index) {
        boolean fade = curDisplayable != displayable;
        double uniformFontSize = getUniformFontSize(displayable);
        curDisplayable = displayable;
        String[] bigText;
        if (getCanvas().isStageView() && QueleaProperties.get().getShowChords()) {
            bigText = displayable.getSections()[index].getText(true, false);
        } else {
            bigText = displayable.getSections()[index].getText(false, false);
        }
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
        this.smallText = smallText;
        draw(curDisplayable, fontSize);
    }

    /**
     * Erase all the text on the getCanvas().
     */
    public void eraseText() {
        setText(null, null, null, true, -1);
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
        drawText(fontSize, displayable instanceof BiblePassage, displayable instanceof BiblePassage);
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
        setTheme(ThemeDTO.DEFAULT_THEME, bible);
        eraseText();
    }

    private double pickSmallFontSize(Font font, String[] text, double width, double height) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        ArrayList<String> al = new ArrayList<String>();
        for (String te : text) {
            if (al.contains("\n")) {
                String[] te2 = te.split("\n");
                for (String te3 : te2) {
                    al.add(te3);
                }
            } else {
                al.add(te);
            }
        }
        double totalHeight = ((metrics.getLineHeight() + getLineSpacing()) * al.size());
        while (totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + getLineSpacing()) * al.size();
        }

        String longestLine = longestLine(font, al);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > width) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalWidth = metrics.computeStringWidth(longestLine);
        }

        return font.getSize();
    }
}
