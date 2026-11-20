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

import java.util.ArrayList;
import java.util.List;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.utils.Chord;

/**
 * Turns a section's lines with chord lines into lyric lines that each carry
 * their chords and where they fall, so apps can draw the chords above the
 * right letters in any font instead of lining up spaces.
 */
public final class ChordSheet {

    private ChordSheet() {
    }

    /**
     * A chord and the character it stands above.
     *
     * @param at the index of the character in the lyric line; it may be past
     * the end of the line when a chord trails it.
     * @param chord the chord, for example "F#m7".
     */
    public record ChordAt(int at, String chord) {
    }

    /**
     * A lyric line and its chords.
     *
     * @param text the lyric line; empty for a line of chords alone, such as
     * an intro.
     * @param chords the chords above it, in order; empty if none.
     */
    public record ChordedLine(String text, List<ChordAt> chords) {

        public ChordedLine {
            chords = List.copyOf(chords);
        }
    }

    /**
     * Pair each chord line with the lyric line under it.
     *
     * @param chordLines the section's lines with chord lines, or null.
     * @return the lyric lines with their chords, or null if there are no
     * chord lines.
     */
    public static List<ChordedLine> parse(List<String> chordLines) {
        if (chordLines == null) {
            return null;
        }
        List<ChordedLine> result = new ArrayList<>();
        List<ChordAt> pending = null;
        boolean anyChords = false;
        for (String line : chordLines) {
            if (line == null) {
                continue;
            }
            if (isChordLine(line)) {
                if (pending != null) {
                    // Two chord lines in a row: the first stands alone.
                    result.add(new ChordedLine("", pending));
                }
                pending = chords(line);
                anyChords = true;
            } else {
                result.add(new ChordedLine(line, pending == null ? List.of() : pending));
                pending = null;
            }
        }
        if (pending != null) {
            result.add(new ChordedLine("", pending));
        }
        return anyChords ? result : null;
    }

    private static boolean isChordLine(String line) {
        return !line.isBlank() && new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS;
    }

    private static List<ChordAt> chords(String line) {
        List<ChordAt> chords = new ArrayList<>();
        for (Chord chord : Chord.getChordsFromLine(line)) {
            chords.add(new ChordAt(chord.getIdx(), chord.getChord()));
        }
        return chords;
    }
}
