/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
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
package org.quelea.services.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks to see whether the user has the theme / properties / etc. files in
 * their home directory and if they don't, copies them across. This is useful
 * since the installer doesn't touch the user's directory, so if this is the
 * first time the user runs the program we want to copy across the default
 * settings.
 * <p>
 * @author Michael
 */
public class UserFileChecker {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private File userDir;

    /**
     * Create a new user file checker.
     * <p>
     * @param userDir the user directory to check.
     */
    public UserFileChecker(File userDir) {
        this.userDir = userDir;
    }

    /**
     * Check the user files to see if they exist, if they don't then copy them
     * over from the master files of the same name in the current directory.
     */
    public void checkUserFiles() {
        LOGGER.log(Level.INFO, "Checking user files");
        checkPropertiesFile();
        checkDir("themes");
        checkDir("vid");
        checkDir("img");
        checkDir("bibles");
        checkDir("dictionaries");
        checkDir("timer");
    }

    /**
     * Check the properties file to see if it needs to be copied.
     */
    private void checkPropertiesFile() {
        File file = new File(userDir, "quelea.properties");
        if(file.exists() && Utils.getTextFromFile(file.getAbsolutePath(), "error").trim().isEmpty()) {
            copyPropertiesFile();
        }
        else if(!file.exists()) {
            copyPropertiesFile();
        }
    }

    /**
     * Check a directory to see if it needs to be copied, copy it over if
     * necessary.
     * @param dirName the name of the directory to copy.
     */
    private void checkDir(String dirName) {
        File file = new File(userDir, dirName);
        if(!file.exists()) {
            copyDir(dirName);
        }
    }

    /**
     * Copy the theme directory over to the user's Quelea home directory.
     */
    private void copyDir(String dirName) {
        LOGGER.log(Level.INFO, "{0} dir doesn't exist in user home, copying", dirName);
        File masterThemes = new File(dirName);
        if(masterThemes.exists()) {
            try {
                Utils.copyFile(masterThemes, new File(userDir, dirName));
                LOGGER.log(Level.INFO, "{0} copied successfully", dirName);
            }
            catch(IOException ex) {
                LOGGER.log(Level.INFO, "Couldn't copy " + dirName + ", creating instead", ex);
                boolean created = new File(userDir, dirName).mkdir();
                if(!created) {
                    LOGGER.log(Level.WARNING, "Can't copy, can't create.", ex);
                }
            }
        }
        else {
            LOGGER.log(Level.WARNING, "{0} dir {1} doesn't exist", new Object[]{dirName, masterThemes.getAbsolutePath()});
            new File(userDir, dirName).mkdir();
        }
    }

    /**
     * Copy the properties file over to the user's Quelea home directory.
     */
    private void copyPropertiesFile() {
        LOGGER.log(Level.INFO, "Properties file doesn't exist in user home, copying");
        File masterProps = new File("quelea.properties");
        if(masterProps.exists()) {
            try {
                Utils.copyFile(masterProps, new File(userDir, "quelea.properties"));
                LOGGER.log(Level.INFO, "Properties file copied successfully");
            }
            catch(IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't copy properties file", ex);
            }
        }
        else {
            LOGGER.log(Level.WARNING, "Master properties file {0} doesn't exist", masterProps.getAbsolutePath());
        }
    }
}
