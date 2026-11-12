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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Lets new devices pair: with a short code shown in Quelea, or by asking and
 * being allowed in Quelea. The remote control password does not pair: it is
 * often widely known, and gives control without anyone at Quelea seeing who
 * connects.
 * <p>
 * A code is six characters, works once and expires after
 * {@link #CODE_LIFETIME}. Five wrong codes in a row close pairing until it is
 * opened again in Quelea, so a code cannot be guessed.
 * <p>
 * A request waits for an answer in Quelea for {@link #REQUEST_LIFETIME}.
 * Only one request waits at a time, so a device cannot fill the screen
 * with them, and an address whose request was denied must wait
 * {@link #DENIED_LOCKOUT} before it asks again.
 */
public final class PairingService {

    public static final Duration CODE_LIFETIME = Duration.ofMinutes(2);
    public static final Duration REQUEST_LIFETIME = Duration.ofMinutes(1);
    public static final Duration DENIED_LOCKOUT = Duration.ofMinutes(2);
    static final int MAX_FAILURES = 5;
    /**
     * No 0/O, 1/I/L: the code is read off a screen and typed.
     */
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    /**
     * Why pairing failed; the API turns it into an error code.
     */
    public enum Failure {
        /**
         * No code is open, or it expired.
         */
        CLOSED,
        /**
         * The code is wrong.
         */
        WRONG,
        /**
         * Too many wrong attempts.
         */
        LOCKED,
        /**
         * Another device's request is already waiting for an answer.
         */
        BUSY
    }

    /**
     * Thrown when a device could not pair.
     */
    public static final class PairingException extends RuntimeException {

        private final Failure failure;

        PairingException(Failure failure, String message) {
            super(message);
            this.failure = failure;
        }

        public Failure getFailure() {
            return failure;
        }
    }

    /**
     * An open pairing code.
     *
     * @param code the code to show.
     * @param scope what a device that pairs with it may do.
     * @param expires when it stops working.
     */
    public record OpenCode(String code, Scope scope, Instant expires) {
    }

    /**
     * A device asking to pair, waiting for an answer in Quelea.
     *
     * @param id the request's id; the device asks for the answer with it.
     * @param name the device's name.
     * @param platform the device's platform, may be null.
     * @param address the address the request came from, may be null.
     * @param expires when the request is dropped if nobody answers.
     */
    public record Request(String id, String name, String platform, String address, Instant expires) {
    }

    /**
     * Where a request stands.
     */
    public enum RequestState {
        PENDING, APPROVED, DENIED, EXPIRED
    }

    /**
     * The answer to a request.
     *
     * @param state where it stands.
     * @param paired the new device and its token, only when approved.
     */
    public record RequestStatus(RequestState state, DeviceRegistry.Paired paired) {
    }

    private final DeviceRegistry registry;
    private final Clock clock;
    private final SecureRandom random;
    private final List<Consumer<DeviceRegistry.Paired>> pairedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Request>> requestListeners = new CopyOnWriteArrayList<>();

    // Guarded by this.
    private OpenCode open;
    private int codeFailures;
    private Request pending;
    /**
     * Answers not yet picked up by their device, by request id.
     */
    private final Map<String, RequestStatus> answers = new HashMap<>();
    private final Map<String, Instant> answerExpiry = new HashMap<>();
    private final Map<String, Instant> deniedUntil = new HashMap<>();

    /**
     * @param registry where paired devices are stored.
     */
    public PairingService(DeviceRegistry registry) {
        this(registry, Clock.systemUTC(), new SecureRandom());
    }

    PairingService(DeviceRegistry registry, Clock clock, SecureRandom random) {
        this.registry = registry;
        this.clock = clock;
        this.random = random;
    }

    /**
     * Open pairing with a new code, replacing any open one.
     *
     * @param scope what a device that pairs with the code may do.
     * @return the code to show.
     */
    public synchronized OpenCode open(Scope scope) {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        open = new OpenCode(code.toString(), scope, clock.instant().plus(CODE_LIFETIME));
        codeFailures = 0;
        return open;
    }

    /**
     * Close pairing; the open code stops working.
     */
    public synchronized void close() {
        open = null;
    }

    /**
     * @return true if a code is open and has not expired.
     */
    public synchronized boolean isOpen() {
        return current() != null;
    }

    /**
     * Pair with a code.
     *
     * @param code the code the user typed or scanned; case does not matter.
     * @param name the device's name.
     * @param platform the device's platform, may be null.
     * @return the new device and its token.
     * @throws PairingException if the code is wrong, expired or pairing is
     * closed.
     */
    public DeviceRegistry.Paired pairWithCode(String code, String name, String platform) {
        Scope scope;
        synchronized (this) {
            OpenCode current = current();
            if (current == null) {
                throw new PairingException(Failure.CLOSED, "Pairing is not open in Quelea");
            }
            String given = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
            if (!constantTimeEquals(given, current.code())) {
                codeFailures++;
                if (codeFailures >= MAX_FAILURES) {
                    open = null;
                    throw new PairingException(Failure.LOCKED, "Too many wrong codes; open pairing again in Quelea");
                }
                throw new PairingException(Failure.WRONG, "Wrong pairing code");
            }
            // One code, one device.
            open = null;
            scope = current.scope();
        }
        return paired(registry.pair(name, platform, scope));
    }

    /**
     * Ask to pair. The request waits until it is allowed or denied in
     * Quelea (see {@link #addRequestListener}) or expires; the device asks
     * for the answer with {@link #requestStatus}.
     *
     * @param name the device's name.
     * @param platform the device's platform, may be null.
     * @param address the address the request came from, may be null.
     * @return the request.
     * @throws PairingException if another request is waiting, or this
     * address was denied a moment ago.
     */
    public Request request(String name, String platform, String address) {
        Request request;
        synchronized (this) {
            Instant now = clock.instant();
            prune(now);
            if (address != null && deniedUntil.containsKey(address)) {
                throw new PairingException(Failure.LOCKED, "The request was denied; try again in a few minutes");
            }
            if (pending != null) {
                throw new PairingException(Failure.BUSY, "Another device is waiting for an answer; try again soon");
            }
            byte[] bytes = new byte[18];
            random.nextBytes(bytes);
            String id = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            request = new Request(id, name, platform, address, now.plus(REQUEST_LIFETIME));
            pending = request;
        }
        requestListeners.forEach(listener -> listener.accept(request));
        return request;
    }

    /**
     * Allow a request: the device pairs.
     *
     * @param id the request's id.
     * @param scope what the device may do.
     * @return the new device, or null if the request is no longer waiting.
     */
    public DeviceRegistry.Paired approve(String id, Scope scope) {
        Request request;
        synchronized (this) {
            request = take(id);
            if (request == null) {
                return null;
            }
        }
        DeviceRegistry.Paired paired = paired(registry.pair(request.name(), request.platform(), scope));
        synchronized (this) {
            answer(id, new RequestStatus(RequestState.APPROVED, paired));
        }
        return paired;
    }

    /**
     * Deny a request. The address it came from cannot ask again for
     * {@link #DENIED_LOCKOUT}.
     *
     * @param id the request's id.
     */
    public synchronized void deny(String id) {
        Request request = take(id);
        if (request == null) {
            return;
        }
        if (request.address() != null) {
            deniedUntil.put(request.address(), clock.instant().plus(DENIED_LOCKOUT));
        }
        answer(id, new RequestStatus(RequestState.DENIED, null));
    }

    /**
     * Withdraw a request, for example when the device stops waiting.
     *
     * @param id the request's id.
     */
    public synchronized void cancel(String id) {
        take(id);
        answers.remove(id);
        answerExpiry.remove(id);
    }

    /**
     * Where a request stands. An approval is handed out once: the token is
     * not kept.
     *
     * @param id the request's id.
     * @return the answer; unknown ids are {@link RequestState#EXPIRED}.
     */
    public synchronized RequestStatus requestStatus(String id) {
        prune(clock.instant());
        if (pending != null && pending.id().equals(id)) {
            return new RequestStatus(RequestState.PENDING, null);
        }
        answerExpiry.remove(id);
        RequestStatus answer = answers.remove(id);
        return answer == null ? new RequestStatus(RequestState.EXPIRED, null) : answer;
    }

    /**
     * @return the request waiting for an answer, or null.
     */
    public synchronized Request pendingRequest() {
        prune(clock.instant());
        return pending;
    }

    /**
     * @param listener called, on the requesting thread, when a device asks
     * to pair; Quelea shows the question.
     */
    public void addRequestListener(Consumer<Request> listener) {
        requestListeners.add(listener);
    }

    /**
     * @param listener a listener added with {@link #addRequestListener}.
     */
    public void removeRequestListener(Consumer<Request> listener) {
        requestListeners.remove(listener);
    }

    private Request take(String id) {
        prune(clock.instant());
        if (pending == null || !pending.id().equals(id)) {
            return null;
        }
        Request request = pending;
        pending = null;
        return request;
    }

    private void answer(String id, RequestStatus status) {
        answers.put(id, status);
        // The device asks every few seconds; an answer nobody picks up goes.
        answerExpiry.put(id, clock.instant().plus(REQUEST_LIFETIME));
    }

    private void prune(Instant now) {
        if (pending != null && !now.isBefore(pending.expires())) {
            pending = null;
        }
        answerExpiry.entrySet().removeIf(entry -> {
            boolean old = !now.isBefore(entry.getValue());
            if (old) {
                answers.remove(entry.getKey());
            }
            return old;
        });
        deniedUntil.values().removeIf(until -> !now.isBefore(until));
    }

    /**
     * @param listener called after a device has paired, for example to
     * close the pairing dialog.
     */
    public void addPairedListener(Consumer<DeviceRegistry.Paired> listener) {
        pairedListeners.add(listener);
    }

    /**
     * @param listener a listener added with {@link #addPairedListener}.
     */
    public void removePairedListener(Consumer<DeviceRegistry.Paired> listener) {
        pairedListeners.remove(listener);
    }

    private DeviceRegistry.Paired paired(DeviceRegistry.Paired paired) {
        pairedListeners.forEach(listener -> listener.accept(paired));
        return paired;
    }

    private OpenCode current() {
        if (open != null && !clock.instant().isBefore(open.expires())) {
            open = null;
        }
        return open;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
