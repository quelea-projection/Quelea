package org.quelea.video;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael
 */
public class RemotePlayer {

    private BufferedReader in;
    private BufferedWriter out;
    private boolean open;
    private boolean playing;

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
    }

    public void pause() {
        writeOut("pause");
        playing = false;
    }

    public void stop() {
        writeOut("stop");
        playing = false;
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

    public void close() {
        if (open) {
            writeOut("close");
            playing = false;
            open = false;
        }
    }

    public boolean isPlaying() {
        return playing;
    }
}
