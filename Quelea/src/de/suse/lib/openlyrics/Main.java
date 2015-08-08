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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import utils.OpenLyricsWriter;


/**
 * CLI app utility.
 * 
 * @author bo
 */
public class Main {
    public static String fill(int len) {
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < len; i++) {
            space.append(" ");
        }

        return space.toString();
    }

    public static void main(String[] args) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String xline;
            StringBuilder xmlsource = new StringBuilder();
            while ((xline = br.readLine()) != null) {
                xmlsource.append(xline.trim());
            }
            in.close();

            OpenLyricsObject ol = new OpenLyricsObject(xmlsource.toString());

            new OpenLyricsWriter(ol).writeToFile(new File("/tmp/foo.xml"), true);
            System.exit(1);

            System.err.println("Lyrics for " + ol.getProperties().getTitleProperty().getDefaultTitle());
            System.err.println("Copyright (C) by " + ol.getProperties().getCopyright());
            System.err.println("Key: " + ol.getProperties().getKey());
            System.err.println("");

            for (int i = 0; i < ol.getVerses().size(); i++) {
                Verse verse = ol.getVerses().get(i);
                System.err.println("    Verse " + verse.getName());
                for (int j = 0; j < verse.getLines().size(); j++) {
                    VerseLine line = verse.getLines().get(j);

                    // Write chords line above text line
                    StringBuilder renderedChords = new StringBuilder();
                    int lastOffset = 0;
                    for (int k = 0; k < line.getChords().size(); k++) {
                        Chord chord = line.getChords().get(k);
                        renderedChords.append(Main.fill(chord.getLineOffset() - lastOffset));
                        renderedChords.append(chord.getRoot())
                                      .append(chord.isFlat() ? "b" : (chord.isSharp() ? "#" : ""))
                                      .append(chord.isMinor() ? "m" : "")
                                      .append(chord.getInterval() > 0 ? chord.getInterval() : "")
                                      .append(chord.getQuality() == null ? "" : chord.getQuality());
                        lastOffset = chord.getLineOffset(); // Also remove all chords space too, but that's cosmetics.
                    }

                    System.err.println("        " + renderedChords.toString());
                    System.err.println("        " + line.getText());
                }
                System.err.println("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}
