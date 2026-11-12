package org.quelea.testutil;

import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import org.quelea.services.utils.QueleaProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Shared setup for tests that need the JavaFX toolkit (but not video). Tests
 * using it are skipped where JavaFX can't be started.
 */
public final class FxTestSupport {

    private static Boolean available;

    private FxTestSupport() {
    }

    /**
     * Start JavaFX (once per test run), or skip the calling test if that isn't
     * possible here.
     */
    public static synchronized void assumeFxAvailable() {
        if (available == null) {
            try {
                CompletableFuture<Void> started = new CompletableFuture<>();
                Platform.startup(() -> started.complete(null));
                started.get(30, TimeUnit.SECONDS);
                available = true;
            } catch (IllegalStateException alreadyStarted) {
                available = true;
            } catch (Throwable ex) {
                available = false;
            }
            if (available) {
                Platform.setImplicitExit(false);
            }
        }
        Assumptions.assumeTrue(available, "JavaFX couldn't be started");
    }

    /**
     * Run the given code on the FX thread and wait for its result.
     */
    public static <T> T onFx(Callable<T> callable) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(callable.call());
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }

    /**
     * Point the properties at a fresh, empty Quelea home. Tests that use a
     * JUnit temp dir as home should call this afterwards, since other tests in
     * the same JVM keep using the properties after the temp dir is deleted.
     */
    public static void useScratchHome() throws Exception {
        Path scratch = Files.createTempDirectory("quelea-test-home");
        scratch.toFile().deleteOnExit();
        QueleaProperties.init(scratch.toString());
    }
}
