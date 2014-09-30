/*
 * Author: Bo Maryniuk <bo@suse.de>
 *
 * Copyright (c) 2013 Bo Maryniuk. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *     3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY BO MARYNIUK "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.suse.lib.openlyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author bo
 */
public class Chord {

    /**
     * Exception for the chord.
     */
    public static class ChordException extends OpenLyricsException {
        public ChordException(String message) {
            super(message);
        }
    }


    // Chord description
    private String root;             // Base symbol of the chord
    private String alteration;       // # or b
    private String quality;          // maj, min, sus etc.
    private int interval;            // 7, 9, 13 etc.
    private int add;                 // Added tone chord.
    private boolean isMinor = false; // Is minor.
    private String bass;             // Bass line, if any. Optional.

    // OpenLyricsProperties and flags
    private int offset = 0;    // Positioning symbol offset from the beginning of the string line.

    // C C+ C4 C6 C7 C9 C9(11) C11
    // Csus Csus2 Csus4 Csus9 Cmaj Cmaj7 Cm
    // Cdim C/B Cadd2/B CaddD C(addD) Cadd9 C(add9)
    // Cm7 Cm11
    /**
     * Default OpenLyrics chord.
     * Any of these: http://openlyrics.info/chordlist.html#chordlist
     */
    public Chord(String chord) throws ChordException {
        this.parse(chord);
    }

    /**
     * Get root of the chord.
     *
     * @return
     */
    public String getRoot() {
        // Circular shift
        return this.root;
    }

    /**
     * Alteration is Bemolle.
     *
     * @return
     */
    public boolean isFlat() {
        return this.alteration != null ? this.alteration.equals("b") : false;
    }

    /**
     * Alteration is Diesis.
     *
     * @return
     */
    public boolean isSharp() {
        return this.alteration != null ? this.alteration.equals("#") : false;
    }

    /**
     * Return True if chord is Minor.
     *
     * @return
     */
    public boolean isMinor() {
        return this.isMinor;
    }

    /**
     * Get quality of the chord.
     *
     * @return
     */
    public String getQuality() {
        return this.quality;
    }

    /**
     * Get chord interval.
     *
     * @return
     */
    public int getInterval() {
        return this.interval;
    }

    /**
     * Get additional construction
     * @return
     */
    public int getAdditional() {
        return this.add;
    }

    /**
     * Get bass line, if any.
     *
     * @return
     */
    public String getBass() {
        return this.bass;
    }

    /**
     * Set offset in the text line it belongs to.
     *
     * @param offset
     */
    public void setLineOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Offset in the text line it belongs to.
     *
     * @return
     */
    public int getLineOffset() {
        return this.offset;
    }

    /**
     * Parse chord.
     *
     * @param chord
     */
    private void parse(String chord) throws ChordException {
        if (chord == null) {
            chord = "";
        }

        chord = chord.replaceAll(" ", "").trim();

        if (chord.equals("")) {
            throw new ChordException("Chord cannot be empty.");
        }

        // Alter notation
        chord = chord.replace("+", "aug").replace("-", "dim");

        // Get root
        this.root = chord.charAt(0) + "";
        if (chord.length() == 1) {
            return;
        }

        // Get interval
        try {
            this.interval = Integer.parseInt(chord.split("/")[0].split("add")[0].replaceAll("[^\\d]", ""));
        } catch (Exception ex) {
            // Just no interval.
        }

        // Get alteration, if any.
        if (chord.charAt(1) == '#' || chord.charAt(1) == 'b') {
            this.alteration = chord.charAt(1) + "";
            if (chord.length() == 2) {
                return;
            }
        }
        chord = chord.substring(0, 1) + " " + chord.substring(1);

        // Get quality
        String[] qualities = new String[]{"maj", "sus", "dim", "aug"};
        for (String q : qualities) {
            if (chord.substring(2).startsWith(q)) {
                this.quality = q;
                break;
            }
        }

        // Find if it is minor or major
        this.isMinor = this.quality != null && chord.replace(this.quality, "").contains("m") || (this.quality == null && chord.contains("m"));


        //this.add;           // Added tone chord.
        if (chord.substring(1).toLowerCase().contains("add")) {
            try {
                this.add = Integer.parseInt(chord.toLowerCase().split("add")[1].split("/")[0]);
            } catch (Exception ex) {
                // Just skip this, they use crazy notation with letters,
                // instead of number for chort types. :-/
            }
        }

        // Get bass
        if (chord.contains("/")) {
            this.bass = chord.split("/")[1];
        }
    }

    /**
     * Transpose chord by N of half-tones.
     *
     * @param transposition
     * @return
     */
    public Chord transpose(int transposition) {
        transposition = transposition - (transposition * 2);

        List<String> dscale = new ArrayList<String>();
        dscale.add("C0");
        dscale.add("C1");
        dscale.add("D0");
        dscale.add("D1");
        dscale.add("E0");
        dscale.add("F0");
        dscale.add("F1");
        dscale.add("G0");
        dscale.add("G1");
        dscale.add("A0");
        dscale.add("A1");
        dscale.add("H0");

        List<String> bscale = new ArrayList<String>();
        bscale.add("C0");
        bscale.add("D1");
        bscale.add("D0");
        bscale.add("E1");
        bscale.add("E0");
        bscale.add("F0");
        bscale.add("G1");
        bscale.add("G0");
        bscale.add("A1");
        bscale.add("A0");
        bscale.add("H1");
        bscale.add("H0");

        List<String> scale = this.isFlat() ? bscale : dscale;
        int idx = scale.indexOf(this.root.toUpperCase() + (this.isFlat() || this.isSharp() ? "1" : "0"));
        Collections.rotate(scale, transposition);

        this.root = scale.get(idx).substring(0, 1);
        this.alteration = scale.get(idx).substring(1).equals("1") ? (this.isFlat() ? "b" : "#") : null;

        return this;
    }
}
