/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * The one place Quelea controls an external PowerPoint slide show through.
 * <p>
 * Every command runs on a single background thread, in order, and returns a
 * future; nothing here ever blocks the JavaFX thread. Each command answers
 * with the resulting {@link SlideState}, which is also handed to the state
 * listeners on the FX thread. On macOS the state is polled a couple of times a
 * second while a show is open, so Quelea follows slide changes made in
 * PowerPoint itself (clicker, keyboard, mouse).
 * <p>
 * Missing macOS permissions are reported to the error handler (by default
 * {@link PowerPointPermissionDialog}); everything else is up to the caller.
 */
public final class PowerPointService {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final long POLL_INTERVAL_MS = 400;
    private static final PowerPointService INSTANCE = new PowerPointService(null);

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> daemon(r, "powerpoint-control"));
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "powerpoint-poll"));
    private final AtomicInteger queued = new AtomicInteger();
    private final List<Consumer<SlideState>> stateListeners = new CopyOnWriteArrayList<>();
    private final Consumer<Runnable> fxRunner;
    private volatile Consumer<PowerPointException> errorHandler = PowerPointPermissionDialog::show;
    private PowerPointBackend backend; // worker thread only
    private ScheduledFuture<?> polling;
    private volatile SlideState lastState = SlideState.NOT_RUNNING;
    private volatile boolean nativeLoop;
    private volatile boolean showOpen;
    private volatile boolean showOnProjector;

    /**
     * @param backend the backend to use, or null to pick the one for this OS.
     */
    PowerPointService(PowerPointBackend backend) {
        this(backend, Platform::runLater);
    }

    PowerPointService(PowerPointBackend backend, Consumer<Runnable> fxRunner) {
        this.backend = backend;
        this.fxRunner = fxRunner;
    }

    public static PowerPointService get() {
        return INSTANCE;
    }

    /**
     * @return true if Quelea is set to show presentations in PowerPoint and
     * this platform supports it.
     */
    public static boolean isEnabled() {
        return QueleaProperties.get().getUsePP() && (Utils.isMac() || Utils.isWindows());
    }

    private static Thread daemon(Runnable r, String name) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
    }

    /**
     * A command for the backend.
     */
    @FunctionalInterface
    interface Command<T> {

        T run(PowerPointBackend backend) throws PowerPointException;
    }

    private PowerPointBackend backend() throws PowerPointException {
        if (backend == null) {
            if (Utils.isMac()) {
                try {
                    backend = MacPowerPointBackend.create();
                } catch (IOException ex) {
                    throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, 0, "Cannot set up PowerPoint control", ex);
                }
            } else if (Utils.isWindows()) {
                backend = new WindowsPowerPointBackend();
            } else {
                throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, "PowerPoint is not supported on this platform");
            }
        }
        return backend;
    }

    <T> CompletableFuture<T> submit(String name, Command<T> command) {
        CompletableFuture<T> result = new CompletableFuture<>();
        queued.incrementAndGet();
        worker.execute(() -> {
            try {
                T value = command.run(backend());
                if (value instanceof SlideState) {
                    publish((SlideState) value, true);
                }
                result.complete(value);
            } catch (PowerPointException ex) {
                try {
                    if (ex.getKind() == PowerPointException.Kind.NOT_RUNNING) {
                        publish(SlideState.NOT_RUNNING, false);
                    }
                    if (ex.isPermissionProblem()) {
                        Consumer<PowerPointException> handler = errorHandler;
                        if (handler != null) {
                            fxRunner.accept(() -> handler.accept(ex));
                        }
                    }
                } finally {
                    result.completeExceptionally(ex);
                }
                LOGGER.log(Level.INFO, "PowerPoint {0} failed: {1}", new Object[]{name, ex.getMessage()});
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
                LOGGER.log(Level.WARNING, "PowerPoint " + name + " failed", ex);
            } finally {
                queued.decrementAndGet();
            }
        });
        return result;
    }

    /**
     * Tell the user about a problem that did not fail the command.
     */
    private void warn(PowerPointException warning) {
        if (warning == null) {
            return;
        }
        LOGGER.log(Level.WARNING, warning.getMessage());
        Consumer<PowerPointException> handler = errorHandler;
        if (handler != null && warning.isPermissionProblem()) {
            fxRunner.accept(() -> handler.accept(warning));
        }
    }

    private void publish(SlideState state, boolean always) {
        if (!always && state.equals(lastState)) {
            return;
        }
        lastState = state;
        for (Consumer<SlideState> listener : stateListeners) {
            fxRunner.accept(() -> listener.accept(state));
        }
    }

    /**
     * Open the presentation in PowerPoint and start its slide show. On Windows
     * this fails with {@link PowerPointException.Kind#ALREADY_RUNNING} if
     * PowerPoint has something open already; see {@link #closeAllAndOpen(File)}.
     *
     * @param file the presentation.
     * @return the state once the show runs.
     */
    public CompletableFuture<SlideState> open(File file) {
        return open(file, null);
    }

    /**
     * Open the presentation and start its slide show on the given screen area
     * where the platform allows (see {@link #projectorArea()}).
     *
     * @param file the presentation.
     * @param screen where Quelea projects to, or null.
     * @return the state once the show runs.
     */
    public CompletableFuture<SlideState> open(File file, Rectangle2D screen) {
        return submit("open", b -> {
            SlideState state = b.open(file, screen);
            warn(b.takeWarning());
            showOnProjector = b.isShowOnScreen();
            showOpen = true;
            startPolling(b);
            return state;
        });
    }

    /**
     * Windows: close whatever PowerPoint has open, then open the presentation.
     *
     * @param file the presentation.
     * @return the state once the show runs.
     */
    public CompletableFuture<SlideState> closeAllAndOpen(File file, Rectangle2D screen) {
        return submit("open", b -> {
            b.closeAll();
            SlideState state = b.open(file, screen);
            warn(b.takeWarning());
            showOnProjector = b.isShowOnScreen();
            showOpen = true;
            startPolling(b);
            return state;
        });
    }

    /**
     * Close the presentation Quelea opened.
     */
    public CompletableFuture<SlideState> close() {
        return submit("close", b -> {
            stopPolling();
            showOpen = false;
            showOnProjector = false;
            return b.close();
        });
    }

    public CompletableFuture<SlideState> next() {
        return submit("next", PowerPointBackend::next);
    }

    public CompletableFuture<SlideState> previous() {
        return submit("previous", PowerPointBackend::previous);
    }

    /**
     * Next slide, or back to the first one after the last (for loop mode,
     * where PowerPoint cannot loop the show itself).
     */
    public CompletableFuture<SlideState> nextWrapping() {
        return submit("next", b -> {
            if (!nativeLoop) {
                SlideState now = b.state();
                if (now.isShowing() && now.isAtEnd()) {
                    return b.gotoSlide(1);
                }
            }
            return b.next();
        });
    }

    /**
     * @param slide 1-based slide number.
     */
    public CompletableFuture<SlideState> gotoSlide(int slide) {
        return submit("gotoSlide", b -> b.gotoSlide(slide));
    }

    public CompletableFuture<SlideState> setBlack(boolean black) {
        return submit("setBlack", b -> b.setBlack(black));
    }

    /**
     * Turn loop mode on or off. Where PowerPoint cannot loop by itself (macOS),
     * {@link #nextWrapping()} wraps around instead.
     */
    public CompletableFuture<Boolean> setLoop(boolean loop) {
        return submit("setLoop", b -> {
            nativeLoop = b.setLoop(loop);
            return nativeLoop;
        });
    }

    public CompletableFuture<Void> focus() {
        return submit("focus", b -> {
            b.focus();
            return null;
        });
    }

    public CompletableFuture<SlideState> refresh() {
        return submit("state", PowerPointBackend::state);
    }

    /**
     * Export the presentation as a PDF made by PowerPoint itself (macOS only).
     *
     * @param presentation the presentation.
     * @param pdf where to write the PDF.
     * @return the number of slides.
     */
    public CompletableFuture<Integer> exportPdf(File presentation, File pdf) {
        return submit("exportPdf", b -> {
            if (!(b instanceof MacPowerPointBackend)) {
                throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, "Only supported on macOS");
            }
            return ((MacPowerPointBackend) b).exportPdf(presentation, pdf);
        });
    }

    /**
     * The area Quelea projects to: where its projection window is, or else
     * where the display settings put it. Call on the FX thread.
     *
     * @return the area in screen coordinates, or null if there is no
     * projector screen.
     */
    public static Rectangle2D projectorArea() {
        DisplayStage window = QueleaApp.get() == null ? null : QueleaApp.get().getProjectionWindow();
        if (window != null && window.isShowing() && window.getWidth() > 0 && window.getHeight() > 0) {
            return new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());
        }
        QueleaProperties props = QueleaProperties.get();
        if (props.isProjectorModeCoords()) {
            Bounds b = props.getProjectorCoords();
            return new Rectangle2D(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        }
        int index = props.getProjectorScreen();
        List<Screen> screens = Screen.getScreens();
        if (index >= 0 && index < screens.size()) {
            return screens.get(index).getBounds();
        }
        return null;
    }

    /**
     * @return true if PowerPoint can export PDFs for Quelea on this machine.
     */
    public static boolean canExportPdf() {
        return Utils.isMac() && MacPowerPointBackend.isInstalled();
    }

    /**
     * @return the last state PowerPoint reported.
     */
    public SlideState getLastState() {
        return lastState;
    }

    /**
     * @return true if the open show is on the screen area Quelea projects to
     * (or no area was given), so Quelea's projection can let it show through.
     */
    public boolean isShowOnProjector() {
        return showOnProjector;
    }

    /**
     * @return true between a successful {@link #open(File)} and {@link #close()}.
     */
    public boolean isShowOpen() {
        return showOpen;
    }

    /**
     * @param listener called on the FX thread with every new state.
     */
    public void addStateListener(Consumer<SlideState> listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(Consumer<SlideState> listener) {
        stateListeners.remove(listener);
    }

    /**
     * @param handler called on the FX thread when a macOS permission is
     * missing; null to only log.
     */
    public void setErrorHandler(Consumer<PowerPointException> handler) {
        this.errorHandler = handler;
    }

    private void startPolling(PowerPointBackend b) {
        if (!b.supportsPolling()) {
            return;
        }
        synchronized (poller) {
            if (polling != null) {
                return;
            }
            polling = poller.scheduleWithFixedDelay(this::poll, POLL_INTERVAL_MS, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopPolling() {
        synchronized (poller) {
            if (polling != null) {
                polling.cancel(false);
                polling = null;
            }
        }
    }

    private void poll() {
        if (!showOpen || queued.get() > 0) {
            return; // A command is on its way and will report the state anyway.
        }
        queued.incrementAndGet();
        worker.execute(() -> {
            try {
                PowerPointBackend b = backend();
                if (!b.supportsPolling()) {
                    stopPolling();
                    return;
                }
                publish(b.state(), false);
            } catch (PowerPointException ex) {
                if (ex.getKind() == PowerPointException.Kind.NOT_RUNNING) {
                    publish(SlideState.NOT_RUNNING, false);
                }
                LOGGER.log(Level.FINE, "PowerPoint poll failed", ex);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "PowerPoint poll failed", ex);
            } finally {
                queued.decrementAndGet();
            }
        });
    }

    /**
     * Close the presentation and stop everything; for when Quelea exits.
     * Waits at most about ten seconds (commands queued before the close run first).
     */
    public void shutdown() {
        stopPolling();
        if (showOpen) {
            try {
                close().get(10, TimeUnit.SECONDS);
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "Could not close the PowerPoint presentation", ex);
            }
        }
        CompletableFuture<Void> done = new CompletableFuture<>();
        worker.execute(() -> {
            if (backend != null) {
                backend.shutdown();
            }
            done.complete(null);
        });
        try {
            done.get(3, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "PowerPoint control did not stop in time", ex);
        }
        poller.shutdownNow();
        worker.shutdownNow();
    }

    /**
     * @param t a failure from one of the futures.
     * @return the PowerPointException inside it, or null.
     */
    public static PowerPointException unwrap(Throwable t) {
        Throwable cause = t;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause instanceof PowerPointException ? (PowerPointException) cause : null;
    }
}
