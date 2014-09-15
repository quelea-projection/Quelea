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
package org.quelea.data.mediaLoop;

import java.io.File;
import java.net.URI;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.quelea.services.utils.Utils;

/**
 * Creates a new media file, which stores the media file and the time it needs
 * before advance.
 *
 * @author Greg
 */
public class MediaFile extends File {

    private int advanceTime = 7;
    private static final long serialVersionUID = -1034547454307518214L;

    /**
     * Creates a new media file from a uri, including the time to advance.
     *
     * @param uri the URI for the file
     * @param secondsBeforeAdvance seconds until display should be advanced to
     * next media
     */
    public MediaFile(URI uri, int secondsBeforeAdvance) {
        super(uri);
        this.advanceTime = secondsBeforeAdvance;

    }

    /**
     * Creates a new media file from a file, specifying a child. It also
     * includes the time to advance.
     *
     * @param parent The parent the file is created from
     * @param child The child associated with this file
     * @param secondsBeforeAdvance seconds until display should be advanced to
     * next media
     */
    public MediaFile(File parent, String child, int secondsBeforeAdvance) {
        super(parent, child);
        this.advanceTime = secondsBeforeAdvance;
    }

    /**
     * Creates a new media file from a string, specifying a child. It also
     * includes the time to advance.
     *
     * @param parent The parent the file is created from
     * @param child The child associated with this file
     * @param secondsBeforeAdvance seconds until display should be advanced to
     * next media
     */
    public MediaFile(String parent, String child, int secondsBeforeAdvance) {
        super(parent, child);
        this.advanceTime = secondsBeforeAdvance;
    }

    /**
     * Creates a new media file from a string specifying the path. It also
     * includes the time to advance.
     *
     * @param pathname The path to the media.
     * @param secondsBeforeAdvance seconds until display should be advanced to
     * next media
     */
    public MediaFile(String pathname, int secondsBeforeAdvance) {
        super(pathname);
        this.advanceTime = secondsBeforeAdvance;
    }

    /**
     * Set the time before the display should be changed to the next media.
     *
     * @param secondsBeforeAdvance The time in seconds
     */
    public void setAdvanceTime(int secondsBeforeAdvance) {
        this.advanceTime = secondsBeforeAdvance;
    }

    /**
     * Gets the time that this media should be shown before it is advanced to
     * the next media.
     *
     * @return The time in seconds
     */
    public int getAdvanceTime() {
        return this.advanceTime;
    }

    /**
     * Gets an image representing this media item
     *
     * @return an image from this media
     */
    public Image getImage() {

        if (Utils.getMediaLoopImageCache().get(this) == null) {
            String s = this.getAbsolutePath();
            if (Utils.fileIsImage(this)) {

                Utils.getMediaLoopImageCache().put(this, new Image("file:" + s));
            } else if (Utils.fileIsVideo(this)) {

                Utils.getMediaLoopImageCache().put(this, Utils.getVidBlankImage(this));

            } else {
                Utils.getMediaLoopImageCache().put(this, Utils.getImageFromColour(Color.BLACK));
            }

        }
        return Utils.getMediaLoopImageCache().get(this);
    }
}
