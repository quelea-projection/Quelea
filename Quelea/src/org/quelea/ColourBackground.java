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
package org.quelea;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.quelea.languages.LabelGrabber;

/**
 *
 * @author Michael
 */
public class ColourBackground implements Background {

    private Color colour;

    /**
     * Create a new background that's a certain colour.
     * <p/>
     * @param colour the colour of the background.
     */
    public ColourBackground(Color colour) {
        this.colour = colour;
    }

    /**
     * Get the current colour of this background, or null if the background is
     * currently an image.
     * <p/>
     * @return the colour of the background.
     */
    public Color getColour() {
        return colour;
    }
    
    /**
     * Get the DB string of this background to store in the database.
     * <p/>
     * @return the background's DB string.
     */
    @Override
    public String getDBString() {
        return "$backgroundcolour:" + getColour();
    }
    
    /**
     * We don't depend on any resources.
     * <p/>
     * @return empty collection.
     */
    @Override
    public Collection<File> getResources() {
        List<File> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundLocation, TextField backgroundVidLocation) {
        backgroundTypeSelect.getSelectionModel().select(LabelGrabber.INSTANCE.getLabel("color.theme.label"));
        backgroundColorPicker.setValue(getColour());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.colour);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ColourBackground other = (ColourBackground) obj;
        if(!Objects.equals(this.colour, other.colour)) {
            return false;
        }
        return true;
    }
    
}
