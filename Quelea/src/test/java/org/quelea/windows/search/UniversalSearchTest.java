package org.quelea.windows.search;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UniversalSearchTest")
public class UniversalSearchTest {

    @Test
    @DisplayName("Ranks a whole title above one that starts with the query, then the rest")
    public void ranksTitles() {
        Assertions.assertEquals(0, UniversalSearch.rankTitle("Härlig är jorden", "härlig ÄR jorden"));
        Assertions.assertEquals(1, UniversalSearch.rankTitle("Härlig är jorden", "harlig"));
        Assertions.assertEquals(2, UniversalSearch.rankTitle("Så härlig är jorden", "härlig"));
    }

    @Test
    @DisplayName("Finds the line of the lyrics that matched")
    public void findsMatchingLine() {
        String lyrics = "Amazing grace\n  How sweet the sound\nThat saved a wretch";
        Assertions.assertEquals("How sweet the sound", UniversalSearch.findLine(lyrics, "SWEET"));
        Assertions.assertEquals("", UniversalSearch.findLine(lyrics, "river"));
    }

    @Test
    @DisplayName("Finds words in a row, whatever is between them")
    public void containsWordsInARow() {
        Assertions.assertTrue(UniversalSearch.containsWords("Through many dangers, toils and snares", "dangers toils"));
        Assertions.assertTrue(UniversalSearch.containsWords("Herren är min herde.", "HERREN ÄR MIN"));
        Assertions.assertFalse(UniversalSearch.containsWords("Through many dangers, toils and snares", "toils dangers"));
    }

    @Test
    @DisplayName("Scrolls the preview to the section that matched, otherwise the first")
    public void findsSectionToScrollTo() {
        List<String> sections = List.of("Amazing grace\nhow sweet the sound", "Through many dangers,\ntoils and snares");
        Assertions.assertEquals(1, UniversalSearch.findSection(sections, "DANGERS"));
        Assertions.assertEquals(1, UniversalSearch.findSection(sections, "dangers toils"));
        Assertions.assertEquals(0, UniversalSearch.findSection(sections, "newton"));
        Assertions.assertEquals(0, UniversalSearch.findSection(List.of(), "newton"));
    }

    @Test
    @DisplayName("Matches names word by word, in any order")
    public void matchesNames() {
        Assertions.assertTrue(UniversalSearch.matchesName("Sunrise at Easter.jpg", "easter sunrise"));
        Assertions.assertTrue(UniversalSearch.matchesName("Påsk.jpg", "pask"));
        Assertions.assertFalse(UniversalSearch.matchesName("Sunrise at Easter.jpg", "easter sunset"));
    }
}
