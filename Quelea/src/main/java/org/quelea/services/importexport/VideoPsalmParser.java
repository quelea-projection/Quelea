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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
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
		List<SongDisplayable> resultSongs = new ArrayList<>();

		String contents = Utils.getTextFromFile(location.getAbsolutePath(), null, Utils.getEncoding(location));
		if (contents == null) {
			return resultSongs;
		}
		if (contents.startsWith(UTF8_BOM)) {
			contents = contents.substring(1);
		}
		JsonElement jElement = JsonParser.parseString(contents);

		if (jElement == null) {
			return resultSongs;
		}

		JsonArray jArray = jElement.getAsJsonObject().get("Songs").getAsJsonArray();

		for (JsonElement element : jArray) {
			String title = "Unknown title";
			if (element.getAsJsonObject().has("Text")) {
				title = element.getAsJsonObject().get("Text").getAsString();
			}
			String author = "";
			if (element.getAsJsonObject().has("Author")) {
				author = element.getAsJsonObject().get("Author").getAsString();
			}
			SongDisplayable song = new SongDisplayable(title, author);
			StringBuilder lyrics = new StringBuilder();
			for (JsonElement verseObj : element.getAsJsonObject().getAsJsonArray("Verses")) {
				if (verseObj.getAsJsonObject().has("Text")) {
					lyrics.append(verseObj.getAsJsonObject().get("Text").getAsString().replaceAll("<[^>]+>", ""));
					lyrics.append("\n\n");
				}
			}
			song.setLyrics(lyrics.toString().trim());
			resultSongs.add(song);
		}
		return resultSongs;
	}

}
