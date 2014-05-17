/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.utils;

import com.memetix.mst.language.Language;
import java.util.HashMap;

/**
 * Provides a map between common language names and the actual language objects
 * used by the translator API.
 *
 * @author Michael
 */
public class LanguageNameMap {

    public static final LanguageNameMap INSTANCE = new LanguageNameMap();
    private final HashMap<String, Language> map;

    /**
     * Initialise the mappings.
     */
    private LanguageNameMap() {
        map = new HashMap<>();
        map.put("arabic", Language.ARABIC);
        map.put("bulgarian", Language.BULGARIAN);
        map.put("catalan", Language.CATALAN);
        map.put("chinese", Language.CHINESE_SIMPLIFIED);
        map.put("czech", Language.CZECH);
        map.put("danish", Language.DANISH);
        map.put("dutch", Language.DUTCH);
        map.put("english", Language.ENGLISH);
        map.put("estonian", Language.ESTONIAN);
        map.put("finnish", Language.FINNISH);
        map.put("french", Language.FRENCH);
        map.put("german", Language.GERMAN);
        map.put("greek", Language.GREEK);
        map.put("hebrew", Language.HEBREW);
        map.put("hindi", Language.HINDI);
        map.put("hungarian", Language.HUNGARIAN);
        map.put("indonesian", Language.INDONESIAN);
        map.put("italian", Language.ITALIAN);
        map.put("japanese", Language.JAPANESE);
        map.put("korean", Language.KOREAN);
        map.put("latvian", Language.LATVIAN);
        map.put("lithuanian", Language.LITHUANIAN);
        map.put("malay", Language.MALAY);
        map.put("norwegian", Language.NORWEGIAN);
        map.put("persian", Language.PERSIAN);
        map.put("polish", Language.POLISH);
        map.put("portuguese", Language.PORTUGUESE);
        map.put("romanian", Language.ROMANIAN);
        map.put("russian", Language.RUSSIAN);
        map.put("slovak", Language.SLOVAK);
        map.put("slovenian", Language.SLOVENIAN);
        map.put("spanish", Language.SPANISH);
        map.put("swedish", Language.SWEDISH);
        map.put("thai", Language.THAI);
        map.put("turkish", Language.TURKISH);
        map.put("urdu", Language.URDU);
        map.put("ukranian", Language.UKRAINIAN);
        map.put("vietnamese", Language.VIETNAMESE);
    }

    /**
     * Get a particular language from text.
     *
     * @param text the text representing the language.
     * @return the language, or null if none was found.
     */
    public Language getLanguage(String text) {
        Language lang = map.get(text.toLowerCase());
        if (lang == null) {
            return Language.fromString(text);
        } else {
            return lang;
        }
    }

}
