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
import java.util.Set;
import java.util.TreeSet;
import org.quelea.services.utils.QueleaProperties;

/**
 * Knows about all language files currently available in Quelea.
 * <p>
 * @author Michael
 */
public class LanguageFileManager {

    public static final LanguageFileManager INSTANCE = new LanguageFileManager();
    private Set<LanguageFile> languageFiles;

    private LanguageFileManager() {
        languageFiles = new TreeSet<>();
        for(File file : new File("languages").listFiles()) {
            if(file.getName().endsWith(".lang")) {
                languageFiles.add(new LanguageFile(file));
            }
        }
    }

    /**
     * Get the language file Quelea is currently using.
     * <p>
     * @return
     */
    public LanguageFile getCurrentFile() {
        return new LanguageFile(QueleaProperties.get().getLanguageFile());
    }

    /**
     * Get a set of all language files currently available to Quelea.
     * <p>
     * @return the set of language files.
     */
    public Set<LanguageFile> languageFiles() {
        return new TreeSet<>(languageFiles);
    }

}
