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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.stage.FileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An exporter for the openlyrics format.
 *
 * @author Michael
 */
public class OpenLyricsExporter implements Exporter {

    public static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Get the file chooser to be used.
     * <p/>
     * @return the zip file chooser..
     */
    @Override
    public FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.ZIP);
        return chooser;
    }

    /**
     * Export the given songs to the given file.
     *
     * @param file the zip file to export to.
     * @param songDisplayables the songs to export to the zip file.
     */
    @Override
    public void exportSongs(final File file, List<SongDisplayable> songDisplayables) {
        final List<SongDisplayable> songDisplayablesThreadSafe = new ArrayList<>(songDisplayables);
        final StatusPanel panel = QueleaApp.get().getMainWindow().getMainPanel().getStatusPanelGroup().addPanel(LabelGrabber.INSTANCE.getLabel("exporting.label") + "...");
        new Thread() {
            public void run() {
                try {
                    final HashSet<String> names = new HashSet<>();
                    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file), Charset.forName("UTF-8"))) {
                        for (int i = 0; i < songDisplayablesThreadSafe.size(); i++) {
                            SongDisplayable song = songDisplayablesThreadSafe.get(i);
                            String name = song.getTitle() + ".xml";
                            while (names.contains(name)) {
                                name = PDFExporter.incrementExtension(name);
                            }
                            names.add(name);
                            out.putNextEntry(new ZipEntry(name));
                            out.write(getXML(song).getBytes(Charset.forName("UTF-8")));
                            panel.setProgress((double) i / songDisplayablesThreadSafe.size());
                        }
                        panel.done();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't export openlyrics songs", ex);
                }
            }
        }.start();

    }

    /**
     * Convert a particular song to Openlyrics standard XML. Doesn't do chords
     * or translations at present - that should be implemented at a later stage.
     *
     * @param song the song to convert to XML.
     * @return the openlyrics XML for the given song.
     */
    private static String getXML(SongDisplayable song) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("song");
            rootElement.setAttribute("xmlns", "http://openlyrics.info/namespace/2009/song");
            rootElement.setAttribute("version", "0.9");
            doc.appendChild(rootElement);

            Element propertiesElement = doc.createElement("properties");
            rootElement.appendChild(propertiesElement);

            Element titlesElement = doc.createElement("titles");
            propertiesElement.appendChild(titlesElement);
            Element titleElement = doc.createElement("title");
            titleElement.appendChild(doc.createTextNode(song.getTitle()));
            titlesElement.appendChild(titleElement);

            if (song.getAuthor() != null && !song.getAuthor().trim().isEmpty()) {
                Element authorsElement = doc.createElement("authors");
                propertiesElement.appendChild(authorsElement);
                Element authorElement = doc.createElement("author");
                authorElement.appendChild(doc.createTextNode(song.getAuthor()));
                authorsElement.appendChild(authorElement);
            }

            if (song.getCcli() != null && !song.getCcli().trim().isEmpty()) {
                Element ccliElement = doc.createElement("ccliNo");
                propertiesElement.appendChild(ccliElement);
                ccliElement.appendChild(doc.createTextNode(song.getCcli()));
            }

            Element lyricsElement = doc.createElement("lyrics");
            rootElement.appendChild(lyricsElement);

            int sectionNum = 1;
            for (TextSection section : song.getSections()) {
                Element verseElement = doc.createElement("verse");
                verseElement.setAttribute("name", "v" + sectionNum);
                lyricsElement.appendChild(verseElement);
                Element linesElement = doc.createElement("lines");
                verseElement.appendChild(linesElement);

                for (String line : section.getText(false, false)) {
                    linesElement.appendChild(doc.createTextNode(line));
                    linesElement.appendChild(doc.createElement("br"));
                }
                sectionNum++;
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();

        } catch (ParserConfigurationException | TransformerException | DOMException ex) {
            LOGGER.log(Level.WARNING, "Couldn't export openlyrics songs", ex);
            return null;
        }
    }

    @Override
    public String getStrExtension() {
        return "zip";
    }

}
