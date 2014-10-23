/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Check that the images in the "img" folder conform to the requirements. At the
 * moment this just involves checking they're the right size, but could be
 * expanded later to check other things like constrast and colour matching as
 * well.
 *
 * @author Michael
 */
public class ImageChecker {

    /**
     * The height that all the images should be.
     */
    private static final int HEIGHT = 1080;
    /**
     * The width that all the images should be.
     */
    private static final int WIDTH = 1920;
    /**
     * Only formats (file extensions) specified in this array are permitted.
     */
    private static final String[] ALLOWED_FORMATS = {"png", "jpg"};
    /**
     * The image directory to check.
     */
    private File imageDir;

    /**
     * Create a new image checker to check the specified directory.
     *
     * @param imageDir the directory to check.
     */
    public ImageChecker(File imageDir) {
        this.imageDir = imageDir;
    }

    /**
     * Run the image checker.
     */
    public void runCheck() {
        boolean ok = true;
        System.out.println("\nChecking images:");
        List<String> badFileNames = new ArrayList<>();
        for(File file : imageDir.listFiles()) {
            if(Utils.fileIsImage(file)) {
                boolean thisok = true;
                BufferedImage image=null;
                try {
                    image = ImageIO.read(file);
                }
                catch(IOException ex) {
                    Logger.getLogger(ImageChecker.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(image == null) {
                    System.err.println("ERROR: " + file.getName() + " appears to be corrupt.");
                    thisok = false;
                }
                else { //If not corrupt
                    if(!formatOK(file.getName())) {
                        System.err.println("ERROR: " + file.getName() + " is not in a required format. It must be in one of the following: " + getFormatsString());
                        thisok = false;
                    }
                    if(image.getWidth() != WIDTH) {
                        System.err.println("ERROR: " + file.getName() + " width should be " + WIDTH + " but is " + image.getWidth());
                        thisok = false;
                    }
                    if(image.getHeight() != HEIGHT) {
                        System.err.println("ERROR: " + file.getName() + " height should be " + HEIGHT + " but is " + image.getHeight());
                        thisok = false;
                    }
                }

                if(!thisok) { //If something is wrong with the image
                    ok = false;
                    if(!badFileNames.contains(file.getName())) {
                        badFileNames.add(file.getName());
                    }
                }
            }
            else {
                System.out.println("Skipping " + file.getName() + ", not an image file");
            }
        }
        System.out.println();
        if(ok) {
            System.out.println("All images ok.\n");
        }
        else {
            System.err.println("Some images don't conform to the requirements:");
            for(String name : badFileNames) {
                System.err.println(name);
            }
            System.err.println("Please read IMAGE GUIDELINES.txt in the \"img\" folder, correct these images according to the guidelines and then try again.\n");
            System.exit(1);
        }
    }

    /**
     * Get a single string of all the formats we allow. Enables easy printing.
     *
     * @return a single string of all the formats we allow.
     */
    private static String getFormatsString() {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < ALLOWED_FORMATS.length; i++) {
            ret.append(ALLOWED_FORMATS[i]);
            if(i != ALLOWED_FORMATS.length - 1) {
                ret.append(", ");
            }
        }
        return ret.toString();
    }

    /**
     * Determine if the given file name is in a format we accept.
     *
     * @param fileName the file name to check.
     * @return true if it's in an accepted format, false otherwise.
     */
    private static boolean formatOK(String fileName) {
        String suffix = fileName.split("\\.")[fileName.split("\\.").length - 1].toLowerCase().trim();
        for(String format : ALLOWED_FORMATS) {
            if(suffix.equals(format.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Run the image checker. If an argument is specified this will be used as
     * the image directory to check, otherwise the default "img" directory will
     * be chosen.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        String dir = "img";
        if(args.length > 0) {
            dir = args[0];
        }
        new ImageChecker(new File(dir)).runCheck();
    }
}
