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

import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.data.displayable.WebDisplayable;

/**
 * The item type names used in the v2 API.
 */
public final class ItemTypes {

    private ItemTypes() {
    }

    /**
     * Get the API type name of a displayable.
     *
     * @param d the displayable, may be null.
     * @return one of song, bible, presentation, pdf, images, image, video,
     * audio, timer, web or other; null for null.
     */
    public static String of(Displayable d) {
        if (d == null) {
            return null;
        }
        if (d instanceof SongDisplayable) {
            return "song";
        }
        if (d instanceof BiblePassage) {
            return "bible";
        }
        if (d instanceof PresentationDisplayable) {
            return "presentation";
        }
        if (d instanceof PdfDisplayable) {
            return "pdf";
        }
        if (d instanceof ImageGroupDisplayable) {
            return "images";
        }
        if (d instanceof ImageDisplayable) {
            return "image";
        }
        if (d instanceof VideoDisplayable) {
            return "video";
        }
        if (d instanceof AudioDisplayable) {
            return "audio";
        }
        if (d instanceof TimerDisplayable) {
            return "timer";
        }
        if (d instanceof WebDisplayable) {
            return "web";
        }
        return "other";
    }
}
