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

import com.sun.jna.Memory;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import utils.PlatformUtils;

/**
 * A JavaFX rendered VLC window which is responsible for moving where it's told,
 * and playing video files. Transparent windows can then sit on top of this
 * giving the impression of a video background. This is a singleton since more
 * than one can cause native crashes - something we don't want to deal with
 * (hence this is hard-coded to just follow the projection window around.)
 * <p/>
 * @author Michael, Greg
 */
public class VLCWindowDirect extends VLCWindow {

    /**
     * Use this thread for all VLC media player stuff to keep this class thread
     * safe.
     */
    private static final ExecutorService VLC_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    protected static final VLCWindow DIRECT_INSTANCE = new VLCWindowDirect();

    private DirectMediaPlayerComponent mediaPlayer;
    private boolean hideButton;
    private boolean show;
    private boolean paused;
    private volatile boolean init;
    private String location;
    private volatile double hue = 0;

    private int WIDTH = 1920;
    private int HEIGHT = 1080;
    private BorderPane borderPane;
    private Canvas canvas;
    private PixelWriter pixelWriter;
    private WritablePixelFormat<ByteBuffer> pixelFormat;
    private BorderPane broderPane;
    private Stage stage;
    private Scene scene;
    private int[] intBuffer;
    private BufferFormat bufferFormat;
    private boolean canvasScaled = false;

    private VLCWindowDirect() {

        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.fxRunAndWait(new Runnable() {

                        @Override
                        public void run() {
                            stage = new Stage(StageStyle.UNDECORATED);
                            PlatformUtils.setFullScreenAlwaysOnTop(stage, true);
                            stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    if (newValue.booleanValue()) {
                                        //focused
                                        stage.toBack();
                                    } else {
                                        //not focused
                                    }
                                }
                            });
                            borderPane = new BorderPane();
                            borderPane.setStyle("-fx-background-color: black;");
                            canvas = new Canvas(1920, 1080);
                            canvas.getGraphicsContext2D().setFill(Color.BLACK);
                            borderPane.setCenter(canvas);
                            scene = new Scene(borderPane);
                            scene.setFill(Color.BLACK);
                            pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
                            pixelFormat = PixelFormat.getByteBgraPreInstance();

                        }
                    });
                    mediaPlayer = new MediaPlayerComponent();
                    mediaPlayer.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

                        @Override
                        public void finished(MediaPlayer mp) {
                            if (mediaPlayer.getMediaPlayer().subItemCount() > 0) {
                                String mrl = mediaPlayer.getMediaPlayer().subItems().remove(0);
                                mediaPlayer.getMediaPlayer().startMedia(mrl);
                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        canvas.setVisible(true);
                                    }
                                });
                            }
                        }
                    });
                    Utils.fxRunAndWait(new Runnable() {

                        @Override
                        public void run() {
                            stage.setScene(scene);

                        }
                    });
                    init = true;

                    LOGGER.log(Level.INFO, "Video initialised ok");
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, "Couldn't initialise video, almost definitely because VLC (or correct version of VLC) was not found.", ex);
                }
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
                            mediaPlayer.getMediaPlayer().setAdjustVideo(true);
                            mediaPlayer.getMediaPlayer().setHue((int) (hue * 360));
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    if (stage != null) {
                                        stage.toBack();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
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

    /**
     * Defines the Media player component to be used here
     */
    private class MediaPlayerComponent extends DirectMediaPlayerComponent {

        /**
         * Initializes the super constructor with new VLCBufferFormatCallback
         */
        public MediaPlayerComponent() {
            super(new VLCBufferFormatCallback());

        }
/*
        @Override
      public void display(Memory nativeBuffer) {
        ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
        pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getWidth()*4);
        //pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getWidth() * 4, WIDTH);
                    
      }*/
      
        /**
         * when the the component wants to get the render callback, a new render
         * callback adapter Class is created
         *
         * @return new custom render callback adapter
         */
        @Override
        protected RenderCallback onGetRenderCallback() {
            return new VLCRenderCallback();
        }

    }

    /**
     * Convenience function that scales the canvas to the proper aspect ratio.
     *
     * @return true if the canvas has been scaled, false otherwise
     */
    private boolean scaleCanvas() {
        if (mediaPlayer == null) {
            return false;
        }
        if (mediaPlayer.getMediaPlayer().isPlaying()) {
            Dimension d = mediaPlayer.getMediaPlayer().getVideoDimension();
            if (d == null) {
                return false;
            }
            double tWidth = d.getWidth();
            double tHeight = (d.getHeight() * WIDTH) / tWidth;
            if (tHeight > HEIGHT) {//scale width 
                tWidth = (tWidth * HEIGHT) / d.getHeight();
                tHeight = HEIGHT;
                canvas.setScaleX(tWidth / (double) WIDTH);
                canvas.setScaleY(1);
            } else {
                tWidth = WIDTH;
                canvas.setScaleX(1);
                canvas.setScaleY(tHeight / (double) HEIGHT);
            }
            stage.toBack();
            return true;
        } else {
            return false;
        }

    }

    /**
     * Private class that is a render callback. This class is responsible for
     * the actual rendering of frames.
     */
    private class VLCRenderCallback implements RenderCallback {

        /**
         * Constructor that initializes super class
         */
        private VLCRenderCallback() {
        }

        /**
         * Method that is called every frame that should be rendered
         *
         * @param mediaPlayer media player calling the method
         * @param rgbBuffer int array holding picture data
         */
        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffer, BufferFormat bufferFormat) {
            ByteBuffer byteBuffer = nativeBuffer[0].getByteBuffer(0, nativeBuffer[0].size());
            pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getWidth()*4);
        }
    }

    /**
     * Callback to get the buffer format to use for video playback.
     */
    private class VLCBufferFormatCallback implements BufferFormatCallback {

        /**
         * Implements get buffer format.
         *
         * @param sourceWidth The width of the video playing
         * @param sourceHeight The height of the video playing
         * @return Buffer Format describing how to render the video
         */
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            final int width;
            final int height;

            width = WIDTH;
            height = HEIGHT;

            return new RV32BufferFormat(width, height);
        }
    }

    @Override
    public void setRepeat(final boolean repeat) {
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("setRepeat() start");
                if (init) {
                    mediaPlayer.getMediaPlayer().setRepeat(repeat);
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
                        mediaPlayer.getMediaPlayer().prepareMedia(sanitisedPath);
                    } else {
                        mediaPlayer.getMediaPlayer().prepareMedia(sanitisedPath, Utils.splitVLCOpts(options));
                    }
                    if (stretch) {
                        mediaPlayer.getMediaPlayer().setAspectRatio(WIDTH + ":" + HEIGHT);
                    } else {
                        mediaPlayer.getMediaPlayer().setAspectRatio(null);
                    }
                }
