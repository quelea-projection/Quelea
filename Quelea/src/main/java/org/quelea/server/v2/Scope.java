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

import com.google.gson.annotations.SerializedName;

/**
 * What a paired device may do. {@link #CONTROL} includes everything
 * {@link #VIEW} allows.
 */
public enum Scope {

    /**
     * Read state, schedule and texts; no commands.
     */
    @SerializedName("view")
    VIEW,
    /**
     * Everything, including commands.
     */
    @SerializedName("control")
    CONTROL;

    /**
     * @param required the scope an endpoint needs.
     * @return true if this scope is enough for it.
     */
    public boolean allows(Scope required) {
        return this == CONTROL || required == VIEW;
    }
}
