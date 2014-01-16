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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A parser for parsing OpenLP databases.
 * <p>
 * @author Michael
 */
public class OpenLPParser implements SongParser {

    /**
     * Get a list of the songs contained in the given OpenLP database.
     * <p>
     * @param location the location of the OpenLP database.
     * @return a list of the songs found.
     * @throws IOException if something goes wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + location.getAbsolutePath());
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM songs");
            while(rs.next()) {
                String lyrics = getLyricsFromXML(rs.getString("lyrics"));
                String title = rs.getString("title");
                String ccli = rs.getString("ccli_number");
                String copyright = rs.getString("copyright");
                if(title != null && lyrics != null && !title.isEmpty() && !lyrics.isEmpty()) {
                    SongDisplayable displayable = new SongDisplayable(title, "");
                    displayable.setLyrics(lyrics);
                    if(ccli != null) {
                        displayable.setCcli(ccli.trim());
                    }
                    if(copyright != null) {
                        displayable.setCopyright(copyright.trim());
                    }
                    ret.add(displayable);
                }
            }
            return ret;
        }
        catch(ClassNotFoundException | SQLException ex) {
            return ret;
        }
    }

    private String getLyricsFromXML(String lyricsXML) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(lyricsXML.getBytes("UTF8")));
            Node root = doc.getElementsByTagName("song").item(0).getFirstChild();
            StringBuilder lyrics = new StringBuilder();
            NodeList verses = root.getChildNodes();
            for(int j = 0; j < verses.getLength(); j++) {
                lyrics.append(verses.item(j).getTextContent()).append("\n\n");
            }
            return lyrics.toString().trim();
        }
        catch(ParserConfigurationException | SAXException | IOException | DOMException ex) {
            return null;
        }
    }

}
