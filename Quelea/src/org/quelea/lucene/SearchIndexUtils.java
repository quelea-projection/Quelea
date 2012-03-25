/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.lucene;

/**
 * General utility methods for search indexes.
 * @author Michael
 */
public class SearchIndexUtils {
    
    /**
     * Don't make me...
     */
    private SearchIndexUtils() {
        throw new AssertionError();
    }
    
    /**
     * Sanitise the given query so it's "lucene-safe". Make sure it's what we
     * want as well - treat as a phrase with a partial match for the last word.
     *
     * @param query the query to sanitise.
     * @return the sanitised query.
     */
    public static String makeLuceneQuery(String query) {
        query = query.replaceAll("[^a-zA-Z0-9 ]", "");
        query = query.trim();
        if(query.isEmpty()) {
            return query;
        }
        if(query.contains(" ")) {
            query = "\"" + query + "*\"";
        }
        else {
            query = query + "*";
        }
        return query;
    }
    
}
