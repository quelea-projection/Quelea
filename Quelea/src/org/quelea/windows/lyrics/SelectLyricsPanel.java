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
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.WordDrawer;
import org.quelea.windows.main.widgets.DisplayPreview;
import org.quelea.windows.stage.StageDrawer;

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
    private final LyricDrawer lyricDrawer;
    private final StageDrawer stageDrawer;

    /**
     * Create a new lyrics panel.
     * <p/>
     * @param containerPanel the container panel this panel is contained within.
     */
    public SelectLyricsPanel(LivePreviewPanel containerPanel) {
        lyricDrawer = new LyricDrawer();
        stageDrawer = new StageDrawer();
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        lyricsList = new SelectLyricsList();
        previewCanvas = new DisplayCanvas(false, false, false, this::updateCanvas, Priority.LOW);
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
//        
    }

    public void selectFirst() {
        if (lyricsList.getItems().size() > 0) {
            lyricsList.selectionModelProperty().get().clearAndSelect(0);
        }
    }

    public void selectLast() {
        if (lyricsList.getItems().size() > 0) {
            lyricsList.selectionModelProperty().get().clearSelection();
            lyricsList.selectionModelProperty().get().selectLast();
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
    public void showDisplayable(TextDisplayable displayable, int index) {
//        removeCurrentDisplayable();
        setCurrentDisplayable(displayable);
        lyricsList.itemsProperty().get().clear();
        lyricsList.setShowQuickEdit(displayable instanceof SongDisplayable);
        for (TextSection section : displayable.getSections()) {
            lyricsList.itemsProperty().get().add(section);
        }
        lyricsList.selectionModelProperty().get().select(index);
        lyricsList.scrollTo(index);
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
     * Get the length of the item on this panel.
     * <p/>
     * @return the length of the item on this panel.
     */
    public int getSlideCount() {
        return lyricsList.getItems().size();
    }

    /**
     * Advances the current slide.
     * <p/>
     */
    public void advance() {
        int start = getIndex();
        lyricsList.selectionModelProperty().get().selectNext();
        updateCanvas();

        int end = getIndex();
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        boolean lastSongTest = qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1);
        if (start == end && QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !lastSongTest) {
            qmp.getPreviewPanel().goLive();
        }
    }

    /**
     * Moves to the previous slide.
     * <p/>
     */
    public void previous() {
        int start = getIndex();
        lyricsList.selectionModelProperty().get().selectPrevious();
        updateCanvas();

        int end = getIndex();
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        //Check to see if first song first verse
        boolean fsfv = qmp.getSchedulePanel().getScheduleList().getItems().get(0) == qmp.getLivePanel().getDisplayable()
                && (qmp.getLivePanel().getLyricsPanel().getLyricsList().getSelectionModel().getSelectedIndex() == 0);
        if ((start == end || qmp.getLivePanel().getLyricsPanel().getLyricsList().getItems().size() == 1)
                && QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !fsfv) {
            //Assuming preview panel is one ahead, and should be one behind
            int index = qmp.getSchedulePanel().getScheduleList().getSelectionModel().getSelectedIndex();
            if (qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1)) {
                index -= 1;
            } else {
                index -= 2;
            }
            if (index >= 0) {
                qmp.getSchedulePanel().getScheduleList().getSelectionModel().clearAndSelect(index);
                qmp.getPreviewPanel().selectLastLyric();
                qmp.getPreviewPanel().goLive();
                //qmp.getSchedulePanel().getScheduleList().getSelectionModel().clearAndSelect(index);

            }
        }
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
        lyricDrawer.clear();
    }

    /**
     * Called to updateOnSizeChange the contents of the canvases when the list
     * selection changes.
     */
    @Override
    public void updateCanvas() {
        int selectedIndex = lyricsList.selectionModelProperty().get().getSelectedIndex();
        for (DisplayCanvas canvas : getCanvases()) {
            WordDrawer drawer;
            if (canvas.isStageView()) {
                drawer = stageDrawer;
            } else {
                drawer = lyricDrawer;
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
                drawer.setTheme(currentSection.getTempTheme());
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
        if (canvas.isStageView()) {
            return stageDrawer;
        } else {
            return lyricDrawer;
        }
    }
}
