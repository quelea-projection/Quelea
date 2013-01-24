package org.quelea.windows.main;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 * Implements ContainedPanel with additional canvas registering
 *
 * @author tomaszpio@gmail.com
 */
public abstract class AbstractPanel extends BorderPane implements ContainedPanel {

    private class PriorityComparator implements Comparator<DisplayCanvas> {

        @Override
        public int compare(DisplayCanvas o1, DisplayCanvas o2) {
            return o2.getDravingPriority().getPriority() - o1.getDravingPriority().getPriority();
        }
        
    }
    SortedSet<DisplayCanvas> canvases = new TreeSet<>(new PriorityComparator());
    protected LivePreviewPanel containerPanel;
    protected Displayable currentDisplayable = null;
    private static final Logger LOGGER = LoggerUtils.getLogger();
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

        currentDisplayable = displayable;
        updateCanvas();
    }

    public void updateCanvas() {

        for (DisplayCanvas canvas : getCanvases()) {
            canvas.setCurrentDisplayable(currentDisplayable);
            getDrawer(canvas).draw(currentDisplayable);
        }
    }

    @Override
    public abstract void focus();

    @Override
    public void clear() {
        for (DisplayCanvas canvas : getCanvases()) {
            canvas.getChildren().clear();;
        }
    }

    @Override
    public abstract int getCurrentIndex();
    
    public abstract DisplayableDrawer getDrawer(DisplayCanvas canvas);
}
