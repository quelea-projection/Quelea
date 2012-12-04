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

import org.quelea.windows.main.DisplayCanvas;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.image.AbstractPanel;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.LivePreviewPanel;

/**
 * The panel where the lyrics for different songs can be selected.
 *
 * @author Michael
 */
public class SelectLyricsPanel extends AbstractPanel {

    private final SelectLyricsList lyricsList;
    private final DisplayCanvas previewCanvas;
    private final SplitPane splitPane;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private DisplayableDrawer drawer = new LyricDrawer();
    /**
     * Create a new lyrics panel.
     *
     * @param containerPanel the container panel this panel is contained within.
     */
    public SelectLyricsPanel(LivePreviewPanel containerPanel) {
        this.containerPanel = containerPanel;
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        lyricsList = new SelectLyricsList();
        previewCanvas = new DisplayCanvas(false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateOnSizeChange() {
                updateCanvas();
            }
        }, "SelectLyricsPanel preview");
        splitPane.getItems().add(lyricsList);
        splitPane.getItems().add(previewCanvas);
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
    }

    /**
     * Set one line mode on or off.
     *
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
     *
     * @param displayable the displayable to show.
     * @param index the index of the displayable to show.
     */
    public void showDisplayable(TextDisplayable displayable, int index) {
        clear();
        currentDisplayable = displayable;
        for (TextSection section : displayable.getSections()) {
            lyricsList.itemsProperty().get().add(section);
        }
        lyricsList.selectionModelProperty().get().select(index);
        lyricsList.scrollTo(index);
    }

    /**
     * Get the current displayed index.
     *
     * @return the current displayed index.
     */
    public int getIndex() {
        return lyricsList.selectionModelProperty().get().getSelectedIndex();
    }

    /**
     * Get the lyrics list on this panel.
     *
     * @return the select lyrics list.
     */
    public SelectLyricsList getLyricsList() {
        return lyricsList;
    }

    /**
     * Clear the current panel.
     */
    @Override
    public void clear() {
        lyricsList.itemsProperty().get().clear();
        super.clear();
    }

    /**
     * Focus on this panel.
     */
    @Override
    public void focus() {
        lyricsList.requestFocus();
    }

    public DisplayCanvas getPreviewCanvas() {
        return previewCanvas;
    }

    /**
     * Called to updateOnSizeChange the contents of the canvases when the list
     * selection changes.
     */
    @Override
    public void updateCanvas() {
        int selectedIndex = lyricsList.selectionModelProperty().get().getSelectedIndex();
        for (DisplayCanvas canvas : getCanvases()) {
            LyricDrawer drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            if (selectedIndex == -1 || selectedIndex >= lyricsList.itemsProperty().get().size()) {

                drawer.setTheme(null);
                drawer.eraseText();
                continue;
            }
            TextSection currentSection = lyricsList.itemsProperty().get().get(selectedIndex);
            if (currentSection.getTempTheme() != null) {
                drawer.setTheme(currentSection.getTempTheme());
            } else {
                drawer.setTheme(currentSection.getTheme());
            }
            drawer.setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
//            if(canvas.isStageView()) {
            drawer.setText((TextDisplayable) currentDisplayable, selectedIndex);
//            }
//            else {
//                canvas.setText(currentSection.getText(false, false), currentSection.getSmallText());
//            }
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
