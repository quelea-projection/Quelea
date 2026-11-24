package org.quelea.windows.main;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.robot.Robot;
import javafx.geometry.Point2D;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assumptions;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

class PictureInPictureResizeTest {
    @Test
    @Disabled("WSLg can reject an unfocused window move while PiP expands at the screen edge")
    void realHoverAtScreenEdgeKeepsTheControlsOnscreen() throws Exception {
        // WSLg can restore the old window position after each resize while the
        // pointer is over PiP. The expanded pin/return buttons can then end up
        // beyond the screen edge, despite the requested bounds being correct.
        // Keeping these controls reachable matters because PiP is meant to be
        // used while another application has focus. Investigate an atomic native
        // move/resize or another placement strategy that preserves that focus,
        // then verify native Ubuntu Wayland and X11 as well as WSLg.
        Assumptions.assumeTrue(System.getenv("WSL_DISTRO_NAME") != null, "requires WSLg");
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        Point2D[] originalPointer = new Point2D[1];
        javafx.geometry.Rectangle2D[] screen = new javafx.geometry.Rectangle2D[1];
        try {
            onFx(() -> {
                main[0] = new Stage();
                var source = new StackPane(new Rectangle(400, 225, Color.BLUE));
                main[0].setScene(new Scene(source, 400, 225));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> null).toggle();
                floating[0] = (Stage) Window.getWindows().stream()
                        .filter(w -> w instanceof Stage && w != main[0]).findFirst().orElseThrow();
                originalPointer[0] = new Robot().getMousePosition();
                return null;
            });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 96) < 2
                    && floating[0].getOpacity() == 1, 5000));
            Thread.sleep(400);
            onFx(() -> {
                var stage = floating[0];
                var screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
                screen[0] = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
                var root = stage.getScene().getRoot();
                var bounds = root.localToScreen(root.getBoundsInLocal());
                new Robot().mouseMove((bounds.getMinX() + bounds.getMaxX()) / 2, (bounds.getMinY() + bounds.getMaxY()) / 2);
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-pin").getOpacity() >= 0.99, 5000));
            onFx(() -> {
                var pin = floating[0].getScene().getRoot().lookup(".pip-pin");
                var bounds = pin.localToScreen(pin.getBoundsInLocal());
                assertTrue(bounds.getMinX() >= screen[0].getMinX() && bounds.getMaxX() <= screen[0].getMaxX(),
                        "the pin must remain reachable after expansion");
                return null;
            });
        } finally {
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                if (originalPointer[0] != null) new Robot().mouseMove(originalPointer[0]);
                return null;
            });
        }
    }

    @Test
    void controlsStayHiddenDuringResizeAndReturnWhenGeometrySettles() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        int[] resizeSamples = {0};
        boolean[] visibleWhileResizing = {false};
        try {
            onFx(() -> {
                StackPane source = new StackPane(new Rectangle(400, 225, Color.RED));
                new Scene(source, 400, 225);
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 300, 200));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> null).toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                                .equals(w.getTitle())).findFirst().orElseThrow();
                return null;
            });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 96) < 2
                    && floating[0].getOpacity() == 1, 5000));
            Thread.sleep(350); // Let the initial window-manager measurement finish.
            onFx(() -> {
                var scene = floating[0].getScene();
                scene.widthProperty().addListener((obs, before, after) -> {
                    if (after.doubleValue() > 110 && after.doubleValue() < 380) {
                        resizeSamples[0]++;
                        var root = scene.getRoot();
                        if (root.lookup(".pip-return").getOpacity() > 0.02
                                || root.lookup(".pip-pin").getOpacity() > 0.02
                                || root.lookup(".pip-resize-grip").getOpacity() > 0.02) {
                            visibleWhileResizing[0] = true;
                        }
                    }
                });
                PictureInPictureLogoTest.hover(floating[0], true);
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99, 5000));
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], false); return null; });
            Thread.sleep(260); // Reverse after collapse has started.
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], true); return null; });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99, 5000));
            onFx(() -> {
                var scene = floating[0].getScene();
                boolean mac = org.quelea.services.utils.Utils.isMac();
                Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "+", "+", KeyCode.EQUALS,
                        false, !mac, false, mac));
                assertEquals(0, scene.getRoot().lookup(".pip-return").getOpacity(), 0.01,
                        "keyboard resizing should hide the moving controls too");
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99, 5000));
            onFx(() -> {
                var root = floating[0].getScene().getRoot();
                assertTrue(resizeSamples[0] > 1, "the expansion should still animate");
                assertFalse(visibleWhileResizing[0], "controls should not jump while the window changes size");
                assertTrue(root.lookup(".pip-pin").isVisible());
                assertFalse(root.lookup(".pip-return").isMouseTransparent());
                Node grip = root.lookup(".pip-resize-grip");
                Event.fireEvent(grip, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, true));
                assertEquals(0, grip.getOpacity(), 0.01);
                assertFalse(grip.isMouseTransparent(), "the grip must retain its mouse grab during a drag");
                assertTrue(root.lookup(".pip-return").isMouseTransparent());
                Event.fireEvent(grip, mouse(MouseEvent.MOUSE_DRAGGED, 150, 130, true));
                Event.fireEvent(grip, mouse(MouseEvent.MOUSE_RELEASED, 150, 130, false));
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99, 5000),
                    "controls should return after a manual resize");
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], false); return null; });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() <= 97
                    && floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99, 5000));
            onFx(() -> {
                assertFalse(visibleWhileResizing[0], "controls should also stay hidden while collapsing");
                assertFalse(floating[0].getScene().getRoot().lookup(".pip-pin").isVisible());
                assertFalse(floating[0].getScene().getRoot().lookup(".pip-resize-grip").isMouseTransparent());
                return null;
            });
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); return null; });
        }
    }

    @Test
    void expandsInwardAtScreenEdgesAndRecoversFromRapidHoverChanges() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        double[] edge = new double[2];
        try {
            onFx(() -> {
                StackPane source = new StackPane(new Rectangle(400, 225, Color.RED));
                new Scene(source, 400, 225);
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 300, 200));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> null).toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                                .equals(w.getTitle())).findFirst().orElseThrow();
                return null;
            });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 96) < 2
                    && Math.abs(floating[0].getScene().getHeight() - 96) < 2
                    && floating[0].getOpacity() == 1, 5000));
            Thread.sleep(350);
            onFx(() -> {
                var bounds = Screen.getPrimary().getVisualBounds();
                edge[0] = bounds.getMaxX() - 10;
                edge[1] = bounds.getMaxY() - 10;
                floating[0].setX(edge[0] - floating[0].getWidth());
                floating[0].setY(edge[1] - floating[0].getHeight());
                return null;
            });
            Thread.sleep(300); // Let the window manager report its actual position.
            onFx(() -> {
                edge[0] = floating[0].getX() + floating[0].getWidth();
                edge[1] = floating[0].getY() + floating[0].getHeight();
                PictureInPictureLogoTest.hover(floating[0], true);
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01
                    && floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99
                    && Math.abs(edge[0] - floating[0].getX() - floating[0].getWidth()) < 3
                    && Math.abs(edge[1] - floating[0].getY() - floating[0].getHeight()) < 3, 5000),
                    onFx(() -> PictureInPictureLogoTest.state(floating[0])));
            onFx(() -> {
                assertEquals(edge[0], floating[0].getX() + floating[0].getWidth(), 3);
                assertEquals(edge[1], floating[0].getY() + floating[0].getHeight(), 3);
                PictureInPictureLogoTest.hover(floating[0], false);
                return null;
            });
            Thread.sleep(240); // Interrupt the collapse after its short exit delay.
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], true); return null; });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000));
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], false); return null; });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 96) < 2
                    && floating[0].getScene().getRoot().lookup(".pip-return").getOpacity() >= 0.99
                    && Math.abs(edge[0] - floating[0].getX() - floating[0].getWidth()) < 3
                    && Math.abs(edge[1] - floating[0].getY() - floating[0].getHeight()) < 3, 5000),
                    onFx(() -> PictureInPictureLogoTest.state(floating[0])));
            onFx(() -> {
                assertEquals(edge[0], floating[0].getX() + floating[0].getWidth(), 3);
                assertEquals(edge[1], floating[0].getY() + floating[0].getHeight(), 3);
                return null;
            });
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); return null; });
        }
    }

    @Test
    void previewScalesBothSidesWithoutClippingAndRemembersTheChosenSize() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        try {
            onFx(() -> {
                HBox source = new HBox(new Rectangle(200, 225, Color.RED), new Rectangle(200, 225, Color.BLUE));
                new Scene(source, 400, 225);
                source.resize(400, 225);
                source.layout();
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 300, 200));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> null).toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                                .equals(w.getTitle())).findFirst().orElseThrow();
                PictureInPictureLogoTest.hover(floating[0], true);
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000));
            assertBothSides(floating[0]);
            // Exercise the real resize grip in both directions, not just the Stage API.
            resize(floating[0], -150, -80);
            boolean shrunk = waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 250) < 2, 5000);
            assertTrue(shrunk, onFx(() -> PictureInPictureLogoTest.state(floating[0])));
            assertBothSides(floating[0]);
            resize(floating[0], 350, 255);
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 600) < 2, 5000));
            assertBothSides(floating[0]);
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], false); return null; });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() <= 97, 5000));
            onFx(() -> { PictureInPictureLogoTest.hover(floating[0], true); return null; });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 600) < 2
                    && Math.abs(floating[0].getScene().getHeight() - 400) < 2, 5000), "the preview size should be retained");
            assertBothSides(floating[0]);
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); return null; });
        }
    }

    private static void resize(Stage stage, double dx, double dy) throws Exception {
        onFx(() -> {
            var grip = stage.getScene().getRoot().lookup(".pip-resize-grip");
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_PRESSED, 100, 100, true));
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_DRAGGED, 100 + dx, 100 + dy, true));
            Event.fireEvent(grip, mouse(MouseEvent.MOUSE_RELEASED, 100 + dx, 100 + dy, false));
            return null;
        });
    }

    private static MouseEvent mouse(javafx.event.EventType<MouseEvent> type, double x, double y, boolean down) {
        return new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, down, false, false, false, false, false, null);
    }

    private static void assertBothSides(Stage stage) throws Exception {
        assertTrue(waitFor(() -> {
            WritableImage image = stage.getScene().getRoot().snapshot(null, null);
            int y = (int) image.getHeight() / 2;
            return Color.RED.equals(image.getPixelReader().getColor((int) image.getWidth() / 4, y))
                    && Color.BLUE.equals(image.getPixelReader().getColor((int) image.getWidth() * 3 / 4, y));
        }, 5000), "both sides of the live content should remain visible at the current size");
    }
}
