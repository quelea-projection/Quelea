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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * Parsers SoF RTF files.
 * @author Michael
 */
public class SoFParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        if(file==null) {
            return ret;
        }
        
        String fileText = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())))
                .replace("\\page", "%PAGEMARKER%")
                .replaceAll("(\\\\par[\\s]*\n){3,}", "%PAGEMARKER%")
                .replace("1691", "%PAGEMARKER%1691")
                .replace("\\line", "\\par\n");
        
        RTFEditorKit rtfParser = new RTFEditorKit();
        javax.swing.text.Document document = rtfParser.createDefaultDocument();
        String rawText;
        try {
            rtfParser.read(new ByteArrayInputStream(fileText.getBytes()), document, 0);
            rawText = document.getText(0, document.getLength());
        }
        catch(BadLocationException ex) {
            statusPanel.done();
            throw new IOException("Error importing SOF", ex);
        }

        String[] songs = rawText.split("%PAGEMARKER%");

        for(int i=0 ; i <songs.length ; i++) {
            statusPanel.setProgress((double) i / songs.length);
            
            String songText = songs[i];
            if (songText.toLowerCase().contains("regrettably, permission has not been granted")) {
                continue;
            }
            songText = songText.replaceFirst("[0-9]+", "");
            int endIdx = songText.indexOf("A Songs of Fellowship Worship Resource");
            if (endIdx == -1) {
                endIdx = songText.length();
            }
            songText = songText.substring(0, endIdx).trim();

            Pattern p = Pattern.compile("\n[\\s]*(([A-Z]+[^a-zA-Z\\.]+){2,})");
            Matcher m = p.matcher(songText);
            if (m.find()) {
                String title = m.group(1);
                title = replaceLast(title, "[^a-zA-z]+", "");
                if(title.length()>2) {
                    title = title.substring(0, 1)+title.substring(1, title.length()).toLowerCase();
                }
                int position = m.start();
                String header = songText.substring(0, position).trim();
                String[] headerParts = header.split("\n");
                String author = headerParts[0].trim();
                author = replaceLast(author, "[^a-zA-z]+", "");
                String copyright = null;
                for (String headerPart : headerParts) {
                    if (headerPart.toLowerCase().contains("copyright")) {
                        copyright = headerPart.trim();
                        break;
                    }
                }

                String lyrics = songText.substring(position, songText.length()).trim();
                
                SongDisplayable song = new SongDisplayable(title, author);
                song.setLyrics(lyrics);
                song.setCopyright(copyright);
                ret.add(song);
            }
        }
        statusPanel.done();
        return ret;
    }
    
    private static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

}
