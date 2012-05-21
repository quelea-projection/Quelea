/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.LoggerUtils;

/**
 * Sets the relevant path to the native RXTX library. Used for anything that
 * writes to the serial port - such as controlling a projector from the PC.
 *
 * @author Michael
 */
public class RXTXPathSetter {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Modify java.library.path to include the path to the appropriate RXTX
     * library - differs based on the OS, but here we cover Windows, Mac and
     * Linux, both 32 and 64 bit for each system.
     *
     * @return true if the path was set successfully, false otherwise.
     */
    public static boolean setPaths() {
        String s;
        if(System.getProperty("os.name").startsWith("Windows")) {
            if(is64Bit()) {
                s = "lib/nativerxtx/Windows64";
            }
            else {
                s = "lib/nativerxtx/Windows";
            }
        }
        else if(System.getProperty("os.name").startsWith("Mac")) {
            s = "lib/nativerxtx/Mac_OS_X";
        }
        else if(System.getProperty("os.name").startsWith("Linux")) {
            if(is64Bit()) {
                s = "lib/nativerxtx/Linux/x86_64";
            }
            else {
                s = "lib/nativerxtx/Linux/i686";
            }
        }
        else {
            LOGGER.log(Level.WARNING, "Couldn''t detect OS version for RXTX initialisation: {0}", System.getProperty("os.name"));
            return false;
        }
        System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + s);
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        }
        catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.log(Level.WARNING, "Couldn't initialise RXTX native path.", ex);
            return false;
        }
        LOGGER.log(Level.INFO, "Initialised RXTX Java path successfully for {0}. java.library.path is now \"{1}\"", new Object[]{System.getProperty("os.name"), System.getProperty("java.library.path")});
        return true;
    }

    /**
     * Determine if the current OS is 64 bit.
     *
     * @return true if it's 64 bit, false if 32.
     */
    private static boolean is64Bit() {
        return System.getProperty("os.arch").contains("64");
    }
}
