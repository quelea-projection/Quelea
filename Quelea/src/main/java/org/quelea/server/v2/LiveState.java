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
 * Everything a remote needs to draw its main screen: what is live and how the
 * projection looks. Returned by {@code GET /api/v2/state} and sent as the
 * WebSocket snapshot.
 *
 * @param itemId the live item's id, or null if nothing is live.
 * @param title the live item's title, or null.
 * @param itemType the live item's type (see {@link ItemTypes}), or null.
 * @param sectionIndex the zero-based selected section, or -1.
 * @param sectionCount the number of sections or slides in the live item.
 * @param logo true if the logo is showing.
 * @param black true if the screen is blacked out.
 * @param clear true if the text is cleared.
 * @param canClear true if the live item has text to clear; Quelea disables
 * its Clear button for presentations, images, videos and web pages.
 * @param recording true while audio is being recorded.
 * @param scheduleRevision the current schedule revision.
 * @param media the playback of a live video, audio file or timer, or null
 * if the live item is none of those.
 */
public record LiveState(
        String itemId,
        String title,
        String itemType,
        int sectionIndex,
        int sectionCount,
        boolean logo,
        boolean black,
        boolean clear,
        boolean canClear,
        boolean recording,
        long scheduleRevision,
        Media media) {

    /**
     * The playback of the live video, audio file or timer.
     *
     * @param kind "video", "audio" or "timer".
     * @param playing true while it plays or counts.
     * @param loop true if it plays again when it ends; null for a timer.
     * @param position how far it has played, from 0 to 1; null for a timer.
     */
    public record Media(String kind, boolean playing, Boolean loop, Double position) {
    }
}
