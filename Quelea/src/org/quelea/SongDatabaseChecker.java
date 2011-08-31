package org.quelea;

import org.apache.commons.lang.StringUtils;
import org.quelea.displayable.Song;

/**
 * A class responsible for checking a new song against existing songs in the database to see whether it is similar or
 * the same.
 * @author Michael
 */
public class SongDatabaseChecker {

    /**
     * Checks whether the given new song is the same or similar to a song already existing in the database.
     * @param newSong the new song to check.
     * @return true if the song is the same or similar to an existing song, false otherwise.
     */
    public boolean checkSong(Song newSong) {
        for(Song databaseSong : SongDatabase.get().getSongs()) {
            String databaseLyrics = databaseSong.getLyrics(false, false).replaceAll("[^\\p{L}]", "");
            String newLyrics = newSong.getLyrics(false, false).replaceAll("[^\\p{L}]", "");
            int maxDistance;
            if(newLyrics.length() < databaseLyrics.length()) {
                maxDistance = newLyrics.length() / 10;
            }
            else {
                maxDistance = databaseLyrics.length() / 10;
            }
            if(StringUtils.getLevenshteinDistance(databaseLyrics, newLyrics) <= maxDistance) {
                return true;
            }
        }
        return false;
    }
}
