/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing the SongParser Songs.MB database. This is in the access
 * DB (Jet engine) format.
 * <p>
 * @author Michael
 */
public class SongProParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Parse the file to get the songs.
     * <p>
     * @param file the Songs.SDB file.
     * @param statusPanel the status panel to update.
     * @return a list of the songs found in the Songs.SDB file.
     * @throws IOException if something went wrong with the import.
     */
    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        Database db = new DatabaseBuilder(file).setCharset(Charset.forName("ISO-8859-1")).setReadOnly(true).open();
        Table table = db.getTable("Chorus");
        int total = table.getRowCount();
        int iter = 0;
        for (Row r1 : table) {
            statusPanel.setProgress((double) iter / total);
            String title = r1.getString("Title");
            String author = r1.getString("Author");
            String copyright = r1.getString("Copyright");
            String key = r1.getString("Key");
            String comments = r1.getString("Comments");
//                String sequence = r1.getString("Sequence"); //TODO: For future use when we implement song sequences?

            StringBuilder lyrics = new StringBuilder();
            for (int i = 1; i <= 8; i++) {
                String versesec = getSection("Verse " + i, r1.getString("Verse" + i));
                if (versesec != null && !versesec.trim().isEmpty()) {
                    lyrics.append(versesec);
                }
            }
            String chorussec = getSection("Chorus", r1.getString("Chorus"));
            if (chorussec != null && !chorussec.trim().isEmpty()) {
                lyrics.append(chorussec);
            }
            String bridgesec = getSection("Bridge", r1.getString("Bridge"));
            if (bridgesec != null && !bridgesec.trim().isEmpty()) {
                lyrics.append(bridgesec);
            }

            SongDisplayable song = new SongDisplayable(title, author);
            song.setLyrics(lyrics.toString().trim());
            song.setKey(key);
            song.setInfo(comments);
            song.setCopyright(copyright);
            ret.add(song);
            iter++;
        }
        statusPanel.done();
        return ret;
    }

    private String getSection(String secName, String sec) {
        if (sec == null || sec.trim().isEmpty()) {
            return null;
        }
        if (sec.contains("\\rtf1")) {
            sec = sec.replaceAll("\\\\\\w+|\\{.*?\\}|}", "").trim();
        }
        StringBuilder ret = new StringBuilder();
        sec = sec.replace("\n\n", "\n<>\n");
        sec = sec.replace("\r\n\r\n", "\r\n<>\r\n");
        ret.append(secName).append("\n");
        ret.append(sec);
        ret.append("\n\n");
        return ret.toString();
    }
}
