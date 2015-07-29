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
package org.quelea.windows.newsong;

import java.awt.Color;

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
