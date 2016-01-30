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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registers the Quelea bundled fonts with the JVM.
 *
 * @author Michael
 */
public class FontInstaller {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Register the bundled fonts.
     */
    public void setupBundledFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for(File file : new File("icons/bundledfonts").listFiles()) {
            if(file.getName().toLowerCase().endsWith("otf") || file.getName().toLowerCase().endsWith("ttf")) {
                try {
                    javafx.scene.text.Font fxFont;
                    try(FileInputStream fis = new FileInputStream(file)) {
                        fxFont = javafx.scene.text.Font.loadFont(fis, 72);                        
                    }
                    if(fxFont == null) {
                        LOGGER.log(Level.WARNING, "Couldn't load font {0}", file.getAbsolutePath());
                    }
                    boolean geSuccess = ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, file));
                    if(geSuccess && fxFont != null) {
                        LOGGER.log(Level.INFO, "Loaded bundled font {0}", file.getAbsolutePath());
                    }
                }
                catch(FontFormatException | IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't load font " + file.getAbsolutePath(), ex);
                }
            }
        }
    }

}
