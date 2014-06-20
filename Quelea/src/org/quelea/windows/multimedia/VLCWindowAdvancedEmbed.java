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
import static java.awt.SystemColor.window;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.time.StopWatch;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.advancedPlayer.RemotePlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * A native VLC window which is responsible for moving where it's told, and
 * playing video files. Transparent windows can then sit on top of this giving
 * the impression of a video background. This is an out of process video player,
 * providing capability for more than one to be created without crashes (At
 * least for the native JVM). We then fade between them on the output. This
 * follows the singleton method, so that the user can choose to use the in
 * process embedded media player, this is accomplished through the interface
 * VLCWindow.
 * <p/>
 * @author Greg with help from elsewhere
 */
public class VLCWindowAdvancedEmbed extends VLCWindow {

    private boolean hideButton;
    private boolean show;
    private boolean paused;
    private volatile boolean init;
    private String location;
    private volatile double hue = 0;
    private RemotePlayer player;
    private RemotePlayer player2;
    private Window window;
    private Canvas canvas;
    private boolean isPlayer1 = false;
    private final static double FADE_SPEED = 1.0; //in seconds
    private FadeThread fadeThread;
    private Runnable runOnFinished;
    private ScheduledExecutorService onFinishedExc;
    //temp variables
    private boolean muteTemp;
    private double progressTemp;
    private boolean isPlayingTemp;
    private boolean isPausedTemp;
    private int tempX, tempY, tempWidth, tempHeight;
    private boolean showing;

    public static final VLCWindowAdvancedEmbed INSTANCE = new VLCWindowAdvancedEmbed();

