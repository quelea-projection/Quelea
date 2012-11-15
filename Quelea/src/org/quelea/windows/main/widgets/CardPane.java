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

import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Emulates (sort of) a swing card layout - JavaFX doesn't have one but we can
 * trivially create one out of a StackPane.
 * <p/>
 * @author Michael
 */
public class CardPane extends StackPane {

    private HashMap<String, Node> items = new HashMap<>();
    private Node currentPane = null;

    /**
     * Add a node to this card pane.
     * <p/>
     * @param node the node to add.
     * @param label the label used for selecting this node.
     */
    public void add(Node node, String label) {
        items.put(label, node);
    }

    /**
     * Show the node with the given label.
     * <p/>
     * @param label the label whose node to show.
     * @throws IllegalArgumentException if the label isn't valid.
     */
    public void show(String label) {
        getChildren().clear();
        currentPane = items.get(label);
        if(currentPane == null) {
            throw new IllegalArgumentException("Label " + label + " doesn't exist in this card pane!");
        }
        else {
            getChildren().add(currentPane);
        }
    }

    /**
     * Get the current panel being shown.
     * <p/>
     * @return the currently shown panel, or null if none is being shown.
     */
    public Node getCurrentPane() {
        return currentPane;
    }
}
