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


package utils;

import de.suse.lib.openlyrics.Chord;
import de.suse.lib.openlyrics.OpenLyricsObject;
import de.suse.lib.openlyrics.Verse;
import de.suse.lib.openlyrics.VerseLine;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Store OpenLyrics object into XML DOM.
 *
 * @author bo
 */
public class OpenLyricsWriter {
    private OpenLyricsObject ol;
    private Document doc;

    /**
     * IO Exception of the OpenLyrics Writer object when file exists.
     */
    public static class OpenLyricsWriterFileExistsException extends IOException {
        public OpenLyricsWriterFileExistsException(String message) {
            super(message);
        }
    }


    /**
     * IO Exception of the OpenLyrics Write object when file cannot be written.
     */
    public static class OpenLyricsWriterWriteErrorException extends IOException {
        public OpenLyricsWriterWriteErrorException(String message) {
            super(message);
        }
    }


    /**
     * Constructor.
     * 
     * @param ol
     * @throws ParserConfigurationException
     */
    public OpenLyricsWriter(OpenLyricsObject ol) 
            throws ParserConfigurationException {
        this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.ol = ol;
        this.createDOM();
    }


    /**
     * Write the XML into the file on the filesystem.
     *
     * @param xfile
     */
    public void writeToFile(File xfile, boolean overwrite)
            throws OpenLyricsWriterFileExistsException,
                   OpenLyricsWriterWriteErrorException,
                   TransformerConfigurationException,
                   TransformerException {
        if (!overwrite && xfile.exists()) {
            throw new OpenLyricsWriterFileExistsException(String.format("The file %s exists.", xfile.getAbsolutePath()));
        }

        if (overwrite && xfile.exists() && !xfile.canWrite()) {
            throw new OpenLyricsWriterWriteErrorException(String.format("Write access denied file %s exists.", xfile.getAbsolutePath()));
        }

        // Write XML
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(this.doc);
        StreamResult result = new StreamResult(xfile);

        //transformer.transform(source, result);
        transformer.transform(source, new StreamResult(System.out));
    }


