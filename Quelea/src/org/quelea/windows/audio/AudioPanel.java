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
package org.quelea.windows.audio;

import java.util.logging.Logger;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.tags.services.multimedia.MultimediaControlPanel;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.ContainedPanel;

/**
 * A panel used in the live / preview panels for playing audio.
 *
 * @author tomaszpio@gmail.com
 */
public class AudioPanel extends BorderPane implements ContainedPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private MultimediaControlPanel controlPanel = new AudioControlPanel();

    /**
     * Create a new image panel.
     */
    public AudioPanel() {
        setCenter(controlPanel);
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
    public void showDisplayable(AudioDisplayable displayable) {
        controlPanel.loadMultimedia(displayable);
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
}
