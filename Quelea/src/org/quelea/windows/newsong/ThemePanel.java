/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.newsong;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.javafx.dialog.Dialog;
import org.quelea.data.ColourBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * The panel where the user chooses what visual theme a song should have.
 * <p/>
 * @author Michael
 */
public class ThemePanel extends BorderPane {

    private static final double THRESHOLD = 0.1;
    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private final DisplayPreview preview;
    private final ThemeToolbar themeToolbar;
    private ThemeDTO selectedTheme = null;
    private DisplayPositionSelector positionSelector;

    /**
     * Create and initialise the theme panel.
     */
    public ThemePanel() {
        positionSelector = new DisplayPositionSelector(this);
        positionSelector.prefWidthProperty().bind(widthProperty());
        positionSelector.prefHeightProperty().bind(heightProperty());
        DisplayCanvas canvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateTheme(true, null);
            }
        }, Priority.LOW);
        preview = new DisplayPreview(canvas);
        VBox centrePane = new VBox();
        Label label = new Label("      " + LabelGrabber.INSTANCE.getLabel("hover.for.position.label") + ":");
        label.setStyle("-fx-text-fill:#666666;");
        centrePane.setStyle("-fx-background-color:#dddddd;");
        centrePane.getChildren().add(label);
        StackPane themePreviewPane = new StackPane();
        themePreviewPane.getChildren().add(preview);
        themePreviewPane.getChildren().add(positionSelector);
        centrePane.getChildren().add(themePreviewPane);
        setCenter(centrePane);
        LyricDrawer drawer = new LyricDrawer();
        drawer.setCanvas(canvas);
        drawer.setText(SAMPLE_LYRICS, null, false, -1);
        themeToolbar = new ThemeToolbar(this);
        setTop(themeToolbar);
        updateTheme(false, null);
        setMaxSize(800, 600);
    }

    /**
     * Update the canvas with the current theme.
     */
    public void updateTheme(boolean warning, ThemeDTO newTheme) {
        final ThemeDTO theme = (newTheme != null) ? newTheme : getTheme();
        if(warning && theme.getBackground() instanceof ColourBackground) {
            checkAccessibility(theme.getFontPaint(), ((ColourBackground) theme.getBackground()).getColour());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LyricDrawer drawer = new LyricDrawer();
                drawer.setCanvas(preview.getCanvas());
                drawer.setTheme(theme);
                drawer.setText(SAMPLE_LYRICS, null, false, -1);
            }
        });
    }

    /**
     * Set the current theme to represent in this panel.
     * <p/>
     * @param theme the theme to represent.
     */
    public void setTheme(ThemeDTO theme) {
        themeToolbar.setTheme(theme);
        positionSelector.setTheme(theme);
        updateTheme(false, null);
    }

    /**
     * Check whether the two colours are too closely matched to read clearly. If
     * they are, display a warning message.
     * <p/>
     * @param col1 first colour.
     * @param col2 second colour.
     */
    private void checkAccessibility(Color col1, Color col2) {
        double diff = Utils.getColorDifference(col1, col2);
        if(diff < THRESHOLD) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("warning.label"), LabelGrabber.INSTANCE.getLabel("similar.colors.text"));
        }
    }

    /**
     * Get the canvas on this theme panel.
     * <p/>
     * @return the canvas on this theme panel.
     */
    public DisplayCanvas getCanvas() {
        return preview.getCanvas();
    }

    /**
     * Get the theme currently represented by the state of this panel.
     * <p/>
     * @return the current theme.
     */
    public ThemeDTO getTheme() {
        ThemeDTO ret = themeToolbar.getTheme();
        ret.setTextPosition(positionSelector.getSelectedButtonIndex());
        return ret;
    }

    /**
     * @return the selectedTheme
     */
    public ThemeDTO getSelectedTheme() {
        return selectedTheme;
    }
}
