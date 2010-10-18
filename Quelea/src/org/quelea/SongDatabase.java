package org.quelea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.quelea.display.Song;

/**
 * The class that controls the database that stores all the song data in flat
 * file format on the disk. At present this is just a zip file containing XML
 * files of all the songs.
 * @author Michael
 */
public class SongDatabase {

    private File database;

    /**
     * Initialise a song database at a specified file location.
     * @param databaseLocation the location of the database.
     * @throws IOException if there is a problem with the database.
     */
    public SongDatabase(String databaseLocation) throws IOException {
        database = new File(databaseLocation);
    }

    /**
     * Get all the songs from the database and return them as an array of
     * songs.
     * @return all the songs in the database.
     */
    public Song[] getSongs() {
        try {
            ZipFile zipFile = new ZipFile(database);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            List<Song> songs = new ArrayList<Song>();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try {
                    songs.add(Song.parseXML(zipFile.getInputStream(entry)));
                }
                catch(IOException ex) {
                    System.err.println("Failed to get " + entry.getName() + " from database.");
                }
            }
            zipFile.close();
            return songs.toArray(new Song[songs.size()]);
        }
        catch(IOException ex) {
            return null;
        }
    }

    /**
     * Add the specified song to the database.
     * @param song the song to add.
     */
    public boolean addSong(Song song) {
        try {
            File songFile = File.createTempFile("queleasong", "tmp");
            FileInputStream inputStream = new FileInputStream(songFile);
            PrintWriter writer = new PrintWriter(songFile);
            writer.println(song.getXML());
            writer.close();
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(database));
            zipStream.putNextEntry(new ZipEntry(song.getTitle() + "," + song.getAuthor() + "," + song.hashCode() + ".xml"));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1) {
                zipStream.write(buffer, 0, bytesRead);
            }
            zipStream.closeEntry();
            zipStream.close();
            return true;
        }
        catch(IOException ex) {
            return false;
        }
    }
}
