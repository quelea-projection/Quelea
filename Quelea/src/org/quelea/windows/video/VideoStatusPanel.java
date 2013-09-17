/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.video;

import javafx.scene.layout.BorderPane;

/**
 * A panel that goes along the bottom of the control panel and shows the video
 * time and volume.
 * @author Michael
 */
public class VideoStatusPanel extends BorderPane {
    
    private TimeDisplay timeDisplay;
    private VolumeSlider volumeSlider;
    
    /**
     * Create a new video status panel.
     */
    public VideoStatusPanel() {
        timeDisplay = new TimeDisplay();
        setLeft(timeDisplay);
        volumeSlider = new VolumeSlider();
        setCenter(volumeSlider);
    }

    /**
     * Get the time display on the status panel.
     * @return the time display.
     */
    public TimeDisplay getTimeDisplay() {
        return timeDisplay;
    }

    /**
     * Get the volume slider on the status panel.
     * @return the volume slider.
     */
    public VolumeSlider getVolumeSlider() {
        return volumeSlider;
    }
    
}
