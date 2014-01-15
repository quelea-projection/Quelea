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

import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class DefaultListCell<T> extends ListCell<T> {

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if(empty) {
            setText(null);
            setGraphic(null);
        }
        else if(item instanceof Node) {
            setText(null);
            Node currentNode = getGraphic();
            Node newNode = (Node) item;
            if(currentNode == null || !currentNode.equals(newNode)) {
                setGraphic(newNode);
            }
        }
        else {
            setText(item == null ? "null" : item.toString());
            setGraphic(null);
        }
    }
}
