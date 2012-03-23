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
package org.quelea.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
     * Run the image checker.
     *
     * @param args command line arguments (not used.)
     */
    public static void main(String[] args) {
        boolean ok = true;
        System.out.println("\nChecking images:");
        List<String> badFileNames = new ArrayList<>();
        for(File file : new File("img").listFiles()) {
            if(Utils.fileIsImage(file)) {
                BufferedImage image = Utils.getImage(file.getAbsolutePath());
                if(image.getWidth() != WIDTH) {
                    System.err.println("ERROR: " + file.getName() + " width should be " + WIDTH + " but is " + image.getWidth());
                    if(!badFileNames.contains(file.getName())) {
                        badFileNames.add(file.getName());
                    }
                    ok = false;
                }
                if(image.getHeight() != HEIGHT) {
                    System.err.println("ERROR: " + file.getName() + " height should be " + HEIGHT + " but is " + image.getHeight());
                    if(!badFileNames.contains(file.getName())) {
                        badFileNames.add(file.getName());
                    }
                    ok = false;
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
}
