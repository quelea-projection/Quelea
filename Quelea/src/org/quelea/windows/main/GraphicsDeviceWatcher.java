/*
 * This file is part of Quelea, free projection software for churches.
 * 
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
//import sun.awt.X11.Screen;

/**
 * A singleton class that watches graphics devices. Any other class can register
 * a listener on this class to receive notifications when the list of devices
 * changes.
 *
 * @author Michael
 */
public class GraphicsDeviceWatcher {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static GraphicsDeviceWatcher INSTANCE = new GraphicsDeviceWatcher();
    private List<GraphicsDeviceListener> listeners;
    private ScheduledExecutorService poller;
    private int lastDeviceCount;

    /**
     * Create a new device watcher. Internal use only (singleton.)
     */
    private GraphicsDeviceWatcher() {
        //lastDeviceCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
        lastDeviceCount = javafx.stage.Screen.getScreens().size();
        listeners = new ArrayList<>();
        poller = Executors.newScheduledThreadPool(1);
        poller.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                //GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                ObservableList<javafx.stage.Screen> monitors = javafx.stage.Screen.getScreens();
                int thisDeviceCount = monitors.size();  // devices.length;
                if(thisDeviceCount != lastDeviceCount) {
                    for(GraphicsDeviceListener listener : listeners) {
                        LOGGER.log(Level.INFO, "Number of devices changed, was {0} now {1}", new Object[]{lastDeviceCount, thisDeviceCount});
                        if (thisDeviceCount > lastDeviceCount && QueleaProperties.get().getUseAutoExtend()) {
                            QueleaProperties.get().setProjectorModeScreen();
                            QueleaProperties.get().setProjectorScreen(thisDeviceCount - 1);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    QueleaApp.get().getMainWindow().getOptionsDialog().getDisplaySetupPanel().getProjectorPanel().update();
                                    QueleaApp.get().getMainWindow().getOptionsDialog().getDisplaySetupPanel().getProjectorPanel().setScreen(thisDeviceCount - 1);
                                }
                            });
                        }
                        lastDeviceCount = thisDeviceCount;
                        listener.devicesChanged(monitors);
                    }
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Add a graphics device listener to this watcher.
     *
     * @param listener the listener to add.
     */
    public void addGraphicsDeviceListener(GraphicsDeviceListener listener) {
        listeners.add(listener);
    }
}
