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
package org.quelea.data.chord;

/**
 * Transposes a line of chords down or up a certain amount of semitones.
 * @author Michael
 */
public class ChordLineTransposer {

    private final String line;

    /**
     * Create a new chord line transposer.
     * @param line the line to transpose.
     */
    public ChordLineTransposer(String line) {
        this.line = line;
    }

    /**
     * Transpose the line by the given number of semitones.
     * @param semitones the number of semitones to transpose by, positive or
     * negative.
     * @param newKey the new key to transpose to. This can be null if not known
     * but if it is known it means we can properly transpose chords (otherwise 
     * we can end up with things like E/Ab rather than E/G#.
     * @return the transposed line.
     */
    public String transpose(int semitones, String newKey) {
        String chordLine = line;
        String marker = "";
        int markerStart = chordLine.length() - "//chords".length();
        if (markerStart >= 0 && chordLine.regionMatches(true, markerStart, "//chords", 0, "//chords".length())) {
            marker = chordLine.substring(markerStart);
            chordLine = chordLine.substring(0, markerStart);
        }
        if (chordLine.isBlank()) {
            return chordLine + marker;
        }
        boolean startSpace = chordLine.startsWith(" ");
        if(!startSpace) {
            chordLine = " " + chordLine;
        }
        String[] chords = chordLine.split("\\s+");
        String[] whitespace = chordLine.split("[A-Za-z0-9#/]+");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < chords.length; i++) {
            ret.append(new ChordTransposer(chords[i]).transpose(semitones, newKey));
            if (i < whitespace.length) {
                ret.append(whitespace[i]);
            }
        }
        String str = ret.toString();
        if(!startSpace) {
            str = str.substring(1);
        }
        return str + marker;
    }

}
