package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

/** Visibility of the projector is independent of the stage view and its controls. */
class PictureInPictureProjectionTest {
    private Stage main;
    private Stage projection;
    private Stage stageView;
    private PictureInPictureWindow pip;
    private int stageChanges;

    @BeforeEach
    void setUp() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            main = new Stage();
            main.setScene(new Scene(new StackPane(), 350, 200));
            main.show();
            projection = new Stage();
            projection.setScene(new Scene(new StackPane(), 300, 160));
            stageView = new Stage();
            stageView.setScene(new Scene(new StackPane(), 300, 160));
            stageView.show();
            stageView.showingProperty().addListener((o, before, after) -> stageChanges++);
            pip = new PictureInPictureWindow(main, () -> projection.getScene().getRoot(), () -> null,
                    () -> { }, () -> projection);
            return null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        onFx(() -> {
            main.hide();
            projection.hide();
            stageView.hide();
            return null;
        });
    }

    @Test
    void hidesVisibleProjectionAndRestoresItWithoutTouchingStageView() throws Exception {
        onFx(() -> {
            projection.show();
            pip.toggle();
            assertFalse(projection.isShowing(), "the main projection should be hidden in PiP");
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            pip.toggle();
            assertTrue(projection.isShowing(), "the previously visible projection should return");
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void initiallyHiddenProjectionStaysHiddenWhenPipCloses() throws Exception {
        onFx(() -> {
            pip.toggle();
            assertFalse(projection.isShowing());
            pip.toggle();
            assertFalse(projection.isShowing());
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void visibilityChangesAreDeferredAndRestoredAsRequested() throws Exception {
        onFx(() -> {
            projection.show();
            pip.toggle();
            assertTrue(pip.setProjectionShowing(projection, false));
            assertTrue(pip.setProjectionShowing(projection, true));
            assertFalse(projection.isShowing(), "a show request must not reveal the projection during PiP");
            assertTrue(pip.setProjectionShowing(projection, false));
            pip.toggle();
            assertFalse(projection.isShowing(), "a later hide request must not be undone on return");
            assertFalse(pip.setProjectionShowing(projection, true), "requests outside PiP should not be deferred");
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void aShowRequestDuringPipRestoresAnInitiallyHiddenProjection() throws Exception {
        onFx(() -> {
            pip.toggle();
            assertTrue(pip.setProjectionShowing(projection, true));
            assertFalse(projection.isShowing());
            pip.toggle();
            assertTrue(projection.isShowing());
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void anInitiallyHiddenStageViewStaysHidden() throws Exception {
        onFx(() -> {
            stageView.hide();
            stageChanges = 0;
            projection.show();
            pip.toggle();
            assertFalse(stageView.isShowing());
            pip.toggle();
            assertFalse(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void replacingProjectionDuringPipDoesNotReopenTheOldWindow() throws Exception {
        onFx(() -> {
            Stage oldProjection = projection;
            oldProjection.show();
            pip.toggle();
            projection = new Stage();
            projection.setScene(new Scene(new StackPane(), 200, 100));
            assertTrue(pip.setProjectionShowing(projection, true));
            assertFalse(projection.isShowing());
            pip.toggle();
            assertFalse(oldProjection.isShowing());
            assertTrue(projection.isShowing());
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }

    @Test
    void closingMainWindowDoesNotReshowTheProjection() throws Exception {
        onFx(() -> {
            projection.show();
            pip.toggle();
            main.hide();
            assertFalse(projection.isShowing(), "shutting down must not reopen the projection");
            assertTrue(stageView.isShowing());
            assertEquals(0, stageChanges);
            return null;
        });
    }
}
