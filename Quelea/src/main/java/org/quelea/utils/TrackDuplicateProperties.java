package org.quelea.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Overrides properties to throw an exception on a duplicate key.
 * @author Michael
 */
public class TrackDuplicateProperties extends Properties {
	
	private List<Object> duplicateKeys = new ArrayList<>();
    
    @Override
    public synchronized Object put(Object key, Object value) {
        if (get(key) != null) {
            duplicateKeys.add(key);
        }
        return super.put(key, value);
    }
	
	public void loadNoDuplicates(Reader reader) throws IOException {
		super.load(reader);
		if(!getDuplicateKeys().isEmpty()) {
			throw new IllegalArgumentException("Duplicate keys found: " + duplicateKeys);
		}
	}
	
	public List<Object> getDuplicateKeys() {
		return Collections.unmodifiableList(duplicateKeys);
	}
    
}
