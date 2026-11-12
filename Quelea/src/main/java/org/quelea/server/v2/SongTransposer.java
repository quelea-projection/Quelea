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
package org.quelea.server.v2;

import org.quelea.data.chord.ChordLineTransposer;
import org.quelea.data.chord.ChordTransposer;
import org.quelea.services.utils.LineTypeChecker;

/**
 * Transposes a song's chords without the song editor, the same way the
 * editor's transpose dialog does: the key is moved, and chord lines are
 * spelled for the new key.
 */
final class SongTransposer {

    private SongTransposer() {
    }

    /**
     * @param key the song's key, may be null or empty.
     * @param semitones how far to move it.
     * @return the new key, or the given value if there is none.
     */
    static String transposeKey(String key, int semitones) {
        if (key == null || key.isBlank()) {
            return key;
        }
        return new ChordTransposer(key.strip()).transpose(semitones, null);
    }

    /**
     * Transpose the chord lines of a song's lyrics; other lines stay as
     * they are.
     *
     * @param lyrics the lyrics with chords, as the song stores them.
     * @param newKey the song's key after transposing, or null to take it
     * from the first chord.
     * @param semitones how far to move the chords.
     * @return the transposed lyrics.
     */
    static String transposeLyrics(String lyrics, String newKey, int semitones) {
        String key = newKey == null || newKey.isBlank() ? keyFromFirstChord(lyrics, semitones) : newKey;
        StringBuilder result = new StringBuilder(lyrics.length());
        String[] lines = lyrics.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                result.append(new ChordLineTransposer(line).transpose(semitones, key));
            } else {
                result.append(line);
            }
            if (i < lines.length - 1) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    /**
     * Guess the new key from the first chord, so sharps or flats are chosen
     * well when the song has no key. The same rule as the song editor.
     */
    private static String keyFromFirstChord(String lyrics, int semitones) {
        for (String line : lyrics.split("\n")) {
            if (new LineTypeChecker(line).getLineType() != LineTypeChecker.Type.CHORDS) {
                continue;
            }
            String first = null;
            for (String token : line.strip().split("\\s+")) {
                if (!token.isEmpty()) {
                    first = token;
                    break;
                }
            }
            if (first == null) {
                return null;
            }
            String key = new ChordTransposer(first).transpose(semitones, null);
            if (key.length() > 2) {
                key = key.substring(0, 2);
            }
            if (key.length() == 2) {
                if (key.charAt(1) == 'B') {
                    key = key.charAt(0) + "b";
                } else if (key.charAt(1) != 'b' && key.charAt(1) != '#') {
                    key = Character.toString(key.charAt(0));
                }
            }
            return key;
        }
        return null;
    }
}
