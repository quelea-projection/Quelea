package org.quelea.windows.newsong;

import java.awt.*;

/**
 * Add some information about where something should be highlighted.
 * @author Michael
 */
public class HighlightIndex {

    private final int startIndex;
    private final int endIndex;
    private final Color highlightColor;

    /**
     * Create a new highlight index.
     * @param startIndex     the index where highlighting should start.
     * @param endIndex       the index where highlighting should end.
     * @param highlightColor the colour of the highlight.
     */
    public HighlightIndex(int startIndex, int endIndex, Color highlightColor) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.highlightColor = highlightColor;
    }

    /**
     * Get the start index of the highlight (inclusive.)
     * @return the start index of the highlight.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Get the end index of the highlight (exclusive.)
     * @return the end index of the highlight.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Get the highlight colour.
     * @return the highlight colour.
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

}
