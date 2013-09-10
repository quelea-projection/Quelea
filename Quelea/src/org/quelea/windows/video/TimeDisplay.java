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

import javafx.scene.control.Label;

/**
 * Responsible for showing the current and total time on a video. Converts 
 * seconds into a nice HH:MM:SS format.
 *
 * @author Michael
 */
public class TimeDisplay extends Label {

    private int currentSeconds;
    private int totalSeconds;
    
    /**
     * Create a new time display.
     */
    public TimeDisplay() {
        update();
    }

    /**
     * Set the amount of current seconds to display.
     * @param currentSeconds the current seconds.
     */
    public void setCurrentSeconds(int currentSeconds) {
        this.currentSeconds = currentSeconds;
        update();
    }

    /**
     * Set the amount of total seconds to display.
     * @param totalSeconds the total seconds.
     */
    public void setTotalSeconds(int totalSeconds) {
        this.totalSeconds = totalSeconds;
        update();
    }

    /**
     * Re-draw the label.
     */
    private void update() {
        setText(formatIntoHHMMSS(currentSeconds) + " / " + formatIntoHHMMSS(totalSeconds));
    }

    /**
     * Format the seconds to a nice string.
     * @param secsIn number of seconds.
     * @return a string consisting of HH:MM:SS
     */
    private static String formatIntoHHMMSS(int secsIn) {

        int hours = secsIn / 3600,
                remainder = secsIn % 3600,
                minutes = remainder / 60,
                seconds = remainder % 60;

        return ((hours < 10 ? "0" : "") + hours
                + ":" + (minutes < 10 ? "0" : "") + minutes
                + ":" + (seconds < 10 ? "0" : "") + seconds);

    }
}
