/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.displayable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's a video.
 * @author Michael
 */
public class VideoDisplayable implements Displayable {

    private final File file;

    /**
     * Create a new image displayable.
     * @param file the file for the displayable.
     * @param type the type of video.
     */
    public VideoDisplayable(File file) {
        this.file = file;
    }

    /**
     * Get the displayable file.
     * @return the displayable file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static VideoDisplayable parseXML(Node node) {
        return new VideoDisplayable(new File(node.getTextContent()));
    }

    /**
     * Get the XML that forms this image displayable.
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<filevideo>");
        ret.append(Utils.escapeXML(file.getAbsolutePath()));
        ret.append("</filevideo>");
        return ret.toString();
    }

    /**
     * Get the preview icon of this video.
     * @return the video's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/video.png"));
    }

    /**
     * Get the preview text for the image.
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return file.getName();
    }

    /**
     * Get any resources this displayable needs.
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        return new ArrayList<>();
    }

    /**
     * Get the text to print on the order of service.
     * @return "Video file: " and the name of the video file.
     */
    @Override
    public String getPrintText() {
        return "Video file: " + file.getName();
    }

    /**
     * Determine whether videos support clearing, which they don't.
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