    //construct a black background, and two players to fade between
    /**
     * Create a new advanced embedded media player. This will create a black
     * "background" which is actually an awt window/canvas. It also creates the
     * two RemotePlayers to fade between. The RemotePlayers create out of
     * process media players.
     */
    private VLCWindowAdvancedEmbed() {

        show = true;
        player = new RemotePlayer("log.txt");
        player2 = new RemotePlayer("log2.txt");
        player2.setOpacity(1);
        player2.setVolume(100);
        player.setOpacity(1);
        player.setVolume(100);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                window = new Window(null);
                window.setBackground(Color.BLACK);
                canvas = new Canvas();
                canvas.setBackground(Color.BLACK);
                window.add(canvas);
                window.setVisible(true);
                window.toBack();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                player.close();
                player2.close();
            }
        });
        ScheduledExecutorService exc = Executors.newSingleThreadScheduledExecutor();
        exc.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (init) {

                    if (isPlayer1) {
                        player.setHue((int) (hue * 360));
                    } else {
                        player2.setHue((int) (hue * 360));
                    }

                }
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
        try {
            /**
             * this should not cause native crashes, but should be enough to
             * determine whether VLCJ will work or not.
             *
             */

            MediaPlayerFactory factory = new MediaPlayerFactory("--no-video-title-show");
            init = true;
        } catch (Exception ex) {
            init = false;
        }

        windowToBack();
    }

    /**
     * Determine if VLC has initialised correctly.
     * <p>
     * @return true if it has, false if it hasn't because something went wrong
     * (the most likely cause is an outdated version.)
     */
    @Override
    public boolean isInit() {
        return init;
    }

    /**
     * Transition between remote players. It also changes which player is
     * active.
     */
    public void transitionBetween() {
        if (init) {
            isPlayer1 = !isPlayer1;

            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {

                                window.toBack();
                                window.toBack();

                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Logger.getLogger(VLCWindowAdvancedEmbed.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    window.setOpacity(1);
                    for (double i = 0; i < 100; i++) {

                        if (isPlayer1) {
                            player.setOpacity(i / 100);
                            player.setVolume((int) i);
                            player2.setOpacity(1.0 - (i / 100.0));
                            player2.setVolume((int) (100 - i));
                        } else {
                            player2.setOpacity(i / 100);
                            player2.setVolume((int) i);
                            player.setOpacity(1.0 - (i / 100.0));
                            player.setVolume((int) (100 - i));

                        }
                        StopWatch s = new StopWatch();
                        s.start();
                        while (s.getTime() < ((FADE_SPEED * 1000) / 100)) {
                            //do nothing 
                        }

                    }

                    if (isPlayer1) {
                        player2.stop();
                    } else {
                        player.stop();
                    }
                }
            });
        }

    }

    /**
     * Fades out the active player and pauses the video of the active player.
     *
     * @param pause Determine whether the active video should be paused or not.
     */
    public void fadeOutActive(final boolean pause) {
        if (init) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    for (double i = 0; i < 100; i++) {

                        if (isPlayer1) {

                            player2.setOpacity(1.0 - (i / 100.0));
                            player2.setVolume((int) (100 - i));
                        } else {

                            player.setOpacity(1.0 - (i / 100.0));
                            player.setVolume((int) (100 - i));

                        }
                        StopWatch s = new StopWatch();
                        s.start();
                        while (s.getTime() < ((FADE_SPEED * 1000) / 100)) {
                            //do nothing 
                        }

                    }
                    if (pause) {
                        if (isPlayer1) {
                            player2.pause();
                        } else {
                            player.pause();
                        }
                    }
                }

            });
        }

    }

    /**
     * Fades up active player and resumes if resumePlayback is true.
     *
     * @param resumePlayback If True the active player will resume upon fading
     * up, If false, the active player will only fade up.
     */
    public void fadeUpActive(final boolean resumePlayback) {
        if (init) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    if (resumePlayback) {
                        if (isPlayer1) {
                            player.play();
                        } else {
                            player2.play();
                        }
                    }
                    for (double i = 0; i < 100; i++) {
                        if (isPlayer1) {
                            player.setOpacity(i / 100);
                            player.setVolume((int) i);

                        } else {
                            player2.setOpacity(i / 100);
                            player2.setVolume((int) i);

                        }
                        StopWatch s = new StopWatch();
                        s.start();
                        while (s.getTime() < ((FADE_SPEED * 1000) / 100)) {
                            //do nothing 
                        }

                    }

                }

            });
        }
    }

    /**
     * Set the repeat for the remote player
     *
     * @param repeat If true, the player will repeat (loop), If false, the
     * player will not loop.
     */
    @Override
    public void setRepeat(final boolean repeat) {
        //may be broken for some instances 
        if (init) {
            
                player.setRepeat(repeat);
           
                player2.setRepeat(repeat);
            
        }

    }

    /**
     * Load the video specified in path into the inactive media player. Upon
     * play, the players get transitioned
     *
     * @param path The path of the video desired to load.
     */
    @Override
    public void load(final String path) {
        if (init) {

            this.location = path;
            isPlayer1 = !isPlayer1;
            paused = false;
            String sanitisedPath = path;
            sanitisedPath = sanitisedPath.trim();
            if (sanitisedPath.startsWith("www")) {
                sanitisedPath = "http://" + sanitisedPath;
            }
            if (isPlayer1) {
                player.load(sanitisedPath);

            } else {
                player2.load(sanitisedPath);

            }

        }

    }

    /**
     * Starts a thread executer that polls the correct media player to see if it
     * has finished.
     */
    private void startOnCompletionWatcher() {

        onFinishedExc = Executors.newSingleThreadScheduledExecutor();
        onFinishedExc.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
               
                if (init) {
                    if (Math.abs(getProgressPercent() - 1.0) < 0.01) {
                        if (!(runOnFinished == null)) {
                            runOnFinished.run();

                        }
                        onFinishedExc.shutdownNow();
                    }

                }
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
    }

    /**
     * Plays the loaded video and transitions to the active video player.
     */
    @Override
    public void play() {

        if (init) {
            isPlayer1 = !isPlayer1;
            transitionBetween();
            if (isPlayer1) {
                player.play();
            } else {
                player2.play();
            }
            startOnCompletionWatcher();
        }

    }

    /**
     * Plays a video from a file, and transitions to the active video player.
     *
     * @param vid The path of the video desired to play.
     */
    @Override
    public void play(final String vid) {
        this.location = vid;

        if (init) {
            transitionBetween();
            paused = false;
            if (isPlayer1) {
                player.load(vid);
                player.play();
            } else {
                player2.load(vid);
                player2.play();
            }
            startOnCompletionWatcher();

        }

    }

    /**
     * Get the last played video file.
     *
     * @return The path to the last video file that was played.
     */
    @Override
    public String getLastLocation() {
        return location;
    }

    /**
     * Pause the active video player
     */
    @Override
    public void pause() {

        if (init) {

            paused = true;
            if (isPlayer1) {
                player.pause();
            } else {
                player2.pause();
            }

        }

    }

    /**
     * Fades off the active player, and stops that video..
     */
    @Override
    public void stop() {
        location = null;

        if (init) {

            paused = false;
            if (isPlayer1) {
                if (player.isPlaying()) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            for (double i = 0; i < 100; i++) {

                                player.setOpacity(1.0 - (i / 100.0));
                                player.setVolume((int) (100 - i));

                                StopWatch s = new StopWatch();
                                s.start();
                                while (s.getTime() < ((FADE_SPEED * 1000) / 100)) {
                                    //do nothing 
                                }

                            }

                            player.stop();
                        }
                    });

                }

            } else {
                if (player2.isPlaying()) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            for (double i = 0; i < 100; i++) {

                                player2.setOpacity(1.0 - (i / 100.0));
                                player2.setVolume((int) (100 - i));

                                StopWatch s = new StopWatch();
                                s.start();
                                while (s.getTime() < ((FADE_SPEED * 1000) / 100)) {
                                    //do nothing 
                                }

                            }

                            player2.stop();
                        }
                    });

                }
            }

        }

    }

    /**
     * Determines whether the active video player is muted.
     *
     * @return Returns true if the active video is muted, or Returns false
     * otherwise.
     */
    @Override
    public boolean isMute() {

        if (init) {
            if (isPlayer1) {
                muteTemp = player.isMute();
            } else {
                muteTemp = player2.isMute();
            }

        } else {
            muteTemp = false;
        }

        return muteTemp;
    }

    /**
     * Set the mute for the active video player.
     *
     * @param mute True if mute is desired, false otherwise.
     */
    @Override
    public void setMute(final boolean mute) {

        if (init) {
          
                player.setMute(mute);
            
                player2.setMute(mute);
            

        }

    }

    /**
     * Get the progress of the currently playing active video player.
     *
     * @return Returns the percent progress (0-1)
     */
    @Override
    public double getProgressPercent() {
        if (init) {
            if (isPlayer1) {
                progressTemp = (double) player.getTime() / player.getLength();
            } else {
                progressTemp = (double) player2.getTime() / player2.getLength();
            }

        } else {
            progressTemp = 0;
        }
        return progressTemp;
    }

    /**
     * Set the progress of the currently playing active video player.
     *
     * @param percent The desired percentage elapsed.
     */
    @Override
    public void setProgressPercent(final double percent) {

        if (init) {
            if (isPlayer1) {
                player.setPosition((float) percent);
            } else {
                player2.setPosition((float) percent);
            }

        }

    }

    /**
     * Determine whether the active video is playing.
     *
     * @return True if the active video is playing, False otherwise.
     */
    @Override
    public boolean isPlaying() {

        if (init) {
            if (isPlayer1) {
                isPlayingTemp = player.isPlaying();
            } else {
                isPlayingTemp = player2.isPlaying();
            }

        } else {
            isPlayingTemp = false;
        }

        return isPlayingTemp;
    }

    /**
     * Determine whether the active video player is paused
     *
     * @return True if the active video player is paused, False otherwise.
     */
    @Override
    public boolean isPaused() {

        if (init) {
            isPausedTemp = paused;
        } else {
            isPausedTemp = false;
        }

        return isPausedTemp;
    }

    /**
     * Sets a runnable to be executed upon completion of active video. Note that
     * Execution will happen only after the first completion for a video with
     * repeat set to true.
     *
     * @param onFinished The runnable to be executed.
     */
    @Override
    public void setOnFinished(final Runnable onFinished) {
        runOnFinished = onFinished;

    }

    /**
     * Show the active video player instantly.
     */
    @Override
    public void show() {

        if (init) {
            if(isPlayer1){
                player.setOpacity(1);
            }else{
                player2.setOpacity(1);
            }
            windowToBack();
        }

    }

    /**
     * Hide the active video player instantly.
     */
    @Override
    public void hide() {

        if (init) {
            if(isPlayer1){
                player.setOpacity(0);
            }else{
                player2.setOpacity(0);
            }
            windowToBack();
        }

    }

    /**
     * Fades out player and pauses if hidden, otherwise fades back and resumes
     * playback
     *
     * @param hide True if to be hidden, false otherwise
     */
    @Override
    public void setHideButton(final boolean hide) {

        if (init) {
            hideButton = hide;
            if (hideButton) {
                fadeOutActive(true);
            } else {
                fadeUpActive(true);
            }
            windowToBack();

        }

    }

    /**
     * Updates the state of the black background, and pushes it to the back.
     */
    private void windowToBack() {

        if (init) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.toBack();

                }
            });
        }

    }

    /**
     * Set the location of both video players.
     *
     * @param x The desired x coordinate of the players.
     * @param y The desired y coordinate of the players.
     */
    @Override
    public void setLocation(final int x, final int y) {

        player.setLocation(x, y);

        player2.setLocation(x, y);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                window.setLocation(x, y);
                windowToBack();
            }
        });

    }

    /**
     * Set the size of both video players
     *
     * @param width The desired width of the players.
     * @param height The desired height of the players.
     */
    @Override
    public void setSize(final int width, final int height) {

        player.setSize(width, height);

        player2.setSize(width, height);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                window.setSize(width, height);
                canvas.setSize(width, height);
                windowToBack();
            }
        });

    }

    /**
     * Refresh the position of the video players when the projection window
     * changes location.
     */
    @Override
    public void refreshPosition() {
        Utils.fxRunAndWait(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getProjectionWindow().toFront();
                showing = QueleaApp.get().getProjectionWindow().isShowing();
                if (showing) {
                    tempX = (int) QueleaApp.get().getProjectionWindow().getX();
                    tempY = (int) QueleaApp.get().getProjectionWindow().getY();
                    tempWidth = (int) QueleaApp.get().getProjectionWindow().getWidth();
                    tempHeight = (int) QueleaApp.get().getProjectionWindow().getHeight();
                }
            }
        });

        if (init) {
            if (showing) {
                show();
                setLocation(tempX, tempY);
                setSize(tempWidth, tempHeight);
            } else {
                hide();
            }
        }

    }

    /**
     * Thread that will fade the hue
     */
    private class FadeThread extends Thread {

        private static final double INCREMENT = 0.002;
        private double toVal;
        private volatile boolean go = true;

        /**
         * Creates a new Fade thread.
         *
         * @param toVal To which value the Hue should be faded to.
         */
        public FadeThread(double toVal) {
            this.toVal = toVal;
        }

        /**
         * Fade the hue at the given increment.
         */
        @Override
        public void run() {
            double diff = toVal - getHue();
            if (diff < 0) {
                while (diff < 0 && go) {
                    setHue(getHue() - INCREMENT);
                    diff = toVal - getHue();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        //Meh
                    }
                }
            } else if (diff > 0) {
                while (diff > 0 && go) {
                    setHue(getHue() + INCREMENT);
                    diff = toVal - getHue();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        //Meh
                    }
                }
            }
            setHue(toVal);
        }

        /**
         * Stop fading
         */
        public void halt() {
            go = false;
        }

    }

    /**
     * Fade the hue of the current video.
     *
     * @param hue The desired hue to fade to.
     */
    @Override
    public synchronized void fadeHue(final double hue) {
        if (fadeThread != null) {
            fadeThread.halt();
        }
        fadeThread = new FadeThread(hue);
        fadeThread.start();
    }

    /**
     * Set the hue of the current video.
     *
     * @param hue The desired hue to fade to.
     */
    @Override
    public void setHue(final double hue) {
        this.hue = hue;
    }

    /**
     * Get the current hue of the video playing back.
     *
     * @return The hue value.
     */
    @Override
    public double getHue() {
        return hue;
    }

}
