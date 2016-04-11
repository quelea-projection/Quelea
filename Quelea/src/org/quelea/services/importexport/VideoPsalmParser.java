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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing VideoPsalm databases, exported to json format.
 *
 * @author Michael
 */
public class VideoPsalmParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * Get a list of the songs contained in the given pack.
     *
     * @param location the location of the json or vpc file.
     * @return a list of the songs found.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) {
        List<SongDisplayable> ret = new ArrayList<>();

        JsonElement jelement;
        if (location.getName().endsWith(".json")) {
            String contents = Utils.getTextFromFile(location.getAbsolutePath(), null, "UTF-8");
            if (contents == null) {
                return ret;
            }
            if (contents.startsWith(UTF8_BOM)) {
                contents = contents.substring(1);
            }
            jelement = new JsonParser().parse(contents);
        } else {
            InputStreamReader isr = fromZip(location);
            if (isr == null) {
                jelement = null;                
            }
            else {
                jelement = new JsonParser().parse(isr);
            }
        }
        
        if(jelement == null) {
            return ret;
        }

        JsonArray arr = jelement.getAsJsonObject().get("Songs").getAsJsonArray();

        for (JsonElement obj : arr) {
            String title = "Unknown title";
            if (obj.getAsJsonObject().has("Text")) {
                title = obj.getAsJsonObject().get("Text").getAsString();
            }
            String author = "";
            if (obj.getAsJsonObject().has("Author")) {
                author = obj.getAsJsonObject().get("Author").getAsString();
            }
            SongDisplayable song = new SongDisplayable(title, author);
            StringBuilder lyrics = new StringBuilder();
            for (JsonElement verseObj : obj.getAsJsonObject().getAsJsonArray("Verses")) {
                lyrics.append(verseObj.getAsJsonObject().get("Text").getAsString());
                lyrics.append("\n\n");
            }
            song.setLyrics(lyrics.toString().trim());
            ret.add(song);
        }
        return ret;
    }

    private InputStreamReader fromZip(File location) {
        try {
            ZipFile file = new ZipFile(location, Charset.forName("Cp437"));
            final Enumeration<? extends ZipEntry> entries = file.entries();
            if (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                return new InputStreamReader(file.getInputStream(entry));
            }
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error importing VideoPsalm", ex);
            return null;
        }
    }

    public static void main(String[] args) {
        new VideoPsalmParser().getSongs(new File("C:\\users\\michael\\desktop\\x.vpc"), null);
    }

}
