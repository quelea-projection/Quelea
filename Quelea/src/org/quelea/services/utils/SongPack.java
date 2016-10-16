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
package org.quelea.services.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.importexport.PDFExporter;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * A song pack that contains a number of songs and can be written to a
 * compressed archive.
 *
 * @author Michael
 */
public class SongPack {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final List<SongDisplayable> songs;

    /**
     * Create a new song pack.
     */
    public SongPack() {
        songs = new ArrayList<>();
    }

    /**
     * Add a song to this pack.
     *
     * @param song the song to add.
     */
    public void addSong(SongDisplayable song) {
        songs.add(song);
    }
    
    public void addSongs(Collection<SongDisplayable> songsToAdd) {
        songs.addAll(songsToAdd);
    }
    
    /**
     * Create a new song pack from a file.
     *
     * @param file the file to create the song pack from.
     * @return the song pack that's been created
     * @throws IOException if something went wrong.
     */
    public static SongPack fromFile(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file, Charset.forName("UTF-8"))) {
            SongPack ret = new SongPack();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                SongDisplayable song = SongDisplayable.parseXML(zipFile.getInputStream(entry));
                if (song != null) {
                    ret.addSong(song);
                }
            }
            return ret;
        }
    }

    /**
     * Write this song pack to a file.
     *
     * @param file the file to write to.
     */
    public void writeToFile(final File file) {
        if (file == null) {
            return;
        }
        
        final StatusPanel panel = QueleaApp.get().getMainWindow().getMainPanel().getStatusPanelGroup().addPanel(LabelGrabber.INSTANCE.getLabel("exporting.label") + "...");
        final List<SongDisplayable> songDisplayablesThreadSafe = new ArrayList<>(songs);
        new Thread() {
            public void run() {
                final HashSet<String> names = new HashSet<>();
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file), Charset.forName("UTF-8"))) {
                    for(int i=0 ; i<songDisplayablesThreadSafe.size() ; i++) {
                        SongDisplayable song = songDisplayablesThreadSafe.get(i);
                        String name = song.getTitle() + ".xml";
                        while (names.contains(name)) {
                            name = PDFExporter.incrementExtension(name);
                        }
                        names.add(name);
                        zos.putNextEntry(new ZipEntry(name));
                        zos.write(song.getXML().getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                        panel.setProgress((double) i / songDisplayablesThreadSafe.size());
                    }
                    panel.done();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't write the song pack to file", ex);
                }
            }
        }.start();
        
    }

    /**
     * Get the songs in this song pack.
     *
     * @return the songs in this song pack.
     */
    public List<SongDisplayable> getSongs() {
        return new ArrayList<>(songs);
    }
}
