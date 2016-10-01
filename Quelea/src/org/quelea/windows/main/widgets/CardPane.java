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
package org.quelea.windows.main.widgets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Emulates (sort of) a swing card layout - JavaFX doesn't have one but we can
 * trivially create one out of a StackPane.
 * <p/>
 * @author Michael
 */
public class CardPane<T extends Node> extends StackPane implements Iterable<T> {

    private HashMap<String, T> items = new HashMap<>();
    private T currentPane = null;

    /**
     * Add a node to this card pane.
     * <p/>
     * @param node the node to add.
     * @param label the label used for selecting this node.
     */
    public void add(T node, String label) {
        items.put(label, node);
        node.setVisible(false);
        getChildren().add(node);
    }

    /**
     * Remove a node on this card pane.
     * <p/>
     * @param label the label of the node to remove.
     */
    public void remove(String label) {
        getChildren().remove(items.get(label));
        items.remove(label);
    }

    /**
     * Get all the panels currently on the card pane.
     * <p/>
     * @return
     */
    public Collection<T> getPanels() {
        return items.values();
    }

    /**
     * Show the node with the given label.
     * <p/>
     * @param label the label whose node to show.
     * @throws IllegalArgumentException if the label isn't valid.
     */
    public void show(String label) {
        for(T node : items.values()) {
            node.setVisible(false);
        }
        currentPane = items.get(label);
        if(currentPane == null) {
            throw new IllegalArgumentException("Label " + label + " doesn't exist in this card pane!");
        }
        else {
            currentPane.setVisible(true);
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

    @Override
    public Iterator<T> iterator() {
        return getPanels().iterator();
    }
}
