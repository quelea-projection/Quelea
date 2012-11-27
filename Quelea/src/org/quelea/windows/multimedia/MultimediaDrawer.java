package org.quelea.windows.multimedia;

import javafx.scene.layout.BorderPane;
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
        getControlPanel().loadMultimedia((MultimediaDisplayable) displayable);
        multimediaView.setSmooth(true);
        multimediaView.setMediaPlayer(getControlPanel().getPlayer());
        BorderPane pane = new BorderPane();
        pane.setTop(getControlPanel());
        pane.setCenter(multimediaView);
        canvas.getChildren().add(pane);
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
}
