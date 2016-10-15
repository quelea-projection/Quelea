/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2016 Michael Berry
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing the Mission Praise RTF files
 * <p>
 * @author Michael
 */
public class MissionPraiseParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Parse the file to get the songs.
     * <p>
     * @param file the RTF file.
     * @param statusPanel the status panel to update.
     * @return a list of the songs found in the Songs.MB file.
     * @throws IOException if something went wrong with the import.
     */
    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        String text = Utils.getTextFromFile(file.getAbsolutePath(), "", "CP1250");
        text = text.replace("’", "'");
        try {
            if (!text.isEmpty()) {
                RTFEditorKit rtfParser = new RTFEditorKit();
                Document document = rtfParser.createDefaultDocument();
                rtfParser.read(new ByteArrayInputStream(text.getBytes("CP1250")), document, 0);
                String plainText = document.getText(0, document.getLength());
                String title = getTitle(plainText);
                String lyrics = getLyrics(plainText);
                String author = getAuthor(plainText);
                String copyright = getCopyright(plainText);
                
                SongDisplayable song = new SongDisplayable(title, author);
                song.setLyrics(lyrics);
                song.setCopyright(copyright);
                ret.add(song);
            }
        } catch (IOException | BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Error importing mission praise", ex);
        }
        return ret;
    }
    
    private String getTitle(String plainText) {
        String line = plainText.split("\n")[0].trim();
        return line.replace(" ", " ").trim();
//        return line.replace(" ", " ").replaceAll("^[0-9]+\\s+", "").trim();
    }
    
    private String getLyrics(String plainText) {
        String[] arr = plainText.split("\n");
        int endIdx = arr.length;
        for(int i=1 ; i<arr.length ; i++) {
            if(arr[i].startsWith("©")) {
                endIdx = i;
                break;
            }
        }
        StringBuilder ret = new StringBuilder();
        for(int i=1 ; i<endIdx ; i++) {
            ret.append(arr[i].replaceAll("^[0-9]+\\s+", "").replace(" ", " ").trim()).append("\n");
        }
        return ret.toString().trim();
    }
    
    private String getAuthor(String plainText) {
        for(String str : plainText.split("\n")) {
            if(str.matches("^[a-zA-z].+")) {
                return str.trim();
            }
        }
        return "";
    }
    
    private String getCopyright(String plainText) {
        for(String str : plainText.split("\n")) {
            if(str.startsWith("©")) {
                return str.trim();
            }
        }
        return "";
    }

}
