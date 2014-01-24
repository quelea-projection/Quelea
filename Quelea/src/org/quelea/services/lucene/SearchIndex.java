/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.lucene;

import java.util.Collection;

/**
 * The top level interface for search indexes.
 *
 * @author Michael
 * @param <T> the type of class we want to search.
 */
public interface SearchIndex<T> {
    /**
     * Filter the songs based on the contents of the lyrics or the title.
     */
    public enum FilterType {

        TITLE, AUTHOR, BODY
    }
    
    /**
     * Add an item to this search index.
     * @param t the item to add.
     */
    void add(T t);
    
    /**
     * Add all of the items in this collection to this search index.
     * @param t the collection of items to add.
     */
    void addAll(Collection<? extends T> t);
    
    /**
     * Remove the given item from this search index.
     * @param t the item to remove.
     */
    void remove(T t);
    
    /**
     * Remove everything from the given search index.
     */
    void clear();
    
    /**
     * Update the given item in this search index.
     * @param t the item to update.
     */
    void update(T t);
    
    /**
     * Get the number of entries in this search index.
     * @return the number of entries in this search index.
     */
    int size();
    
    /**
     * Get an array of results based on the given query string and filter type.
     * FilterType may be ignored where appropriate.
     * @param queryString the string on which the results should be queried.
     * @param type the type to search.
     * @return an array of results in the index that match the given query 
     * string and type.
     */
    T[] filter(String queryString, FilterType type);
    
}
