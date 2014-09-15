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
package org.quelea.windows.lyrics;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePreviewPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * The panel where the lyrics for different songs can be selected.
 * <p/>
 * @author Michael
 */
public class SelectLyricsPanel extends AbstractPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final SelectLyricsList lyricsList;
    private final DisplayCanvas previewCanvas;
    private final SplitPane splitPane;
    private final LyricDrawer drawer;

    /**
     * Create a new lyrics panel.
     * <p/>
     * @param containerPanel the container panel this panel is contained within.
     */
    public SelectLyricsPanel(LivePreviewPanel containerPanel) {
        drawer = new LyricDrawer();
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        lyricsList = new SelectLyricsList();
        previewCanvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateCanvas();
            }
        }, Priority.LOW, false, null);
        DisplayPreview preview = new DisplayPreview(previewCanvas);
        splitPane.setStyle("-fx-background-color: rgba(0, 0, 0);");
        splitPane.getItems().add(lyricsList);
        splitPane.getItems().add(preview);

        setCenter(splitPane);
        registerDisplayCanvas(previewCanvas);
        lyricsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TextSection>() {
            @Override
            public void changed(ObservableValue<? extends TextSection> ov, TextSection t, TextSection t1) {
                updateCanvas();
            }
        });
        lyricsList.itemsProperty().addListener(new ChangeListener<ObservableList<TextSection>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<TextSection>> ov, ObservableList<TextSection> t, ObservableList<TextSection> t1) {
                updateCanvas();
            }
        });
        lyricsList.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode().equals(KeyCode.PAGE_DOWN)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
                }
                else if(t.getCode().equals(KeyCode.PAGE_UP)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
                }
            }
        });
    }

    public void selectFirst() {
        if (lyricsList.getItems().size() > 0) {
            lyricsList.selectionModelProperty().get().clearAndSelect(0);
        }
    }

    public void select(int index) {
        if (index >= 0 && index < lyricsList.getItems().size()) {
            lyricsList.selectionModelProperty().get().select(index);
        }
    }

    /**
     * Set one line mode on or off.
     * <p/>
     * @param on if one line mode should be turned on, false otherwise.
     */
    public void setOneLineMode(boolean on) {
        lyricsList.setOneLineMode(on);
    }

    @Override
    public void requestFocus() {
        lyricsList.requestFocus();
    }

    /**
     * Show a given text displayable on this panel.
     * <p/>
     * @param displayable the displayable to show.
     * @param index the index of the displayable to show.
     */
    public void showDisplayable(TextDisplayable displayable, final int index) {
//        removeCurrentDisplayable();
        setCurrentDisplayable(displayable);
        lyricsList.setShowQuickEdit(displayable instanceof SongDisplayable);
        for (TextSection section : displayable.getSections()) {
            lyricsList.itemsProperty().get().add(section);
        }
        lyricsList.selectionModelProperty().get().select(index);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lyricsList.scrollTo(index);
            }
        });

    }

    /**
     * Get the current displayed index.
     * <p/>
     * @return the current displayed index.
     */
    public int getIndex() {
        return lyricsList.selectionModelProperty().get().getSelectedIndex();
    }

    /**
     * Advances the current slide.
     * <p/>
     */
    public void advance() {
        lyricsList.selectionModelProperty().get().selectNext();
        updateCanvas();
    }

    /**
     * Moves to the previous slide.
     * <p/>
     */
    public void previous() {
        lyricsList.selectionModelProperty().get().selectPrevious();
        updateCanvas();
    }

    /**
     * Get the lyrics list on this panel.
     * <p/>
     * @return the select lyrics list.
     */
    public SelectLyricsList getLyricsList() {
        return lyricsList;
    }

    /**
     * Clear the current panel.
     */
    @Override
    public void removeCurrentDisplayable() {
        super.removeCurrentDisplayable();
        lyricsList.itemsProperty().get().clear();
        drawer.clear();
    }

    /**
     * Called to updateOnSizeChange the contents of the canvases when the list
     * selection changes.
     */
    @Override
    public void updateCanvas() {
        int selectedIndex = lyricsList.selectionModelProperty().get().getSelectedIndex();
        int nextIndex = selectedIndex + 1;
        for (DisplayCanvas canvas : getCanvases()) {
            if (canvas.isStageView()) {
                if (QueleaProperties.get().getStageUsePreview()) {
                    drawer.setCanvas(canvas.getPreviewCanvas());
                    if ((nextIndex >= lyricsList.itemsProperty().get().size())) {
                        updatePreview(canvas.getPreviewCanvas());
                    } else {
                        AbstractPanel.setIsNextPreviewed(false);
                        TextSection nextSection = lyricsList.itemsProperty().get().get(nextIndex);
                        if (nextSection.getTempTheme() != null) {
                            drawer.setTheme(nextSection.getTempTheme());
                        } else {
                            ThemeDTO newTheme = nextSection.getTheme();
                            drawer.setTheme(newTheme);
                        }
                        drawer.setCapitaliseFirst(nextSection.shouldCapitaliseFirst());
                        drawer.setText((TextDisplayable) getCurrentDisplayable(), nextIndex);
                        canvas.setCurrentDisplayable(getCurrentDisplayable());
                    }
                } else {
                    canvas.getPreviewCanvas().clearCurrentDisplayable();
                }

            }
            drawer.setCanvas(canvas);
            if (selectedIndex == -1 || selectedIndex >= lyricsList.itemsProperty().get().size()) {
                if (!canvas.getPlayVideo()) {
                    drawer.setTheme(ThemeDTO.DEFAULT_THEME);
                }
                drawer.eraseText();
                continue;
            }
            TextSection currentSection = lyricsList.itemsProperty().get().get(selectedIndex);
            if (currentSection.getTempTheme() != null) {
                if (currentSection.getTheme().getOverrideTheme()) {
                    drawer.setTheme(currentSection.getTheme());
                } else {
                    drawer.setTheme(currentSection.getTempTheme());
                }
            } else {
                ThemeDTO newTheme = currentSection.getTheme();
                drawer.setTheme(newTheme);

            }
            drawer.setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
            drawer.setText((TextDisplayable) getCurrentDisplayable(), selectedIndex);
            canvas.setCurrentDisplayable(getCurrentDisplayable());
        }
    }

    @Override
    public int getCurrentIndex() {
        return lyricsList.getSelectionModel().getSelectedIndex();
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        return drawer;
    }
}
