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
 * What the v2 remote API can see and do in Quelea. The server only talks to
 * this interface, so it can be tested without JavaFX.
 * <p>
 * All methods may be called from any thread. Implementations that touch the
 * user interface throw {@link FxCall.TimeoutException} if the FX thread does
 * not respond in time. Commands return the live state right after the
 * command ran.
 */
public interface QueleaControl {

    /**
     * @return the current live state.
     */
    LiveState getState();

    /**
     * @return the current schedule.
     */
    ScheduleSnapshot getSchedule();

    /**
     * Go to the next section, or the next item if the setting for that is on.
     *
     * @return the new state.
     */
    LiveState next();

    /**
     * Go to the previous section.
     *
     * @return the new state.
     */
    LiveState previous();

    /**
     * Select a section of the live item.
     *
     * @param index the zero-based section index.
     * @return the new state.
     * @throws IllegalArgumentException if the index is out of range or
     * nothing is live.
     */
    LiveState gotoSection(int index);

    /**
     * Put the next schedule item live.
     *
     * @return the new state.
     */
    LiveState nextItem();

    /**
     * Put the previous schedule item live.
     *
     * @return the new state.
     */
    LiveState previousItem();

    /**
     * Put a schedule item live.
     *
     * @param itemId the item's id.
     * @return the new state.
     * @throws NoSuchItemException if no schedule item has that id.
     */
    LiveState goLive(String itemId);

    /**
     * Set the logo, black and clear states. A null argument leaves that state
     * as it is.
     *
     * @param logo the new logo state, or null.
     * @param black the new black state, or null.
     * @param clear the new clear state, or null.
     * @return the new state.
     */
    LiveState setScreen(Boolean logo, Boolean black, Boolean clear);

    /**
     * Start or stop audio recording.
     *
     * @param recording true to record.
     * @return the new state.
     */
    LiveState setRecording(boolean recording);
}
