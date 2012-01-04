/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.displayable;

/**
 * Objects that conform to this interface can be searched with a string.
 * @author Michael
 */
public interface Searchable {
    
    /**
     * The possible search results - title if a search string is found in the
     * title of this song, lyrics if it's in the lyrics, none if it's not found.
     */
    public enum SearchResult {
        TITLE, LYRICS, NONE
    }

    /**
     * Determine whether this object is returned if the user searches for the given string.
     * @param s the search string.
     * @return true if the object should be returned as part of the search results, false otherwise.
     */
    SearchResult search(String s);
}
