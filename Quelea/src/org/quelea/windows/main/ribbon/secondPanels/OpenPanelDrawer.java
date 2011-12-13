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
package org.quelea.windows.main.ribbon.secondPanels;

import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import org.quelea.languages.LabelGrabber;

/**
 * PanelDrawer...
 * @author Michael
 */
public class OpenPanelDrawer implements SecondPanelDrawer {

    /**
     * Draw onto the JPanel.
     * @param panel the panel to draw onto.
     */
    @Override
    public void draw(JPanel panel) {
        panel.removeAll();
        Graphics2D graphics = (Graphics2D) panel.getGraphics();
        graphics.clearRect(0, 0, panel.getWidth(), panel.getHeight());
        graphics.setFont(new Font("Verdana", 0, 20));
        graphics.drawString(LabelGrabber.INSTANCE.getLabel("open.schedule.text"), 20, 40);
        graphics.setFont(new Font("Verdana", 0, 14));
        graphics.drawString(LabelGrabber.INSTANCE.getLabel("open.existing.schedule.text"), 20, 60);
    }

}
