/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.windows.video;

import javafx.scene.layout.BorderPane;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.windows.main.ContainedPanel;

/**
 * A panel used in the live / preview panels for displaying videos.
 * @author Michael
 */
public class VideoPanel extends BorderPane implements ContainedPanel {

    private VideoControlPanel controlPanel = new VideoControlPanel();

    /**
     * Create a new image panel.
     */
    public VideoPanel() {
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
        //Nada. Can't clear a video.
    }

    /**
     * Get the video control panel on this video panel.
     * @return the video control panel.
     */
    public VideoControlPanel getVideoControlPanel() {
        return controlPanel;
    }

    /**
     * Show a given video displayable on the panel.
     * @param displayable the video displayable.
     */
    public void showDisplayable(VideoDisplayable displayable) {
        controlPanel.loadVideo(displayable);
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
}
