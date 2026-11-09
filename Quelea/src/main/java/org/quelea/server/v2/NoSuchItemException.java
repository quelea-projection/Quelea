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

/**
 * Thrown when a request names a schedule item that does not exist (any more).
 */
public final class NoSuchItemException extends RuntimeException {

    private final String itemId;

    public NoSuchItemException(String itemId) {
        super("No schedule item with id " + itemId);
        this.itemId = itemId;
    }

    /**
     * @return the id that was not found.
     */
    public String getItemId() {
        return itemId;
    }
}
