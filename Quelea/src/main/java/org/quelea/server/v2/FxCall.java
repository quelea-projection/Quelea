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

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javafx.application.Platform;

/**
 * Runs work on the JavaFX thread from a network thread and waits for the
 * result, but never longer than a timeout. A network thread must never block
 * the FX thread or wait on it indefinitely: a stuck FX thread would otherwise
 * also hang every remote client.
 * <p>
 * A task that has not started when the timeout expires is cancelled and will
 * not run later, so a timed-out command never takes effect behind the
 * caller's back.
 */
public final class FxCall {

    /**
     * The default time to wait for the FX thread.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    private static final FxCall FX = new FxCall(Platform::runLater, Platform::isFxApplicationThread);

    private final Executor fxExecutor;
    private final BooleanSupplier onFxThread;

    FxCall(Executor fxExecutor, BooleanSupplier onFxThread) {
        this.fxExecutor = fxExecutor;
        this.onFxThread = onFxThread;
    }

    /**
     * @return the instance that runs work on the real JavaFX thread.
     */
    public static FxCall fx() {
        return FX;
    }

    /**
     * Run a task on the FX thread and return its result, waiting at most
     * {@link #DEFAULT_TIMEOUT}.
     *
     * @param task the task to run.
     * @param <T> the result type.
     * @return the task's result.
     * @throws TimeoutException if the FX thread did not finish in time.
     */
    public <T> T call(Callable<T> task) {
        return call(task, DEFAULT_TIMEOUT);
    }

    /**
     * Run a task on the FX thread and return its result.
     *
     * @param task the task to run.
     * @param timeout how long to wait for the FX thread.
     * @param <T> the result type.
     * @return the task's result.
     * @throws TimeoutException if the FX thread did not finish in time.
     */
    public <T> T call(Callable<T> task, Duration timeout) {
        if (onFxThread.getAsBoolean()) {
            try {
                return task.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new CallException(ex);
            }
        }
        FutureTask<T> future = new FutureTask<>(task);
        fxExecutor.execute(future);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException ex) {
            future.cancel(false);
            throw new TimeoutException(timeout);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new CallException(cause);
        } catch (InterruptedException ex) {
            future.cancel(false);
            Thread.currentThread().interrupt();
            throw new CallException(ex);
        }
    }

    /**
     * Run a task on the FX thread and wait for it, at most
     * {@link #DEFAULT_TIMEOUT}.
     *
     * @param task the task to run.
     * @throws TimeoutException if the FX thread did not finish in time.
     */
    public void run(Runnable task) {
        call(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Thrown when the FX thread did not run a task within the timeout.
     */
    public static final class TimeoutException extends RuntimeException {

        TimeoutException(Duration timeout) {
            super("The JavaFX thread did not respond within " + timeout.toMillis() + " ms");
        }
    }

    /**
     * Wraps a checked exception thrown by a task, or an interruption while
     * waiting for it.
     */
    public static final class CallException extends RuntimeException {

        CallException(Throwable cause) {
            super(cause);
        }
    }
}
