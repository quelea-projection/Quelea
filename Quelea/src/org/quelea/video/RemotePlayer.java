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
package org.quelea.video;

import java.awt.Canvas;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;

/**
 * Controls an OutOfProcessPlayer via input / output process streams.
 * @author Michael
 */
public class RemotePlayer {

    private BufferedReader in;
    private BufferedWriter out;
    private boolean open;
    private boolean playing;
    private boolean paused;

    /**
     * Internal use only.
     */
    RemotePlayer(StreamWrapper wrapper) {
        out = new BufferedWriter(new OutputStreamWriter(wrapper.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(wrapper.getInputStream()));
        playing = false;
        open = true;
    }

    /**
     * Write a given command out to the remote VM player.
     * @param command the command to send.
     */
    private void writeOut(String command) {
        if (!open) {
            throw new IllegalArgumentException("This remote player has been closed!");
        }
        try {
            out.write(command + "\n");
            out.flush();
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't perform operation", ex);
        }
    }

    /**
     * Block until receiving input from the remote VM player.
     * @return the input string received.
     */
    private String getInput() {
        try {
            return in.readLine();
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't perform operation", ex);
        }
    }

    /**
     * Load the given path into the remote player.
     * @param path the path to load.
     */
    public void load(String path) {
        writeOut("open " + path);
    }

    /**
     * Load the given path into the remote player as a video to loop.
     * @param path the path to load.
     */
    public void loadLoop(String path, final Canvas displayCanvas) {
        int width = displayCanvas.getWidth();
        int height = displayCanvas.getHeight();
        writeOut("openloop " + width + "x" + height + " " + path);
        displayCanvas.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                changeSize(displayCanvas.getWidth(), displayCanvas.getHeight());
            }
        });
    }
    
    private void changeSize(int width, int height) {
        writeOut("changesize " + width + "x" + height);
    }
    
    /**
     * Play the loaded video.
     */
    public void play() {
        writeOut("play");
        playing = true;
        paused = false;
    }

    /**
     * Pause the video.
     */
    public void pause() {
        if(!paused) {
            writeOut("pause");
            playing = false;
            paused = true;
        }
    }

    /**
     * Stop the video.
     */
    public void stop() {
        writeOut("stop");
        playing = false;
        paused = false;
    }
    
    /**
     * Set the volume.
     * @param volume the volume to set the player to.
     */
    public void setVolume(int volume) {
        writeOut("setVolume " + volume);
    }

    /**
     * Determine if the current video is playable, i.e. one is loaded and 
     * ready to start playing when play() is called.
     * @return true if the video is playable, false otherwise.
     */
    public boolean isPlayable() {
        writeOut("playable?");
        return Boolean.parseBoolean(getInput());
    }

    /**
     * Get the length of the currently loaded video.
     * @return the length of the currently loaded video.
     */
    public long getLength() {
        writeOut("length?");
        return Long.parseLong(getInput());
    }

    /**
     * Get the time in milliseconds of the current position in the video.
     * @return the time in milliseconds of the current position in the video.
     */
    public long getTime() {
        writeOut("time?");
        return Long.parseLong(getInput());
    }

    /**
     * Set the time in milliseconds of the current position in the video.
     * @param time the time in milliseconds of the current position in the
     * video.
     */
    public void setTime(long time) {
        writeOut("setTime " + time);
    }

    /**
     * Determine if this video is muted.
     * @return true if it's muted, false if not.
     */
    public boolean getMute() {
        writeOut("mute?");
        return Boolean.parseBoolean(getInput());
    }

    /**
     * Set whether this video is muted.
     * @param mute true to mute, false to unmute.
     */
    public void setMute(boolean mute) {
        writeOut("setMute " + mute);
    }

    /**
     * Terminate the OutOfProcessPlayer. MUST be called before closing, otherwise
     * the player won't quit!
     */
    public void close() {
        if (open) {
            writeOut("close");
            playing = false;
            open = false;
        }
    }

    /**
     * Determine whether the remote player is playing.
     * @return true if its playing, false otherwise.
     */
    public boolean isPlaying() {
        return playing;
    }
    
    /**
     * Determine whether the remote player is paused.
     * @return true if its paused, false otherwise.
     */
    public boolean isPaused() {
        return paused;
    }
    
}
