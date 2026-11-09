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

import java.util.List;

/**
 * The notices scrolling across the screen, and the saved ones that can be
 * shown again. Answered by {@code GET /api/v2/notices}.
 *
 * @param notices the notices being shown, in the order they were added.
 * @param saved the saved notices, to offer as ready-made texts.
 */
public record Notices(List<Active> notices, List<Saved> saved) {

    public Notices {
        notices = List.copyOf(notices);
        saved = List.copyOf(saved);
    }

    /**
     * A notice being shown.
     *
     * @param id the notice's id, for removing it: "nt_3".
     * @param text the text.
     * @param timesLeft how many more times it scrolls past, or null if it
     * keeps going until removed.
     * @param color the text colour as "#rrggbb".
     */
    public record Active(String id, String text, Integer timesLeft, String color) {
    }

    /**
     * A saved notice.
     *
     * @param text the text.
     * @param times how many times it scrolls past, or 0 for until removed.
     * @param color the text colour as "#rrggbb".
     */
    public record Saved(String text, int times, String color) {
    }

    /**
     * How a remote asks for a new notice.
     *
     * @param text the text, not blank.
     * @param times how many times it scrolls past, 1 to {@value #MAX_TIMES},
     * or 0 for until removed.
     * @param color the text colour as "#rrggbb", or null for white.
     */
    public record Request(String text, int times, String color) {

        /**
         * The most times a notice can be asked to scroll past.
         */
        public static final int MAX_TIMES = 100;

        /**
         * The longest notice text accepted.
         */
        public static final int MAX_LENGTH = 500;

        public Request {
            text = text == null ? "" : text.strip().replaceAll("\\s+", " ");
            if (text.isEmpty()) {
                throw new IllegalArgumentException("\"text\" is required");
            }
            if (text.length() > MAX_LENGTH) {
                throw new IllegalArgumentException("\"text\" is longer than " + MAX_LENGTH + " characters");
            }
            if (times < 0 || times > MAX_TIMES) {
                throw new IllegalArgumentException("\"times\" must be 0 (until removed) or 1 to " + MAX_TIMES);
            }
            if (color != null && !color.matches("#[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("\"color\" must look like #ffcc00");
            }
        }
    }
}
