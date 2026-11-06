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

    private final String bibleName;
    private final String[] verseTexts;

    /**
     * Create the text of a passage in one version.
     *
     * @param bibleName  the name of the bible.
     * @param verseTexts the text of each verse, in passage order, with null for
     *                   a verse that doesn't exist in this version.
     */
    public BibleVersionText(String bibleName, String[] verseTexts) {
        this.bibleName = bibleName;
        this.verseTexts = Arrays.copyOf(verseTexts, verseTexts.length);
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
