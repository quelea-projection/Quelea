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

import de.suse.lib.openlyrics.OpenLyricsException;
import de.suse.lib.openlyrics.OpenLyricsObject;
import de.suse.lib.openlyrics.Verse;
import de.suse.lib.openlyrics.VerseLine;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.StatusPanel;
import org.xml.sax.SAXException;

/**
 * A parser for parsing zip files of openlyrics.
 *
 * @author Michael
 */
public class OpenLyricsParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Get a list of the openlyrics songs contained in the given zip file.
     *
     * @param location the location of the zip file.
     * @return a list of the songs found.
     * @throws IOException if something goes wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        final ZipFile file = new ZipFile(location, Charset.forName("Cp437"));
        List<SongDisplayable> ret = new ArrayList<>();
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                StringBuilder fileContents = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(entry), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        fileContents.append(line.trim());
                    }
                }
                OpenLyricsObject ol = new OpenLyricsObject(fileContents.toString());
                String lyrics = getLyrics(ol);
                String title = ol.getProperties().getTitleProperty().getDefaultTitle();
                String author = getAuthor(ol);
                if (!lyrics.isEmpty()) {
                    SongDisplayable displayable = new SongDisplayable(title, author);
                    displayable.setLyrics(lyrics);
                    ret.add(displayable);
                }
                else {
                    LOGGER.log(Level.INFO, "Song had empty lyrics");
                }
            }
        } catch (OpenLyricsException | IOException | ParserConfigurationException | SAXException ex) {
            LOGGER.log(Level.WARNING, "Error importing openlyrics archive", ex);
        } finally {
            file.close();
        }
        return ret;
    }

    private String getAuthor(OpenLyricsObject ol) {
        StringBuilder ret = new StringBuilder();
        List<String> authors = ol.getProperties().getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            ret.append(authors.get(i));
            if (i < authors.size() - 1) {
                ret.append(", ");
            }
        }
        return ret.toString().trim();
    }

    /**
     * Get lyrics as a string from an openlyrics object.
     *
     * @param ol the openlyrics POJO
     * @return the lyrics as a string
     */
    private String getLyrics(OpenLyricsObject ol) {
        StringBuilder ret = new StringBuilder();
        if (ol == null || ol.getVerses() == null) {
            LOGGER.log(Level.WARNING, "Couldn't create openlyrics object");
        } else {
            for (Verse verse : ol.getVerses()) {
                for (VerseLine line : verse.getLines()) {
                    ret.append(line.getText().trim()).append('\n');
                }
                ret.append('\n');
            }
        }
        return ret.toString().trim();
    }

}
