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

    private final MultimediaControls controlPanel;
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
        else {
            if (playVideo) {
                controlPanel.reset();
                controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getLocation());
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
            }
        }
    }
    
    public void setVisible(boolean visible) {
        VLCWindow.INSTANCE.setWindowVisible(visible);
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
