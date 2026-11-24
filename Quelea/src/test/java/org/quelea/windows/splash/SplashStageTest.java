package org.quelea.windows.splash;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.VersionType;
import org.quelea.testutil.FxTestSupport;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.FxTestSupport.onFx;

class SplashStageTest {

    @BeforeAll
    static void setUp() throws Exception {
        FxTestSupport.assumeAvailable();
    }

    @Test
    void eachReleaseTypeHasItsNewSplashArtwork() {
        assertEquals("icons/splash-new.png", VersionType.RELEASE.getSplashPath());
        assertEquals("icons/splash-beta-new.png", VersionType.BETA.getSplashPath());
        assertEquals("icons/splash-ci-new.png", VersionType.CI.getSplashPath());
        for (VersionType type : VersionType.values()) {
            Image image = new Image(new File(type.getSplashPath()).toURI().toString());
            assertFalse(image.isError(), "Could not load " + type.getSplashPath());
            assertEquals(1585, image.getWidth());
            assertEquals(992, image.getHeight());
        }
    }

    @Test
    void versionAndProgressArePlacedOnTheNewArtwork() throws Exception {
        onFx(() -> {
            SplashStage splash = new SplashStage();
            try {
                double width = splash.getScene().getWidth();
                double height = splash.getScene().getHeight();
                Text version = (Text) splash.getScene().lookup("#splash-version");
                ProgressBar bar = (ProgressBar) splash.getScene().lookup("#splash-progress");
                assertEquals(QueleaProperties.VERSION.getVersionString(), version.getText());
                assertTrue(version.getX() >= width * 0.884);
                assertTrue(version.getX() + version.getLayoutBounds().getWidth() <= width * 0.968);
                assertEquals(height * 0.075, version.getY(), 0.01);
                assertTrue(bar.getLayoutX() > width * 0.125, "The bar should follow the artwork's Loading text");
                assertTrue(bar.getLayoutX() + bar.getPrefWidth() >= width * 0.94);
                assertTrue(bar.getLayoutX() + bar.getPrefWidth() <= width * 0.98);
                assertTrue(bar.getLayoutY() > height * 0.89);
                assertEquals(0, bar.getProgress());
            } finally {
                splash.close();
            }
            return null;
        });
    }

    @Test
    void backgroundUpdatesAreOrderedAndProgressNeverMovesBackwards() throws Exception {
        SplashStage splash = onFx(SplashStage::new);
        try {
            splash.setProgress(0.3);
            splash.setProgress(0.7);
            splash.setProgress(0.4);
            awaitProgress(splash, 0.7);
            assertEquals(0.7, onFx(() -> ((ProgressBar) splash.getScene().lookup("#splash-progress")).getProgress()), 0.001);
            splash.setProgress(2);
            awaitProgress(splash, 1);
        } finally {
            onFx(() -> { splash.close(); return null; });
        }
    }

    @Test
    void finishShowsCompleteProgressBeforeContinuing() throws Exception {
        SplashStage splash = onFx(SplashStage::new);
        CompletableFuture<Double> finished = new CompletableFuture<>();
        try {
            onFx(() -> {
                splash.setProgress(0.5);
                splash.finish(() -> finished.complete(((ProgressBar) splash.getScene().lookup("#splash-progress")).getProgress()));
                assertFalse(finished.isDone(), "Completion should wait for the final animation");
                return null;
            });
            assertEquals(1, finished.get(5, TimeUnit.SECONDS), 0.001);
        } finally {
            onFx(() -> { splash.close(); return null; });
        }
    }

    @Test
    void rapidLateMilestonesAnimateBeforeTheSplashCloses() throws Exception {
        SplashStage splash = onFx(SplashStage::new);
        CompletableFuture<Boolean> finished = new CompletableFuture<>();
        try {
            onFx(() -> {
                splash.show();
                splash.setProgress(0.48);
                return null;
            });
            awaitProgress(splash, 0.48);
            onFx(() -> {
                splash.setProgress(0.85);
                splash.setProgress(0.94);
                splash.setProgress(0.97);
                splash.finish(() -> finished.complete(splash.isShowing()));
                double progress = ((ProgressBar) splash.getScene().lookup("#splash-progress")).getProgress();
                assertTrue(progress < 0.99, "Rapid milestones must not jump straight to completion");
                return null;
            });
            assertTrue(finished.get(5, TimeUnit.SECONDS), "The splash must remain visible during the animation");
            assertEquals(1, onFx(() -> ((ProgressBar) splash.getScene().lookup("#splash-progress")).getProgress()), 0.001);
        } finally {
            onFx(() -> { splash.close(); return null; });
        }
    }

    private static void awaitProgress(SplashStage splash, double expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (onFx(() -> ((ProgressBar) splash.getScene().lookup("#splash-progress")).getProgress()) >= expected - 0.001) {
                return;
            }
            Thread.sleep(20);
        }
        fail("Progress did not reach " + expected);
    }
}
