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

import org.quelea.displayable.SongDisplayable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A song pack that contains a number of songs and can be written to a compressed archive.
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
     * @param song the song to add.
     */
    public void addSong(SongDisplayable song) {
        songs.add(song);
    }

    /**
     * Create a new song pack from a file.
     * @param file the file to create the song pack from.
     * @return the song pack that's been created
     * @throws IOException if something went wrong.
     */
    public static SongPack fromFile(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            SongPack ret = new SongPack();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ret.addSong(SongDisplayable.parseXML(zipFile.getInputStream(entry)));
            }
            return ret;
        }
    }

    /**
     * Write this song pack to a file.
     * @param file the file to write to.
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile(File file) {
        if (file == null) {
            return false;
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            int count = 0;
            for (SongDisplayable song : songs) {
                zos.putNextEntry(new ZipEntry("song" + count + ".xml"));
                zos.write(song.getXML().getBytes());
                zos.closeEntry();
                count++;
            }
            return true;
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't write the song pack to file", ex);
            return false;
        }
    }

    /**
     * Get the songs in this song pack.
     * @return the songs in this song pack.
     */
    public List<SongDisplayable> getSongs() {
        return new ArrayList<>(songs);
    }
}
