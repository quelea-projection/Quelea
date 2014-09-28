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
package org.quelea.data;

import java.io.File;
import java.util.Collection;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

/**
 * A visual background.
 * <p/>
 * @author Michael
 */
public interface Background {

    /**
     * Get the DB string of this background to store in the database.
     * <p/>
     * @return the background's DB string.
     */
    String getString();
    
    /**
     * Get any resources this background may possess.
     * @return any resources this background depends on.
     */
    Collection<File> getResources();
    
    /**
     * Set the given elements on the theme form to the correct values for this background.
     * @param backgroundColorPicker the colour picker
     * @param backgroundTypeSelect the ComboBox to select the background type.
     * @param backgroundLocation the text field representing the background "location".
     */
    void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundImgLocation, TextField backgroundVidLocation, Slider vidHueSlider, CheckBox vidStretchCheckbox);
}
