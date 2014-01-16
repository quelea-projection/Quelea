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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
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
public class VLCWindow {

    /**
     * Use this thread for all VLC media player stuff to keep this class thread
     * safe.
     */
    private static final ExecutorService VLC_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final VLCWindow INSTANCE = new VLCWindow();
    private Window window;
    private Canvas canvas;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;
    private boolean hideButton;
    private boolean show;
    private boolean paused;
    private boolean init;
    private String location;

    private VLCWindow() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                try {
                    window = new Window(null);
                    window.setBackground(Color.BLACK);
                    canvas = new Canvas();
                    canvas.setBackground(Color.BLACK);
                    mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show");
                    mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
                    CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
                    mediaPlayer.setVideoSurface(videoSurface);
                    window.add(canvas);
                    show = true;
                    window.setVisible(true);
                    window.toBack();
                    init = true;
                    LOGGER.log(Level.INFO, "Video initialised ok");
                }
                catch(Exception ex) {
                    LOGGER.log(Level.INFO, "Couldn't initialise video, almost definitely because VLC (or correct version of VLC) was not found.", ex);
                }
            }
        });
    }

    public void setRepeat(final boolean repeat) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setRepeat() start");
                if(init) {
                    mediaPlayer.setRepeat(repeat);
                }
//                System.out.println("setRepeat() end");
            }
        });
    }

    public void load(final String path) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("load() start");
                if(init) {
                    paused = false;
                    mediaPlayer.prepareMedia(path);
                }
//                System.out.println("load() end");
            }
        });
    }

    public void play() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play() start");
                if(init) {
                    paused = false;
                    mediaPlayer.play();
                }
//                System.out.println("play() end");
            }
        });
    }

    public void play(final String vid) {
        this.location = vid;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play(arg) start");
                if(init) {
                    paused = false;
                    mediaPlayer.playMedia(vid);
                }
//                System.out.println("play(arg) end");
            }
        });
    }
    
    public String getLastLocation() {
        return location;
    }

    public void pause() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("pause() start");
                if(init) {
                    paused = true;
                    mediaPlayer.pause();
                }
//                System.out.println("pause() end");
            }
        });
    }

    public void stop() {
        location = null;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("stop() start");
                if(init) {
                    paused = false;
                    mediaPlayer.stop();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            window.toBack();
                        }
                    });
                }
//                System.out.println("stop() end");
            }
        });
    }
    private boolean muteTemp;

    public boolean isMute() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isMute() start");
                if(init) {
                    muteTemp = mediaPlayer.isMute();
                }
                else {
                    muteTemp = false;
                }
//                System.out.println("isMute() end");
            }
        });
        return muteTemp;
    }

    public void setMute(final boolean mute) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setMute() start");
                if(init) {
                    mediaPlayer.mute(mute);
                }
//                System.out.println("setMute() end");
            }
        });
    }
    private double progressTemp;

    public double getProgressPercent() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("getProgressPercent() start");
                if(init) {
                    progressTemp = (double) mediaPlayer.getTime() / mediaPlayer.getLength();
                }
                else {
                    progressTemp = 0;
                }
//                System.out.println("getProgressPercent() end");
            }
        });
        return progressTemp;
    }

    public void setProgressPercent(final double percent) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setProgressPercent() start");
                if(init) {
                    mediaPlayer.setPosition((float) percent);
                }
//                System.out.println("setProgressPercent() end");
            }
        });
    }
    private boolean isPlayingTemp;

    public boolean isPlaying() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isPlaying() start");
                if(init) {
                    isPlayingTemp = mediaPlayer.isPlaying();
                }
                else {
                    isPlayingTemp = false;
                }
//                System.out.println("isPlaying() end");
            }
        });
        return isPlayingTemp;
    }
    private boolean isPausedTemp;

    public boolean isPaused() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isPaused() start");
                if(init) {
                    isPausedTemp = paused;
                }
                else {
                    isPausedTemp = false;
                }
//                System.out.println("isPaused() end");
            }
        });
        return isPausedTemp;
    }

    public void setOnFinished(final Runnable onFinished) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setOnFinished() start");
                if(init) {
                    paused = false;
                    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                        @Override
                        public void finished(MediaPlayer mediaPlayer) {
                            onFinished.run();
                        }
                    });
                }
//                System.out.println("setOnFinished() end");
            }
        });
    }

    public void show() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("show() start");
                if(init) {
                    show = true;
                    updateState();
                }
//                System.out.println("show() end");
            }
        });
    }

    public void hide() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("hide() start");
                if(init) {
                    show = false;
                    updateState();
                }
//                System.out.println("hide() end");
            }
        });
    }

    public void setHideButton(final boolean hide) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setHideButton() start");
                if(init) {
                    hideButton = hide;
                    updateState();
                }
//                System.out.println("setHideButton() end");
            }
        });
    }

    private void updateState() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("updateState() start");
                if(init) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            window.setOpacity((hideButton || !show) ? 0 : 1);
                            window.toBack();
                        }
                    });
                }
//                System.out.println("updateState() end");
            }
        });
    }

    public void setLocation(final int x, final int y) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setLocation() start");
                if(init) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            window.setLocation(x, y);
                        }
                    });
                }
//                System.out.println("setLocation() end");
            }
        });
    }

    public void setSize(final int width, final int height) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setsize() start");
                if(init) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            window.setSize(width, height);
                        }
                    });
                }
//                System.out.println("setsize() end");
            }
        });
    }
    private int tempX, tempY, tempWidth, tempHeight;
    private boolean showing;

    public void refreshPosition() {
        Utils.fxRunAndWait(new Runnable() {
            @Override
            public void run() {
                showing = QueleaApp.get().getProjectionWindow().isShowing();
                if(showing) {
                    tempX = (int) QueleaApp.get().getProjectionWindow().getX();
                    tempY = (int) QueleaApp.get().getProjectionWindow().getY();
                    tempWidth = (int) QueleaApp.get().getProjectionWindow().getWidth();
                    tempHeight = (int) QueleaApp.get().getProjectionWindow().getHeight();
                }
            }
        });
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("refreshPosition() start");
                if(init) {
                    if(showing) {
                        show();
                        setLocation(tempX, tempY);
                        setSize(tempWidth, tempHeight);
                    }
                    else {
                        hide();
                    }
                }
//                System.out.println("refreshPosition() end");
            }
        });
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
        }
        catch(InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.WARNING, "Interrupted or execution error", ex);
        }
    }
}
