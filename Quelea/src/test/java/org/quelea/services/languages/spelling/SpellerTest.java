package org.quelea.services.languages.spelling;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Spell checking")
public class SpellerTest {
    @TempDir Path directory;

    @Test
    public void checksWordsTextAndIgnoredWords() throws Exception {
        Path dictionary = dictionary();
        Speller speller = new Speller(new Dictionary("Test", dictionary.toFile()));
        Assertions.assertTrue(speller.checkWord("GRACE!"));
        Assertions.assertTrue(speller.checkWord("café"));
        Assertions.assertFalse(speller.checkWord("grase"));
        Assertions.assertEquals(Set.of("grase"), speller.getMisspeltWords("Grace and grase"));
        Assertions.assertFalse(speller.checkText("Grace grase", true));
        Assertions.assertTrue(speller.checkText("Grace grase", false));
        speller.addIgnoreWord("grase");
        Assertions.assertTrue(speller.checkText("Grace grase", true));
    }

    @Test
    public void addedWordsAreWrittenToTheDictionary() throws Exception {
        Path dictionary = dictionary();
        Speller speller = new Speller(new Dictionary("Test", dictionary.toFile()));
        speller.addWord("  Hope!  ");
        Assertions.assertTrue(speller.checkWord("hope"));
        Assertions.assertTrue(Files.readString(dictionary, StandardCharsets.UTF_8).contains("hope"));
    }

    @Test
    public void dictionaryCanBeMovedAfterLoading() throws Exception {
        Path dictionary = dictionary();
        new Speller(new Dictionary("Test", dictionary.toFile()));
        Assertions.assertTrue(Files.exists(Files.move(dictionary, directory.resolve("renamed.txt"))));
    }

    @Test
    public void punctuationDoesNotCreateAnEmptyMisspeltWord() throws Exception {
        Speller speller = new Speller(new Dictionary("Test", dictionary().toFile()));
        Assertions.assertTrue(speller.getMisspeltWords("... Grace!").isEmpty());
        Assertions.assertTrue(speller.checkText("... Grace!", true));
    }

    @Test
    public void caseInsensitiveCheckingWorksInTurkishLocale() throws Exception {
        Path dictionary = Files.writeString(directory.resolve("locale.txt"), "israel\n", StandardCharsets.UTF_8);
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Speller speller = new Speller(new Dictionary("Test", dictionary.toFile()));
            Assertions.assertTrue(speller.checkWord("ISRAEL"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    @Disabled("A failed dictionary load leaves an empty entry in the shared cache")
    public void failedDictionaryLoadCanBeRetriedAfterFileAppears() throws Exception {
        // A dictionary that becomes available after an initial read failure
        // must be loadable without restarting the application. Only publish a
        // dictionary to the shared cache after reading it successfully, while
        // preserving correct behavior for concurrent users of the cache.
        Path missing = directory.resolve("later.txt");
        Dictionary dictionary = new Dictionary("Test", missing.toFile());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Speller(dictionary));
        Files.writeString(missing, "grace\n", StandardCharsets.UTF_8);
        Assertions.assertTrue(new Speller(dictionary).checkWord("grace"));
    }

    @Test
    public void suggestionsContainTheClosestSixMatchesInOrder() throws Exception {
        Path dictionary = Files.writeString(directory.resolve("suggestions.txt"),
                "abcdef\nabcdefg\nabcdefgh\nabcdefghi\nabcdefghijk\nabcdefghijkl\nabcdefghijklm\nabcdefghijklmn\n",
                StandardCharsets.UTF_8);
        Speller speller = new Speller(new Dictionary("Test", dictionary.toFile()));
        var suggestions = speller.getSuggestions("abcdefghij");
        Assertions.assertEquals(Set.of("abcdefghi", "abcdefghijk", "abcdefgh", "abcdefghijkl", "abcdefg", "abcdefghijklm"),
                Set.copyOf(suggestions));
        for (int i = 1; i < suggestions.size(); i++) {
            int previous = LevenshteinDistance.computeLevenshteinDistance("abcdefghij", suggestions.get(i - 1));
            int current = LevenshteinDistance.computeLevenshteinDistance("abcdefghij", suggestions.get(i));
            Assertions.assertTrue(previous <= current, suggestions.toString());
        }
    }

    private Path dictionary() throws Exception {
        return Files.writeString(directory.resolve("dictionary.txt"), "grace\nfaith\nand\ncafé\n", StandardCharsets.UTF_8);
    }
}
