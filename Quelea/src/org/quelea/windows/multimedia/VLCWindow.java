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
package org.quelea.windows.multimedia;

import org.quelea.services.utils.QueleaProperties;

/**
 * This class is used to create a standard way of accessing VLC windows. This
 * allows users to determine if they want to use the VLC Out of Process Player
 * or the VLC in process Embedded Media Player
 * 
 */
public abstract class VLCWindow {

    public static final VLCWindow INSTANCE = getInstance();

    /**
     * In process use only. Determines which VLCJ instance should be used,
     * whether it be the in-process player, or the out of process player.
     *
     * @return Returns the instance of VLCWindow that will be accessed
     * throughout Quelea.
     */
    private static final VLCWindow getInstance() {
        QueleaProperties props = QueleaProperties.get();
        if (props.getVLCAdvanced()) {
            return VLCWindowAdvancedEmbed.INSTANCE;
        } else {
            return VLCWindowEmbed.INSTANCE;
        }
    }

    /**
     * Determine whether the VLCJ player has initialized.
     *
     * @return True if it has initialized, otherwise false
     */
    public abstract boolean isInit();

    /**
     * Set the repeat of the current video playing.
     *
     * @param repeat True if repeat desired, false otherwise.
     */
    public abstract void setRepeat(final boolean repeat);

    /**
     * Load the desired video into the video player
     *
     * @param path The path to the desired video
     */
    public abstract void load(final String path);

    /**
     * Play the already loaded video.
     */
    public abstract void play();

    /**
     * Play the video passed into the method.
     *
     * @param vid The path to the desired video
     */
    public abstract void play(final String vid);

    /**
     * Get the last played or loaded video.
     *
     * @return The path to the last played or loaded video.
     */
    public abstract String getLastLocation();

    /**
     * Pause the currently playing video
     */
    public abstract void pause();

    /**
     * Stop the currently playing video
     */
    public abstract void stop();

    /**
     * Get whether the currently playing video is muted.
     *
     * @return True if muted, False otherwise
     */
    public abstract boolean isMute();

    /**
     * Mute the currently playing video.
     *
     * @param mute True if mute desired, otherwise false
     */
    public abstract void setMute(final boolean mute);

    /**
     * Return the progress of the video as a percent.
     *
     * @return The percentage elapsed of the currently playing video.
     */
    public abstract double getProgressPercent();

    /**
     * Set the position of the current video. The position is in percent
     * elapsed.
     *
     * @param percent The desired percentage elapsed.
     */
    public abstract void setProgressPercent(final double percent);

    /**
     * Determine whether the video is currently playing
     *
     * @return True if playing, false otherwise
     */
    public abstract boolean isPlaying();

    /**
     * Determine whether the video is currently paused
     *
     * @return True if paused, false otherwise
     */
    public abstract boolean isPaused();

    /**
     * Set a runnable to be executed upon the completion of the currently
     * playing video
     *
     * @param onFinished The runnable that should be run upon completion of the
     * currently playing video.
     */
    public abstract void setOnFinished(final Runnable onFinished);

    /**
     * Show the video player
     */
    public abstract void show();

    /**
     * Hide the video player
     */
    public abstract void hide();

    /**
     * Determine what happens when the Hide button was clicked
     *
     * @param hide Boolean representing whether the player should be hidden, or
     * shown
     */
    public abstract void setHideButton(final boolean hide);

    /**
     * Set the location of the video playback window
     *
     * @param x The x coordinate of the video playback window.
     * @param y The y coordinate of the video playback window.
     */
    public abstract void setLocation(final int x, final int y);

    /**
     * Set the size of the video playback window
     *
     * @param width The desired width of the video playback window.
     * @param height The desired height of the video playback window.
     */
    public abstract void setSize(final int width, final int height);

    /**
     * Refresh the position of the video playback window to be underneath the
     * JavaFX Quelea projection window
     */
    public abstract void refreshPosition();

    /**
     * Fade the hue of the video currently playing back
     *
     * @param hue The desired hue to fade to.
     */
    public abstract void fadeHue(final double hue);

    /**
     * Set the hue of the video currently playing back
     *
     * @param hue The desired hue to fade to.
     */
    public abstract void setHue(final double hue);

    /**
     * Get the current hue of the video playing back
     *
     * @return The hue of the video.
     */
    public abstract double getHue();

}
