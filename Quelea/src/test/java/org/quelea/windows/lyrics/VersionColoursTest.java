package org.quelea.windows.lyrics;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.ThemePanel;
import org.quelea.windows.style.StyleManager;

/**
 * Each bible version on a slide can have its own colour; with one colour,
 * all the versions after the first have that colour.
 */
@DisplayName("VersionColoursTest")
public class VersionColoursTest {

    private static final String[] BIBLES = {"Colours first", "Colours second", "Colours third", "Colours fourth"};
    private static final String THEME = "fontname:Arial$translatefontname:Arial$fontcolour:0xffffffff"
            + "$translatefontcolour:0xffff00ff$backgroundcolour:0x102030ff";
    private static final Color YELLOW = Color.web("0xffff00ff");
    private static final Color CYAN = Color.web("0x00ffffff");
    private static final Color PINK = Color.web("0xff80c0ff");
    private static DisplayStage previousProjection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-version-colours-test").toString());
        Files.createDirectories(QueleaProperties.get().getBibleDir().toPath());
        for (int i = 0; i < BIBLES.length; i++) {
            String xml = "<XMLBIBLE biblename=\"" + BIBLES[i] + "\"><BIBLEBOOK bnumber=\"43\" bname=\"John\">"
                    + "<CHAPTER cnumber=\"1\"><VERS vnumber=\"1\">Version " + (i + 1) + " of the verse.</VERS>"
                    + "</CHAPTER></BIBLEBOOK></XMLBIBLE>";
            Files.writeString(QueleaProperties.get().getBibleDir().toPath().resolve("colours" + i + ".xml"), xml);
        }
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
    public void colourOfEachTranslation() {
        ThemeDTO theme = ThemeDTO.fromString(THEME, new HashMap<>());
        Assertions.assertEquals(YELLOW, theme.getTranslateFontPaint(0));
        Assertions.assertEquals(YELLOW, theme.getTranslateFontPaint(3), "one colour is used for all translations");
        theme.setMoreTranslateFontPaints(List.of(CYAN, PINK));
        Assertions.assertEquals(YELLOW, theme.getTranslateFontPaint(0));
        Assertions.assertEquals(CYAN, theme.getTranslateFontPaint(1));
        Assertions.assertEquals(PINK, theme.getTranslateFontPaint(2));
        Assertions.assertEquals(PINK, theme.getTranslateFontPaint(5), "later translations keep the last colour");
    }

    @Test
    public void coloursAreKeptInThemeTextAndDatabase() {
        ThemeDTO theme = ThemeDTO.fromString(THEME, new HashMap<>());
        theme.setMoreTranslateFontPaints(List.of(CYAN, PINK));
        Assertions.assertEquals(List.of(CYAN, PINK), ThemeDTO.fromString(theme.asString(), new HashMap<>()).getMoreTranslateFontPaints());
        Assertions.assertEquals(List.of(CYAN, PINK), ThemeDTO.getDTO(theme.getTheme()).getMoreTranslateFontPaints());
        Assertions.assertTrue(ThemeDTO.getDTO(ThemeDTO.fromString(THEME, new HashMap<>()).getTheme()).getMoreTranslateFontPaints().isEmpty());
        Assertions.assertFalse(ThemeDTO.fromString(THEME, new HashMap<>()).asString().contains("translatefontcolours"),
                "themes without more colours are stored as before");
    }

    @Test
    public void versionsAreDrawnInTheirColours() throws Exception {
        ThemeDTO theme = ThemeDTO.fromString(THEME, new HashMap<>());
        theme.setMoreTranslateFontPaints(List.of(CYAN, PINK));
        Drawn drawn = draw(theme, "version-colours.png");
        Assertions.assertEquals(List.of(Color.WHITE, YELLOW, CYAN, PINK), drawn.textColours);
        Assertions.assertEquals(List.of(YELLOW, CYAN, PINK), drawn.separatorColours,
                "each line has the colour of the version below it");
    }

