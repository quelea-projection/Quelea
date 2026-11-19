package org.quelea.services.lucene;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.BibleChapter;

@DisplayName("Bible search index")
public class BibleSearchIndexTest {
    @Test
    public void searchesChaptersAndFoldsAccents() throws Exception {
        BibleChapter first = chapter(1, "The Lord is my shepherd");
        BibleChapter second = chapter(2, "Gr\u00e2ce and peace");
        BibleSearchIndex index = new BibleSearchIndex();
        index.add(first);
        index.add(second);
        Assertions.assertArrayEquals(new BibleChapter[]{first}, index.filter("LORD is", null));
        Assertions.assertArrayEquals(new BibleChapter[]{second}, index.filter("grace", null));
        Assertions.assertEquals(2, index.size());
        Assertions.assertEquals(2, index.filter("", null).length);
    }

    @Test
    public void removeAndClearAlsoRemoveEntriesFromTheUnfilteredResults() throws Exception {
        BibleChapter first = chapter(1, "First");
        BibleChapter second = chapter(2, "Second");
        BibleSearchIndex index = new BibleSearchIndex();
        index.add(first);
        index.add(second);
        index.remove(first);
        Assertions.assertEquals(1, index.size());
        Assertions.assertArrayEquals(new BibleChapter[]{second}, index.filter("", null));
        index.clear();
        Assertions.assertEquals(0, index.size());
        Assertions.assertEquals(0, index.filter("", null).length);
    }

    private BibleChapter chapter(int number, String text) throws Exception {
        String xml = "<chapter cnumber=\"" + number + "\"><verse vnumber=\"1\">" + text + "</verse></chapter>";
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return BibleChapter.parseXML(document.getDocumentElement(), number);
    }
}
