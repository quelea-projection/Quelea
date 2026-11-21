package org.quelea.windows.lyrics;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.QueleaPropertyKeys;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * Bible versions on the same slide are separated by a line with a smaller
 * gap than an empty line, which leaves more room for the text.
 */
@DisplayName("BibleVersionSeparatorTest")
public class BibleVersionSeparatorTest {

    private static final String FIRST = "Separator first";
    private static final String SECOND = "Separator second";
    private static DisplayStage previousProjection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-version-separator-test").toString());
        Files.createDirectories(QueleaProperties.get().getBibleDir().toPath());
        writeBible("first.xml", FIRST, "In the beginning was the Word, and the Word was with God, and the Word was God. "
                + "The same was in the beginning with God. All things were made by him.");
        writeBible("second.xml", SECOND, "I begynnelsen fanns Ordet, och Ordet fanns hos Gud, och Ordet var Gud. "
                + "Det fanns i begynnelsen hos Gud. Genom det har allt blivit till.");
        BibleManager.get().loadBibles(false);
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
        onFx(() -> {
            QueleaApp.get().setProjectionWindow(previousProjection);
            return null;
        });
    }

    @AfterEach
    public void resetOption() {
        QueleaProperties.get().remove(QueleaPropertyKeys.bibleVersionSeparatorKey);
    }

    @Test
    public void lineBetweenVersionsLeavesMoreRoomForText() throws Exception {
        QueleaProperties.get().setProperty(QueleaPropertyKeys.bibleVersionSeparatorKey, "false");
        Drawn without = draw("version-separator-off.png");
        QueleaProperties.get().setProperty(QueleaPropertyKeys.bibleVersionSeparatorKey, "true");
        Drawn with = draw("version-separator-on.png");

        Assertions.assertEquals(0, without.lines, "no line without the option");
        Assertions.assertEquals(1, with.lines, "one line between the two versions");
        Assertions.assertTrue(with.fontSize >= without.fontSize,
                "the smaller gap should leave the same or more room: " + with.fontSize + " < " + without.fontSize);
        // Compared to the size of the text, which is bigger with the line
        Assertions.assertTrue(with.gapToSecondVersion / with.fontSize < without.gapToSecondVersion / without.fontSize,
                "the gap between the versions should be smaller with the line");
    }

    private static final class Drawn {
        int lines;
        double fontSize;
        double gapToSecondVersion;
    }

    private static Drawn draw(String snapshotName) throws Exception {
        return onFx(() -> {
            DisplayCanvas canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(canvas), 640, 360, Color.BLACK));
            stage.show();
            LyricDrawer drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            drawer.setPlayVideo(false);
            drawer.setText(passage(), 0);
            Group text = null;
            for (Node node : canvas.getChildren()) {
                if (node instanceof Group && !((Group) node).getChildren().isEmpty()) {
                    text = (Group) node;
                    break;
                }
            }
            Assertions.assertNotNull(text);
            Drawn drawn = new Drawn();
            double lastFirstVersionY = -1;
            double firstSecondVersionY = -1;
            for (Node node : text.getChildren()) {
                if (node instanceof Line) {
                    drawn.lines++;
                } else if (node instanceof FormattedText && !isBlank((FormattedText) node)) {
                    FormattedText line = (FormattedText) node;
                    // The other versions are drawn in the translation colour
                    if (!isSecondVersion(line)) {
                        lastFirstVersionY = line.getLayoutY();
                        drawn.fontSize = Math.max(drawn.fontSize, fontSize(line));
                    } else if (firstSecondVersionY < 0 && line.getLayoutY() > lastFirstVersionY && lastFirstVersionY >= 0) {
                        firstSecondVersionY = line.getLayoutY();
                    }
                }
            }
            drawn.gapToSecondVersion = firstSecondVersionY - lastFirstVersionY;
            try {
                File dir = new File("build/test-snapshots");
                dir.mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null), "png", new File(dir, snapshotName));
            } catch (Exception ex) {
                // Only for looking at
            }
            stage.hide();
            return drawn;
        });
    }

    private static boolean isBlank(FormattedText line) {
        for (Node child : line.getChildren()) {
            if (child instanceof javafx.scene.text.Text && !((javafx.scene.text.Text) child).getText().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSecondVersion(FormattedText line) {
        for (Node child : line.getChildren()) {
            if (child instanceof javafx.scene.text.Text) {
                return ThemeDTO.DEFAULT_TRANSLATE_FONT_COLOR.equals(((javafx.scene.text.Text) child).getFill());
            }
        }
        return false;
    }

    private static double fontSize(FormattedText line) {
        for (Node child : line.getChildren()) {
            if (child instanceof javafx.scene.text.Text) {
                return ((javafx.scene.text.Text) child).getFont().getSize();
            }
        }
        return 0;
    }

    private static BiblePassage passage() {
        BibleVerse verse = BibleManager.get().getBibleFromName(FIRST).getBookByNumber(43).getChapter(0).getVerse(1);
        BiblePassage passage = new BiblePassage(FIRST, "John 1:1", new BibleVerse[]{verse}, false);
        List<Bible> versions = new ArrayList<>();
        versions.add(BibleManager.get().getBibleFromName(FIRST));
        versions.add(BibleManager.get().getBibleFromName(SECOND));
        return passage.withVersions(versions);
    }

    private static void writeBible(String file, String name, String text) throws Exception {
        String xml = "<XMLBIBLE biblename=\"" + name + "\"><BIBLEBOOK bnumber=\"43\" bname=\"John\">"
                + "<CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">" + text + "</VERS></CHAPTER></BIBLEBOOK></XMLBIBLE>";
        Files.writeString(QueleaProperties.get().getBibleDir().toPath().resolve(file), xml);
    }

    private static <T> T onFx(Supplier<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(task.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(20, TimeUnit.SECONDS);
    }
}
