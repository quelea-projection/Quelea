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
package org.quelea.windows.image;

import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.DisplayableDrawer;

/**
 * A panel used in the live / preview panels for displaying images.
 *
 * @author Michael
 */
public class ImagePanel extends AbstractPanel {

    private final DisplayCanvas previewCanvas;
    private final ImageDrawer drawer = new ImageDrawer();
    /**
     * Create a new image panel.
     *
     * @param container the container this panel is contained within.
     */
    public ImagePanel() {
        previewCanvas = new DisplayCanvas(false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateCanvas();
            }
        }, Priority.LOW);
        registerDisplayCanvas(previewCanvas);
        setCenter(previewCanvas);
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        drawer.setCanvas(canvas);
        return drawer;
    }
    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        super.clear();
    }

    /**
     * Show a given image displayable on the panel.
     *
     * @param displayable the image displayable.
     */
    public void showDisplayable(ImageDisplayable displayable) {
        setCurrentDisplayable(displayable);
        updateCanvas();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
}
