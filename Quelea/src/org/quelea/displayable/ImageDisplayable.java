package org.quelea.displayable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.quelea.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's an image.
 * @author Michael
 */
public class ImageDisplayable implements Displayable {

    public static final int ICON_WIDTH = 60;
    public static final int ICON_HEIGHT = 60;
    private final File file;
    private final Icon image;
    private Icon resizedImage;

    /**
     * Create a new image displayable.
     * @param file the file for the displayable.
     * @param image a preview icon for the displayable.
     */
    public ImageDisplayable(File file, Icon image) {
        this.file = file;
        this.image = image;
    }

    /**
     * Get the displayable file.
     * @return the displayable file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the displayable image.
     * @return the displayable image.
     */
    public Icon getImage() {
        return image;
    }

    /**
     * Parse some XML representing this object and return the object it represents.
     * @param info the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static ImageDisplayable parseXML(Node node) {
        File file = new File(node.getTextContent());
        Icon icon = Utils.getImageIcon(new File("img/", file.getName()).getAbsolutePath(), ImageDisplayable.ICON_WIDTH, ImageDisplayable.ICON_HEIGHT);
        return new ImageDisplayable(file, icon);
    }

    /**
     * Get the XML that forms this image displayable.
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
     * @return
     */
    @Override
    public Icon getPreviewIcon() {
        if(resizedImage==null) {
            resizedImage = new ImageIcon(Utils.resizeImage(Utils.iconToImage(image), 30, 30));
        }
        return resizedImage;
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
     * Get any resources this displayable needs (in this case the image.)
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        List<File> files = new ArrayList<File>();
        files.add(file);
        return files;
    }
}
