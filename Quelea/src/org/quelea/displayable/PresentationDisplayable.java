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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.quelea.powerpoint.OOPresentation;
import org.quelea.powerpoint.Presentation;
import org.quelea.powerpoint.PresentationFactory;
import org.quelea.sound.AudioTrack;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's a presentation.
 * @author Michael
 */
public class PresentationDisplayable implements Displayable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final File file;
    private final Presentation presentation;
    private OOPresentation ooPresentation;
    private AudioTrack track;

    /**
     * Create a new presentation displayable
     * @param file the file to create the presentation from.
     */
    public PresentationDisplayable(File file) throws IOException {
        this.file = file;
        presentation = new PresentationFactory().getPresentation(file);
        if(presentation==null) {
            throw new IOException("Error with presentation, couldn't open " + file);
        }
        try {
            ooPresentation = new OOPresentation(file.getAbsolutePath());
        }
        catch (Exception ex) {
            ooPresentation = null;
        }
    }

    /**
     * Get the displayable file.
     * @return the displayable file.
     */
    public Presentation getPresentation() {
        return presentation;
    }

    /**
     * Get the OO presentation object.
     * @return the openoffice API backed presentation.
     */
    public OOPresentation getOOPresentation() {
        return ooPresentation;
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static PresentationDisplayable parseXML(Node node) {
        try {
            return new PresentationDisplayable(new File(node.getTextContent()));
        }
        catch(IOException ex) {
            LOGGER.log(Level.INFO, "Couldn't create presentation for schedule", ex);
            return null;
        }
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
    
    /**
     * Get rid of this presentation displayable.
     */
    @Override
    public void dispose() {
        if(ooPresentation!=null) {
            ooPresentation.dispose();
        }
    }
    
    @Override
    public void setAudio(AudioTrack track) {
        this.track = track;
    }

    @Override
    public AudioTrack getAudio() {
        return track;
    }
}
