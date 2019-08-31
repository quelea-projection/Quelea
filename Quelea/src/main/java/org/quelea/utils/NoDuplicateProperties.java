package org.quelea.utils;

import java.util.Properties;

/**
 * Overrides properties to throw an exception on a duplicate key.
 * @author Michael
 */
public class NoDuplicateProperties extends Properties {
    
    @Override
    public synchronized Object put(Object key, Object value) {
        if (get(key) != null) {
            throw new IllegalArgumentException(key + " already present.");
        }
        return super.put(key, value);
    }
    
}
