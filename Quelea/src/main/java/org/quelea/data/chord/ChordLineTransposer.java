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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transposes a line of chords down or up a certain amount of semitones.
 * @author Michael
 */
public class ChordLineTransposer {

    // Keep the same parentheses, hyphens and repeat markers accepted by LineTypeChecker.
    private static final Pattern CHORD_OR_REPEAT = Pattern.compile(
            "(?<repeat>(?i:[0-9]+x|x[0-9]+))|(?:(?![xX][0-9]|[0-9]+[xX])[^\\s()\\-])+");
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
        Matcher matcher = CHORD_OR_REPEAT.matcher(chordLine);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group();
            String replacement = matcher.group("repeat") != null
                    ? token : new ChordTransposer(token).transpose(semitones, newKey);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.append(marker).toString();
    }

}
