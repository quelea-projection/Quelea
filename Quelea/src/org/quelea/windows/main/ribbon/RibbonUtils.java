package org.quelea.windows.main.ribbon;

import java.awt.Dimension;
import java.util.Arrays;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.quelea.utils.Utils;

/**
 * Utility methods for creating the ribbon.
 * @author Michael
 */
public class RibbonUtils {

    private RibbonUtils() {
        throw new AssertionError();
    }

    public static void applyStandardResizePolicies(JRibbonBand band) {
        band.setResizePolicies(Arrays.<RibbonBandResizePolicy>asList(
                new CoreRibbonResizePolicies.None(band.getControlPanel()),
                new CoreRibbonResizePolicies.Mirror(band.getControlPanel()),
                new CoreRibbonResizePolicies.Mid2Low(band.getControlPanel()),
                new IconRibbonBandResizePolicy(band.getControlPanel())));
    }
    
    /**
     * Get an icon to be displayed on a flamingo ribbon from a file name.
     * @param file the path of the file
     * @param width the width of the icon
     * @param height the height of the icon
     */
    public static ResizableIcon getRibbonIcon(String file, int width, int height) {
        return ImageWrapperResizableIcon.getIcon(Utils.getImage(file), new Dimension(width, height));
    }
}
