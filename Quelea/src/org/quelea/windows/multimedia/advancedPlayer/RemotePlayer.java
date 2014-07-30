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
package org.quelea.windows.multimedia.advancedPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls an OutOfProcessPlayer via input / output process streams.
 *
 * @author Michael and revised by Greg
 */
public class RemotePlayer {

    private boolean open;
    private boolean playing;
    private boolean paused;
    private BufferedReader in;
    private BufferedWriter out;

    /**
     * Creates a new remote player. This will create an out of process player
     * using the OOPPlayer class.
     *
     * @param logFileLocation The error log from the remote player will get
     * output to this log file. It creates a new file each run.
     */
    public RemotePlayer(String logFileLocation) {

        ProcessBuilder pb = new ProcessBuilder();
        String fullClassName = OOPPlayer.class.getName();
        String pathToClassFiles = new File("./build/classes").getPath();
        String pathSeparator = File.pathSeparator; // ":" on Linux, ";" on Windows
        String pathToLib = new File("./lib/vlcj-3.0.1.jar").getPath();

        pb.command("java", "-cp", pathToLib + pathSeparator + pathToClassFiles, fullClassName, "myArg");
        //debug log for started process-- maybe it should be read and be added to.
        //as of now, the file gets replaced each run.
        File log = new File(logFileLocation);
        try {

            pb.redirectError(log);

            Process p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            playing = false;
            open = true;
        } catch (IOException ex) {
            Logger.getLogger(OOPPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Will write a command to the output stream, which will be passed to the
     * out of process player.
     *
     * @param command The command to sent to the out of process player
     */
    private void writeOut(String command) {
        if (!open) {
            throw new IllegalArgumentException("This remote player has been closed!");
        }
        try {
            out.write(command + "\n");
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't perform operation", ex);
        }

    }

    /**
     * Reads the latest line of the stream output from the out of process player
     * and returns the value
     *
     * @return the latest input from the out of process player
     */
    private String getInput() {
        try {
            return in.readLine();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't perform operation", ex);
        }
    }

    /**
     * Loads a video in the out of process player.
     *
     * @param path The URL of the video to be loaded
     */
    public void load(String path) {
        writeOut("open " + path);
    }

    /**
     * Play the already loaded video in the out of process player.
     */
    public void play() {
        writeOut("play");
        playing = true;
        paused = false;
    }

    /**
     * Pause the video playing in the out of process player.
     */
    public void pause() {
        if (!paused) {
            writeOut("pause");
            playing = false;
            paused = true;
        }
    }

    /**
     * Stop the video playing in the out of process player.
     */
    public void stop() {
        writeOut("stop");
        playing = false;
        paused = false;
    }

    /**
     * Set the hue of the video playing in the out of process player.
     *
     * @param hue The desired value of hue.
     */
    public void setHue(int hue) {
        writeOut("setHue " + hue);
    }

    /**
     * Set the repeat of the current video in the out of process player;
     *
     * @param repeat True if repeat is desired, false otherwise.
     */
    public void setRepeat(boolean repeat) {
        writeOut("setRepeat " + repeat);
    }

    /**
     * Set the position of the desired video by the percent of the video.
     *
     * @param percent The position in percent the video is to be set to.
     */
    public void setPosition(float percent) {
        writeOut("setPosition " + percent);
    }

    /**
     * Returns whether the out of process player is playable.
     *
     * @return True if it is playable, False otherwise.
     */
    public boolean isPlayable() {
        writeOut("playable?");
        return Boolean.parseBoolean(getInput());
    }

    /**
     * Gets the length of the video.
     *
     * @return The length of the video in milliseconds
     */
    public long getLength() {
        writeOut("length?");
        return Long.parseLong(getInput());
    }

    /**
     * Gets the opacity of the video window
     * @return the opacity of the remote player
     */
    public float getOpacity() {
        writeOut("opacity?");
        return Float.parseFloat(getInput());
    }

    /**
     * See if the remote player has initialized properly
     *
     * @return true if initialized, false otherwise
     */
    public boolean isInit() {
        writeOut("init?");
        return Boolean.parseBoolean(getInput());
    }

    /**
     * Gets the current time of the video.
     *
     * @return The current time of the video in number of milliseconds
     */
    public long getTime() {
        writeOut("time?");
        return Long.parseLong(getInput());
    }

    /**
     * Set the current time of the video
     *
     * @param time The desired time in milliseconds elapsed.
     */
    public void setTime(long time) {
        writeOut("setTime " + time);
    }

    /**
     * Determine whether the out of process player is muted.
     *
     * @return True if it is muted, False otherwise
     */
    public boolean isMute() {
        writeOut("mute?");
        return Boolean.parseBoolean(getInput());
    }

    /**
     * Set the mute of the out of process player.
     *
     * @param mute True if the player is to be muted, False otherwise
     */
    public void setMute(boolean mute) {
        writeOut("setMute " + mute);
    }

    /**
     * Terminate the OutOfProcessPlayer. MUST be called before closing,
     * otherwise the player won't quit!
     */
    public void close() {
        if (open) {
            writeOut("close");
            playing = false;
            open = false;
        }
    }

    /**
     * Sets the video window to the back-most window
     */
    public void toBack() {
        writeOut("toBack");
    }
    
    /**
     * Sets the video window to the front-most window
     */
    public void toFront(){
        writeOut("toFront");
    }

    /**
     * Sets the opacity of the output window
     *
     * @param opacity The opacity (0-1) that the out of process player window
     * should be.
     */
    public void setOpacity(double opacity) {
        writeOut("setOpacity " + opacity);
    }

    /**
     * Sets the volume of the out of process player.
     *
     * @param volume The volume (0-100) that the out of process player should
     * be.
     */
    public void setVolume(int volume) {
        writeOut("setVolume " + volume);
    }

    /**
     * Sets the location of the out of process video playback window.
     *
     * @param x The desired x coordinate of the window.
     * @param y The desired y coordinate of the window.
     */
    public void setLocation(int x, int y) {
        writeOut("setXLocation " + x);
        writeOut("setYLocation " + y);

    }

    /**
     * Sets the size of the out of process video playback window.
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        writeOut("setWidthSize " + width);
        writeOut("setHeightSize " + height);
    }

    /**
     * Determine whether the out of process player is playing.
     *
     * @return true if its playing, false otherwise.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Determine whether the out of process player player is paused.
     *
     * @return true if its paused, false otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

}
