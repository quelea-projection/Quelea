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
package org.quelea.services.importexport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.windows.main.StatusPanel;

/**
 * The interface for all the different song parsers that parse songs from 
 * various sources.
 * @author Michael
 */
public interface SongParser {

    /**
     * Get all the songs from a particular location.
     * @param location the location to search for the songs.
     * @param statusPanel the status panel used when parsing this song. It can
     * be updated or ignored.
     * @return a list of all the songs found in the given location that have 
     * been parsed.
     * @throws IOException if something goes wrong accessing the given location.
     */
    List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException;
}
