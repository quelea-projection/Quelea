/*
 * Author: Bo Maryniuk <bo@suse.de>
 *
 * Copyright (c) 2013 Bo Maryniuk. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *     3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY BO MARYNIUK "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package de.suse.lib.openlyrics;

import de.suse.lib.openlyrics.properties.OpenLyricsProperties;
import de.suse.lib.openlyrics.properties.TempoProperty;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * OpenLyrics Song Bean.
 * Parsed from XML string source.
 *
 * @author bo
 */
public class OpenLyricsObject {
    private Document doc;
    private OpenLyricsProperties properties;
    private Map<Locale, List<Verse>> lyrics;


    /**
     * Parse OLBean from the XML.
     * 
     * @param source
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public OpenLyricsObject(String source)
            throws ParserConfigurationException,
                   SAXException,
                   IOException,
                   OpenLyricsException {
        this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
        this.properties = new OpenLyricsProperties();
        this.lyrics = new HashMap<Locale, List<Verse>>();

        this.parseProperties();
        this.parseLyrics();
    }


    /**
     * Parse properties.
     */
    private void parseProperties() throws OpenLyricsException {
        NodeList prop = this.doc.getElementsByTagName("properties");
        if (prop != null && prop.getLength() > 0) {
            Element properties = (Element) prop.item(0);
            this.getTitlesProp(properties);
            this.getAuthorsProp(properties);
            this.getCopyrightProp(properties);
            this.getTempoProp(properties);
            this.getKeyProp(properties);
            this.getPublisherProp(properties);
            this.getVerseOrderProp(properties);
            this.getCommentsProp(properties);
        }
    }


    /**
     * Get comments property.
     *
     * @param properties
     */
    private void getCommentsProp(Element properties) {
        NodeList commentsNodeList = properties.getElementsByTagName("comments");
        if (commentsNodeList != null) {
            for (int i = 0; i < commentsNodeList.getLength(); i++) {
                this.properties.addComment(((Element) commentsNodeList.item(i))
                        .getChildNodes().item(0).getTextContent());
            }
        }
    }


    /**
     * Get verse order.
     *
     * @param properties
     */
    private void getVerseOrderProp(Element properties) {
        NodeList verseOrderNodeList = properties.getElementsByTagName("verseOrder");
        if (verseOrderNodeList != null && verseOrderNodeList.getLength() > 0) {
            this.properties.setVerseOrder(Arrays.asList(((Element) verseOrderNodeList.item(0))
                    .getChildNodes().item(0).getTextContent().split("\\s+")));
        }
    }


    /**
     * Get publisher info.
     * 
     * @param properties
     */
    private void getPublisherProp(Element properties) {
        NodeList publisherNodeList = properties.getElementsByTagName("publisher");
        if (publisherNodeList != null && publisherNodeList.getLength() > 0) {
            this.properties.setPublisher(((Element) publisherNodeList.item(0))
                    .getChildNodes().item(0).getTextContent());
        }
    }


    /**
     * Get song key.
     *
     * @param properties
     */
    private void getKeyProp(Element properties) {
        NodeList keyNodeList = properties.getElementsByTagName("key");
        if (keyNodeList != null && keyNodeList.getLength() > 0) {
            this.properties.setKey(((Element) keyNodeList.item(0))
                    .getChildNodes().item(0).getTextContent());

            // Ensure correct syntax
            if (this.properties.getKey().length() > 1) {
                this.properties.setKey(this.properties.getKey().substring(0, 1).toUpperCase()
                        + this.properties.getKey().substring(1).toLowerCase());
            } else {
                this.properties.setKey(this.properties.getKey().toUpperCase());
            }
        }
    }


    /**
     * Get the tempo property, if any.
     *
     * @param properties
     */
    private void getTempoProp(Element properties) throws OpenLyricsException {
        NodeList tempoNodeList = properties.getElementsByTagName("tempo");
        if (tempoNodeList != null && tempoNodeList.getLength() > 0) {
            Element tempoNode = (Element) tempoNodeList.item(0);
            this.properties.setTempo(new TempoProperty(tempoNode.getChildNodes().item(0).getTextContent().toLowerCase(),
                                                       tempoNode.getAttribute("type").toLowerCase()));
        }
    }


    /**
     * Get copyright notice.
     *
     * @param properties
     */
    private void getCopyrightProp(Element properties) {
        NodeList copyrightNodeList = properties.getElementsByTagName("copyright");
        if (copyrightNodeList != null && copyrightNodeList.getLength() > 0) {
            this.properties.setCopyright(copyrightNodeList.item(0)
                    .getChildNodes().item(0).getTextContent());
        }
    }


