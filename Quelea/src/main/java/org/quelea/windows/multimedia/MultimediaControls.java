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
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
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
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.quelea.data.GlobalThemeStore;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.style.MediaButton;
import org.quelea.windows.style.QueleaIcon;
import javafx.scene.layout.Region;

/**
 * The multimedia controls containing a play / pause button, stop button, and a
 * position slider on a gradient background.
 * <p/>
 *
 * @author Michael
 */
public class MultimediaControls extends StackPane {

    private static final int SLIDER_UPDATE_RATE = 100;
    private static final double CARD_WIDTH = 230;
    private boolean playpause;
    private MediaButton playButton;
    private MediaButton stopButton;
    private MediaButton loopButton;
    private Slider posSlider;
    private boolean disablePosSliderListener;
    private boolean disableControls;
    private Label elapsedTime;
    private Label totalTime;
    private Slider volSlider;
    private int vol = 100;
    private final MediaButton muteButton;
    private boolean looping;
    private final ReadOnlyBooleanWrapper playing = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper loop = new ReadOnlyBooleanWrapper(false);

    private Runnable onPlay;
    private Runnable onPause;
    private Runnable onStop;
    private Consumer<Double> onSeek;
    private Consumer<Boolean> onLoopChanged;

    private Consumer<Double> onVolumeChanged;

