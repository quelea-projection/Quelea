package org.quelea.windows.video;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Video frames are handed from GStreamer to the FX thread through
 * {@link LatestFrameHandoff}. Previously every frame was queued with its own
 * Platform.runLater, so whenever the FX thread fell behind (e.g. while the
 * theme editor redrew on every slider tick) native frames piled up without
 * limit and memory use grew to tens of gigabytes.
 */
@DisplayName("LatestFrameHandoff")
public class LatestFrameHandoffTest {

    /** Stands in for the FX thread: tasks only run when we say so. */
    private final Queue<Runnable> fxQueue = new ArrayDeque<>();
    private final List<String> shown = new ArrayList<>();
    private final List<String> released = new ArrayList<>();
    private LatestFrameHandoff<String> handoff;

    @BeforeEach
    public void setUp() {
        handoff = new LatestFrameHandoff<>(fxQueue::add, shown::add, released::add);
    }

    private void runFxQueue() {
        while (!fxQueue.isEmpty()) {
            fxQueue.remove().run();
        }
    }

    @Test
    @DisplayName("A frame is shown on the consumer thread")
    public void showsFrame() {
        handoff.offer("f1");
        assertTrue(shown.isEmpty(), "Frames must only be shown on the consumer thread");
        runFxQueue();
        assertEquals(List.of("f1"), shown);
        assertTrue(released.isEmpty());
    }

    @Test
    @DisplayName("Frames don't pile up while the consumer is busy")
    public void framesDoNotPileUp() {
        for (int i = 0; i < 1000; i++) {
            handoff.offer("f" + i);
        }
        assertEquals(1, fxQueue.size(), "Only one delivery should ever be scheduled");
        assertEquals(999, released.size(), "Superseded frames should be released at once");
        runFxQueue();
        assertEquals(List.of("f999"), shown, "Only the newest frame should be shown");
    }

    @Test
    @DisplayName("Every frame is either shown or released")
    public void noFrameIsLost() {
        for (int round = 0; round < 50; round++) {
            for (int i = 0; i < round % 7 + 1; i++) {
                handoff.offer(round + ":" + i);
            }
            if (round % 3 == 0) {
                runFxQueue();
            }
        }
        handoff.dispose();
        runFxQueue();
        int offered = 0;
        for (int round = 0; round < 50; round++) {
            offered += round % 7 + 1;
        }
        assertEquals(offered, shown.size() + released.size());
    }

    @Test
    @DisplayName("Frames offered after dispose are released immediately")
    public void disposeReleasesFrames() {
        handoff.offer("waiting");
        handoff.dispose();
        assertEquals(List.of("waiting"), released);
        handoff.offer("late");
        assertEquals(List.of("waiting", "late"), released);
        runFxQueue();
        assertTrue(shown.isEmpty(), "Nothing should be shown after dispose");
    }

    @Test
    @DisplayName("Clear releases the waiting frame but keeps accepting frames")
    public void clearReleasesWaitingFrame() {
        handoff.offer("old");
        handoff.clear();
        assertEquals(List.of("old"), released);
        handoff.offer("new");
        runFxQueue();
        assertEquals(List.of("new"), shown);
    }

    @Test
    @DisplayName("Concurrent producers never leave more than one frame waiting")
    public void concurrentProducers() throws Exception {
        AtomicInteger shownCount = new AtomicInteger();
        AtomicInteger releasedCount = new AtomicInteger();
        Queue<Runnable> queue = new java.util.concurrent.ConcurrentLinkedQueue<>();
        LatestFrameHandoff<Integer> h = new LatestFrameHandoff<>(queue::add,
                f -> shownCount.incrementAndGet(), f -> releasedCount.incrementAndGet());
        int producers = 4;
        int perProducer = 10_000;
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    return;
                }
                for (int i = 0; i < perProducer; i++) {
                    h.offer(i);
                }
            });
            t.start();
            threads.add(t);
        }
        start.countDown();
        int maxQueued = 0;
        while (threads.stream().anyMatch(Thread::isAlive)) {
            maxQueued = Math.max(maxQueued, queue.size());
            Runnable r = queue.poll();
            if (r != null) {
                r.run();
            }
        }
        for (Thread t : threads) {
            t.join();
        }
        Runnable r;
        while ((r = queue.poll()) != null) {
            r.run();
        }
        assertTrue(maxQueued <= 1, "At most one delivery should be queued, was " + maxQueued);
        assertEquals(producers * perProducer, shownCount.get() + releasedCount.get());
    }
}
