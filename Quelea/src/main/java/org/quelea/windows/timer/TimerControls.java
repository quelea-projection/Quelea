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
package org.quelea.windows.timer;

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
import org.quelea.windows.main.widgets.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The timer controls containing a play / pause button, stop button
 * <p/>
 *
 * @author Michael and Ben
 */
public class TimerControls extends StackPane {

    private static final Image PLAY_IMAGE = new Image("file:icons/play.png");
    private static final Image PAUSE_IMAGE = new Image("file:icons/pause.png");
    private static final Image STOP_IMAGE = new Image("file:icons/stop.png");
    private static final Image PLAY_IMAGE_DISABLE = new Image("file:icons/playdisable.png");
    private static final Image PAUSE_IMAGE_DISABLE = new Image("file:icons/pausedisable.png");
    private static final Image STOP_IMAGE_DISABLE = new Image("file:icons/stopdisable.png");
    private boolean playpause;
    private final ImageView playButton;
    private final ImageView stopButton;
    private boolean disableControls;
    private Set<Timer> timers;
    private boolean sync = false;

    public TimerControls() {
        timers = new HashSet<>();
        Rectangle rect = new Rectangle(230, 80);
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
        playButton.setOnMouseClicked((MouseEvent t) -> {
            if (!disableControls) {
                play(playButton.getImage().equals(PAUSE_IMAGE));
            }
        });

        stopButton = new ImageView(STOP_IMAGE);
        stopButton.setOnMouseClicked((MouseEvent t) -> {
            if (!disableControls) {
                reset();
            }
        });
        setButtonParams(stopButton);
        stopButton.setTranslateX(30);
        getChildren().add(stopButton);
    }

    public void loadMultimedia(String path, boolean stretch) {
        this.sync = true;
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

    public void play(boolean pause) {
        playpause = !playpause;
        if (pause) {
            if (!disableControls) {
                playButton.setImage(PLAY_IMAGE);
            } else {
                playButton.setImage(PLAY_IMAGE_DISABLE);
            }
            timers.forEach(Timer::pause);
        } else {
            if (!disableControls) {
                playButton.setImage(PAUSE_IMAGE);
            } else {
                playButton.setImage(PAUSE_IMAGE_DISABLE);
            }
            timers.forEach(Timer::play);
        }
    }

    public void reset() {

        if (disableControls) {
            playButton.setImage(PLAY_IMAGE_DISABLE);
        } else {
            playButton.setImage(PLAY_IMAGE);
            timers.forEach(Timer::stop);

        }
        playpause = false;
    }

    private void setButtonParams(final ImageView button) {
        button.setOnMouseEntered((MouseEvent t) -> {
            if (!disableControls) {
                button.setEffect(new Glow(0.5));
            }
        });
        button.setOnMouseExited((MouseEvent t) -> {
            button.setEffect(null);
        });
        button.setFitWidth(50);
        button.setPreserveRatio(true);
        button.setTranslateY(-10);
    }

    public void addTimer(Timer timer) {
        timers.add(timer);
    }

    public void togglePause() {
        play(playButton.getImage().equals(PAUSE_IMAGE));
    }

    public boolean status() {
        return playButton.getImage().equals(PAUSE_IMAGE);
    }

}
