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
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.ThemeUtils;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.widgets.DisplayPositionSelector;
import org.quelea.windows.main.widgets.DisplayPreview;
import org.quelea.windows.stage.StageDrawer;

/**
 * The panel where the user chooses what visual theme a song should have.
 * <p/>
 * @author Michael
 */
public class ThemePanel extends BorderPane {

    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private String[] text;
    private final DisplayPreview preview;
    private final ThemeToolbar themeToolbar;
    private DisplayPositionSelector positionSelector;
    private String saveHash = "";
    private final Button confirmButton;
    private ComboBox<ThemeDTO> themeCombo;

    /**
     * Create and initialise the theme panel
     */
    public ThemePanel() {
        this(null, null);
    }

    public ThemePanel(InlineCssTextArea wordsArea, Button confirmButton) {
        this(wordsArea, confirmButton, false);
    }

    /**
     * Create and initialise the theme panel.
     * <p>
     * @param wordsArea the text area to use for words. If null, sample lyrics
     * will be used.
     */
    public ThemePanel(InlineCssTextArea wordsArea, Button confirmButton, boolean showThemeCopyPanel) {
        this.confirmButton = confirmButton;
        positionSelector = new DisplayPositionSelector(this);
        positionSelector.prefWidthProperty().bind(widthProperty());
        positionSelector.prefHeightProperty().bind(heightProperty());
        DisplayCanvas canvas = new DisplayCanvas(false, () -> updateTheme(true), Priority.LOW);
        preview = new DisplayPreview(canvas);
        VBox centrePane = new VBox();
        Label label = new Label("      " + LabelGrabber.INSTANCE.getLabel("hover.for.position.label") + ":");
        if (!QueleaProperties.get().getUseDarkTheme()) {
            label.setStyle("-fx-text-fill:#666666;");
            centrePane.setStyle("-fx-background-color:#dddddd;");
        }
        centrePane.getChildren().add(label);
        StackPane themePreviewPane = new StackPane();
        themePreviewPane.getStyleClass().add("text-area");
        themePreviewPane.getChildren().add(preview);
        themePreviewPane.getChildren().add(positionSelector);
        centrePane.getChildren().add(themePreviewPane);
        setCenter(centrePane);
        final WordDrawer drawer;
        if (canvas.isStageView()) {
            drawer = new StageDrawer();
        } else {
            drawer = new LyricDrawer();
        }
        drawer.setCanvas(canvas);
        text = SAMPLE_LYRICS;
        if (wordsArea != null) {
            ChangeListener<String> cl = (ov, t, newText) -> {
                SongDisplayable dummy = new SongDisplayable("", "");
                dummy.setLyrics(newText);
                TextSection[] sections = dummy.getSections();
                if (sections.length > 0 && sections[0].getText(false, false).length > 0) {
                    text = sections[0].getText(false, false);
                } else {
                    text = SAMPLE_LYRICS;
                }
                if (isEmpty(text)) {
                    text = SAMPLE_LYRICS;
                }
                updateTheme(false);
            };
            wordsArea.textProperty().addListener(cl);
            cl.changed(null, null, wordsArea.getText());
        }

        VBox northBox = new VBox();
        HBox themeSelectPanel = new HBox();
        themeSelectPanel.setPadding(new Insets(5));
        Label themeSelectLabel = new Label(LabelGrabber.INSTANCE.getLabel("theme.copy.label") + ": ");
        themeSelectLabel.setPadding(new Insets(3,0,0,0));
        themeCombo = new ComboBox<>();
        themeCombo.setItems(ThemeUtils.getThemes());

        Button copyButton = new Button(LabelGrabber.INSTANCE.getLabel("copy"));
        copyButton.setOnAction(event -> {
            setTheme(themeCombo.getValue());
        });
        themeSelectPanel.setSpacing(5);
        themeSelectPanel.getChildren().addAll(themeSelectLabel, themeCombo, copyButton);

        themeToolbar = new ThemeToolbar(this);
        if(showThemeCopyPanel) {
            northBox.getChildren().add(themeSelectPanel);
        }
        northBox.getChildren().add(themeToolbar);

        setTop(northBox);
        updateTheme(false);
        setMaxSize(800, 600);
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
     * Determine if the save hash has changed since resetSaveHash() was last
     * called.
     * <p>
     * @return true if the hash has changed, false otherwise.
     */
    public boolean hashChanged() {
        return !getSaveHash().equals(saveHash);
    }

    /**
     * Reset the save hash to the current state of the panel.
     */
    public void resetSaveHash() {
        saveHash = getSaveHash();
    }

    /**
     * Get the current save hash.
     *
     * @return the current save hash.
     */
    private String getSaveHash() {
        return Integer.toString(getTheme().hashCode());
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
        final ThemeDTO theme = getTheme();
        Platform.runLater(() -> {
            WordDrawer drawer;
            if (preview.getCanvas().isStageView()) {
                drawer = new StageDrawer();
            } else {
                drawer = new LyricDrawer();
            }
            drawer.setCanvas(preview.getCanvas());
            drawer.setTheme(theme);
            drawer.setText(text, null, null, false, -1);

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
        updateTheme(false);
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
        if (themeToolbar == null) {
            return ThemeDTO.DEFAULT_THEME;
        }
        ThemeDTO ret = themeToolbar.getTheme();
        ret.setTextPosition(positionSelector.getSelectedButtonIndex());
        return ret;
    }
}
