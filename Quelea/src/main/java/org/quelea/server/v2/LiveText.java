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
 * The text typed in Quelea's live text window, shown on phones with the
 * lyrics app while the main screen is empty. Kept here so remote control
 * v2 can serve it whether or not the old Mobile Lyrics server runs.
 */
public final class LiveText {

    private static volatile String text = "";

    private LiveText() {
    }

    /**
     * @return the live text, empty when there is none.
     */
    public static String get() {
        return text;
    }

    /**
     * Set the live text and tell connected apps.
     *
     * @param newText the text, or null or empty to clear it.
     */
    public static void set(String newText) {
        String value = newText == null ? "" : newText;
        if (value.equals(text)) {
            return;
        }
        text = value;
        RemoteEventBus.get().publish(new RemoteEvent.LiveTextChanged(value));
    }
}
