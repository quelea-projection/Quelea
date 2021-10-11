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
package org.quelea.services.utils;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * Checks the type of the line.
 *
 * @author Michael
 */
public class LineTypeChecker {
    
    public static final String CHORD_REGEX = "(\\s*(((([a-hA-H](#|b|♯|♭?)[0-9]*)|\\/)*((sus|dim|º|ø|\\+|maj|dom|min|m|M|aug|add)?[0-9]*){3}(#|b|♯|♭)?[0-9]*)\\s*)+)";

    /**
     * The type of the line.
     */
    public enum Type {

        NORMAL, TITLE, CHORDS, NONBREAK

    }

    private final String line;

    /**
     * Create a new line type checker to check a particular line.
     *
     * @param line the line to check.
     */
    public LineTypeChecker(String line) {
        this.line = line;
    }
    
    public static void main(String[] args) {
        System.out.println(new LineTypeChecker("his face is").getLineType());
    }

    /**
     * Get the line type.
     *
     * @return the type of the line.
     */
    public Type getLineType() {
        if(line==null) {
            return null;
        }
        if (checkTitle()) {
            return Type.TITLE;
        } else if (checkChords()) {
            return Type.CHORDS;
        } else if (checkNonBreak()) {
            return Type.NONBREAK;
        } else {
            return Type.NORMAL;
        }
    }

    private boolean checkNonBreak() {
        return line.trim().equals("<>")
                || line.trim().equals("\\u00A0")
                || (line.length() == 1 && line.charAt(0) == 160);
    }

    /**
     * Check whether this line is a line containing only chords.
     *
     * @return true if it's a chord line, false otherwise.
     */
    private boolean checkChords() {
        if (line.trim().isEmpty()) {
            return false;
        }
        if (line.toLowerCase().endsWith("//chords")) {
            return true;
        }
        if (line.toLowerCase().endsWith("//lyrics")) {
            return false;
        }
        String checkLine = line.replace('-', ' ');
        checkLine = checkLine.replace('(', ' ');
        checkLine = checkLine.replace(')', ' ');
        checkLine = checkLine.replaceAll("[xX][0-9]+", "");
        checkLine = checkLine.replaceAll("[0-9]+[xX]", "");
        for (String s : checkLine.split("\\s")) {
            if (s.trim().isEmpty()) {
                continue;
            }
            if (!s.matches("^" + CHORD_REGEX + "$")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether this line is the title of a section.
     *
     * @return true if it's the title of a section, false otherwise.
     */
    private boolean checkTitle() {
        String processedLine = line.toLowerCase().trim()
                .replace("{", "").replace("}", "")
                .replace("[", "").replace("]", "")
                .replace("<", "").replace(">", "")
                .replace("(", "").replace(")", "");
        if (processedLine.endsWith("//title")) {
            return true;
        }
        return processedLine.toLowerCase().startsWith("verse")
                || processedLine.toLowerCase().startsWith("chorus")
                || processedLine.toLowerCase().startsWith("tag")
                || processedLine.toLowerCase().startsWith("pre-chorus")
                || processedLine.toLowerCase().startsWith("pre chorus")
                || processedLine.toLowerCase().startsWith("coda")
                || processedLine.toLowerCase().startsWith("bridge")
                || processedLine.toLowerCase().startsWith("intro")
                || processedLine.toLowerCase().startsWith("outro")
                || processedLine.toLowerCase().startsWith("interlude")
                || processedLine.toLowerCase().startsWith("ending");
    }

    private static final TreeMap<String, String> titleMap = new TreeMap<>();
    private static final TreeMap<String, String> titleMapRev = new TreeMap<>((o1, o2) -> Integer.compare(o2.length(), o1.length()));

    private static int hashLength = 3;

    private static String nextHash() {
        StringBuilder ret = new StringBuilder(hashLength);
        ret.append("#".repeat(hashLength));
        hashLength++;
        return ret.toString();
    }

    public synchronized static String[] encodeSpecials(String[] toEncode) {
        titleMap.clear();
        titleMapRev.clear();
        hashLength = 3;
        String[] ret = new String[toEncode.length];
        for (int i = 0; i < ret.length; i++) {
            String line = toEncode[i];
            if (new LineTypeChecker(line).getLineType() != Type.NORMAL) {
                if (!titleMap.containsKey(line)) {
                    String hash = nextHash();
                    titleMap.put(line, hash);
                    titleMapRev.put(hash, line);
                }
                line = titleMap.get(line);
            }
            ret[i] = line;
        }
        return ret;
    }

    public synchronized static String[] decodeSpecials(String[] toDecode) {
        for (String key : titleMapRev.keySet()) {
            for (int i = 0; i < toDecode.length; i++) {
                String line = toDecode[i];
                if (line.trim().equalsIgnoreCase(key.trim())) {
                    toDecode[i] = titleMapRev.get(key);
                }
            }
        }
        return toDecode;
    }
}
