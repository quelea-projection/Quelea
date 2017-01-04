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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.Presentation;
import org.quelea.data.powerpoint.PresentationFactory;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's a presentation.
 * <p/>
 * @author Michael
 */
public class PresentationDisplayable implements Displayable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final File file;
    private final Presentation presentation;
    private OOPresentation ooPresentation;

    /**
     * Create a new presentation displayable
     * <p/>
     * @param file the file to create the presentation from.
     */
    public PresentationDisplayable(File file) throws IOException {
        this.file = file;
        presentation = new PresentationFactory().getPresentation(file);
        if(presentation == null) {
            throw new IOException("Error with presentation, couldn't open " + file);
        }
        if(QueleaProperties.get().getUseOO()) {
            try {
                ooPresentation = new OOPresentation(file.getAbsolutePath());
            }
            catch(Exception ex) {
                LOGGER.log(Level.WARNING, "Couldn't create OO presentation", ex);
                ooPresentation = null;
            }
        }
    }

    /**
     * Get the displayable file.
     * <p/>
     * @return the displayable file.
     */
    public Presentation getPresentation() {
        return presentation;
    }

    /**
     * Get the OO presentation object.
     * <p/>
     * @return the openoffice API backed presentation.
     */
    public OOPresentation getOOPresentation() {
        return ooPresentation;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     * <p/>
     * @param node the XML node representing this object.
     * @param fileChanges a map of any file changes that may have occurred.
     * @return the object as defined by the XML.
     */
    public static PresentationDisplayable parseXML(Node node, Map<String, String> fileChanges) {
        try {
            return new PresentationDisplayable(Utils.getChangedFile(node, fileChanges));
        }
        catch(IOException ex) {
            LOGGER.log(Level.INFO, "Couldn't create presentation for schedule", ex);
            return null;
        }
    }

    /**
     * Get the XML that forms this presentation displayable.
     * <p/>
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
     * <p/>
     * @return the powerpoint preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/powerpoint.png", 30, 30, false, true));
    }

    /**
     * Get the preview text to display in the schedule.
     * <p/>
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return file.getName();
    }

    /**
     * Give a blank list (no resources.) TODO: Include powerpoint as resource
     * <p/>
     * @return blank list since presentations at the moment contain no resources
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        files.add(file);
        return files;
    }

    /**
     * Presentations cannot be meaningfully cleared, so return false.
     * <p/>
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
        if(ooPresentation != null) {
            ooPresentation.dispose();
        }
    }
}
