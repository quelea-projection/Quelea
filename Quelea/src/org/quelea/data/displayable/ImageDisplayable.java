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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's an image.
 *
 * @author Michael
 */
public class ImageDisplayable implements Displayable {

    public static final int ICON_WIDTH = 60;
    public static final int ICON_HEIGHT = 60;
    private final File file;
    private Image image;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new image displayable.
     *
     * @param file the file for the displayable.
     */
    public ImageDisplayable(File file) {
        this.file = file;
        image = new Image("file:" + file.getAbsolutePath());
    }

    public ImageDisplayable(Image image) {
        this.file = null;
        this.image = image;
    }
    /**
     * Get the displayable file.
     *
     * @return the displayable file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the displayable image.
     *
     * @return the displayable image.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static ImageDisplayable parseXML(Node node) {
        File file = new File(node.getTextContent());
        return new ImageDisplayable(new File(QueleaProperties.get().getImageDir(), file.getName()));
    }

    /**
     * Get the XML that forms this image displayable.
     *
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<fileimage>");
        ret.append(Utils.escapeXML(file.getName()));
        ret.append("</fileimage>");
        return ret.toString();
    }

    /**
     * Get the preview icon for this displayable (30x30.)
     *
     * @return the preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        ImageView small = new ImageView(new Image("file:" + file.getAbsolutePath(), 30, 30, false, true));
        return small;
    }

    /**
     * Get the preview text for the image.
     *
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return file.getName();
    }

    /**
     * Get any resources this displayable needs (in this case the image.)
     *
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<>();
        files.add(file);
        return files;
    }

    /**
     * Images don't support clearing of text (they contain no text) so false,
     * always.
     *
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
