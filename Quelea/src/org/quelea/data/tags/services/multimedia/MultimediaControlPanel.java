package org.quelea.data.tags.services.multimedia;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author tomaszpio@gmail.com
 */
public abstract class MultimediaControlPanel extends BorderPane {

    protected static final Logger LOGGER = LoggerUtils.getLogger();
    protected String filePath;
    protected Button mute;
    protected Button pause;
    protected Button play;
    protected MediaPlayer player;
    protected Slider positionSlider;
    protected List<Canvas> registeredCanvases;
    protected Button stop;
    protected MediaView view;

    protected class CurrentTimeListener implements InvalidationListener {

        public CurrentTimeListener() {
        }
        
        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    final Duration currentTime = player.getCurrentTime();
                    updatePositionSlider(currentTime);
                }
            });
        }
    }

    protected class PositionListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) {
            if (oldValue && !newValue) {
                double pos = positionSlider.getValue();
                final Duration seekTo = player.getTotalDuration().multiply(pos);
                seekAndUpdatePosition(seekTo);
            }
        }
    }

    public MultimediaControlPanel() {

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
        positionSlider = new Slider(0, 1.0, 0.1);
        positionSlider.setDisable(false);
        positionSlider.setValue(0);
        positionSlider.valueChangingProperty().addListener(new PositionListener());
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
     * @param multimedia the video path to load.
     */
    public abstract void loadMultimedia(MultimediaDisplayable displayable);

    /**
     * Register a canvas to be controlled via this video control panel.
     * <p/>
     * @param canvas the canvas to control.
     */
    public void registerCanvas(final Canvas canvas) {
        registeredCanvases.add(canvas);
    }

    protected void seekAndUpdatePosition(Duration duration) {
        if (player.getStatus() == Status.STOPPED) {
            player.pause();
        }
        player.seek(duration);
        if (player.getStatus() != Status.PLAYING) {
            updatePositionSlider(duration);
        }
    }

    protected void updatePositionSlider(Duration currentTime) {
        if (positionSlider.isValueChanging()) {
            return;
        }
        final Duration total = player.getTotalDuration();
        if (total == null || currentTime == null) {
            positionSlider.setValue(0);
        } else {
            positionSlider.setValue(currentTime.toMillis() / total.toMillis());
        }
    }
}
