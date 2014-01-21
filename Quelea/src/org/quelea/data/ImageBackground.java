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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * An image background.
 * @author Michael
 */
public class ImageBackground implements Background, Serializable {

    private String imageName;
    private transient Image originalImage;
    private transient boolean init = false;

    /**
     * Create a new background that's a certain image.
     * <p/>
     * @param imageName the name of the background image in the img folder.
     */
    public ImageBackground(String imageName) {
        this.imageName = imageName;
        initImage();
    }
    
    private void initImage() {
        if(!init) {
            init=true;
            File f = new File("img", imageName);
            if(f.exists() && !imageName.trim().isEmpty()) {
                originalImage = new Image(f.toURI().toString());
            }
            else {
                originalImage = Utils.getImageFromColour(Color.BLACK);
            }
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
        initImage();
        return originalImage;
    }

    /**
     * Get the image background file.
     * <p/>
     * @return the file representing the image background.
     */
    public File getImageFile() {
        return new File(new File("img"), imageName.trim());
    }

    /**
     * Get the current image location of this background, or null if the
     * background is currently a colour.
     * <p/>
     * @return the current image location of the background.
     */
    public String getImageLocation() {
        return imageName;
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
    public void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundImgLocation, TextField backgroundVidLocation, Slider vidHueSlider) {
        backgroundTypeSelect.getSelectionModel().select(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundImgLocation.setText(new File(getImageLocation()).getName());
        backgroundColorPicker.setValue(Color.BLACK);
        backgroundVidLocation.clear();
        vidHueSlider.setValue(0);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.imageName);
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
        if(!Objects.equals(this.imageName, other.imageName)) {
            return false;
        }
        if(!Objects.equals(this.originalImage, other.originalImage)) {
            return false;
        }
        return true;
    }
}
