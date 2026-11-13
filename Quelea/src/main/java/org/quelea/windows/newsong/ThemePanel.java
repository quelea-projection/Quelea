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
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
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

    /** Standard and smallest size of the dialogs that show this panel. */
    private static final double DIALOG_WIDTH = 640;
    private static final double DIALOG_HEIGHT = 500;
    private static final double DIALOG_MIN_WIDTH = 560;
    private static final double DIALOG_MIN_HEIGHT = 500;
    /** Extra width for the font lists when the labels of a language are long. */
    private static final double DIALOG_FONT_ROOM = 60;
    private static final String DIALOG_SIZED_KEY = "quelea.theme.dialog.sized";

    public static final String[] SAMPLE_LYRICS = {"Amazing Grace how sweet the sound", "That saved a wretch like me", "I once was lost but now am found", "Was blind, but now I see."};
    private String[] text;
    private final DisplayPreview preview;
    /**
     * Single drawer reused for every preview update. Creating a new drawer per
     * update (e.g. on every slider tick) used to start a new video player each
     * time for video themes, which were never stopped.
     */
    private final WordDrawer drawer;
    /**
     * Whether this panel is currently in a showing window. Video backgrounds
     * are only played while it is, so closing a theme dialog stops the video.
     */
    private final ObservableValue<Boolean> showing;
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
        Label label = new Label(LabelGrabber.INSTANCE.getLabel("hover.for.position.label"));
        label.getStyleClass().addAll("muted-label", "theme-position-hint");
        centrePane.getStyleClass().add("preview-backdrop");
        centrePane.getChildren().add(label);
        StackPane themePreviewPane = new StackPane();
        themePreviewPane.getStyleClass().add("text-area");
        themePreviewPane.getChildren().add(preview);
        themePreviewPane.getChildren().add(positionSelector);
        centrePane.getChildren().add(themePreviewPane);
        setCenter(centrePane);
        if (canvas.isStageView()) {
            drawer = new StageDrawer();
        } else {
            drawer = new LyricDrawer();
        }
        drawer.setCanvas(canvas);
        showing = sceneProperty().flatMap(Scene::windowProperty).flatMap(Window::showingProperty).orElse(false);
        setPreviewVideo(showing.getValue());
        showing.addListener((ov, wasShowing, isShowing) -> {
            setPreviewVideo(isShowing);
            if (isShowing) {
                updateTheme(false);
            }
        });
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
        themeSelectPanel.getStyleClass().add("theme-copy-row");
        themeSelectPanel.setAlignment(Pos.CENTER_LEFT);
        Label themeSelectLabel = new Label(LabelGrabber.INSTANCE.getLabel("theme.copy.label") + ":");
        themeSelectLabel.setMinWidth(Region.USE_PREF_SIZE);
        themeCombo = new ComboBox<>();
        themeCombo.setItems(ThemeUtils.getThemes());

        Button copyButton = new Button(LabelGrabber.INSTANCE.getLabel("copy"));
        copyButton.setOnAction(event -> {
            setTheme(themeCombo.getValue());
        });
        themeSelectPanel.setSpacing(8);
        themeSelectPanel.getChildren().addAll(themeSelectLabel, themeCombo, copyButton);

        themeToolbar = new ThemeToolbar(this);
        if(showThemeCopyPanel) {
            northBox.getChildren().add(themeSelectPanel);
        }
        northBox.getChildren().add(themeToolbar);

        setTop(northBox);
        updateTheme(false);
    }

    /**
     * Give a dialog that holds this panel the standard size: room for the
     * editor's font lists, resizable (the preview grows with the window),
     * and not smaller than the editor needs. That depends on the language
     * (long labels need more room), so the minimum width is worked out from
     * the content when the dialog is shown.
     *
     * @param dialog the dialog window.
     */
    public void sizeDialog(Stage dialog) {
        setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialog.setResizable(true);
        dialog.setMinWidth(DIALOG_MIN_WIDTH);
        dialog.setMinHeight(DIALOG_MIN_HEIGHT);
        dialog.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
            double frame = dialog.getWidth() - dialog.getScene().getWidth();
            double needed = Math.ceil(dialog.getScene().getRoot().minWidth(-1) + frame);
            dialog.setMinWidth(Math.max(DIALOG_MIN_WIDTH, needed));
            // The first time, also leave the font lists some room
            boolean firstShow = dialog.getProperties().putIfAbsent(DIALOG_SIZED_KEY, Boolean.TRUE) == null;
            double comfortable = needed + DIALOG_FONT_ROOM;
            if (dialog.getWidth() < needed || (firstShow && dialog.getWidth() < comfortable)) {
                dialog.setWidth(firstShow ? comfortable : needed);
            }
        });
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
            drawer.setTheme(theme);
            drawer.setText(text, null, null, false, -1);
        });
    }

    private void setPreviewVideo(boolean play) {
        if (drawer instanceof LyricDrawer) {
            ((LyricDrawer) drawer).setPlayVideo(play);
        }
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
