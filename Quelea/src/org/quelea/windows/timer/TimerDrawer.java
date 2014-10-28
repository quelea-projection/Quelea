package org.quelea.windows.timer;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
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
    private StackPane stack;

    public TimerDrawer(TimerControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        if (getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        } else {
            TimerDisplayable td = (TimerDisplayable) displayable;
            timer = new Timer(td.getSeconds());
            stack = new StackPane();
            stack.getChildren().add(timer);
            StackPane.setAlignment(timer, td.getTextPosition());
            getCanvas().getChildren().add(stack);
            timer.toFront();
            controlPanel.setTimer(timer);
            if (playVideo) {
                controlPanel.reset();
                controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getLocation());
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
                timer.play();
            }
        }
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            getCanvas().clearNonPermanentChildren();
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }
}
