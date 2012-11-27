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
package org.quelea.windows.multimedia;

import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.ContainedPanel;
import org.quelea.windows.main.LivePreviewPanel;

/**
 * A panel used in the live / preview panels for playing audio.
 *
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends BorderPane implements ContainedPanel {

    private LivePreviewPanel containerPanel;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel(LivePreviewPanel panel) {
        this.containerPanel = panel;
    }

    /**
     * Focus on this video panel. Currently unimplemented.
     */
    @Override
    public void focus() {
        containerPanel.getDrawer().requestFocus();
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        containerPanel.getDrawer().clear();
    }

    /**
     * Get the video control panel on this video panel.
     *
     * @return the video control panel.
     */
    public MultimediaControlPanel getMultimediaControlPanel() {
        return ((MultimediaDrawer)containerPanel.getDrawer()).getControlPanel();
    }

    /**
     * Show a given video displayable on the panel.
     *
     * @param displayable the video displayable.
     */
    public void showDisplayable(MultimediaDisplayable displayable) {
        
        for (DisplayCanvas canvas : containerPanel.getCanvases()) {
            MultimediaDrawer drawer = (MultimediaDrawer) containerPanel.getDrawer(canvas);
            drawer.draw(displayable);
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
