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
     * Internal use only.
     */
    public RemotePlayer(String logFileLocation) {

        ProcessBuilder pb = new ProcessBuilder();
        String fullClassName = OOPPlayer.class.getName();
        String pathToClassFiles = new File("./build/classes").getPath();
        String pathSeparator = File.pathSeparator; // ":" on Linux, ";" on Windows
        String pathToLib = new File("./lib/vlcj-3.0.1.jar").getPath();

        pb.command("java", "-cp", pathToLib + pathSeparator + pathToClassFiles, fullClassName, "myArg");

        File log = new File(logFileLocation); //debug log for started process
        try {

            pb.redirectError(log);

            Process p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            System.out.println("Started");
            playing = false;
            open = true;
        } catch (IOException ex) {
            Logger.getLogger(OOPPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

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

    private String getInput() {
        try {
            return in.readLine();
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't perform operation", ex);
        }
    }

    public void load(String path) {
        writeOut("open " + path);
    }

    public void play() {
        writeOut("play");
        playing = true;
        paused = false;
    }

    public void pause() {
        if (!paused) {
            writeOut("pause");
            playing = false;
            paused = true;
        }
    }

    public void stop() {
        writeOut("stop");
        playing = false;
        paused = false;
    }

    public void setHue(int hue) {
        writeOut("setHue " + hue);
    }

    public void setRepeat(boolean repeat) {
        writeOut("setRepeat " + repeat);
    }

    public void setPosition(float percent) {
        writeOut("setPosition " + percent);
    }

    public boolean isPlayable() {
        writeOut("playable?");
        return Boolean.parseBoolean(getInput());
    }

    public long getLength() {
        writeOut("length?");
        return Long.parseLong(getInput());
    }

    public long getTime() {
        writeOut("time?");
        return Long.parseLong(getInput());
    }

    public void setTime(long time) {
        writeOut("setTime " + time);
    }

    public boolean isMute() {
        writeOut("mute?");
        return Boolean.parseBoolean(getInput());
    }

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

    public void toBack() {
        writeOut("toBack");
    }

    public void setOpacity(double opacity) {
        writeOut("setOpacity " + opacity);
    }

    public void setVolume(int volume) {
        writeOut("setVolume " + volume);
    }

    public void setLocation(int x, int y) {
        writeOut("setXLocation " + x);
        writeOut("setYLocation " + y);

    }

    public void setSize(int width, int height) {
        writeOut("setWidthSize " + width);
        writeOut("setHeightSize " + height);
    }

    /**
     * Determine whether the remote player is playing.
     *
     * @return true if its playing, false otherwise.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Determine whether the remote player is paused.
     *
     * @return true if its paused, false otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

}
