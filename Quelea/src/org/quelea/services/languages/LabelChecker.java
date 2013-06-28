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
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

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
        try(StringReader reader = new StringReader(Utils.getTextFromFile(langFile.getAbsolutePath(), ""))) {
            labels.load(reader);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        File englangFile = QueleaProperties.get().getEnglishLanguageFile();
        try(StringReader reader = new StringReader(Utils.getTextFromFile(englangFile.getAbsolutePath(), ""))) {
            engLabels.load(reader);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean compare() {
        boolean ok = true;
        boolean first = false;
        System.out.print("Checking \"" + name + "\"...");
        for(Object okey : engLabels.keySet()) {
            String key = (String) okey;
            String prop = labels.getProperty(key);
            if(prop == null) {
                ok = false;
                if(!first) {
                    first = true;
                    System.out.println();
                    System.err.println("MISSING LABELS:");
                }
                System.err.println(key + " (" + engLabels.getProperty(key) + ")");
            }
        }
        if(ok) {
            System.out.println("All good.");
        }
        return ok;
    }

    public static void main(String[] args) {
        boolean ok = true;
        for(File file : new File("languages").listFiles()) {
            if(!file.getName().equals("gb.lang")) { //Exclude english file since this is what we work from!
                boolean result = new LabelChecker(file.getName()).compare();
                if(!result) {
                    ok = false;
                }
            }
        }
        if(!ok) {
            System.err.println();
            System.err.println("WARNING: Some language files have missing labels. "
                    + "This is normal for intermediate builds and development releases, "
                    + "but for final releases this should be fixed if possible ."
                    + "Ideally find the original person who contributed the file "
                    + "and ask them to translate the missing labels, "
                    + "or if this isn't possible use Google Translate "
                    + "(only as a last resort though!)");
        }
    }
}
