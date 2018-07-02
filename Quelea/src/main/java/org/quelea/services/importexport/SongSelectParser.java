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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for songs in the songselect USR format.
 *
 * @author Michael
 */
public class SongSelectParser implements SongParser {

    /**
     * Get all the songs from the given location.
     *
     * @param location the USR file.
     * @return a list of parsed songs.
     * @throws IOException if something went wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        String text = Utils.getTextFromFile(location.getAbsolutePath(), "");
        String ccli = null;
        for(String line : text.split("\n")) {
            if(line.trim().startsWith("[S A")) {
                ccli = line.trim().substring(4, line.length()-1);
            }
        }
        Properties p = new Properties();
        p.load(new FileReader(location));
        String lyricsRaw = p.getProperty("Words");
        StringBuilder lyricsFinal = new StringBuilder();
        String[] sectionHeadings = p.getProperty("Fields").split(Pattern.quote("/t"));
        String[] lyricSections = lyricsRaw.split(Pattern.quote("/t"));
        for (int i = 0; i < lyricSections.length; i++) {
            if (i < sectionHeadings.length) {
                lyricsFinal.append(sectionHeadings[i].trim()).append("\n");
            }
            for (String line : lyricSections[i].split(Pattern.quote("/n"))) {
                lyricsFinal.append(line.trim()).append("\n");
            }
            lyricsFinal.append("\n");
        }

        String title = p.getProperty("Title");
        String author = p.getProperty("Author");
        String lyrics = lyricsFinal.toString().trim();

        List<SongDisplayable> ret = new ArrayList<>();
        SongDisplayable song = new SongDisplayable(title, author);
        song.setLyrics(lyrics);
        song.setKey(p.getProperty("Keys"));
        song.setCopyright(p.getProperty("Copyright"));
        song.setCcli(ccli);
        ret.add(song);
        return ret;
    }

}
