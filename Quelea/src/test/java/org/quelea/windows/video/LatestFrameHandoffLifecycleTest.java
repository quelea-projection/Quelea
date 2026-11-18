package org.quelea.windows.video;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Frame ownership and scheduling without requiring JavaFX or native codecs. */
@DisplayName("LatestFrameHandoff lifecycle")
public class LatestFrameHandoffLifecycleTest {

    @Test
    @DisplayName("Frames arriving during delivery are coalesced into a later delivery")
    public void offersDuringDelivery() {
        Queue<Runnable> queue = new ArrayDeque<>();
        List<String> shown = new ArrayList<>();
        List<String> released = new ArrayList<>();
        AtomicReference<LatestFrameHandoff<String>> ref = new AtomicReference<>();
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(queue::add, frame -> {
            shown.add(frame);
            if (frame.equals("first")) {
                ref.get().offer("superseded");
                ref.get().offer("latest");
            }
        }, released::add);
        ref.set(handoff);

        handoff.offer("first");
        while (!queue.isEmpty()) {
            queue.remove().run();
        }
        Assertions.assertEquals(List.of("first", "latest"), shown);
        Assertions.assertEquals(List.of("superseded"), released);
    }

    @Test
    @DisplayName("Disposal during delivery releases waiting and later frames exactly once")
    public void disposeDuringDelivery() throws Exception {
        Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        List<String> shown = new CopyOnWriteArrayList<>();
        List<String> released = new CopyOnWriteArrayList<>();
        CountDownLatch deliveryStarted = new CountDownLatch(1);
        CountDownLatch finishDelivery = new CountDownLatch(1);
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(queue::add, frame -> {
            shown.add(frame);
            deliveryStarted.countDown();
            try {
                if (!finishDelivery.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Delivery was not allowed to finish");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AssertionError(ex);
            }
        }, released::add);

        ExecutorService consumer = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "test video consumer");
            thread.setDaemon(true);
            return thread;
        });
        try {
            handoff.offer("displaying");
            Future<?> delivery = consumer.submit(queue.remove());
            Assertions.assertTrue(deliveryStarted.await(5, TimeUnit.SECONDS));
            handoff.offer("superseded");
            handoff.offer("waiting");
            Assertions.assertEquals(1, queue.size());
            handoff.dispose();
            handoff.dispose();
            handoff.offer("late");
            finishDelivery.countDown();
            delivery.get(5, TimeUnit.SECONDS);
            while (!queue.isEmpty()) {
                queue.remove().run();
            }
            Assertions.assertEquals(List.of("displaying"), shown);
            Assertions.assertEquals(List.of("superseded", "waiting", "late"), released);
        } finally {
            finishDelivery.countDown();
            consumer.shutdownNow();
        }
    }

    @Test
    @Disabled("Clearing a pending frame does not cancel its queued delivery")
    @DisplayName("Repeated clears do not accumulate scheduled delivery tasks")
    public void repeatedClearsKeepSchedulingBounded() {
        // Clearing removes the pending frame but leaves its delivery task in the
        // consumer queue. The next offer sees an empty slot and queues another:
        // 100 clear/offer cycles leave 101 tasks, contrary to the bounded-queue
        // contract. Repeated video resets can therefore flood a busy FX thread.
        // Track whether a delivery is scheduled separately from the pending frame,
        // coordinating clear, delivery and new offers so no wakeup is lost.
        Queue<Runnable> queue = new ArrayDeque<>();
        List<String> shown = new ArrayList<>();
        List<String> released = new ArrayList<>();
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(queue::add, shown::add, released::add);
        for (int i = 0; i < 100; i++) {
            handoff.offer("old-" + i);
            handoff.clear();
        }
        handoff.offer("current");
        Assertions.assertEquals(1, queue.size(), "Only one delivery should be queued while the consumer is busy");
        while (!queue.isEmpty()) {
            queue.remove().run();
        }
        Assertions.assertEquals(List.of("current"), shown);
        Assertions.assertEquals(100, released.size());
    }
}
