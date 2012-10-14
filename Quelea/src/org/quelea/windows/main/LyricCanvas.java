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
package org.quelea.windows.main;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.TextDisplayable;
import org.quelea.notice.NoticeDrawer;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The canvas where the lyrics / images / media are drawn.
 * <p/>
 * @author Michael
 */
public class LyricCanvas extends StackPane {

    private Theme theme;
    private String[] text;
    private String[] smallText;
    private boolean fadeText;
    private Paint lastColor;
    private boolean cleared;
    private boolean blacked;
    private boolean capitaliseFirst;
    private NoticeDrawer noticeDrawer;
    private boolean stageView;
    private Group textGroup;
    private TextDisplayable curDisplayable;
    private int curIndex;
    private ImageView background;
    private ImageView blackImg;

    /**
     * Create a new canvas where the lyrics should be displayed.
     * <p/>
     * @param showBorder true if the border should be shown around any text
     * (only if the options say so) false otherwise.
     */
    public LyricCanvas(boolean showBorder, boolean stageView) {
        setMinHeight(0);
        setMinWidth(0);
        this.stageView = stageView;
        blackImg = new ImageView(Utils.getImageFromColour(Color.BLACK));
        noticeDrawer = new NoticeDrawer(this);
        text = new String[]{};
        theme = Theme.DEFAULT_THEME;
        textGroup = new Group();
        background = getNewImageView();
        getChildren().add(0, background);
        getChildren().add(textGroup);
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                update();
            }
        });
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                update();
            }
        });
    }

    private ImageView getNewImageView() {
        ImageView ret = new ImageView(Utils.getImageFromColour(Color.BLACK));
        ret.setFitHeight(getHeight());
        ret.setFitWidth(getWidth());
        StackPane.setAlignment(ret, Pos.CENTER);
        return ret;
    }

    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                drawText();
                if(blacked) {
                    if(getChildren().contains(background)) {
                        getChildren().add(0, blackImg);
                        getChildren().remove(background);
                    }
                }
                else {
                    if(!getChildren().contains(background)) {
                        getChildren().remove(blackImg);
                        getChildren().add(0, background);
                    }
                }
                background.setFitHeight(getHeight());
                background.setFitWidth(getWidth());
                blackImg.setFitHeight(getHeight());
                blackImg.setFitWidth(getWidth());
            }
        });
    }

    private void drawText() {
        if(cleared||blacked) {
            textGroup.getChildren().clear();
            return;
        }
        Font font = theme.getFont();
        List<String> newText = sanctifyText();
        double fontSize = pickFontSize(font, newText, getWidth(), getHeight());
        font = Font.font(font.getName(), fontSize);
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        int y = 0;
        final Group newTextGroup = new Group();
        final Group oldTextGroup = textGroup;
//        newTextGroup.setOpacity(0);
        getChildren().add(newTextGroup);
        getChildren().remove(oldTextGroup);

        ParallelTransition paintTransition = new ParallelTransition();
        for(String line : newText) {
            Text t = new Text(line);
            double width = metrics.computeStringWidth(line);
            double centreOffset = (getWidth() - width) / 2;
            t.setFont(font);
            t.setX(centreOffset);
            t.setY(y);
            if(theme.getFontPaint() == lastColor || lastColor == null) {
                t.setFill(theme.getFontPaint());
            }
            else {
                Timeline paintTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(t.fillProperty(), lastColor)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(t.fillProperty(), theme.getFontPaint())));
                paintTransition.getChildren().add(paintTimeline);
            }
            y += metrics.getLineHeight();
            newTextGroup.getChildren().add(t);
        }
        if(!paintTransition.getChildren().isEmpty()) {
            paintTransition.play();
        }
        lastColor = theme.getFontPaint();

//        double fadeTime;
//        if(fadeText) {
//            fadeTime = 0.5;
//        }
//        else {
//            fadeTime = 0.01;
//        }

//        ParallelTransition fadeTransition = new ParallelTransition();
//        Timeline fadeOutTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(oldTextGroup.opacityProperty(), 1)),
//                new KeyFrame(Duration.seconds(fadeTime), new KeyValue(oldTextGroup.opacityProperty(), 0)));
//        fadeTransition.getChildren().add(fadeOutTimeline);
//        Timeline fadeInTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(newTextGroup.opacityProperty(), 0)),
//                new KeyFrame(Duration.seconds(fadeTime), new KeyValue(newTextGroup.opacityProperty(), 1)));
//        fadeTransition.getChildren().add(fadeInTimeline);
//
//        fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent t) {
//                getChildren().remove(oldTextGroup);
//            }
//        });
        textGroup = newTextGroup;
