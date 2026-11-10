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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 * Talks to a long-running {@code osascript -l JavaScript pptbridge.js} process
 * that has powerpoint.applescript compiled already, so each command is a
 * single Apple event (~10-100 ms) instead of a new osascript process that has
 * to compile the script first (~250 ms or more).
 * <p>
 * One request per line of JSON on the process' stdin, one response per line
 * on its stdout (see pptbridge.js). The process is started on first use and
 * restarted if it dies or a command times out; after
 * {@link #MAX_STARTS} failed starts the bridge reports itself unusable and
 * {@link FallbackRunner} switches to {@link OsascriptRunner}.
 */
class PowerPointBridge implements AppleScriptRunner {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    static final int MAX_STARTS = 4;
    private static final long READY_TIMEOUT_MS = 15_000;

    /** The bridge process itself failed (not the PowerPoint command). */
    static class BridgeException extends PowerPointException {

        BridgeException(String message, Throwable cause) {
            super(Kind.FAILED, 0, message, cause);
        }
    }

    private final List<String> command;
    private final Gson gson = new Gson();
    private final Map<Long, CompletableFuture<JsonObject>> pending = new HashMap<>();
    private Process process;
    private OutputStream stdin;
    private CompletableFuture<Void> ready;
    private long nextId = 1;
    private int starts;
    private boolean closed;

    /**
     * @param command the full command that starts the bridge.
     */
    PowerPointBridge(List<String> command) {
        this.command = new ArrayList<>(command);
    }

    static PowerPointBridge create() throws IOException {
        return new PowerPointBridge(Arrays.asList("/usr/bin/osascript", "-l", "JavaScript",
                ScriptFiles.get(ScriptFiles.BRIDGE).getAbsolutePath(),
                ScriptFiles.get(ScriptFiles.APPLESCRIPT).getAbsolutePath()));
    }

    /**
     * @return false once the bridge failed to start too often.
     */
    synchronized boolean isUsable() {
        return !closed && starts < MAX_STARTS || isAlive();
    }

    private boolean isAlive() {
        return process != null && process.isAlive();
    }

    @Override
    public synchronized String run(long timeoutMillis, String... args) throws PowerPointException {
        ensureStarted();
        long id = nextId++;
        CompletableFuture<JsonObject> answer = new CompletableFuture<>();
        synchronized (pending) {
            pending.put(id, answer);
        }
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.add("args", gson.toJsonTree(args));
        try {
            stdin.write((gson.toJson(request) + "\n").getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        } catch (IOException ex) {
            forget(id);
            kill();
            throw new BridgeException("Could not send to the PowerPoint bridge", ex);
        }
        JsonObject response;
        try {
            response = answer.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            forget(id);
            // PowerPoint (or a dialog in it) is hanging. The bridge is stuck in
            // that Apple event, so start a fresh one for the next command.
            kill();
            throw new PowerPointException(PowerPointException.Kind.TIMEOUT, -1712,
                    "PowerPoint did not answer " + args[0] + " within " + timeoutMillis + " ms", ex);
        } catch (InterruptedException ex) {
            forget(id);
            Thread.currentThread().interrupt();
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0, "Interrupted", ex);
        } catch (ExecutionException ex) {
            throw new BridgeException("The PowerPoint bridge stopped", ex.getCause());
        }
        if (response.has("ok") && response.get("ok").getAsBoolean()) {
            return response.has("result") ? response.get("result").getAsString().trim() : "";
        }
        int code = response.has("code") ? response.get("code").getAsInt() : 0;
        String message = response.has("message") ? response.get("message").getAsString() : "Unknown error";
        throw PowerPointException.fromAppleScript(code, message);
    }

    private void forget(long id) {
        synchronized (pending) {
            pending.remove(id);
        }
    }

    private void ensureStarted() throws PowerPointException {
        if (closed) {
            throw new BridgeException("The PowerPoint bridge is closed", null);
        }
        if (isAlive() && ready != null && ready.isDone() && !ready.isCompletedExceptionally()) {
            return;
        }
        kill();
        if (starts >= MAX_STARTS) {
            throw new BridgeException("The PowerPoint bridge failed to start " + starts + " times", null);
        }
        starts++;
        long started = System.nanoTime();
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException ex) {
            throw new BridgeException("Could not start the PowerPoint bridge", ex);
        }
        stdin = process.getOutputStream();
        ready = new CompletableFuture<>();
        startReader(process, ready);
        startErrorLogger(process);
        try {
            ready.get(READY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            kill();
            throw new BridgeException("The PowerPoint bridge could not start: " + ex.getCause().getMessage(), ex.getCause());
        } catch (TimeoutException ex) {
            kill();
            throw new BridgeException("The PowerPoint bridge did not start in time", ex);
        } catch (InterruptedException ex) {
            kill();
            Thread.currentThread().interrupt();
            throw new BridgeException("Interrupted", ex);
        }
        starts = 0; // A working start resets the budget.
        LOGGER.log(Level.INFO, "PowerPoint bridge started in {0} ms",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
    }

    private void startReader(Process proc, CompletableFuture<Void> readyFuture) {
        Thread reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    handleLine(line, readyFuture);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "PowerPoint bridge output closed", ex);
            }
            BridgeException gone = new BridgeException("The PowerPoint bridge exited", null);
            readyFuture.completeExceptionally(gone);
            failPending(gone);
        }, "powerpoint-bridge-reader");
        reader.setDaemon(true);
        reader.start();
    }

    private void handleLine(String line, CompletableFuture<Void> readyFuture) {
        JsonObject msg;
        try {
            msg = JsonParser.parseString(line).getAsJsonObject();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Unexpected output from the PowerPoint bridge: {0}", line);
            return;
        }
        if (msg.has("event")) {
            String event = msg.get("event").getAsString();
            if ("ready".equals(event)) {
                readyFuture.complete(null);
            } else if ("fatal".equals(event)) {
                String message = msg.has("message") ? msg.get("message").getAsString() : "fatal error";
                readyFuture.completeExceptionally(new IOException(message));
            }
            return;
        }
        if (msg.has("id")) {
            CompletableFuture<JsonObject> answer;
            synchronized (pending) {
                answer = pending.remove(msg.get("id").getAsLong());
            }
            if (answer != null) {
                answer.complete(msg);
            }
        }
    }

    private void startErrorLogger(Process proc) {
        Thread logger = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    LOGGER.log(Level.INFO, "PowerPoint bridge: {0}", line);
                }
            } catch (IOException ex) {
                // Process ended.
            }
        }, "powerpoint-bridge-stderr");
        logger.setDaemon(true);
        logger.start();
    }

    private void failPending(Exception ex) {
        List<CompletableFuture<JsonObject>> waiting;
        synchronized (pending) {
            waiting = new ArrayList<>(pending.values());
            pending.clear();
        }
        for (CompletableFuture<JsonObject> f : waiting) {
            f.completeExceptionally(ex);
        }
    }

    private void kill() {
        if (process != null) {
            try {
                stdin.close();
            } catch (IOException ignored) {
                // Already closed.
            }
            process.destroy();
            try {
                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ex) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            process = null;
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (process != null) {
            // Closing stdin makes the bridge leave its read loop and exit.
            try {
                stdin.close();
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (IOException | InterruptedException ex) {
                process.destroyForcibly();
            }
            process = null;
        }
    }
}
