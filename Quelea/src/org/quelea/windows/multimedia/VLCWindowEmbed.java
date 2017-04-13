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
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayStage;
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
public class VLCWindowEmbed extends VLCWindow {
    
    /**
     * Use this thread for all VLC media player stuff to keep this class thread
     * safe.
     */
    private static final ExecutorService VLC_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    protected static final VLCWindow EMBED_INSTANCE = new VLCWindowEmbed();
    private JFrame frame;
    private Canvas canvas;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer = null;
    private CanvasVideoSurface videoSurface;
    private boolean paused;
    private volatile boolean init;
    private String location;
    private volatile double hue = 0;
    private boolean disposeFrame = false;

    static public boolean setFullScreen(final JFrame frame, boolean doPack) {
        
        // ops! the projector is not running fullscreen, so just display it as it
        if (QueleaProperties.get().isProjectorModeCoords()) {
            frame.setUndecorated(true);
            frame.setVisible(true);
            return true;
        }
            
        int projectorScreen = QueleaProperties.get().getProjectorScreen();
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        if (projectorScreen < 0 || projectorScreen >= gs.length) {
            projectorScreen = 0;
        }
        
        GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
        boolean result = device.isFullScreenSupported();

        frame.dispose();
        
        // this code works well on linux, but not on windows
        if (result && Utils.isLinux()) {
            frame.setUndecorated(true);
            frame.setResizable(true);
            frame.setAlwaysOnTop(true);

            frame.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent arg0) {
                    //frame.setAlwaysOnTop(true);
                }

                @Override
                public void focusLost(FocusEvent arg0) {
                    //frame.setAlwaysOnTop(false);
                }
            });

            if (doPack)
                frame.pack();

            gs[projectorScreen].setFullScreenWindow(frame);
        }
        // this code works well on windows, not on linux
        else {
            frame.setUndecorated(true);
            
            frame.setPreferredSize(gs[projectorScreen].getDefaultConfiguration().getBounds().getSize());
            frame.setLocation(gs[projectorScreen].getDefaultConfiguration().getBounds().x, frame.getY());

            if (doPack)
                frame.pack();

            frame.setResizable(true);

            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            boolean successful = frame.getExtendedState() == Frame.MAXIMIZED_BOTH;

            frame.setVisible(true);

            if (!successful)
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        return result;
    }
    
    private VLCWindowEmbed() {

        runOnVLCThread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    frame = new JFrame();
                    frame.setBackground(Color.BLACK);
                    frame.setType(JFrame.Type.UTILITY);
                    frame.setTitle(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
                    
                    canvas = new Canvas();
                    canvas.setBackground(Color.BLACK);
                });
                
                mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show", "--mouse-hide-timeout=0", "--no-xlib");
                videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
                createMediaPlayer();
                
                SwingUtilities.invokeAndWait(() -> {
                    frame.add(canvas);
                    setFullScreen(frame, false);
                    frame.toBack();
                });
                
                init = true;
                LOGGER.log(Level.INFO, "Video initialised ok");
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "Couldn't initialise video, almost definitely because VLC (or correct version of VLC) was not found.", ex);
            }
        });
        ScheduledExecutorService exc = Executors.newSingleThreadScheduledExecutor();
        exc.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (init) {
                    runOnVLCThread(new Runnable() {
                        @Override
                        public void run() {
                            mediaPlayer.setAdjustVideo(true);
                            mediaPlayer.setHue((int) (hue * 360));
                        }
                    });
                }
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
    }
    
    private void createMediaPlayer() {        
        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        mediaPlayer.setVideoSurface(videoSurface);
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void finished(MediaPlayer mp) {
                if (mediaPlayer.subItemCount() > 0) {
                    String mrl = mediaPlayer.subItems().remove(0);
                    mediaPlayer.playMedia(mrl);
                }
            }
        });
    }

    /**
     * Determine if VLC has initialised correctly.
     * <p>
     * @return true if it has, false if it hasn't because something went wrong
     * (the most likely cause is an outdated version.)
     */
    @Override
    public boolean isInit() {
        runOnVLCThreadAndWait(new Runnable() {

            @Override
            public void run() {
                //Just to block until construction has finished!
            }
        });
        return init;
    }

    @Override
    public void setRepeat(final boolean repeat) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setRepeat() start");
                if (init) {
                    mediaPlayer.setRepeat(repeat);
                }
