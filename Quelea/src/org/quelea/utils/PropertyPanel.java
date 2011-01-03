package org.quelea.utils;

/**
 * Classes using this interface write some properties to Quelea's properties
 * file, and read these properties to set their contents.
 * @author Michael
 */
public interface PropertyPanel {

    /**
     * Write the chosen properties based on the contents of this object.
     */
    void setProperties();

    /**
     * Update the contents of the object based on the properties.
     */
    void readProperties();

}
