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
import org.quelea.services.utils.SongPack;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing song packs. Not really a parser, but here for 
 * completeness.
 * @author Michael
 */
public class QSPParser implements SongParser {

    /**
     * Get a list of the songs contained in the given pack.
     * @param location the location of the QSP file.
     * @return a list of the songs found.
     * @throws IOException if something goes wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        return SongPack.fromFile(location).getSongs();
    }

}
