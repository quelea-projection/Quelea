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
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.pdf.*;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's a PDF.
 * <p/>
 * @author Arvid, based on PresentationDisplayable
 */
public class PdfDisplayable implements Displayable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final File file;
    private final Pdf presentation;

    /**
     * Create a new PDF displayable
     * <p/>
     * @param file the file to create the PDF presentation from.
     */
    public PdfDisplayable(File file) throws IOException {
        this.file = file;
        presentation = new PdfFactory().getPresentation(file);
        if (presentation == null) {
            throw new IOException("Error with PDF, couldn't open " + file);
        }
    }

    /**
     * PDF presentations cannot be meaningfully cleared, so return false.
     * <p/>
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }

    /**
     * Get the XML that forms this PDF displayable.
     * <p/>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<filepdf>");
        ret.append(Utils.escapeXML(file.getAbsolutePath()));
        ret.append("</filepdf>");
        return ret.toString();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static PdfDisplayable parseXML(Node node, Map<String, String> fileChanges) throws IOException {
        return new PdfDisplayable(Utils.getChangedFile(node, fileChanges));
    }

    /**
     * Get the preview icon of this PDF.
     * <p/>
     * @return the PDF preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/add_pdf.png", 30, 30, false, true));
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
     * Give a list of the file(s)
     * <p/>
     * @return the file(s)
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        files.add(file);
        return files;
    }

    /**
     * Get rid of this presentation displayable.
     */
    @Override
    public void dispose() {
    }

    /**
     * Get the displayable file.
     * <p/>
     * @return the displayable file.
     */
    public Pdf getPresentation() {
        return presentation;
    }

}
