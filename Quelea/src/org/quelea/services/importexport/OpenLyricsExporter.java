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

/**
 * An exporter for the openlyrics format.
 * @author Michael
 */
public class OpenLyricsExporter implements Exporter {

    /**
     * Get the file chooser to be used.
     * <p/>
     * @return the xip file chooser..
     */
    @Override
    public FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.ZIP);
        return chooser;
    }

    @Override
    public void writeSongPack(File file, List<SongDisplayable> songDisplayables) {
        //TODO: Implement
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
