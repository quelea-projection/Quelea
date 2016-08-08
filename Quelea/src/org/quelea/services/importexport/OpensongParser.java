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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.UnicodeReader;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A parser for importing Opensong databases
 * <p>
 * @author Michael
 */
public class OpensongParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        ZipFile file = new ZipFile(location, Charset.forName("Cp437"));
        List<SongDisplayable> ret = new ArrayList<>();
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new InputSource(new UnicodeReader(file.getInputStream(entry), "UTF-8")));
                NodeList list = doc.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getNodeName().equalsIgnoreCase("song")) {
                        SongDisplayable displayable = getDisplayable(list.item(i));
                        if (displayable != null) {
                            ret.add(displayable);
                        }
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            LOGGER.log(Level.WARNING, "Error importing opensong", ex);
        } finally {
            file.close();
        }
        return ret;
    }

    /**
     * Get a song displayable from a song node.
     *
     * @param root the "song" root node
     * @return the SongDisplayable represented by this node.
     */
    private SongDisplayable getDisplayable(Node root) {
        NodeList nl = root.getChildNodes();
        String title = "";
        String author = "";
        String lyrics = "";
        String copyright = "";
        String key = "";
        String ccli = "";
        String capo = "";
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeName().equalsIgnoreCase("title")) {
                title = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("author")) {
                author = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("copyright")) {
                copyright = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("key")) {
                key = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("ccli")) {
                ccli = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("capo")) {
                capo = node.getTextContent();
            } else if (node.getNodeName().equalsIgnoreCase("lyrics")) {
                lyrics = node.getTextContent();
            }
        }
        if (title.isEmpty()) {
            return null;
        }
        SongDisplayable ret = new SongDisplayable(title, author);
        ret.setLyrics(processLyrics(lyrics));
        ret.setCopyright(copyright);
        ret.setKey(key);
        ret.setCcli(ccli);
        ret.setCapo(capo);
        return ret;
    }

    private String processLyrics(String lyrics) {
        StringBuilder ret = new StringBuilder();
        String[] arr = lyrics.split("\n");
        boolean ignoreVerse = false;
        for (String line : arr) {
            line = line.trim();
            if (line.startsWith("[") && line.endsWith("]") && line.length() > 2) {
                ignoreVerse = false;
                String sectionTitle = line.substring(1, line.length() - 1);
                if (sectionTitle.startsWith("V")) {
                    if (sectionTitle.length() > 1) {
                        line = "Verse " + sectionTitle.substring(1);
                    } else {
                        line = "Verse";
                    }
                } else if (sectionTitle.startsWith("C")) {
                    if (sectionTitle.length() > 1) {
                        line = "Chorus " + sectionTitle.substring(1);
                    } else {
                        line = "Chorus";
                    }
                } else if (sectionTitle.startsWith("T")) {
                    if (sectionTitle.length() > 1) {
                        line = "Tag " + sectionTitle.substring(1);
                    } else {
                        line = "Tag";
                    }
                } else if (sectionTitle.startsWith("B")) {
                    if (sectionTitle.length() > 1) {
                        line = "Bridge " + sectionTitle.substring(1);
                    } else {
                        line = "Bridge";
                    }
                } else {
                    line = "Verse";
                }
            } else if (line.startsWith(".")) {
                line = line.substring(1);
            } else if (line.endsWith("||")) {
                line = line.substring(0, line.length()-2) + "\n";
            } else if (line.equals("---")) {
                line = "";
            } else if (line.startsWith(";")) {
                ignoreVerse = true;
            }
            line = line.replace("|", "\n");
            
            if (!ignoreVerse) {
                ret.append(line).append("\n");
            }
        }
        return ret.toString();
    }

}
