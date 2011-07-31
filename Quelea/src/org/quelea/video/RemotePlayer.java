package org.quelea.video;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

    private String getInput() {
        try {
            return in.readLine();
        }
        catch (IOException ex) {
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
        if(!paused) {
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

    public boolean getMute() {
        writeOut("mute?");
        return Boolean.parseBoolean(getInput());
    }

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
