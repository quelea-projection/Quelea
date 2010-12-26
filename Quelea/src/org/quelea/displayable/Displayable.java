package org.quelea.displayable;

import javax.swing.Icon;

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

    /**
     * Get the preview icon to be displayed in the schedule.
     * @return the preview icon.
     */
    Icon getPreviewIcon();

    /**
     * Get the preview text to be displayed in the schedule.
     * @return the preview text.
     */
    String getPreviewText();

}
