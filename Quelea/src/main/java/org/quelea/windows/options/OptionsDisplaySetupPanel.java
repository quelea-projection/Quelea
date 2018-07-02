/*
 * This file is part of Quelea, free projection software for churches.
 * 
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

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.GraphicsDeviceListener;
import org.quelea.windows.main.GraphicsDeviceWatcher;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * <p/>
 * @author Michael
 */
public class OptionsDisplaySetupPanel extends GridPane implements PropertyPanel {

    private final SingleDisplayPanel monitorPanel, projectorPanel, stagePanel;

    /**
     * Create a new display setup panel.
     */
    public OptionsDisplaySetupPanel() {
        setHgap(30);
        setVgap(10);
        setPadding(new Insets(30));
        monitorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("control.screen.label") + ":", "icons/monitor.png", false, false);
        GridPane.setConstraints(monitorPanel, 1, 1);
        getChildren().add(monitorPanel);
        projectorPanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("projector.screen.label") + ":", "icons/projector.png", true, true);
        GridPane.setConstraints(projectorPanel, 2, 1);
        getChildren().add(projectorPanel);
        stagePanel = new SingleDisplayPanel(LabelGrabber.INSTANCE.getLabel("stage.screen.label") + ":", "icons/stage.png", true, true);
        GridPane.setConstraints(stagePanel, 3, 1);
        getChildren().add(stagePanel);
        readProperties();

        OptionsDisplaySetupPanel panel = this;
        GraphicsDeviceWatcher.INSTANCE.addGraphicsDeviceListener(new GraphicsDeviceListener() {
//            @Override
//            public void devicesChanged(GraphicsDevice[] devices) {
//                Platform.runLater(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        monitorPanel.update();
//                        projectorPanel.update();
//                        stagePanel.update();
//                        updatePos();
//                    }
//                });
//             
//            }
            @Override
            public void devicesChanged(ObservableList<Screen> devices) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        monitorPanel.update();
                        projectorPanel.update();
                        stagePanel.update();
                        updatePos();
                        
                        // want to make the this panel the next panel the users sees upon entering the options
                        // as they will most likely want to change this next
                        QueleaApp.get().getMainWindow().getOptionsDialog().setCurrentPanel(panel);
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
        if(!QueleaProperties.get().isProjectorModeCoords()) {
            projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
        }
        stagePanel.setCoords(QueleaProperties.get().getStageCoords());
        if(!QueleaProperties.get().isStageModeCoords()) {
            stagePanel.setScreen(QueleaProperties.get().getStageScreen());
        }
    }

    /**
     * Update the position of the windows based on the options set in the
     * panels.
     */
    private void updatePos() {
//        MainWindow mainWindow = Application.get().getMainWindow();
        DisplayStage appWindow = QueleaApp.get().getProjectionWindow();
        DisplayStage stageWindow = QueleaApp.get().getStageWindow();
        if(projectorPanel.getOutputBounds() == null) {
            if(appWindow != null) {
                appWindow.setFullScreenAlwaysOnTopImmediate(false);
                appWindow.hide();
            }
        }
        else {
            if(appWindow == null) {
                appWindow = new DisplayStage(projectorPanel.getOutputBounds(), false);
            }
            final DisplayStage fiLyricWindow = appWindow; //Fudge for AIC
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    fiLyricWindow.setAreaImmediate(projectorPanel.getOutputBounds());
                    if(!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                        fiLyricWindow.show();
                    }
                    
                    // non-custom positioned windows are fullscreen
                    if (!projectorPanel.customPosition()) {
                        fiLyricWindow.setFullScreenAlwaysOnTop(true);
                    }
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
                stageWindow = new DisplayStage(projectorPanel.getOutputBounds(), true);
            }
            final DisplayStage fiStageWindow = stageWindow; //Fudge for AIC
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    fiStageWindow.setArea(stagePanel.getOutputBounds());
                    if(!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                        fiStageWindow.show();
                    }
                }
            });
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VLCWindow.INSTANCE.refreshPosition();
            }
        });
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
    
    public SingleDisplayPanel getProjectorPanel() {
        return projectorPanel;
    }
}
