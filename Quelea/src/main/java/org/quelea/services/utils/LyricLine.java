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

/**
 * A line of lyrics - wrapper class for the line itself, and whether it's a
 * translated line or not.
 *
 * @author Michael
 */
public class LyricLine {

    private final boolean isTranslateLine;
    private final String line;
    private final boolean separator;
    private final int translationIndex;

    /**
     * Create a new lyric line.
     * @param isTranslateLine true if the line is translated, false otherwise.
     * @param line 
     */
    public LyricLine(boolean isTranslateLine, String line) {
        this(isTranslateLine, line, false, 0);
    }

    /**
     * Create a new line of one of several translations shown at the same
     * time (such as bible versions.)
     * @param line the text of the line.
     * @param translationIndex which translation: 0 for the first.
     */
    public LyricLine(String line, int translationIndex) {
        this(true, line, false, translationIndex);
    }

    private LyricLine(boolean isTranslateLine, String line, boolean separator, int translationIndex) {
        this.isTranslateLine = isTranslateLine;
        this.line = line;
        this.separator = separator;
        this.translationIndex = Math.max(0, translationIndex);
    }

    /**
     * Create the gap between two bible versions on one slide, which is drawn
     * as a line where it can be (and as an empty line elsewhere.)
     * @param translationIndex the translation below the gap: 0 for the
     * first.
     * @return the separator.
     */
    public static LyricLine separator(int translationIndex) {
        return new LyricLine(false, "", true, translationIndex);
    }

    /**
     * Create a new lyric line that's not translated.
     * @param line the text for the non-translated line.
     */
    public LyricLine(String line) {
        this(false, line, false, 0);
    }

    /**
     * Get which translation this line belongs to, when several are shown at
     * the same time.
     * @return 0 for the first translation (or a line that isn't translated.)
     */
    public int getTranslationIndex() {
        return translationIndex;
    }

    /**
     * Determine if this is the gap between two bible versions.
     * @return true if it is, false if it's a line of text.
     */
    public boolean isSeparator() {
        return separator;
    }

    /**
     * Determine if thi line is translated.
     * @return true if the line is translated, false otherwise.
     */
    public boolean isTranslateLine() {
        return isTranslateLine;
    }

    /**
     * Get the line's text as a string.
     * @return the line's text as a string.
     */
    public String getLine() {
        return line;
    }

}
