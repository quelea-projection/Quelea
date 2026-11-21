package org.quelea.data;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * The size of the translation compared to the main text is set in the theme,
 * kept when the theme is saved and used when the lyrics are drawn.
 */
@DisplayName("TranslationSizeTest")
public class TranslationSizeTest {

    private static final String THEME = "fontname:Arial$translatefontname:Arial$fontcolour:0xffffffff"
            + "$translatefontcolour:0xccccccff$backgroundcolour:0x000000ff";
    private static final String FIRST = "Size first";
    private static final String SECOND = "Size second";
    private static DisplayStage previousProjection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-translation-size-test").toString());
        Files.createDirectories(QueleaProperties.get().getBibleDir().toPath());
        writeBible("first.xml", FIRST, "In the beginning was the Word, and the Word was with God, and the Word was God. "
                + "The same was in the beginning with God. All things were made by him; and without him was not any "
                + "thing made that was made. In him was life; and the life was the light of men.");
        writeBible("second.xml", SECOND, "I begynnelsen fanns Ordet, och Ordet fanns hos Gud, och Ordet var Gud. "
                + "Det fanns i begynnelsen hos Gud. Genom det har allt blivit till, och utan det har inget blivit till "
                + "av allt som finns till. I det var liv, och livet var människornas ljus.");
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

    @Test
    public void sizeIsKeptInTheThemeText() {
        ThemeDTO theme = ThemeDTO.fromString(THEME + "$translatefontsize:70", new HashMap<>());
        Assertions.assertEquals(70, theme.getTranslateFontSizePercent());
        ThemeDTO again = ThemeDTO.fromString(theme.asString(), new HashMap<>());
        Assertions.assertEquals(70, again.getTranslateFontSizePercent());
        Assertions.assertEquals(35, again.getTranslateFontSize(50), 0.001);
    }

    @Test
    public void sizeIsKeptInTheDatabaseTheme() {
        ThemeDTO theme = ThemeDTO.fromString(THEME + "$translatefontsize:100", new HashMap<>());
        Assertions.assertEquals(100, ThemeDTO.getDTO(theme.getTheme()).getTranslateFontSizePercent());
    }

    @Test
    public void olderThemesKeepTheOldSize() {
        ThemeDTO theme = ThemeDTO.fromString(THEME, new HashMap<>());
        Assertions.assertEquals(0, theme.getTranslateFontSizePercent());
        Assertions.assertFalse(theme.asString().contains("translatefontsize"));
        double offset = QueleaProperties.get().getTranslationFontSizeOffset();
        Assertions.assertEquals(50 - offset, theme.getTranslateFontSize(50), 0.001);
    }

    @Test
    public void translationDrawnAtTheSameSizeAt100Percent() throws Exception {
        double[] sizes = drawnSizes(100);
        Assertions.assertEquals(1, sizes.length, "main text and translation should be the same size");
    }

    @Test
    public void translationDrawnAtHalfTheSizeAt50Percent() throws Exception {
        double[] sizes = drawnSizes(50);
        Assertions.assertEquals(2, sizes.length, "main text and translation should differ in size");
        Assertions.assertEquals(0.5, sizes[0] / sizes[1], 0.01);
    }

    @Test
    public void bibleVersionsDrawnAtTheSameSizeAt100Percent() throws Exception {
        double[] result = drawnPassage(100);
        Assertions.assertEquals(1, result.length - 1, "both versions should be the size of the main text");
        Assertions.assertTrue(result[result.length - 1] <= 360, "the text should fit on the slide: " + result[result.length - 1]);
    }

    @Test
    public void bibleVersionsDrawnAtHalfTheSizeAt50Percent() throws Exception {
        double[] result = drawnPassage(50);
        Assertions.assertEquals(2, result.length - 1, "the second version should be smaller");
        Assertions.assertEquals(0.5, result[0] / result[1], 0.01);
        Assertions.assertTrue(result[result.length - 1] <= 360, "the text should fit on the slide: " + result[result.length - 1]);
    }

