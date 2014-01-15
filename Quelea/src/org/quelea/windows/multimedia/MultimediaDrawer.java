package org.quelea.windows.multimedia;

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

    private MultimediaControls controlPanel;
    private boolean playVideo;

    public MultimediaDrawer(MultimediaControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        if(getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        }
        /*else if(getCanvas().isLogoShowing()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(QueleaProperties.get().getLogoImage().getImage());
            getCanvas().getChildren().add(0, imageView);
            imageView.toFront();
        }*/
        else {
            if (playVideo) {
                controlPanel.reset();
                controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getFile().getAbsolutePath());
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
            }
        }
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            getCanvas().setOpacity(0);
        } else {
            getCanvas().setOpacity(1);
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }
}
