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

import java.util.regex.Pattern;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;

/**
 * A class responsible for checking a new song against existing songs in the
 * database to see whether it is similar or the same.
 *
 * @author Michael
 */
public class SongDuplicateChecker {

    private static final Pattern NON_LETTERS = Pattern.compile("[^\\p{L}]");
    private String[] databaseLyrics;

    public static void main(String[] args) {
        new SongDuplicateChecker().checkSongs(null);
    }

    public boolean[] checkSongs(SongDisplayable[] newSongs) {
        final SongDisplayable[] songs = SongManager.get().getSongs();
        final String[] songLyrics = new String[songs.length];
        for (int i = 0; i < songLyrics.length; i++) {
            songLyrics[i] = songs[i].getLyrics(false, false, false).replaceAll("[^\\p{L}]", "");
        }
        boolean[] sameArr = new boolean[newSongs.length];
        for(int i=0 ; i<newSongs.length ; i++) {
            //System.out.println(i + " of " + newSongs.length);
            SongDisplayable newSong = newSongs[i];
            String newLyrics = newSong.getLyrics(false, false, false).replaceAll("[^\\p{L}]", "");
            int distance = new AparapiLevenshteinDistance().leastCompare(newLyrics, songLyrics);
            if(distance<30) {
                sameArr[i] = true;
            }
            else {
                sameArr[i] = false;
            }
        }
        return sameArr;
    }

    /**
     * Checks whether the given new song is the same or similar to a song
     * already existing in the database. The database is only read the first
     * time this is called, so use one checker for all the songs of an import.
     * This method can be called from several threads at once.
     *
     * @param newSong the new song to check.
     * @return true if the song is the same or similar to an existing song,
     * false otherwise.
     */
    public boolean checkSong(SongDisplayable newSong) {
        String newLyrics = comparableLyrics(newSong);
        for(String lyrics : getDatabaseLyrics()) {
            if(isSimilar(lyrics, newLyrics)) {
                return true;
            }
        }
        return false;
    }

    private synchronized String[] getDatabaseLyrics() {
        if(databaseLyrics == null) {
            SongDisplayable[] songs = SongManager.get().getSongs();
            databaseLyrics = new String[songs.length];
            for(int i = 0; i < songs.length; i++) {
                databaseLyrics[i] = comparableLyrics(songs[i]);
            }
        }
        return databaseLyrics;
    }

    private static String comparableLyrics(SongDisplayable song) {
        return NON_LETTERS.matcher(song.getLyrics(false, false, false)).replaceAll("");
    }

    /**
     * Determine whether two lyrics are similar, meaning that the edit distance
     * between them is at most a tenth of the length of the shorter one. The edit
     * distance is at least the difference in length, and calculating it stops as
     * soon as it passes the limit, so most pairs are ruled out cheaply.
     *
     * @param lyrics1 the first lyrics.
     * @param lyrics2 the second lyrics.
     * @return true if they're similar, false otherwise.
     */
    static boolean isSimilar(String lyrics1, String lyrics2) {
        int maxDistance = Math.min(lyrics1.length(), lyrics2.length()) / 10;
        if(Math.abs(lyrics1.length() - lyrics2.length()) > maxDistance) {
            return false;
        }
        return new LevenshteinDistance(maxDistance).apply(lyrics1, lyrics2) != -1;
    }
}
