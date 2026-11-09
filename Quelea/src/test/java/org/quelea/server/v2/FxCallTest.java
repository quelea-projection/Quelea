package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests FxCall against a stand-in "FX thread", so no JavaFX toolkit is needed.
 */
class FxCallTest {

    private final ExecutorService fxThread = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        fxThread.shutdownNow();
    }

    private FxCall offFxThread() {
        return new FxCall(fxThread, () -> false);
    }

    @Test
    void returnsResultFromFxThread() {
        String threadName = offFxThread().call(() -> Thread.currentThread().getName());
        assertFalse(threadName.equals(Thread.currentThread().getName()));
    }

    @Test
    void runsDirectlyWhenAlreadyOnFxThread() {
        FxCall onFx = new FxCall(r -> {
            throw new AssertionError("must not dispatch");
        }, () -> true);
        assertEquals(Thread.currentThread().getName(), onFx.call(() -> Thread.currentThread().getName()));
    }

    @Test
    void timesOutAndCancelsTaskThatHasNotStarted() throws InterruptedException {
        // Block the "FX thread" so the task stays queued.
        fxThread.execute(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        AtomicBoolean ran = new AtomicBoolean();

        assertThrows(FxCall.TimeoutException.class,
                () -> offFxThread().call(() -> ran.getAndSet(true), Duration.ofMillis(50)));

        fxThread.shutdown();
        assertTrue(fxThread.awaitTermination(5, TimeUnit.SECONDS));
        assertFalse(ran.get(), "a timed-out command must not run later");
    }

    @Test
    void rethrowsRuntimeExceptionsUnchanged() {
        NoSuchItemException thrown = new NoSuchItemException("it_1");
        NoSuchItemException caught = assertThrows(NoSuchItemException.class, () -> offFxThread().call(() -> {
            throw thrown;
        }));
        assertSame(thrown, caught);
    }

    @Test
    void wrapsCheckedExceptions() {
        FxCall.CallException caught = assertThrows(FxCall.CallException.class, () -> offFxThread().call(() -> {
            throw new IOException("disk");
        }));
        assertTrue(caught.getCause() instanceof IOException);
    }

    @Test
    void wrapsCheckedExceptionsOnFxThreadToo() {
        FxCall onFx = new FxCall(Runnable::run, () -> true);
        assertThrows(FxCall.CallException.class, () -> onFx.call(() -> {
            throw new IOException("disk");
        }));
    }
}
