package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/** Opens a real full-screen window, so it is deliberately opt-in. */
@EnabledIfSystemProperty(named = "quelea.test.pipFullScreen", matches = "true")
class PictureInPictureFullscreenTest {
    @Test
    void fullScreenProjectionReturnsAfterPictureInPicture() throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1];
        Stage[] projection = new Stage[1];
        PictureInPictureWindow[] pip = new PictureInPictureWindow[1];
        try {
            onFx(() -> {
                main[0] = new Stage();
                main[0].setScene(new Scene(new StackPane(), 300, 200));
                main[0].show();
                projection[0] = new Stage();
                projection[0].setScene(new Scene(new StackPane(), 300, 200));
                projection[0].setFullScreenExitHint("");
                projection[0].setFullScreen(true);
                projection[0].show();
                pip[0] = new PictureInPictureWindow(main[0], () -> projection[0].getScene().getRoot(),
                        () -> null, () -> { }, () -> projection[0]);
                return null;
            });
            Thread.sleep(750); // Allow the window manager to enter full screen.
            Assumptions.assumeTrue(waitFor(() -> projection[0].isFullScreen(), 3_000),
                    "the window manager did not allow full screen");
            onFx(() -> {
                pip[0].toggle();
                assertFalse(projection[0].isShowing());
                return null;
            });
            Thread.sleep(750); // Allow any full-screen exit notification to arrive.
            onFx(() -> {
                pip[0].toggle();
                assertTrue(projection[0].isShowing());
                return null;
            });
            assertTrue(waitFor(() -> projection[0].isFullScreen(), 5_000),
                    "a full-screen projection should return full-screen");
        } finally {
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                if (projection[0] != null) projection[0].hide();
                return null;
            });
        }
    }
}
