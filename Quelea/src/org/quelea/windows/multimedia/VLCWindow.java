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
import org.quelea.services.utils.Utils;

/**
 * A native VLC window which is responsible for moving where it's told, and
 * playing video files. Transparent windows can then sit on top of this giving
 * the impression of a video background. This is a singleton since more than one
 * can cause native crashes - something we don't want to deal with (hence this
 * is hard-coded to just follow the projection window around.)
 * <p/>
 * @author Michael
 */
public abstract class VLCWindow {

    /**
     * Use this thread for all VLC media player stuff to keep this class thread
     * safe.
     */
    public static final VLCWindow INSTANCE = getInstance();
    private static final boolean USE_JAVA_FX_FOR_VLC = QueleaProperties.get().getUseJavaFXforVLCRendering();

    private static VLCWindow getInstance() {

        if (USE_JAVA_FX_FOR_VLC) {
            return VLCWindowDirect.DIRECT_INSTANCE;
        } else if (Utils.isMac()) {
            return MacVideo.MAC_INSTANCE;
        } else {
            return VLCWindowEmbed.EMBED_INSTANCE;
        }
    }

    /**
     * Determine if VLC has initialised correctly.
     * <p>
     * @return true if it has, false if it hasn't because something went wrong
     * (the most likely cause is an outdated version.)
     */
    public abstract boolean isInit();

    public abstract void setRepeat(final boolean repeat);

    public abstract void load(final String path, final String options, final boolean stretch);

    public abstract void play();

    public abstract void play(final String vid, final String options, final boolean stretch);

    public abstract String getLastLocation();

    public abstract void pause();

    public abstract void stop();

    public abstract boolean isMute();

    public abstract void setMute(final boolean mute);

    public abstract double getProgressPercent();

    public abstract void setProgressPercent(final double percent);

    public abstract boolean isPlaying();

    public abstract boolean isPaused();

    public abstract void setOnFinished(final Runnable onFinished);

    public abstract void show();

    public abstract void hide();

    public abstract void setHideButton(final boolean hide);

    public abstract void setLocation(final int x, final int y);

    public abstract void setSize(final int width, final int height);

    public abstract void refreshPosition();

    public abstract void fadeHue(final double hue);

    public abstract void setHue(final double hue);

    public abstract double getHue();
    
    public abstract long getTime();
    
    public abstract void setWindowVisible(boolean visible);
    
    public abstract long getTotal();
    
    public abstract int getVolume();
    
    public abstract void setVolume(int volume);

}
