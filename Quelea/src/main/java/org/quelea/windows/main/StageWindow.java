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
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.stage.Screen;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Fabian
 */
public class StageWindow extends DisplayStage {
    
    public static StageWindow create() {
        final ObservableList<Screen> monitors = Screen.getScreens();
        final int projectorScreen = QueleaProperties.get().getProjectorScreen();
        StageWindow window = null;     
        if (needsHidding()) {
            LOGGER.log(Level.INFO, "Hiding stage display on monitor 0 (base 0!)");
            window = new StageWindow(Utils.getBoundsFromRect2D(monitors.get(0).getVisualBounds()), true);
            window.hide();
        } else if (QueleaProperties.get().isStageModeCoords()) {
            LOGGER.log(Level.INFO, "Starting stage display: ", QueleaProperties.get().getStageCoords());
            window = new StageWindow(QueleaProperties.get().getStageCoords(), true);
        } else {
            final int stageScreen = QueleaProperties.get().getStageScreen();
            LOGGER.log(Level.INFO, "Starting stage display on monitor {0} (base 0!)", stageScreen);
            window = new StageWindow(Utils.getBoundsFromRect2D(monitors.get(stageScreen).getVisualBounds()), true);
        }

        return window;
    }
    public StageWindow(Bounds area, boolean stageView) {
        super(area, stageView);
    }
    
    public static boolean needsHidding() {
        final ObservableList<Screen> monitors = Screen.getScreens();

        final int stageScreen = QueleaProperties.get().getStageScreen();
        final int monitorNumber = monitors.size();
        
        final boolean windowNeedsHidding;
        if (!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitorNumber || stageScreen < 0)) {
            windowNeedsHidding = true;
        } else {
            windowNeedsHidding = false;
        }
        
        return windowNeedsHidding;
    }
    
    @Override
    protected void windowChanged() {
        
        // this is here incase the stage monitor drops out
        if (needsHidding()) {
            hide();
        }
    }
}