//                System.out.println("load() end");
            }
        });
    }

    @Override
    public void play() {
        canvasScaled = false;
        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                //timeline.play();
            }
        });
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play() start");
                if (init) {
                    paused = false;
                    mediaPlayer.getMediaPlayer().start();
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            canvas.setVisible(true);
                        }
                    });
                }
//                System.out.println("play() end");
            }
        });
    }

    @Override
    public void play(final String vid, final String options, final boolean stretch) {
        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                //timeline.play();
            }
        });
        this.location = vid;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("play("+vid+") start");
                if (init) {
                    paused = false;
                    if (mediaPlayer.getMediaPlayer().isPlaying()) {
                        mediaPlayer.getMediaPlayer().stop();
                    }
                    if (options == null) {
                        mediaPlayer.getMediaPlayer().startMedia(vid);
                    } else {
                        mediaPlayer.getMediaPlayer().startMedia(vid, Utils.splitVLCOpts(options));
                    }
                    if (stretch) {
                        mediaPlayer.getMediaPlayer().setAspectRatio(WIDTH + ":" + HEIGHT);
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                canvas.setScaleX(1);
                                canvas.setScaleY(1);
                            }
                        });
                        canvasScaled = true;
                    } else {
                        mediaPlayer.getMediaPlayer().setAspectRatio(null);
                        canvasScaled = false;
                    }
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            canvas.setVisible(true);
                        }
                    });
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
        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                //timeline.pause();
            }
        });
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("pause() start");
                if (init) {
                    paused = true;
                    mediaPlayer.getMediaPlayer().pause();
                }
//                System.out.println("pause() end");
            }
        });
    }

    @Override
    public void stop() {

        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                //timeline.stop();
            }
        });
        location = null;
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("stop() start");
                if (init) {
                    paused = false;
                    mediaPlayer.getMediaPlayer().stop();
                    Utils.sleep(50); //make sure media player has had time to loop if looping
                    if (!mediaPlayer.getMediaPlayer().isPlaying()) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                canvas.setVisible(false);
                            }
                        });
                    }
                    //window to back
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
                    muteTemp = mediaPlayer.getMediaPlayer().isMute();
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
                    mediaPlayer.getMediaPlayer().mute(mute);
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
                    progressTemp = (double) mediaPlayer.getMediaPlayer().getTime() / mediaPlayer.getMediaPlayer().getLength();
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
                    mediaPlayer.getMediaPlayer().setPosition((float) percent);
                }
//                System.out.println("setProgressPercent() end");
            }
        });
    }
    private boolean isPlayingTemp;

    @Override
    public boolean isPlaying() {
        runOnVLCThreadAndWait(new Runnable() {
            @Override
            public void run() {
//                System.out.println("isPlaying() start");
                if (init) {
                    isPlayingTemp = mediaPlayer.getMediaPlayer().isPlaying();
                } else {
                    isPlayingTemp = false;
                }
//                System.out.println("isPlaying() end");
            }
        });
        return isPlayingTemp;
    }
    private boolean isPausedTemp;

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
                    mediaPlayer.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
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
                    show = true;
                    updateState();
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
                    show = false;
                    updateState();
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
                if (init) {
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
                if (init) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if ((!hideButton && show)) {
                                stage.show();
                            } else {
                                stage.hide();
                            }
                            stage.toBack();

                        }
                    });
                }
//                System.out.println("updateState() end");
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.setX(x);
                            stage.setY(y);

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
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            WIDTH = width;
                            HEIGHT = height;

                            stage.setWidth(width);
                            stage.setHeight(height);
                            canvas.setWidth(width);
                            canvas.setHeight(height);

                        }
                    });
                }
//                System.out.println("setsize() end");
            }
        });
    }
    private int tempX, tempY, tempWidth, tempHeight;
    private boolean showing;

    @Override
    public void refreshPosition() {
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
        runOnVLCThread(new Runnable() {
            @Override
            public void run() {
//                System.out.println("refreshPosition() start");
                if (init) {
                    if (showing) {
                        show();
                        setLocation(tempX, tempY);
                        setSize(tempWidth, tempHeight);
                    } else {
                        hide();
                    }
                }
//                System.out.println("refreshPosition() end");
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
        return mediaPlayer.getMediaPlayer().getTime();
    }

    @Override
    public long getTotal() {
        return mediaPlayer.getMediaPlayer().getLength();
    }

    @Override
    public int getVolume() {
        return mediaPlayer.getMediaPlayer().getVolume();
    }

    @Override
    public void setVolume(int volume) {
        mediaPlayer.getMediaPlayer().setVolume(volume);
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
    
    public void setWindowVisible(boolean visible) { 
    }
}
