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

    /**
     * Create a new lyric line.
     * @param isTranslateLine true if the line is translated, false otherwise.
     * @param line 
     */
    public LyricLine(boolean isTranslateLine, String line) {
        this.isTranslateLine = isTranslateLine;
        this.line = line;
    }

    /**
     * Create a new lyric line that's not translated.
     * @param line the text for the non-translated line.
     */
    public LyricLine(String line) {
        this.isTranslateLine = false;
        this.line = line;
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
