package org.quelea.services.utils;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
    public void removedReferenceCannotEvictAReplacement() throws Exception {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "old");
        Reference<?> removed = referenceFor(map, "key");
        map.remove("key");
        map.put("key", "new");
        removed.enqueue(); // Deterministically simulate collection of the old value.
        Assertions.assertEquals("new", map.get("key"));
    }

    @Test
    public void updatedEntryCannotBeEvictedWhenOldValueIsCollected() throws Exception {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "old");
        Reference<?> old = referenceFor(map, "key");
        Assertions.assertEquals("old", map.entrySet().iterator().next().setValue("new"));
        old.enqueue();
        Assertions.assertEquals("new", map.get("key"));
    }

    @Test
    public void entriesCanBeComparedLikeOrdinaryMapEntries() {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "value");
        Assertions.assertTrue(map.entrySet().contains(new AbstractMap.SimpleEntry<>("key", "value")));
    }

    @Test
    public void staleReferenceDoesNotRemoveNullKey() throws Exception {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("old", "discarded");
        Reference<?> old = referenceFor(map, "old");
        map.remove("old");
        map.put(null, "kept");
        old.enqueue();
        Assertions.assertEquals("kept", map.get(null));
        Assertions.assertEquals(1, map.size());
    }

    @Test
    public void currentEnqueuedReferenceIsRemoved() throws Exception {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put(null, "discarded");
        Reference<?> current = referenceFor(map, null);
        current.clear();
        current.enqueue();
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void entryUpdatesKeepTheirValueAndPreviousValue() throws Exception {
        SoftHashMap<String, String> map = new SoftHashMap<>();
        map.put("key", "first");
        Map.Entry<String, String> entry = map.entrySet().iterator().next();
        Assertions.assertEquals("first", entry.setValue("second"));
        Reference<?> second = referenceFor(map, "key");
        Assertions.assertEquals("second", entry.setValue("third"));
        Assertions.assertEquals("third", entry.getValue());
        Assertions.assertEquals(new AbstractMap.SimpleEntry<>("key", "third"), entry);
        second.clear();
        second.enqueue();
        Assertions.assertEquals("third", map.get("key"));
    }

    private Reference<?> referenceFor(SoftHashMap<?, ?> map, Object key) throws Exception {
        Field field = SoftHashMap.class.getDeclaredField("hash");
        field.setAccessible(true);
        return (Reference<?>) ((Map<?, ?>) field.get(map)).get(key);
    }
}
