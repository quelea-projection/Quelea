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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing SongBeamer songs.
 * <p>
 * @author Michael
 */
public class SongBeamerParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Parse the file to get the songs.
     * <p>
     * @param file the SNG file.
     * @param statusPanel the status panel to update.
     * @return a song from the SNG file
     * @throws IOException if something went wrong with the import.
     */
    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("Cp1250")));
        String fileLine;
        StringBuilder rawLinesBuilder = new StringBuilder();
        while ((fileLine = reader.readLine()) != null) {
            rawLinesBuilder.append(fileLine).append("\n");
        }
        String rawLines = rawLinesBuilder.toString();

        Map<String, String> songProps = new HashMap<>();
        for (String line : rawLines.split("\n")) {
            if (line.trim().startsWith("#")) {
                String propName = line.substring(1, line.indexOf("="));
                String propValue = line.substring(line.indexOf("=") + 1);
                songProps.put(propName, propValue);
            } else {
                rawLines = rawLines.substring(rawLines.indexOf(line));
                break;
            }
        }
        int numTranslations = 1;
        while (songProps.get("TitleLang" + (numTranslations + 1)) != null) {
            numTranslations++;
        }

        List<List<String>> translations = new ArrayList<>();
        for (int i = 0; i < numTranslations; i++) {
            translations.add(i, new ArrayList<>());
        }

        for (String section : rawLines.split("---")) {
            if (!section.isEmpty()) {
                section = section.trim();
                boolean title = new LineTypeChecker(section.split("\n")[0]).getLineType() == Type.TITLE;
                int translationNum = 0;
                for (String line : section.split("\n")) {
                    if (line.trim().isEmpty()) {
                        if (!title) {
                            for (int i = 0; i < numTranslations; i++) {
                                translations.get(i).add("<>");
                            }
                        }
                        continue;
                    }
                    if (title) {
                        title = false;
                        for (int i = 0; i < numTranslations; i++) {
                            translations.get(i).add(line.trim());
                        }
                    } else {
                        translations.get(translationNum).add(line.trim());
                        translationNum = (translationNum + 1) % numTranslations;
                    }
                }
                while (translationNum != 0) {
                    translations.get(translationNum).add("");
                    translationNum = (translationNum + 1) % numTranslations;
                }
                for (int i = 0; i < numTranslations; i++) {
                    translations.get(i).add("");
                }
            }
        }
        String title = songProps.get("Title");
        if (title == null) {
            title = "Untitled";
        }
        String author = songProps.get("Author");
        if (author == null) {
            author = "";
        }
        SongDisplayable song = new SongDisplayable(title, author);
        if (songProps.get("#(c)") != null) {
            song.setCopyright(songProps.get("#(c)"));
        }
        if (songProps.get("key") != null) {
            song.setKey(songProps.get("key"));
        }
        song.setLyrics(arrToString(translations.get(0)));
        for (int i = 1; i < numTranslations; i++) {
            song.getTranslations().put(songProps.get("TitleLang" + (i + 1)), arrToString(translations.get(i)));
        }
        ret.add(song);
        return ret;
    }

    private static String arrToString(List<String> list) {
        StringBuilder ret = new StringBuilder();
        for (String str : list) {
            ret.append(str).append("\n");
        }
        return ret.toString();
    }
}
