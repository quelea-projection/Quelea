package org.quelea.services.print;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

/**
 * Checks what ends up in the PDFs that are printed for a schedule and a song.
 */
@DisplayName("SchedulePrintTest")
public class SchedulePrintTest {

    static final String CHORD_LYRICS = "Verse 1\n"
            + "F#m7         Bsus4\n"
            + "Amazing grace how sweet the sound\n"
            + "\n"
            + "Chorus\n"
            + "Dmaj7      E\n"
            + "My chains are gone";
    static final String PLAIN_LYRICS = "Verse 1\n"
            + "Great is thy faithfulness\n"
            + "Morning by morning";

    private static Path home;

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void setUp() throws Exception {
        home = Files.createTempDirectory("quelea-print-test");
        QueleaProperties.init(home.toString());
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
    }

    static SongDisplayable chordSong() {
        return new SongDisplayable.Builder("Amazing Grace", "John Newton").lyrics(CHORD_LYRICS).get();
    }

    static Schedule schedule() {
        Schedule schedule = new Schedule();
        schedule.add(chordSong());
        schedule.add(new SongDisplayable.Builder("Great Is Thy Faithfulness", "Thomas Chisholm").lyrics(PLAIN_LYRICS).get());
        return schedule;
    }

    private File pdf(String name) {
        return tempDir.resolve(name + ".pdf").toFile();
    }

    /**
     * The text of a PDF, with all runs of white space (including the
     * non-breaking spaces used to line up chords) turned into one space.
     */
    static String text(File pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(document).replace(' ', ' ').replaceAll("\\s+", " ");
        }
    }

    static int pages(File pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            return document.getNumberOfPages();
        }
    }

    @Test
    public void chordSongIsRecognised() {
        Assertions.assertTrue(chordSong().hasChords());
    }

    @Test
    public void overviewHasNoLyrics() throws IOException {
        File file = pdf("overview");
        new SchedulePDFPrinter().print(schedule(), SchedulePrintContent.OVERVIEW, file);
        String text = text(file);
        Assertions.assertTrue(text.contains("Order of Service"), text);
        Assertions.assertTrue(text.contains("Amazing Grace"), text);
        Assertions.assertTrue(text.contains("Great Is Thy Faithfulness"), text);
        Assertions.assertFalse(text.contains("how sweet the sound"), text);
        Assertions.assertFalse(text.contains("Morning by morning"), text);
        Assertions.assertFalse(text.contains("F#m7"), text);
        Assertions.assertEquals(1, pages(file));
    }

    @Test
    public void defaultPrintIsTheOverview() throws IOException {
        Assertions.assertEquals(schedule().getPrintXML(SchedulePrintContent.OVERVIEW), schedule().getPrintXML());
        File file = pdf("default");
        new SchedulePDFPrinter().print(schedule(), file);
        Assertions.assertEquals(1, pages(file));
    }

    @Test
    public void lyricsWithoutChords() throws IOException {
        File file = pdf("lyrics");
        new SchedulePDFPrinter().print(schedule(), SchedulePrintContent.LYRICS, file);
        String text = text(file);
        Assertions.assertTrue(text.contains("Order of Service"), text);
        Assertions.assertTrue(text.contains("Amazing grace how sweet the sound"), text);
        Assertions.assertTrue(text.contains("My chains are gone"), text);
        Assertions.assertTrue(text.contains("Morning by morning"), text);
        Assertions.assertTrue(text.contains("John Newton"), text);
        Assertions.assertFalse(text.contains("F#m7"), text);
        Assertions.assertFalse(text.contains("Dmaj7"), text);
        // The overview, then one page for each song
        Assertions.assertEquals(3, pages(file));
    }

    @Test
    public void lyricsWithChords() throws IOException {
        File file = pdf("chords");
        new SchedulePDFPrinter().print(schedule(), SchedulePrintContent.LYRICS_WITH_CHORDS, file);
        String text = text(file);
        Assertions.assertTrue(text.contains("Amazing grace how sweet the sound"), text);
        Assertions.assertTrue(text.contains("Morning by morning"), text);
        Assertions.assertTrue(text.contains("F#m7 Bsus4"), text);
        Assertions.assertTrue(text.contains("Dmaj7 E"), text);
        Assertions.assertEquals(3, pages(file));
    }

    @Test
    public void lyricsAreEscaped() throws IOException {
        Schedule schedule = new Schedule();
        schedule.add(new SongDisplayable.Builder("Rock & Roll <Live>", "A & B").lyrics("Verse\nYou & me < them").get());
        File file = pdf("escaped");
        new SchedulePDFPrinter().print(schedule, SchedulePrintContent.LYRICS, file);
        String text = text(file);
        Assertions.assertTrue(text.contains("You & me < them"), text);
        Assertions.assertTrue(text.contains("Rock & Roll <Live>"), text);
    }

    @Test
    public void songPrintWithAndWithoutChords() throws IOException {
        SongDisplayable song = chordSong();
        File without = pdf("song");
        SongPDFPrinter.INSTANCE.printXML(song.getPrintXML(false, false), without);
        String text = text(without);
        Assertions.assertTrue(text.contains("Amazing grace how sweet the sound"), text);
        Assertions.assertFalse(text.contains("F#m7"), text);

        File with = pdf("song-chords");
        SongPDFPrinter.INSTANCE.printXML(song.getPrintXML(false, true), with);
        text = text(with);
        Assertions.assertTrue(text.contains("Amazing grace how sweet the sound"), text);
        Assertions.assertTrue(text.contains("F#m7 Bsus4"), text);
    }

    @Test
    public void songPrintChordsFieldStillUsed() {
        SongDisplayable song = chordSong();
        song.setPrintChords(true);
        Assertions.assertEquals(song.getPrintXML(false, true), song.getPrintXML(false));
        song.setPrintChords(false);
        Assertions.assertEquals(song.getPrintXML(false, false), song.getPrintXML(false));
    }

    @Test
    public void pagesAreRendered() throws IOException {
        File file = pdf("render");
        new SchedulePDFPrinter().print(schedule(), SchedulePrintContent.LYRICS, file);
        PdfPageRenderer.Pages pages = PdfPageRenderer.render(file, 50);
        Assertions.assertEquals(3, pages.pageCount());
        Assertions.assertEquals(3, pages.pages().size());
        Assertions.assertTrue(pages.pages().get(0).getWidth() > 100);
        Assertions.assertTrue(pages.pages().get(0).getHeight() > pages.pages().get(0).getWidth());
    }

    @Test
    public void choicesAreRemembered() {
        Assertions.assertEquals(SchedulePrintContent.OVERVIEW, SchedulePrintContent.fromName(null));
        Assertions.assertEquals(SchedulePrintContent.OVERVIEW, SchedulePrintContent.fromName("nonsense"));
        QueleaProperties.get().setSchedulePrintContent(SchedulePrintContent.LYRICS_WITH_CHORDS);
        QueleaProperties.get().setPrintSongChords(true);
        QueleaProperties.init(home.toString()); // read back from the file
        Assertions.assertEquals(SchedulePrintContent.LYRICS_WITH_CHORDS, QueleaProperties.get().getSchedulePrintContent());
        Assertions.assertTrue(QueleaProperties.get().getPrintSongChords());
        QueleaProperties.get().setSchedulePrintContent(SchedulePrintContent.OVERVIEW);
        QueleaProperties.get().setPrintSongChords(false);
    }
}
