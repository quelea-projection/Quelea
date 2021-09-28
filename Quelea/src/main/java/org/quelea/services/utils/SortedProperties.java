package org.quelea.services.utils;

import org.apache.commons.collections4.iterators.IteratorEnumeration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

class SortedProperties extends Properties {

    @Override
    public synchronized Enumeration<Object> keys() {
        final Set<Object> keySet = keySet();
        final List<String> keys = new ArrayList<>(keySet.size());
        for (final Object key : keySet) {
            keys.add(key.toString());
        }
        Collections.sort(keys);
        return new IteratorEnumeration<>(keys.iterator());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Enumeration<Object> keys = keys();
        Set<Map.Entry<Object, Object>> entrySet = new LinkedHashSet<>();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            entrySet.add(new AbstractMap.SimpleEntry<>(key, getProperty((String) key)));
        }
        return entrySet;
    }
}