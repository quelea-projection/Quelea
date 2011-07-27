/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quelea.importexport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.quelea.displayable.Song;

/**
 *
 * @author Michael
 */
interface SongParser {

    List<Song> getSongs(File location) throws IOException;

}
