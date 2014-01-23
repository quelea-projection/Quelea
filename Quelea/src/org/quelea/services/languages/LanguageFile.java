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
import java.text.Collator;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * A language file used by Quelea to display interface labels.
 * <p/>
 * @author Michael
 */
public class LanguageFile implements Comparable<LanguageFile> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String languageName;
    private File languageFile;

    /**
     * Create a language file from a specified file.
     * <p/>
     * @param file the file to create the language file from.
     */
    public LanguageFile(File file) {
        this.languageFile = file;
        Properties props = new Properties();
        try(StringReader reader = new StringReader(Utils.getTextFromFile(file.getAbsolutePath(), ""))) {
            props.load(reader);
        }
        catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't read in language file as property file: " + file.getAbsolutePath(), ex);
        }
        languageName = props.getProperty("LANGUAGENAME");
        if(languageName == null || languageName.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "{0} doesn''t have a language name!", file.getAbsolutePath());
        }
    }

    /**
     * The name of the language represented by this file.
     */
    public String getLanguageName() {
        return languageName;
    }

    public File getFile() {
        return languageFile;
    }

    @Override
    public String toString() {
        return languageName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.languageFile);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final LanguageFile other = (LanguageFile) obj;
        if(!Objects.equals(this.languageFile, other.languageFile)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LanguageFile o) {
        Collator collator = Collator.getInstance();
        if(languageName == null && o.languageName == null) {
            return 0;
        }
        if(languageName == null) {
            return -1;
        }
        if(o.languageName == null) {
            return 1;
        }
        return collator.compare(languageName, o.languageName);
    }
}
