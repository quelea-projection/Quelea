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
import java.net.URI;
import java.util.Objects;

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
        MultimediaDisplayable multimediaDisplayable = (MultimediaDisplayable) displayable;
        URI uri = new File(multimediaDisplayable.getLocation()).toURI();

        ImageView imageView = getCanvas().getNewImageView();
        imageView.imageProperty().bind(vidDisplay.imageProperty());
        imageView.setPreserveRatio(true);
        getCanvas().getChildren().add(0, imageView);

        ImageView background = getCanvas().getNewImageView();
        background.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
        getCanvas().getChildren().add(0, background);

        if(!Objects.equals(vidDisplay.getUri(), uri)) {
            controlPanel.reset();
            vidDisplay.setURI(uri);
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
