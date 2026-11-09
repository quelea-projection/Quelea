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
import org.quelea.data.displayable.Displayable;

/**
 * Gives each schedule item an id that stays the same while Quelea runs, even
 * when the item is moved. v1 addresses items by position, so two clients
 * moving items at the same time could act on the wrong one.
 * <p>
 * Ids follow the displayable object, not its position or its
 * {@code equals}: moving an item by drag and drop wraps the same displayable
 * in a new list entry, and two copies of one song are still two items.
 */
public final class ItemIds {

    private final Map<Displayable, String> ids = new IdentityHashMap<>();
    private long next = 1;

    /**
     * Get the id of a displayable, assigning a new one the first time.
     *
     * @param displayable the displayable, may be null.
     * @return its id, or null for null.
     */
    public synchronized String idFor(Displayable displayable) {
        if (displayable == null) {
            return null;
        }
        String id = ids.get(displayable);
        if (id == null) {
            id = "it_" + next++;
            ids.put(displayable, id);
        }
        return id;
    }

    /**
     * Find the displayable with a given id.
     *
     * @param id the id.
     * @return the displayable, or null if no item has that id.
     */
    public synchronized Displayable find(String id) {
        if (id == null) {
            return null;
        }
        for (Map.Entry<Displayable, String> entry : ids.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Forget every displayable that is not in the given collection, so that
     * removed items can be garbage collected. Their ids are never reused.
     *
     * @param keep the displayables still in use (schedule, live, preview).
     */
    public synchronized void retainOnly(Collection<? extends Displayable> keep) {
        Set<Displayable> keepSet = Collections.newSetFromMap(new IdentityHashMap<>());
        keepSet.addAll(keep);
        ids.keySet().removeIf(d -> !keepSet.contains(d));
    }

    /**
     * @return the number of displayables currently tracked.
     */
    public synchronized int size() {
        return ids.size();
    }
}
