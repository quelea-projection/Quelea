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
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A parser for parsing Presentation Manager databases.
 * <p>
 * @author Michael
 */
public class PresentationManagerParser implements SongParser {

    /**
     * Get a list of the songs contained in the given Presentation Manager
     * database.
     * <p>
     * @throws IOException if something goes wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        List<SongDisplayable> ret = new ArrayList<>();
        String rawXML = Utils.getTextFromFile(location.getAbsolutePath(), "", "UTF-16LE");
        String[] lines = rawXML.split("\n");
        StringBuilder xmlText = new StringBuilder();
        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
                continue;
            }
            if (!line.startsWith("<?xml")) {
                xmlText.append(line.trim()).append("\n");
            }
        }
        String xmlStr = xmlText.toString();
        if (!xmlStr.trim().endsWith("</song>")) {
            xmlStr += "</song>";
        }
        SongDisplayable song = getSongFromXML(xmlStr);
        if (song != null) {
            ret.add(song); //Format seems to miss out the end tag...
        }
        return ret;
    }

    private SongDisplayable getSongFromXML(String lyricsXML) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(lyricsXML.getBytes("UTF-8")));
            Node root = doc.getElementsByTagName("song").item(0);
            StringBuilder lyrics = new StringBuilder();
            String title = "Presentation Manager Song";
            String author = "";
            NodeList rootChildren = root.getChildNodes();
            for (int i = 0; i < rootChildren.getLength(); i++) {
                if (rootChildren.item(i).getNodeName().equalsIgnoreCase("verses")) {
                    NodeList verses = rootChildren.item(i).getChildNodes();
                    for (int j = 0; j < verses.getLength(); j++) {
                        String verseText = verses.item(j).getTextContent().trim();
                        if (!verseText.isEmpty()) {
                            String[] verseLines = verseText.split("\n");
                            StringBuilder verseBuilder = new StringBuilder();
                            for (String verseLine : verseLines) {
                                verseLine = verseLine.trim();
                                if (verseLine.isEmpty()) {
                                    verseLine = " "; //Alt + 0160, non breaking space
                                }
                                verseBuilder.append(verseLine).append("\n");
                            }
                            lyrics.append(verseBuilder.toString().trim()).append("\n\n");
                        }
                    }
                }

                if (rootChildren.item(i).getNodeName().equalsIgnoreCase("attributes")) {
                    NodeList attributes = rootChildren.item(i).getChildNodes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        String name = attributes.item(j).getNodeName();
                        if (name.equalsIgnoreCase("Title")) {
                            title = attributes.item(j).getTextContent();
                        }
                        if (name.equalsIgnoreCase("Author")) {
                            author = attributes.item(j).getTextContent();
                        }
                    }
                }
            }
            SongDisplayable ret = new SongDisplayable(title, author);
            ret.setLyrics(lyrics.toString().trim());
            return ret;
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            return null;
        }
    }

}
