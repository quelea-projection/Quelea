/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.VBox;
import javax.swing.JFrame;
import org.quelea.Application;
import org.quelea.utils.Utils;

/**
 * A group of status panels that shows all the background tasks Quelea is 
 * currently processing.
 * @author Michael
 */
public class StatusPanelGroup extends VBox {

    private List<StatusPanel> panels;

    /**
     * Create a new status panel group.
     */
    public StatusPanelGroup() {
        panels = new ArrayList<>();
    }

    /**
     * Add a status panel to the given group.
     * @param label the label to put on the status panel.
     * @return the status panel.
     */
    public synchronized StatusPanel addPanel(String label) {
        StatusPanel panel = new StatusPanel(this, label, panels.size());
        getChildren().add(panel);
        panels.add(panel);
        return panel;
    }

    /**
     * Remove a status panel at the given index.
     * @param index the index of the panel to remove.
     */
    public void removePanel(int index) {
        StatusPanel panel = panels.get(index);
        if (panel != null) {
            getChildren().remove(panel);
            panels.set(index, null);
        }
    }

}
