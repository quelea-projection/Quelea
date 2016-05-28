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
import java.util.List;
import javafx.stage.FileChooser;
import org.quelea.data.displayable.SongDisplayable;

/**
 * A generic exporter interface whose implementations provide functionality to
 * export to particular formats.
 *
 * @author Michael
 */
public interface Exporter {

    /**
     * Get a file chooser to select the file to export to.
     *
     * @return a file chooser to select the file to export to.
     */
    FileChooser getChooser();

    /**
     * Write the song pack to the specified file.
     * <p/>
     * @param file the file to write the song pack to.
     * @param songDisplayables the songs to write to the file.
     */
    void exportSongs(final File file, final List<SongDisplayable> songDisplayables);

    /**
     * Get the file extension this exporter exports to.
     *
     * @return the file extension this exporter exports to.
     */
    String getStrExtension();

}
