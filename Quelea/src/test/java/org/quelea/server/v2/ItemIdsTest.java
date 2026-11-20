package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;

class ItemIdsTest {

    @Test
    void sameDisplayableKeepsItsId() {
        ItemIds ids = new ItemIds();
        FakeDisplayable song = new FakeDisplayable("Amazing Grace");
        String first = ids.idFor(song);
        assertEquals(first, ids.idFor(song));
        assertSame(song, ids.find(first));
    }

    @Test
    void equalCopiesGetDifferentIds() {
        ItemIds ids = new ItemIds();
        FakeDisplayable copy1 = new FakeDisplayable("Amazing Grace");
        FakeDisplayable copy2 = new FakeDisplayable("Amazing Grace");
        assertEquals(copy1, copy2);
        assertNotEquals(ids.idFor(copy1), ids.idFor(copy2));
    }

    @Test
    void nullHasNoId() {
        ItemIds ids = new ItemIds();
        assertNull(ids.idFor(null));
        assertNull(ids.find(null));
        assertNull(ids.find("it_999"));
    }

    @Test
    void retainOnlyForgetsRemovedItemsAndNeverReusesIds() {
        ItemIds ids = new ItemIds();
        FakeDisplayable kept = new FakeDisplayable("kept");
        FakeDisplayable removed = new FakeDisplayable("removed");
        String keptId = ids.idFor(kept);
        String removedId = ids.idFor(removed);

        ids.retainOnly(List.of(kept));

        assertEquals(1, ids.size());
        assertNull(ids.find(removedId));
        assertEquals(keptId, ids.idFor(kept));
        FakeDisplayable added = new FakeDisplayable("added");
        assertNotEquals(removedId, ids.idFor(added));
    }

    @Test
    void transferKeepsTheIdForAReplacement() {
        ItemIds ids = new ItemIds();
        FakeDisplayable song = new FakeDisplayable("Song");
        FakeDisplayable transposed = new FakeDisplayable("Song");
        String id = ids.idFor(song);

        ids.transfer(song, transposed);

        assertEquals(id, ids.idFor(transposed));
        assertSame(transposed, ids.find(id));
        assertEquals(1, ids.size());
    }

    @Test
    void retainOnlyComparesByIdentity() {
        ItemIds ids = new ItemIds();
        FakeDisplayable original = new FakeDisplayable("same");
        String id = ids.idFor(original);

        ids.retainOnly(List.of(new FakeDisplayable("same")));

        assertNull(ids.find(id));
    }
}
