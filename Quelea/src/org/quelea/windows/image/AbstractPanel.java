package org.quelea.windows.image;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.windows.main.ContainedPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.LivePreviewPanel;

/**
 * Implements ContainedPanel with additional canvas registering
 *
 * @author tomaszpio@gmail.com
 */
public abstract class AbstractPanel extends BorderPane implements ContainedPanel {

    Set<DisplayCanvas> canvases = new HashSet<>();
    protected LivePreviewPanel containerPanel;
    protected Displayable currentDisplayable = null;

    public AbstractPanel() {
    }

    /**
     *
     * @return
     */
    @Override
    public Set<DisplayCanvas> getCanvases() {
        return canvases;
    }

    @Override
    public void registerDisplayCanvas(DisplayCanvas canvas) {
        canvases.add(canvas);
    }

    /**
     * Show a given video displayable on the panel.
     *
     * @param displayable the video displayable.
     */
    public void showDisplayable(MultimediaDisplayable displayable) {

        if (currentDisplayable != null && !currentDisplayable.equals(displayable)) {
            updateCanvas();
        } else if (currentDisplayable == null) {
            currentDisplayable = displayable;
            updateCanvas();
        }
    }

    public void updateCanvas() {
        for (DisplayCanvas canvas : getCanvases()) {
            containerPanel.getDrawer(canvas).draw(currentDisplayable);
        }
    }

    @Override
    public abstract void focus();

    @Override
    public abstract void clear();

    @Override
    public abstract int getCurrentIndex();
}
