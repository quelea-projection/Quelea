package org.quelea.windows.video;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
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
    @DisplayName("Repeated clears do not accumulate scheduled delivery tasks")
    public void repeatedClearsKeepSchedulingBounded() {
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

    @Test
    @DisplayName("An empty delivery after clear does not prevent later frames")
    public void emptyDeliveryAfterClearAllowsFutureFrames() {
        Queue<Runnable> queue = new ArrayDeque<>();
        List<String> shown = new ArrayList<>();
        List<String> released = new ArrayList<>();
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(queue::add, shown::add, released::add);
        handoff.offer("old");
        handoff.clear();
        queue.remove().run();
        handoff.offer("new");
        Assertions.assertEquals(1, queue.size());
        queue.remove().run();
        Assertions.assertEquals(List.of("old"), released);
        Assertions.assertEquals(List.of("new"), shown);
    }

    @Test
    @DisplayName("A rejected delivery releases the frame and allows a later retry")
    public void rejectedDeliveryReleasesFrame() {
        Queue<Runnable> queue = new ArrayDeque<>();
        List<String> shown = new ArrayList<>();
        List<String> released = new ArrayList<>();
        AtomicBoolean reject = new AtomicBoolean(true);
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(task -> {
            if (reject.getAndSet(false)) {
                throw new IllegalStateException("Consumer unavailable");
            }
            queue.add(task);
        }, shown::add, released::add);

        Assertions.assertThrows(IllegalStateException.class, () -> handoff.offer("rejected"));
        Assertions.assertEquals(List.of("rejected"), released);
        handoff.offer("next");
        Assertions.assertEquals(1, queue.size());
        queue.remove().run();
        Assertions.assertEquals(List.of("next"), shown);
    }

    @Test
    @DisplayName("A rejected delivery releases the latest frame offered while scheduling")
    public void rejectedDeliveryReleasesConcurrentReplacement() throws Exception {
        Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        List<String> shown = new CopyOnWriteArrayList<>();
        List<String> released = new CopyOnWriteArrayList<>();
        CountDownLatch schedulingStarted = new CountDownLatch(1);
        CountDownLatch rejectScheduling = new CountDownLatch(1);
        AtomicBoolean reject = new AtomicBoolean(true);
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(task -> {
            if (reject.getAndSet(false)) {
                schedulingStarted.countDown();
                try {
                    if (!rejectScheduling.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Scheduler was not allowed to finish");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(ex);
                }
                throw new IllegalStateException("Consumer unavailable");
            }
            queue.add(task);
        }, shown::add, released::add);
        ExecutorService producer = Executors.newSingleThreadExecutor();
        try {
            Future<?> first = producer.submit(() -> handoff.offer("first"));
            Assertions.assertTrue(schedulingStarted.await(5, TimeUnit.SECONDS));
            handoff.offer("replacement");
            Assertions.assertEquals(List.of("first"), released);
            rejectScheduling.countDown();
            ExecutionException failure = Assertions.assertThrows(
                    ExecutionException.class, () -> first.get(5, TimeUnit.SECONDS));
            Assertions.assertInstanceOf(IllegalStateException.class, failure.getCause());
            Assertions.assertEquals(List.of("first", "replacement"), released);

            handoff.offer("next");
            Assertions.assertEquals(1, queue.size());
            queue.remove().run();
            Assertions.assertEquals(List.of("next"), shown);
        } finally {
            rejectScheduling.countDown();
            producer.shutdownNow();
        }
    }

    @Test
    @DisplayName("A synchronous delivery failure does not discard a newer scheduled frame")
    public void synchronousDeliveryFailureKeepsNewerFrame() {
        Queue<Runnable> queue = new ArrayDeque<>();
        List<String> shown = new ArrayList<>();
        List<String> released = new ArrayList<>();
        AtomicBoolean firstDelivery = new AtomicBoolean(true);
        AtomicReference<LatestFrameHandoff<String>> ref = new AtomicReference<>();
        LatestFrameHandoff<String> handoff = new LatestFrameHandoff<>(task -> {
            if (firstDelivery.getAndSet(false)) {
                task.run();
            } else {
                queue.add(task);
            }
        }, frame -> {
            shown.add(frame);
            if (frame.equals("first")) {
                ref.get().offer("next");
                throw new IllegalStateException("Display failed after taking ownership");
            }
        }, released::add);
        ref.set(handoff);

        Assertions.assertThrows(IllegalStateException.class, () -> handoff.offer("first"));
        Assertions.assertEquals(1, queue.size());
        queue.remove().run();
        Assertions.assertEquals(List.of("first", "next"), shown);
        Assertions.assertTrue(released.isEmpty());
    }

    @Test
    @DisplayName("Disposal racing producers releases every waiting frame once")
    public void disposalRacingProducersReleasesEveryFrame() throws Exception {
        Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        Map<Integer, Integer> released = new ConcurrentHashMap<>();
        List<Integer> shown = new CopyOnWriteArrayList<>();
        LatestFrameHandoff<Integer> handoff = new LatestFrameHandoff<>(queue::add, shown::add,
                frame -> released.merge(frame, 1, Integer::sum));
        int producerCount = 4;
        int framesPerProducer = 500;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(producerCount + 1);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int producer = 0; producer < producerCount; producer++) {
                int firstFrame = producer * framesPerProducer;
                tasks.add(workers.submit(() -> {
                    start.await();
                    for (int i = 0; i < framesPerProducer; i++) {
                        handoff.offer(firstFrame + i);
                    }
                    return null;
                }));
            }
            tasks.add(workers.submit(() -> {
                start.await();
                handoff.dispose();
                return null;
            }));
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(5, TimeUnit.SECONDS);
            }
            Assertions.assertEquals(producerCount * framesPerProducer, released.size());
            Assertions.assertTrue(released.values().stream().allMatch(count -> count == 1));
            Assertions.assertTrue(queue.size() <= 1);
            Runnable task;
            while ((task = queue.poll()) != null) {
                task.run();
            }
            Assertions.assertTrue(shown.isEmpty());
            Assertions.assertEquals(producerCount * framesPerProducer, released.size());
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }
}
