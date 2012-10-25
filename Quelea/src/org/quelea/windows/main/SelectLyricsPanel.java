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
package org.quelea.windows.main;

import java.util.HashSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.TextSection;

/**
 * The panel where the lyrics for different songs can be selected.
 *
 * @author Michael
 */
public class SelectLyricsPanel extends BorderPane implements ContainedPanel {

    private final SelectLyricsList lyricsList;
    private final LivePreviewPanel containerPanel;
    private final LyricCanvas previewCanvas;
    private final SplitPane splitPane;
    private TextDisplayable curDisplayable;

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
        previewCanvas = new LyricCanvas(false, false);
        splitPane.getItems().add(lyricsList);
        splitPane.getItems().add(previewCanvas);
        setCenter(splitPane);
//        containerPanel.registerLyricCanvas(previewCanvas);
        lyricsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TextSection>() {

            @Override
            public void changed(ObservableValue<? extends TextSection> ov, TextSection t, TextSection t1) {
                updateCanvases();
            }
        });
        lyricsList.itemsProperty().addListener(new ChangeListener<ObservableList<TextSection>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<TextSection>> ov, ObservableList<TextSection> t, ObservableList<TextSection> t1) {
                updateCanvases();
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
        curDisplayable = displayable;
        for(TextSection section : displayable.getSections()) {
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
        updateCanvases();
    }

    /**
     * Focus on this panel.
     */
    @Override
    public void focus() {
        lyricsList.requestFocus();
    }

    public LyricCanvas getPreviewCanvas() {
        return previewCanvas;
    }
    
    /**
     * Called to update the contents of the canvases when the list selection
     * changes.
     */
    private void updateCanvases() {
        int selectedIndex = lyricsList.selectionModelProperty().get().getSelectedIndex();
        HashSet<LyricCanvas> canvases = new HashSet<>();
        canvases.add(previewCanvas);
        canvases.addAll(containerPanel.getCanvases());
        for(LyricCanvas canvas : canvases) {
            if(selectedIndex == -1 || selectedIndex >= lyricsList.itemsProperty().get().size()) {
                canvas.setTheme(null);
                canvas.eraseText();
                continue;
            }
            TextSection currentSection = lyricsList.itemsProperty().get().get(selectedIndex);
            if(currentSection.getTempTheme() != null) {
                canvas.setTheme(currentSection.getTempTheme());
            }
            else {
                canvas.setTheme(currentSection.getTheme());
            }
            canvas.setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
//            if(canvas.isStageView()) {
                canvas.setText(curDisplayable, selectedIndex);
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
}
