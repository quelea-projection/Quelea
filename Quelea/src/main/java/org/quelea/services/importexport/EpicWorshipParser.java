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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for the EpicWorship Song Pack file
 * <p/>
 * @author Ben
 */
public class EpicWorshipParser implements SongParser {

    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        ArrayList<SongDisplayable> ret = new ArrayList<>();
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(location), Utils.getEncoding(location)))){
            JsonElement root = JsonParser.parseReader(bfr);
            if (!root.isJsonObject() || !root.getAsJsonObject().has("songs")
                    || !root.getAsJsonObject().get("songs").isJsonArray()) {
                throw new IOException("EpicWorship song pack must contain a songs array");
            }
            JsonArray songs = root.getAsJsonObject().getAsJsonArray("songs");
            for (JsonElement entry : songs) {
                if (!entry.isJsonObject()) {
                    throw new IOException("EpicWorship song pack contains a non-object song");
                }
                JsonObject songInfo = entry.getAsJsonObject();
                String name = text(songInfo, "name");
                String author = text(songInfo, "author");
                String lyrics = text(songInfo, "content");

                SongDisplayable s = new SongDisplayable(name, author);

                ArrayList<String> section = new ArrayList<>();
                int sectionCount = 0;
                String[] lines = lyrics.replace("\r\n", "\n").replace('\r', '\n').split("\n");
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
        } catch (JsonParseException ex) {
            throw new IOException("Invalid EpicWorship song pack", ex);
        }
        return ret;
    }

    private String text(JsonObject song, String field) {
        JsonElement value = song.get(field);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : "";
    }
}
