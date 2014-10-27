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
package org.quelea.data.displayable;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import javafx.scene.image.ImageView;

/**
 * An item such as a song that can be displayed on the projection screen.
 *
 * @author Michael
 */
public interface Displayable extends Serializable {

    /**
     * Determine if this displayable can be "cleared" in some way.
     *
     * @return true if it can be cleared, false otherwise.
     */
    boolean supportClear();

    /**
     * Get the XML describing this displayable.
     *
     * @return the xml.
     */
    String getXML();

    /**
     * Get the preview icon to be displayed in the schedule.
     *
     * @return the preview icon.
     */
    ImageView getPreviewIcon();

    /**
     * Get the preview text to be displayed in the schedule.
     *
     * @return the preview text.
     */
    String getPreviewText();

    /**
     * Get any file resources that this displayable needs to work. For songs
     * this can be backgrounds, for videos this is the video file, etc.
     *
     * @return any files that this displayable relies upon.
     */
    Collection<File> getResources();

    /**
     * Free any resources used by this displayable when it's no longer needed.
     */
    void dispose();

}
