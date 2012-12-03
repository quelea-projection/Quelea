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
    private Displayable currentDisplayable = null;

    public MultimediaDrawer(MultimediaControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        if (currentDisplayable != null && !currentDisplayable.equals(displayable)) {
            drawDisplayable(displayable);
        } else if (currentDisplayable == null) {
            currentDisplayable = displayable;
            drawDisplayable(displayable);
        }
    }

    @Override
    public void clear() {
        if (getControlPanel().getPlayer() != null) {
            getControlPanel().getPlayer().stop();
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

    /**
     * @return the controlPanel
     */
    public MultimediaControlPanel getControlPanel() {
        return controlPanel;
    }

    private void drawDisplayable(Displayable displayable) {
        LOGGER.info("MultimediaDrawer drawer on " + canvas.getName());
        getControlPanel().loadMultimedia((MultimediaDisplayable) displayable);
        multimediaView.setSmooth(true);
        multimediaView.setFitHeight(canvas.getHeight());
        multimediaView.setFitWidth(canvas.getWidth());
        multimediaView.setMediaPlayer(getControlPanel().getPlayer());
        VBox pane = new VBox();
        pane.setPadding(new Insets(10));
        pane.setSpacing(8);
        pane.getChildren().addAll(multimediaView, getControlPanel());
    }
}
