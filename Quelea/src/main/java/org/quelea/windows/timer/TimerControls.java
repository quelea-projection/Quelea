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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
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
import org.quelea.windows.style.MediaButton;
import org.quelea.windows.style.QueleaIcon;
import javafx.scene.layout.Region;

/**
 * The timer controls containing a play / pause button, stop button
 * <p/>
 *
 * @author Michael and Ben
 */
public class TimerControls extends StackPane {

    private static final double CARD_WIDTH = 230;
    private boolean playpause;
    private final MediaButton playButton;
    private final MediaButton stopButton;
    private boolean disableControls;
    private Set<Timer> timers;
    private boolean sync = false;
    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(false);

    public TimerControls() {
        timers = new HashSet<>();
        // Rounded card behind the controls; see .media-controls-card in base.css
        Region rect = new Region();
        rect.getStyleClass().add("media-controls-card");
        rect.setMinSize(CARD_WIDTH, 80);
        rect.setPrefSize(CARD_WIDTH, 80);
        rect.setMaxSize(CARD_WIDTH, 80);
        getChildren().add(rect);

        playButton = new MediaButton(QueleaIcon.PLAY, 46, true);
        setButtonParams(playButton);
        playButton.setTranslateX(-30);
        getChildren().add(playButton);
        playButton.setOnAction(t -> {
            if (!disableControls) {
                play(playButton.getIcon() == QueleaIcon.PAUSE);
            }
        });

        stopButton = new MediaButton(QueleaIcon.STOP, 46, false);
        stopButton.setOnAction(t -> {
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
        playButton.setIcon(playpause ? QueleaIcon.PAUSE : QueleaIcon.PLAY);
        playButton.setDisable(disable);
        stopButton.setDisable(disable);
    }

    public void play(boolean pause) {
        playpause = !playpause;
        running.set(!pause);
        if (pause) {
            playButton.setIcon(QueleaIcon.PLAY);
            timers.forEach(Timer::pause);
        } else {
            playButton.setIcon(QueleaIcon.PAUSE);
            timers.forEach(Timer::play);
        }
    }

    public void reset() {

        playButton.setIcon(QueleaIcon.PLAY);
        if (!disableControls) {
            timers.forEach(Timer::stop);
        }
        playpause = false;
        running.set(false);
    }

    /**
     * @return true while the timer counts; false when paused or stopped.
     */
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    private void setButtonParams(final MediaButton button) {
        // Timers have no position slider, so the buttons sit in the middle of the card
        button.setTranslateY(0);
    }

    public void addTimer(Timer timer) {
        timers.add(timer);
    }

    public void togglePause() {
        play(playButton.getIcon() == QueleaIcon.PAUSE);
    }

    public boolean status() {
        return playButton.getIcon() == QueleaIcon.PAUSE && !disableControls;
    }

}
