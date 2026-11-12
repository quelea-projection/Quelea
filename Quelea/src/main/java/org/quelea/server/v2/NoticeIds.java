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

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.quelea.services.notice.Notice;

/**
 * Gives each notice being shown a stable id ("nt_1", "nt_2" …) for as long
 * as it is shown. Like {@link ItemIds}, ids follow the object, not its text:
 * two notices with the same text get different ids.
 */
final class NoticeIds {

    private final Map<Notice, String> ids = new IdentityHashMap<>();
    private long next = 1;

    synchronized String idFor(Notice notice) {
        return ids.computeIfAbsent(notice, n -> "nt_" + next++);
    }

    synchronized Notice find(String id) {
        for (Map.Entry<Notice, String> entry : ids.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Forget the notices that are no longer shown.
     *
     * @param shown the notices shown now.
     */
    synchronized void retainOnly(Collection<Notice> shown) {
        Set<Notice> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        keep.addAll(shown);
        ids.keySet().removeIf(n -> !keep.contains(n));
    }
}
