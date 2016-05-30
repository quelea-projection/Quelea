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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for the EpicWorship Song Pack file
 * <p/>
 * @author Ben
 */
public class EpicWorshipParser implements SongParser {

    Pattern NAME = Pattern.compile("\"name\":\"?.+?\"(?=,)");
    Pattern AUTHOR = Pattern.compile("\"author\":\".+?\"(?=,)");
    Pattern LYRICS = Pattern.compile("\"content\":\"?.+?\"");

    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(location), "UTF-8"));
        ArrayList<SongDisplayable> ret = new ArrayList<>();
        String fullContents = bfr.readLine();
        String songsWhole = fullContents.substring(10, fullContents.length() - 2);
        String[] songInfoList = songsWhole.split("\\},\\{");
        for (String songInfo : songInfoList) {
            songInfo = songInfo.replace("{", "").replace("{", "");
            Matcher m1 = NAME.matcher(songInfo);
            String name = "";
            String author = "";
            String lyrics = "";
            if (m1.find()) {
                name = m1.group(0);
                name = format(name);
            }

            Matcher m2 = AUTHOR.matcher(songInfo);
            if (m2.find()) {
                author = m2.group(0);
                author = format(author);
            }

            Matcher m3 = LYRICS.matcher(songInfo);
            if (m3.find()) {
                lyrics = m3.group(0);
                lyrics = format(lyrics);
            }

            SongDisplayable s = new SongDisplayable(name, author);

            ArrayList<String> section = new ArrayList<>();
            int sectionCount = 0;
            String[] lines = lyrics.split("\\\\n");
            for (String line : lines) {
                if (!line.isEmpty()) {
                    section.add(line);
                } else {
                    String[] sectionsArray = new String[section.size()];
                    s.addSection(sectionCount, new TextSection("", section.toArray(sectionsArray), section.toArray(sectionsArray), true));
                    sectionCount++;
                    section.clear();
                }
            }
            String[] sectionsArray = new String[section.size()];
            s.addSection(sectionCount, new TextSection("", section.toArray(sectionsArray), section.toArray(sectionsArray), true));

            ret.add(s);
        }
        return ret;
    }

    private String format(String s) {
        String n = "";
        n = s.split(":", 2)[1];
        n = n.replace("\"", "");
        return n;
    }
}
