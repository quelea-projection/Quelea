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
package org.quelea;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.LoggerUtils;

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
        lastDeviceCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
        listeners = new ArrayList<>();
        poller = Executors.newScheduledThreadPool(1);
        poller.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                int thisDeviceCount = devices.length;
                if(thisDeviceCount != lastDeviceCount) {
                    for(GraphicsDeviceListener listener : listeners) {
                        LOGGER.log(Level.INFO, "Number of devices changed, was {0} now {1}", new Object[]{lastDeviceCount, thisDeviceCount});
                        lastDeviceCount = thisDeviceCount;
                        listener.devicesChanged(devices);
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
