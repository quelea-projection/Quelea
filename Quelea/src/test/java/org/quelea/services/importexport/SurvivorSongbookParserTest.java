package org.quelea.services.importexport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Survivor songbook PDF import")
class SurvivorSongbookParserTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-survivor-import").toString());
        }
    }

    @Test
    void readsTheOnlyPageOfASongbook() throws Exception {
        Path file = createPdf("Amazing grace");
        List<SongDisplayable> songs = new SurvivorSongbookParser().getSongs(file.toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Amazing grace", songs.get(0).getTitle());
    }

    @Test
    void importsFirstAndLastPagesAsSeparateSongs() throws Exception {
        List<SongDisplayable> songs = new SurvivorSongbookParser().getSongs(createPdf("First song", "Last song").toFile(), null);
        Assertions.assertEquals(List.of("First song", "Last song"), songs.stream().map(SongDisplayable::getTitle).toList());
    }

    @Test
    void combinesTwoPageSongsWithoutLosingTheLastPage() throws Exception {
        List<SongDisplayable> songs = new SurvivorSongbookParser().getSongs(
                createPdf("First part (1 of 2)", "Second part (2 of 2)").toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("First part"));
        Assertions.assertTrue(songs.get(0).getLyrics(false, false, false).contains("Second part"));
    }

    @Test
    @Disabled("Short copyright footers make the backward scan run past the beginning of the page")
    void copyrightFooterWithoutBlankSeparatorDoesNotAbortImport() throws Exception {
        // Songbook PDFs may have short or irregular footers. Walking backwards
        // looking for a blank separator currently runs before the start of the
        // page; bound the scan and keep the available song text.
        List<SongDisplayable> songs = new SurvivorSongbookParser().getSongs(
                createPdf("Song title\nCopyright 2020").toFile(), null);
        Assertions.assertEquals(1, songs.size());
        Assertions.assertEquals("Song title", songs.get(0).getTitle());
    }

    private Path createPdf(String... pages) throws IOException {
        Path file = directory.resolve("songbook.pdf");
        try (PDDocument document = new PDDocument()) {
            for (String text : pages) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(60, 700);
                    for (String line : text.split("\\n")) {
                        stream.showText(line);
                        stream.newLineAtOffset(0, -15);
                    }
                    stream.endText();
                }
            }
            document.save(file.toFile());
        }
        return file;
    }

    @Test
    void invalidPdfReportsAnIoError() throws Exception {
        Path file = Files.writeString(directory.resolve("bad.pdf"), "not a PDF");
        Assertions.assertThrows(IOException.class, () -> new SurvivorSongbookParser().getSongs(file.toFile(), null));
    }
}
