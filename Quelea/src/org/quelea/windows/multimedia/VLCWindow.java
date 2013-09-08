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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                if(init) {
                    mediaPlayer.setRepeat(repeat);
                }
            }
        });
    }

    public void load(final String path) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = false;
                    mediaPlayer.prepareMedia(path);
                }
            }
        });
    }

    public void play() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = false;
                    mediaPlayer.play();
                }
            }
        });
    }

    public void play(final String vid) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = false;
                    mediaPlayer.playMedia(vid);
                }
            }
        });
    }

    public void pause() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = true;
                    mediaPlayer.pause();
                }
            }
        });
    }

    public void stop() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = false;
                    mediaPlayer.stop();
                }
            }
        });
    }
    private boolean muteTemp;

    public boolean isMute() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    muteTemp = mediaPlayer.isMute();
                }
                else {
                    muteTemp = false;
                }
            }
        });
        return muteTemp;
    }

    public void setMute(final boolean mute) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    mediaPlayer.mute(mute);
                }
            }
        });
    }
    private double progressTemp;

    public double getProgressPercent() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    progressTemp = (double) mediaPlayer.getTime() / mediaPlayer.getLength();
                }
                else {
                    progressTemp = 0;
                }
            }
        });
        return progressTemp;
    }

    public void setProgressPercent(final double percent) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    mediaPlayer.setPosition((float) percent);
                }
            }
        });
    }
    private boolean isPlayingTemp;

    public boolean isPlaying() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    isPlayingTemp = mediaPlayer.isPlaying();
                }
                else {
                    isPlayingTemp = false;
                }
            }
        });
        return isPlayingTemp;
    }
    private boolean isPausedTemp;

    public boolean isPaused() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    isPausedTemp = paused;
                }
                else {
                    isPausedTemp = false;
                }
            }
        });
        return isPausedTemp;
    }

    public void setOnFinished(final Runnable onFinished) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    paused = false;
                    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                        @Override
                        public void finished(MediaPlayer mediaPlayer) {
                            onFinished.run();
                        }
                    });
                }
            }
        });
    }

    public void show() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    show = true;
                    updateState();
                }
            }
        });
    }

    public void hide() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    show = false;
                    updateState();
                }
            }
        });
    }

    public void setHideButton(final boolean hide) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    hideButton = hide;
                    updateState();
                }
            }
        });
    }

    private void updateState() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    window.setOpacity((hideButton || !show) ? 0 : 1);
                }
            }
        });
    }

    public void setLocation(final int x, final int y) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    window.setLocation(x, y);
                }
            }
        });
    }

    public void setSize(final int width, final int height) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    window.setSize(width, height);
                }
            }
        });
    }
    private int tempX, tempY, tempWidth, tempHeight;

    public void refreshPosition() {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                if(init) {
                    Utils.fxRunAndWait(new Runnable() {
                        @Override
                        public void run() {
                            tempX = (int) QueleaApp.get().getProjectionWindow().getX();
                            tempY = (int) QueleaApp.get().getProjectionWindow().getY();
                            tempWidth = (int) QueleaApp.get().getProjectionWindow().getWidth();
                            tempHeight = (int) QueleaApp.get().getProjectionWindow().getHeight();
                        }
                    });
                    setLocation(tempX, tempY);
                    setSize(tempWidth, tempHeight);
                }
            }
        });
    }

    /**
     * Run the specified runnable on the VLC thread. All VLC actions should be
     * executed this way to avoid threading issues.
     * <p/>
     * @param r the runnable to run.
     */
    private void runOnVLCThread(Runnable r) {
        VLC_EXECUTOR.submit(r);
    }
}
