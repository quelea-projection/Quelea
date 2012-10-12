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

import java.awt.GraphicsDevice;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import org.quelea.QueleaApp;
import org.quelea.GraphicsDeviceListener;
import org.quelea.GraphicsDeviceWatcher;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.main.LyricWindow;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * @author Michael
 */
public class OptionsDisplaySetupPanel extends GridPane implements PropertyPanel {

    private final SingleDisplayPanel monitorPanel, projectorPanel, stagePanel;

    /**
     * Create a new display setup panel.
     */
    public OptionsDisplaySetupPanel() {
        setHgap(30);
        monitorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("control.screen.label")+":", "icons/monitor.png", false, false);
        GridPane.setConstraints(monitorPanel, 1, 1);
        getChildren().add(monitorPanel);
        projectorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("projector.screen.label")+":", "icons/projector.png", true, true);
        GridPane.setConstraints(projectorPanel, 2, 1);
        getChildren().add(projectorPanel);
        stagePanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("stage.screen.label")+":", "icons/stage.png", true, true);
        GridPane.setConstraints(stagePanel, 3, 1);
        getChildren().add(stagePanel);
        readProperties();
        
        GraphicsDeviceWatcher.INSTANCE.addGraphicsDeviceListener(new GraphicsDeviceListener() {

            @Override
            public void devicesChanged(GraphicsDevice[] devices) {
                Platform.runLater(new Runnable() {

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
//        MainWindow mainWindow = Application.get().getMainWindow();
        LyricWindow lyricWindow = QueleaApp.get().getLyricWindow();
        LyricWindow stageWindow = QueleaApp.get().getStageWindow();
        if(projectorPanel.getOutputBounds() == null) {
            if(lyricWindow != null) {
                lyricWindow.hide();
            }
        }
        else {
            if(lyricWindow == null) {
                lyricWindow = new LyricWindow(projectorPanel.getOutputBounds(), false);
            }
            final LyricWindow fiLyricWindow = lyricWindow; //Fudge for AIC
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    fiLyricWindow.setArea(projectorPanel.getOutputBounds());
                    fiLyricWindow.show();
                }
            });
        }
        if(stagePanel.getOutputBounds() == null) {
            if(stageWindow != null) {
                stageWindow.hide();
            }
        }
        else {
            if(stageWindow == null) {
                stageWindow = new LyricWindow(projectorPanel.getOutputBounds(), true);
            }
            final LyricWindow fiStageWindow = stageWindow; //Fudge for AIC
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    fiStageWindow.show();
                    fiStageWindow.setArea(stagePanel.getOutputBounds());
                }
            });
        }
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
