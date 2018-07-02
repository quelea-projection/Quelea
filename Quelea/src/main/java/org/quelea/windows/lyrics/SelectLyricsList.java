/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.lyrics;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;

/**
 * A list displaying the different sections in the song.
 * <p/>
 * @author Michael
 */
public class SelectLyricsList extends ListView<TextSection> {

    private static final Cursor Q_CURSOR = new ImageCursor(new Image("file:icons/edit32.png"), 6, 27);
    private boolean oneLineMode;
    private boolean showQuickEdit;
    private int quickEditIndex;

    /**
     * Create a new schedule list.
     */
    public SelectLyricsList() {
        oneLineMode = QueleaProperties.get().getOneLineMode();
        setOnMouseMoved((MouseEvent t) -> {
            if (showQuickEdit && (t.isShiftDown() || t.isControlDown()) && !itemsProperty().get().isEmpty()) {
                setCursor(Q_CURSOR);
            } else {
                setCursor(Cursor.DEFAULT);
            }
            getScene().setOnKeyPressed((KeyEvent t1) -> {
                if (showQuickEdit && (t1.isShiftDown() || t1.isControlDown()) && !itemsProperty().get().isEmpty()) {
                    setCursor(Q_CURSOR);
                } else {
                    setCursor(Cursor.DEFAULT);
                }
            });
            getScene().setOnKeyReleased((KeyEvent t1) -> {
                if (showQuickEdit && (t1.isShiftDown() || t1.isControlDown()) && !itemsProperty().get().isEmpty()) {
                    setCursor(Q_CURSOR);
                } else {
                    setCursor(Cursor.DEFAULT);
                }
            });
        });
        setCellFactory((ListView<TextSection> p) -> {
            ListCell<TextSection> cell = new LyricListCell(this);
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.isControlDown()) {
                    if (!cell.isEmpty()) {
                        quickEditIndex = getItems().indexOf(cell.getItem());
                        event.consume();
                    }
                }
            });
            return cell;
        });
    }

    public int getQuickEditIndex() {
        return quickEditIndex;
    }

    public void setShowQuickEdit(boolean showQuickEdit) {
        this.showQuickEdit = showQuickEdit;
    }

    /**
     * Set whether this list should use one line mode.
     * <p/>
     * @param val true if it should be in one line mode, false otherwise.
     */
    public void setOneLineMode(boolean val) {
        if (this.oneLineMode == val) {
            return;
        }
        this.oneLineMode = val;
        int selectedIndex = selectionModelProperty().get().getSelectedIndex();
        List<TextSection> elements = new ArrayList<>(itemsProperty().get().size());
        for (int i = 0; i < itemsProperty().get().size(); i++) {
            elements.add(itemsProperty().get().get(i));
        }
        itemsProperty().get().clear();
        for (TextSection section : elements) {
            itemsProperty().get().add(section);
        }
        selectionModelProperty().get().select(selectedIndex);
        scrollTo(selectedIndex);
    }
}
