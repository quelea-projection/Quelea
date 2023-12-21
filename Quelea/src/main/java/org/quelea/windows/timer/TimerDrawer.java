package org.quelea.windows.timer;

import java.io.File;
import java.util.Calendar;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.quelea.data.Background;
import org.quelea.data.ImageBackground;
import org.quelea.data.ColourBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.widgets.Timer;
import org.quelea.utils.FXFontMetrics;
import org.quelea.windows.video.VidDisplay;

/**
 * @author tomaszpio@gmail.com, Michael, Ben
 */
public class TimerDrawer extends DisplayableDrawer {

    private final TimerControls controlPanel;
    private Timer timer;
    private Timer stageTimer;
    private DisplayCanvas mainCanvas;
    private DisplayCanvas stageCanvas;
    private StackPane stack;
    private VidDisplay vidDisplay;

    public TimerDrawer(TimerControls controlPanel) {
        this.controlPanel = controlPanel;
        this.vidDisplay = new VidDisplay();
    }

    @Override
    public void draw(Displayable displayable) {
        TimerDisplayable td = (TimerDisplayable) displayable;
        td.setDrawer(this);
        int seconds = td.getSeconds();
        if (seconds == -1) {
            Calendar now = Calendar.getInstance();
            seconds = (int) ((now.getTimeInMillis() - td.getTimeToFinish().getTimeInMillis()) / -1000);
            if (seconds < 0) {
                seconds = 0;
            }
        }

        if (getCanvas().isStageView()) {
            stageCanvas = getCanvas();
            stageTimer = new Timer(seconds, td.getPretext(), td.getPosttext());
            stageTimer.setTheme(td.getTheme());
            controlPanel.addTimer(stageTimer);
            stageTimer.setFill(QueleaProperties.get().getStageLyricsColor());
            stageTimer.setEffect(null);
            ImageView imageView = stageCanvas.getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            stageCanvas.getChildren().add(0, imageView);
            stageCanvas.getChildren().add(stageTimer);
            stageTimer.setFontSize(pickFontSize(td.getTheme().getFont(), stageTimer, stageCanvas));
            stageTimer.toFront();
        } else {
            mainCanvas = getCanvas();
            timer = new Timer(seconds, td.getPretext(), td.getPosttext());
            timer.setTheme(td.getTheme());
            controlPanel.addTimer(timer);
            stack = new StackPane();
            StackPane.setAlignment(timer, timer.getTextPosition());

            if (td.getTheme().getBackground() instanceof VideoBackground) {
                String url = ((VideoBackground) td.getTheme().getBackground()).getVLCVidString();
                controlPanel.loadMultimedia(((VideoBackground) td.getTheme().getBackground()).getVLCVidString(),
                        ((VideoBackground) td.getTheme().getBackground()).getStretch());
                controlPanel.reset();

                ImageView imageView = mainCanvas.getNewImageView();
                imageView.imageProperty().bind(vidDisplay.imageProperty());
                imageView.setPreserveRatio(true);
                vidDisplay.setURI(new File(url).toURI());
                vidDisplay.setLoop(true);
                vidDisplay.play();
                mainCanvas.getChildren().add(0, imageView);
            } else if (td.getTheme().getBackground() instanceof ImageBackground) {
                ImageView imageView = mainCanvas.getNewImageView();
                imageView.setImage(((ImageBackground) td.getTheme().getBackground()).getImage());
                mainCanvas.getChildren().add(0, imageView);
            } else if (td.getTheme().getBackground() instanceof ColourBackground) {
                ImageView imageView = mainCanvas.getNewImageView();
                imageView.setImage(Utils.getImageFromColour(((ColourBackground) td.getTheme().getBackground()).getColour()));
                mainCanvas.getChildren().add(0, imageView);
            } else {
                // New background type?
            }
            stack.getChildren().add(timer);
            mainCanvas.getChildren().add(stack);
            timer.setFontSize(pickFontSize(td.getTheme().getFont(), timer, mainCanvas));
            timer.toFront();
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }

    private double pickFontSize(Font font, Timer timer, DisplayCanvas canvas) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        String text = timer.toString();
        String[] splitText = text.split("\n");
        double lineSpacing = QueleaProperties.get().getAdditionalLineSpacing() * canvas.getHeight() / 1000.0;

        double totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        while (totalHeight > canvas.getHeight() * 0.8) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = new FXFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        }

        String longestLine = longestLine(font, splitText);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > canvas.getWidth() * 0.8) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = new FXFontMetrics(font);
            totalWidth = metrics.computeStringWidth(longestLine);
        }

        return font.getSize();
    }

    private String longestLine(Font font, String[] text) {
        FXFontMetrics metrics = new FXFontMetrics(font);
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

    public void setTheme(ThemeDTO theme) {
        boolean sync = false;
        if (stageTimer != null) {
            stageTimer.setTheme(theme);
            stageTimer.setFill(QueleaProperties.get().getStageLyricsColor());
            stageTimer.setEffect(null);
            stageTimer.setFontSize(pickFontSize(theme.getFont(), stageTimer, stageCanvas));
            stageTimer.toFront();
        }
        if (timer != null) {
            timer.setTheme(theme);
            timer.setFontSize(pickFontSize(theme.getFont(), timer, mainCanvas));
            mainCanvas.clearNonPermanentChildren();
            Background back = theme.getBackground();
            if (back instanceof VideoBackground) {
                controlPanel.loadMultimedia(((VideoBackground) back).getVLCVidString(),
                        ((VideoBackground) back).getStretch());
                controlPanel.addTimer(timer);
                if (stageTimer != null) {
                    sync = true;
                }
            } else if (back instanceof ImageBackground) {
                ImageView imageView = mainCanvas.getNewImageView();
                imageView.setImage(((ImageBackground) back).getImage());
                mainCanvas.getChildren().add(0, imageView);
            } else if (back instanceof ColourBackground) {
                ImageView imageView = mainCanvas.getNewImageView();
                imageView.setImage(Utils.getImageFromColour(((ColourBackground) back).getColour()));
                mainCanvas.getChildren().add(0, imageView);
            } else {
                // New background type?
            }
            timer.toFront();
        }
        if (sync) {
            timer.synchronise(stageTimer);
        }
    }
}
