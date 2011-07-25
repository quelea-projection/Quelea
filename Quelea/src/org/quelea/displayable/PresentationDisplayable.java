package org.quelea.displayable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.quelea.powerpoint.Presentation;
import org.w3c.dom.Node;
import org.quelea.utils.Utils;

/**
 *
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
     * @param info the XML node representing this object.
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

    @Override
    public String getPreviewText() {
        return file.getName();
    }

    @Override
    public String getPrintText() {
        return "Presentation: " + file.getName();
    }

    @Override
    public Collection<File> getResources() {
        return new ArrayList<File>();
    }
    
}
