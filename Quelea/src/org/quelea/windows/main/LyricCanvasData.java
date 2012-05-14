/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import org.quelea.Theme;

/**
 * Shared data between both parts of a lyrics canvas, the base part (used for
 * the background) and the top part (used for the text and anything else that
 * should be overlayed on the background.)
 *
 * @author Michael
 */
public class LyricCanvasData {

    private boolean cleared;
    private boolean blacked;
    private boolean stageView;
    private Theme theme;

    /**
     * Create new shared canvas data. The shared theme will be set to the
     * default theme.
     */
    public LyricCanvasData(boolean stageView) {
        cleared = false;
        blacked = false;
        this.stageView = stageView;
        theme = Theme.DEFAULT_THEME;
    }

    /**
     * Toggle the cleared attribute (if it's on, set it to off, and vice versa.
     */
    public void toggleCleared() {
        cleared ^= true;
    }

    /**
     * Toggle the blacked attribute (if it's on, set it to off, and vice versa.
     */
    public void toggleBlacked() {
        blacked ^= true;
    }

    /**
     * Set the theme.
     *
     * @param theme the theme to set.
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * Determine if the blacked attribute is set.
     *
     * @return true if it's set, false otherwise.
     */
    public boolean isBlacked() {
        return blacked;
    }

    /**
     * Determine if the cleared attribute is set.
     *
     * @return true if it's set, false otherwise.
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Determine if the stage view attribute is set.
     *
     * @return true if it's set, false otherwise.
     */
    public boolean isStageView() {
        return stageView;
    }

    /**
     * Get the curent theme.
     *
     * @return the current theme in use.
     */
    public Theme getTheme() {
        return theme;
    }
}
