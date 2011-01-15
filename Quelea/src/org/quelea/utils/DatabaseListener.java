package org.quelea.utils;

/**
 * The update method on this interface should be called whenever the database has updated.
 * @author Michael
 */
public interface DatabaseListener {

    /**
     * Signifies that the database has been updated.
     */
    void update();

}
