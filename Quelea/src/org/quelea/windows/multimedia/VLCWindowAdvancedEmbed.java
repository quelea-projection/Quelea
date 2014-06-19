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

    public static final VLCWindowAdvancedEmbed INSTANCE = new VLCWindowAdvancedEmbed();

    //construct a black background, and two players to fade between
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
        init = true;
        updateState();
    }

    /**
     * Determine if VLC has initialised correctly.
     * <p>
     * @return true if it has, false if it hasn't because something went wrong
     * (the most likely cause is an outdated version.)
     */
    public boolean isInit() {
        return init;
    }

    //transition between players
    public void transitionBetween() {
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
                } catch (InterruptedException ex) {
                    Logger.getLogger(VLCWindowAdvancedEmbed.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
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

    //fades out active player and pauses
    public void fadeOutActive(final boolean active) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                for (double i = 0; i < 100; i++) {

                    boolean logicBool = isPlayer1;
                    if (!active) {
                        logicBool = !logicBool;
                    }
                    if (logicBool) {

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
                if (isPlayer1) {
                    player2.pause();
                } else {
                    player.pause();
                }
            }

        });

    }
//fades up active player and resumes

    public void fadeUpActive(boolean resumePlayback) {

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
        if (resumePlayback) {
            if (isPlayer1) {
                player.play();
            } else {
                player2.play();
            }
        }
    }

    //set repeat for the active player
    //may be broken for some instances 
    public void setRepeat(final boolean repeat) {
        if (isPlayer1) {
            player.setRepeat(repeat);
        } else {
            player2.setRepeat(repeat);
        }

    }

    //load video
    //other Quelea code calls stop before load usually, so this method loads a video in the other
    //player, and upon play, the players get switched to show the active player.
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
//plays the loaded video and transitions to active video player

    public void play() {

        if (init) {
            isPlayer1 = !isPlayer1;
            transitionBetween();
            if (isPlayer1) {
                player.play();
            } else {
                player2.play();
            }

        }

    }
//plays a video from a file, and transitions to active video player

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

        }

    }

    public String getLastLocation() {
        return location;
    }

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
//stop fades off, so it looks nice when loading a new video

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
    private boolean muteTemp;

    //gets boolean showing mute from main player
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

    //sets mute for active video player
    public void setMute(final boolean mute) {

        if (init) {
            if (isPlayer1) {
                player.setMute(mute);
            } else {
                player2.setMute(mute);
            }

        }

    }
    private double progressTemp;

    //gets progress in percent, may be broken in some instances
    public double getProgressPercent() {

        if (init) {
            if (isPlayer1) {
                progressTemp = (double) player.getTime() / player.getLength();
            } else {
                progressTemp = (double) player.getTime() / player.getLength();
            }

        } else {
            progressTemp = 0;
        }

        return progressTemp;
    }

    //set progress percent for active player
    public void setProgressPercent(final double percent) {

        if (init) {
            if (isPlayer1) {
                player.setPosition((float) percent);
            } else {
                player2.setPosition((float) percent);
            }

        }

    }
    private boolean isPlayingTemp;

    //return is playing
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
    private boolean isPausedTemp;

    public boolean isPaused() {

        if (init) {
            isPausedTemp = paused;
        } else {
            isPausedTemp = false;
        }

        return isPausedTemp;
    }

//needs to be adapted somehow to acommodate out of process playback (any ideas??????)
    public void setOnFinished(final Runnable onFinished) {

//        runOnVLCThread(new Runnable() {
//            @Override
//            public void run() {
////                System.out.println("setOnFinished() start");
//                if (init) {
//                    paused = false;
//                    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
//                        @Override
//                        public void finished(MediaPlayer mediaPlayer) {
//                            if (mediaPlayer.subItemCount() == 0) {
//                                onFinished.run();
//                            }
//                        }
//                    });
//                }
////                System.out.println("setOnFinished() end");
//            }
//        });
    }

    public void show() {

        if (init) {
            show = true;
            updateState();
        }

    }

    public void hide() {

        if (init) {
            show = false;
            updateState();
        }
//              
    }

    public void setHideButton(final boolean hide) {

        if (init) {
            hideButton = hide;
            updateState();
        }

    }

    private void updateState() {

        if (init) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
//                    if (isPlayer1) {
//                          player.setOpacity((hideButton || !show) ? 0 : 1);
//
//                    } else {
//                         player2.setOpacity((hideButton || !show) ? 0 : 1);
//
//                    }
                    window.toBack();

                }
            });
        }

    }

    public void setLocation(final int x, final int y) {

        player.setLocation(x, y);

        player2.setLocation(x, y);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                window.setLocation(x, y);
            }
        });

    }

    public void setSize(final int width, final int height) {

        player.setSize(width, height);

        player2.setSize(width, height);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                window.setSize(width, height);
                canvas.setSize(width, height);
            }
        });

    }
    private int tempX, tempY, tempWidth, tempHeight;
    private boolean showing;

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
//       
    }

    private FadeThread fadeThread;

    private class FadeThread extends Thread {

        private static final double INCREMENT = 0.002;
        private double toVal;
        private volatile boolean go = true;

        public FadeThread(double toVal) {
            this.toVal = toVal;
        }

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

        public void halt() {
            go = false;
        }

    }

    public synchronized void fadeHue(final double hue) {
        if (fadeThread != null) {
            fadeThread.halt();
        }
        fadeThread = new FadeThread(hue);
        fadeThread.start();
    }

    public void setHue(final double hue) {
        this.hue = hue;
    }

    public double getHue() {
        return hue;
    }

}
