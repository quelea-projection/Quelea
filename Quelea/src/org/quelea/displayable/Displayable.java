package org.quelea.displayable;

import javax.swing.*;
import java.io.File;
import java.util.Collection;

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
    
    /**
     * Get the text to display when printed.
     * @return the printed text.
     */
    String getPrintText();

    /**
     * Get any file resources that this displayable needs to work. For songs this can be backgrounds, for videos this is
     * the video file, etc.
     * @return any files that this displayable relies upon.
     */
    Collection<File> getResources();

}