//                System.out.println("setRepeat() end");
            }
        });
    }

    @Override
    public void load(final String path, final String options, final boolean stretch) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("load("+path+") start");                
                if (init) {
                    paused = false;
                    String sanitisedPath = path;
                    sanitisedPath = sanitisedPath.trim();
                    if (sanitisedPath.startsWith("www")) {
                        sanitisedPath = "http://" + sanitisedPath;
                    }
                    if (options == null) {
                        mediaPlayer.prepareMedia(sanitisedPath);
                    } else {
                        mediaPlayer.prepareMedia(sanitisedPath, Utils.splitVLCOpts(options));
                    }
                    if (stretch) {
                        mediaPlayer.setAspectRatio(canvas.getWidth() + ":" + canvas.getHeight());
                    } else {
                        mediaPlayer.setAspectRatio(null);
                    }
                }
//                System.out.println("load() end");
            }
        });
    }

    @Override
    public void play() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play() start");
                if (init) {
                    paused = false;
                    mediaPlayer.play();
                }
//                System.out.println("play() end");
            }
        });
    }

    @Override
    public void play(final String vid, final String options, final boolean stretch) {
        this.location = vid;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play("+vid+") start");
                if (init) {
                    paused = false;
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    if (options == null) {
                        mediaPlayer.playMedia(vid);
                    } else {
                        mediaPlayer.playMedia(vid, Utils.splitVLCOpts(options));
                    }
                    if (stretch) {
                        mediaPlayer.setAspectRatio(canvas.getWidth() + ":" + canvas.getHeight());
                    } else {
                        mediaPlayer.setAspectRatio(null);
                    }
                }
//                System.out.println("play(arg) end");
            }
        });
    }

    @Override
    public String getLastLocation() {
        return location;
    }

    @Override
    public void pause() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("pause() start");
                if (init) {
                    paused = true;
                    mediaPlayer.pause();
                }
//                System.out.println("pause() end");
            }
        });
    }

    @Override
    public void stop() {
        location = null;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("stop() start");
                if (init) {
                    paused = false;
                    mediaPlayer.stop();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            //frame.toBack();
                        }
                    });
                }
//                System.out.println("stop() end");
            }
        });
    }
    private boolean muteTemp;

    @Override
    public boolean isMute() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isMute() start");
                if (init) {
                    muteTemp = mediaPlayer.isMute();
                } else {
                    muteTemp = false;
                }
//                System.out.println("isMute() end");
            }
        });
        return muteTemp;
    }

    @Override
    public void setMute(final boolean mute) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setMute() start");
                if (init) {
                    mediaPlayer.mute(mute);
                }
//                System.out.println("setMute() end");
            }
        });
    }
    private double progressTemp;

    @Override
    public double getProgressPercent() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("getProgressPercent() start");
                if (init) {
                    progressTemp = (double) mediaPlayer.getTime() / mediaPlayer.getLength();
                } else {
                    progressTemp = 0;
                }
//                System.out.println("getProgressPercent() end");
            }
        });
        return progressTemp;
    }

    @Override
    public void setProgressPercent(final double percent) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setProgressPercent() start");
                if (init) {
                    mediaPlayer.setPosition((float) percent);
                }
//                System.out.println("setProgressPercent() end");
            }
        });
    }
    private volatile boolean isPlayingTemp;

    @Override
    public boolean isPlaying() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isPlaying() start");
                if (init) {
                    isPlayingTemp = mediaPlayer.isPlaying();
                } else {
                    isPlayingTemp = false;
                }
//                System.out.println("isPlaying() end");
            }
        });
        return isPlayingTemp;
    }
    private volatile boolean isPausedTemp;

    @Override
    public boolean isPaused() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isPaused() start");
                if (init) {
                    isPausedTemp = paused;
                } else {
                    isPausedTemp = false;
                }
//                System.out.println("isPaused() end");
            }
        });
        return isPausedTemp;
    }

    @Override
    public void setOnFinished(final Runnable onFinished) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setOnFinished() start");
                if (init) {
                    paused = false;
                    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                        @Override
                        public void finished(MediaPlayer mediaPlayer) {
                            if (mediaPlayer.subItemCount() == 0) {
                                onFinished.run();
                            }
                        }
                    });
                }
