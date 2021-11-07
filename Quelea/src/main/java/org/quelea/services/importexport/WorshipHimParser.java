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

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A parser for parsing MediaShout databases, exported to txt format.
 *
 * @author Michael
 */
public class WorshipHimParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Get a list of the songs contained in the given pack.
     *
     * @param location the location of the txt file.
     * @return a list of the songs found.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) {
        List<SongDisplayable> ret = new ArrayList<>();
        try {
            Database d = new DatabaseBuilder()
                    .setReadOnly(true)
                    .setFile(location)
                    .open();

            Table table = d.getTable("Songs");
            for (Row row : table) {
                String title = row.get("Title").toString();
                if (title.trim().isEmpty()) continue; //Ignore empty titled songs
                String author = row.get("Author").toString();
                String lyrics = row.get("Lyrics").toString()
                        .replace("\u0001", "\n")
                        .replaceAll("\n\n+", "\n\n")
                        .trim();

                SongDisplayable song = new SongDisplayable(title, author);
                song.setLyrics(lyrics);
                ret.add(song);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error importing worship hymn library", ex);
        }
        return ret;
    }

}
