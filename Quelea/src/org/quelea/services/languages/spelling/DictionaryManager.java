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
package org.quelea.services.languages.spelling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Michael
 */
public class DictionaryManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final DictionaryManager INSTANCE = new DictionaryManager();
    private Map<String, Dictionary> dictionaries;
    private Properties props = new Properties();

    private DictionaryManager() {
        try(StringReader reader = new StringReader(Utils.getTextFromFile(new File(QueleaProperties.get().getDictionaryDir(), "dictionaries.properties").getAbsolutePath(), ""))) {
            props.load(reader);
        }
        catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't load dictionaries", ex);
        }
        dictionaries = new HashMap<>();
        Speller init = new Speller(null);
        for(String propName : props.stringPropertyNames()) {
            File file = new File(QueleaProperties.get().getDictionaryDir(), propName);
            if(!file.exists()) {
                continue;
            }
            Dictionary dict = new Dictionary(props.getProperty(propName), file);
            dictionaries.put(propName, dict);
            init.setDictionary(dict);
        }
    }

    public Set<Dictionary> getDictionaries() {
        return new HashSet<>(dictionaries.values());
    }

    public Dictionary getFromFilename(String name) {
        return dictionaries.get(name);
    }
}
