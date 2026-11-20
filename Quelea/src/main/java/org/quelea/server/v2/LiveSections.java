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

import java.util.List;

/**
 * The sections of the live item as returned by
 * {@code GET /api/v2/live/sections}.
 *
 * @param itemId the live item's id, or null if nothing is live.
 * @param sectionIndex the zero-based selected section, or -1.
 * @param sections the sections in order; slides of presentations, PDFs and
 * image groups have no text.
 */
public record LiveSections(String itemId, int sectionIndex, List<Section> sections) {

    public LiveSections {
        sections = List.copyOf(sections);
    }

    /**
     * One section.
     *
     * @param title the section title, for example "Verse 1", or null.
     * @param lines the text lines without chords.
     * @param chordLines the lines with chord lines included, or null if the
     * section has no chords.
     * @param chorded the lyric lines with their chords and where each falls,
     * or null if the section has no chords.
     */
    public record Section(String title, List<String> lines, List<String> chordLines,
            List<ChordSheet.ChordedLine> chorded) {

        /**
         * A section whose {@code chorded} lines are worked out from its
         * chord lines.
         */
        public Section(String title, List<String> lines, List<String> chordLines) {
            this(title, lines, chordLines, ChordSheet.parse(chordLines));
        }
    }
}
