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

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.quelea.services.utils.Utils;

/**
 * The multimedia controls containing a play / pause button, stop button, and a
 * position slider on a gradient background.
 * <p/>
 * @author Michael
 */
public class MultimediaControls extends StackPane {

    private static final int SLIDER_UPDATE_RATE = 100;
    private static final Image PLAY_IMAGE = new Image("file:icons/play.png");
    private static final Image PAUSE_IMAGE = new Image("file:icons/pause.png");
    private static final Image STOP_IMAGE = new Image("file:icons/stop.png");
    private static final Image PLAY_IMAGE_DISABLE = new Image("file:icons/playdisable.png");
    private static final Image PAUSE_IMAGE_DISABLE = new Image("file:icons/pausedisable.png");
    private static final Image STOP_IMAGE_DISABLE = new Image("file:icons/stopdisable.png");
    private static final Image VOLUME = new Image("file:icons/volume.png");
    private boolean playpause;
    private ImageView playButton;
    private ImageView stopButton;
    private Slider posSlider;
    private boolean disableControls;
    private Label elapsedTime;
    private Label totalTime;
    private Slider volSlider;
    private int vol = 100;
    private final ImageView muteButton;

    public MultimediaControls() {
        Rectangle rect = new Rectangle(230, 100);
        Stop[] stops = new Stop[]{new Stop(0, Color.BLACK), new Stop(1, Color.GREY)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        rect.setOpacity(0.8);
        rect.setFill(lg1);
        rect.setArcHeight(20);
        rect.setArcWidth(20);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(5.0);
        ds.setOffsetX(5.0);
        ds.setColor(Color.GRAY);
        ds.setInput(new Reflection(5, 0.4, 0.3, 0));
        rect.setEffect(ds);
        getChildren().add(rect);

        playButton = new ImageView(PLAY_IMAGE);
        setButtonParams(playButton);
        playButton.setTranslateX(-30);
        getChildren().add(playButton);
        playButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (!disableControls) {
                    play();
                }
                volSlider.setVisible(false);
            }
        });

        stopButton = new ImageView(STOP_IMAGE);
        stopButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (!disableControls) {
                    reset();
                }
                volSlider.setVisible(false);
            }
        });
        setButtonParams(stopButton);
        stopButton.setTranslateX(30);
        getChildren().add(stopButton);

        posSlider = new Slider(0, 1, 0);
        posSlider.setDisable(true);
        posSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                if (!disableControls && (VLCWindow.INSTANCE.isPlaying() || VLCWindow.INSTANCE.isPaused()) && posSlider.isValueChanging()) {
                    VLCWindow.INSTANCE.setProgressPercent(posSlider.getValue());
                    long time = (long) (VLCWindow.INSTANCE.getProgressPercent() * VLCWindow.INSTANCE.getTotal());
                    if (VLCWindow.INSTANCE.getTotal() != 0) {
                        elapsedTime.setText(getTime(time));
                    }
                }
            }
        });
        posSlider.setTranslateY(30);
        posSlider.setPrefWidth(rect.getWidth() - 20);
        posSlider.setMaxWidth(rect.getWidth() - 20);
        getChildren().add(posSlider);

        muteButton = new ImageView(VOLUME);
        muteButton.setFitWidth(25);
        muteButton.setPreserveRatio(true);

        volSlider = new Slider(0, 100, 0);
        volSlider.setMaxHeight(70);
        volSlider.setVisible(false);
        volSlider.setOrientation(Orientation.VERTICAL);
        volSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            Double vol1 = volSlider.getValue();
            vol = vol1.intValue();
            System.out.println((double)vol/100);
            VLCWindow.INSTANCE.setVolume(vol);
        });
        muteButton.setTranslateY(30);
        muteButton.setTranslateX(posSlider.getMaxWidth() - 80);
        getChildren().add(muteButton);
        muteButton.setOnMouseClicked((MouseEvent t) -> {
            if (!disableControls) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        volSlider.setVisible(!volSlider.isVisible());
                        volSlider.setValue(VLCWindow.INSTANCE.getVolume());
                        volSlider.requestFocus();
                    }
                });
            }
        });

        volSlider.setTranslateX(muteButton.localToScene(0, 0).getX());
        volSlider.setTranslateY(muteButton.localToScene(0, 0).getY() - 45);
        getChildren().add(volSlider);

        volSlider.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                volSlider.setVisible(false);
            }
        });

        elapsedTime = new Label();
        elapsedTime.setPrefWidth(rect.getWidth() - 20);
        elapsedTime.setMaxWidth(rect.getWidth() - 20);
        elapsedTime.setBackground(Background.EMPTY);
        elapsedTime.setTranslateY(40);
        elapsedTime.setTextFill(Color.WHITE);
        getChildren().add(elapsedTime);

        totalTime = new Label();
        totalTime.setPrefWidth(rect.getWidth() - 20);
        totalTime.setMaxWidth(rect.getWidth() - 20);
        totalTime.setBackground(Background.EMPTY);
        totalTime.setTranslateY(40);
        totalTime.setAlignment(Pos.BASELINE_RIGHT);
        totalTime.setTextFill(Color.WHITE);
        getChildren().add(totalTime);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!disableControls && VLCWindow.INSTANCE.isPlaying() && !posSlider.isValueChanging()) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            posSlider.setValue(VLCWindow.INSTANCE.getProgressPercent());
                            if (VLCWindow.INSTANCE.getTotal() != 0) {
                                elapsedTime.setText(getTime(VLCWindow.INSTANCE.getTime()));
                                totalTime.setText(getTime(VLCWindow.INSTANCE.getTotal()));
                            }
                        }
                    });
                }
            }
        }, 0, SLIDER_UPDATE_RATE, TimeUnit.MILLISECONDS);

        ScheduledExecutorService update = Executors.newSingleThreadScheduledExecutor();
        update.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!disableControls && VLCWindow.INSTANCE.isPlaying() && !volSlider.isValueChanging()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            volSlider.setValue(VLCWindow.INSTANCE.getVolume());
                        }
                    });
                }
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
        VLCWindow.INSTANCE.setOnFinished(new Runnable() {
            @Override
            public void run() {
                if (getScene() != null && !disableControls) {
                    reset();
                }
            }
        });
    }

    public void loadMultimedia(String path) {
        reset();
        if (!path.trim().startsWith("http") && !path.trim().startsWith("dvdsimple") && !path.trim().startsWith("bluray")) {
            path = Utils.getVLCStringFromFile(new File(path));
        }
        String[] locationParts = path.split("[\\r\\n]+");
        if (locationParts.length == 1) {
            VLCWindow.INSTANCE.load(locationParts[0], null, false);
        } else {
            VLCWindow.INSTANCE.load(locationParts[0], locationParts[1], false);
        }
    }

    public void setDisableControls(boolean disable) {
        this.disableControls = disable;
        if (disable) {
            if (!playpause) {
                playButton.setImage(PLAY_IMAGE_DISABLE);
            } else {
                playButton.setImage(PAUSE_IMAGE_DISABLE);
            }
            stopButton.setImage(STOP_IMAGE_DISABLE);
        } else {
            if (!playpause) {
                playButton.setImage(PLAY_IMAGE);
            } else {
                playButton.setImage(PAUSE_IMAGE);
            }
            stopButton.setImage(STOP_IMAGE);
        }
    }

    public void play() {
        playpause = !playpause;
        if (playpause) {
            playButton.setImage(PAUSE_IMAGE);
            VLCWindow.INSTANCE.setRepeat(false);
            VLCWindow.INSTANCE.setHue(0);
            VLCWindow.INSTANCE.play();
            posSlider.setDisable(false);
            muteButton.setDisable(false);
        } else {
            playButton.setImage(PLAY_IMAGE);
            VLCWindow.INSTANCE.pause();
        }
    }

    public void reset() {
        VLCWindow.INSTANCE.stop();
        if (disableControls) {
            playButton.setImage(PLAY_IMAGE_DISABLE);
        } else {
            playButton.setImage(PLAY_IMAGE);
        }
        playpause = false;
        posSlider.setValue(0);
        posSlider.setDisable(true);
        muteButton.setDisable(true);
        Platform.runLater(() -> {
            elapsedTime.setText("");
            totalTime.setText("");
        });
    }

    private void setButtonParams(final ImageView button) {
        button.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (!disableControls) {
                    button.setEffect(new Glow(0.5));
                }
            }
        });
        button.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                button.setEffect(null);
            }
        });
        button.setFitWidth(50);
        button.setPreserveRatio(true);
        button.setTranslateY(-10);
    }

    /**
     * Method to get the elapsed time of the video
     *
     * @param elapsedTimeMillis Time elapsed
     */
    private String getTime(long elapsedTimeMillis) {
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        int hours = (int) elapsedTimeSec / 3600;
        int minutes = (int) (elapsedTimeSec % 3600) / 60;
        int seconds = (int) elapsedTimeSec % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return time;
    }
}
