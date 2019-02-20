/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Chord {

    private int idx;
    private String chord;

    public Chord(int idx, String chord) {
        this.idx = idx;
        this.chord = chord;
    }

    public int getIdx() {
        return idx;
    }

    public String getChord() {
        return chord;
    }
    
    @Override
    public String toString() {
        return "Chord{" + "chord=" + chord + ", idx=" + idx + '}';
    }
    
    public static List<Chord> getChordsFromLine(String chordLine) {
        List<Chord> ret = new ArrayList<>();
        int cindex = -1;
        for (int i = 0; i < chordLine.length(); i++) {
            if (!Character.isWhitespace(chordLine.charAt(i))) {
                cindex = i;
                while (!Character.isWhitespace(chordLine.charAt(i))) {
                    i++;
                    if (i >= chordLine.length()) {
                        break;
                    }
                }
                ret.add(new Chord(cindex, chordLine.substring(cindex, i)));
            }
        }
        return ret;
    }

}
