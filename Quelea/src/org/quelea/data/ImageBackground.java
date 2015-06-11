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
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.ImageManager;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * An image background.
 * <p>
 * @author Michael
 */
public class ImageBackground implements Background, Serializable {

    private final String imageName;
    private transient SoftReference<Image> originalImage;

    /**
     * Create a new background that's a certain image.
     * <p/>
     * @param imageName the name of the background image in the user img folder.
     */
    public ImageBackground(String imageName) {
        this.imageName = imageName;
        initImage();
    }

    private Image initImage() {
        File f = new File(QueleaProperties.get().getImageDir(), imageName);
        Image img;
        if(f.exists() && !imageName.trim().isEmpty()) {
            img = ImageManager.INSTANCE.getImage(f.toURI().toString());
            originalImage = new SoftReference<>(img);
        }
        else {
            img = Utils.getImageFromColour(Color.BLACK);
            originalImage = new SoftReference<>(img);
        }
        return img;
    }

    /**
     * Get the background image.
     * <p>
     * @return the background image.
     */
    public Image getImage() {
        Image img = null;
        if(originalImage != null) {
            img = originalImage.get();
        }
        if(img == null) {
            img = initImage();
        }
        return img;
    }

    /**
     * Get the image background file.
     * <p/>
     * @return the file representing the image background.
     */
    public File getImageFile() {
        return new File(QueleaProperties.get().getImageDir(), imageName.trim());
    }

    /**
     * Get the DB string of this background to store in the database.
     * <p/>
     * @return the background's DB string.
     */
    @Override
    public String getString() {
        return imageName;
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
    public void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundImgLocation, TextField backgroundVidLocation, Slider vidHueSlider, CheckBox vidStretchCheckbox) {
        backgroundTypeSelect.getSelectionModel().select(LabelGrabber.INSTANCE.getLabel("image.theme.label"));
        backgroundImgLocation.setText(imageName);
        backgroundColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.fireEvent(new ActionEvent());
        backgroundVidLocation.clear();
        vidHueSlider.setValue(0);
        vidStretchCheckbox.setSelected(false);
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
