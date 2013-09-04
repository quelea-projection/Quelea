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

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.DisplayableDrawer;

/**
 * A panel used in the live / preview panels for playing audio.
 *
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends AbstractPanel {

    private final DisplayCanvas previewCanvas;
    private MultimediaDrawer drawer;
    private MultimediaControlPanel controlPanel;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel(MultimediaControlPanel controlPanel) {
        this.controlPanel = controlPanel;
        drawer = new MultimediaDrawer(controlPanel);
        previewCanvas = new MultimediaPreviewCanvas(false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateCanvas(); //@todo to be fixed updating on resize due to some errors which appears when updating
                //of multimedia canvas  occur to often
            }
        }, Priority.LOW);
        registerDisplayCanvas(previewCanvas);
        setCenter(previewCanvas);
    }

    @Override
    public void registerDisplayCanvas(DisplayCanvas canvas) {
        super.registerDisplayCanvas(canvas); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        VLCMediaPlayer player = controlPanel.getPlayer();
        if (player != null) {
            player.stop();
        }
        super.clear();
    }

    /**
     * Get the video control panel on this video panel.
     *
     * @return the video control panel.
     */
    public MultimediaControlPanel getMultimediaControlPanel() {
        return controlPanel;
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        drawer.setCanvas(canvas);
        return drawer;
    }
}
