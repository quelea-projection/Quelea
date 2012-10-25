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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * A fully fleshed out class that allows for context menus to be shown on right
 * click.
 */
public class ContextMenuListCell<T> extends ListCell<T> {

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ContextMenu contextMenu) {
        return forListView(contextMenu, null);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final ContextMenu contextMenu, final Callback<ListView<T>, ListCell<T>> cellFactory) {
        return new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> listView) {
                ListCell<T> cell = cellFactory == null ? new DefaultListCell<T>() : cellFactory.call(listView);
                cell.setContextMenu(contextMenu);
                return cell;
            }
        };
    }

    public ContextMenuListCell(ContextMenu contextMenu) {
        setContextMenu(contextMenu);
    }
}
