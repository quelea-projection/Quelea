package org.quelea.display;

/**
 * An item such as a song that can be displayed on the projection screen.
 * @author Michael
 */
public interface Displayable {

    /**
     * Get the XML describing this displayable.
     * @return the xml.
     */
    String getXML();

}
