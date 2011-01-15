package org.quelea.utils;

import java.awt.*;

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
        if(checkTitle()) {
            return Type.TITLE;
        }
        else if(checkChords()) {
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
        if(line.isEmpty()) {
            return false;
        }
        String checkLine = line.replace('-', ' ');
        checkLine = checkLine.replace('(', ' ');
        checkLine = checkLine.replace(')', ' ');
        checkLine = checkLine.replaceAll("[xX][0-9]+", "");
        checkLine = checkLine.replaceAll("[0-9]+[xX]", "");
        for(String s : checkLine.split("\\s")) {
            if(s.trim().isEmpty()) {
                continue;
            }
            if(!s.matches("([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?[0-9]*){3}(#|b)?[0-9]*)(/([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?[0-9]*){3}(#|b)?[0-9]*))?")) {
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
        String processedLine = line.toLowerCase().trim().replace("(", "").replace(")", "");
        return processedLine.toLowerCase().startsWith("verse")
                || processedLine.toLowerCase().startsWith("chorus")
                || processedLine.toLowerCase().startsWith("tag")
                || processedLine.toLowerCase().startsWith("pre-chorus")
                || processedLine.toLowerCase().startsWith("pre chorus")
                || processedLine.toLowerCase().startsWith("coda")
                || processedLine.toLowerCase().startsWith("ending")
                || processedLine.toLowerCase().startsWith("bridge")
                || processedLine.toLowerCase().startsWith("intro")
                || processedLine.toLowerCase().startsWith("outro");
    }
}
