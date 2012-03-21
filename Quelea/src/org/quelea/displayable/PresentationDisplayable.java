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
import javax.swing.Icon;
import org.quelea.powerpoint.Presentation;
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's a presentation, at this time that means it's a 
 * powerpoint presentation converted into a number of static images.
 * @author Michael
 */
public class PresentationDisplayable implements Displayable {

    private final File file;
    private final Presentation presentation;

    public PresentationDisplayable(File file) {
        this.file = file;
        presentation = new Presentation(file.getAbsolutePath());
    }

    /**
     * Get the displayable file.
     * @return the displayable file.
     */
    public Presentation getPresentation() {
        return presentation;
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static PresentationDisplayable parseXML(Node node) {
        return new PresentationDisplayable(new File(node.getTextContent()));
    }

    /**
     * Get the XML that forms this presentation displayable.
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<filepresentation>");
        ret.append(Utils.escapeXML(file.getAbsolutePath()));
        ret.append("</filepresentation>");
        return ret.toString();
    }

    /**
     * Get the preview icon of this video.
     * @return the powerpoint preview icon.
     */
    @Override
    public Icon getPreviewIcon() {
        return Utils.getImageIcon("icons/powerpoint.png", 30, 30);
    }

    /**
     * Get the preview text to display in the schedule.
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return file.getName();
    }

    /**
     * Get the text to display when printing the order of service.
     * @return the text to display when printing.
     */
    @Override
    public String getPrintText() {
        return "Presentation: " + file.getName();
    }

    /**
     * Give a blank list (no resources.)
     * TODO: Include powerpoint as resource
     * @return blank list since presentations at the moment contain no resources
     */
    @Override
    public Collection<File> getResources() {
        return new ArrayList<>();
    }

    /**
     * Presentations cannot be meaningfully cleared, so return false.
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }
}
