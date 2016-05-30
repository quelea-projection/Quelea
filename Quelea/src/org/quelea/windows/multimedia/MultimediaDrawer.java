package org.quelea.windows.multimedia;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.ImageView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayableDrawer;

/**
 *
 * @author tomaszpio@gmail.com, Michael
 */
public class MultimediaDrawer extends DisplayableDrawer {

    private final MultimediaControls controlPanel;
    private boolean playVideo;

    public MultimediaDrawer(MultimediaControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        if (getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        } else {
            if (!playVideo) {
                controlPanel.reset();
                controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getLocation());
            }
            VLCWindow.INSTANCE.play();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(MultimediaDrawer.class.getName()).log(Level.SEVERE, null, ex);
            }
            VLCWindow.INSTANCE.pause();
            if (playVideo) {
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
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
