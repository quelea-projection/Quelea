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
 * A change in Quelea that remote clients are told about. Each event's
 * {@link #type()} is the {@code type} field of the WebSocket message; the
 * record components are its {@code data}.
 */
public sealed interface RemoteEvent {

    /**
     * @return the event type as sent to clients, for example
     * {@code live.section}.
     */
    String type();

    /**
     * A new item went live, or the live item was removed (all fields null or
     * zero).
     *
     * @param itemId the schedule item id, or null if nothing is live.
     * @param title the item's title.
     * @param itemType the item type, see {@link ItemTypes}.
     * @param sectionCount the number of sections or slides.
     */
    record LiveItemChanged(String itemId, String title, String itemType, int sectionCount) implements RemoteEvent {

        @Override
        public String type() {
            return "live.item";
        }
    }

    /**
     * The selected section or slide of the live item changed.
     *
     * @param itemId the live item's id.
     * @param index the zero-based section index, or -1 if none is selected.
     */
    record LiveSectionChanged(String itemId, int index) implements RemoteEvent {

        @Override
        public String type() {
            return "live.section";
        }
    }

    /**
     * The logo, black or clear state of the projection changed.
     *
     * @param logo true if the logo is showing.
     * @param black true if the screen is blacked out.
     * @param clear true if the text is cleared.
     */
    record ScreenChanged(boolean logo, boolean black, boolean clear) implements RemoteEvent {

        @Override
        public String type() {
            return "live.screen";
        }
    }

    /**
     * The schedule changed. Clients fetch the schedule again.
     *
     * @param revision the new schedule revision.
     */
    record ScheduleChanged(long revision) implements RemoteEvent {

        @Override
        public String type() {
            return "schedule.changed";
        }
    }

    /**
     * Audio recording started or stopped.
     *
     * @param recording true while recording.
     */
    record RecordingChanged(boolean recording) implements RemoteEvent {

        @Override
        public String type() {
            return "recording";
        }
    }
}