    public MultimediaControls() {
        // Rounded card behind the controls; see .media-controls-card in base.css
        Region rect = new Region();
        rect.getStyleClass().add("media-controls-card");
        rect.setMinSize(CARD_WIDTH, 100);
        rect.setPrefSize(CARD_WIDTH, 100);
        rect.setMaxSize(CARD_WIDTH, 100);
        getChildren().add(rect);

        onPlay = () -> {
        };
        onVolumeChanged = v -> {
        };
        playButton = new MediaButton(QueleaIcon.PLAY, 46, true);
        setButtonParams(playButton);
        playButton.setTranslateX(-30);
        getChildren().add(playButton);
        playButton.setOnAction(t -> {
            if (!disableControls) {
                play();
            }
            volSlider.setVisible(false);
        });

        onPause = () -> {
        };
        onStop = () -> {
        };
        stopButton = new MediaButton(QueleaIcon.STOP, 46, false);
        stopButton.setOnAction(t -> {
            if (!disableControls) {
                reset();
            }
            volSlider.setVisible(false);
        });
        setButtonParams(stopButton);
        stopButton.setTranslateX(30);
        getChildren().add(stopButton);

        onSeek = d -> {
        };
        posSlider = new Slider(0, 1, 0);
        posSlider.setDisable(true);
        posSlider.valueProperty().addListener(o -> {
            if (disablePosSliderListener) {
                return;
            }
            if (posSlider.isValueChanging()) {
                onSeek.accept(posSlider.getValue());
            }
        });
        posSlider.setTranslateY(30);
        posSlider.setPrefWidth(CARD_WIDTH - 20);
        posSlider.setMaxWidth(CARD_WIDTH - 20);
        getChildren().add(posSlider);

        loopButton = new MediaButton(QueleaIcon.LOOP, 28, false);
        loopButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("video.loop.tooltip")));
        loopButton.setOnAction(e -> {
            if (!disableControls) {
                setLoop(loopButton.getIcon() == QueleaIcon.LOOP);
            }
        });
        setButtonParams(loopButton);
        loopButton.setTranslateY(-30);
        loopButton.setTranslateX(posSlider.getMaxWidth() - 110);
        getChildren().add(loopButton);

        muteButton = new MediaButton(QueleaIcon.VOLUME_UP, 28, false);

        volSlider = new Slider(0, 1, 1);
        volSlider.setMaxHeight(70);
        volSlider.setVisible(false);
        volSlider.setOrientation(Orientation.VERTICAL);
        volSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            Double vol1 = volSlider.getValue();
            vol = vol1.intValue();
            onVolumeChanged.accept(vol1);
        });
        muteButton.setTranslateY(30);
        muteButton.setTranslateX(posSlider.getMaxWidth() - 80);
        getChildren().add(muteButton);
        muteButton.setOnAction(t -> {
            if (!disableControls) {
                Platform.runLater(() -> {
                    volSlider.setVisible(!volSlider.isVisible());
//                        volSlider.setValue(VLCWindow.INSTANCE.getVolume());
                    volSlider.requestFocus();
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
        elapsedTime.setPrefWidth(CARD_WIDTH - 20);
        elapsedTime.setMaxWidth(CARD_WIDTH - 20);
        elapsedTime.setBackground(Background.EMPTY);
        elapsedTime.setTranslateY(40);
        elapsedTime.getStyleClass().add("media-time-label");
        getChildren().add(elapsedTime);

        totalTime = new Label();
        totalTime.setPrefWidth(CARD_WIDTH - 20);
        totalTime.setMaxWidth(CARD_WIDTH - 20);
        totalTime.setBackground(Background.EMPTY);
        totalTime.setTranslateY(40);
        totalTime.setAlignment(Pos.BASELINE_RIGHT);
        totalTime.getStyleClass().add("media-time-label");
        getChildren().add(totalTime);

//        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
//        service.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                if (!disableControls && VLCWindow.INSTANCE.isPlaying() && !posSlider.isValueChanging()) {
//                    Platform.runLater(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            posSlider.setValue(VLCWindow.INSTANCE.getProgressPercent());
//                            if (VLCWindow.INSTANCE.getTotal() != 0) {
//                                elapsedTime.setText(getTime(VLCWindow.INSTANCE.getTime()));
//                                totalTime.setText(getTime(VLCWindow.INSTANCE.getTotal()));
//                            }
//                        }
//                    });
//                }
//            }
//        }, 0, SLIDER_UPDATE_RATE, TimeUnit.MILLISECONDS);

//        ScheduledExecutorService update = Executors.newSingleThreadScheduledExecutor();
//        update.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                if (!disableControls && VLCWindow.INSTANCE.isPlaying() && !volSlider.isValueChanging()) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            volSlider.setValue(VLCWindow.INSTANCE.getVolume());
//                        }
//                    });
//                }
//            }
//        }, 0, 2000, TimeUnit.MILLISECONDS);
//        VLCWindow.INSTANCE.setOnFinished(new Runnable() {
//            @Override
//            public void run() {
//                if (getScene() != null && !disableControls && !looping) {
//                    reset();
//                }
//            }
//        });
    }

    public void loadMultimedia(String path) {
        reset();
        if (!path.trim().startsWith("http") && !path.trim().startsWith("dvdsimple") && !path.trim().startsWith("bluray")) {
            path = Utils.getVLCStringFromFile(new File(path));
        }
    }

    public void setDisableControls(boolean disable) {
        this.disableControls = disable;
        playButton.setIcon(playpause ? QueleaIcon.PAUSE : QueleaIcon.PLAY);
        playButton.setDisable(disable);
        stopButton.setDisable(disable);
    }

    public void setOnPlay(Runnable onPlay) {
        this.onPlay = onPlay;
    }

    public void setOnPause(Runnable onPause) {
        this.onPause = onPause;
    }

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }

    public void setOnSeek(Consumer<Double> onSeek) {
        this.onSeek = onSeek;
    }

    public void setOnLoopChanged(Consumer<Boolean> onLoopChanged) {
        this.onLoopChanged = onLoopChanged;
    }

    public void setOnVolumeChanged(Consumer<Double> onVolumeChanged) {
        this.onVolumeChanged = onVolumeChanged;
    }

    public void setPosition(double pos) {
        if (!posSlider.isValueChanging()) {
            posSlider.setValue(pos);
        }
    }

    /**
     * Turn looping on or off, as the loop button does.
     *
     * @param loopOn true to play the media again when it ends.
     */
    public void setLoop(boolean loopOn) {
        loopButton.setIcon(loopOn ? QueleaIcon.LOOP_ON : QueleaIcon.LOOP);
        loopButton.setActive(loopOn);
        loop.set(loopOn);
        onLoopChanged.accept(loopOn);
    }

    /**
     * @return true while the media plays; false when paused or stopped.
     * May change off the FX thread when the media ends.
     */
    public ReadOnlyBooleanProperty playingProperty() {
        return playing.getReadOnlyProperty();
    }

    /**
     * @return true if the media plays again when it ends.
     */
    public ReadOnlyBooleanProperty loopProperty() {
        return loop.getReadOnlyProperty();
    }

    /**
     * @return how far the media has played, from 0 to 1, as the position
     * slider shows it.
     */
    public double getPosition() {
        return posSlider.getValue();
    }

    /**
     * Jump to a position, as dragging the position slider does.
     *
     * @param pos the position, from 0 to 1.
     */
    public void seek(double pos) {
        disablePosSliderListener = true;
        posSlider.setValue(pos);
        disablePosSliderListener = false;
        onSeek.accept(pos);
    }

    public void play() {
        playpause = !playpause;
        playing.set(playpause);
        if (playpause) {
            playButton.setIcon(QueleaIcon.PAUSE);

//            VLCWindow.INSTANCE.setRepeat(looping);
//            VLCWindow.INSTANCE.play();

            posSlider.setDisable(false);
            muteButton.setDisable(false);
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (lp.getBlacked()) {
                lp.clickBlacked();
            }
            if (lp.getLogoed()) {
                lp.clickLogoed();
            }
            onPlay.run();
        } else {
            playButton.setIcon(QueleaIcon.PLAY);
            onPause.run();
        }
    }

    public void reset() {
        playButton.setIcon(QueleaIcon.PLAY);
        playpause = false;
        playing.set(false);
        disablePosSliderListener = true;
        posSlider.setValue(0);
        posSlider.setDisable(true);
        disablePosSliderListener = false;
        muteButton.setDisable(true);
        Platform.runLater(() -> {
            elapsedTime.setText("");
            totalTime.setText("");
        });
        onStop.run();
    }

    private void setButtonParams(final MediaButton button) {
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
