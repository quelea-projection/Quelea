/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.server.v2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 * Collects changes in Quelea and hands them to remote listeners, each with an
 * increasing sequence number.
 * <p>
 * Events are usually published on the FX thread, but listeners run on a
 * single dispatcher thread of their own, in publish order. A slow network
 * listener can therefore never hold up the FX thread.
 */
public final class RemoteEventBus {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final RemoteEventBus INSTANCE = new RemoteEventBus(Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "remote-v2-events");
        t.setDaemon(true);
        return t;
    }));

    /**
     * Receives events from the bus.
     */
    @FunctionalInterface
    public interface Listener {

        /**
         * Called on the dispatcher thread for every event.
         *
         * @param seq the event's sequence number, increasing by one per event.
         * @param event the event.
         */
        void onEvent(long seq, RemoteEvent event);
    }

    /**
     * A registration that can be cancelled.
     */
    @FunctionalInterface
    public interface Subscription extends AutoCloseable {

        /**
         * Stop receiving events.
         */
        @Override
        void close();
    }

    private final ExecutorService dispatcher;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private long seq;

    RemoteEventBus(ExecutorService dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @return the bus shared by the whole application.
     */
    public static RemoteEventBus get() {
        return INSTANCE;
    }

    /**
     * Register a listener.
     *
     * @param listener the listener.
     * @return a subscription that removes the listener when closed.
     */
    public Subscription subscribe(Listener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /**
     * Publish an event to all listeners. Returns at once; delivery happens on
     * the dispatcher thread.
     *
     * @param event the event.
     */
    public synchronized void publish(RemoteEvent event) {
        long eventSeq = ++seq;
        try {
            dispatcher.execute(() -> deliver(eventSeq, event));
        } catch (RejectedExecutionException ex) {
            LOGGER.log(Level.FINE, "Remote event dropped after shutdown: {0}", event.type());
        }
    }

    /**
     * @return the sequence number of the most recently published event, or 0.
     */
    public synchronized long lastSeq() {
        return seq;
    }

    private void deliver(long eventSeq, RemoteEvent event) {
        for (Listener listener : listeners) {
            try {
                listener.onEvent(eventSeq, event);
            } catch (RuntimeException ex) {
                logListenerFailure(ex);
            }
        }
    }

    private static void logListenerFailure(RuntimeException failure) {
        try {
            LOGGER.log(Level.WARNING, "Remote event listener failed", failure);
        } catch (RuntimeException loggingFailure) {
            // Logging must never stop delivery to the remaining listeners,
            // for example when the log file is not set up.
        }
    }
}
