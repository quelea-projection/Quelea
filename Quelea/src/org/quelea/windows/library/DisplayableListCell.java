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
package org.quelea.windows.library;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * A fully fleshed out class that allows for context menus to be shown on right
 * click.
 * <p/>
 * @author Michael
 */
public class DisplayableListCell<T> extends ListCell<T> {

    /**
     * Provide a callback that sets the given context menu on each cell.
     * <p/>
     * @param <T> the generic type of the cell.
     * @param contextMenu the context menu to show.
     * @return a callback that sets the given context menu on each cell.
     */
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ContextMenu contextMenu) {
        return forListView(contextMenu, null, null);
    }

    /**
     * Provide a callback that sets the given context menu on each cell, if and
     * only if the constraint given passes. If the constraint is null, it will
     * always pass.
     * <p/>
     * @param <T> the generic type of the cell.
     * @param contextMenu the context menu to show.
     * @param cellFactory the cell factory to use.
     * @param constraint the constraint placed on showing the context menu - it
     * will only be shown if this constraint passes, or it is null.
     * @return a callback that sets the given context menu on each cell.
     */
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final ContextMenu contextMenu, final Callback<ListView<T>, ListCell<T>> cellFactory,
            final Constraint<T> constraint) {
        return new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> listView) {
                final ListCell<T> cell = cellFactory == null ? new DefaultListCell<T>() : cellFactory.call(listView);
                cell.itemProperty().addListener(new ChangeListener<T>() {
                    @Override
                    public void changed(ObservableValue<? extends T> ov, T oldVal, T newVal) {
                        if(newVal == null || (constraint != null && !constraint.isTrue(newVal))) {
                            cell.setContextMenu(null);
                        }
                        else {
                            cell.setContextMenu(contextMenu);
                        }
                    }
                });
                return cell;
            }
        };
    }
}