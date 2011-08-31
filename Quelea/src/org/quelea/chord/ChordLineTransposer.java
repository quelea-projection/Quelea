package org.quelea.chord;

/**
 * Transposes a line of chords down or up a certain amount of semitones.
 * @author Michael
 */
public class ChordLineTransposer {

    private String line;

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
     * @return the transposed line.
     */
    public String transpose(int semitones, String newKey) {
        boolean startSpace = line.startsWith(" ");
        boolean chordsComment = line.endsWith("//chords");
        if(!startSpace) {
            line = " " + line;
        }
        if(chordsComment) {
            line = line.substring(0,line.indexOf("//chords"));
        }
        String[] chords = line.split("\\s+");
        String[] whitespace = line.split("[A-Za-z0-9#/]+");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < chords.length; i++) {
            ret.append(new ChordTransposer(chords[i]).transpose(semitones, newKey));
            if (i < whitespace.length) {
                ret.append(whitespace[i]);
            }
        }
        if(!startSpace) {
            line = line.substring(1);
        }
        String str = ret.toString();
        if(!startSpace) {
            str = str.substring(1);
        }
        if(chordsComment) {
            line = line + "//chords";
        }
        return str;
    }

}
