package org.quelea.data.bible;

import org.quelea.services.utils.LoggerUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BibleBookNameUtil {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static Map<Integer, List<String>> BOOK_NAME_STRATEGIES = new HashMap<>();

    static {
        BOOK_NAME_STRATEGIES.put(66, Arrays.asList("Genesis",
                "Exodus",
                "Leviticus",
                "Numbers",
                "Deuteronomy",
                "Joshua",
                "Judges",
                "Ruth",
                "1 Samuel",
                "2 Samuel",
                "1 Kings",
                "2 Kings",
                "1 Chronicles",
                "2 Chronicles",
                "Ezra",
                "Nehemiah",
                "Esther",
                "Job",
                "Psalms",
                "Proverbs",
                "Ecclesiastes",
                "Song of Solomon",
                "Isaiah",
                "Jeremiah",
                "Lamentations",
                "Ezekiel",
                "Daniel",
                "Hosea",
                "Joel",
                "Amos",
                "Obadiah",
                "Jonah",
                "Micah",
                "Nahum",
                "Habakkuk",
                "Zephaniah",
                "Haggai",
                "Zechariah",
                "Malachi",
                "Matthew",
                "Mark",
                "Luke",
                "John",
                "Acts",
                "Romans",
                "1 Corinthians",
                "2 Corinthians",
                "Galatians",
                "Ephesians",
                "Philippians",
                "Colossians",
                "1 Thessalonians",
                "2 Thessalonians",
                "1 Timothy",
                "2 Timothy",
                "Titus",
                "Philemon",
                "Hebrews",
                "James",
                "1 Peter",
                "2 Peter",
                "1 John",
                "2 John",
                "3 John",
                "Jude",
                "Revelation"));
    }

    public static String getBookNameForIndex(int index, int length) {
        LOGGER.log(Level.INFO, "Getting book name if possible: length " + length + " index " + index);
        List<String> bookNames = BOOK_NAME_STRATEGIES.get(length);
        if(bookNames==null) {
            LOGGER.log(Level.INFO, "No book names known for length" + length);
            return "Book " + (index+1);
        }
        else {
            return bookNames.get(index);
        }
    }
}
