/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.mediaLoop;

import java.io.Serializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * Creates a media loop thumbnail that is used in the preview of media loops
 *
 * @author Greg
 */
public class MediaLoopThumbnail extends BorderPane implements Serializable {

    private static final String BORDER_STYLE_SELECTED_ACTIVE = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_SELECTED_INACTIVE = "-fx-padding: 0.2em;-fx-border-color: #999999;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    public static final DataFormat MEDIA_LOOP_THUMBNAIL_FORMAT = new DataFormat("medialoopthumbnail");
    private boolean selected;
    private boolean active;
    private MediaFile slide;
    private int index = -1;

    /**
     * Creates a new media loop thumbnail
     *
     * @param slide the data slide that needs to be shown
     */
    public MediaLoopThumbnail(MediaFile slide) {
        this.slide = slide;
        Image image = null;
        if (Utils.fileIsImage(slide)) {
            image = new Image("file:" + slide.getAbsolutePath());
        } else {

            image = Utils.getVidBlankImage(slide.getAbsoluteFile());
        }

        if (image == null) {
            image = Utils.getImageFromColour(Color.BLACK);
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(QueleaApp.get().getProjectionWindow().getCanvas().getHeight()
                / (QueleaApp.get().getProjectionWindow().getCanvas().getWidth() / 200));
        imageView.setPreserveRatio(true);

        setTop(imageView);
        setCenter(new Label(slide.getName()));
    }

    /**
     * Set whether this slide is selected
     *
     * @param selected True if selected, false otherwise
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        recalcBorder();
    }

    /**
     * Set whether this slide is active
     *
     * @param active True if active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
        recalcBorder();
    }

    /**
     * Recalculate what the border should be and apply it
     */
    private void recalcBorder() {
        if (selected) {
            if (active) {
                setStyle(BORDER_STYLE_SELECTED_ACTIVE);
            } else {
                setStyle(BORDER_STYLE_SELECTED_INACTIVE);
            }
        } else {
            setStyle(BORDER_STYLE_DESELECTED);
        }
    }

    /**
     * Get the name of the slide
     *
     * @return the name of the slide
     */
    public String getName() {
        return slide.getName();
    }

    /**
     * Get the index of the slide
     *
     * @return the index of the slide
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the media file from this thumbnail
     *
     * @return the media file
     */
    public MediaFile getMediaFile() {
        return slide;
    }

    /**
     * Set the index of the slide
     *
     * @param desiredIndex the desired index of the slide
     */
    public void setIndex(int desiredIndex) {
        this.index = desiredIndex;
    }

}
