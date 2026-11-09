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
 * What the API returns about the song database, the bibles and the themes.
 */
public final class Catalog {

    private Catalog() {
    }

    /**
     * A song in a search result.
     *
     * @param id the song's database id.
     * @param title the title.
     * @param author the author, or empty.
     */
    public record Song(long id, String title, String author) {
    }

    /**
     * A song with its text.
     *
     * @param id the song's database id.
     * @param title the title.
     * @param author the author, or empty.
     * @param sections the sections in the order they are written, not the
     * sung sequence.
     * @param translations the names of the song's translations.
     */
    public record SongDetails(long id, String title, String author, List<LiveSections.Section> sections,
            List<String> translations) {

        public SongDetails {
            sections = List.copyOf(sections);
            translations = List.copyOf(translations);
        }
    }

    /**
     * An image or video in Quelea's library.
     *
     * @param name the file name, used to pick it in other requests.
     * @param size the size in bytes.
     */
    public record MediaFile(String name, long size) {
    }

    /**
     * A new song for the database, from {@code POST /api/v2/songs}.
     *
     * @param title the title, not blank.
     * @param author the author, or null.
     * @param lyrics the lyrics in Quelea's text format: sections separated by
     * blank lines, each optionally starting with a title line such as
     * "Verse 1" or "Chorus", and chord lines above the text if wanted.
     * @param ccli the CCLI song number, or null.
     * @param copyright the copyright line, or null.
     * @param key the key, such as "G", or null.
     */
    public record NewSong(String title, String author, String lyrics, String ccli, String copyright, String key) {

        /**
         * The longest lyrics accepted; long hymns stay well below.
         */
        public static final int MAX_LYRICS = 20_000;

        /**
         * The longest title, author, CCLI number, copyright or key accepted.
         */
        public static final int MAX_FIELD = 200;

        public NewSong {
            title = tidy(title, "title");
            if (title == null) {
                throw new IllegalArgumentException("\"title\" is required");
            }
            author = tidy(author, "author");
            ccli = tidy(ccli, "ccli");
            copyright = tidy(copyright, "copyright");
            key = tidy(key, "key");
            if (lyrics == null || lyrics.isBlank()) {
                throw new IllegalArgumentException("\"lyrics\" is required");
            }
            if (lyrics.length() > MAX_LYRICS) {
                throw new IllegalArgumentException("\"lyrics\" is longer than " + MAX_LYRICS + " characters");
            }
            // Windows and old Mac line endings become plain newlines.
            lyrics = lyrics.replace("\r\n", "\n").replace('\r', '\n').strip();
        }

        private static String tidy(String value, String field) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String tidied = value.strip().replaceAll("\\s+", " ");
            if (tidied.length() > MAX_FIELD) {
                throw new IllegalArgumentException("\"" + field + "\" is longer than " + MAX_FIELD + " characters");
            }
            return tidied;
        }
    }

    /**
     * An installed bible.
     *
     * @param name the bible's name, used to pick it in other requests.
     * @param isDefault true if it is the default bible.
     */
    public record Translation(String name, boolean isDefault) {
    }

    /**
     * A book of a bible.
     *
     * @param name the book's name in that bible.
     * @param chapters the number of chapters.
     */
    public record Book(String name, int chapters) {
    }

    /**
     * The verses of a passage.
     *
     * @param translation the bible's name.
     * @param location the passage as Quelea writes it, for example
     * "John 3:16-18".
     * @param verses the verses in order.
     */
    public record Passage(String translation, String location, List<Verse> verses) {

        public Passage {
            verses = List.copyOf(verses);
        }
    }

    /**
     * One verse.
     *
     * @param chapter the chapter number.
     * @param verse the verse number.
     * @param text the verse text.
     */
    public record Verse(int chapter, int verse, String text) {
    }

    /**
     * A theme.
     *
     * @param name the theme's name, used to pick it in other requests.
     * @param builtIn true for Quelea's default theme.
     */
    public record Theme(String name, boolean builtIn) {
    }
}
