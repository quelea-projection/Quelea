package org.quelea.services.utils;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SortedPropertiesTest {
    @Test
    void keysEntriesAndStoredSettingsAreSorted() throws Exception {
        SortedProperties properties = new SortedProperties();
        properties.setProperty("z.last", "last");
        properties.setProperty("a.first", "first");
        properties.setProperty("m.middle", "middle");
        Assertions.assertEquals(List.of("a.first", "m.middle", "z.last"), Collections.list(properties.keys()));
        Assertions.assertEquals(List.of("a.first", "m.middle", "z.last"),
                properties.entrySet().stream().map(entry -> entry.getKey().toString()).toList());
        StringWriter writer = new StringWriter();
        properties.store(writer, null);
        String stored = writer.toString();
        Assertions.assertTrue(stored.indexOf("a.first=first") < stored.indexOf("m.middle=middle"));
        Assertions.assertTrue(stored.indexOf("m.middle=middle") < stored.indexOf("z.last=last"));
    }
}
