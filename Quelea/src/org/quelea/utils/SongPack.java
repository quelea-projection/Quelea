package org.quelea.utils;

import org.quelea.displayable.Song;

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
    private final List<Song> songs;

    /**
     * Create a new song pack.
     */
    public SongPack() {
        songs = new ArrayList<Song>();
    }

    /**
     * Add a song to this pack.
     * @param song the song to add.
     */
    public void addSong(Song song) {
        songs.add(song);
    }

    /**
     * Create a new song pack from a file.
     * @param file the file to create the song pack from.
     * @return the song pack that's been created
     * @throws IOException if something went wrong.
     */
    public static SongPack fromFile(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        try {
            SongPack ret = new SongPack();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ret.addSong(Song.parseXML(zipFile.getInputStream(entry)));
            }
            return ret;
        }
        finally {
            zipFile.close();
        }
    }

    /**
     * Write this song pack to a file.
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile(File file) {
        if(file == null) {
            return false;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            try {
                int count = 0;
                for(Song song : songs) {
                    zos.putNextEntry(new ZipEntry("song" + count + ".xml"));
                    zos.write(song.getXML().getBytes());
                    zos.closeEntry();
                    count++;
                }
                return true;
            }
            finally {
                zos.close();
            }
        }
        catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't write the song pack to file", ex);
            return false;
        }
    }

    /**
     * Get the songs in this song pack.
     * @return the songs in this song pack.
     */
    public List<Song> getSongs() {
        return new ArrayList<Song>(songs);
    }
}
