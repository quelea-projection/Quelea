package org.quelea.testutil;

import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import org.quelea.services.utils.QueleaProperties;

/** Starts JavaFX without making non-video tests depend on GStreamer. */
public final class FxTestSupport {
    private FxTestSupport() {
    }

    public static synchronized void assumeAvailable() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-fx-test").toString());
        }
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            Assumptions.assumeTrue(started.await(20, TimeUnit.SECONDS), "JavaFX did not start");
        } catch (IllegalStateException alreadyStarted) {
            // Another test started the toolkit.
        } catch (UnsupportedOperationException unavailable) {
            Assumptions.abort("JavaFX is unavailable: " + unavailable);
        }
        Platform.setImplicitExit(false);
    }

    public static <T> T onFx(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.call());
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result.get(20, TimeUnit.SECONDS);
    }
}
