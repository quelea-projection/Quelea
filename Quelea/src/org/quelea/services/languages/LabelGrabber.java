/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by * the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.languages;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * Responsible for grabbing the appropriate labels from the current langauges
 * file based on keys.
 * <p/>
 * @author Michael
 */
public class LabelGrabber extends ResourceBundle {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final LabelGrabber INSTANCE = new LabelGrabber();
    private Properties labels;
    private Properties engLabels;
    private boolean english;

    /**
     * Create the label grabber.
     */
    private LabelGrabber() {
        labels = new Properties();
        engLabels = new Properties();
        File langFile = QueleaProperties.get().getLanguageFile();
        if (langFile == null) {
            LOGGER.log(Level.SEVERE, "Couldn't load languages file, file was null");
            return;
        }
        LOGGER.log(Level.INFO, "Using languages file {0}", langFile.getAbsolutePath());
        try (StringReader reader = new StringReader(Utils.getTextFromFile(langFile.getAbsolutePath(), ""))) {
            labels.load(reader);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't load languages file", ex);
        }
        if (langFile.getName().equals("gb.lang")) {
            LOGGER.log(Level.INFO, "Setting en-uk locale");
            Locale.setDefault(Locale.UK);
        } else {
            LOGGER.log(Level.INFO, "Setting {0} locale", labels.getProperty("LOCALE", "en"));
            Locale.setDefault(new Locale(labels.getProperty("LOCALE", "en")));
        }

        if (langFile.getName().equals("gb.lang")) {
            english = true;
        } else {
            english = false;
            File englangFile = QueleaProperties.get().getEnglishLanguageFile();
            if (englangFile == null) {
                LOGGER.log(Level.SEVERE, "No english language file!");
            } else {
                try (StringReader reader = new StringReader(Utils.getTextFromFile(englangFile.getAbsolutePath(), ""))) {
                    engLabels.load(reader);
                    LOGGER.log(Level.INFO, "Using english language file as backup");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Couldn't load english language file", ex);
                }
            }
        }
    }

    /**
     * Determine if a particular key has an entry in the currently selected
     * language.
     *
     * @param key the label key to check.
     * @return true if a definition exists, false otherwise.
     */
    public boolean isLocallyDefined(String key) {
        return labels.getProperty(key) != null;
    }

    /**
     * Get a label from the language file.
     * <p/>
     * @param key the key to use to get the label.
     * @return the textual string in the appropriate language.
     */
    public String getLabel(String key) {
        String ret = labels.getProperty(key);
        if (ret == null) {
            ret = engLabels.getProperty(key);
            if (english) { //Only a warning for the default english lang file - others will probably have stuff missing.
                LOGGER.log(Level.WARNING, "Missing label in language file: {0}", key);
            }
            if (ret == null) {
                return key;
            }
        }
        return ret;
    }
    
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }
    
    @Override
    public Object handleGetObject(String key) {
        return getLabel(key);
    }
    
    // Overrides handleKeySet() so that the getKeys() implementation
    // can rely on the keySet() value.
    @Override
    protected Set<String> handleKeySet() {
        Set<String> result = labels.stringPropertyNames();
        Set<String> resultEng = engLabels.stringPropertyNames();
        Set<String> ret = new HashSet<>();
        ret.addAll(result);
        ret.addAll(resultEng);
        return ret;
    }
}
