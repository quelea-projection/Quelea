package org.quelea;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * General utility class containing a bunch of static methods.
 * @author Michael
 */
public class Utils {

    /**
     * Don't instantiate me. I bite.
     */
    private Utils() {
        throw new AssertionError();
    }

    /**
     * Get an image icon from the location of a specified file.
     * @param location the location of the image to use.
     * @return the icon formed from the image, or null if an IOException
     * occured.
     */
    public static Icon getImageIcon(String location) {
        try {
            Image image = ImageIO.read(new File(location));
            return new ImageIcon(image);
        }
        catch(IOException ex) {
            return null;
        }
    }
}
