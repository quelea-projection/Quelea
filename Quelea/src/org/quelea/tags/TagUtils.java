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
package org.quelea.tags;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing static utility methods useful in tags.
 * @author Michael
 */
public class TagUtils {
    
    /**
     * I bite.
     */
    private TagUtils() {
        throw new AssertionError();
    }
    
    /**
     * Get a list of tags from a semi-colon delimited string of tags.
     * @param str the string of tags.
     * @return a list of tags.
     */
    public static List<String> getTagsFromString(String str) {
        String[] rawTags = str.toLowerCase().split(";");
        List<String> tags = new ArrayList<>(rawTags.length);
        for (String tag : rawTags) {
            tags.add(tag.trim());
        }
        return tags;
    }
    
}
