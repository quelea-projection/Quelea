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
import java.util.Map;
import java.util.TreeMap;

/**
 * What the open lyrics display needs, without a token: the JSON counterpart
 * of the mobile lyrics server on port 1111. Returned by
 * {@code GET /api/v2/public/live} and sent as the public WebSocket snapshot.
 *
 * @param title the live item's title, or null.
 * @param itemType the live item's type, or null.
 * @param sectionIndex the zero-based selected section, or -1.
 * @param lines the text of the selected section, empty if it has none.
 * @param chorded the selected section's lines with their chords, or null if
 * it has no chords.
 * @param translations the live song's translations of the selected section,
 * by translation name; empty if there are none.
 * @param liveText the text typed in Quelea's live text window, empty when
 * there is none; shown while the main screen is empty.
 * @param bible for a Bible passage, the verses on the selected slide, so an
 * app can show them from another Bible installed in Quelea; otherwise
 * null.
 * @param logo true if the logo is showing.
 * @param black true if the screen is blacked out.
 * @param clear true if the text is cleared.
 */
public record PublicLive(String title, String itemType, int sectionIndex, List<String> lines,
        List<ChordSheet.ChordedLine> chorded, Map<String, List<String>> translations, String liveText,
        BibleSlide bible, boolean logo, boolean black, boolean clear) {

    /**
     * The verses on the live slide of a Bible passage.
     *
     * @param translation the Bible's name, for example "King James Version".
     * @param location the whole passage, for example "John 3:16-18".
     * @param book the book's number, 1 for Genesis to 66 for Revelation,
     * the same in every Bible.
     * @param verses the verses on the slide.
     */
    public record BibleSlide(String translation, String location, int book, List<VerseRef> verses) {

        public BibleSlide {
            verses = List.copyOf(verses);
        }
    }

    /**
     * A verse.
     *
     * @param chapter the chapter number.
     * @param verse the verse number.
     */
    public record VerseRef(int chapter, int verse) {
    }

    public PublicLive {
        lines = List.copyOf(lines);
        translations = Map.copyOf(translations);
    }

    /**
     * Read the public view of the live item.
     *
     * @param control the control to read from.
     * @return the public view.
     */
    static PublicLive from(QueleaControl control) {
        LiveState state = control.getState();
        LiveSections sections = control.getLiveSections();
        int index = sections.sectionIndex();
        LiveSections.Section section = index >= 0 && index < sections.sections().size()
                ? sections.sections().get(index)
                : null;
        // A sorted map keeps the JSON in a stable order.
        Map<String, List<String>> translations = new TreeMap<>(control.getLiveTranslations());
        return new PublicLive(state.title(), state.itemType(), index,
                section == null ? List.of() : section.lines(), section == null ? null : section.chorded(),
                translations, LiveText.get(), control.getLiveBibleSlide(), state.logo(), state.black(),
                state.clear());
    }
}
