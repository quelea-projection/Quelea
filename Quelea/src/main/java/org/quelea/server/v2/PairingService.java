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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lets new devices pair, with a short code shown in Quelea or with the
 * remote control password.
 * <p>
 * A code is six characters, works once and expires after
 * {@link #CODE_LIFETIME}. Five wrong codes in a row close pairing until it is
 * opened again in Quelea, so a code cannot be guessed. Five wrong passwords
 * block password pairing for {@link #PASSWORD_LOCKOUT}.
 */
public final class PairingService {

    public static final Duration CODE_LIFETIME = Duration.ofMinutes(2);
    public static final Duration PASSWORD_LOCKOUT = Duration.ofMinutes(1);
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
         * The code or password is wrong.
         */
        WRONG,
        /**
         * Too many wrong attempts.
         */
        LOCKED,
        /**
         * Pairing with the password is turned off.
         */
        PASSWORD_DISABLED
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

    private final DeviceRegistry registry;
    private final Clock clock;
    private final SecureRandom random;
    private final Supplier<String> password;
    private final BooleanSupplier passwordAllowed;
    private final List<Consumer<DeviceRegistry.Paired>> pairedListeners = new CopyOnWriteArrayList<>();

    // Guarded by this.
    private OpenCode open;
    private int codeFailures;
    private int passwordFailures;
    private Instant passwordLockedUntil = Instant.MIN;

    /**
     * @param registry where paired devices are stored.
     * @param password the current remote control password.
     * @param passwordAllowed whether pairing with the password is on.
     */
    public PairingService(DeviceRegistry registry, Supplier<String> password, BooleanSupplier passwordAllowed) {
        this(registry, password, passwordAllowed, Clock.systemUTC(), new SecureRandom());
    }

    PairingService(DeviceRegistry registry, Supplier<String> password, BooleanSupplier passwordAllowed, Clock clock,
            SecureRandom random) {
        this.registry = registry;
        this.password = password;
        this.passwordAllowed = passwordAllowed;
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
     * Pair with the remote control password. Gives full control, like the
     * password does in v1.
     *
     * @param given the password the user typed.
     * @param name the device's name.
     * @param platform the device's platform, may be null.
     * @return the new device and its token.
     * @throws PairingException if the password is wrong, password pairing
     * is off or temporarily blocked.
     */
    public DeviceRegistry.Paired pairWithPassword(String given, String name, String platform) {
        synchronized (this) {
            if (!passwordAllowed.getAsBoolean()) {
                throw new PairingException(Failure.PASSWORD_DISABLED, "Pairing with the password is turned off");
            }
            Instant now = clock.instant();
            if (now.isBefore(passwordLockedUntil)) {
                throw new PairingException(Failure.LOCKED, "Too many wrong passwords; try again in a minute");
            }
            String expected = password.get();
            if (given == null || expected == null || expected.isEmpty() || !constantTimeEquals(given, expected)) {
                passwordFailures++;
                if (passwordFailures >= MAX_FAILURES) {
                    passwordFailures = 0;
                    passwordLockedUntil = now.plus(PASSWORD_LOCKOUT);
                    throw new PairingException(Failure.LOCKED, "Too many wrong passwords; try again in a minute");
                }
                throw new PairingException(Failure.WRONG, "Wrong password");
            }
            passwordFailures = 0;
        }
        return paired(registry.pair(name, platform, Scope.CONTROL));
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
