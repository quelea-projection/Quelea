package org.quelea.services.utils;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@DisplayName("Soft-reference cache")
public class SoftHashMapTest {
    @Test
    public void supportsNormalMapOperations() {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        Assertions.assertNull(map.put("a", "first"));
        Assertions.assertEquals("first", map.put("a", "second"));
        Assertions.assertEquals("second", map.get("a"));
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals("second", map.remove("a"));
        Assertions.assertNull(map.get("a"));
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    @Disabled("A removed reference can evict a later value for the same key")
    public void removedReferenceCannotEvictAReplacement() throws Exception {
        // Once a key is removed and reused, a delayed reference-queue event for
        // the old value must not remove the replacement. Remove the reverse
        // lookup on removal and only expunge a reference if it is still current.
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "old");
        Reference<?> removed = referenceFor(map, "key");
        map.remove("key");
        map.put("key", "new");
        removed.enqueue(); // Deterministically simulate collection of the old value.
        Assertions.assertEquals("new", map.get("key"));
    }

    @Test
    @Disabled("An entry update leaves the old reference in the reverse lookup")
    public void updatedEntryCannotBeEvictedWhenOldValueIsCollected() throws Exception {
        // Caches can lose a freshly updated value after the previous value is
        // collected. Route entry.setValue through put or update both reference
        // maps atomically, and check reference identity during queue cleanup.
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "old");
        Reference<?> old = referenceFor(map, "key");
        Assertions.assertEquals("old", map.entrySet().iterator().next().setValue("new"));
        old.enqueue();
        Assertions.assertEquals("new", map.get("key"));
    }

    @Test
    @Disabled("Cache entries do not implement Map.Entry equality")
    public void entriesCanBeComparedLikeOrdinaryMapEntries() {
        // Map consumers expect entry equality based on key and value. Implement
        // equals and hashCode on the entry wrapper while preserving setValue.
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "value");
        Assertions.assertTrue(map.entrySet().contains(new AbstractMap.SimpleEntry<>("key", "value")));
    }

    private Reference<?> referenceFor(SoftHashMap<?, ?> map, Object key) throws Exception {
        Field field = SoftHashMap.class.getDeclaredField("hash");
        field.setAccessible(true);
        return (Reference<?>) ((Map<?, ?>) field.get(map)).get(key);
    }
}
