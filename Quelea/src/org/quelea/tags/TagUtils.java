package org.quelea.tags;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class TagUtils {
    
    public static List<String> getTagsFromString(String str) {
        String[] rawTags = str.toLowerCase().split(";");
        List<String> tags = new ArrayList<>(rawTags.length);
        for (String tag : rawTags) {
            tags.add(tag.trim());
        }
        return tags;
    }
    
}
