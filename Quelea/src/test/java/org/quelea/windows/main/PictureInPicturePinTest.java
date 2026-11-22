package org.quelea.windows.main;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

class PictureInPicturePinTest {
    private Stage main;
    private PictureInPictureWindow pip;

    @BeforeEach
    void setUp() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            StackPane source = new StackPane(new Rectangle(400, 225, Color.BLUE));
            main = new Stage();
            main.setScene(new Scene(source, 400, 225));
            main.show();
            pip = new PictureInPictureWindow(main, () -> source, () -> null, () -> { }, () -> null);
            pip.toggle();
            return null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        onFx(() -> { if (main != null) main.hide(); return null; });
    }

    private Stage floating() {
        return Window.getWindows().stream().filter(w -> w instanceof Stage && w != main)
                .map(w -> (Stage) w).filter(w -> LabelGrabber.INSTANCE.getLabel("picture.in.picture.title")
                        .equals(w.getTitle())).findFirst().orElseThrow();
    }

    private void expand() throws Exception {
        onFx(() -> { PictureInPictureLogoTest.hover(floating(), true); return null; });
        assertTrue(waitFor(() -> floating().getScene().getWidth() >= 399
                && floating().getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000));
    }

    @Test
    void pinKeepsThePreviewOpenUntilUnpinned() throws Exception {
        expand();
        javafx.scene.paint.Paint[] unselectedColor = new javafx.scene.paint.Paint[1];
        onFx(() -> {
            var root = floating().getScene().getRoot();
            root.applyCss();
            root.layout();
            ToggleButton pin = (ToggleButton) root.lookup(".pip-pin");
            Button restore = (Button) root.lookup(".pip-return");
            assertTrue(pin.isVisible());
            assertFalse(pin.isSelected());
            assertTrue(pin.getBoundsInParent().getMaxX() < restore.getBoundsInParent().getMinX(),
                    "pin should sit directly to the left of return");
            assertNotNull(((javafx.scene.layout.Region) pin.getGraphic()).getShape(), "pin icon CSS must load");
            var pinIcon = (javafx.scene.layout.Region) pin.getGraphic();
            assertEquals(11, pinIcon.getPrefWidth(), 0.01);
            assertEquals(18, pinIcon.getPrefHeight(), 0.01, "the pin should retain its narrow proportions");
            assertEquals(LabelGrabber.INSTANCE.getLabel("picture.in.picture.pin"), pin.getTooltip().getText());
            unselectedColor[0] = pin.getBackground().getFills().get(0).getFill();
            pin.fire();
            pin.requestFocus();
            assertTrue(pin.isSelected());
            assertEquals(LabelGrabber.INSTANCE.getLabel("picture.in.picture.unpin"), pin.getTooltip().getText());
            assertEquals(pin.getTooltip().getText(), pin.getAccessibleText());
            PictureInPictureLogoTest.hover(floating(), false);
            return null;
        });
        assertTrue(waitFor(() -> !unselectedColor[0].equals(((ToggleButton) floating().getScene().getRoot()
                .lookup(".pip-pin")).getBackground().getFills().get(0).getFill()), 5000),
                "the selected pin should have a visible state");
        onFx(() -> {
            ToggleButton pin = (ToggleButton) floating().getScene().getRoot().lookup(".pip-pin");
            var icon = (javafx.scene.layout.Region) pin.getGraphic();
            assertEquals(Color.WHITE, icon.getBackground().getFills().get(0).getFill(),
                    "the selected icon must contrast with the selected background");
            if (pin.getBorder() != null) {
                pin.getBorder().getStrokes().forEach(stroke ->
                        assertNotEquals(Color.WHITE, stroke.getTopStroke(), "focus must not add a white circle"));
            }
            return null;
        });
        Thread.sleep(800); // Longer than the leave delay and collapse animation.
        onFx(() -> {
            var root = floating().getScene().getRoot();
            assertTrue(floating().getScene().getWidth() >= 399, "the pinned view should stay expanded");
            assertEquals(0, root.lookup(".pip-logo-cover").getOpacity(), 0.01);
            ((ToggleButton) root.lookup(".pip-pin")).fire();
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() <= 97
                && floating().getScene().getRoot().lookup(".pip-logo-cover").getOpacity() > 0.99, 5000),
                "unpinning with the pointer away should restore the compact tile");
        onFx(() -> {
            assertFalse(floating().getScene().getRoot().lookup(".pip-pin").isVisible(),
                    "the compact tile should keep its existing unobstructed logo");
            return null;
        });
    }

    @Test
    void pinSelectionIsKeptWhenPipIsReopenedAndReturnStillWorks() throws Exception {
        expand();
        onFx(() -> {
            ToggleButton pin = (ToggleButton) floating().getScene().getRoot().lookup(".pip-pin");
            pin.fire();
            PictureInPictureLogoTest.hover(floating(), false);
            pip.toggle();
            assertFalse(pip.isShowing());
            pip.toggle();
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() >= 399
                && floating().getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5000),
                "reopening should retain the user's pin selection");
        onFx(() -> {
            ToggleButton pin = (ToggleButton) floating().getScene().getRoot().lookup(".pip-pin");
            assertTrue(pin.isSelected());
            ((Button) floating().getScene().getRoot().lookup(".pip-return")).fire();
            assertFalse(pip.isShowing());
            return null;
        });
    }

    @Test
    void spaceIsAvailableToTheFocusedPinButton() throws Exception {
        expand();
        onFx(() -> {
            ToggleButton pin = (ToggleButton) floating().getScene().getRoot().lookup(".pip-pin");
            pin.requestFocus();
            assertSame(pin, floating().getScene().getFocusOwner());
            boolean[] received = {false};
            pin.addEventHandler(KeyEvent.KEY_PRESSED, event -> received[0] = true);
            Event.fireEvent(pin, new KeyEvent(KeyEvent.KEY_PRESSED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
            assertTrue(received[0], "the preview's hold-Space shortcut must not steal button activation");
            Event.fireEvent(pin, new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
            assertTrue(pin.isSelected(), "Space should toggle the focused pin button");
            return null;
        });
    }

    @Test
    void keyboardCanReachPinWithoutHoldingSpace() throws Exception {
        onFx(() -> {
            var root = (StackPane) floating().getScene().getRoot();
            root.getChildren().get(0).requestFocus();
            Event.fireEvent(floating().getScene(), new KeyEvent(KeyEvent.KEY_PRESSED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() >= 399, 5000));
        onFx(() -> {
            var scene = floating().getScene();
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "\t", "\t", KeyCode.TAB,
                    false, false, false, false));
            ((Button) scene.getRoot().lookup(".pip-return")).requestFocus();
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
            PictureInPictureLogoTest.hover(floating(), false);
            // A window resize can generate a mouse-moved event without the
            // pointer changing screen position. It must not end keyboard use.
            Event.fireEvent(scene, new MouseEvent(MouseEvent.MOUSE_MOVED, 20, 20, 20, 20,
                    MouseButton.NONE, 0, false, false, false, false, false, false, false,
                    false, false, true, null));
            return null;
        });
        Thread.sleep(800);
        onFx(() -> {
            var scene = floating().getScene();
            assertTrue(scene.getWidth() >= 399, "keyboard navigation should keep controls reachable");
            ToggleButton pin = (ToggleButton) scene.getRoot().lookup(".pip-pin");
            assertTrue(pin.isVisible());
            pin.requestFocus();
            assertSame(pin, scene.getFocusOwner());
            Event.fireEvent(scene, new MouseEvent(MouseEvent.MOUSE_PRESSED, 1, 1, 1, 1,
                    MouseButton.PRIMARY, 1, false, false, false, false, true, false, false,
                    false, false, true, null));
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() <= 97, 5000),
                "switching back to the mouse should restore hover behaviour");
    }

    @Test
    void movingMouseAfterKeyboardNavigationRestoresHoverBehaviour() throws Exception {
        expand();
        onFx(() -> {
            var scene = floating().getScene();
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "\t", "\t", KeyCode.TAB,
                    false, false, false, false));
            ((Button) scene.getRoot().lookup(".pip-return")).requestFocus();
            Event.fireEvent(scene, new MouseEvent(MouseEvent.MOUSE_MOVED, 45, 40, 45, 40,
                    MouseButton.NONE, 0, false, false, false, false, false, false, false,
                    false, false, true, null));
            PictureInPictureLogoTest.hover(floating(), false);
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() <= 97, 5_000),
                "moving the pointer away should leave keyboard navigation and collapse the preview");
    }

    @Test
    void leavingDirectlyAfterKeyboardNavigationRestoresHoverBehaviour() throws Exception {
        expand();
        onFx(() -> {
            var scene = floating().getScene();
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "\t", "\t", KeyCode.TAB,
                    false, false, false, false));
            ((Button) scene.getRoot().lookup(".pip-return")).requestFocus();
            Event.fireEvent(scene.getRoot(), new MouseEvent(MouseEvent.MOUSE_EXITED, 120, 120, 120, 120,
                    MouseButton.NONE, 0, false, false, false, false, false, false, false,
                    false, false, true, null));
            return null;
        });
        assertTrue(waitFor(() -> floating().getScene().getWidth() <= 97, 5_000),
                "a mouse exit at a new screen position should end keyboard navigation");
    }

    @Test
    void movingMouseAfterKeyboardNavigationDoesNotCollapsePinnedPreview() throws Exception {
        expand();
        onFx(() -> {
            var scene = floating().getScene();
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "\t", "\t", KeyCode.TAB,
                    false, false, false, false));
            ToggleButton pin = (ToggleButton) scene.getRoot().lookup(".pip-pin");
            pin.requestFocus();
            pin.fire();
            assertTrue(pin.isSelected());
            Event.fireEvent(scene.getRoot(), new MouseEvent(MouseEvent.MOUSE_EXITED, 120, 120, 120, 120,
                    MouseButton.NONE, 0, false, false, false, false, false, false, false,
                    false, false, true, null));
            return null;
        });
        Thread.sleep(800);
        assertTrue(onFx(() -> floating().getScene().getWidth() >= 399), "pinning should keep the preview expanded");
    }
}
