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
package org.quelea.services.languages;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Utility class to loosely convert the default British English languages file
 * to a separate American English languages file. There's only a few minor
 * changes, so this can probably be done automatically.
 * <p/>
 * @author Michael
 */
public class USEnglishConverter {

    public static void main(String[] args) {
        try {
            System.out.println("Converting language file to American English...");
            Properties gb = new Properties();
            gb.load(new FileReader("languages/gb.lang"));
            Properties usa = new Properties();
            for(Object k : gb.keySet()) {
                String key = (String) k;
                usa.setProperty(key, translateToUS(gb.getProperty(key)));
            }
            usa.setProperty("LANGUAGENAME", "English (US)");
            usa.store(new FileWriter("languages/us.lang"), "THIS IS AN AUTO GENERATED FILE, AND WILL BE OVERWRITTEN EACH TIME QUELEA IS REBUILT. DO NOT EDIT MANUALLY!");
            System.out.println("Successfully converted language file to American English.");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Translate a string from GB to US english. At present, just deals with
     * colour because I think that's the only difference we have...
     * <p/>
     * @param gb the string in GB english
     * @return the string in US engligh
     */
    private static String translateToUS(final String gb) {
        String us = gb;
        us = Pattern.compile("colour").matcher(us).replaceAll("color");
        us = Pattern.compile("Colour").matcher(us).replaceAll("Color");
        us = Pattern.compile("sanitise").matcher(us).replaceAll("sanitize");
        us = Pattern.compile("Sanitise").matcher(us).replaceAll("Sanitize");
        us = Pattern.compile("initialise").matcher(us).replaceAll("initialize");
        us = Pattern.compile("Initialise").matcher(us).replaceAll("Initialize");
        us = Pattern.compile("capitalise").matcher(us).replaceAll("capitalize");
        us = Pattern.compile("Capitalise").matcher(us).replaceAll("Capitalize");
        if(!us.equals(gb)) {
            System.out.println("Translated \"" + gb + "\" ==> \"" + us + "\"");
        }
        return us;
    }
}
