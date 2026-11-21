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
package org.quelea.data.bible;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The text of a passage in one bible version. The text is kept with the passage
 * rather than looked up, so a saved schedule still shows it on a machine where
 * that bible isn't installed.
 *
 * @author Arvid
 */
public final class BibleVersionText implements Serializable {

    /**
     * How a version's verses were paired with the passage's.
     */
    public enum Match {
        /**
         * Paired knowing how both bibles number the verses.
         */
        AUTOMATIC,
        /**
         * Paired by number somewhere, because the numbering of a bible wasn't
         * known there.
         */
        BY_NUMBER,
        /**
         * Chosen by the user.
         */
        CHOSEN
    }

    private final String bibleName;
    private final String[] verseTexts;
    private final String[] verseRefs;
    private final Match match;
    private final String chosenReference;
    private final String location;

    /**
     * Create the text of a passage in one version, numbered as the passage.
     *
     * @param bibleName  the name of the bible.
     * @param verseTexts the text of each verse, in passage order, with null for
     *                   a verse that doesn't exist in this version.
     */
    public BibleVersionText(String bibleName, String[] verseTexts) {
        this(bibleName, verseTexts, new String[verseTexts.length], Match.AUTOMATIC, null, null);
    }

    /**
     * Create the text of a passage in one version.
     *
     * @param bibleName  the name of the bible.
     * @param verseTexts the text of each verse of the passage in this version,
     *                   in passage order: null for a verse that doesn't exist
     *                   in this version, or an empty string for one whose text
     *                   is already in the line above.
     * @param verseRefs  this version's own chapter and verse numbers for each
     *                   verse, such as "31:2" or "31:1-2", or null where they
     *                   are the same as the passage's.
     * @param match      how the verses were paired.
     * @param chosenReference for verses chosen by the user, the passage they
     *                   chose in this version, such as "Psalms 31:2-4".
     * @param location   the book, chapters and verses this version shows, or
     *                   null if not known.
     */
    public BibleVersionText(String bibleName, String[] verseTexts, String[] verseRefs, Match match,
                            String chosenReference, String location) {
        this.bibleName = bibleName;
        this.verseTexts = Arrays.copyOf(verseTexts, verseTexts.length);
        this.verseRefs = Arrays.copyOf(verseRefs, verseTexts.length);
        this.match = match;
        this.chosenReference = chosenReference;
        this.location = location;
    }

    /**
     * Get this version's own chapter and verse numbers for a verse.
     *
     * @param index the position of the verse in the passage.
     * @return the numbers, such as "31:2" or "31:1-2", or null if they are the
     * same as the passage's.
     */
    public String getVerseRef(int index) {
        return verseRefs[index];
    }

    /**
     * Determine whether the verses were paired knowing how both bibles number
     * them.
     *
     * @return false if some verses were paired by number because the
     * numbering of a bible wasn't known there.
     */
    public boolean isCertain() {
        return match != Match.BY_NUMBER;
    }

    /**
     * Get how the verses were paired with the passage's.
     *
     * @return how they were paired.
     */
    public Match getMatch() {
        return match;
    }

    /**
     * Get the passage the user chose to show in this version.
     *
     * @return the passage, such as "Psalms 31:2-4", or null if the verses
     * weren't chosen by the user.
     */
    public String getChosenReference() {
        return chosenReference;
    }

    /**
     * Get the book, chapters and verses this version shows.
     *
     * @return the location, such as "Psalms 31:2-4", or null if not known.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get the number of verses of the passage.
     *
     * @return the number of verses.
     */
    public int size() {
        return verseTexts.length;
    }

    /**
     * Get the name of the bible this text comes from.
     *
     * @return the bible name.
     */
    public String getBibleName() {
        return bibleName;
    }

    /**
     * Get the text of a verse in this version.
     *
     * @param index the position of the verse in the passage.
     * @return the text, or null if the verse doesn't exist in this version.
     */
    public String getVerseText(int index) {
        return verseTexts[index];
    }
}
