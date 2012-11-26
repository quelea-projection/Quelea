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

import org.quelea.windows.main.DisplayCanvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.windows.main.ContainedPanel;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePreviewPanel;

/**
 * A panel used in the live / preview panels for displaying images.
 *
 * @author Michael
 */
public class ImagePanel extends BorderPane implements ContainedPanel {

    private LivePreviewPanel containerPanel;
    private final DisplayCanvas previewCanvas;

    /**
     * Create a new image panel.
     *
     * @param container the container this panel is contained within.
     */
    public ImagePanel(LivePreviewPanel panel) {
        this.containerPanel = panel;
        previewCanvas = new DisplayCanvas(false, false, "PreviewCanvas");
        containerPanel.registerDisplayCanvas(previewCanvas);
        setCenter(previewCanvas);
    }

    @Override
    public void focus() {
        containerPanel.getDrawer().requestFocus();
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        //updateCanvases();
    }

    /**
     * Show a given image displayable on the panel.
     *
     * @param displayable the image displayable.
     */
    public void showDisplayable(ImageDisplayable displayable) {
        for (DisplayCanvas canvas : containerPanel.getCanvases()) {
            containerPanel.getDrawer(canvas).draw();
        }

    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    @Override
    public void updateCanvases() {
    }
}
