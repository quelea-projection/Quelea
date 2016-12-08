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
package org.quelea.windows.main.widgets;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;

/**
 * A pane that can be overlaid on a component when it's loading something.
 * <p/>
 * @author Michael
 */
public class LoadingPane extends StackPane {

    private FadeTransition trans;
    private ProgressBar bar;

    /**
     * Create the loading pane.
     */
    public LoadingPane() {
        setAlignment(Pos.CENTER);
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        Text text = new Text(LabelGrabber.INSTANCE.getLabel("loading.text") + "...");
        text.setStyle(" -fx-font: bold italic 20pt \"Arial\";");
        FadeTransition textTransition = new FadeTransition(Duration.seconds(1.5), text);
        textTransition.setAutoReverse(true);
        textTransition.setFromValue(0);
        textTransition.setToValue(1);
        textTransition.setCycleCount(Transition.INDEFINITE);
        textTransition.play();
        content.getChildren().add(text);
        bar = new ProgressBar();
        content.getChildren().add(bar);
        getChildren().add(content);
        setOpacity(0);
        setStyle("-fx-background-color: #555555;");
        setVisible(false);
    }
    
    public void setProgress(double progress) {
        bar.setProgress(progress);
    }

    /**
     * Show (fade in) the loading pane.
     */
    public synchronized void show() {
        setVisible(true);
        setProgress(-1);
        if(trans != null) {
            trans.stop();
        }
        trans = new FadeTransition(Duration.seconds(0.2), this);
        trans.setFromValue(getOpacity());
        trans.setToValue(0.6);
        trans.play();
    }

    /**
     * Hide (fade out) the loading pane.
     */
    public synchronized void hide() {
        setVisible(false);
        if(trans != null) {
            trans.stop();
        }
        trans = new FadeTransition(Duration.seconds(0.2), this);
        trans.setFromValue(getOpacity());
        trans.setToValue(0);
        trans.play();
        trans.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                setVisible(false);
            }
        });
    }
}
