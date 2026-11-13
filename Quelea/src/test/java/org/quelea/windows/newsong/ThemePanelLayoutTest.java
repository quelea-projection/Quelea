package org.quelea.windows.newsong;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.DisplayPreview;

/**
 * The theme preview must fit inside the theme panel as soon as the panel is
 * shown, e.g. when the theme tab of the song editor is opened (it used to
 * start out too big and only fit after the window was resized.)
 */
@DisplayName("ThemePanelLayoutTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThemePanelLayoutTest {

    private static DisplayStage previousProjection;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-theme-panel-test").toString());
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
    @Order(1)
    public void previewFitsInContentSizedWindow() throws Exception {
        assertPreviewFits(-1, -1, -1);
    }

    @Test
    @Order(2)
    public void previewFitsInLargeWindow() throws Exception {
        assertPreviewFits(1100, 760, -1);
    }

    @Test
    @Order(3)
    public void previewFitsAfterWindowShrinks() throws Exception {
        assertPreviewFits(1100, 760, 560);
    }

    /**
     * Open the theme tab of a new song window and check the preview fits.
     *
     * @param width the window width, or -1 to size the window to its content.
     * @param height the window height (when a width is given.)
     * @param shrinkTo if not -1, the height to shrink the window to after the
     * theme tab was laid out at the first size.
     */
    private static void assertPreviewFits(double width, double height, double shrinkTo) throws Exception {
        ThemePanel[] panel = new ThemePanel[1];
        TabPane tabs = onFx(() -> {
            SongEntryWindow window = new SongEntryWindow();
            window.resetNewSong();
            TabPane pane = (TabPane) window.getScene().lookup(".tab-pane");
            panel[0] = (ThemePanel) pane.getTabs().get(pane.getTabs().size() - 1).getContent();
            if (width > 0) {
                window.setWidth(width);
                window.setHeight(height);
            }
            window.show();
            return pane;
        });
        Thread.sleep(500);
        onFx(() -> {
            tabs.getSelectionModel().selectLast();
            return null;
        });
        Thread.sleep(800);
        if (shrinkTo > 0) {
            onFx(() -> {
                ((Stage) tabs.getScene().getWindow()).setHeight(shrinkTo);
                ((Stage) tabs.getScene().getWindow()).setWidth(800);
                return null;
            });
            Thread.sleep(800);
        }
        String problem = onFx(() -> {
            Node previewNode = findPreview(panel[0]);
            Bounds previewBounds = panel[0].sceneToLocal(previewNode.localToScene(previewNode.getLayoutBounds()));
            tabs.getScene().getWindow().hide();
            if (previewBounds.getMaxY() > panel[0].getHeight() + 1) {
                return "preview bottom " + previewBounds.getMaxY() + " is below the panel (" + panel[0].getHeight() + ")";
            }
            if (previewBounds.getHeight() < 100) {
                return "preview is only " + previewBounds.getHeight() + "px high";
            }
            return null;
        });
        Assertions.assertNull(problem, problem);
    }

    private static Node findPreview(Node node) {
        if (node instanceof DisplayPreview) {
            return node;
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                Node found = findPreview(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static <R> R onFx(Supplier<R> supplier) throws Exception {
        CompletableFuture<R> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(supplier.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }
}
