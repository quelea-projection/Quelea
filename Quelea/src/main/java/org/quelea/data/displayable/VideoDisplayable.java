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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A displayable that's a video.
 * <p>
 * @author Michael
 */
public class VideoDisplayable implements MultimediaDisplayable, Serializable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final String location;

    /**
     * Create a new video displayable.
     * <p>
     * @param location the location of the displayable.
     */
    public VideoDisplayable(String location) {
        this.location = location;
    }

    /**
     * Get the displayable location.
     * <p>
     * @return the displayable location.
     */
    @Override
    public String getLocation() {
        return location;
    }

    /**
     * Get the displayable location (as a file.)
     * <p>
     * @return the displayable location (as a file.)
     */
    public File getLocationAsFile() {
        return new File(location);
    }

    /**
     * Get the displayable name.
     * <p>
     * @return the displayable name.
     */
    @Override
    public String getName() {
        return new File(location).getName();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p>
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static VideoDisplayable parseXML(Node node, Map<String, String> fileChanges) {
        if (node instanceof Element) {
            String youtubeTitle = ((Element) node).getAttribute("youtubetitle");
            if (youtubeTitle != null && !youtubeTitle.trim().isEmpty()) {
                return null; //Youtube no longer supported
            }
        }
        File file = Utils.getChangedFile(node, fileChanges);
        if (!file.exists()) {
            LOGGER.log(Level.WARNING, "Video file {0} doesn't exist.", file.getAbsolutePath());
            return null;
        }
        return new VideoDisplayable(file.getAbsolutePath());
    }

    /**
     * Get the XML that forms this image displayable.
     * <p>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<filevideo>");
        if (QueleaProperties.get().getEmbedMediaInScheduleFile()) {
            ret.append(Utils.escapeXML(new File(location).getName()));
        } else {
            ret.append(Utils.escapeXML(location));
        }
        ret.append("</filevideo>");
        return ret.toString();
    }

    /**
     * Get the preview icon of this video.
     * <p>
     * @return the video's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        if (Utils.fileIsVideo(getLocationAsFile())) {
            return new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-sch-video-light.png" : "file:icons/ic-sch-video.png",30,30,false,true));
        } else {
            return new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-sch-audio-light.png" : "file:icons/ic-sch-audio.png",30,30,false,true));
        }
    }

    /**
     * Get the preview text for the image.
     * <p>
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return getName();
    }

    /**
     * Get any resources this displayable needs.
     * <p>
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        files.add(new File(location));
        return files;
    }

    /**
     * Determine whether videos support clearing, which they don't.
     * <p>
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

    public boolean isVideo() {
        if (Utils.fileIsVideo(getLocationAsFile())) {
            return true;
        } else {
            return false;
        }
    }
}
