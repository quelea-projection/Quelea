package org.quelea.windows.timer;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.quelea.data.ImageBackground;
import org.quelea.data.ColourBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePreviewPanel;
import org.quelea.windows.main.QueleaApp;
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
    private StackPane stack;

    public TimerDrawer(TimerControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        TimerDisplayable td = (TimerDisplayable) displayable;
        timer = new Timer(td.getSeconds(), td.getPretext(), td.getPosttext());
        timer.setTheme(td.getTheme());
        if (getCanvas().isStageView()) {
            td.addStageDrawer(this);
            controlPanel.setStageTimer(timer);
            timer.setFill(QueleaProperties.get().getStageLyricsColor());
            timer.setEffect(null);
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
            getCanvas().getChildren().add(timer);
        } else {
            td.addDrawer(this);
            controlPanel.setTimer(timer, td.getBackground() instanceof VideoBackground);
            stack = new StackPane();
            StackPane.setAlignment(timer, timer.getTextPosition());

            if (td.getBackground() instanceof VideoBackground) {
                if (playVideo) {
                    controlPanel.reset();
                    controlPanel.loadMultimedia(((VideoBackground) td.getBackground()).getVLCVidString(),
                            ((VideoBackground) td.getTheme().getBackground()).getStretch());
                    VLCWindow.INSTANCE.refreshPosition();
                    VLCWindow.INSTANCE.show();
                }
            } else if (td.getBackground() instanceof ImageBackground) {
                ImageView imageView = getCanvas().getNewImageView();
                imageView.setImage(((ImageBackground) td.getBackground()).getImage());
                getCanvas().getChildren().add(0, imageView);
            } else if (td.getBackground() instanceof ColourBackground) {
                ImageView imageView = getCanvas().getNewImageView();
                imageView.setImage(Utils.getImageFromColour(((ColourBackground) td.getBackground()).getColour()));
                getCanvas().getChildren().add(0, imageView);
            } else {
                // New background type?
            }
            controlPanel.reset();

            stack.getChildren().add(timer);
            getCanvas().getChildren().add(stack);
        }
        timer.setFontSize(pickFontSize(td.getTheme().getFont()));
        timer.toFront();
        timer.play();
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }

    private double pickFontSize(Font font) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
        String text = timer.toString();
        String[] splitText = text.split("\n");
        double lineSpacing = QueleaProperties.get().getAdditionalLineSpacing() * getCanvas().getHeight() / 1000.0;

        double totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        while (totalHeight > getCanvas().getHeight() * 0.8) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + lineSpacing * splitText.length);
        }

        String longestLine = longestLine(font, splitText);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > getCanvas().getWidth() * 0.8) {
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
        timer.setTheme(theme);
        if (getCanvas().isStageView()) {
            timer.setFill(QueleaProperties.get().getStageLyricsColor());
            timer.setEffect(null);
        }
        timer.setFontSize(pickFontSize(theme.getFont()));
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            getCanvas().clearNonPermanentChildren();
        }
    }
}
