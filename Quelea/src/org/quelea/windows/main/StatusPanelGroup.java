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
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.quelea.Application;
import org.quelea.utils.Utils;

/**
 * A group of status panels that shows all the background tasks Quelea is 
 * currently processing.
 * @author Michael
 */
public class StatusPanelGroup extends JPanel {

    private List<StatusPanel> panels;

    /**
     * Create a new status panel group.
     */
    public StatusPanelGroup() {
        panels = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Add a status panel to the given group.
     * @param label the label to put on the status panel.
     * @return the status panel.
     */
    public synchronized StatusPanel addPanel(String label) {
        StatusPanel panel = new StatusPanel(this, label, panels.size());
        add(panel);
        panels.add(panel);
        Application.get().getMainWindow().validate();
        Application.get().getMainWindow().repaint();
        return panel;
    }

    /**
     * Remove a status panel at the given index.
     * @param index the index of the panel to remove.
     */
    public void removePanel(int index) {
        StatusPanel panel = panels.get(index);
        if (panel != null) {
            remove(panel);
            Application.get().getMainWindow().validate();
            Application.get().getMainWindow().repaint();
            panels.set(index, null);
        }
    }

    /**
     * Testing.
     * @param args 
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        StatusPanelGroup group = new StatusPanelGroup();

        StatusPanel panel = group.addPanel("Hello");
        group.addPanel("Hello2");
        group.addPanel("Hello3");
        group.addPanel("Hello4");

        frame.add(group, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        Utils.sleep(1000);
        panel.done();
    }
}
