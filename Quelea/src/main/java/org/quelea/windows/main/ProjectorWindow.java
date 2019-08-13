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

import java.util.logging.Level;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.stage.Screen;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.multimedia.VLCWindow;
import org.quelea.services.utils.LoggerUtils;
import java.util.logging.Logger;

/**
 *
 * @author Fabian
 */
public class ProjectorWindow extends DisplayStage {
    
    public static ProjectorWindow create() {
        final ObservableList<Screen> monitors = Screen.getScreens();
        final int projectorScreen = QueleaProperties.get().getProjectorScreen();
        ProjectorWindow window = null;
        if (needsHidding()) {
            LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
            window = new ProjectorWindow(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), false);
            window.hide();
        } else if (QueleaProperties.get().isProjectorModeCoords()) {
            LOGGER.log(Level.INFO, "Starting projector display: ", QueleaProperties.get().getProjectorCoords());
            window = new ProjectorWindow(QueleaProperties.get().getProjectorCoords(), false);
        } else {
            LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
            window = new ProjectorWindow(Utils.getBoundsFromRect2D(monitors.get(projectorScreen).getBounds()), false);
            window.setFullScreenAlwaysOnTop(true);
        }
        
        return window;
    }
    
    public ProjectorWindow(Bounds area, boolean stageView) {
        super(area, stageView);
    }
    
    public static boolean needsHidding() {
        final ObservableList<Screen> monitors = Screen.getScreens();
        final int projectorScreen = QueleaProperties.get().getProjectorScreen();
        final int monitorNumber = monitors.size();
        
        final boolean windowNeedsHidding;
        if (!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitorNumber || projectorScreen < 0)) {
            windowNeedsHidding = true;
        } else {
            windowNeedsHidding = false;
        }
        
        return windowNeedsHidding;
    }
    
    @Override
    protected void windowChanged() {
        
        // this is here incase the projector monitor drops out
        // which often happens at my church when another TV is plugged in or out
        if (needsHidding()) {
            hide();
            VLCWindow.INSTANCE.hide();
        }
        else {
            VLCWindow.INSTANCE.refreshPosition();
        }
    }
}
