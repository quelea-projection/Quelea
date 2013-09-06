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
import org.quelea.windows.main.QueleaApp;
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

    public static final VLCWindow INSTANCE = new VLCWindow();
    private Window window;
    private Canvas canvas;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;
    private boolean hideButton;
    private boolean show;

    private VLCWindow() {
        window = new Window(null);
        window.setBackground(Color.BLACK);
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show");
        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
        mediaPlayer.setVideoSurface(videoSurface);
        window.add(canvas);
//        window.setOpacity(0);
        show = true;
        window.setVisible(true);
    }

    public void setRepeat(boolean repeat) {
        mediaPlayer.setRepeat(repeat);
    }

    public void load(String path) {
        mediaPlayer.prepareMedia(path);
    }

    public void play() {
        mediaPlayer.play();
    }

    public void play(String vid) {
        mediaPlayer.playMedia(vid);
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public boolean isMute() {
        return mediaPlayer.isMute();
    }

    public void setMute(boolean mute) {
        mediaPlayer.mute(mute);
    }

    public void show() {
        show = true;
        updateState();
    }

    public void hide() {
        show = false;
        updateState();
    }

    public void setHideButton(boolean hide) {
        this.hideButton = hide;
        updateState();
    }

    private void updateState() {
        window.setOpacity((hideButton || !show) ? 0 : 1);
    }

    public void setLocation(int x, int y) {
        window.setLocation(x, y);
    }

    public void setSize(int width, int height) {
        window.setSize(width, height);
    }

    public void refreshPosition() {
        setLocation((int) QueleaApp.get().getProjectionWindow().getX(), (int) QueleaApp.get().getProjectionWindow().getY());
        setSize((int) QueleaApp.get().getProjectionWindow().getWidth(), (int) QueleaApp.get().getProjectionWindow().getHeight());
    }
}
