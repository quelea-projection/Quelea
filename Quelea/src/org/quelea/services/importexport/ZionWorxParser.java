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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing ZionWorx databases.
 * <p>
 * @author Michael
 */
public class ZionWorxParser implements SongParser {

    /**
     * Get a list of the songs contained in the given ZionWorx database.
     * <p>
     * @param location the location of the ZionWorx MainTable.dat file.
     * @return a list of the songs found.
     * @throws IOException if something goes wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> songs = new ArrayList<>();
        File csvLocation = new ZWCsvConverter(location).getCSV();
        String fileContent = Utils.getTextFromFile(csvLocation.getAbsolutePath(), null);
        RawTextWrapper rawText;
        while((rawText = getRawText(fileContent)) != null) {
            songs.add(getSong(rawText.rawText));
            fileContent = fileContent.substring(rawText.endIndex);
        }
        return songs;
    }

    /**
     * Get a song displayable for one section of raw text.
     * <p>
     * @param rawText the raw text for one particular song.
     * @return the song obtained from this raw text.
     */
    private SongDisplayable getSong(String rawText) {
        int startTitleIndex = rawText.indexOf("\\");
        int endTitleIndex = rawText.indexOf("\\", startTitleIndex + 1);
        String title = rawText.substring(startTitleIndex + 1, endTitleIndex);
        if(title.startsWith("\\")) {
            title = title.substring(1);
        }
        if(title.startsWith(",")) {
            title = title.substring(1);
        }
        SongDisplayable song = new SongDisplayable(title, "");
        rawText = rawText.substring(endTitleIndex);
        int lastIndex = rawText.lastIndexOf("\\,");
        String sub = rawText.substring(0, lastIndex);
        rawText = rawText.substring(sub.lastIndexOf("\\,")+2, lastIndex);
        if(rawText.startsWith(",")) {
            rawText = rawText.substring(1);
        }
        if(rawText.startsWith("\\")) {
            rawText = rawText.substring(1);
        }
        rawText = rawText.trim(); //It should now be the lyrics
        song.setLyrics(rawText);
        return song;
    }

    /**
     * Get the raw text for a particular number song (starting at 1.)
     * <p>
     * @param fileContent the full CSV file content.
     * @param num the number of the song to get.
     * @return the raw CSV text just for one song.
     */
    private RawTextWrapper getRawText(String fileContent) {
        Pattern patt = Pattern.compile("\\\\[0-9]+\\\\");
        Matcher matcher = patt.matcher(fileContent);
        if(matcher.find()) {
            int startIndex = matcher.start();
            if(startIndex == -1) {
                return null;
            }
            startIndex += matcher.end(); //Chop off index number / comma / backslashes
            int endIndex = fileContent.length();
            if(matcher.find()) {
                endIndex = matcher.start();
            }
            return new RawTextWrapper(fileContent.substring(startIndex, endIndex), endIndex);
        }
        return null;
    }

    /**
     * Wraps the raw text retrieved from the CSV file for a particular song, as
     * well as the end index (so the correct amount of the string can be cut off
     * ready for the next song.)
     */
    static class RawTextWrapper {

        private String rawText;
        private int endIndex;

        public RawTextWrapper(String str, int endIndex) {
            this.rawText = str;
            this.endIndex = endIndex;
        }

    }

}
