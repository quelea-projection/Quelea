package org.quelea.windows.lyrics;

import org.quelea.windows.main.DisplayableDrawer;
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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Responsible for drawing lyircs and their background.
 * <p/>
 * @author Ben Goodwin, tomaszpio@gmail.com, Michael
 */
public class LyricDrawer extends DisplayableDrawer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String[] text;
    private Group textGroup;
    private ThemeDTO theme;
    private TextDisplayable curDisplayable;
    private boolean capitaliseFirst;
    private Map<DisplayCanvas, Boolean> lastClearedState;

    public LyricDrawer() {
        text = new String[]{};
        theme = ThemeDTO.DEFAULT_THEME;
        textGroup = new Group();
        lastClearedState = new HashMap<>();
    }

    private void drawText(double defaultFontSize) {
        boolean stageView = getCanvas().isStageView();
        if(getCanvas().getCanvasBackground() != null) {
            if(!getCanvas().getChildren().contains(getCanvas().getCanvasBackground())
                    && !getCanvas().getChildren().contains(textGroup)) {
                getCanvas().getChildren().add(0, getCanvas().getCanvasBackground());
                getCanvas().getChildren().add(textGroup);
            }
        }
        Font font = Font.font(theme.getFont().getFamily(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        if(stageView) {
            font = Font.font(QueleaProperties.get().getStageTextFont(), QueleaProperties.get().getMaxFontSize());
        }
        if(font == null) {
            font = ThemeDTO.DEFAULT_FONT.getFont();
        }
        DropShadow shadow = theme.getShadow().getDropShadow();
        if(stageView) {
            shadow = new DropShadow();
        }
        if(shadow == null) {
            shadow = ThemeDTO.DEFAULT_SHADOW.getDropShadow();
        }

        shadow.setHeight(5);
        shadow.setWidth(5);

        List<String> newText = sanctifyText(text);
        double fontSize;
        if(defaultFontSize > 0) {
            fontSize = defaultFontSize;
        }
        else {
            fontSize = pickFontSize(font, newText, getCanvas().getWidth() * 0.9, getCanvas().getHeight() * 0.9);
        }
        if(!stageView) {
            font = Font.font(font.getFamily(),
                    theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSize);
        }
        else {
            font = Font.font(font.getFamily(), FontWeight.NORMAL,
                    FontPosture.REGULAR, fontSize);
        }
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        int y = 0;
        final Group newTextGroup = new Group();
        StackPane.setAlignment(newTextGroup, QueleaProperties.get().getTextPositionInternal().getLayoutPos());

        for(Iterator< Node> it = getCanvas().getChildren().iterator(); it.hasNext();) {
            Node node = it.next();
            if(node instanceof Group) {
                it.remove();
            }
        }

        getCanvas().getChildren().add(newTextGroup);
        getCanvas().pushLogoNoticeToFront();

        ParallelTransition paintTransition = new ParallelTransition();
        for(String line : newText) {
            Text t;
            t = new Text(line);

            double width = metrics.computeStringWidth(line);
            double centreOffset = (getCanvas().getWidth() - width) / 2;

            t.setFont(font);
            t.setEffect(shadow);
            if(stageView && QueleaProperties.get().getStageTextAlignment().equalsIgnoreCase("Left")) {
                t.setX(getCanvas().getWidth());
            }
            else {
                t.setX(centreOffset);
            }
            t.setY(y);

            Color lineColor;
            if(stageView && new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                lineColor = QueleaProperties.get().getStageChordColor();
            }
            else if(stageView) {
                lineColor = QueleaProperties.get().getStageLyricsColor();
            }
            else {
                lineColor = theme.getFontPaint();
            }
            if(lineColor == null) {
                LOGGER.log(Level.WARNING, "Warning: Font Color not initialised correctly. Using default font colour.");
                lineColor = ThemeDTO.DEFAULT_FONT_COLOR;
            }
            t.setFill(lineColor);
            y += metrics.getLineHeight() + getLineSpacing();

            newTextGroup.getChildren().add(t);
        }
        if(!paintTransition.getChildren().isEmpty()) {
            paintTransition.play();
        }
        textGroup = newTextGroup;
        if(getCanvas().isCleared() && !getLastClearedState()) {
            setLastClearedState(true);
            FadeTransition t = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), textGroup);
            t.setToValue(0);
            t.play();
        }
        else if(getCanvas().isCleared()) {
            textGroup.setOpacity(0);
        }
        else if(!getCanvas().isCleared() && getLastClearedState()) {
            setLastClearedState(false);
            FadeTransition t = new FadeTransition(Duration.seconds(QueleaProperties.get().getFadeDuration()), textGroup);
            t.setFromValue(0);
            t.setToValue(1);
            t.play();
        }
    }

    private boolean getLastClearedState() {
        Boolean val = lastClearedState.get(getCanvas());
        if(val == null) {
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
    public void setTheme(ThemeDTO theme) {
        if(theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        boolean sameVid = false;
        if(theme.getBackground() instanceof VideoBackground
                && VLCWindow.INSTANCE.getLastLocation() != null) {
            String newLocation = ((VideoBackground) theme.getBackground()).getVideoFile().getAbsolutePath();
            String oldLocation = VLCWindow.INSTANCE.getLastLocation();
            if(newLocation.equals(oldLocation)) {
                sameVid = true;
            }
        }
        this.theme = theme;
        Image image;
        if(getCanvas().isStageView()) {
            image = Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor());
        }
        else if(theme.getBackground() instanceof ImageBackground) {
            image = ((ImageBackground) theme.getBackground()).getImage();
        }
        else if(theme.getBackground() instanceof ColourBackground) {
            Color color = ((ColourBackground) theme.getBackground()).getColour();
            image = Utils.getImageFromColour(color);
        }
        else if(theme.getBackground() instanceof VideoBackground && getCanvas().getPlayVideo()) {
            image = null;
        }
        else if(theme.getBackground() instanceof VideoBackground) {
            image = Utils.getVidBlankImage(((VideoBackground) theme.getBackground()).getVideoFile());
        }
        else {
            LOGGER.log(Level.SEVERE, "Bug: Unhandled theme background case, trying to use default background: " + theme.getBackground(), new RuntimeException("DEBUG EXCEPTION FOR STACK TRACE"));
            image = Utils.getImageFromColour(ThemeDTO.DEFAULT_BACKGROUND.getColour());
        }
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), getCanvas().getCanvasBackground());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        Node newBackground;
        if(image == null) {
            if(!sameVid || !VLCWindow.INSTANCE.isPlaying()) {
                final String location = ((VideoBackground) theme.getBackground()).getVideoFile().getAbsolutePath();
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
                VLCWindow.INSTANCE.setRepeat(true);
                VLCWindow.INSTANCE.play(location);
            }
            newBackground = null; //transparent
        }
        else {
            if(getCanvas().getPlayVideo() && !(theme.getBackground() instanceof VideoBackground)) {
                VLCWindow.INSTANCE.stop();
            }
            final ImageView newImageView = getCanvas().getNewImageView();
            newImageView.setFitHeight(getCanvas().getHeight());
            newImageView.setFitWidth(getCanvas().getWidth());
            newImageView.setImage(image);
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

    private double pickFontSize(Font font, List<String> text, double width, double height) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double totalHeight = ((metrics.getLineHeight() + getLineSpacing()) * text.size());
        while(totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if(font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + getLineSpacing()) * text.size();
        }

        String longestLine = longestLine(font, text);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while(totalWidth > width) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if(font.getSize() < 1) {
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

    private String longestLine(Font font, List<String> text) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double longestWidth = -1;
        String longestStr = null;
        for(String line : text) {
            double width = metrics.computeStringWidth(line);
            if(width > longestWidth) {
                longestWidth = width;
                longestStr = line;
            }
        }
        return longestStr;
    }

    /**
     * Take the raw text and format it into a number of lines nicely, where the
     * lines aren't more than the maximum length.
     * <p/>
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<String> sanctifyText(String[] lines) {
        List<String> ret = new ArrayList<>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for(String line : lines) {
            if(getCanvas().isStageView()) {
                ret.add(line);
            }
            else {
                ret.addAll(splitLine(line, maxLength));
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
        if(line.length() > maxLength) {
            if(containsNotAtEnd(line, ";")) {
                for(String s : splitMiddle(line, ';')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            }
            else if(containsNotAtEnd(line, ",")) {
                for(String s : splitMiddle(line, ',')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            }
            else if(containsNotAtEnd(line, " ")) {
                for(String s : splitMiddle(line, ' ')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            }
            else {
                sections.addAll(splitLine(new StringBuilder(line).insert(line.length() / 2, "-").toString(), maxLength));
            }
        }
        else {
            if(!getCanvas().isStageView()) {
                line = line.trim();
            }
            if(capitaliseFirst && QueleaProperties.get().checkCapitalFirst()) {
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
        for(int i = 0; i < line.length(); i++) {
            if(line.charAt(i) == delimiter) {
                int curDistance = Math.abs(nearestIndex - middle);
                int newDistance = Math.abs(i - middle);
                if(newDistance < curDistance || nearestIndex < 0) {
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
        if(!QueleaProperties.get().getUseUniformFontSize()) {
            return -1;
        }
        Font font = theme.getFont();
        font = Font.font(font.getName(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                QueleaProperties.get().getMaxFontSize());
        double fontSize = Double.POSITIVE_INFINITY;
        for(int i = 0; i < displayable.getSections().length; i++) {
            String[] text;
            if(getCanvas().isStageView()) {
                text = displayable.getSections()[i].getText(true, false);
            }
            else {
                text = displayable.getSections()[i].getText(false, false);
            }
            double newSize = pickFontSize(font, sanctifyText(text), getCanvas().getWidth() * 0.9, getCanvas().getHeight() * 0.9);
            if(newSize < fontSize) {
                fontSize = newSize;
            }
        }
        if(fontSize == Double.POSITIVE_INFINITY) {
            fontSize = -1;
        }
        return fontSize;
    }

    public void setText(TextDisplayable displayable, int index) {
        boolean fade;
        if(curDisplayable == displayable) {
            fade = false;
        }
        else {
            fade = true;
        }
        double uniformFontSize = getUniformFontSize(displayable);
        curDisplayable = displayable;
        String[] bigText;
        if(getCanvas().isStageView()) {
            bigText = displayable.getSections()[index].getText(true, false);
        }
        else {
            bigText = displayable.getSections()[index].getText(false, false);
        }
        setText(bigText, displayable.getSections()[index].getSmallText(), fade, uniformFontSize);
    }

    /**
     * Set the text to appear on the getCanvas(). The lines will be
     * automatically wrapped and if the text is too large to fit on the screen
     * in the current font, the size will be decreased until all the text fits.
     * <p/>
     * @param text an array of the lines to display on the canvas, one entry in
     * the array is one line.
     * @param smallText an array of the small lines to be displayed on the
     * getCanvas().
     */
    public void setText(String[] text, String[] smallText, boolean fade, double fontSize) {
        if(text == null) {
            text = new String[0];
        }
        if(smallText == null) {
            smallText = new String[0];
        }
        this.text = Arrays.copyOf(text, text.length);
        draw(curDisplayable, fontSize);
    }

    /**
     * Erase all the text on the getCanvas().
     */
    public void eraseText() {
        setText(null, null, true, -1);
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
        drawText(fontSize);
        if(getCanvas().getCanvasBackground() instanceof ImageView) {
            ImageView imgBackground = (ImageView) getCanvas().getCanvasBackground();
            imgBackground.setFitHeight(getCanvas().getHeight());
            imgBackground.setFitWidth(getCanvas().getWidth());
        }
        else if(getCanvas().getCanvasBackground() != null) {
            LOGGER.log(Level.WARNING, "BUG: Unrecognised image background - " + getCanvas().getCanvasBackground().getClass(), new RuntimeException("DEBUG EXCEPTION"));
        }
    }

    @Override
    public void clear() {
        if(getCanvas().getChildren() != null) {
            getCanvas().clearNonPermanentChildren();
        }
        setTheme(ThemeDTO.DEFAULT_THEME);
        eraseText();
    }
}
