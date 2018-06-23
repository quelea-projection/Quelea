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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;

/**
 * A parser for parsing the Easyworship database. Easyworship uses the Paradox
 * database format.
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
        try {
            Class.forName("com.googlecode.paradox.Driver");
            LOGGER.log(Level.INFO, "Easyworship importer from {0}", file.getParent());
            Connection conn = DriverManager.getConnection("jdbc:paradox:/" + file.getParent());
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"Songs.DB\"");
            while(rs.next()) {
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                if(author == null) {
                    author = "";
                }
                String lyrics = rs.getString("Words");
                String copyright = rs.getString("Copyright");
                String num = rs.getString("Song Number");
                ret.add(getSong(title, author, lyrics, copyright, num));
            }

        }
        catch(ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.INFO, "Couldn't import using SQL from " + file.getParent(), ex);
        }
        return ret;
    }

    private SongDisplayable getSong(String title, String author, String songContent, String copyright, String ccli) {
        if(songContent.contains(FNT)) {
            int fntInd = songContent.indexOf(FNT) + FNT.length();
            songContent = songContent.substring(fntInd);
        }
        songContent = songContent.replace("\\line", "\n");
        songContent = songContent.replaceAll("\\\\[a-z0-9]+[ ]?", "");
        if(songContent.contains("{{")) {
            songContent = songContent.substring(0, songContent.indexOf("{{"));
        }
        songContent = trimLines(songContent);
        songContent = songContent.replaceAll("\n[\n]+", "\n\n");
        Matcher matcher = Pattern.compile("(\\\\\\'([0-9a-f][0-9a-f]))").matcher(songContent);
        while(matcher.find()) {
            String num = matcher.group(2);
            int b = Integer.parseInt(num, 16);
            char val = new String(new byte[]{(byte) b}, Charset.forName("windows-1252")).charAt(0);
            songContent = songContent.replace(matcher.group(1), Character.toString(val));
        }
        songContent = songContent.replace("{", "");
        songContent = songContent.replace("}", "");
        songContent = trimLines(songContent);
        SongDisplayable song = new SongDisplayable(title, author);
        song.setLyrics(songContent);
        song.setCopyright(copyright);
        song.setCcli(ccli);
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

}
