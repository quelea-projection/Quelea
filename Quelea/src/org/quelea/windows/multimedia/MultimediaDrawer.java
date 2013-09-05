package org.quelea.windows.multimedia;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.windows.main.DisplayableDrawer;

/**
 *
 * @author tomaszpio@gmail.com
 */
public class MultimediaDrawer extends DisplayableDrawer {

    private MultimediaControlPanel controlPanel;

    public MultimediaDrawer(MultimediaControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        drawDisplayable(displayable);
    }

    @Override
    public void clear() {
//        if(controlPanel.getPlayer() != null) {
//            controlPanel.getPlayer().stop();
//            controlPanel.clear();
//        }
        if(getCanvas().getChildren() != null) {
            getCanvas().clearApartFromNotice();
        }
    }

    @Override
    public void requestFocus() {
    }

    private void drawDisplayable(final Displayable displayable) {
        if(getCanvas().isCleared() || getCanvas().isBlacked()) {
            clear();
        }
        else {
            controlPanel.loadMultimedia((MultimediaDisplayable) displayable);
            VBox pane = new VBox();
//            pane.getChildren().add(controlPanel.getPlayer());

            if(getCanvas() instanceof MultimediaPreviewCanvas) {
                pane.getChildren().add(controlPanel);
            }
            getCanvas().getChildren().add(pane);
        }
    }
}
