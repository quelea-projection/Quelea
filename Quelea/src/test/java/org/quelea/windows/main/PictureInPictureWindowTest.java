package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class PictureInPictureWindowTest {
    private Stage main;
    private PictureInPictureWindow pictureInPicture;
    private final java.util.List<Boolean> iconifiedChanges = new java.util.concurrent.CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            StackPane canvas = new StackPane(new Rectangle(200, 100, Color.BLUE));
            main = new Stage();
            main.iconifiedProperty().addListener((obs, before, after) -> iconifiedChanges.add(after));
            main.setScene(new Scene(canvas, 400, 300));
            main.show();
            pictureInPicture = new PictureInPictureWindow(main, () -> canvas, () -> null, () -> { }, () -> null);
            return null;
        });
    }

    private Stage floatingWindow() {
        return Window.getWindows().stream().filter(w -> w instanceof Stage && w != main)
                .map(w -> (Stage) w).filter(w -> org.quelea.services.languages.LabelGrabber.INSTANCE
                        .getLabel("picture.in.picture.title").equals(w.getTitle()))
                .findFirst().orElseThrow();
    }

    @AfterEach
    void tearDown() throws Exception {
        onFx(() -> {
            main.hide();
            return null;
        });
    }

    @Test
    void floatingWindowRemainsIndependentAndCanRestoreMainWindow() throws Exception {
        onFx(() -> {
            pictureInPicture.toggle();
            Stage floating = floatingWindow();
            assertTrue(floating.isAlwaysOnTop());
            assertEquals(StageStyle.UNDECORATED, floating.getStyle());
            assertNull(floating.getOwner());
            assertTrue(floating.isShowing());
            assertTrue(main.isIconified());
            assertEquals(javafx.geometry.Insets.EMPTY, ((StackPane) floating.getScene().getRoot()).getPadding(),
                    "the floating preview should extend to the window edges");
            return null;
        });
        Thread.sleep(500); // Give the window manager time to process minimisation.
        String beforeRestore = onFx(() -> {
            Stage floating = floatingWindow();
            String state = "main showing=" + main.isShowing() + ", iconified=" + main.isIconified();
            floating.getScene().getAccelerators().get(MainWindow.PICTURE_IN_PICTURE_SHORTCUT).run();
            assertFalse(floating.isShowing());
            return state + "; immediately after: showing=" + main.isShowing() + ", iconified=" + main.isIconified();
        });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> !main.isIconified(), 10_000),
                () -> "the window manager should restore the main window; before: " + beforeRestore + "; changes=" + iconifiedChanges);
    }

    private static MouseEvent mouse(EventType<MouseEvent> type, double x, double y,
            MouseButton button, int clicks, boolean primaryDown, boolean still) {
        return new MouseEvent(type, x, y, x, y, button, clicks,
                false, false, false, false, primaryDown, false, false,
                false, false, still, null);
    }

    @Test
    void overlayButtonAndDoubleClickReturnToQuelea() throws Exception {
        onFx(() -> {
            pictureInPicture.toggle();
            Stage floating = floatingWindow();
            floating.getScene().getRoot().applyCss();
            Button button = (Button) floating.getScene().getRoot().lookup(".pip-return");
            assertNotNull(button);
            assertTrue(button.getText().isEmpty(), "the return control should be an icon, not a text bar");
            assertNotNull(button.getTooltip());
            assertNotNull(button.getAccessibleText());
            assertNotNull(((javafx.scene.layout.Region) button.getGraphic()).getShape(), "return icon CSS must load");
            button.fire();
            assertFalse(floating.isShowing());
            pictureInPicture.toggle();
            StackPane root = (StackPane) floating.getScene().getRoot();
            Node preview = root.getChildren().get(0);
            Event.fireEvent(preview, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.PRIMARY, 2, true, true));
            Event.fireEvent(preview, mouse(MouseEvent.MOUSE_RELEASED, 100, 100, MouseButton.PRIMARY, 2, false, true));
            Event.fireEvent(preview, mouse(MouseEvent.MOUSE_CLICKED, 100, 100, MouseButton.PRIMARY, 2, false, true));
            assertFalse(floating.isShowing(), "double-clicking the preview should return to Quelea");
            return null;
        });
    }

    @Test
    void previewCanBeMovedAndResizedWithoutWindowDecorations() throws Exception {
        onFx(() -> { pictureInPicture.toggle(); PictureInPictureLogoTest.hover(floatingWindow(), true); return null; });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> floatingWindow().getScene().getWidth() >= 399, 5000));
        double[] origin = new double[2];
        onFx(() -> {
            Stage floating = floatingWindow();
            StackPane root = (StackPane) floating.getScene().getRoot();
            root.applyCss();
            Node preview = root.getChildren().get(0);
            origin[0] = floating.getX(); origin[1] = floating.getY();
            Event.fireEvent(preview, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.PRIMARY, 1, true, true));
            Event.fireEvent(preview, mouse(MouseEvent.MOUSE_DRAGGED, 145, 125, MouseButton.PRIMARY, 1, true, false));
            return null;
        });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> Math.abs(origin[0] + 45 - floatingWindow().getX()) < 2
                && Math.abs(origin[1] + 25 - floatingWindow().getY()) < 2, 5000), "drag should move the floating window");
        onFx(() -> {
            Stage floating = floatingWindow();
            StackPane root = (StackPane) floating.getScene().getRoot();
            Node grip = root.lookup(".pip-resize-grip");
            assertNotNull(grip);
            double width = floating.getWidth(), height = floating.getHeight();
            double frameWidth = width - floating.getScene().getWidth();
            double frameHeight = height - floating.getScene().getHeight();
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.PRIMARY, 1, true, true));
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_DRAGGED, 160, 135, MouseButton.PRIMARY, 1, true, false));
            assertEquals(width + 60, floating.getWidth(), 1);
            assertEquals(height + 35, floating.getHeight(), 1);
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.PRIMARY, 1, true, true));
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_DRAGGED, -1000, -1000, MouseButton.PRIMARY, 1, true, false));
            assertEquals(240 + frameWidth, floating.getWidth(), 2);
            assertEquals(135 + frameHeight, floating.getHeight(), 2);
            assertTrue(floating.isShowing());
            return null;
        });
    }

    @Test
    void escapeAndKeyboardResizingWork() throws Exception {
        onFx(() -> { pictureInPicture.toggle(); PictureInPictureLogoTest.hover(floatingWindow(), true); return null; });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> floatingWindow().getScene().getWidth() >= 399, 5000));
        double[] origin = new double[1];
        onFx(() -> {
            Stage floating = floatingWindow();
            double width = floating.getWidth();
            boolean mac = org.quelea.services.utils.Utils.isMac();
            Event.fireEvent(floating.getScene(), new KeyEvent(KeyEvent.KEY_PRESSED, "+", "+", KeyCode.EQUALS,
                    false, !mac, false, mac));
            assertTrue(floating.getWidth() > width, "shortcut-plus should enlarge the preview");
            origin[0] = floating.getX();
            Event.fireEvent(floating.getScene(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT,
                    false, false, true, false));
            return null;
        });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> Math.abs(origin[0] - 20 - floatingWindow().getX()) < 2,
                5000), "Alt-arrow should move the borderless window");
        onFx(() -> {
            Stage floating = floatingWindow();
            Event.fireEvent(floating.getScene(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));
            assertFalse(floating.isShowing());
            return null;
        });
    }

    @Test
    void closingMainWindowAlsoClosesFloatingWindow() throws Exception {
        onFx(() -> {
            pictureInPicture.toggle();
            Stage floating = floatingWindow();
            main.hide();
            assertFalse(floating.isShowing());
            return null;
        });
    }

    @Test
    void repeatedTogglesReuseTheWindow() throws Exception {
        Stage first = onFx(() -> {
            pictureInPicture.toggle();
            Stage window = floatingWindow();
            pictureInPicture.toggle();
            assertFalse(window.isShowing());
            pictureInPicture.toggle();
            assertSame(window, floatingWindow());
            assertTrue(window.isShowing());
            return window;
        });
        Thread.sleep(1_500);
        onFx(() -> {
            assertTrue(first.isShowing(), "an old restore request must not close a newly opened preview");
            assertTrue(main.isIconified());
            return null;
        });
    }

    @Test
    void minimisedWindowCanBeShownAgain() throws Exception {
        onFx(() -> { main.setIconified(true); return null; });
        Thread.sleep(500);
        onFx(() -> {
            main.hide();
            main.setIconified(false);
            main.show();
            main.toFront();
            return null;
        });
        assertTrue(org.quelea.testutil.VideoTestSupport.waitFor(() -> !main.isIconified(), 5_000),
                () -> "showing the operator window again should restore it; changes=" + iconifiedChanges);
    }

    @Test
    void returningDoesNotHideAnOwnedDialog() throws Exception {
        Stage[] dialog = new Stage[1];
        int[] mainHides = {0};
        try {
            onFx(() -> {
                dialog[0] = new Stage();
                dialog[0].initOwner(main);
                dialog[0].setScene(new Scene(new StackPane(), 120, 80));
                dialog[0].show();
                main.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> mainHides[0]++);
                pictureInPicture.toggle();
                return null;
            });
            Thread.sleep(500);
            onFx(() -> { pictureInPicture.toggle(); return null; });
            Thread.sleep(1_500);
            onFx(() -> {
                assertEquals(0, mainHides[0], "the restore fallback must not hide a window with an owned dialog");
                assertTrue(dialog[0].isShowing(), "the owned dialog must remain open");
                if (main.isIconified()) {
                    Stage floating = floatingWindow();
                    assertTrue(floating.isShowing(), "if restoration was rejected, keep the preview available");
                    assertTrue(floating.getScene().getRoot().lookup(".pip-hint").isVisible(),
                            "explain how to restore the main window without hiding the dialog");
                } else {
                    assertFalse(pictureInPicture.isShowing(), "a restored owner must not reopen the preview over its dialog");
                }
                return null;
            });
        } finally {
            onFx(() -> { if (dialog[0] != null) dialog[0].hide(); return null; });
        }
    }
}
