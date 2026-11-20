package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoteEventBusTest {

    private ExecutorService dispatcher;
    private RemoteEventBus bus;

    @BeforeEach
    void setUp() {
        dispatcher = Executors.newSingleThreadExecutor();
        bus = new RemoteEventBus(dispatcher);
    }

    @AfterEach
    void tearDown() {
        dispatcher.shutdownNow();
    }

    private void drain() throws InterruptedException {
        dispatcher.shutdown();
        assertTrue(dispatcher.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void deliversEventsInOrderWithIncreasingSeq() throws InterruptedException {
        List<Long> seqs = new ArrayList<>();
        List<String> types = new ArrayList<>();
        bus.subscribe((seq, event) -> {
            seqs.add(seq);
            types.add(event.type());
        });

        bus.publish(new RemoteEvent.ScreenChanged(false, true, false));
        bus.publish(new RemoteEvent.LiveSectionChanged("it_1", 2));
        bus.publish(new RemoteEvent.ScheduleChanged(7));
        drain();

        assertEquals(List.of(1L, 2L, 3L), seqs);
        assertEquals(List.of("live.screen", "live.section", "schedule.changed"), types);
        assertEquals(3, bus.lastSeq());
    }

    @Test
    void failingListenerDoesNotStopOthers() throws InterruptedException {
        List<Long> received = new ArrayList<>();
        bus.subscribe((seq, event) -> {
            throw new IllegalStateException("broken client");
        });
        bus.subscribe((seq, event) -> received.add(seq));

        bus.publish(new RemoteEvent.RecordingChanged(true));
        bus.publish(new RemoteEvent.RecordingChanged(false));
        drain();

        assertEquals(List.of(1L, 2L), received);
    }

    @Test
    void closedSubscriptionReceivesNothing() throws InterruptedException {
        List<Long> received = new ArrayList<>();
        RemoteEventBus.Subscription subscription = bus.subscribe((seq, event) -> received.add(seq));
        bus.publish(new RemoteEvent.RecordingChanged(true));
        subscription.close();
        bus.publish(new RemoteEvent.RecordingChanged(false));
        drain();

        // The first event may or may not arrive before close, the second never does.
        assertTrue(received.isEmpty() || received.equals(List.of(1L)));
    }

    @Test
    void publishAfterShutdownIsIgnored() {
        dispatcher.shutdown();
        bus.publish(new RemoteEvent.RecordingChanged(true));
        assertEquals(1, bus.lastSeq());
    }
}
