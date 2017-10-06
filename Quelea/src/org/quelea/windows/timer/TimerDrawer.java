package org.quelea.windows.timer;

import java.util.Calendar;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import static java.lang.Math.abs;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
import org.quelea.windows.multimedia.VLCWindow;

/**
 *
 * @author tomaszpio@gmail.com, Michael, Ben
 */
public class TimerDrawer extends DisplayableDrawer {

    private final TimerControls controlPanel;
    private boolean playVideo;
    private Timer timer;
    private Timer stageTimer;
    private DisplayCanvas main;
    private DisplayCanvas stage;
    private StackPane stack;

    public TimerDrawer(TimerControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        TimerDisplayable td = (TimerDisplayable) displayable;
        td.addDrawer(this);
        int seconds = td.getSeconds();
        if (seconds == -1) {
            Calendar now = Calendar.getInstance();
            seconds = (int) ((now.getTimeInMillis() - td.getTimeToFinish().getTimeInMillis()) / -1000);
            if (seconds < 0) {
                seconds = 0;
            }
        }

        if (getCanvas().isStageView()) {
            stage = getCanvas();
            stageTimer = new Timer(seconds, td.getPretext(), td.getPosttext());
            stageTimer.setTheme(td.getTheme());
            controlPanel.setStageTimer(stageTimer);
            stageTimer.setFill(QueleaProperties.get().getStageLyricsColor());
            stageTimer.setEffect(null);
            ImageView imageView = stage.getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            stage.getChildren().add(0, imageView);
            stage.getChildren().add(stageTimer);
            stageTimer.setFontSize(pickFontSize(td.getTheme().getFont(), stageTimer, stage));
            stageTimer.toFront();
        } else {
            main = getCanvas();
            timer = new Timer(seconds, td.getPretext(), td.getPosttext());
            timer.setTheme(td.getTheme());
            controlPanel.setTimer(timer, td.getTheme().getBackground() instanceof VideoBackground);
            stack = new StackPane();
            StackPane.setAlignment(timer, timer.getTextPosition());

            if (td.getTheme().getBackground() instanceof VideoBackground) {
                if (playVideo) {
                    controlPanel.loadMultimedia(((VideoBackground) td.getTheme().getBackground()).getVLCVidString(),
                            ((VideoBackground) td.getTheme().getBackground()).getStretch());
                    VLCWindow.INSTANCE.refreshPosition();
                    VLCWindow.INSTANCE.show();
                    controlPanel.reset();
                }
            } else if (td.getTheme().getBackground() instanceof ImageBackground) {
                ImageView imageView = main.getNewImageView();
                imageView.setImage(((ImageBackground) td.getTheme().getBackground()).getImage());
                main.getChildren().add(0, imageView);
            } else if (td.getTheme().getBackground() instanceof ColourBackground) {
                ImageView imageView = main.getNewImageView();
                imageView.setImage(Utils.getImageFromColour(((ColourBackground) td.getTheme().getBackground()).getColour()));
                main.getChildren().add(0, imageView);
            } else {
                // New background type?
            }
            stack.getChildren().add(timer);
            main.getChildren().add(stack);
            timer.setFontSize(pickFontSize(td.getTheme().getFont(), timer, main));
            timer.toFront();
        }
    }

    @Override
    public void clear() {
        getCanvas().clearCurrentDisplayable();
    }

    @Override
    public void requestFocus() {
    }

    private double pickFontSize(Font font, Timer timer, DisplayCanvas canvas) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        String text = timer.toString();
        String[] splitText = text.split("\n");
        double lineSpacing = QueleaProperties.get().getAdditionalLineSpacing() * canvas.getHeight() / 1000.0;

        double totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        while (totalHeight > canvas.getHeight() * 0.8) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        }

        String longestLine = longestLine(font, splitText);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > canvas.getWidth() * 0.8) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalWidth = metrics.computeStringWidth(longestLine);
        }

        return font.getSize();
    }

    private String longestLine(Font font, String[] text) {
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

    public void setTheme(ThemeDTO theme) {
        boolean sync = false;
        if (stageTimer != null) {
            stageTimer.setTheme(theme);
            stageTimer.setFill(QueleaProperties.get().getStageLyricsColor());
            stageTimer.setEffect(null);
            stageTimer.setFontSize(pickFontSize(theme.getFont(), stageTimer, stage));
            stageTimer.toFront();
        }
        if (timer != null) {
            timer.setTheme(theme);
            timer.setFontSize(pickFontSize(theme.getFont(), timer, main));
            main.clearNonPermanentChildren();
            Background back = theme.getBackground();
            if (back instanceof VideoBackground) {
                setPlayVideo(main.getPlayVideo());
                controlPanel.loadMultimedia(((VideoBackground) back).getVLCVidString(),
                        ((VideoBackground) back).getStretch());
                VLCWindow.INSTANCE.show();
                controlPanel.setTimer(timer, true);
                if (stageTimer != null) {
                    sync = true;
                }
            } else if (back instanceof ImageBackground) {
                ImageView imageView = main.getNewImageView();
                imageView.setImage(((ImageBackground) back).getImage());
                main.getChildren().add(0, imageView);
            } else if (back instanceof ColourBackground) {
                ImageView imageView = main.getNewImageView();
                imageView.setImage(Utils.getImageFromColour(((ColourBackground) back).getColour()));
                main.getChildren().add(0, imageView);
            } else {
                // New background type?
            }
            timer.toFront();
        }
        if (sync) {
            timer.synchronise(stageTimer);
        }
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            if (main != null) {
                main.clearNonPermanentChildren();
            }
            if (stage != null) {
                stage.clearNonPermanentChildren();
            }
        }
    }
}
