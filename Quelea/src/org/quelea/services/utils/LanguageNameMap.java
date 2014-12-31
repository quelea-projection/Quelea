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
        putNativeLanguages();
        putEnglishLanguages();
        putSwedishLanguages();
    }
    
    /**
     * Put names of the languages (in Swedish) into the map.
     */
    private void putSwedishLanguages() {
        map.put("arabiska", Language.ARABIC);
        map.put("bulgariska", Language.BULGARIAN);
        map.put("katalanska", Language.CATALAN);
        map.put("kinesiska", Language.CHINESE_SIMPLIFIED);
        map.put("mandarin", Language.CHINESE_SIMPLIFIED);
        map.put("standardkinesiska", Language.CHINESE_SIMPLIFIED);
        map.put("tjeckiska", Language.CZECH);
        map.put("danska", Language.DANISH);
        map.put("nederlänska", Language.DUTCH);
        map.put("hollänska", Language.DUTCH);
        map.put("engelska", Language.ENGLISH);
        map.put("estlänska", Language.ESTONIAN);
        map.put("finska", Language.FINNISH);
        map.put("franska", Language.FRENCH);
        map.put("tyska", Language.GERMAN);
        map.put("grekiska", Language.GREEK);
        map.put("hebreiska", Language.HEBREW);
        map.put("hindi", Language.HINDI);
        map.put("ungerska", Language.HUNGARIAN);
        map.put("indonesiska", Language.INDONESIAN);
        map.put("italienska", Language.ITALIAN);
        map.put("japanska", Language.JAPANESE);
        map.put("koreanska", Language.KOREAN);
        map.put("lettiska", Language.LATVIAN);
        map.put("litauiska", Language.LITHUANIAN);
        map.put("malajiska", Language.MALAY);
        map.put("norska", Language.NORWEGIAN);
        map.put("persiska", Language.PERSIAN);
        map.put("polska", Language.POLISH);
        map.put("portugisiska", Language.PORTUGUESE);
        map.put("rumänska", Language.ROMANIAN);
        map.put("slovakiska", Language.SLOVAK);
        map.put("ryska", Language.RUSSIAN);
        map.put("slovenska", Language.SLOVENIAN);
        map.put("spanska", Language.SPANISH);
        map.put("svenska", Language.SWEDISH);
        map.put("thailändska", Language.THAI);
        map.put("turkiska", Language.TURKISH);
        map.put("urdu", Language.URDU);
        map.put("ukrainska", Language.UKRAINIAN);
        map.put("vietnamesiska", Language.VIETNAMESE);
    }

    /**
     * Put names of the languages (in English) into the map.
     */
    private void putEnglishLanguages() {
        map.put("arabic", Language.ARABIC);
        map.put("bulgarian", Language.BULGARIAN);
        map.put("catalan", Language.CATALAN);
        map.put("chinese", Language.CHINESE_SIMPLIFIED);
        map.put("mandarin", Language.CHINESE_SIMPLIFIED);
        map.put("mandarin chinese", Language.CHINESE_SIMPLIFIED);
        map.put("czech", Language.CZECH);
        map.put("danish", Language.DANISH);
        map.put("dutch", Language.DUTCH);
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
        map.put("slovak", Language.SLOVAK);
        map.put("russian", Language.RUSSIAN);
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
     * Put names of the languages (in their native language) into the map.
     */
    private void putNativeLanguages() {
        map.put("العربية", Language.ARABIC);
        map.put("български", Language.BULGARIAN);
        map.put("català", Language.CATALAN);
        map.put("官话", Language.CHINESE_SIMPLIFIED);
        map.put("čeština", Language.CZECH);
        map.put("dansk", Language.DANISH);
        map.put("nederlands", Language.DUTCH);
        map.put("english", Language.ENGLISH);
        map.put("eesti", Language.ESTONIAN);
        map.put("suomalainen", Language.FINNISH);
        map.put("français", Language.FRENCH);
        map.put("deutsch", Language.GERMAN);
        map.put("ελληνικά", Language.GREEK);
        map.put("עברית", Language.HEBREW);
        map.put("हिंदी", Language.HINDI);
        map.put("magyar", Language.HUNGARIAN);
        map.put("indonesia", Language.INDONESIAN);
        map.put("italiano", Language.ITALIAN);
        map.put("日本の", Language.JAPANESE);
        map.put("한국의", Language.KOREAN);
        map.put("latvijas", Language.LATVIAN);
        map.put("lietuvos", Language.LITHUANIAN);
        map.put("melayu", Language.MALAY);
        map.put("norsk", Language.NORWEGIAN);
        map.put("فارسی", Language.PERSIAN);
        map.put("polski", Language.POLISH);
        map.put("português", Language.PORTUGUESE);
        map.put("român", Language.ROMANIAN);
        map.put("русский", Language.RUSSIAN);
        map.put("slovenská", Language.SLOVAK);
        map.put("slovenščina", Language.SLOVENIAN);
        map.put("español", Language.SPANISH);
        map.put("svenska", Language.SWEDISH);
        map.put("ภาษาไทย", Language.THAI);
        map.put("türk", Language.TURKISH);
        map.put("اردو", Language.URDU);
        map.put("український", Language.UKRAINIAN);
        map.put("việt", Language.VIETNAMESE);
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
