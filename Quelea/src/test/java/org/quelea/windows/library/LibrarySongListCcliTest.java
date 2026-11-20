package org.quelea.windows.library;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

class LibrarySongListCcliTest {
    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-library-search").toString());
        }
    }

    @Test
    void appendsSortedNumberMatchesWithoutChangingExistingResults() {
        SongDisplayable existing = song(1, "Alpha");
        SongDisplayable zulu = song(2, "Zulu");
        SongDisplayable beta = song(3, "Beta");
        existing.setLastSearch("123");
        zulu.setLastSearch("old search");
        beta.setLastSearch("old search");
        List<SongDisplayable> results = new ArrayList<>(List.of(existing, existing));

        LibrarySongList.appendCcliMatches(results, new SongDisplayable[]{existing, zulu, beta, zulu});

        Assertions.assertEquals(List.of(existing, existing, beta, zulu), results);
        Assertions.assertEquals("123", existing.getLastSearch());
        Assertions.assertNull(beta.getLastSearch());
        Assertions.assertNull(zulu.getLastSearch());
    }

    private static SongDisplayable song(long id, String title) {
        return new SongDisplayable.Builder(title, "Author").lyrics("Verse 1\nWords").id(id).get();
    }
}
