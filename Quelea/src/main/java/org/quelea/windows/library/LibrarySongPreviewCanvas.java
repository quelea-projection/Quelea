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
package org.quelea.windows.library;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.WordDrawer;

/**
 * A pane that can be overlaid on a component when it's loading something.
 * <p/>
 * @author Michael
 */
public class LibrarySongPreviewCanvas extends StackPane {

    private FadeTransition trans;
    private DisplayCanvas canvas;
    private SongDisplayable displayable;

    /**
     * Create the loading pane.
     */
    public LibrarySongPreviewCanvas() {
        setMaxSize(250, 167);
        canvas = new DisplayCanvas(false, false, false, this::updateCanvas, DisplayCanvas.Priority.LOW);
        canvas.setMaxSize(250, 167);
        getChildren().add(canvas);
        setOpacity(0);
//        setStyle("-fx-background-color: #555555;");
        setVisible(false);
        setMouseTransparent(true);
    }

    public void updateCanvas() {
        WordDrawer drawer = new LyricDrawer();
        drawer.setCanvas(canvas);
        if (displayable == null || displayable.getSections().length == 0) {
            drawer.eraseText();
        } else {
            TextSection currentSection = displayable.getSections()[0];
            ThemeDTO newTheme = currentSection.getTheme();
            drawer.setTheme(newTheme);
            drawer.setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
            drawer.setText(displayable, 0);
            canvas.setCurrentDisplayable(displayable);
        }
    }

    public void setSong(SongDisplayable displayable) {
        this.displayable = displayable;
        canvas.update();
    }

    /**
     * Show (fade in) the loading pane.
     */
    public synchronized void show() {
        setVisible(true);
        if (trans != null) {
            trans.stop();
        }
        trans = new FadeTransition(Duration.seconds(0.2), this);
        trans.setFromValue(getOpacity());
        trans.setToValue(0.8);
        trans.play();
    }

    /**
     * Hide (fade out) the loading pane.
     */
    public synchronized void hide() {
        setVisible(false);
        if (trans != null) {
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
