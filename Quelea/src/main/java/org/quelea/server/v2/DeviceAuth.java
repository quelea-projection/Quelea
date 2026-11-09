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

import java.util.Optional;

/**
 * Checks the token a client sends and says which paired device it belongs
 * to.
 */
@FunctionalInterface
public interface DeviceAuth {

    /**
     * Accepts no token at all. Used until devices can be paired, so that
     * only the open endpoints answer.
     */
    DeviceAuth NONE = token -> Optional.empty();

    /**
     * @param token the token from the Authorization header or the WebSocket
     * auth message; may be null.
     * @return the device, or empty if the token is unknown or revoked.
     */
    Optional<Device> authenticate(String token);

    /**
     * A paired device.
     *
     * @param id the device id.
     * @param name the name the device gave when pairing.
     * @param scope what the device may do.
     */
    record Device(String id, String name, Scope scope) {

        /**
         * @param required the scope an endpoint needs.
         * @return true if this device may use it.
         */
        public boolean can(Scope required) {
            return scope.allows(required);
        }
    }
}
