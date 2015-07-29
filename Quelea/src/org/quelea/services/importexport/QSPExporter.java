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
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.SongPack;

/**
 * An exporter for writing QSP files.
 * @author Michael
 */
public class QSPExporter implements Exporter {
    
    /**
     * Get the file chooser to be used.
     * <p/>
     * @return the song pack file chooser.
     */
    @Override
    public FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.SONG_PACK);
        return chooser;
    }

    /**
     * Write the song pack to the specified file, closing the window when done.
     * <p/>
     * @param file the file to write the song pack to.
     */
    @Override
    public void exportSongs(final File file, final List<SongDisplayable> songDisplayables) {
        final SongPack pack = new SongPack();
        pack.addSongs(songDisplayables);
        pack.writeToFile(file);

    }

    @Override
    public String getStrExtension() {
        return "qsp";
    }

}
