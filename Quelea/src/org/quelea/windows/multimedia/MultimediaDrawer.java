package org.quelea.windows.multimedia;

import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
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
        if(playVideo) {
            controlPanel.reset();
            controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getFile().getAbsolutePath());
            VLCWindow.INSTANCE.refreshPosition();
            VLCWindow.INSTANCE.show();
        }
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if(playVideo) {
            getCanvas().setOpacity(0);
        }
        else {
            getCanvas().setOpacity(1);
        }
        controlPanel.setDisableControls(!playVideo);
    }

    @Override
    public void clear() {
        if(getCanvas().getChildren() != null) {
            getCanvas().clearApartFromNotice();
        }
    }

    @Override
    public void requestFocus() {
    }
}
