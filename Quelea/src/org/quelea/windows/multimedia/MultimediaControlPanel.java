package org.quelea.windows.multimedia;

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
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author tomaszpio@gmail.com
 */
public abstract class MultimediaControlPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    protected String filePath;
    protected Button mute;
    protected Button pause;
    protected Button play;
//    protected VLCMediaPlayer player;
    protected Slider positionSlider;
    protected Slider volumeSlider;
    protected Button stop;

    protected class CurrentTimeListener implements InvalidationListener {

        public CurrentTimeListener() {
        }

        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
//                    updatePositionSlider(player.getCurrentTime(), player.getTotalTime());
                }
            });
        }
    }

    public void clear() {
    }

//    public VLCMediaPlayer getPlayer() {
//        return player;
//    }

    protected class PositionListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) {
            if(oldValue && !newValue) {
                double pos = positionSlider.getValue();
//                long seekTo= (long)(player.getTotalTime()*pos);
//                seekAndUpdatePosition(seekTo);
            }
        }
    }

    protected class VolumeListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) {
            if(oldValue && !newValue) {
                double pos = volumeSlider.getValue();
//                player.setVolume(pos);
            }
        }
    }

    public MultimediaControlPanel() {
        play = new Button("", new ImageView(new Image("file:icons/play.png")));
//        play.setDisable(true);
        play.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
//                player.play();
            }
        });
        pause = new Button("", new ImageView(new Image("file:icons/pause.png")));
//        pause.setDisable(true);
        pause.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
//                player.pause();
            }
        });
        stop = new Button("", new ImageView(new Image("file:icons/stop.png")));
//        stop.setDisable(true);
        stop.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
//                player.stop();
                positionSlider.setValue(0);
            }
        });
        mute = new Button("", new ImageView(new Image("file:icons/mute.png")));
//        mute.setDisable(true);
        mute.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
//                player.setMute(!player.isMute());
            }
        });
        positionSlider = new Slider(0, 1.0, 0.1);
        positionSlider.setDisable(false);
        positionSlider.setValue(0);
        positionSlider.valueChangingProperty().addListener(new PositionListener());

        volumeSlider = new Slider(0, 1.0, 0.1);
        volumeSlider.setValue(0);
        volumeSlider.valueChangingProperty().addListener(new VolumeListener());

        VBox controlPanel = new VBox();
        VBox sliderPanel = new VBox();
        sliderPanel.getChildren().add(positionSlider);
        sliderPanel.getChildren().add(volumeSlider);
        HBox buttonPanel = new HBox();
        buttonPanel.getChildren().add(play);
        buttonPanel.getChildren().add(pause);
        buttonPanel.getChildren().add(stop);
        buttonPanel.getChildren().add(mute);
        controlPanel.getChildren().add(buttonPanel);
        controlPanel.getChildren().add(sliderPanel);
        setCenter(controlPanel);
    }

    /**
     * Load the given video to be controlled via this panel.
     * <p/>
     * @param multimedia the video path to load.
     */
    public abstract void loadMultimedia(MultimediaDisplayable displayable);

    protected void seekAndUpdatePosition(long seekTo) {
    }

    protected void updatePositionSlider(long currentTime, long totalTime) {
        if(positionSlider.isValueChanging()) {
            return;
        }
        positionSlider.setValue(currentTime / totalTime);
    }
}
