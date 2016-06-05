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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * An overlay that should be put on the song database when no songs are present.
 * It prompts the user to press the add song button to add a song - this is
 * useful from a HCI perspective because otherwise the button is not greatly
 * noticeable.
 * <p/>
 * @author Michael
 */
public class AddSongPromptOverlay extends StackPane {

    private FadeTransition trans;
    public final Label text;

    /**
     * Create the overlay.
     */
    public AddSongPromptOverlay() {
        setAlignment(Pos.CENTER);
        VBox content = new VBox();
        content.setAlignment(Pos.TOP_LEFT);
        StackPane.setMargin(content, new Insets(10,0,0,15));
        ImageView iv = new ImageView(new Image("file:icons/whitearrow.png"));
        content.getChildren().add(iv);
        text = new Label(LabelGrabber.INSTANCE.getLabel("add.song.hint.text"));
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSearchBox().textProperty().addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                        if(t1.isEmpty()) {
                            text.setText(LabelGrabber.INSTANCE.getLabel("add.song.hint.text"));
                        }
                        else {
                            text.setText(LabelGrabber.INSTANCE.getLabel("add.song.hint.search.text"));
                        }
                    }
                });
            }
        });
        text.setWrapText(true);
        text.setTextFill(Color.WHITESMOKE);
        text.setStyle("-fx-font-size:16pt; -fx-font-family:Calibri;");
        content.getChildren().add(text);
        getChildren().add(content);
        setOpacity(0);
        setStyle("-fx-background-color: #555555;");
        setVisible(false);
    }

    /**
     * Show (fade in) the overlay.
     */
    public synchronized void show() {
        setVisible(true);
        if(trans != null) {
            trans.stop();
        }
        trans = new FadeTransition(Duration.seconds(0.2), this);
        trans.setFromValue(getOpacity());
        trans.setToValue(0.6);
        trans.play();
    }

    /**
     * Hide (fade out) the overlay.
     */
    public synchronized void hide() {
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
