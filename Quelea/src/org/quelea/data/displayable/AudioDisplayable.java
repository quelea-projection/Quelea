package org.quelea.data.displayable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 *
 * @author tomaszpio@gmail.com
 */
public class AudioDisplayable implements MultimediaDisplayable {
    private final File file;

    /**
     * create audio dsiplayable instance
     *
     * @param file
     */
    public AudioDisplayable(File file) {
        this.file = file;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static AudioDisplayable parseXML(Node node) {
        return new AudioDisplayable(new File(node.getTextContent()));
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
     * Determine whether videos support clearing, which they don't.
     *
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return true;
    }

    /**
     * Get the XML that forms this image displayable.
     *
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<fileaudio>");
        ret.append(Utils.escapeXML(file.getAbsolutePath()));
        ret.append("</fileaudio>");
        return ret.toString();
    }

    /**
     * Get the preview icon of this audio.
     *
     * @return the video's preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        return new ImageView(new Image("file:icons/audio30.png"));
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
     * Get the text to print on the order of service.
     *
     * @return "Video file: " and the name of the video file.
     */
    @Override
    public String getPrintText() {
        return "Audio file: " + file.getName(); //@todo add translation
    }

    /**
     * Get any resources this displayable needs.
     *
     * @return the image backing this displayable.
     */
    @Override
    public Collection<File> getResources() {
        return new ArrayList<>();
    }

    @Override
    public void dispose() {
    }
}