//                System.out.println("setOnFinished() end");
            }
        });
    }

    @Override
    public void show() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("show() start");                
                if (init) {
                    if (!frame.isVisible()) {
                        frame.setVisible(true);
                    }
                    setWindowVisible(true);
//                    frame.setVisible(true);
                }
//                System.out.println("show() end");
            }
        });
    }

    @Override
    public void hide() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("hide() start");
                if (init) {
//                    setWindowVisible(false);
                    frame.setVisible(false);
                }
//                System.out.println("hide() end");
            }
        });
    }

    @Override
    public void setHideButton(final boolean hide) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setHideButton() start");
//                if (init) {
//                    hideButton = hide;
//                    updateState();
//                }
//                System.out.println("setHideButton() end");
            }
        });
    }

    /**
     * Called when a video is going live or when a live video is going away
     * @param visible if true, the video is going live, else its going away
     */
    @Override
    public void setWindowVisible(boolean visible) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            if (visible) {
                                frame.toFront();
                                Platform.runLater(() -> {
                                    QueleaApp.get().getProjectionWindow().toFront();
                                });
                            }
                            else {
                                frame.toBack();
                            }
                        }
                    });
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, "Couldn't initialise video, almost definitely because VLC (or correct version of VLC) was not found.", ex);
                }
            }
        });
            
    }
    
    @Override
    public void setLocation(final int x, final int y) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setLocation() start");
                if (init) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            frame.setLocation(x, y);
                        }
                    });
                }
//                System.out.println("setLocation() end");
            }
        });
    }

    @Override
    public void setSize(final int width, final int height) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setsize() start");
                if (init) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            frame.setSize(width, height);
                        }
                    });
                }
//                System.out.println("setsize() end");
            }
        });
    }
    private int tempX, tempY, tempWidth, tempHeight;
    private boolean showing;

    private void refreshPositionImmediate() {
        Utils.fxRunAndWait(new Runnable() {
            @Override
            public void run() {
                showing = QueleaApp.get().getProjectionWindow().isShowing();
                if (showing) {
                    tempX = (int) QueleaApp.get().getProjectionWindow().getX();
                    tempY = (int) QueleaApp.get().getProjectionWindow().getY();
                    tempWidth = (int) QueleaApp.get().getProjectionWindow().getWidth();
                    tempHeight = (int) QueleaApp.get().getProjectionWindow().getHeight();
                }
            }
        });
        
//      System.out.println("refreshPosition() start");
        if (init) {
            if (showing) {
                show();
                setLocation(tempX, tempY);
                setSize(tempWidth, tempHeight);
            } else {
                hide();
            }
        }
//      System.out.println("refreshPosition() end");
    }
    
    @Override
    public void refreshPosition() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                refreshPositionImmediate();
            }
        });
    }

    private FadeThread fadeThread;

    private class FadeThread extends Thread {

        private static final double INCREMENT = 0.002;
        private double toVal;
        private volatile boolean go = true;

        public FadeThread(double toVal) {
            this.toVal = toVal;
        }

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

    @Override
    public long getTime() {
        return mediaPlayer.getTime();
    }

    @Override
    public long getTotal() {
        return mediaPlayer.getLength();
    }

    @Override
    public int getVolume() {
        return mediaPlayer.getVolume();
    }

    @Override
    public void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    /**
     * Run the specified runnable on the VLC thread. All VLC actions should be
     * executed on this thread to avoid threading issues.
     * <p/>
     * @param r the runnable to run.
     */
    private void runOnVLCThread(Runnable r) {
        VLC_EXECUTOR.submit(r);
    }

    /**
     * Run the specified runnable on the VLC thread and wait for it to complete.
     * All VLC actions should be executed on this thread to avoid threading
     * issues.
     * <p/>
     * @param r the runnable to run.
     */
    private void runOnVLCThreadAndWait(Runnable r) {
        try {
            VLC_EXECUTOR.submit(r).get();
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.WARNING, "Interrupted or execution error", ex);
        }
    }
}
