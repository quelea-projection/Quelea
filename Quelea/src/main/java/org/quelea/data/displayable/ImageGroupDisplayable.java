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
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import org.quelea.data.imagegroup.ImageGroup;
 import org.quelea.data.imagegroup.ImageGroupFactory;
 import org.quelea.services.utils.QueleaProperties;
 import org.quelea.services.utils.Utils;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;

 /**
 * A displayable that's an image group.
 * <p/>
 * @author Arvid, based on PresentationDisplayable
 */
public class ImageGroupDisplayable implements Displayable {
    private final File[] files;
    private final ImageGroup presentation;
    private final String title;

     public ImageGroupDisplayable(File[] file, String groupTitle) throws IOException {
        this.files = file;
        this.title = groupTitle;
        presentation = new ImageGroupFactory().getPresentation(file);
        if (presentation == null) {
            throw new IOException("Error with image group, couldn't open " + Arrays.toString(file));
        }
    }

    /**
     * Image group presentations cannot be meaningfully cleared, so return
     * false.
     * <p/>
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }

    /**
     * Get the XML that forms this image group displayable.
     * <p/>
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<fileimagegroup>");
        ret.append("<title>");
        ret.append(Utils.escapeXML(title));
        ret.append("</title>");
        ret.append("<files>");
        for (File f : files) {
            String loc;
            if (QueleaProperties.get().getEmbedMediaInScheduleFile()) {
                loc = Utils.escapeXML(Utils.toRelativeStorePath(f));
            } else {
                loc = f.getAbsolutePath();
            }
            ret.append(Utils.escapeXML(loc)).append(";");
        }
        ret.append("</files>");
        ret.append("</fileimagegroup>");
        return ret.toString();
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static ImageGroupDisplayable parseXML(Node node, Map<String, String> fileChanges) throws IOException {
        // Check for the title node. Since title is a later update, we want to maintain compatibility with older exports.
        String title = null;
        NodeList childNodes = node.getChildNodes();
        if (childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if ("title".equals(childNode.getNodeName())) {
                    title = childNode.getTextContent();
                } else if ("files".equals(childNode.getNodeName())) {
                    node = childNode;
                }
            }
        }
        return imageGroupFromXml(node, fileChanges, title);
    }


     private static ImageGroupDisplayable imageGroupFromXml(Node node, Map<String, String> fileChanges, String title) throws IOException {
         String[] files = node.getTextContent().split(";");
         ArrayList<File> tmp = new ArrayList<>();
         for (String f : files) {
             tmp.add(Utils.getChangedFile(f, fileChanges));
         }
         File[] fileArray = tmp.toArray(new File[0]);
         if (title == null) title = concatenatedFileNames(fileArray);
         return new ImageGroupDisplayable(fileArray, title);
     }

     /**
     * Get the preview icon of this image group.
     * <p/>
     * @return the imagegroup preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/image-group-schedule.png", 30, 30, false, true));
    }

    /**
     * Get the preview text to display in the schedule.
     * <p/>
     * @return the preview text.
     */
    @Override
    public String getPreviewText() {
        return this.title;
    }

    public static String concatenatedFileNames(File[] files) {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(f.getName()).append(", ");
        }
        return "Image Group: " + sb.substring(0, sb.toString().lastIndexOf(","));
    }

    /**
     * Give a list of the file(s)
     * <p/>
     * @return the file(s)
     */
    @Override
    public Collection<File> getResources() {
        List<File> f = new ArrayList<>();
        Collections.addAll(f, files);
        return f;
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
    public ImageGroup getPresentation() {
        return presentation;
    }

}
