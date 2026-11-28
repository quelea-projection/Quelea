package org.quelea.windows.newsong;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * The theme preview draws a song's first slide as the live view does: with
 * its translation lines in the translation font, and at the font size of the
 * whole song (not a size that only suits the first slide.)
 */
@DisplayName("ThemePanelPreviewTest")
public class ThemePanelPreviewTest {

    private static final String SHORT_VERSE = "Short\nverse";
    private static final String LONG_CHORUS = "This chorus has quite a lot more text on every line\n"
            + "So that it needs a much smaller font than the verse\n"
            + "If every slide of the song is to have the same size\n"
            + "Which is how the slides are shown live";

    private static DisplayStage previousProjection;
    private static Stage stage;
    private static ThemePanel panel;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-theme-preview-test").toString());
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
            panel = new ThemePanel();
            stage = new Stage();
            stage.setScene(new Scene(panel, 900, 700));
            stage.show();
            panel.setTheme(ThemeDTO.DEFAULT_THEME);
            return null;
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        onFx(() -> {
            stage.hide();
            QueleaApp.get().setProjectionWindow(previousProjection);
            return null;
        });
    }

    @Test
    public void translationLinesUseTheTranslationFont() throws Exception {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        song.setLyrics(SHORT_VERSE + "\n\n" + LONG_CHORUS);
        HashMap<String, String> translations = new HashMap<>();
        translations.put("Svenska", "Kort\nvers\n\nRefräng");
        song.setTranslations(translations);
        song.setCurrentTranslationLyrics("Svenska");
        List<Text> texts = draw(() -> song);
        Text translated = find(texts, "Kort");
        Text main = find(texts, "Short");
        Assertions.assertNotNull(translated, "The translation line isn't in the preview: " + describe(texts));
        Assertions.assertEquals(FontPosture.ITALIC, posture(translated), "Translation lines should be italic (default theme)");
        Assertions.assertEquals(FontPosture.REGULAR, posture(main), "Main lines should not be italic (default theme)");
    }

    @Test
    public void firstSlideUsesTheFontSizeOfTheWholeSong() throws Exception {
        SongDisplayable verseOnly = new SongDisplayable("Song", "Author");
        verseOnly.setLyrics(SHORT_VERSE);
        double alone = find(draw(() -> verseOnly), "Short").getFont().getSize();

        SongDisplayable song = new SongDisplayable("Song", "Author");
        song.setLyrics(SHORT_VERSE + "\n\n" + LONG_CHORUS);
        double inSong = find(draw(() -> song), "Short").getFont().getSize();

        Assertions.assertTrue(inSong < alone * 0.8, "The verse should be drawn smaller next to a long chorus ("
                + inSong + " vs " + alone + " on its own)");
    }

    private static FontPosture posture(Text text) {
        return text.getFont().getStyle().toLowerCase().contains("italic") ? FontPosture.ITALIC : FontPosture.REGULAR;
    }

    private static List<Text> draw(Supplier<SongDisplayable> source) throws Exception {
        onFx(() -> {
            panel.setPreviewSource(source);
            return null;
        });
        Thread.sleep(500);
        return onFx(() -> {
            List<Text> ret = new ArrayList<>();
            collect(panel.getCanvas(), ret);
            return ret;
        });
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

    private static Text find(List<Text> texts, String content) {
        for (Text text : texts) {
            if (text.getText().trim().equals(content)) {
                return text;
            }
        }
        return null;
    }

    private static String describe(List<Text> texts) {
        StringBuilder ret = new StringBuilder();
        for (Text text : texts) {
            ret.append('[').append(text.getText()).append(' ').append(text.getFont()).append(']');
        }
        return ret.toString();
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