    /**
     * Get authors.
     * @param properties
     */
    private void getAuthorsProp(Element properties) {
        NodeList authors = properties.getElementsByTagName("authors");
        if (authors != null && ((Element) authors.item(0)) != null) {
            NodeList authorsList = ((Element) authors.item(0)).getElementsByTagName("author");
            if (authorsList != null) {
                for (int i = 0; i < authorsList.getLength(); i++) {
                    if((authorsList.item(i)).getChildNodes().item(0)!=null) {
                        this.properties.addAuthor(((Element) authorsList.item(i))
                                .getChildNodes().item(0).getTextContent());
                    }
                }
            }
        } else {
            this.properties.addAuthor("Unknown Author");
        }
    }


    /**
     * Get titles properties
     */
    private void getTitlesProp(Element properties) {
        NodeList titles = properties.getElementsByTagName("titles");
        if (titles != null && ((Element) titles.item(0)) != null) {
            NodeList titleList = ((Element) titles.item(0)).getElementsByTagName("title");
            if (titleList != null) {
                for (int i = 0; i < titleList.getLength(); i++) {
                    Element titleNode = (Element) titleList.item(i);
                    String lang = titleNode.getAttribute("lang");
                    Locale locale = null;
                    if (lang.contains("-")) {
                        locale = new Locale(lang.split("-")[0], lang.split("-")[1]);
                    } else if (!lang.equals("")) {
                        locale = new Locale(lang);
                    } else {
                        locale = Locale.getDefault();
                    }

                    this.properties.addTitle(locale, titleNode.getChildNodes().item(0).getTextContent());
                }
            }
        } else {
            this.properties.addTitle(Locale.getDefault(), "Unknown Title");
        }
    }


    /**
     * Parse lyrics.
     */
    private void parseLyrics() throws OpenLyricsException {
        NodeList lyricsNodes = this.doc.getElementsByTagName("lyrics");
        if (lyricsNodes != null && lyricsNodes.getLength() > 0) {
            NodeList verseNodes = lyricsNodes.item(0).getChildNodes();
            for (int i = 0; i < verseNodes.getLength(); i++) {
                if (verseNodes.item(i) instanceof Element) {
                    this.parseVerse((Element) verseNodes.item(i));
                }
            }
        }
    }


    /**
     * Parse one verse.
     * @param verse
     */
    private void parseVerse(Element verseElement) throws OpenLyricsException {
        Locale locale = !verseElement.getAttribute("lang").equals("")
                        ? new Locale(verseElement.getAttribute("lang"))
                        : Locale.getDefault();
        if (this.lyrics.get(locale) == null) {
            this.lyrics.put(locale, new ArrayList<Verse>());
        }

        Verse verse = new Verse();
        verse.setName(verseElement.getAttribute("name")); // XXX: Or!??
        Element linesNode = (Element) verseElement.getElementsByTagName("lines").item(0);
        if (linesNode == null) {
            throw new OpenLyricsException("No lines in the verse.");
        }
        NodeList textNodes = linesNode.getChildNodes();

        StringBuilder textLine = new StringBuilder();
        VerseLine line = new VerseLine();

        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textElement = textNodes.item(i);
            if (textElement.getNodeName().toLowerCase().equals("br")) {
                if (!textLine.toString().isEmpty()) {
                    line.setText(textLine.toString());
                    verse.addLine(line);
                }

                textLine = new StringBuilder();
                line = new VerseLine();
            } else if (textElement.getNodeType() == Node.TEXT_NODE) {
                textLine.append(textElement.getTextContent());
            } else if (textElement.getNodeName().equals("chord")) {
                try {
                    Chord chord = new Chord(((Element) textElement).getAttribute("name"));
                    chord.setLineOffset(textLine.toString().trim().length()); // Skip spaces
                    line.addChord(chord);
                } catch (OpenLyricsException ex) {
                    Logger.getLogger(OpenLyricsObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (!textLine.toString().isEmpty()) {
            line.setText(textLine.toString());
            verse.addLine(line);
        }

        this.lyrics.get(locale).add(verse);
    }


    /**
     * Get lyrics with specified locale.
     * Fallback sequence: specified locale -> system default locale.
     *
     * @return
     */
    public List<Verse> getVerses(Locale locale) {
        List<Verse> verses = this.lyrics.get(locale);
        return verses != null ? verses : this.lyrics.get(Locale.getDefault());
    }


    /**
     * Get lyrics with default system locale.
     * If no lyrics under default locale, en_US returned, which is the most default. :-)
     *
     * @param locale
     * @return
     */
    public List<Verse> getVerses() {
        List<Verse> verses = this.getVerses(Locale.getDefault());
        if (verses == null) {
            verses = this.getVerses(new Locale("en", "US"));
        }
        if (verses == null) {
            Set<Locale> keySet = this.lyrics.keySet();
            if (!keySet.isEmpty()) {
                Locale firstLocale = keySet.iterator().next();
                verses = this.getVerses(firstLocale);
            }
        }
        return verses;
    }


    /**
     * Get properties of the song.
     *
     * @return
     */
    public OpenLyricsProperties getProperties() {
        return properties;
    }
}
