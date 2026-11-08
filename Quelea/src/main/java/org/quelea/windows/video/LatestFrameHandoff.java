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
package org.quelea.windows.video;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Hands frames from a producer thread (a GStreamer streaming thread) to a
 * consumer thread (the FX thread), keeping only the newest one.
 * <p>
 * If the consumer falls behind, frames that are superseded before they're shown
 * are released straight away instead of piling up: at most one frame is ever
 * waiting, and at most one delivery is ever scheduled. Each frame is a full
 * native video buffer, so an unbounded queue here quickly grows to gigabytes.
 *
 * @param <T> the frame type.
 */
final class LatestFrameHandoff<T> {

    private final AtomicReference<T> pending = new AtomicReference<>();
    private final Consumer<Runnable> scheduler;
    private final Consumer<T> show;
    private final Consumer<T> release;
    private volatile boolean disposed;

    /**
     * @param scheduler runs a task on the consumer thread, e.g.
     * {@code Platform::runLater}.
     * @param show shows a frame; called on the consumer thread, and takes over
     * responsibility for releasing the frame.
     * @param release releases a frame that won't be shown.
     */
    LatestFrameHandoff(Consumer<Runnable> scheduler, Consumer<T> show, Consumer<T> release) {
        this.scheduler = scheduler;
        this.show = show;
        this.release = release;
    }

    /**
     * Offer a new frame. Can be called from any thread.
     *
     * @param frame the frame.
     */
    void offer(T frame) {
        if (disposed) {
            release.accept(frame);
            return;
        }
        T previous = pending.getAndSet(frame);
        if (previous != null) {
            // A delivery is already scheduled and will pick up the new frame.
            release.accept(previous);
        } else {
            scheduler.accept(this::deliver);
        }
    }

    private void deliver() {
        T frame = pending.getAndSet(null);
        if (frame == null) {
            return;
        }
        if (disposed) {
            release.accept(frame);
            return;
        }
        show.accept(frame);
    }

    /**
     * Release the waiting frame, if there is one.
     */
    void clear() {
        T frame = pending.getAndSet(null);
        if (frame != null) {
            release.accept(frame);
        }
    }

    /**
     * Release the waiting frame and stop accepting new ones: any frame offered
     * afterwards is released immediately.
     */
    void dispose() {
        disposed = true;
        clear();
    }
}
