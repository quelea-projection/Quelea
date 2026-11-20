package org.quelea.services.lucene;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Song search index")
public class SongSearchIndexTest {
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-search-test").toString());
        }
    }

    @Test
    public void searchesTitleAuthorAndLyricsIgnoringCaseAndAccents() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable first = song(1, "H\u00e4rlig \u00e4r jorden", "Jane Writer", "Rivers of mercy");
        SongDisplayable second = song(2, "Another song", "John Smith", "Peace on earth");
        index.addAll(List.of(first, second));
        Assertions.assertArrayEquals(new SongDisplayable[]{first}, index.filter("HARLIG", SearchIndex.FilterType.TITLE));
        Assertions.assertArrayEquals(new SongDisplayable[]{first}, index.filter("jane", SearchIndex.FilterType.AUTHOR));
        Assertions.assertArrayEquals(new SongDisplayable[]{second}, index.filter("peace on", SearchIndex.FilterType.BODY));
        Assertions.assertEquals(2, index.filter("", SearchIndex.FilterType.TITLE).length);
        Assertions.assertEquals(0, index.filter("no-such-song", SearchIndex.FilterType.TITLE).length);
    }

    @Test
    public void searchesSafelyWithPunctuationAndAccents() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable song = song(21, "Caf\u00e9 (Morning)", "Author", "Words");
        index.add(song);
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("Caf\u00e9 (Morning)", SearchIndex.FilterType.TITLE));
        Assertions.assertEquals(1, index.filter("***", SearchIndex.FilterType.TITLE).length);
    }

    @Test
    public void searchesCanonicalUnicodeVariantsEqually() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable song = song(21, "Caf\u00e9", "Author", "Words");
        index.add(song);
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("cafe\u0301", SearchIndex.FilterType.TITLE));
        SongSearchIndex reverse = new SongSearchIndex();
        SongDisplayable decomposed = song(22, "Cafe\u0301", "Ma\u0301rta", "The cafe\u0301 is open");
        reverse.add(decomposed);
        Assertions.assertArrayEquals(new SongDisplayable[]{decomposed}, reverse.filter("Caf\u00e9", SearchIndex.FilterType.TITLE));
        Assertions.assertArrayEquals(new SongDisplayable[]{decomposed}, reverse.filter("M\u00e1rta", SearchIndex.FilterType.AUTHOR));
        Assertions.assertArrayEquals(new SongDisplayable[]{decomposed}, reverse.filter("caf\u00e9 is", SearchIndex.FilterType.BODY));
    }

    @Test
    public void pastedWhitespaceBetweenWordsActsLikeASpace() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable song = song(22, "Amazing Grace", "Author", "Words");
        index.add(song);
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("Amazing\nGrace", SearchIndex.FilterType.TITLE));
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("Amazing\tGrace", SearchIndex.FilterType.TITLE));
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("Amazing\u00a0Grace", SearchIndex.FilterType.TITLE));
    }

    @Test
    public void updateRemoveAndClearChangeSearchResults() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable song = song(10, "Morning", "Author", "Words");
        index.add(song);
        song.setTitle("Evening");
        index.update(song);
        Assertions.assertEquals(0, index.filter("morning", SearchIndex.FilterType.TITLE).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{song}, index.filter("evening", SearchIndex.FilterType.TITLE));
        index.remove(song);
        Assertions.assertEquals(0, index.size());
        index.add(song);
        index.clear();
        Assertions.assertEquals(0, index.size());
        Assertions.assertEquals(0, index.filter("evening", SearchIndex.FilterType.TITLE).length);
    }

    @Test
    public void addingAReplacementForAnExistingIdRemovesStaleSearchTerms() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable oldSong = song(5, "Morning", "Author", "Old words");
        SongDisplayable replacement = song(5, "Evening", "Author", "New words");
        index.add(oldSong);
        index.add(replacement);
        Assertions.assertEquals(1, index.size());
        Assertions.assertEquals(0, index.filter("morning", SearchIndex.FilterType.TITLE).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{replacement}, index.filter("evening", SearchIndex.FilterType.TITLE));
    }

    @Test
    public void replacementAlsoWorksForNegativeIds() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable first = song(-1, "Morning", "Author", "Old words");
        SongDisplayable replacement = song(-1, "Evening", "Author", "New words");
        index.add(first);
        index.add(replacement);
        Assertions.assertEquals(0, index.filter("morning", SearchIndex.FilterType.TITLE).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{replacement}, index.filter("evening", SearchIndex.FilterType.TITLE));
        index.remove(replacement);
        index.add(song(9, "Different", "Author", "Words"));
        Assertions.assertEquals(0, index.filter("evening", SearchIndex.FilterType.TITLE).length);
    }

    @Test
    public void searchesCcliNumbersByPrefixAndAcceptsCommonFormatting() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable first = song(1, "Grace", "Jane", "Words");
        SongDisplayable second = song(2, "Hope", "John", "Words");
        SongDisplayable empty = song(3, "Empty", "Author", "Words");
        first.setCcli("CCLI #1,234,567");
        second.setCcli("001234567890123456789");
        empty.setCcli(null);
        index.addAll(List.of(first, second, empty));
        for (String query : List.of("1234567", "1234", "1,234,567", "CCLI #1234567", "ccli: 1234")) {
            Assertions.assertArrayEquals(new SongDisplayable[]{first}, index.filter(query, SearchIndex.FilterType.CCLI), query);
        }
        Assertions.assertArrayEquals(new SongDisplayable[]{second}, index.filter("001234567890123456789", SearchIndex.FilterType.CCLI));
        Assertions.assertEquals(0, index.filter("234567", SearchIndex.FilterType.CCLI).length);
        Assertions.assertEquals(0, index.filter("grace 1234", SearchIndex.FilterType.CCLI).length);
        Assertions.assertEquals(0, index.filter("!!!", SearchIndex.FilterType.CCLI).length);
        Assertions.assertEquals(3, index.filter("", SearchIndex.FilterType.CCLI).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{first}, index.filter("grace", SearchIndex.FilterType.TITLE));
        Assertions.assertEquals(0, index.filter("1234567", SearchIndex.FilterType.TITLE).length);
    }

    @Test
    public void updatingReplacingRemovingAndClearingCcliNumbersUpdatesTheIndex() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable original = song(10, "Song", "Author", "Words");
        original.setCcli("1234567");
        index.add(original);
        original.setCcli("7654321");
        index.update(original);
        Assertions.assertEquals(0, index.filter("1234567", SearchIndex.FilterType.CCLI).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{original}, index.filter("7654321", SearchIndex.FilterType.CCLI));
        SongDisplayable replacement = song(10, "Song", "Author", "Words");
        replacement.setCcli("5555555");
        index.add(replacement);
        Assertions.assertEquals(0, index.filter("7654321", SearchIndex.FilterType.CCLI).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{replacement}, index.filter("5555555", SearchIndex.FilterType.CCLI));
        index.remove(replacement);
        Assertions.assertEquals(0, index.filter("5555555", SearchIndex.FilterType.CCLI).length);
        index.add(replacement);
        index.clear();
        Assertions.assertEquals(0, index.filter("5555555", SearchIndex.FilterType.CCLI).length);
    }

    @Test
    public void hiddenDuplicatesAreSearchableOnlyByCcliAndCanBePromoted() {
        SongSearchIndex index = new SongSearchIndex();
        SongDisplayable visible = song(1, "Twin", "Author", "Words");
        SongDisplayable hidden = song(2, "Twin", "Author", "Words");
        visible.setCcli("1111111");
        hidden.setCcli("2222222");
        index.add(visible);
        index.addCcliOnly(hidden);
        Assertions.assertEquals(1, index.size());
        Assertions.assertArrayEquals(new SongDisplayable[]{visible}, index.filter("twin", SearchIndex.FilterType.TITLE));
        Assertions.assertArrayEquals(new SongDisplayable[]{hidden}, index.filter("2222222", SearchIndex.FilterType.CCLI));
        Assertions.assertSame(hidden, index.getByID(2));
        hidden.setCcli("3333333");
        index.addCcliOnly(hidden);
        Assertions.assertEquals(0, index.filter("2222222", SearchIndex.FilterType.CCLI).length);
        Assertions.assertArrayEquals(new SongDisplayable[]{hidden}, index.filter("3333333", SearchIndex.FilterType.CCLI));
        index.add(hidden);
        Assertions.assertEquals(2, index.size());
        Assertions.assertEquals(2, index.filter("twin", SearchIndex.FilterType.TITLE).length);
        index.remove(hidden);
        Assertions.assertNull(index.getByID(2));
        Assertions.assertEquals(0, index.filter("3333333", SearchIndex.FilterType.CCLI).length);
    }

    private SongDisplayable song(long id, String title, String author, String lyrics) {
        SongDisplayable song = new SongDisplayable(title, author);
        song.setID(id);
        song.setLyrics(lyrics);
        return song;
    }
}
