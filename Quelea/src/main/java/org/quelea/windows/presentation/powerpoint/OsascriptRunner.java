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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs each command as its own {@code osascript powerpoint.applescript ...}
 * process. Slower than {@link PowerPointBridge} (~250 ms per command), but has
 * no state, so it is the fallback if the bridge cannot be started.
 * <p>
 * The arguments are passed as argv, never pasted into the script source, so
 * file names with quotes or other special characters are safe.
 */
class OsascriptRunner implements AppleScriptRunner {

    /** osascript ends its error output with the error number, "... (-1743)". */
    private static final Pattern ERROR_NUMBER = Pattern.compile("\\((-?\\d+)\\)\\s*$");

    private final String command;
    private final File script;

    /**
     * @param command the osascript executable.
     * @param script the script to run.
     */
    OsascriptRunner(String command, File script) {
        this.command = command;
        this.script = script;
    }

    OsascriptRunner() throws IOException {
        this("/usr/bin/osascript", ScriptFiles.get(ScriptFiles.APPLESCRIPT));
    }

    @Override
    public String run(long timeoutMillis, String... args) throws PowerPointException {
        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.add(script.getAbsolutePath());
        for (String arg : args) {
            cmd.add(arg);
        }
        Process process;
        try {
            process = new ProcessBuilder(cmd).start();
        } catch (IOException ex) {
            throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, 0, "Could not run osascript", ex);
        }
        try {
            process.getOutputStream().close();
        } catch (IOException ignored) {
            // Nothing to send anyway.
        }
        // Read both streams while waiting, so a full pipe can never block it.
        CompletableFuture<String> out = readAsync(process.getInputStream());
        CompletableFuture<String> err = readAsync(process.getErrorStream());
        try {
            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new PowerPointException(PowerPointException.Kind.TIMEOUT, -1712,
                        "PowerPoint did not answer " + args[0] + " within " + timeoutMillis + " ms", null);
            }
            String stdout = out.get(1, TimeUnit.SECONDS);
            String stderr = err.get(1, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                throw errorFrom(stderr);
            }
            return stdout.trim();
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0, "Interrupted", ex);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ex) {
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0, "Could not read osascript output", ex);
        }
    }

    /**
     * Turn osascript's error output into an exception.
     *
     * @param stderr e.g. "powerpoint.applescript:12:34: execution error: Not
     * authorised to send Apple events to Microsoft PowerPoint. (-1743)"
     * @return the exception.
     */
    static PowerPointException errorFrom(String stderr) {
        String text = stderr == null ? "" : stderr.trim();
        Matcher m = ERROR_NUMBER.matcher(text);
        int code = m.find() ? Integer.parseInt(m.group(1)) : 0;
        int idx = text.indexOf("error: ");
        String message = idx >= 0 ? text.substring(idx + "error: ".length()) : text;
        message = ERROR_NUMBER.matcher(message).replaceFirst("").trim();
        if (code == 0) {
            return new PowerPointException(PowerPointException.Kind.FAILED, 0, message, null);
        }
        return PowerPointException.fromAppleScript(code, message);
    }

    private static CompletableFuture<String> readAsync(InputStream in) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Thread reader = new Thread(() -> {
            try (InputStream input = in) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                input.transferTo(bytes);
                result.complete(bytes.toString(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                result.complete("");
            }
        }, "osascript-reader");
        reader.setDaemon(true);
        reader.start();
        return result;
    }

    @Override
    public void close() {
        // Nothing is kept open.
    }
}
