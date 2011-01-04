package org.quelea.utils;

import java.awt.Color;

/**
 * Checks the type of the line.
 * @author Michael
 */
public class LineTypeChecker {

    /**
     * The type of the line.
     */
    public enum Type {

        NORMAL(null), TITLE(Color.YELLOW), CHORDS(Color.RED);
        private final Color color;

        private Type(Color color) {
            this.color = color;
        }

        /**
         * Get the highlight colour to use for this type.
         */
        public Color getHighlightColor() {
            return color;
        }
    }
    private final String line;

    /**
     * Create a new line type checker to check a particular line.
     * @param line the line to check.
     */
    public LineTypeChecker(String line) {
        this.line = line;
    }

    /**
     * Get the line type.
     * @return the type of the line.
     */
    public Type getLineType() {
        if (checkTitle()) {
            return Type.TITLE;
        }
        else if (checkChords()) {
            return Type.CHORDS;
        }
        else {
            return Type.NORMAL;
        }
    }

    /**
     * Check whether this line is a line containing only chords.
     * @return true if it's a chord line, false otherwise.
     */
    private boolean checkChords() {
        if (line.isEmpty()) {
            return false;
        }
        for (String s : line.split("\\s")) {
            if (s.trim().isEmpty()) {
                continue;
            }
            if (!s.matches("([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?){2}(#|b)?[0-9]*)(/([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?){2}(#|b)?[0-9]*))?")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether this line is the title of a section.
     * @return true if it's the title of a section, false otherwise.
     */
    private boolean checkTitle() {
        return line.toLowerCase().startsWith("verse")
                || line.toLowerCase().startsWith("chorus")
                || line.toLowerCase().startsWith("tag")
                || line.toLowerCase().startsWith("pre-chorus")
                || line.toLowerCase().startsWith("pre chorus")
                || line.toLowerCase().startsWith("coda")
                || line.toLowerCase().startsWith("ending")
                || line.toLowerCase().startsWith("bridge");
    }
}
