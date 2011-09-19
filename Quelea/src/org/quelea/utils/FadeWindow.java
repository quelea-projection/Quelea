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
package org.quelea.utils;

import javax.swing.JWindow;

/**
 * A window that fades in or out gradually rather than suddenly. Requires
 * hardware acceleration.
 * @author Michael
 */
public class FadeWindow extends JWindow {

    private int sleepTime = 15;
    private float speed = 0.03f;
    private boolean nextStateVisible;

    /**
     * Get the time that should be slept between each fade step.
     * @return the time that should be slept between each fade step.
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * Set the time that should be slept between each fade step.
     * @param sleepTime the time that should be slept between each fade step.
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Get the speed of each fade step.
     * @return the speed of each fade step.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the speed of each fade step.
     * @param speed the speed of each fade step.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Fade in or out, replaces setVisible().
     * @param in true if the window should fade in, false to fade out.
     */
    private void fade(final boolean in) {
        if (in) {
            FadeWindow.super.setVisible(true);
        }
        new Thread() {

            private float opacity;

            {
                if (in) {
                    opacity = 0;
                }
                else {
                    opacity = 1;
                }
            }

            @Override
            public void run() {
                if (in) {
                    while (opacity < 1) {
                        setOpacity(opacity);
                        Utils.sleep(sleepTime);
                        opacity += speed;
                    }
                }
                else {
                    while (opacity > 0) {
                        setOpacity(opacity);
                        Utils.sleep(sleepTime);
                        opacity -= speed;
                    }
                    FadeWindow.super.setVisible(false);
                }
            }
        }.start();
    }

    /**
     * Fade the window in and out gradually.
     * @param visible whether the window should be visible or not.
     */
    @Override
    public void setVisible(boolean visible) {
        if (nextStateVisible == visible) {
            return;
        }
        nextStateVisible = visible;
        fade(visible);
    }
}
