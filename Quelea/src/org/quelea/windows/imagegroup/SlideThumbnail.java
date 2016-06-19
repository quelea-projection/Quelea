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
package org.quelea.windows.imagegroup;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.quelea.data.imagegroup.ImageGroupSlide;
import org.quelea.services.utils.QueleaProperties;

/**
 *
 * @author Arvid, based on presentation.SlideThumbnail
 */
public class SlideThumbnail extends BorderPane {

    private static final String BORDER_STYLE_SELECTED_ACTIVE = "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_SELECTED_INACTIVE = "-fx-padding: 0.2em;-fx-border-color: #999999;-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private static final String BORDER_STYLE_DESELECTED = "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;";
    private int num;
    private ImageGroupSlide slide;
    private ImageView image;
    private boolean selected;
    private boolean active;
    
    public SlideThumbnail(ImageGroupSlide slide, int num) {
        this.num = num;
        this.slide = slide;
        image = new ImageView(slide.getImage());
        image.setFitWidth(QueleaProperties.get().getThumbnailSize());
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setCache(true);
        setTop(image);
        setCenter(new Label(Integer.toString(num)));
    }

    public void setSelected(boolean selected) {
        this.selected=selected;
        recalcBorder();
    }

    public void setActive(boolean active) {
        this.active=active;
        image.setFitWidth(QueleaProperties.get().getThumbnailSize());
        recalcBorder();
    }
    
    private void recalcBorder() {
        if(selected) {
            if(active) {
                setStyle(BORDER_STYLE_SELECTED_ACTIVE);
            }
            else {
                setStyle(BORDER_STYLE_SELECTED_INACTIVE);
            }
        }
        else {
            setStyle(BORDER_STYLE_DESELECTED);
        }
    }

    public int getNum() {
        return num;
    }

    public ImageGroupSlide getSlide() {
        return slide;
    }
}
