package org.quelea.windows.newsong;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.fxmisc.richtext.InlineCssTextArea;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * The song editor's slide preview shows the slide the caret is in, drawn
 * as it will be live (at the font size shared by all the slides.)
 */
@DisplayName("SlidePreviewPaneTest")
public class SlidePreviewPaneTest {

    private static final String VERSE = "Short\nverse";
    private static final String CHORUS = "This chorus has quite a lot more text on every line\n"
            + "So that it needs a much smaller font than the verse\n"
            + "If every slide of the song is to have the same size";

    private static DisplayStage previousProjection;
    private static boolean previousPreviewSetting;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-slide-preview-test").toString());
        previousPreviewSetting = QueleaProperties.get().getShowSongEditorPreview();
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
        Platform.setImplicitExit(false);
        onFx(() -> {
            previousProjection = QueleaApp.get().getProjectionWindow();
            QueleaApp.get().setProjectionWindow(new DisplayStage(new BoundingBox(0, 0, 1280, 720), false));
            return null;
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        QueleaProperties.get().setShowSongEditorPreview(previousPreviewSetting);
        onFx(() -> {
            QueleaApp.get().setProjectionWindow(previousProjection);
            return null;
        });
    }

    @Test
    public void caretPositionGivesTheSlide() {
        String lyrics = "Verse 1\nOne\nTwo\n\nChorus\nThree\n\n\nFour";
        Assertions.assertEquals(0, SlidePreviewPane.sectionAt(lyrics, 0));
        Assertions.assertEquals(0, SlidePreviewPane.sectionAt(lyrics, lyrics.indexOf("Two") + 3));
        // On the blank line between the slides: the next slide
        Assertions.assertEquals(1, SlidePreviewPane.sectionAt(lyrics, lyrics.indexOf("Two") + 4));
        Assertions.assertEquals(1, SlidePreviewPane.sectionAt(lyrics, lyrics.indexOf("Three")));
        Assertions.assertEquals(2, SlidePreviewPane.sectionAt(lyrics, lyrics.indexOf("Four") + 2));
        Assertions.assertEquals(2, SlidePreviewPane.sectionAt(lyrics, lyrics.length() + 10));
        // A new, empty line at the end of a slide still belongs to it
        Assertions.assertEquals(0, SlidePreviewPane.sectionAt("One\n", 4));
        Assertions.assertEquals(0, SlidePreviewPane.sectionAt("", 0));
        Assertions.assertEquals(1, SlidePreviewPane.sectionAt("One\r\n\r\nTwo", 9));
    }

    @Test
    public void previewFollowsTheCaretAtTheSongsFontSize() throws Exception {
        QueleaProperties.get().setShowSongEditorPreview(true);
        SongEntryWindow window = onFx(() -> {
            SongEntryWindow w = new SongEntryWindow();
            w.resetNewSong();
            w.setWidth(1100);
            w.setHeight(700);
            w.show();
            InlineCssTextArea area = w.getBasicSongPanel().getLyricsField().getTextArea();
            area.replaceText(VERSE + "\n\n" + CHORUS);
            return w;
        });
        Thread.sleep(400); // let the editor's own text listeners settle
        onFx(() -> {
            window.getBasicSongPanel().getLyricsField().getTextArea().moveTo(1); // in the verse
            return null;
        });
        Thread.sleep(800);
        String verseCaption = onFx(() -> caption(window));
        double verseSize = onFx(() -> fontSizeOf(window, "Short"));
        onFx(() -> {
            InlineCssTextArea area = window.getBasicSongPanel().getLyricsField().getTextArea();
            area.moveTo(area.getText().indexOf("chorus"));
            return null;
        });
        Thread.sleep(800);
        String chorusCaption = onFx(() -> caption(window));
        double chorusSize = onFx(() -> fontSizeOf(window, "smaller"));
        onFx(() -> {
            window.hide();
            return null;
        });
        Assertions.assertTrue(verseCaption.contains("1") && verseCaption.contains("2"), verseCaption);
        Assertions.assertTrue(chorusCaption.startsWith("Slide 2"), chorusCaption);
        Assertions.assertTrue(verseSize > 0, "The verse isn't in the preview");
        Assertions.assertTrue(chorusSize > 0, "The chorus isn't in the preview");
        Assertions.assertEquals(chorusSize, verseSize, 0.5, "Both slides should have the same font size");
    }

    private static SlidePreviewPane pane(SongEntryWindow window) {
        return (SlidePreviewPane) window.getScene().lookup(".slide-preview");
    }

    private static String caption(SongEntryWindow window) {
        for (Node child : pane(window).getChildrenUnmodifiable()) {
            if (child instanceof Label) {
                return ((Label) child).getText();
            }
        }
        return "";
    }

    private static double fontSizeOf(SongEntryWindow window, String word) {
        List<Text> texts = new ArrayList<>();
        collect(pane(window), texts);
        for (Text text : texts) {
            if (text.getText().contains(word)) {
                return text.getFont().getSize();
            }
        }
        return -1;
    }

    private static void collect(Node node, List<Text> texts) {
        if (node instanceof Text) {
            texts.add((Text) node);
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                collect(child, texts);
            }
        }
    }

    private static <R> R onFx(Supplier<R> supplier) throws Exception {
        CompletableFuture<R> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(supplier.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }
}
