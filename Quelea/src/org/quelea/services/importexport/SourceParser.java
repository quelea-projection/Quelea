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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for songs in the source songbook in HTML format.
 * @author Michael
 */
public class SourceParser implements SongParser {

    private static final String START_PARSE_LYRICS = "<!-- InstanceBeginEditable name=\"content\" -->";
    private static final String START_PARSE_AUTHOR = "<!-- InstanceBeginEditable name=\"Author\" -->";
    private static final String END_PARSE = "<!-- InstanceEndEditable -->";
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Get all the songs from the given location.
     * @param location the directory containing all the HTML files containing
     * the songs.
     * @return a list of parsed songs.
     * @throws IOException if something went wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        if (!location.isDirectory()) {
            throw new RuntimeException("Must be a directory.");
        }
        File[] files = location.listFiles();
        List<SongDisplayable> ret = new ArrayList<>();
        for (File file : files) {
            if (file.getName().endsWith("htm") || file.getName().endsWith("html")) {
                String contents = Utils.getTextFromFile(file.getAbsolutePath(), null);
                contents = contents.replaceAll("(&nbsp;)+", " ");
                contents = contents.replaceAll(" +", " ");
                contents = contents.replace("&amp;", "&");
                contents = contents.replace("&quot;", "'");
                contents = contents.replace("&lsquo;", "\"");
                contents = contents.replace("&rsquo;", "\"");
                contents = contents.replace("&lt;", "<");
                contents = contents.replace("&gt;", ">");
                String lyrics = getLyrics(contents);
                if (lyrics == null) {
                    continue;
                }
                String title = getTitle(lyrics, 0);
                SongDisplayable newSong = new SongDisplayable(title, getAuthor(contents));
                newSong.setLyrics(lyrics);
                ret.add(newSong);
            }
            else {
                LOGGER.log(Level.WARNING, "Invalid file {0}", file.getName());
            }
        }
        return ret;
    }

    /**
     * Get the title given the lyrics at the given line
     * @param lyrics the lyrics to use to search for the title.
     * @param line the line number to use for the title.
     * @return a non-blank title.
     */
    private String getTitle(String lyrics, int line) {
        String splitStr = lyrics.split("\n")[line];
        if(splitStr==null) {
            return "";
        }
        String title = splitStr.trim();
        if (title.endsWith(",")) {
            title = title.substring(0, title.length() - 1);
        }
        if (title.endsWith(";")) {
            title = title.substring(0, title.length() - 1);
        }
        if (title.startsWith("'")) {
            title = title.substring(1);
        }
        if (title.startsWith(".")) {
            title = title.substring(1);
        }
        title = title.replaceAll("\\(.+?\\)", "");
        title = title.trim();
        if (title.isEmpty()) {
            return getTitle(lyrics, line + 1);
        }
        return title;
    }

    /**
     * Get the author of the song from the raw HTML.
     * @param contents raw HTML.
     * @return the author.
     */
    private String getAuthor(String contents) {
        int beginIndex = contents.indexOf(START_PARSE_AUTHOR) + START_PARSE_AUTHOR.length();
        if (beginIndex == -1) {
            return "";
        }
        contents = contents.substring(beginIndex);
        contents = contents.substring(0, contents.indexOf(END_PARSE));
        return contents.trim();
    }
    
    /**
     * Get the lyrics of the song from the raw HTML.
     * @param contents raw HTML.
     * @return the lyrics.
     */
    private String getLyrics(String contents) {
        int beginIndex = contents.indexOf(START_PARSE_LYRICS) + START_PARSE_LYRICS.length();
        if (beginIndex == -1) {
            return null;
        }
        contents = contents.substring(beginIndex);
        contents = contents.substring(0, contents.indexOf(END_PARSE));
        contents = contents.replace("\n", "");
        StringBuilder lyrics = new StringBuilder();
        Pattern pattern = Pattern.compile("\\<p\\>(.+?)\\</p\\>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
            String section = matcher.group(0);
            section = section.replace("<br>", "\n");
            section = section.replace("<br/>", "\n");
            section = Utils.removeTags(section);
            String[] lines = section.split("\n");
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].trim();
            }
            StringBuilder verseBuilder = new StringBuilder();
            for (String line : lines) {
                verseBuilder.append(line).append("\n");
            }
            lyrics.append(verseBuilder.toString()).append("\n");
        }
        String ret = lyrics.toString().trim();
        if (ret.isEmpty()) {
            return null;
        }
        return ret;
    }

}
