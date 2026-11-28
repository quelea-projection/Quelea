package org.quelea.windows.main;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.FxTestSupport;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/** Uses the desktop pointer and real window events; opt in on an interactive desktop. */
@EnabledIfSystemProperty(named = "quelea.test.pipHover", matches = "true")
class PictureInPictureHoverTest {
    @RepeatedTest(4)
    void leavingExpandedPreviewRestoresTheLogo(@TempDir Path directory, RepetitionInfo repetition) throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1], floating = new Stage[1];
        Point2D original = onFx(() -> new Robot().getMousePosition());
        Process other = null;
        try {
            onFx(() -> {
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                new Robot().mouseMove(bounds.getMinX() + 10, bounds.getMinY() + 10);
                main[0] = new Stage();
                StackPane content = new StackPane();
                main[0].setScene(new Scene(content, 400, 300));
                main[0].show();
                new PictureInPictureWindow(main[0], () -> content, () -> null, () -> { }, () -> null).toggle();
                floating[0] = (Stage) Window.getWindows().stream()
                        .filter(w -> w instanceof Stage && w != main[0]).findFirst().orElseThrow();
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getOpacity() == 1 && floating[0].getScene().getWidth() <= 97, 5_000));
            other = PictureInPictureFocusTest.showOtherApplication(directory);
            onFx(() -> {
                Bounds bounds = floating[0].getScene().getRoot().localToScreen(floating[0].getScene().getRoot().getBoundsInLocal());
                new Robot().mouseMove((bounds.getMinX() + bounds.getMaxX()) / 2, (bounds.getMinY() + bounds.getMaxY()) / 2);
                return null;
            });
            double widthBeforeExit = repetition.getCurrentRepetition() == 2 ? 140 : 399;
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() >= widthBeforeExit, 5_000), "Hover should expand PiP");
            if (repetition.getCurrentRepetition() == 3) {
                onFx(() -> {
                    ToggleButton pin = (ToggleButton) floating[0].getScene().getRoot().lookup(".pip-pin");
                    pin.fire();
                    assertTrue(pin.isSelected());
                    pin.fire();
                    assertFalse(pin.isSelected());
                    return null;
                });
            } else if (repetition.getCurrentRepetition() == 4) {
                onFx(() -> {
                    Scene scene = floating[0].getScene();
                    Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "\t", "\t", KeyCode.TAB,
                            false, false, false, false));
                    ((Button) scene.getRoot().lookup(".pip-return")).requestFocus();
                    return null;
                });
            }
            onFx(() -> {
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                new Robot().mouseMove(bounds.getMinX() + 10, bounds.getMinY() + 10);
                return null;
            });
            assertTrue(waitFor(() -> floating[0].getScene().getWidth() <= 97, 5_000),
                    () -> {
                        try { return onFx(() -> PictureInPictureLogoTest.state(floating[0])); }
                        catch (Exception ex) { return ex.toString(); }
                    });
        } finally {
            if (other != null) {
                other.destroy();
                if (!other.waitFor(5, TimeUnit.SECONDS)) {
                    other.destroyForcibly();
                    other.waitFor(5, TimeUnit.SECONDS);
                }
            }
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                new Robot().mouseMove(original);
                return null;
            });
        }
    }
}
