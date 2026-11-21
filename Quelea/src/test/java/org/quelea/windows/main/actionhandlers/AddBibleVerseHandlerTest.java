package org.quelea.windows.main.actionhandlers;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.main.schedule.ScheduleList;

class AddBibleVerseHandlerTest {
    private static QueleaProperties previousProperties;
    private static Bible alpha;
    private static Bible beta;

    @BeforeAll
    static void setup() throws Exception {
        FxTestSupport.assumeAvailable();
        previousProperties = QueleaProperties.get();
        Path home = Files.createTempDirectory("quelea-add-verse-test");
        QueleaProperties.init(home.toString());
        Files.createDirectories(QueleaProperties.get().getBibleDir().toPath());
        writeBible("alpha", "Alpha");
        writeBible("beta", "Beta");
        BibleManager.get().loadBibles(false);
        alpha = BibleManager.get().getBibleFromName("Alpha");
        beta = BibleManager.get().getBibleFromName("Beta");
        Assertions.assertNotNull(alpha);
        Assertions.assertNotNull(beta);
    }

    @AfterAll
    static void restore() throws Exception {
        if (previousProperties != null) {
            Field instance = QueleaProperties.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, previousProperties);
            BibleManager.get().loadBibles(false);
        }
    }

    @Test
    void extendingOneOccurrencePreservesParallelVersionsAndTheOtherOccurrence() throws Exception {
        FxTestSupport.onFx(() -> {
            BibleVerse verse = alpha.getBookByNumber(1).getChapter(0).getVerse(1);
            BiblePassage first = new BiblePassage("Alpha", "Genesis 1:1", new BibleVerse[]{verse}, false)
                    .withVersions(List.of(alpha, beta));
            BiblePassage second = new BiblePassage("Alpha", "Genesis 1:1", new BibleVerse[]{verse}, false)
                    .withVersions(List.of(alpha, beta));
            ScheduleList schedule = new ScheduleList();
            schedule.add(first);
            schedule.add(second);

            new AddBibleVerseHandler().add(schedule, second);

            Assertions.assertSame(first, schedule.getItems().get(0).displayable());
            BiblePassage extended = (BiblePassage) schedule.getItems().get(1).displayable();
            Assertions.assertNotSame(second, extended);
            Assertions.assertEquals(List.of(alpha, beta), extended.getBibleVersions());
            Assertions.assertArrayEquals(new int[]{1, 2},
                    java.util.Arrays.stream(extended.getVerses()).mapToInt(BibleVerse::getNum).toArray());
            Assertions.assertEquals(1, schedule.getSelectionModel().getSelectedIndex());
            return null;
        });
    }

    private static void writeBible(String file, String name) throws Exception {
        String xml = "<XMLBIBLE biblename=\"" + name + "\"><BIBLEBOOK bnumber=\"1\" bname=\"Genesis\">"
                + "<CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">" + name + " one</VERS>"
                + "<VERS vnumber=\"2\">" + name + " two</VERS></CHAPTER></BIBLEBOOK></XMLBIBLE>";
        Files.writeString(QueleaProperties.get().getBibleDir().toPath().resolve(file + ".xml"), xml);
    }
}
