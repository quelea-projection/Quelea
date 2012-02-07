/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea;

import com.amd.aparapi.Kernel;
import org.apache.commons.lang.StringUtils;
import org.quelea.displayable.Song;
import org.quelea.utils.opencl.LevenshteinDistance;

/**
 * A class responsible for checking a new song against existing songs in the
 * database to see whether it is similar or the same.
 *
 * @author Michael
 */
public class SongDuplicateChecker {

    public static void main(String[] args) {
        new SongDuplicateChecker().checkSongs(null);
    }

    public boolean[] checkSongs(Song[] newSongs) {
        final Song[] songs = SongDatabase.get().getSongs();
        final String[] songLyrics = new String[songs.length];
        for (int i = 0; i < songLyrics.length; i++) {
            songLyrics[i] = songs[i].getLyrics(false, false).replaceAll("[^\\p{L}]", "");
        }
        boolean[] sameArr = new boolean[newSongs.length];
        for(int i=0 ; i<newSongs.length ; i++) {
            System.out.println(i + " of " + newSongs.length);
            Song newSong = newSongs[i];
            String newLyrics = newSong.getLyrics(false, false).replaceAll("[^\\p{L}]", "");
            int distance = new LevenshteinDistance().leastCompare(newLyrics, songLyrics);
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
     * already existing in the database.
     *
     * @param newSong the new song to check.
     * @return true if the song is the same or similar to an existing song,
     * false otherwise.
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
