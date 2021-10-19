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
import java.util.ArrayList;
import java.util.List;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing MediaShout databases, exported to txt format.
 * @author Michael
 */
public class MediaShoutParser implements SongParser {

    /**
     * Get a list of the songs contained in the given pack.
     * @param location the location of the txt file.
     * @return a list of the songs found.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) {
        List<SongDisplayable> ret = new ArrayList<>();
        String contents = Utils.getTextFromFile(location.getAbsolutePath(), null);
        if(contents==null) {
            return ret;
        }
        String[] songSplit = contents.split("Title\\:");
        for (int i = 1; i < songSplit.length; i++) {
            String title = "";
            String author = "";
            String copyright = "";
            String ccli = "";
            String playorder = ""; //Not yet used
            String lyrics = "";
            String rawSongText = songSplit[i];
            String[] lines = rawSongText.split("\n");
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j].trim();
                if (j == 0) {
                    title = line.trim();
                } else if (line.toLowerCase().startsWith("author:")) {
                    author = line.substring("author:".length()).trim();
                } else if (line.toLowerCase().startsWith("copyright:")) {
                    copyright = line.substring("copyright:".length()).trim();
                } else if (line.toLowerCase().startsWith("ccli:")) {
                    ccli = line.substring("ccli:".length()).trim();
                } else if (line.toLowerCase().startsWith("playorder:")) {
                    playorder = line.substring("playorder:".length()).trim();
                }
            }
            lyrics = rawSongText.substring(rawSongText.indexOf("\n\n")).trim();
            if(!(title.isEmpty()||lyrics.isEmpty())) {
                SongDisplayable song = new SongDisplayable(title, author);
                song.setCopyright(copyright);
                song.setCcli(ccli);
                song.setLyrics(lyrics);
                ret.add(song);
            }
        }
        return ret;
    }

}
