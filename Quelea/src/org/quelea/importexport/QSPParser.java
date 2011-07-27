/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quelea.importexport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.quelea.displayable.Song;
import org.quelea.utils.SongPack;

/**
 *
 * @author Michael
 */
public class QSPParser implements SongParser {

    @Override
    public List<Song> getSongs(File location) throws IOException {
        return SongPack.fromFile(location).getSongs();
    }

}
