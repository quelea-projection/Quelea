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

import java.util.logging.Logger;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaView;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.lyrics.DisplayCanvas;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.ContainedPanel;
import org.quelea.windows.main.LivePreviewPanel;

/**
 * A panel used in the live / preview panels for playing audio.
 *
 * @author tomaszpio@gmail.com
 */
public class MultimediaPanel extends BorderPane implements ContainedPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private MultimediaControlPanel controlPanel;
    private LivePreviewPanel containerPanel;

    /**
     * Create a new image panel.
     */
    public MultimediaPanel(MultimediaControlPanel controlPanel, LivePreviewPanel panel) {
        this.controlPanel = controlPanel;
        setCenter(this.controlPanel);
        this.containerPanel = panel;
    }

    /**
     * Focus on this video panel. Currently unimplemented.
     */
    @Override
    public void focus() {
        //TODO: Something probably
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        if (controlPanel.getPlayer() != null) {
            controlPanel.getPlayer().stop();
            ((MediaView) controlPanel.getView()).setMediaPlayer(null);
        }
    }

    /**
     * Get the video control panel on this video panel.
     *
     * @return the video control panel.
     */
    public MultimediaControlPanel getMultimediaControlPanel() {
        return controlPanel;
    }

    /**
     * Show a given video displayable on the panel.
     *
     * @param displayable the video displayable.
     */
    public void showDisplayable(MultimediaDisplayable displayable) {
        controlPanel.loadMultimedia(displayable);
        if (displayable instanceof VideoDisplayable) {
            for (DisplayCanvas canvas : containerPanel.getCanvases()) {
                LyricDrawer drawer = new LyricDrawer(canvas); //@todo check  if here should be theme setting
                drawer.setText(null, null, true);
                drawer.setTheme(new ThemeDTO(null, null,
                        new VideoBackground(displayable.getFile().getName()),
                        ThemeDTO.DEFAULT_SHADOW));
            }
        }
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
}
