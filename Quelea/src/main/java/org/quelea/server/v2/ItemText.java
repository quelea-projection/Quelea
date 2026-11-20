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
package org.quelea.server.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.utils.LineTypeChecker;

/**
 * Turns Quelea's display text into plain text for API clients. Bible
 * passages carry HTML for verse numbers ({@code <sup>16</sup>}), and some
 * titles span two lines (passage, then translation).
 */
final class ItemText {

    private static final Pattern VERSE_NUMBER = Pattern.compile("<sup>\\s*([^<]*?)\\s*</sup>\\s*");
    private static final Pattern TAG = Pattern.compile("<[^>]*>");
    private static final Pattern SPACES = Pattern.compile("[ \\t\\x0B\\f\\r]{2,}");

    private ItemText() {
    }

    /**
     * @param d the displayable, may be null.
     * @return its title on one line, or null.
     */
    static String title(Displayable d) {
        return d == null ? null : title(d.getPreviewText());
    }

    /**
     * Join the lines of a multi-line title with a middle dot, for example
     * "John 3:16-18 · King James Version".
     *
     * @param text the title as Quelea shows it, may be null.
     * @return the title on one line, or null.
     */
    static String title(String text) {
        if (text == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = plain(line).trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        return String.join(" · ", parts);
    }

    /**
     * Remove markup from a line of display text. A verse number in
     * {@code <sup>} becomes the number followed by a space.
     *
     * @param line the line, may be null.
     * @return the plain line, or null.
     */
    static String plain(String line) {
        if (line == null || line.indexOf('<') < 0 && line.indexOf('&') < 0) {
            return line;
        }
        String text = VERSE_NUMBER.matcher(line).replaceAll("$1 ");
        text = TAG.matcher(text).replaceAll("");
        text = text.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
        return SPACES.matcher(text).replaceAll(" ");
    }

    /**
     * The lines to show of a block of lyrics typed in Quelea, such as one
     * section of a song translation: without its section title, chord
     * lines and blank lines.
     *
     * @param block the text, lines separated by newlines.
     * @return the lyric lines.
     */
    static List<String> lyricLines(String block) {
        List<String> lines = new ArrayList<>();
        for (String line : block.split("\\R")) {
            LineTypeChecker.Type type = new LineTypeChecker(line).getLineType();
            if (line.isBlank() || type == LineTypeChecker.Type.TITLE || type == LineTypeChecker.Type.CHORDS) {
                continue;
            }
            lines.add(plain(line.strip()));
        }
        return lines;
    }
}
