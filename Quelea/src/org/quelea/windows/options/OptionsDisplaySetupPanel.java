/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.quelea.Application;
import org.quelea.GraphicsDeviceListener;
import org.quelea.GraphicsDeviceWatcher;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.OverlayLyricWindow;
import org.quelea.windows.main.MainWindow;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * @author Michael
 */
public class OptionsDisplaySetupPanel extends JPanel implements PropertyPanel {

    private final SingleDisplayPanel monitorPanel, projectorPanel, stagePanel;

    /**
     * Create a new display setup panel.
     */
    public OptionsDisplaySetupPanel() {
        setName(LabelGrabber.INSTANCE.getLabel("display.options.heading"));
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 3, 30, 0));
        monitorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("control.screen.label")+":", "icons/monitor.png", false, false);
        mainPanel.add(monitorPanel);
        projectorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("projector.screen.label")+":", "icons/projector.png", true, true);
        mainPanel.add(projectorPanel);
        stagePanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("stage.screen.label")+":", "icons/stage.png", true, true);
        mainPanel.add(stagePanel);
        readProperties();
        add(mainPanel, BorderLayout.CENTER);
        
        GraphicsDeviceWatcher.INSTANCE.addGraphicsDeviceListener(new GraphicsDeviceListener() {

            @Override
            public void devicesChanged(GraphicsDevice[] devices) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        monitorPanel.update();
                        projectorPanel.update();
                        stagePanel.update();
                        updatePos();
                    }
                });
             
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        monitorPanel.update();
        projectorPanel.update();
        stagePanel.update();
        monitorPanel.setScreen(QueleaProperties.get().getControlScreen());
        projectorPanel.setCoords(QueleaProperties.get().getProjectorCoords());
        if (!QueleaProperties.get().isProjectorModeCoords()) {
            projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
        }
        stagePanel.setCoords(QueleaProperties.get().getStageCoords());
        if (!QueleaProperties.get().isStageModeCoords()) {
            stagePanel.setScreen(QueleaProperties.get().getStageScreen());
        }
    }
    
    /**
     * Update the position of the windows based on the options set in the 
     * panels.
     */
    private void updatePos() {
        MainWindow mainWindow = Application.get().getMainWindow();
        LyricWindow lyricWindow = Application.get().getLyricWindow();
        LyricWindow stageWindow = Application.get().getStageWindow();
        if(projectorPanel.getOutputBounds() == null) {
            if(lyricWindow != null) {
                lyricWindow.setVisible(false);
            }
        }
        else {
            if(lyricWindow == null) {
                lyricWindow = new LyricWindow(projectorPanel.getOutputBounds(), false);
            }
            final LyricWindow fiLyricWindow = lyricWindow; //Fudge for AIC
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fiLyricWindow.setVisible(true);
                    fiLyricWindow.setArea(projectorPanel.getOutputBounds());
                }
            });
        }
        if(stagePanel.getOutputBounds() == null) {
            if(stageWindow != null) {
                stageWindow.setVisible(false);
            }
        }
        else {
            if(stageWindow == null) {
                stageWindow = new LyricWindow(projectorPanel.getOutputBounds(), true);
            }
            final LyricWindow fiStageWindow = stageWindow; //Fudge for AIC
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fiStageWindow.setVisible(true);
                    fiStageWindow.setArea(stagePanel.getOutputBounds());
                }
            });
        }
        if(!Utils.isFrameOnScreen(mainWindow, monitorPanel.getOutputScreen())) {
            Utils.centreOnMonitor(mainWindow, monitorPanel.getOutputScreen());
        }
        Application.get().getMainWindow().repaint();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        props.setControlScreen(monitorPanel.getOutputScreen());
        props.setProjectorCoords(projectorPanel.getCoords());
        props.setStageCoords(stagePanel.getCoords());
        if(projectorPanel.customPosition()) {
            props.setProjectorModeCoords();
        }
        else {
            props.setProjectorModeScreen();
            props.setProjectorScreen(projectorPanel.getOutputScreen());
        }
        if(stagePanel.customPosition()) {
            props.setStageModeCoords();
        }
        else {
            props.setStageModeScreen();
            props.setStageScreen(stagePanel.getOutputScreen());
        }
        updatePos();
    }

}