    /**
     * Create DOM from the OpenLyrics object.
     */
    private void createDOM() {
        // Root
        Element songElement = this.doc.createElement("song");
        songElement.setAttribute("xmlns", "http://openlyrics.info/namespace/2009/song");
        songElement.setAttribute("createdIn", "JOpenLyricsLib");
        songElement.setAttribute("modifiedDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date()));
        this.doc.appendChild(songElement);

        // Assemble properties
        songElement.appendChild(this.getProperties());

        // Assemble lyrics
        songElement.appendChild(this.getLyrics());
    }


    /**
     * Get all the properties.
     *
     * @param properties
     */
    private Element getProperties() {
        Element properties = this.doc.createElement("properties");
        properties.appendChild(this.getTitles());
        properties.appendChild(this.getAuthors());
        properties.appendChild(this.getCopyright());
        properties.appendChild(this.getComments());
        properties.appendChild(this.getVerseOrder());
        properties.appendChild(this.getKey());
        properties.appendChild(this.getPublisher());
        properties.appendChild(this.getTempo());

        return properties;
    }


    /**
     * Get tempo of the song.
     *
     * @return
     */
    private Element getTempo() {
        Element tempoElement = this.doc.createElement("tempo");
        if (this.ol.getProperties().getTempo() != null) {
            tempoElement.setAttribute("type", this.ol.getProperties().getTempo().getType());
            tempoElement.appendChild(this.doc.createTextNode(this.ol.getProperties().getTempo().getTempo()));
        }

        return tempoElement;
    }


    /**
     * Get publisher of the song.
     * 
     * @return
     */
    private Element getPublisher() {
        Element publisherElement = this.doc.createElement("publisher");
        if (this.ol.getProperties().getPublisher() != null && !this.ol.getProperties().getPublisher().isEmpty()) {
            publisherElement.appendChild(this.doc.createTextNode(this.ol.getProperties().getPublisher()));
        }

        return publisherElement;
    }


    /**
     * Get key tonality.
     *
     * @return
     */
    private Element getKey() {
        Element keyElement = this.doc.createElement("key");
        if (this.ol.getProperties().getKey() != null && !this.ol.getProperties().getKey().isEmpty()) {
            keyElement.appendChild(this.doc.createTextNode(this.ol.getProperties().getKey()));
        }

        return keyElement;
    }


    /**
     * Get verse order.
     *
     * @return
     */
    private Element getVerseOrder() {
        // XXX: Verse order needs to be redesigned entirely.

        Element verseOrderElement = this.doc.createElement("verseOrder");
        List<String> verseOrderList = this.ol.getProperties().getVerseOrder();
        StringBuilder verseOrderText = new StringBuilder();
        if (verseOrderList != null && !verseOrderList.isEmpty()) {
            for (int i = 0; i < verseOrderList.size(); i++) {
                verseOrderText.append(verseOrderList.get(i)).append(" ");
            }
        } else {
            // Plain from XML
            List<Verse> verses = this.ol.getVerses();
            for (int i = 0; i < verses.size(); i++) {
                verseOrderText.append(verses.get(i).getName()).append(" ");
            }
        }

        verseOrderElement.appendChild(this.doc.createTextNode(verseOrderText.toString().trim()));

        return verseOrderElement;
    }


    /**
     * Get comments to the song.
     *
     * @return
     */
    private Element getComments() {
        Element commentsBlockElement = this.doc.createElement("comments");
        List<String> comments = this.ol.getProperties().getComments();
        if (comments != null && !comments.isEmpty()) {
            for (int i = 0; i < comments.size(); i++) {
                Element commentElement = this.doc.createElement("comment");
                commentElement.appendChild(this.doc.createTextNode(comments.get(i)));
                commentsBlockElement.appendChild(commentElement);
            }
        }

        return commentsBlockElement;
    }


    /**
     * Get copyright.
     * 
     * @return
     */
    private Element getCopyright() {
        Element copyrightElement = this.doc.createElement("copyright");
        String copyrightNotice = this.ol.getProperties().getCopyright();
        copyrightElement.appendChild(this.doc.createTextNode((copyrightNotice != null && !copyrightNotice.isEmpty())
                                                             ? copyrightNotice : "Unknown"));
        return copyrightElement;
    }


    /**
     * Get authors of the song.
     *
     * @return
     */
    private Element getAuthors() {
        Element authorsElement = this.doc.createElement("authors");

        // Gather authors
        List<Locale> titleLocales = this.ol.getProperties().getTitleProperty().getTitleLocales();
        for (int i = 0; i < titleLocales.size(); i++) {
            Element authorElement = this.doc.createElement("author");
            authorElement.appendChild(this.doc.createTextNode(this.ol.getProperties().getTitleProperty().getTitle(titleLocales.get(i))));
            authorsElement.appendChild(authorElement);
        }

        return authorsElement;
    }


    /**
     * Get titles of the song.
     * 
     * @return
     */
    private Element getTitles() {
        Element titlesElement = this.doc.createElement("titles");

        // Gather titles
        List<Locale> titleLocales = this.ol.getProperties().getTitleProperty().getTitleLocales();
        for (int i = 0; i < titleLocales.size(); i++) {
            Element titleElement = this.doc.createElement("title");
            titleElement.setAttribute("lang", titleLocales.get(i).getLanguage());
            titleElement.appendChild(this.doc.createTextNode(this.ol.getProperties().getTitleProperty().getTitle(titleLocales.get(i))));
            titlesElement.appendChild(titleElement);
        }

        return titlesElement;
    }


    /**
     * Get all the lyrics with chords.
     * 
     * @return
     */
    private Element getLyrics() {
        Element lyrics = this.doc.createElement("lyrics");

        // Get verses
        for (int i = 0; i < this.ol.getVerses().size(); i++) {
            Verse verse = this.ol.getVerses().get(i);

            // Create verse element
            Element verseElement = this.doc.createElement("verse");
            if (verse.getName() != null) {
                verseElement.setAttribute("name", verse.getName());
            }

            // Lines (I have no idea why they do exists at all if we already have verse!)
            Element linesElement = this.doc.createElement("lines");
            verseElement.appendChild(linesElement);

            // Add lines to the verse
            for (int j = 0; j < verse.getLines().size(); j++) {
                VerseLine line = verse.getLines().get(j);

                // Create line element with chords
                int substroffset = 0;
                for (int cidx = 0; cidx < line.getChords().size(); cidx++) {
                    Chord chord = line.getChords().get(cidx);
                    linesElement.appendChild(this.doc.createTextNode(line.getText().substring(substroffset, chord.getLineOffset())));
                    substroffset = chord.getLineOffset();

                    // Add chord to DOM
                    Element chordElement = this.doc.createElement("chord");
                    chordElement.setAttribute("name", chord.getRoot()); // XXX: Needs a proper chord render
                    linesElement.appendChild(chordElement);
                }
                // Add the rest of the text left after chords.
                linesElement.appendChild(this.doc.createTextNode(line.getText().substring(substroffset)));

                // Do not <br/> to the last line in the verse
                if ((j + 1) < verse.getLines().size()) {
                    linesElement.appendChild(this.doc.createElement("br"));
                }
            }

            // Add verse to DOM
            lyrics.appendChild(verseElement);
        }

        return lyrics;
    }
}
