/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.powerpoint;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;

/**
 * Static methods useful for the openoffice world of presentations.
 * @author Michael
 */
public class OOUtils {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    
    /**
     * No instantiation.
     */
    private OOUtils() {
        throw new AssertionError();
    }
    
    /**
     * Attempt to initialise the openoffice presentation system, if the relevant
     * properties are set. If initialisation fails an appropriate message will
     * be displayed to the user.
     */
    public static void attemptInit() {
        if(QueleaProperties.get().getUseOO()) {
            LOGGER.log(Level.INFO, "Setting up openoffice");
            OOPresentation.init(QueleaProperties.get().getOOPath());
            if(OOPresentation.isInit()) {
                LOGGER.log(Level.INFO, "Successfully set up openoffice");
            }
            else {
                LOGGER.log(Level.INFO, "Failed to set up openoffice");
                JOptionPane.showMessageDialog(null, LabelGrabber.INSTANCE.getLabel("setup.oo.failed.text"), LabelGrabber.INSTANCE.getLabel("setup.oo.failed.title"), JOptionPane.WARNING_MESSAGE);
            }
        }
        else {
            LOGGER.log(Level.INFO, "Not setting up openoffice, option not selected");
        }
    }
    
}