    @Test
    public void smallerBibleVersionsLeaveRoomForTheMainText() throws Exception {
        double[] full = drawnPassage(100);
        double[] half = drawnPassage(50);
        double mainFull = full[full.length - 2];
        double mainHalf = half[half.length - 2];
        // The passage is wrapped to fit at the size the versions are drawn at
        Assertions.assertTrue(mainHalf > mainFull * 1.05,
                "with the other version at half size the main text should get bigger: " + mainHalf + " vs " + mainFull);
    }

    /**
     * Draw a passage in two bible versions (wrapped by the drawer to fit) and
     * get the different font sizes, smallest first, followed by the height
     * of the drawn text.
     */
    private static double[] drawnPassage(int percent) throws Exception {
        return onFx(() -> {
            DisplayCanvas canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(canvas), 640, 360, Color.BLACK));
            stage.show();
            ThemeDTO theme = ThemeDTO.fromString(THEME + "$translatefontsize:" + percent, new HashMap<>());
            BibleVerse verse = BibleManager.get().getBibleFromName(FIRST).getBookByNumber(43).getChapter(0).getVerse(1);
            List<Bible> versions = new ArrayList<>();
            versions.add(BibleManager.get().getBibleFromName(FIRST));
            versions.add(BibleManager.get().getBibleFromName(SECOND));
            BiblePassage passage = new BiblePassage(FIRST, "John 1:1", new BibleVerse[]{verse}, false).withVersions(versions);
            for (TextSection section : passage.getSections()) {
                section.setTheme(theme);
            }
            passage.setTheme(theme);
            LyricDrawer drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            drawer.setPlayVideo(false);
            drawer.setTheme(theme);
            drawer.setText(passage, 0);
            Set<Double> found = new TreeSet<>();
            double height = 0;
            // The lyrics are the first group; the next one is the small text
            // with the location and the version names
            for (Node node : canvas.getChildren()) {
                if (node instanceof Group && !((Group) node).getChildren().isEmpty()) {
                    collectSizes(node, found);
                    height = node.getLayoutBounds().getHeight();
                    break;
                }
            }
            stage.hide();
            double[] ret = new double[found.size() + 1];
            int i = 0;
            for (double size : found) {
                ret[i++] = size;
            }
            ret[i] = height;
            return ret;
        });
    }

    private static void writeBible(String file, String name, String text) throws Exception {
        String xml = "<XMLBIBLE biblename=\"" + name + "\"><BIBLEBOOK bnumber=\"43\" bname=\"John\">"
                + "<CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">" + text + "</VERS></CHAPTER></BIBLEBOOK></XMLBIBLE>";
        Files.writeString(QueleaProperties.get().getBibleDir().toPath().resolve(file), xml);
    }

    /**
     * Draw two lines with a translation and get the different font sizes
     * that were used, smallest first.
     */
    private static double[] drawnSizes(int percent) throws Exception {
        return onFx(() -> {
            DisplayCanvas canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(canvas), 640, 360, Color.BLACK));
            stage.show();
            ThemeDTO theme = ThemeDTO.fromString(THEME + "$translatefontsize:" + percent, new HashMap<>());
            LyricDrawer drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            drawer.setTheme(theme);
            drawer.setText(new String[]{"First line", "Second line"},
                    new String[]{"Första raden", "Andra raden"}, null, false, -1);
            Set<Double> found = new TreeSet<>();
            collectSizes(canvas, found);
            stage.hide();
            return found.stream().mapToDouble(Double::doubleValue).toArray();
        });
    }

    private static void collectSizes(Node node, Set<Double> sizes) {
        // Verse numbers are drawn smaller (superscript), so leave them out
        if (node instanceof Text && !((Text) node).getText().isBlank() && !((Text) node).getText().trim().matches("\\d+")) {
            sizes.add(Math.round(((Text) node).getFont().getSize() * 100) / 100.0);
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                collectSizes(child, sizes);
            }
        }
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
