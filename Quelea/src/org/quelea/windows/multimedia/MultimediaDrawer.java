package org.quelea.windows.multimedia;

import javafx.geometry.Insets;
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

    protected MediaView multimediaView = new MediaView();
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
        if (controlPanel.getPlayer() != null) {
            controlPanel.getPlayer().stop();
            multimediaView.setMediaPlayer(null);
        }
        if (canvas.getChildren() != null) {
            canvas.getChildren().clear();
        }
    }

    @Override
    public void requestFocus() {
        multimediaView.requestFocus();
    }

    private void drawDisplayable(Displayable displayable) {
        LOGGER.info("MultimediaDrawer drawer on " + canvas.getName());
        controlPanel.loadMultimedia((MultimediaDisplayable) displayable);
        multimediaView.setSmooth(true);
        multimediaView.setFitHeight(canvas.getHeight());
        multimediaView.setFitWidth(canvas.getWidth());
        multimediaView.setMediaPlayer(controlPanel.getPlayer());
        VBox pane = new VBox();
        pane.getChildren().add(multimediaView);
        pane.getChildren().add(controlPanel);
        canvas.getChildren().add(pane);
    }
}
