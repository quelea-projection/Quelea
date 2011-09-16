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
