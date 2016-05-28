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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 * A parser for FreeWorship XML files
 *
 * @author Michael
 */
public class FreeWorshipParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        try {
            List<SongDisplayable> ret = new ArrayList<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Node root = doc.getDocumentElement();
            Node props = getChildByName(root, "properties");
            String title = "";
            String author = "";
            String copyright = "";
            String publisher = "";
            String ccli = "";
            for (int i = 0; i < props.getChildNodes().getLength(); i++) {
                Node propNode = props.getChildNodes().item(i);
                if (propNode.getNodeName().equals("titles")) {
                    title = getChildByName(propNode, "title").getTextContent();
                } else if (propNode.getNodeName().equals("authors")) {
                    author = getChildByName(propNode, "author").getTextContent();
                } else if (propNode.getNodeName().equals("copyright")) {
                    copyright = propNode.getTextContent();
                } else if (propNode.getNodeName().equals("ccliNo")) {
                    ccli = propNode.getTextContent();
                } else if (propNode.getNodeName().equals("publisher")) {
                    publisher = propNode.getTextContent();
                }
            }
            SongDisplayable song = new SongDisplayable(title, author);
            song.setCopyright(copyright);
            song.setCopyright(publisher);
            song.setCopyright(ccli);
            StringBuilder lyricsBuilder = new StringBuilder();
            Node lyrics = getChildByName(root, "lyrics");
            for (int i = 0; i < lyrics.getChildNodes().getLength(); i++) {
                Node verseNode = lyrics.getChildNodes().item(i);
                if (!verseNode.getNodeName().equals("verse")) {
                    continue;
                }
                Node lines = getChildByName(verseNode, "lines");
                lyricsBuilder.append(innerXml(lines)).append("\n\n");
            }
            song.setLyrics(lyricsBuilder.toString());
            ret.add(song);
            return ret;
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            LOGGER.log(Level.WARNING, "Something went wrong importing FreeWorship songs", ex);
            return null;
        }
    }

    private Node getChildByName(Node parent, String name) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeName().equals(name)) {
                return parent.getChildNodes().item(i);
            }
        }
        return null;
    }

    private String innerXml(Node node) {
        DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            String str = lsSerializer.writeToString(childNodes.item(i));
            if (str.endsWith("<br/>")) {
                str = "\n";
            }
            sb.append(str);
        }
        return sb.toString();
    }

}
