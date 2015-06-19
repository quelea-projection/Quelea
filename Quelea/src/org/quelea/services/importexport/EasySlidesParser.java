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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.UnicodeReader;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael
 */
public class EasySlidesParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    @Override
    public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
        try {
            ArrayList<SongDisplayable> ret = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new UnicodeReader(new FileInputStream(file), "UTF-8")));
            NodeList songs = doc.getChildNodes().item(0).getChildNodes();
            for(int i = 0; i < songs.getLength(); i++) {
                NodeList attribNodes = songs.item(i).getChildNodes();
                String title = null, author = "", capo = "", copyright = "", lyrics = "";
                for(int j = 0; j < attribNodes.getLength(); j++) {
                    Node curNode = attribNodes.item(j);
                    if(curNode.getNodeName().equalsIgnoreCase("title1")) {
                        title = curNode.getTextContent();
                    }
                    if(curNode.getNodeName().equalsIgnoreCase("writer")) {
                        author = curNode.getTextContent();
                    }
                    if(curNode.getNodeName().equalsIgnoreCase("capo")) {
                        capo = curNode.getTextContent();
                    }
                    if(curNode.getNodeName().equalsIgnoreCase("copyright")) {
                        copyright = curNode.getTextContent();
                    }
                    if(curNode.getNodeName().equalsIgnoreCase("contents")) {
                        lyrics = curNode.getTextContent();
                    }
                }
                if(title == null) {
                    continue;
                }
                SongDisplayable song = new SongDisplayable(title, author);
                song.setCopyright(copyright);
                if(!capo.equalsIgnoreCase("-1")) {
                    song.setCapo(capo);
                }
                for(TextSection section : getSections(lyrics)) {
                    song.addSection(section);
                }
                ret.add(song);
            }
            return ret;
        }
        catch(ParserConfigurationException | SAXException | IOException | DOMException ex) {
            LOGGER.log(Level.WARNING, "Something went wrong importing easyslides songs", ex);
            return null;
        }
    }

    private List<TextSection> getSections(String lyrics) {
        String[] sections = lyrics.split("\\[.+?\\]");
        List<TextSection> ret = new ArrayList<>();
        for(String section : sections) {
            section = section.trim();
            if(section.isEmpty()) {
                continue;
            }
            ret.add(new TextSection("", section.split("\n"), null, true));
        }
        return ret;
    }

}