//
//        fadeTransition.play();
    }

    private double pickFontSize(Font font, List<String> text, double width, double height) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        double totalHeight = (metrics.getLineHeight()) * text.size();
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
     * Determine if this canvas is part of a stage view.
     * <p/>
     * @return true if its a stage view, false otherwise.
     */
    public boolean isStageView() {
        return stageView;
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
     * Take the raw text and format it into a number of lines nicely, where the
     * lines aren't more than the maximum length.
     * <p/>
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<String> sanctifyText() {
        List<String> ret = new ArrayList<>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for(String line : text) {
            if(stageView) {
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
//            else if(containsNotAtEnd(line, "-")) {
//                for(String s : splitMiddle(line, '-')) {
//                    sections.addAll(splitLine(s, maxLength));
//                }
//            }
            else {
                sections.addAll(splitLine(new StringBuilder(line).insert(line.length() / 2, "-").toString(), maxLength));
            }
        }
        else {
            if(!stageView) {
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
     * Toggle the clearing of this canvas - still leave the background image in
     * place but remove all the text.
     */
    public void toggleClear() {
        cleared ^= true; //invert
        update();
    }

    /**
     * Determine whether this canvas is cleared.
     * <p/>
     * @return true if the canvas is cleared, false otherwise.
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Toggle the blacking of this canvas - remove the text and background image
     * (if any) just displaying a black screen.
     */
    public void toggleBlack() {
        blacked ^= true; //invert
        update();
    }

    /**
     * Determine whether this canvas is blacked.
     * <p/>
     * @return true if the canvas is blacked, false otherwise.
     */
    public boolean isBlacked() {
        return blacked;
    }

    /**
     * Set the theme of this canvas.
     * <p/>
     * @param theme the theme to place on the canvas.
     */
    public void setTheme(Theme theme) {
        if(theme == null) {
            theme = Theme.DEFAULT_THEME;
        }
        if(this.theme == theme) {
            return;
        }
        this.theme = theme;
        Image image = theme.getBackground().getImage();
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), background);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        final ImageView newBackground = getNewImageView();
        newBackground.setFitHeight(getHeight());
        newBackground.setFitWidth(getWidth());
        newBackground.setImage(image);
        getChildren().add(0, newBackground);
        final ImageView oldBackground = background;
        fadeOut.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                getChildren().remove(oldBackground);
            }
        });
        background = newBackground;
        fadeOut.play();
        update();
    }

    /**
     * Get the theme currently in use on the canvas.
     * <p/>
     * @return the current theme
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Erase all the text on the canvas.
     */
    public void eraseText() {
        setText(null, null, true);
    }

    public void setText(TextDisplayable displayable, int index) {
        boolean fade;
        if(curDisplayable == displayable) {
            if(curIndex == index) {
                return;
            }
            fade = false;
        }
        else {
            fade = true;
        }
        curDisplayable = displayable;
        curIndex = index;
        setText(displayable.getSections()[index].getText(false, false), displayable.getSections()[index].getSmallText(), fade);
    }

    /**
     * Set the text to appear on the canvas. The lines will be automatically
     * wrapped and if the text is too large to fit on the screen in the current
     * font, the size will be decreased until all the text fits.
     * <p/>
     * @param text an array of the lines to display on the canvas, one entry in
     * the array is one line.
     * @param smallText an array of the small lines to be displayed on the
     * canvas.
     */
    public void setText(String[] text, String[] smallText, boolean fade) {
        if(text == null) {
            text = new String[0];
        }
        if(smallText == null) {
            smallText = new String[0];
        }
        this.smallText = Arrays.copyOf(smallText, smallText.length);
        this.text = Arrays.copyOf(text, text.length);
        this.fadeText = fade;
        update();
    }

    /**
     * Get the text currently set to appear on the canvas. The text may or may
     * not be shown depending on whether the canvas is blacked or cleared.
     * <p/>
     * @return the current text.
     */
    public String[] getText() {
        return Arrays.copyOf(text, text.length);
    }

    /**
     * Get the notice drawer, used for drawing notices onto this lyrics canvas.
     * <p/>
     * @return the notice drawer.
     */
    public NoticeDrawer getNoticeDrawer() {
        return noticeDrawer;
    }

    /**
     * Testing stuff, nothing to see here...
     * <p/>
     * @param args command line args
     */
    public static void main(String[] args) {
        new JFXPanel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LyricCanvas canvas = new LyricCanvas(false, false);
                Stage stage = new Stage();
                stage.setWidth(800);
                stage.setHeight(600);
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent t) {
                        System.exit(0);
                    }
                });
                stage.setScene(new Scene(canvas));
                stage.show();

                canvas.setText(new String[]{"Line 1", "line 2", "BLAHBLAH BLAH BLAH"},
                        new String[]{"Tim Hughes", "CCLI number 1469714", "Another line"}, true);

                canvas.setTheme(new Theme(Theme.DEFAULT_FONT, Color.WHITE, new Background(Color.BLUE)));
            }
        });

    }
}
