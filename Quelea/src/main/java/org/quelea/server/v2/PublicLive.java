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
 * What the open lyrics display needs, without a token: the JSON counterpart
 * of the mobile lyrics server on port 1111. Returned by
 * {@code GET /api/v2/public/live} and sent as the public WebSocket snapshot.
 *
 * @param title the live item's title, or null.
 * @param itemType the live item's type, or null.
 * @param sectionIndex the zero-based selected section, or -1.
 * @param lines the text of the selected section, empty if it has none.
 * @param logo true if the logo is showing.
 * @param black true if the screen is blacked out.
 * @param clear true if the text is cleared.
 */
public record PublicLive(String title, String itemType, int sectionIndex, List<String> lines,
        boolean logo, boolean black, boolean clear) {

    public PublicLive {
        lines = List.copyOf(lines);
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
        List<String> lines = index >= 0 && index < sections.sections().size()
                ? sections.sections().get(index).lines()
                : List.of();
        return new PublicLive(state.title(), state.itemType(), index, lines,
                state.logo(), state.black(), state.clear());
    }
}