    @Test
    public void oneColourForAllVersions() throws Exception {
        Drawn drawn = draw(ThemeDTO.fromString(THEME, new HashMap<>()), "version-colours-one.png");
        Assertions.assertEquals(List.of(Color.WHITE, YELLOW), drawn.textColours);
    }

    @Test
    public void themeEditorKeepsTheColours() throws Exception {
        ThemeDTO theme = ThemeDTO.fromString(THEME, new HashMap<>());
        theme.setMoreTranslateFontPaints(List.of(CYAN, PINK));
        List<Color> colours = onFx(() -> {
            ThemePanel panel = new ThemePanel();
            panel.setTheme(theme);
            Stage stage = new Stage();
            Scene scene = new Scene(panel, 1000, 760);
            StyleManager.apply(scene);
            stage.setScene(scene);
            stage.show();
            List<Color> result = panel.getTheme().getMoreTranslateFontPaints();
            snapshot(scene.getRoot(), "version-colours-editor.png");
            stage.hide();
            return result;
        });
        Assertions.assertEquals(List.of(CYAN, PINK), colours);
    }

    @Test
    public void ctrlClickInThePaletteChoosesMoreColours() throws Exception {
        ThemePanel panel = onFx(() -> {
            ThemePanel p = new ThemePanel();
            p.setTheme(ThemeDTO.fromString(THEME, new HashMap<>()));
            Stage stage = new Stage();
            Scene scene = new Scene(p, 1000, 760);
            StyleManager.apply(scene);
            stage.setScene(scene);
            stage.show();
            stage.toFront();
            stage.requestFocus();
            return p;
        });
        // The palette closes itself when its window loses focus, so only open
        // it once the window has settled with the focus
        for (int i = 0; i < 30 && !onFx(() -> panel.getScene().getWindow().isFocused()); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(500);
        ColorPicker picker = onFx(() -> {
            ColorPicker found = null;
            for (Node node : panel.lookupAll(".color-picker")) {
                if (node instanceof ColorPicker && YELLOW.equals(((ColorPicker) node).getValue())) {
                    found = (ColorPicker) node;
                }
            }
            Assertions.assertNotNull(found, "the translation colour picker");
            found.show();
            return found;
        });
        // The palette opens in a popup window; give it time to appear
        for (int i = 0; i < 50 && !onFx(() -> isPaletteShowing(picker)); i++) {
            Thread.sleep(100);
        }
        // It also closes when the test window loses the focus (to another
        // app); only the palette's own closing is under test here
        onFx(() -> {
            ((javafx.stage.PopupWindow) palette(picker).getScene().getWindow()).setAutoHide(false);
            return null;
        });
        Thread.sleep(200);
        List<StackPane> squares = onFx(() -> {
            List<StackPane> ret = new ArrayList<>();
            for (Node node : palette(picker).lookupAll(".color-square")) {
                Color colour = squareColour(node);
                if (colour != null && !colour.equals(YELLOW) && ret.stream().noneMatch(s -> colour.equals(squareColour(s)))) {
                    ret.add((StackPane) node);
                }
            }
            return ret;
        });
        Assertions.assertTrue(squares.size() > 2, "the palette should have colour squares");
        Color first = squareColour(squares.get(5));
        Color second = squareColour(squares.get(20));

        // The very first click in the palette, with Cmd/Ctrl held down
        onFx(() -> click(squares.get(5), true));
        Assertions.assertEquals(List.of(first), onFx(() -> panel.getTheme().getMoreTranslateFontPaints()));
        Assertions.assertTrue(onFx(picker::isShowing), "Ctrl-click keeps the palette open");
        // The mouse moves a little while pressed, and Cmd/Ctrl is let go before the button
        onFx(() -> {
            fire(squares.get(20), MouseEvent.MOUSE_PRESSED, true);
            fire(squares.get(21), MouseEvent.MOUSE_DRAGGED, true);
            fire(squares.get(21), MouseEvent.MOUSE_RELEASED, false);
            fire(squares.get(21), MouseEvent.MOUSE_CLICKED, false);
            return null;
        });
        Assertions.assertEquals(List.of(first, second), onFx(() -> panel.getTheme().getMoreTranslateFontPaints()),
                "a Cmd/Ctrl-click that moves a little still adds the colour it started on");
        Assertions.assertEquals(YELLOW, onFx(() -> panel.getTheme().getTranslateFontPaint()));
        // As the bible versions they are for (a colour can be in the palette twice)
        Assertions.assertEquals(Set.of("2", "3", "4"), onFx(() -> {
            Set<String> numbers = new java.util.TreeSet<>();
            for (Node badge : palette(picker).lookupAll(".colour-order-badge")) {
                if (badge.getParent().getParent() instanceof javafx.scene.layout.GridPane) {
                    numbers.add(((javafx.scene.control.Label) badge).getText());
                }
            }
            return numbers;
        }), "the three chosen colours are numbered in the palette");
        Assertions.assertTrue(onFx(() -> picker.lookup(".picker-color-rect") instanceof javafx.scene.shape.Rectangle
                && ((javafx.scene.shape.Rectangle) picker.lookup(".picker-color-rect")).getFill() instanceof javafx.scene.paint.LinearGradient),
                "the swatch on the picker is split into the colours");

        // Hovering over a new colour shows the next number only with Cmd/Ctrl held down
        StackPane fresh = squares.get(40);
        onFx(() -> fire(fresh, MouseEvent.MOUSE_ENTERED, false));
        onFx(() -> fire(fresh, MouseEvent.MOUSE_MOVED, true));
        Thread.sleep(200);
        Assertions.assertEquals("5", onFx(() -> hoverNumber(picker)));
        Thread.sleep(200);
        onFx(() -> {
            snapshot(palette(picker), "version-colours-palette.png");
            snapshot(picker, "version-colours-picker.png");
            snapshot(panel.getScene().getRoot(), "version-colours-editor-chosen.png");
            return null;
        });
        onFx(() -> fire(fresh, MouseEvent.MOUSE_MOVED, false));
        Thread.sleep(200);
        Assertions.assertNull(onFx(() -> hoverNumber(picker)), "no number without Cmd/Ctrl: a click replaces the colours");

        onFx(() -> click(squares.get(5), true));
        Assertions.assertEquals(List.of(second), onFx(() -> panel.getTheme().getMoreTranslateFontPaints()),
                "Ctrl-clicking a chosen colour takes it away");
        onFx(() -> click(squares.get(5), false));
        Thread.sleep(200);
        Assertions.assertEquals(first, onFx(() -> panel.getTheme().getTranslateFontPaint()));
        Assertions.assertTrue(onFx(() -> panel.getTheme().getMoreTranslateFontPaints()).isEmpty(),
                "an ordinary click chooses just that colour");
        Assertions.assertTrue(onFx(() -> ((javafx.scene.shape.Rectangle) picker.lookup(".picker-color-rect")).getFill() instanceof Color),
                "one colour fills the whole swatch");
        onFx(() -> {
            picker.hide();
            panel.getScene().getWindow().hide();
            return null;
        });
    }

    private static boolean isPaletteShowing(ColorPicker picker) {
        try {
            palette(picker);
            return true;
        } catch (AssertionError ex) {
            return false;
        }
    }

    private static javafx.scene.Parent palette(ColorPicker picker) {
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window instanceof javafx.stage.PopupWindow && window.isShowing()
                    && ((javafx.stage.PopupWindow) window).getOwnerWindow() == picker.getScene().getWindow()
                    && window.getScene().getRoot().lookup(".color-square") != null) {
                return window.getScene().getRoot();
            }
        }
        throw new AssertionError("the palette isn't showing");
    }

    private static Color squareColour(Node square) {
        for (Node child : ((StackPane) square).getChildren()) {
            if (child instanceof javafx.scene.shape.Rectangle && ((javafx.scene.shape.Rectangle) child).getFill() instanceof Color) {
                Color colour = (Color) ((javafx.scene.shape.Rectangle) child).getFill();
                return colour.getOpacity() > 0 ? colour : null;
            }
        }
        return null;
    }

    private static Void click(StackPane square, boolean shortcut) {
        for (EventType<MouseEvent> type : List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED)) {
            fire(square, type, shortcut);
        }
        return null;
    }

    private static Void fire(StackPane square, EventType<MouseEvent> type, boolean shortcut) {
        Node target = square.getChildren().get(0);
        boolean pressed = type == MouseEvent.MOUSE_PRESSED || type == MouseEvent.MOUSE_DRAGGED;
        MouseEvent event = new MouseEvent(type, 5, 5, 5, 5,
                type == MouseEvent.MOUSE_MOVED || type == MouseEvent.MOUSE_ENTERED ? MouseButton.NONE : MouseButton.PRIMARY, 1,
                false, shortcut, false, shortcut, pressed, false, false, false, false,
                false, false, true, new PickResult(target, 5, 5));
        Event.fireEvent(type == MouseEvent.MOUSE_ENTERED ? square : target, event);
        return null;
    }

    /**
     * Get the number on the palette's enlarged square under the mouse.
     */
    private static String hoverNumber(ColorPicker picker) {
        for (Node badge : palette(picker).lookupAll(".colour-order-badge")) {
            if (!(badge.getParent().getParent() instanceof javafx.scene.layout.GridPane)) {
                return ((javafx.scene.control.Label) badge).getText();
            }
        }
        return null;
    }

    private static final class Drawn {
        final List<Color> textColours = new ArrayList<>();
        final List<Color> separatorColours = new ArrayList<>();
    }

    private static Drawn draw(ThemeDTO theme, String snapshotName) throws Exception {
        return onFx(() -> {
            DisplayCanvas canvas = new DisplayCanvas(false, null, DisplayCanvas.Priority.LOW);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(canvas), 640, 360, Color.BLACK));
            stage.show();
            BiblePassage passage = passage();
            for (var section : passage.getSections()) {
                section.setTheme(theme);
            }
            passage.setTheme(theme);
            LyricDrawer drawer = new LyricDrawer();
            drawer.setCanvas(canvas);
            drawer.setPlayVideo(false);
            drawer.setTheme(theme);
            drawer.setText(passage, 0);
            Drawn drawn = new Drawn();
            Set<Color> textColours = new LinkedHashSet<>();
            for (Node node : canvas.getChildren()) {
                if (node instanceof Group && !((Group) node).getChildren().isEmpty()) {
                    for (Node child : ((Group) node).getChildren()) {
                        if (child instanceof Line) {
                            drawn.separatorColours.add((Color) ((Line) child).getStroke());
                        } else if (child instanceof FormattedText) {
                            for (Node part : ((FormattedText) child).getChildren()) {
                                if (part instanceof Text && !((Text) part).getText().isBlank()) {
                                    Paint fill = ((Text) part).getFill();
                                    textColours.add((Color) fill);
                                }
                            }
                        }
                    }
                    break;
                }
            }
            drawn.textColours.addAll(textColours);
            snapshot(canvas, snapshotName);
            stage.hide();
            return drawn;
        });
    }

    private static BiblePassage passage() {
        BibleVerse verse = BibleManager.get().getBibleFromName(BIBLES[0]).getBookByNumber(43).getChapter(0).getVerse(1);
        BiblePassage passage = new BiblePassage(BIBLES[0], "John 1:1", new BibleVerse[]{verse}, false);
        List<Bible> versions = new ArrayList<>();
        for (String name : BIBLES) {
            versions.add(BibleManager.get().getBibleFromName(name));
        }
        return passage.withVersions(versions);
    }

    private static void snapshot(Node node, String name) {
        try {
            File dir = new File("build/test-snapshots");
            dir.mkdirs();
            ImageIO.write(SwingFXUtils.fromFXImage(node.snapshot(null, null), null), "png", new File(dir, name));
        } catch (Exception ex) {
            // Only for looking at
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
