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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
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
        URL u = null;
        try {
            File jarFile = new File(QueleaProperties.getQueleaUserHome().getAbsolutePath(), "Paradox_JDBC41.jar");
            u = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/");
            String classname = "com.hxtt.sql.paradox.ParadoxDriver";
            URLClassLoader ucl = new URLClassLoader(new URL[]{u});
            Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
            DriverManager.registerDriver(new DriverShim(d));
            Connection conn = DriverManager.getConnection("jdbc:paradox:/" + file.getParent() + "?useUnicode=true&characterEncoding=UTF-8");
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"Songs.DB\"");
            while(rs.next()) {
                byte[] arr = rs.getBytes("Title");
                StringBuilder titleBuilder = new StringBuilder();
                for(byte b : arr) {
                    char c = (char)(b&0xff);
                    titleBuilder.append(c);
                }
                String title = titleBuilder.toString();
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
        catch(ClassNotFoundException | IllegalAccessException | InstantiationException | MalformedURLException | SQLException ex) {
            LOGGER.log(Level.INFO, "Couldn't import using SQL from " + u, ex);
            ret.clear();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "US-ASCII")); //Easyworhsip DB always in this encoding
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
                    SongDisplayable song = getSong("", "", songContent.toString(), "", "");
                    if(song != null) {
                        ret.add(song);
                    }
                    songContent = new StringBuilder();
                }
            }
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
            char val = (char) Integer.parseInt(num, 16);
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

class DriverShim implements Driver {

    private Driver driver;

    DriverShim(Driver d) {
        this.driver = d;
    }

    public boolean acceptsURL(String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    public Connection connect(String u, Properties p) throws SQLException {
        return this.driver.connect(u, p);
    }

    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
        return this.driver.getPropertyInfo(u, p);
    }

    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.driver.getParentLogger();
    }
}
