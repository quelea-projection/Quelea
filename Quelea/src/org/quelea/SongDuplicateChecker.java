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

/**
 * A class responsible for checking a new song against existing songs in the
 * database to see whether it is similar or the same.
 *
 * @author Michael
 */
public class SongDuplicateChecker {

    private int x;

    public static void main(String[] args) {
        new SongDuplicateChecker().checkSongs(null);
    }

    private int[] toIntArr(String str) {
        int[] ret = new int[str.length()];
        for(int i = 0; i < str.length(); i++) {
            ret[i] = str.charAt(i);
        }
        return ret;
    }

    public boolean[] checkSongs(Song[] newSongs) {
//        final Song[] songs = SongDatabase.get().getSongs();
//        final String[] songLyrics = new String[songs.length];
//        for (int i = 0; i < songLyrics.length; i++) {
//            songLyrics[i] = songs[i].getLyrics(false, false).replaceAll("[^\\p{L}]", "");
//        }

        final int d[] = new int[10000];
        final int result[] = new int[1];

        String s1 = "Hello";
        String s2 = "zello";
        final int[] s1Arr = toIntArr(s1);
        final int[] s2Arr = toIntArr(s2);
        final int s1Length = s1Arr.length;
        final int s2Length = s2Arr.length;

        Kernel kernel = new Kernel() {

            @Override
            public void run() {
                result[0] = ld(s1Arr, s2Arr, s1Length, s2Length);
            }

            public int ld(int[] s, int[] t, int sLength, int tLength) {
                int n; // length of s
                int m; // length of t
                int i; // iterates through s
                int j; // iterates through t
                int s_i; // ith character of s
                int t_j; // jth character of t
                int cost = 0; // cost
//
                n = sLength;
                m = tLength;
                if(n == 0) {
                    return m;
                }
                if(m == 0) {
                    return n;
                }
                int firstSize = n + 1;

                for(i = 0; i <= n; i++) {
                    d[firstSize * i + 0] = i;
                }

                for(j = 0; j <= m; j++) {
                    d[firstSize * 0 + j] = j;
                }

                for(i = 1; i <= n; i++) {
                    s_i = s[i - 1];
                    for(j = 1; j <= m; j++) {
                        t_j = t[j - 1];
                        cost = s_i == t_j ? 0 : 1;
                        int a = d[firstSize * (i - 1) + j] + 1;
                        int b = d[firstSize * i + (j - 1)] + 1;
                        int c = d[firstSize * (i - 1) + (j - 1)] + cost;

                        int mi = a;
                        if(b < mi) {
                            mi = b;
                        }
                        if(c < mi) {
                            mi = c;
                        }

                        d[firstSize * i + j] = mi;
                    }
                }
                return d[firstSize * n + m];
            }
        };
        kernel.execute(1);
        System.out.println(result[0]);
        return new boolean[0];
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
