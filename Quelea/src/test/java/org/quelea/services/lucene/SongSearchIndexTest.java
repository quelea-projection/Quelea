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

    private SongDisplayable song(long id, String title, String author, String lyrics) {
        SongDisplayable song = new SongDisplayable(title, author);
        song.setID(id);
        song.setLyrics(lyrics);
        return song;
    }
}
