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
package org.quelea.data.displayable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A displayable that's an media loop.
 *
 * @author Greg
 */
public class MediaLoopDisplayable implements Displayable, Comparable<MediaLoopDisplayable>, Serializable {

    /**
     * Comparable to compare different instances of media loop displayable
     *
     * @param Displayable to compare against
     * @return compare results
     */
    @Override
    public int compareTo(MediaLoopDisplayable o) {
        if (o.getMediaFiles().containsAll(this.getMediaFiles())) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * The builder responsible for building this media loop.
     */
    public static class Builder {

        private final MediaLoopDisplayable mediaLoop;

        /**
         * Create a new builder with the required fields.
         * <p/>
         * @param title The title of the media loop.
         * @param files The files in the media loop.
         */
        public Builder(String title, ArrayList<MediaFile> files) {
            mediaLoop = new MediaLoopDisplayable(files);
            mediaLoop.setTitle(title);
        }

        /**
         * Set the id of the media loop.
         * <p/>
         * @param id the song's id.
         * @return this builder.
         */
        public Builder id(long id) {
            mediaLoop.id = id;
            return this;
        }

        /**
         * add media to the media loop
         *
         * @param file the Media file to be added
         * @return this builder
         */
        public Builder addMedia(MediaFile file) {
            mediaLoop.add(file);
            return this;
        }

        /**
         * Get the song from this builder with all the fields set appropriately.
         * <p/>
         * @return the song.
         */
        public MediaLoopDisplayable get() {
            return mediaLoop;
        }
    }
    public static final int ICON_WIDTH = 60;
    public static final DataFormat MEDIA_LOOP_DISPLAYABLE_FORMAT = new DataFormat("medialoopdisplayable");
    public static final int ICON_HEIGHT = 60;
    private ArrayList<MediaFile> media = new ArrayList();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean quickInsert = false;
    private String title = "Media Loop";
    private long id = 0;

    /**
     * Create a new media loop displayable.
     *
     * @param media List of media files for display.
     */
    public MediaLoopDisplayable(ArrayList<MediaFile> media) {
        this.media = media;
    }

    /**
     * Creates a new media loop displayable.
     *
     */
    public MediaLoopDisplayable() {

    }

    /**
     * Get the displayable files.
     *
     * @return the displayable files.
     */
    public ArrayList<MediaFile> getMediaFiles() {
        return media;
    }

    /**
     * Sets the title of this displayable
     *
     * @param title the desired title of this displayable
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Add media file to media loop list
     *
     * @param file the file to be added
     */
    public void add(MediaFile file) {
        media.add(file);
    }

    /**
     * Get the unique ID of the song.
     * <p/>
     * @return the ID of the song.
     */
    public long getID() {
        return id;
    }

    /**
     * Set the unique ID of this song.
     * <p/>
     * @param id the id of the song.
     */
    public void setID(long id) {
        this.id = id;
    }

    /**
     * gets whether this displayable is quick insert
     *
     * @return true if quick insert, false otherwise
     */
    public boolean isQuickInsert() {
        return quickInsert;
    }

    /**
     * Sets whether this displayable is quick insert
     *
     * @param quickInsert true if quick insert, false otherwise
     */
    public void setQuickInsert(boolean quickInsert) {
        this.quickInsert = quickInsert;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static MediaLoopDisplayable parseXML(Node node) {

        MediaLoopDisplayable ret = new MediaLoopDisplayable();
        NodeList nodeList = node.getChildNodes();
        try {
            ret.setTitle(nodeList.item(0).getTextContent());
        } catch (DOMException ex) {
            LoggerUtils.getLogger().log(Level.WARNING, "Error parsing XML");
            return ret;
        }
        for (int iterator = 1; iterator < nodeList.getLength(); iterator++) {
            try {
                Node currentNode = nodeList.item(iterator);

                MediaFile file = new MediaFile(currentNode.getChildNodes().item(0).getTextContent(),
                        Integer.parseInt(currentNode.getChildNodes().item(1).getTextContent()));
                ret.add(file);
            } catch (DOMException | NumberFormatException ex) {
                LoggerUtils.getLogger().log(Level.WARNING, "Was not able to parse Media Loop XML", ex);
            }
        }

        return ret;
    }

    /**
     * Get the XML that forms this image displayable.
     *
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<mediaLoop>");
        ret.append("<mediaLoopName>");
        ret.append(title);
        ret.append("</mediaLoopName>");
        for (MediaFile f : media) {
            ret.append("<mediaLoopMedia>");
            ret.append("<file>");
            ret.append(Utils.escapeXML(f.getName()));
            ret.append("</file>");
            ret.append("<advanceTime>");
            ret.append(f.getAdvanceTime());
            ret.append("</advanceTime>");
            ret.append("</mediaLoopMedia>");
        }
        ret.append("</mediaLoop>");
        return ret.toString();
    }

    /**
     * Get the preview icon for this displayable (30x30.)
     *
     * @return the preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        Image image = null;

        if (Utils.fileIsImage(media.get(0))) {
            image = new Image("file:" + media.get(0).getAbsolutePath());
        } else {

            image = Utils.getVidBlankImage(media.get(0).getAbsoluteFile());
        }

        if (image == null) {
            image = Utils.getImageFromColour(Color.BLACK);
        }
        ImageView small = new ImageView(image);
        small.setFitHeight(30);
        small.setFitWidth(30);
        return small;
    }

    /**
     * Get the image representing this media loop
     *
     * @return the image representing this media loop
     */
    public Image getImage() {
        Image image = null;

        if (Utils.fileIsImage(media.get(0))) {
            image = new Image("file:" + media.get(0).getAbsolutePath());
        } else {

            image = Utils.getVidBlankImage(media.get(0).getAbsoluteFile());
        }

        if (image == null) {
            image = Utils.getImageFromColour(Color.BLACK);
        }

        return image;
    }

    /**
     * Get the preview text for the media loop.
     *
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return title;
    }

    /**
     * Get any resources this displayable needs (in this case the all the media
     * files)
     *
     * @return the media files backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        for (MediaFile mediaFile : media) {
            files.add(mediaFile);
        }
        return files;
    }

    /**
     * Get the summary to print in an order of service.
     *
     * @return the summary as a string. Just the media loop and the file name at
     * present.
     */
    @Override
    public String getPrintText() {
        return "Media Loop: " + title;
    }

    /**
     * Media loops don't support clearing of text (they contain no text) so
     * false, always.
     *
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }

    @Override
    public void dispose() {
        //Nothing needed here.
    }
}
