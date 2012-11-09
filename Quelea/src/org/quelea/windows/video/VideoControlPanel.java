/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.javafx.dialog.Dialog;
import org.quelea.displayable.VideoDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

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
    private List<Canvas> registeredCanvases;
    private String videoPath;
    private MediaView view;
    private MediaPlayer player;

    /**
     * Create a new video control panel.
     */
    public VideoControlPanel() {
        view = new MediaView();
        view.setSmooth(true);
        play = new Button("", new ImageView(new Image("file:icons/play.png")));
//        play.setDisable(true);
        play.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                player.play();
            }
        });
        pause = new Button("", new ImageView(new Image("file:icons/pause.png")));
//        pause.setDisable(true);
        pause.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                player.pause();
            }
        });
        stop = new Button("", new ImageView(new Image("file:icons/stop.png")));
//        stop.setDisable(true);
        stop.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                player.stop();
                positionSlider.setValue(0);
                vidStatusPanel.getTimeDisplay().setCurrentSeconds(0);
                vidStatusPanel.getTimeDisplay().setTotalSeconds(0);
            }
        });
        mute = new Button("", new ImageView(new Image("file:icons/mute.png")));
//        mute.setDisable(true);
        mute.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                player.setMute(!player.isMute());
            }
        });
        positionSlider = new Slider(0, 1000, 0);
        positionSlider.setDisable(true);
        positionSlider.setValue(0);
        setCenter(view);
        registeredCanvases = new ArrayList<>();

        VBox controlPanel = new VBox();
        HBox sliderPanel = new HBox();
        sliderPanel.getChildren().add(positionSlider);
        HBox buttonPanel = new HBox();
        buttonPanel.getChildren().add(play);
        buttonPanel.getChildren().add(pause);
        buttonPanel.getChildren().add(stop);
        buttonPanel.getChildren().add(mute);
        controlPanel.getChildren().add(buttonPanel);
        controlPanel.getChildren().add(sliderPanel);
        setTop(controlPanel);

        vidStatusPanel = new VideoStatusPanel();
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
    public void loadVideo(VideoDisplayable video) {
        this.videoPath = video.getFile().getAbsolutePath();
        try {
            player = new MediaPlayer(new Media(new File(videoPath).toURI().toString()));
            view.setMediaPlayer(player);
            player.play();
        }
        catch(MediaException ex) {
            LOGGER.log(Level.WARNING, "Video Error", ex);
            MediaException.Type type = ex.getType();
            switch(type) {
                case MEDIA_UNSUPPORTED:
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("video.error.title"), LabelGrabber.INSTANCE.getLabel("video.error.unsupported"));
                    break;
                default:
                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("video.error.title"), LabelGrabber.INSTANCE.getLabel("video.error.general"));
            }
        }
    }
}
