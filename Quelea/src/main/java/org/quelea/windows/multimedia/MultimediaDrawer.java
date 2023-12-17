package org.quelea.windows.multimedia;

import javafx.scene.image.ImageView;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.video.VidDisplay;

import java.io.File;

/**
 * @author tomaszpio@gmail.com, Michael
 */
public class MultimediaDrawer extends DisplayableDrawer {

    private final MultimediaControls controlPanel;
    private final VidDisplay vidDisplay;

    public MultimediaDrawer(MultimediaControls controlPanel) {
        this.controlPanel = controlPanel;
        vidDisplay = new VidDisplay();
    }

    @Override
    public void draw(Displayable displayable) {
        ImageView imageView = getCanvas().getNewImageView();
        imageView.imageProperty().bind(vidDisplay.imageProperty());
        imageView.setPreserveRatio(true);
        getCanvas().getChildren().add(0, imageView);

        ImageView background = getCanvas().getNewImageView();
        background.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
        getCanvas().getChildren().add(0, background);

        MultimediaDisplayable multimediaDisplayable = (MultimediaDisplayable) displayable;
        controlPanel.reset();
        vidDisplay.setURI(new File(multimediaDisplayable.getLocation()).toURI());
        controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getLocation());
        controlPanel.setOnPlay(vidDisplay::play);
        controlPanel.setOnPause(vidDisplay::pause);
        controlPanel.setOnStop(vidDisplay::stop);
        controlPanel.setOnSeek(vidDisplay::seek);
        controlPanel.setOnLoopChanged(vidDisplay::setLoop);
        controlPanel.setOnVolumeChanged(vidDisplay::setVolume);

        vidDisplay.setOnPosChanged(controlPanel::setPosition);
        vidDisplay.setOnFinished(controlPanel::reset);
    }

    public void setPlayVideo() {
        getCanvas().clearNonPermanentChildren();
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }
}
