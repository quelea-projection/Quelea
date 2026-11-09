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
package org.quelea.server.v2;

import org.quelea.windows.main.MainWindow;

/**
 * The v2 remote core: the event bus, the watcher that feeds it and the
 * control that the API calls. Created once the main window exists.
 */
public final class RemoteCore {

    private final QueleaControl control;
    private final RemoteEventBus bus;

    private RemoteCore(QueleaControl control, RemoteEventBus bus) {
        this.control = control;
        this.bus = bus;
    }

    /**
     * Attach to the main window and start publishing events. May be called
     * from any thread; the listeners are attached on the FX thread.
     *
     * @param window the main window, fully built.
     * @return the core.
     * @throws FxCall.TimeoutException if the FX thread did not respond.
     */
    public static RemoteCore install(MainWindow window) {
        RemoteEventBus bus = RemoteEventBus.get();
        ItemIds ids = new ItemIds();
        LiveStateWatcher watcher = new LiveStateWatcher(window.getMainPanel(), window.getMainToolbar(), ids, bus);
        FxCall.fx().run(watcher::install);
        QueleaControl control = new FxQueleaControl(window.getMainPanel(), window.getMainToolbar(), ids,
                watcher.reader(), FxCall.fx());
        return new RemoteCore(control, bus);
    }

    /**
     * @return what the API can see and do in Quelea.
     */
    public QueleaControl control() {
        return control;
    }

    /**
     * @return the bus that carries changes to remote clients.
     */
    public RemoteEventBus bus() {
        return bus;
    }
}
