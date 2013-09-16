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
import org.quelea.services.utils.Utils;

/**
 * Implements ContainedPanel with additional canvas registering
 * <p/>
 * @author tomaszpio@gmail.com
 */
public abstract class AbstractPanel extends BorderPane implements ContainedPanel {

    private static class PriorityComparator implements Comparator<DisplayCanvas> {

        @Override
        public int compare(DisplayCanvas o1, DisplayCanvas o2) {
            return o2.getDravingPriority().getPriority() - o1.getDravingPriority().getPriority();
        }
    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private SortedSet<DisplayCanvas> canvases = new TreeSet<>(new PriorityComparator());
    private Displayable currentDisplayable = null;

    public void setCurrentDisplayable(Displayable currentDisplayable) {
        this.currentDisplayable = currentDisplayable;
    }

    public Displayable getCurrentDisplayable() {
        return currentDisplayable;
    }

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
     * <p/>
     * @param displayable the video displayable.
     */
    public void showDisplayable(MultimediaDisplayable displayable) {
        currentDisplayable = displayable;
        updateCanvas();
    }

    public void updateCanvas() {
        assert Utils.fxThread();
        for(DisplayCanvas canvas : getCanvases()) {
            canvas.setCurrentDisplayable(currentDisplayable);
            if(!canvas.isBlacked()) {
                getDrawer(canvas).draw(currentDisplayable);
            }
        }
    }

    @Override
    public void removeCurrentDisplayable() {
        assert Utils.fxThread();
        for(DisplayCanvas canvas : getCanvases()) {
            canvas.clearCurrentDisplayable();
            canvas.clearApartFromNotice();
        }
    }

    @Override
    public abstract int getCurrentIndex();

    public abstract DisplayableDrawer getDrawer(DisplayCanvas canvas);
}
