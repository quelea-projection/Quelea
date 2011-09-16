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
package org.quelea.windows.options;

import org.quelea.Application;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * @author Michael
 */
public class OptionsDisplaySetupPanel extends JPanel implements PropertyPanel {

    private final SingleDisplayPanel monitorPanel, projectorPanel;

    /**
     * Create a new display setup panel.
     */
    public OptionsDisplaySetupPanel() {
        setName("Display");
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 30, 0));
        monitorPanel = new SingleDisplayPanel("Control screen:", "icons/monitor.png", false);
        mainPanel.add(monitorPanel);
        projectorPanel = new SingleDisplayPanel("Projector screen:", "icons/projector.png", true);
        mainPanel.add(projectorPanel);
        readProperties();
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * @inheritDoc
     */
    public final void readProperties() {
        monitorPanel.update();
        projectorPanel.update();
        monitorPanel.setScreen(QueleaProperties.get().getControlScreen());
        projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
    }

    /**
     * @inheritDoc
     */
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        MainWindow mainWindow = Application.get().getMainWindow();
        LyricWindow lyricWindow = Application.get().getLyricWindow();
        props.setControlScreen(getControlDisplay());
        props.setProjectorScreen(getProjectorDisplay());

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        if(getProjectorDisplay() == -1) {
            if(lyricWindow != null) {
                lyricWindow.setVisible(false);
            }
        }
        else {
            if(lyricWindow == null) {
                lyricWindow = new LyricWindow(gds[getProjectorDisplay()].getDefaultConfiguration().getBounds());
            }
            lyricWindow.setVisible(true);
            lyricWindow.setArea(gds[getProjectorDisplay()].getDefaultConfiguration().getBounds());
        }
        if(!Utils.isFrameOnScreen(mainWindow, getControlDisplay())) {
            Utils.centreOnMonitor(mainWindow, getControlDisplay());
        }
    }

    /**
     * Get the display that the control window should be sent to.
     * @return the display that the control window should be sent to.
     */
    public int getControlDisplay() {
        return monitorPanel.getOutputDisplay();
    }

    /**
     * Get the display that the projector window should be sent to.
     * @return the display that the projector window should be sent to.
     */
    public int getProjectorDisplay() {
        return projectorPanel.getOutputDisplay();
    }

}
