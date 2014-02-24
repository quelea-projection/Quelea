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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing the easyworship Songs.MB database. This is in a
 * binary-ish format with the song text in plaintext - so we use this to extract
 * the song text without using a third party library. It's not foolproof and is
 * a bit of a hack but does the job for most songs ok it seems...!
 * <p>
 * @author Michael
 */
public class EasyWorshipParser implements SongParser {

    private static final String FNT = "\\fntnamaut";
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Parse the file to get the songs.
     * <p>
     * @param file the Songs.MB file.
     * @param statusPanel the status panel to update.
     * @return a list of the songs found in the Songs.MB file.
     * @throws IOException if something went wrong with the import.
     */
    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder songContent = new StringBuilder();
        boolean inSong = false;
        while((line = reader.readLine()) != null) {
            if(!inSong && line.contains(FNT)) {
                inSong = true;
            }
            if(inSong) {
                songContent.append(line).append("\n");
            }
            if(inSong && line.contains("}")) {
                inSong = false;
                SongDisplayable song = getSong(songContent.toString());
                if(song != null) {
                    ret.add(song);
                }
                songContent = new StringBuilder();
            }
        }
        return ret;
    }

    private SongDisplayable getSong(String songContent) {
        int fntInd = songContent.indexOf(FNT) + FNT.length();
        songContent = songContent.substring(fntInd);
        songContent = songContent.replace("\\line", "\n");
        songContent = songContent.replaceAll("\\\\[a-z0-9]+", "");
        if(songContent.contains("{{")) {
            songContent = songContent.substring(0, songContent.indexOf("{{"));
        }
        songContent = trimLines(songContent);
        songContent = songContent.replaceAll("\n[\n]+", "\n\n");
        songContent = songContent.replace("\\'85", "...");
        songContent = songContent.replace("\\'91", "'");
        songContent = songContent.replace("\\'92", "'");
        songContent = songContent.replace("\\'93", "\"");
        songContent = songContent.replace("\\'94", "\"");
        songContent = songContent.replace("\\'96", "-");
        songContent = songContent.replace("{", "");
        songContent = songContent.replace("}", "");
        songContent = trimLines(songContent);
        SongDisplayable song = new SongDisplayable("", "");
        song.setLyrics(songContent);
        if(song.getTitle() == null || song.getTitle().isEmpty()) { //Invalid song, so forget it
            song = null;
        }
        return song;
    }

    private String trimLines(String oldContent) {
        StringBuilder ret = new StringBuilder();
        for(String line : oldContent.split("\n")) {
            ret.append(line.trim()).append("\n");
        }
        return ret.toString().trim();
    }

    public static void main(String[] args) throws Exception {
        EasyWorshipParser ewp = new EasyWorshipParser();
        ewp.getSongs(new File("C:\\Users\\Michael\\Documents\\Church\\Databases\\Easyworship\\Softouch\\EasyWorship\\Default\\Databases\\Data\\Songs.MB"), null);
    }

}
