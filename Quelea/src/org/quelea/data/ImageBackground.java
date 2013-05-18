/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Michael
 */
public class ImageBackground implements Background {

    private String imageLocation;
    private Image originalImage;

    /**
     * Create a new background that's a certain image.
     * <p/>
     * @param imageLocation the location of the background image.
     */
    public ImageBackground(String imageLocation) {
        File f = new File("img", imageLocation);
        this.imageLocation = imageLocation;
        if(f.exists()) {
            originalImage = new Image(f.toURI().toString());
        }
        else {
            originalImage = Utils.getImageFromColour(Color.BLACK);
        }
    }

    /**
     * Create a new background that's a certain image.
     * <p/>
     * @param image the background image.
     */
    public ImageBackground(Image image) {
        originalImage = image;
    }

    /**
     * Get the background image.
     */
    public Image getImage() {
        return originalImage;
    }

    /**
     * Get the image background file.
     * <p/>
     * @return the file representing the image background.
     */
    public File getImageFile() {
        return new File(new File("img"), imageLocation.trim());
    }

    /**
     * Get the current image location of this background, or null if the
     * background is currently a colour.
     * <p/>
     * @return the current image location of the background.
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Get the DB string of this background to store in the database.
     * <p/>
     * @return the background's DB string.
     */
    @Override
    public String getString() {
        return getImageLocation();
    }

    /**
     * Return any resources we depend upon, in this case the background image.
     * <p/>
     * @return the background image.
     */
    @Override
    public Collection<File> getResources() {
        List<File> ret = new ArrayList<>();
        ret.add(getImageFile());
        return ret;
    }

    @Override
    public void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundImgLocation, TextField backgroundVidLocation) {
        backgroundTypeSelect.getSelectionModel().select(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundImgLocation.setText(new File(getImageLocation()).getName());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.imageLocation);
        hash = 71 * hash + Objects.hashCode(this.originalImage);
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
        final ImageBackground other = (ImageBackground) obj;
        if(!Objects.equals(this.imageLocation, other.imageLocation)) {
            return false;
        }
        if(!Objects.equals(this.originalImage, other.originalImage)) {
            return false;
        }
        return true;
    }
}
