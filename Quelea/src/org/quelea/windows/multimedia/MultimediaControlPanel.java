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
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author tomaszpio@gmail.com
 */
public class MultimediaControlPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String filePath;
    private ToggleButton mute;
    private Button pause;
    private Button play;
    private Slider positionSlider;
    private Slider volumeSlider;
    private Button stop;

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
        play.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                VLCWindow.INSTANCE.setRepeat(false);
                VLCWindow.INSTANCE.play();
            }
        });
        pause = new Button("", new ImageView(new Image("file:icons/pause.png")));
        pause.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                VLCWindow.INSTANCE.pause();
            }
        });
        stop = new Button("", new ImageView(new Image("file:icons/stop.png")));
        stop.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                VLCWindow.INSTANCE.stop();
                positionSlider.setValue(0);
            }
        });
        mute = new ToggleButton("", new ImageView(new Image("file:icons/mute.png")));
        mute.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                VLCWindow.INSTANCE.setMute(mute.isSelected());
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
//        controlPanel.getChildren().add(sliderPanel);
        setCenter(controlPanel);
    }

    public void setDisableControls(boolean disable) {
        play.setDisable(disable);
        pause.setDisable(disable);
        stop.setDisable(disable);
        mute.setDisable(disable);
    }

    /**
     * Load the given multimedia to be controlled via this panel.
     * <p/>
     * @param path the multimedia path to load.
     */
    public void loadMultimedia(String path) {
        this.filePath = path;
        VLCWindow.INSTANCE.stop();
        VLCWindow.INSTANCE.load(filePath);
    }

    protected void seekAndUpdatePosition(long seekTo) {
    }

    protected void updatePositionSlider(long currentTime, long totalTime) {
        if(positionSlider.isValueChanging()) {
            return;
        }
        positionSlider.setValue(currentTime / totalTime);
    }
}
