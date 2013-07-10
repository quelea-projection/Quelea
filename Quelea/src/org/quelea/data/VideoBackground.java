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
import javafx.scene.control.TextField;
import org.quelea.services.languages.LabelGrabber;

/**
 * A background comprising of a video.
 * <p/>
 * @author Michael
 */
public class VideoBackground implements Background, Serializable {

    private String vidLocation;

    public VideoBackground(String vidLocation) {
        this.vidLocation = vidLocation;
    }

    /**
     * Get the video background file.
     * <p/>
     * @return the file representing the video background
     */
    public File getVideoFile() {
        return new File(new File("vid"), vidLocation.trim());
    }

    @Override
    public String getString() {
        vidLocation = new File(vidLocation).getName();
        return vidLocation.trim();
    }

    @Override
    public Collection<File> getResources() {
        List<File> ret = new ArrayList<>();
        ret.add(getVideoFile());
        return ret;
    }

    @Override
    public void setThemeForm(ColorPicker backgroundColorPicker, ComboBox<String> backgroundTypeSelect, TextField backgroundImgLocation, TextField backgroundVidLocation) {
        backgroundTypeSelect.getSelectionModel().select(LabelGrabber.INSTANCE.getLabel("video.theme.label"));
        backgroundVidLocation.setText(getVideoFile().getName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.vidLocation);
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
        final VideoBackground other = (VideoBackground) obj;
        if(!Objects.equals(this.vidLocation, other.vidLocation)) {
            return false;
        }
        return true;
    }
    
}
