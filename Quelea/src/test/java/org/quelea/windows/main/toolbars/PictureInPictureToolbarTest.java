package org.quelea.windows.main.toolbars;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.style.StyleManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class PictureInPictureToolbarTest {
    @Test
    void iconIsAccessibleAndAtTheRightEndEvenWithRecordingControls() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            Stage stage = new Stage();
            try {
                ToolBar toolbar = new ToolBar(new Button("First"), new Button("Second"));
                Region spacer = new Region();
                AtomicInteger activations = new AtomicInteger();
                Button pip = MainToolbar.addPictureInPictureControl(toolbar, spacer, activations::incrementAndGet);
                TextField recordingPath = new TextField("Recording");
                toolbar.getItems().add(toolbar.getItems().indexOf(spacer), recordingPath);
                VBox root = new VBox(toolbar);
                Scene scene = new Scene(root, 800, 100);
                StyleManager.apply(scene);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                Bounds toolbarBounds = toolbar.localToScene(toolbar.getBoundsInLocal());
                Bounds buttonBounds = pip.localToScene(pip.getBoundsInLocal());
                assertTrue(toolbarBounds.getMaxX() - buttonBounds.getMaxX() < 25,
                        "the picture-in-picture icon should sit at the right edge");
                assertEquals(pip, toolbar.getItems().getLast());
                assertTrue(pip.getText().isEmpty());
                assertNotNull(pip.getTooltip());
                assertNotNull(pip.getAccessibleText());
                assertNotNull(((Region) pip.getGraphic()).getShape(), "the picture-in-picture icon must be styled");
                pip.fire();
                assertEquals(1, activations.get());
            } finally {
                stage.hide();
            }
            return null;
        });
    }
}
