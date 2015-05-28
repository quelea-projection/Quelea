/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.widgets;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

/**
 *
 * @author Michael
 */
public class BooleanCell<E> extends TableCell<E, Boolean> {

    private CheckBox checkBox;

    public BooleanCell() {

        checkBox = new CheckBox();
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(isEditing()) {
                    commitEdit(newValue == null ? false : newValue);
                }
            }
        });
        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setEditable(true);


    }

    @Override
    public void startEdit() {
        super.startEdit();
        if(isEmpty()) {
            return;
        }
        checkBox.setDisable(false);
        checkBox.requestFocus();

    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        checkBox.setDisable(true);
    }

    @Override
    public void commitEdit(Boolean value) {
        super.commitEdit(value);
        checkBox.setDisable(true);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if(empty) {
            setText(null);
            setGraphic(null);
        }
        else if(!isEmpty()) {
            checkBox.setSelected(item);
        }
    }
}
