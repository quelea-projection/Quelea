/*
 * This file is part of Quelea, free projection software for churches.
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.javafx.dialog.Dialog;
import org.quelea.data.Background;
import org.quelea.data.ColourBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * A panel that holds the toolbar and display selector for Theme settings
 *
 * @author Ben Goodwin
 */
public class ThemeSettingsPane extends BorderPane {

    private ThemePanel themePanel;
    private ThemeToolbar toolbar;
    private static final double THRESHOLD = 0.1;
    private String[] text;
    private final DisplayPreview preview;
    private final DisplayPositionSelector positionSelector;
    private final Button confirmButton;
    private final boolean bible;
    private boolean init = false;

    public ThemeSettingsPane(TextArea wordsArea, Button confirmButton, boolean bible, ThemePanel panel) {
        this.bible = bible;
        themePanel = panel;
        VBox centerPane = new VBox();

        positionSelector = new DisplayPositionSelector(this);
        positionSelector.prefWidthProperty().bind(widthProperty());
        positionSelector.prefHeightProperty().bind(heightProperty());
        DisplayCanvas canvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateTheme(false);
            }
        }, DisplayCanvas.Priority.LOW);
        preview = new DisplayPreview(canvas);

        Label label = new Label("      " + LabelGrabber.INSTANCE.getLabel("hover.for.position.label") + ":");
        label.setStyle("-fx-text-fill:#666666;");
        centerPane.setStyle("-fx-background-color:#dddddd;");
        centerPane.getChildren().add(label);
        StackPane themePreviewPane = new StackPane();
        themePreviewPane.getChildren().add(preview);
        themePreviewPane.getChildren().add(positionSelector);
        centerPane.getChildren().add(themePreviewPane);
        this.setCenter(centerPane);

        this.confirmButton = confirmButton;

        final LyricDrawer drawer = new LyricDrawer();
        drawer.setCanvas(canvas);
        text = ThemePanel.SAMPLE_LYRICS;
        if (wordsArea != null) {
            ChangeListener<String> cl = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String t, String newText) {
                    SongDisplayable dummy = new SongDisplayable("", "");
                    dummy.setLyrics(newText);
                    TextSection[] sections = dummy.getSections();
                    if (sections.length > 0 && sections[0].getText(false, false).length > 0) {
                        text = sections[0].getText(false, false);
                    } else {
                        text = ThemePanel.SAMPLE_LYRICS;
                    }
                    if (isEmpty(text)) {
                        text = ThemePanel.SAMPLE_LYRICS;
                    }
                    updateTheme(false);
                }
            };
            wordsArea.textProperty().addListener(cl);
            cl.changed(null, null, wordsArea.getText());
        }

        toolbar = new ThemeToolbar(this, bible);
        this.setTop(toolbar);

        if(init) {
            updateTheme(false);
        }
        setMaxSize(800, 600);
        init = true;
    }

    private boolean isEmpty(String[] text) {
        for (String str : text) {
            if (!str.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the confirm button used on this theme panel.
     * <p>
     * @return the confirm button.
     */
    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * Update the canvas with the current theme.
     * <p>
     * @param warning true if a warning should be shown if the colours
     * represented by the current theme are too similar.
     */
    public void updateTheme(boolean warning) {
        ThemeDTO theme;
        try {
            theme = getTheme();
        } catch (NullPointerException e) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        if (bible) {
            if (warning && theme.getBibleBackground() instanceof ColourBackground) {
                checkAccessibility(theme.getBibleFontPaint(), ((ColourBackground) theme.getBibleBackground()).getColour());
            }
        } else {
            if (warning && theme.getBackground() instanceof ColourBackground) {
                checkAccessibility(theme.getFontPaint(), ((ColourBackground) theme.getBackground()).getColour());
            }
        }
        final ThemeDTO ftheme = theme;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LyricDrawer drawer = new LyricDrawer();
                drawer.setCanvas(preview.getCanvas());
                drawer.setTheme(ftheme, bible);
                drawer.setText(text, null, null, false, -1);
            }
        });
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
        if (diff < THRESHOLD) {
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
        return themePanel.getTheme();
    }

    public ThemeToolbar getToolbar() {
        return toolbar;
    }

    public int getTextPosition() {
        return positionSelector.getSelectedButtonIndex();
    }

    public void setTheme(ThemeDTO theme, boolean bible) {
        toolbar.setTheme(theme, bible);
        positionSelector.setTheme(theme, bible);
        updateTheme(false);
    }
}
