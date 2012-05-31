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
package org.quelea.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;

/**
 * Player used to play one sound file at a time. At present, supports the
 * following formats: AIFF, AU, WAV, AAC, MP3, OGG, FLAC.
 *
 * @author Michael
 */
public class AudioPlayer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Object lock = new Object();
    private volatile boolean paused = false;
    private PlayThread playThread;
    private Playlist playlist;
    private int volume;
    private final List<AudioListener> listeners;

    /**
     * Create the audio player.
     */
    public AudioPlayer() {
        listeners = new ArrayList<>();
        playlist = new Playlist();
        volume = 100;
    }

    /**
     * Add an audio listener to listen for events on this player.
     *
     * @param listener the listener to add.
     */
    public void addAudioListener(AudioListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove an audio listener from this player.
     *
     * @param listener the listener to remove.
     */
    public void removeAudioListener(AudioListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get the current volume of the player, between 0-100.
     *
     * @return the current volume.
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Set the volume of the player, between 0-100.
     *
     * @param volume the volume to set.
     */
    public void setVolume(int volume) {
        if(volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0-100, but \"" + volume + "\" was given.");
        }
        this.volume = volume;
        if(playThread != null) {
            playThread.updateVolume();
        }
        for(AudioListener listener : listeners) {
            listener.volumeChanged(volume);
        }
    }

    /**
     * Play the given piece of music. Stop any existing music, if playing.
     */
    public void play() {
        AudioTrack track = playlist.getCurrentTrack();
        if(track == null) {
            throw new IllegalArgumentException("Can't play null track");
        }
        stop();
        playThread = new PlayThread(track);
        playThread.start();
        for(AudioListener listener : listeners) {
            listener.played(track);
        }
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    /**
     * Toggle whether this player is paused. If it is already, unpause it, if
     * not, pause it.
     */
    public void togglePause() {
        if(!paused) {
            paused = true;
        }
        else {
            synchronized(lock) {
                paused = false;
                lock.notifyAll();
            }
        }
        for(AudioListener listener : listeners) {
            listener.paused(isPaused());
        }
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Stop the currently playing track.
     */
    public void stop() {
        if(playThread != null) {
            playThread.halt();
            paused = false;
        }
        for(AudioListener listener : listeners) {
            listener.stopped();
        }
    }

    /**
     * The thread that's responsible for playing the audio in the background.
     */
    private class PlayThread extends Thread {

        private static final float HALT_RATE = 3f;
        private boolean mustHalt;
        private SourceDataLine line;
        private AudioTrack track;

        /**
         * Create a new play thread.
         *
         * @param path the path to the file to play.
         */
        public PlayThread(AudioTrack track) {
            this.track = track;
            setDaemon(true);
        }

        /**
         * Commence halting of this playing thread.
         */
        public void halt() {
            mustHalt = true;
        }

        /**
         * Main thread method to execute in the background - play the path and
         * respond to pause / halt notifications.
         */
        @Override
        public void run() {
            AudioInputStream din = null;
            try {
                AudioInputStream in = track.getAudioInputStream();
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                        baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                        false);
                din = AudioSystem.getAudioInputStream(decodedFormat, in);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                line = (SourceDataLine) AudioSystem.getLine(info);
                if(line != null) {
                    line.open(decodedFormat);
                    byte[] data = new byte[4096];
                    // Start
                    line.start();
                    updateVolume();

                    int nBytesRead;
                    synchronized(lock) {
                        while((nBytesRead = din.read(data, 0, data.length)) != -1) {
                            while(paused) {
                                if(mustHalt) {
                                    break;
                                }
                                if(line.isRunning()) {
                                    line.stop();
                                }
                                try {
                                    lock.wait();
                                }
                                catch (InterruptedException e) {
                                }
                            }

                            if(!line.isRunning()) {
                                line.start();
                            }
                            if(mustHalt) {
                                FloatControl c = (FloatControl) line.getControl(Type.MASTER_GAIN);
                                c.setValue(c.getValue() - HALT_RATE);
                                if(c.getValue() <= c.getMinimum() + 5) {
                                    break;
                                }
                            }

                            line.write(data, 0, nBytesRead);
                        }
                    }
                    //Stop here
                    for(AudioListener listener : listeners) {
                        listener.stopped();
                    }
                    line.drain();
                    line.stop();
                    line.close();
                    din.close();
                }

            }
            catch (IOException | LineUnavailableException ex) {
                LOGGER.log(Level.WARNING, "Error playing audio", ex);
            }
            finally {
                if(din != null) {
                    try {
                        din.close();
                    }
                    catch (IOException e) {
                    }
                }
            }
        }

        /**
         * Internal method to update the volume on this thread.
         */
        private void updateVolume() {
            if(line != null) {
                FloatControl c = (FloatControl) line.getControl(Type.MASTER_GAIN);
                double range = c.getMaximum() - c.getMinimum();
                int logVal = logToLin(volume);
                double val = (logVal / 100.0) * range + c.getMinimum();
                if(val < c.getMinimum()) {
                    LOGGER.log(Level.WARNING, "val out of range, {0}. Minimum is {1}. Volume was set at {2}", new Object[]{val, c.getMinimum(), volume});
                    val = c.getMinimum();
                }
                if(val > c.getMaximum()) {
                    LOGGER.log(Level.WARNING, "val out of range, {0}. Maximum is {1}. Volume was set at {2}", new Object[]{val, c.getMaximum(), volume});
                    val = c.getMaximum();
                }
                c.setValue((float) val);
            }
        }

        /**
         * Converts a logarithmic value to a linear one, so we get a linear
         * volume control.
         */
        private int logToLin(int level) {
            return (int) ((Math.log(level) / Math.log(100)) * 100);
        }
    }
}