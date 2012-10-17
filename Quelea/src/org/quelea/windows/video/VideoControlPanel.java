/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.video;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.quelea.utils.LoggerUtils;

/**
 * The control panel for displaying the video.
 * <p/>
 * @author Michael
 */
public class VideoControlPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Button play;
    private Button pause;
    private Button stop;
    private Button mute;
    private Slider positionSlider;
    private VideoStatusPanel vidStatusPanel;
    private Node videoArea;
    private List<Canvas> registeredCanvases;
    private ScheduledExecutorService executorService;
    private boolean pauseCheck;
    private String videoPath;

    /**
     * Create a new video control panel.
     */
    public VideoControlPanel() {

        executorService = Executors.newSingleThreadScheduledExecutor();
        play = new Button("",new ImageView(new Image("file:icons/play.png")));
        play.setDisable(true);
        play.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                playVideo();
            }
        });
        pause = new Button("",new ImageView(new Image("file:icons/pause.png")));
        pause.setDisable(true);
        pause.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                pauseVideo();
            }
        });
        stop = new Button("",new ImageView(new Image("file:icons/stop.png")));
        stop.setDisable(true);
        stop.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                stopVideo();
                positionSlider.setValue(0);
                vidStatusPanel.getTimeDisplay().setCurrentSeconds(0);
                vidStatusPanel.getTimeDisplay().setTotalSeconds(0);
            }
        });
        mute = new Button("",new ImageView(new Image("file:icons/mute.png")));
        mute.setDisable(true);
        mute.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                setMute(!getMute());
            }
        });
        positionSlider = new Slider(0, 1000, 0);
        positionSlider.setDisable(true);
        positionSlider.setValue(0);
        videoArea = new Group();
        setCenter(videoArea);
        registeredCanvases = new ArrayList<>();

        BorderPane controlPanel = new BorderPane();
        HBox sliderPanel = new HBox();
        sliderPanel.getChildren().add(positionSlider);
        controlPanel.setBottom(sliderPanel);
        HBox buttonPanel = new HBox();
        buttonPanel.getChildren().add(play);
        buttonPanel.getChildren().add(pause);
        buttonPanel.getChildren().add(stop);
        buttonPanel.getChildren().add(mute);
        controlPanel.setTop(buttonPanel);
        setTop(controlPanel);

        vidStatusPanel = new VideoStatusPanel();
        vidStatusPanel.getVolumeSlider().addRunner(new Runnable() {
            @Override
            public void run() {
                setVolume(vidStatusPanel.getVolumeSlider().getValue());
            }
        });
        setBottom(vidStatusPanel);
    }

    /**
     * Register a canvas to be controlled via this video control panel.
     * <p/>
     * @param canvas the canvas to control.
     */
    public void registerCanvas(final Canvas canvas) {
        registeredCanvases.add(canvas);
    }

    /**
     * Get a list of registered lyric canvases.
     * <p/>
     * @return a list of registered lyric canvases.
     */
    public List<Canvas> getRegisteredCanvases() {
        return registeredCanvases;
    }

    /**
     * Load the given video to be controlled via this panel.
     * <p/>
     * @param videoPath the video path to load.
     */
    public void loadVideo(String videoPath) {
        this.videoPath = videoPath;
    }

    /**
     * Play the loaded video.
     */
    public void playVideo() {
    }

    /**
     * Get the current time of the video.
     * <p/>
     * @return the current time of the video.
     */
    public long getTime() {
        return 0;
    }

    /**
     * Set the current time of the video.
     * <p/>
     * @param time the current time of the video.
     */
    public void setTime(long time) {
    }

    /**
     * Pause the currently playing video.
     */
    public void pauseVideo() {
    }

    /**
     * Stop the currently playing video.
     */
    public void stopVideo() {
    }

    /**
     * Set whether the video is muted.
     * <p/>
     * @param muteState true to mute, false to unmute.
     */
    public void setMute(boolean muteState) {
    }

    /**
     * Determine if this video is muted.
     * <p/>
     * @return true if muted, false if not.
     */
    public boolean getMute() {
        return false;
    }

    /**
     * Set the volume of the video.
     * <p/>
     * @param volume the video volume.
     */
    public void setVolume(double volume) {
    }

    /**
     * Close down all the players controlled via this control panel and stop the
     * external VM's / remote players it controls.
     */
    public void close() {
    }

    /**
     * Try and stop and clear up if we haven't already.
     * <p/>
     * @throws Throwable if something goes wrong.
     */
    @Override
    protected void finalize() throws Throwable {
        stopVideo();
        super.finalize();
        close();
    }
}
