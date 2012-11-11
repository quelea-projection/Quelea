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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transposes a chord down or up a certain number of semitones.
 *
 * @author Michael
 */
public class ChordTransposer {

    /**
     * Internal class containing the actual chord letter and the "tail"
     * (whatever comes after the chord letter.)
     */
    private static class ChordTail {

        private String chord;
        private String tail;

        /**
         * Create a new chord tail object.
         *
         * @param chord the chord.
         * @param tail the tail.
         */
        public ChordTail(String chord, String tail) {
            this.chord = chord;
            this.tail = tail;
        }
    }
    /**
     * The most natural keys in order, starting at A.
     */
    private static final List<String> TRANSPOSE_STEPS = new ArrayList<String>() {

        {
            addAll(Arrays.asList(new String[]{"A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab"}));
        }
    };
    /*
     * Sometimes we have E/G# like chords, the second part (after the forward
     * slash) is the chord2, tail2 variables.
     */
    private String chord;
    private String chord2;
    private String tail;
    private String tail2;

    /**
     * Create a new chord transposer.
     *
     * @param chord the chord to transpose.
     */
    public ChordTransposer(String chord) {
        if(chord == null) {
            return;
        }
        chord = chord.trim();
        if(chord.contains("/")) {
            String[] parts = chord.split("/");

            ChordTail ct = sanitise(parts[0]);
            this.chord = ct.chord;
            tail = ct.tail;

            ChordTail ct2 = sanitise(parts[1]);
            chord2 = ct2.chord;
            tail2 = ct2.tail;
        }
        else {
            ChordTail ct = sanitise(chord);
            this.chord = ct.chord;
            tail = ct.tail;
            chord2 = null;
            tail2 = null;
        }
    }

    /**
     * Put the correct capitalisation on the input and set the tail.
     *
     * @param chord the chord to sanitise
     * @return sanitised chord.
     */
    private ChordTail sanitise(String chord) {
        if(chord == null || chord.isEmpty()) {
            return new ChordTail("", "");
        }
        chord = Character.toUpperCase(chord.charAt(0)) + chord.substring(1);
        String localTail;
        if(chord.length() > 1) {
            if(Character.toLowerCase(chord.charAt(1)) == 'b' || chord.charAt(1) == '#') {
                localTail = chord.substring(2, chord.length());
                chord = chord.substring(0, 2);
            }
            else {
                localTail = chord.substring(1, chord.length());
                chord = chord.substring(0, 1);
            }
        }
        else {
            localTail = "";
        }
        String newChord;
        /*
         * "Naturalise" the chords - A# is more commonly represented as Bb for
         * instance.
         */
        switch(chord) {
            case "A#":
                newChord = "Bb";
                break;
            case "B#":
                newChord = "C";
                break;
            case "Cb":
                newChord = "B";
                break;
            case "Db":
                newChord = "C#";
                break;
            case "E#":
                newChord = "F";
                break;
            case "Fb":
                newChord = "E";
                break;
            case "Gb":
                newChord = "F#";
                break;
            case "G#":
                newChord = "Ab";
                break;
            default:
                newChord = chord;
        }
        return new ChordTail(newChord, localTail);
    }

    /**
     * Transpose the given chord by the given number of semitones.
     *
     * @param semitones the number of semitones to transpose by, positive or
     * negative.
     * @param newKey the new key to transpose to.
     * @return the new, transposed chord.
     */
    public String transpose(int semitones, String newKey) {
        if(chord == null || chord.isEmpty()) {
            return chord;
        }
        int index = TRANSPOSE_STEPS.indexOf(chord);
        index += semitones;
        index %= TRANSPOSE_STEPS.size();
        if(index < 0) {
            index = TRANSPOSE_STEPS.size() + index;
        }
        String transposedChord = TRANSPOSE_STEPS.get(index);
        if(newKey != null) {
            transposedChord = toSharpFlat(isSharpKey(newKey), transposedChord);
        }
        transposedChord += tail;
        boolean sharpKey = isSharpKey(transposedChord);
        if(chord2 != null) {
            index = TRANSPOSE_STEPS.indexOf(chord2);
            index += semitones;
            index %= TRANSPOSE_STEPS.size();
            if(index < 0) {
                index = TRANSPOSE_STEPS.size() + index;
            }
            String transposedChord2 = TRANSPOSE_STEPS.get(index) + tail2;
            transposedChord += "/" + toSharpFlat(sharpKey, transposedChord2);
        }
        return transposedChord;
    }

    /**
     * Convert a certain chord that's either a sharp or a flat into a sharp or a
     * flat chord (A# into Bb for instance.) Chords that are already in their
     * form proposed for conversion are returned unchanged.
     *
     * @param sharp true if this chord should be converted into a sharp chord,
     * false if it should be converted into a flat chord.
     * @param chord the chord to convert.
     * @return the converted chord.
     */
    private String toSharpFlat(boolean sharp, String chord) {
        if(sharp && isSharpKey(chord)) {
            return chord;
        }
        else if(!sharp && !isSharpKey(chord)) {
            return chord;
        }
        else if(sharp && !isSharpKey(chord)) {
            return toSharp(chord);
        }
        else if(!sharp && isSharpKey(chord)) {
            return toFlat(chord);
        }
        //Bug in program if we reach here.
        else {
            throw new AssertionError("Bug with " + sharp + " and " + chord);
        }
    }

    /**
     * Return the sharp version of a given "flat" chord.
     *
     * @param chord the flat chord.
     * @return the equivalent sharp chord.
     */
    private String toSharp(String chord) {
        switch(chord) {
            case "Db":
                return "C#";
            case "Eb":
                return "D#";
            case "Fb":
                return "E";
            case "Gb":
                return "F#";
            case "Ab":
                return "G#";
            case "Bb":
                return "A#";
            case "Cb":
                return "B";
            default:
                return chord;
        }
    }

    /**
     * Return the flat version of a given "sharp" chord.
     *
     * @param chord the sharp chord.
     * @return the equivalent flat chord.
     */
    private String toFlat(String chord) {
        switch(chord) {
            case "D#":
                return "Eb";
            case "E#":
                return "F";
            case "F#":
                return "Gb";
            case "G#":
                return "Ab";
            case "A#":
                return "Bb";
            case "B#":
                return "C";
            case "C#":
                return "Db";
            default:
                return chord;
        }
    }

    /**
     * Determine if the given key should use mainly sharps or flats. For
     * instance, A contains 5 sharps so is a sharp key, F contains 1 flat so is
     * a flat key. For keys like Am and C with no sharps or flats, at present
     * just return sharp.
     *
     * @param chord the chord to check.
     * @return true if the key is sharp, false if it's flat.
     */
    private boolean isSharpKey(String chord) {
        if(chord.contains("#")) {
            return true;
        }
        if(chord.contains("b")) {
            return false;
        }
        String[] sharpKeys = new String[]{"D", "E", "G", "A", "B", "C", "Em", "Bm", "Am"};
        for(String key : sharpKeys) {
            if(chord.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
