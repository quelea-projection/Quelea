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
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.util.Callback;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * A list displaying the different sections in the song.
 * <p/>
 * @author Michael
 */
public class SelectLyricsList extends ListView<TextSection> {

    private static final Cursor Q_CURSOR = new ImageCursor(new Image("file:icons/edit32.png"));
    private boolean oneLineMode;

    /**
     * Create a new schedule list.
     */
    public SelectLyricsList() {
        oneLineMode = QueleaProperties.get().getOneLineMode();
        setOnMouseMoved(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent t) {
                if((t.isShiftDown() || t.isControlDown()) && !itemsProperty().get().isEmpty()) {
                    setCursor(Q_CURSOR);
                }
                else {
                    setCursor(Cursor.DEFAULT);
                }
            }
        });
        setCellFactory(new Callback<ListView<TextSection>, ListCell<TextSection>>() {
            @Override
            public ListCell<TextSection> call(ListView<TextSection> p) {
                ListCell<TextSection> cell = new ListCell<TextSection>() {
                    @Override
                    protected void updateItem(TextSection t, boolean bln) {
                        super.updateItem(t, bln);
                        if(t != null) {
                            String[] text = t.getText(false, false);
                            StringBuilder builder = new StringBuilder();
                            for(String str : text) {
                                builder.append(str);
                                if(oneLineMode) {
                                    builder.append(" ");
                                }
                                else {
                                    builder.append("\n");
                                }
                            }
                            setText(builder.toString().trim());
                        }
                    }
                };

                return cell;
            }
        });
    }

    /**
     * Set whether this list should use one line mode.
     * <p/>
     * @param val true if it should be in one line mode, false otherwise.
     */
    public void setOneLineMode(boolean val) {
        if(this.oneLineMode == val) {
            return;
        }
        this.oneLineMode = val;
        int selectedIndex = selectionModelProperty().get().getSelectedIndex();
        List<TextSection> elements = new ArrayList<>(itemsProperty().get().size());
        for(int i = 0; i < itemsProperty().get().size(); i++) {
            elements.add(itemsProperty().get().get(i));
        }
        itemsProperty().get().clear();
        for(TextSection section : elements) {
            itemsProperty().get().add(section);
        }
        selectionModelProperty().get().select(selectedIndex);
        scrollTo(selectedIndex);
    }
}
