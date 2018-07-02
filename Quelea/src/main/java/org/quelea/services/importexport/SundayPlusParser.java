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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for sunday plus files.
 *
 * @author Michael
 */
public class SundayPlusParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        final ZipFile file = new ZipFile(location);
        List<SongDisplayable> ret = new ArrayList<>();
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                String title = new File(entry.getName()).getName();
                LOGGER.log(Level.INFO, "Found {0}", title);
                if (title.contains(".")) {
                    String[] parts = title.split("\\.");
                    StringBuilder titleBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        titleBuilder.append(parts[i]);
                        if (i < parts.length - 2) {
                            titleBuilder.append(".");
                        }
                    }
                    title = titleBuilder.toString().replace("_", " ");
                    if (title.endsWith("[1]")) {
                        title = title.substring(0, title.length() - 3);
                    }
                }

                try (java.util.Scanner s = new java.util.Scanner(file.getInputStream(entry))) {
                    //This gets the string from the inputstream without messing up line endings...
                    String fileContents = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    //And this replaces all "pard" RTF control words with "par" control words, so Swing's RTF parser can understand it and extract the plain text.
                    fileContents = fileContents.replaceAll("\\\\pard", "\\\\par");
                    String lyrics = getLyrics(fileContents);
                    if (!lyrics.isEmpty()) {
                        LOGGER.log(Level.INFO, "Adding song");
                        SongDisplayable displayable = new SongDisplayable(title, "");
                        displayable.setLyrics(lyrics);
                        ret.add(displayable);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error importing sunday plus", ex);
        } finally {
            file.close();
        }
        return ret;
    }

    private String getLyrics(String raw) {
        try {
            RTFEditorKit rtfParser = new RTFEditorKit();
            Document document = rtfParser.createDefaultDocument();
            rtfParser.read(new ByteArrayInputStream(raw.getBytes()), document, 0);
            String text = document.getText(0, document.getLength());
            StringBuilder textBuilder = new StringBuilder();
            for (String line : text.split("\n")) {
                textBuilder.append(line.trim()).append("\n");
            }
            return textBuilder.toString().replaceAll("[\n]{2,}", "\n\n").replace("^", "'").trim();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Invalid RTF string, trying old method");
            return getLyricsOld(raw);
        }
    }

    private String getLyricsOld(String raw) {
        Pattern p = Pattern.compile("\\{\\\\pard.+?\\}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(raw);
        StringBuilder sections = new StringBuilder();
        while (m.find()) {
            String verse = m.group();
            if (verse.contains("\\qc")) {
                verse = verse.substring(verse.indexOf("\\qc") + "\\qc".length(), verse.length() - 1);
            }
            verse = verse.replace("\\par", "\n");
            verse = verse.replace("^", "'");
            verse = verse.trim();
            if (!verse.isEmpty()) {
                sections.append(verse).append("\n\n");
            }
        }
        return sections.toString().trim();
    }

}
