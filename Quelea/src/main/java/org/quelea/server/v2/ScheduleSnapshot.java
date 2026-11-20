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
 * The schedule as returned by {@code GET /api/v2/schedule}.
 *
 * @param revision the schedule revision; it changes whenever the schedule
 * does.
 * @param items the items in schedule order.
 */
public record ScheduleSnapshot(long revision, List<Entry> items) {

    public ScheduleSnapshot {
        items = List.copyOf(items);
    }

    /**
     * One schedule item.
     *
     * @param id the item's stable id.
     * @param type the item type, see {@link ItemTypes}.
     * @param title the item's title.
     * @param live true if this item is live.
     * @param preview true if this item is in the preview panel.
     */
    public record Entry(String id, String type, String title, boolean live, boolean preview) {
    }
}
