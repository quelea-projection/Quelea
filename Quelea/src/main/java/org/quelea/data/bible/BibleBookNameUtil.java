package org.quelea.data.bible;

import org.quelea.services.languages.LabelGrabber;
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
        BOOK_NAME_STRATEGIES.put(66, Arrays.asList(
            LabelGrabber.INSTANCE.getLabel("bible.book.genesis"),
            LabelGrabber.INSTANCE.getLabel("bible.book.exodus"),
            LabelGrabber.INSTANCE.getLabel("bible.book.leviticus"),
            LabelGrabber.INSTANCE.getLabel("bible.book.numbers"),
            LabelGrabber.INSTANCE.getLabel("bible.book.deuteronomy"),
            LabelGrabber.INSTANCE.getLabel("bible.book.joshua"),
            LabelGrabber.INSTANCE.getLabel("bible.book.judges"),
            LabelGrabber.INSTANCE.getLabel("bible.book.ruth"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1samuel"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2samuel"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1kings"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2kings"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1chronicles"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2chronicles"),
            LabelGrabber.INSTANCE.getLabel("bible.book.ezra"),
            LabelGrabber.INSTANCE.getLabel("bible.book.nehemiah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.esther"),
            LabelGrabber.INSTANCE.getLabel("bible.book.job"),
            LabelGrabber.INSTANCE.getLabel("bible.book.psalms"),
            LabelGrabber.INSTANCE.getLabel("bible.book.proverbs"),
            LabelGrabber.INSTANCE.getLabel("bible.book.ecclesiastes"),
            LabelGrabber.INSTANCE.getLabel("bible.book.songofsolomon"),
            LabelGrabber.INSTANCE.getLabel("bible.book.isaiah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.jeremiah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.lamentations"),
            LabelGrabber.INSTANCE.getLabel("bible.book.ezekiel"),
            LabelGrabber.INSTANCE.getLabel("bible.book.daniel"),
            LabelGrabber.INSTANCE.getLabel("bible.book.hosea"),
            LabelGrabber.INSTANCE.getLabel("bible.book.joel"),
            LabelGrabber.INSTANCE.getLabel("bible.book.amos"),
            LabelGrabber.INSTANCE.getLabel("bible.book.obadiah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.jonah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.micah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.nahum"),
            LabelGrabber.INSTANCE.getLabel("bible.book.habakkuk"),
            LabelGrabber.INSTANCE.getLabel("bible.book.zephaniah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.haggai"),
            LabelGrabber.INSTANCE.getLabel("bible.book.zechariah"),
            LabelGrabber.INSTANCE.getLabel("bible.book.malachi"),
            LabelGrabber.INSTANCE.getLabel("bible.book.matthew"),
            LabelGrabber.INSTANCE.getLabel("bible.book.mark"),
            LabelGrabber.INSTANCE.getLabel("bible.book.luke"),
            LabelGrabber.INSTANCE.getLabel("bible.book.john"),
            LabelGrabber.INSTANCE.getLabel("bible.book.acts"),
            LabelGrabber.INSTANCE.getLabel("bible.book.romans"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1corinthians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2corinthians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.galatians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.ephesians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.philippians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.colossians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1thessalonians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2thessalonians"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1timothy"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2timothy"),
            LabelGrabber.INSTANCE.getLabel("bible.book.titus"),
            LabelGrabber.INSTANCE.getLabel("bible.book.philemon"),
            LabelGrabber.INSTANCE.getLabel("bible.book.hebrews"),
            LabelGrabber.INSTANCE.getLabel("bible.book.james"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1peter"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2peter"),
            LabelGrabber.INSTANCE.getLabel("bible.book.1john"),
            LabelGrabber.INSTANCE.getLabel("bible.book.2john"),
            LabelGrabber.INSTANCE.getLabel("bible.book.3john"),
            LabelGrabber.INSTANCE.getLabel("bible.book.jude"),
            LabelGrabber.INSTANCE.getLabel("bible.book.revelation")
        ));
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
