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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.QueleaApp;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 * A native VLC window which is responsible for moving where it's told, and
 * playing video files. Transparent windows can then sit on top of this giving
 * the impression of a video background. This is a singleton since more than one
 * can cause native crashes - something we don't want to deal with (hence this
 * is hard-coded to just follow the projection window around.)
 * <p/>
 * @author Michael
 */
public class VLCWindow {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final VLCWindow INSTANCE = new VLCWindow();
    private Window window;
    private Canvas canvas;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;
    private boolean hideButton;
    private boolean show;
    private boolean paused;
    private boolean init;

    private VLCWindow() {
        try {
            window = new Window(null);
            window.setBackground(Color.BLACK);
            canvas = new Canvas();
            canvas.setBackground(Color.BLACK);
            mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show");
            mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
            CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
            mediaPlayer.setVideoSurface(videoSurface);
            window.add(canvas);
            show = true;
            window.setVisible(true);
            init = true;
            LOGGER.log(Level.INFO, "Video initialised ok");
        }
        catch(Exception ex) {
            LOGGER.log(Level.INFO, "Couldn't initialise video, almost definitely because VLC was not found.", ex);
        }
    }

    public void setRepeat(boolean repeat) {
        if(init) {
            mediaPlayer.setRepeat(repeat);
        }
    }

    public void load(String path) {
        if(init) {
            paused = false;
            mediaPlayer.prepareMedia(path);
        }
    }

    public void play() {
        if(init) {
            paused = false;
            mediaPlayer.play();
        }
    }

    public void play(String vid) {
        if(init) {
            paused = false;
            mediaPlayer.playMedia(vid);
        }
    }

    public void pause() {
        if(init) {
            paused = true;
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if(init) {
            paused = false;
            mediaPlayer.stop();
        }
    }

    public boolean isMute() {
        if(init) {
            return mediaPlayer.isMute();
        }
        else {
            return false;
        }
    }

    public void setMute(boolean mute) {
        if(init) {
            mediaPlayer.mute(mute);
        }
    }

    public double getProgressPercent() {
        if(init) {
            return (double) mediaPlayer.getTime() / mediaPlayer.getLength();
        }
        else {
            return 0;
        }
    }

    public void setProgressPercent(double percent) {
        if(init) {
            mediaPlayer.setPosition((float) percent);
        }
    }

    public boolean isPlaying() {
        if(init) {
            return mediaPlayer.isPlaying();
        }
        else {
            return false;
        }
    }

    public boolean isPaused() {
        if(init) {
            return paused;
        }
        else {
            return false;
        }
    }

    public void setOnFinished(final Runnable onFinished) {
        if(init) {
            paused = false;
            mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    onFinished.run();
                }
            });
        }
    }

    public void show() {
        if(init) {
            show = true;
            updateState();
        }
    }

    public void hide() {
        if(init) {
            show = false;
            updateState();
        }
    }

    public void setHideButton(boolean hide) {
        if(init) {
            this.hideButton = hide;
            updateState();
        }
    }

    private void updateState() {
        if(init) {
            window.setOpacity((hideButton || !show) ? 0 : 1);
        }
    }

    public void setLocation(int x, int y) {
        if(init) {
            window.setLocation(x, y);
        }
    }

    public void setSize(int width, int height) {
        if(init) {
            window.setSize(width, height);
        }
    }

    public void refreshPosition() {
        if(init) {
            setLocation((int) QueleaApp.get().getProjectionWindow().getX(), (int) QueleaApp.get().getProjectionWindow().getY());
            setSize((int) QueleaApp.get().getProjectionWindow().getWidth(), (int) QueleaApp.get().getProjectionWindow().getHeight());
        }
    }
}
