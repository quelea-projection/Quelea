package org.quelea.chord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transposes a chord down or up a certain number of semitones.
 * @author Michael
 */
public class ChordTransposer {

    private static final List<String> TRANSPOSE_STEPS = new ArrayList<String>() {

        {
            addAll(Arrays.asList(new String[]{"A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab"}));
        }
    };
    private String chord;
    private String tail;

    /**
     * Create a new chord transposer.
     * @param chord the chord to transpose.
     */
    public ChordTransposer(String chord) {
        chord = chord.trim();
        this.chord = sanitise(chord);
    }

    /**
     * Put the correct capitalisation on the input and set the tail.
     * @param chord the chord to sanitise
     * @return sanitised chord.
     */
    private String sanitise(String chord) {
        if (chord.isEmpty()) {
            tail = "";
            return "";
        }
        chord = Character.toUpperCase(chord.charAt(0)) + chord.substring(1);
        if (chord.length() > 1) {
            if (Character.toLowerCase(chord.charAt(1)) == 'b' || chord.charAt(1) == '#') {
                tail = chord.substring(2, chord.length());
                chord = chord.substring(0, 2);
            }
            else {
                tail = chord.substring(1, chord.length());
                chord = chord.substring(0, 1);
            }
        }
        else {
            tail = "";
        }
        String newChord;
        switch (chord) {
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
        return newChord;
    }

    /**
     * Transpose the given chord by the given number of semitones.
     * @param semitones the number of semitones to transpose by, positive or
     * negative.
     * @return the new, transposed chord.
     */
    public String transpose(int semitones) {
        if(chord.isEmpty()) {
            return "";
        }
        int index = TRANSPOSE_STEPS.indexOf(chord);
        index += semitones;
        index %= TRANSPOSE_STEPS.size();
        String transposedChord = TRANSPOSE_STEPS.get(index);
        return transposedChord + tail;
    }
}
