package org.quelea.displayable;

/**
 * Objects that conform to this interface can be searched with a string.
 * @author Michael
 */
public interface Searchable {

    /**
     * Determine whether this object is returned if the user searches for the
     * given string.
     * @param s the search string.
     * @return true if the object should be returned as part of the search
     * results, false otherwise.
     */
    boolean search(String s);

}
