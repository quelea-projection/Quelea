/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        for (DisplayCanvas canvas : getCanvases()) {
            canvas.setCurrentDisplayable(currentDisplayable);
            getDrawer(canvas).draw(currentDisplayable);
        }
    }

    @Override
    public void removeCurrentDisplayable() {
        assert Utils.fxThread();
        for (DisplayCanvas canvas : getCanvases()) {
            /*if(!canvas.isLogoShowing()) {
             canvas.clearCurrentDisplayable();
             }*/
            canvas.clearNonPermanentChildren();
        }
    }

    @Override
    public abstract int getCurrentIndex();

    public abstract DisplayableDrawer getDrawer(DisplayCanvas canvas);
}
