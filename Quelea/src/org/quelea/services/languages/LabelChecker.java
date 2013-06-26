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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.quelea.services.utils.QueleaProperties;

/**
 * Run as a standalone script - checks to see whether the language files are
 * complete, and for those that aren't shows the missing labels that need
 * translating.
 * <p/>
 * @author Michael
 */
public class LabelChecker {

    private Properties labels;
    private Properties engLabels;
    private String name;

    public LabelChecker(String name) {
        this.name = name;
        labels = new Properties();
        engLabels = new Properties();
        File langFile = new File("languages", name);
        try(InputStream stream = new FileInputStream(langFile)) {
            labels.load(stream);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        File englangFile = QueleaProperties.get().getEnglishLanguageFile();
        try(InputStream stream = new FileInputStream(englangFile)) {
            engLabels.load(stream);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void compare() {
        boolean ok = true;
        System.err.println("File: " + name);
        for(Object okey : engLabels.keySet()) {
            String key = (String) okey;
            String prop = labels.getProperty(key);
            if(prop == null) {
                ok = false;
                System.err.println("MISSING: " + key + " (" + engLabels.getProperty(key) + ")");
            }
        }
        if(ok) {
            System.err.println("ALL LABELS OK.");
        }
    }

    public static void main(String[] args) {
        for(File file : new File("languages").listFiles()) {
            if(!file.getName().equals("gb.lang")) { //Exclude english file since this is what we work from!
                new LabelChecker(file.getName()).compare();
            }
        }
    }
}
