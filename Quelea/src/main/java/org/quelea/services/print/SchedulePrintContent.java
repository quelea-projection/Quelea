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
package org.quelea.services.print;

/**
 * What a printed schedule contains.
 */
public enum SchedulePrintContent {

    /**
     * Only the order of service.
     */
    OVERVIEW("print.content.overview", false, false),
    /**
     * The order of service followed by the lyrics of each song, without chords.
     */
    LYRICS("print.content.lyrics", true, false),
    /**
     * The order of service followed by the lyrics of each song, with chords.
     */
    LYRICS_WITH_CHORDS("print.content.lyrics.chords", true, true);

    private final String labelKey;
    private final boolean lyrics;
    private final boolean chords;

    SchedulePrintContent(String labelKey, boolean lyrics, boolean chords) {
        this.labelKey = labelKey;
        this.lyrics = lyrics;
        this.chords = chords;
    }

    /**
     * @return the key of the label describing this option.
     */
    public String getLabelKey() {
        return labelKey;
    }

    /**
     * @return true if the song lyrics are printed after the overview.
     */
    public boolean includesLyrics() {
        return lyrics;
    }

    /**
     * @return true if the lines of chords are printed with the lyrics.
     */
    public boolean includesChords() {
        return chords;
    }

    /**
     * Get the option with the given name, falling back to the overview for
     * unknown or missing values.
     *
     * @param name the name of the option (as returned by name()).
     * @return the matching option.
     */
    public static SchedulePrintContent fromName(String name) {
        if (name != null) {
            for (SchedulePrintContent content : values()) {
                if (content.name().equalsIgnoreCase(name.trim())) {
                    return content;
                }
            }
        }
        return OVERVIEW;
    }
}
