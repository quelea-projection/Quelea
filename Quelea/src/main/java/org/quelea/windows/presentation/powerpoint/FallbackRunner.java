/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 * Uses the fast {@link PowerPointBridge} and falls back to one osascript
 * process per command if the bridge itself breaks. Errors from PowerPoint
 * (not running, no permission, ...) are passed on as they are.
 */
class FallbackRunner implements AppleScriptRunner {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final PowerPointBridge bridge;
    private final AppleScriptRunner fallback;

    FallbackRunner(PowerPointBridge bridge, AppleScriptRunner fallback) {
        this.bridge = bridge;
        this.fallback = fallback;
    }

    /**
     * @return true while the fast bridge is in use (cheap enough to poll).
     */
    boolean isFast() {
        return bridge != null && bridge.isUsable();
    }

    @Override
    public String run(long timeoutMillis, String... args) throws PowerPointException {
        if (isFast()) {
            try {
                return bridge.run(timeoutMillis, args);
            } catch (PowerPointBridge.BridgeException ex) {
                LOGGER.log(Level.WARNING, "PowerPoint bridge failed, running " + args[0] + " through osascript", ex);
            }
        }
        return fallback.run(timeoutMillis, args);
    }

    @Override
    public void close() {
        if (bridge != null) {
            bridge.close();
        }
        fallback.close();
    }
}
