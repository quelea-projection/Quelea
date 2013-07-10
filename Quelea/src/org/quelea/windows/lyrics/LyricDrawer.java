package org.quelea.windows.lyrics;

import org.quelea.windows.main.DisplayableDrawer;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.data.Background;
import org.quelea.data.ColourBackground;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.multimedia.MediaPlayerFactory;

/**
 * @author tomaszpio@gmail.com
 */
public class LyricDrawer extends DisplayableDrawer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String[] text;
    private Group textGroup;
    private Paint lastColor;
    private ThemeDTO theme;
    private TextDisplayable curDisplayable;
    private boolean capitaliseFirst;
    private boolean showVideoControls = false;
    private MediaView newVideo = null;
    private MediaPlayer player = null;
    private BorderPane buttonsPanel = null;

    public LyricDrawer(boolean showVideoControls, BorderPane buttonsPanel) {
        this.showVideoControls = showVideoControls;
        this.buttonsPanel = buttonsPanel;
        text = new String[]{};
        theme = ThemeDTO.DEFAULT_THEME;
        textGroup = new Group();

    }

    private void drawText() {
        if(!getCanvas().getChildren().contains(getCanvas().getCanvasBackground())
                && !getCanvas().getChildren().contains(textGroup)) {
            getCanvas().getChildren().add(0, getCanvas().getCanvasBackground());
            getCanvas().getChildren().add(textGroup);
        }
        if(getCanvas().isCleared() || getCanvas().isBlacked()) {
            text = new String[0];
            this.text = Arrays.copyOf(text, text.length);
        }
        Font font = theme.getFont();
        if(font == null) {
            font = ThemeDTO.DEFAULT_FONT.getFont();
        }
        DropShadow shadow = theme.getShadow().getDropShadow();
        if(shadow == null) {
            shadow = ThemeDTO.DEFAULT_SHADOW.getDropShadow();
        }

        shadow.setHeight(5);
        shadow.setWidth(5);

        List<String> newText = sanctifyText();
        double fontSize = pickFontSize(font, newText, getCanvas().getWidth(), getCanvas().getHeight());
        font = Font.font(font.getName(),
                theme.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                theme.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                fontSize);
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

        ParallelTransition paintTransition = new ParallelTransition();
        for(String line : newText) {
            Text t;
            t = new Text(line);

            double width = metrics.computeStringWidth(line);
            double centreOffset = (getCanvas().getWidth() - width) / 2;

            t.setFont(font);
            t.setEffect(shadow);
            t.setX(centreOffset);
            t.setY(y);

            if(theme.getFontPaint()
                    == lastColor || lastColor == null) {
                t.setFill(theme.getFontPaint());
            }
            else {
                Timeline paintTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(t.fillProperty(), lastColor)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(t.fillProperty(), theme.getFontPaint())));
                paintTransition.getChildren().add(paintTimeline);
            }
            y += metrics.getLineHeight();

            newTextGroup.getChildren()
                    .add(t);
        }
        if(!paintTransition.getChildren().isEmpty()) {
            paintTransition.play();
        }
        lastColor = theme.getFontPaint();

        textGroup = newTextGroup;
    }

    /**
     * Set the theme of this getCanvas().
     * <p/>
     * @param theme the theme to place on the getCanvas().
     */
    public void setTheme(ThemeDTO theme) {
        if(theme == null || getCanvas().isBlacked()) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        if(this.getCanvas().getCurrentDisplayable() instanceof SongDisplayable) {
            final SongDisplayable song = (SongDisplayable) this.getCanvas().getCurrentDisplayable();
            if(song != null) {
                final Background sectionThemeBackground = song.getSections()[0].getTheme().getBackground();
                if(theme.getBackground() instanceof VideoBackground
                        && sectionThemeBackground instanceof VideoBackground) {
                    String newLocation = ((VideoBackground) theme.getBackground()).getVideoFile().toURI().toString();

                    String oldLocation = ((VideoBackground) sectionThemeBackground).getVideoFile().toURI().toString();
                    if(newLocation.equals(oldLocation)) {
                        return;
                    }
                }
            }
        }
        this.theme = theme;
        Image image = null;
        if(theme.getBackground() instanceof ImageBackground) {
            image = ((ImageBackground) theme.getBackground()).getImage();
        }
        else if(theme.getBackground() instanceof ColourBackground) {
            Color color = ((ColourBackground) theme.getBackground()).getColour();
            image = Utils.getImageFromColour(color);
        }
        else if(theme.getBackground() instanceof VideoBackground) {
            image = null;
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
            newVideo = new MediaView();
            String location = ((VideoBackground) theme.getBackground()).getVideoFile().toURI().toString();
            try {
                player = MediaPlayerFactory.getInstance(location);
                player.setVolume(0);
                player.setAutoPlay(!getCanvas().getType().equals(DisplayCanvas.Type.PREVIEW));
                player.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
                newVideo.setMediaPlayer(player);
                if(showVideoControls) {
                    HBox buttonPanel = new HBox();
                    Button play = new Button("", new ImageView(new Image("file:icons/play.png", 16, 16, false, true)));
                    play.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent t) {
                            if(player != null) {
                                player.play();
                            }
                        }
                    });
                    Button pause = new Button("", new ImageView(new Image("file:icons/pause.png", 16, 16, false, true)));

                    pause.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent t) {
                            if(player != null) {
                                player.pause();
                            }
                        }
                    });
                    buttonPanel.getChildren().add(play);
                    buttonPanel.getChildren().add(pause);
                    buttonsPanel.getChildren().add(buttonPanel);
                }

                getCanvas().getChildren().add(0, newVideo);
                newBackground = newVideo;
            }
            catch(MediaException ex) {
                return;
                //Don't shout about it at this point.
            }
        }
        else {
            final ImageView newImageVIew = getCanvas().getNewImageView();
            newImageVIew.setFitHeight(getCanvas().getHeight());
            newImageVIew.setFitWidth(getCanvas().getWidth());
            newImageVIew.setImage(image);
            getCanvas().getChildren().add(newImageVIew);
            newBackground = newImageVIew;
        }
        final Node oldBackground = getCanvas().getCanvasBackground();

        fadeOut.setOnFinished(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                getCanvas().getChildren().remove(oldBackground);
            }
        });
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
        double totalHeight = (metrics.getLineHeight() * text.size());
        while(totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if(font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight()) * text.size();
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
    private List<String> sanctifyText() {
        List<String> ret = new ArrayList<>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for(String line : text) {
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

    public void setText(TextDisplayable displayable, int index) {
        boolean fade;
        if(curDisplayable == displayable) {
            fade = false;
        }
        else {
            fade = true;
        }
        curDisplayable = displayable;
        String[] bigText;
        if(getCanvas().isStageView()) {
            bigText = displayable.getSections()[index].getText(true, false);
        }
        else {
            bigText = displayable.getSections()[index].getText(false, false);
        }
        setText(bigText, displayable.getSections()[index].getSmallText(), fade);
    }

    /**
     * Set the text to appear on the getCanvas(). The lines will be automatically
     * wrapped and if the text is too large to fit on the screen in the current
     * font, the size will be decreased until all the text fits.
     * <p/>
     * @param text an array of the lines to display on the canvas, one entry in
     * the array is one line.
     * @param smallText an array of the small lines to be displayed on the
     * getCanvas().
     */
    public void setText(String[] text, String[] smallText, boolean fade) {
        if(text == null) {
            text = new String[0];
        }
        if(smallText == null) {
            smallText = new String[0];
        }
        this.text = Arrays.copyOf(text, text.length);
        draw(curDisplayable);
    }

    /**
     * Erase all the text on the getCanvas().
     */
    public void eraseText() {
        setText(null, null, true);
    }

    /**
     * Get the text currently set to appear on the getCanvas(). The text may or may
     * not be shown depending on whether the canvas is blacked or cleared.
     * <p/>
     * @return the current text.
     */
    public String[] getText() {
        return Arrays.copyOf(text, text.length);
    }

    public void draw(Displayable displayable) {
        drawText();
        if(getCanvas().getCanvasBackground() instanceof ImageView) {
            ImageView imgBackground = (ImageView) getCanvas().getCanvasBackground();
            imgBackground.setFitHeight(getCanvas().getHeight());
            imgBackground.setFitWidth(getCanvas().getWidth());
        }
        else if(getCanvas().getCanvasBackground() instanceof MediaView) {
            MediaView vidBackground = (MediaView) getCanvas().getCanvasBackground();
            vidBackground.setPreserveRatio(false);
            vidBackground.setFitHeight(getCanvas().getHeight());
            vidBackground.setFitWidth(getCanvas().getWidth());
        }
        else {
            LOGGER.log(Level.WARNING, "BUG: Unrecognised image background");
        }
    }

    @Override
    public void clear() {
        if(getCanvas().getChildren() != null) {
            getCanvas().getChildren().clear();
        }
        setTheme(ThemeDTO.DEFAULT_THEME);
        if(player != null) {
            player.stop();
        }
        player = null;
        newVideo = null;
        eraseText();
    }

    public void updateCanvas(Displayable displayable, SelectLyricsList lyricsList, int selectedIndex) {
        curDisplayable = (TextDisplayable) displayable;
        TextSection currentSection = lyricsList.itemsProperty().get().get(selectedIndex);
        if(currentSection.getTempTheme() != null) {
            setTheme(currentSection.getTempTheme());
        }
        else {
            setTheme(currentSection.getTheme());
        }
        setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
        setText(curDisplayable, selectedIndex);
    }
}
