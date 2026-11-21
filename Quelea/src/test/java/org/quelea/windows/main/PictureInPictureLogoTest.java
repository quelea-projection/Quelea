package org.quelea.windows.main;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

class PictureInPictureLogoTest {
    static void hover(Stage stage, boolean entered) {
        Event.fireEvent(stage.getScene().getRoot(), new MouseEvent(
                entered ? MouseEvent.MOUSE_ENTERED : MouseEvent.MOUSE_EXITED,
                20, 20, 20, 20, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null));
    }

    @Test
    void expandsAndFadesToCurrentLiveContentThenReturnsToCompactLogo() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], projection = new Stage[1], floating = new Stage[1];
        Rectangle[] live = new Rectangle[1];
        boolean[] intermediateOpacity = {false};
        try {
            onFx(() -> {
                live[0] = new Rectangle(400, 225, Color.RED);
                StackPane source = new StackPane(live[0]);
                projection[0] = new Stage();
                projection[0].setScene(new Scene(source, 400, 225));
                projection[0].show();
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 300, 200));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> projection[0]).toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0] && w != projection[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                                .equals(w.getTitle())).findFirst().orElseThrow();
                var root = floating[0].getScene().getRoot();
                root.applyCss();
                Node cover = root.lookup(".pip-logo-cover");
                ImageView logo = (ImageView) root.lookup(".pip-logo-mark");
                assertTrue(cover.isMouseTransparent());
                assertFalse(logo.getImage().isError());
                assertNotNull(root.lookup(".pip-return"));
                assertNotNull(root.lookup(".pip-resize-grip"));
                cover.opacityProperty().addListener((obs, old, value) -> {
                    if (value.doubleValue() > 0.01 && value.doubleValue() < 0.99) intermediateOpacity[0] = true;
                });
                return null;
            });
            assertTrue(waitFor(() -> Math.abs(floating[0].getScene().getWidth() - 96) < 2
                    && Math.abs(floating[0].getScene().getHeight() - 96) < 2, 5000),
                    onFx(() -> state(floating[0])));
            onFx(() -> { hover(floating[0], true); return null; });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399 && floating[0].getScene().getHeight() >= 224
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000));
            assertTrue(intermediateOpacity[0], "the logo should fade rather than disappear instantly");
            awaitCenter(floating[0], Color.RED);
            onFx(() -> { hover(floating[0], false); live[0].setFill(Color.BLUE); return null; });
            boolean collapsed = waitFor(() -> floating[0].getScene().getWidth() <= 97 && floating[0].getScene().getHeight() <= 97
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() > 0.99, 5000);
            assertTrue(collapsed, onFx(() -> state(floating[0])));
            assertTrue(waitFor(() -> {
                ImageView frame = (ImageView) floating[0].getScene().getRoot().lookup(".pip-frame");
                return frame.getImage() != null && Color.BLUE.equals(center(frame.getImage()));
            }, 5000), "the live view keeps updating behind the logo");
            onFx(() -> { hover(floating[0], true); return null; });
            awaitCenter(floating[0], Color.BLUE);
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); if (projection[0] != null) projection[0].hide(); return null; });
        }
    }

    @Test
    void keyboardCanTemporarilyRevealThePreviewWithoutStealingButtonSpace() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        try {
            onFx(() -> {
                StackPane source = new StackPane(new Rectangle(200, 100, Color.RED));
                main[0] = new Stage();
                main[0].setScene(new Scene(source, 300, 200));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> source, () -> null, () -> { }, () -> null).toggle();
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != main[0])
                        .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                                .equals(w.getTitle())).findFirst().orElseThrow();
                StackPane root = (StackPane) floating[0].getScene().getRoot();
                root.getChildren().get(0).requestFocus();
                Event.fireEvent(floating[0].getScene(), key(KeyEvent.KEY_PRESSED));
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= 399
                    && floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000));
            onFx(() -> { Event.fireEvent(floating[0].getScene(), key(KeyEvent.KEY_RELEASED)); return null; });
            boolean collapsed = waitFor(() -> floating[0].getScene().getWidth() <= 97, 5000);
            assertTrue(collapsed, onFx(() -> state(floating[0])));
            onFx(() -> {
                Button restore = (Button) floating[0].getScene().getRoot().lookup(".pip-return");
                restore.requestFocus();
                assertSame(restore, floating[0].getScene().getFocusOwner());
                boolean[] received = {false};
                restore.addEventHandler(KeyEvent.KEY_PRESSED, e -> received[0] = true);
                Event.fireEvent(restore, key(KeyEvent.KEY_PRESSED));
                assertTrue(received[0], "Space must remain available to the focused return button");
                return null;
            });
        } finally {
            onFx(() -> { if (main[0] != null) main[0].hide(); return null; });
        }
    }

    private static KeyEvent key(javafx.event.EventType<KeyEvent> type) {
        return new KeyEvent(type, " ", " ", KeyCode.SPACE, false, false, false, false);
    }

    static String state(Stage stage) {
        var root = stage.getScene().getRoot();
        return "stage=" + stage.getWidth() + "x" + stage.getHeight() + ", scene="
                + stage.getScene().getWidth() + "x" + stage.getScene().getHeight()
                + ", opacity=" + root.lookup(".pip-logo-cover").getOpacity() + ", hover=" + root.isHover();
    }

    private static Color center(javafx.scene.image.Image image) {
        return image.getPixelReader().getColor((int) image.getWidth() / 2, (int) image.getHeight() / 2);
    }

    private static void awaitCenter(Stage stage, Color color) throws Exception {
        assertTrue(waitFor(() -> color.equals(center(stage.getScene().getRoot().snapshot(null, null))), 10_000),
                "The preview did not show " + color);
    }
}
