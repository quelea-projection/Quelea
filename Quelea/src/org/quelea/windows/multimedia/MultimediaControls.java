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
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    private boolean playpause;
    private ImageView playButton;
    private ImageView stopButton;
    private Slider posSlider;
    private boolean disableControls;

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
            }
        });

        stopButton = new ImageView(STOP_IMAGE);
        stopButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (!disableControls) {
                    reset();
                }
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
                }
            }
        });
        posSlider.setTranslateY(30);
        posSlider.setPrefWidth(rect.getWidth() - 20);
        posSlider.setMaxWidth(rect.getWidth() - 20);
        getChildren().add(posSlider);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!disableControls && VLCWindow.INSTANCE.isPlaying() && !posSlider.isValueChanging()) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            posSlider.setValue(VLCWindow.INSTANCE.getProgressPercent());
                        }
                    });
                }
            }
        }, 0, SLIDER_UPDATE_RATE, TimeUnit.MILLISECONDS);
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
}
