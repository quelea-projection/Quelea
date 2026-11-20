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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 * The paired devices, stored in {@code remote-devices.json} in Quelea's user
 * folder. Only a SHA-256 hash of each token is stored, so the file does not
 * let anyone control Quelea.
 * <p>
 * This is the {@link DeviceAuth} the v2 server uses once devices can be
 * paired. All methods are thread-safe.
 */
public final class DeviceRegistry implements DeviceAuth {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int TOKEN_BYTES = 32;
    /**
     * Last-seen times are saved at most this often, so that every request
     * does not write the file.
     */
    private static final Duration LAST_SEEN_SAVE_INTERVAL = Duration.ofMinutes(5);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * A paired device as shown in the settings.
     *
     * @param id the device id.
     * @param name the name the device gave.
     * @param platform the platform the device gave, for example "ios".
     * @param scope what the device may do.
     * @param created when it was paired, in epoch milliseconds.
     * @param lastSeen when it last made a request, in epoch milliseconds.
     */
    public record DeviceInfo(String id, String name, String platform, Scope scope, long created, long lastSeen) {
    }

    /**
     * The result of pairing: the only time the token is known in clear.
     *
     * @param deviceId the new device's id.
     * @param name the device's name as stored.
     * @param token the token the device must send from now on.
     * @param scope what the device may do.
     */
    public record Paired(String deviceId, String name, String token, Scope scope) {
    }

    /**
     * As stored in the file.
     */
    private record Stored(String id, String name, String platform, Scope scope, String tokenHash, long created,
            long lastSeen) {

        Stored seen(long when) {
            return new Stored(id, name, platform, scope, tokenHash, created, when);
        }

        DeviceInfo info() {
            return new DeviceInfo(id, name, platform, scope, created, lastSeen);
        }
    }

    private final Path file;
    private final Clock clock;
    private final SecureRandom random;
    private final Map<String, Stored> byHash = new LinkedHashMap<>();
    private final List<Consumer<String>> revokedListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private long lastSaved;

    /**
     * Load the registry from a file. A missing file means no devices; an
     * unreadable one is moved aside and logged, so that a broken file never
     * stops Quelea from starting.
     *
     * @param file the JSON file.
     */
    public DeviceRegistry(Path file) {
        this(file, Clock.systemUTC(), new SecureRandom());
    }

    DeviceRegistry(Path file, Clock clock, SecureRandom random) {
        this.file = file;
        this.clock = clock;
        this.random = random;
        load();
    }

    @Override
    public Optional<Device> authenticate(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        String hash = hash(token);
        synchronized (this) {
            Stored stored = byHash.get(hash);
            if (stored == null) {
                return Optional.empty();
            }
            long now = clock.millis();
            byHash.put(hash, stored.seen(now));
            if (now - lastSaved > LAST_SEEN_SAVE_INTERVAL.toMillis()) {
                save();
            }
            return Optional.of(new Device(stored.id(), stored.name(), stored.scope()));
        }
    }

    /**
     * Pair a new device.
     *
     * @param name the name the device gave.
     * @param platform the platform the device gave, may be null.
     * @param scope what the device may do.
     * @return the device id and its token.
     */
    public Paired pair(String name, String platform, Scope scope) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        byte[] idBytes = new byte[4];
        random.nextBytes(idBytes);
        String id = "d_" + HexFormat.of().formatHex(idBytes);
        long now = clock.millis();
        String storedName = clean(name, 64, "Device");
        synchronized (this) {
            byHash.put(hash(token), new Stored(id, storedName, clean(platform, 32, "unknown"),
                    scope, hash(token), now, now));
            save();
        }
        LOGGER.log(Level.INFO, "Paired remote device {0} ({1})", new Object[]{id, storedName});
        changeListeners.forEach(Runnable::run);
        return new Paired(id, storedName, token, scope);
    }

    /**
     * Revoke a device: its token stops working at once and its connections
     * are closed.
     *
     * @param deviceId the device id.
     * @return true if there was such a device.
     */
    public boolean revoke(String deviceId) {
        boolean removed;
        synchronized (this) {
            removed = byHash.values().removeIf(stored -> stored.id().equals(deviceId));
            if (removed) {
                save();
            }
        }
        if (removed) {
            LOGGER.log(Level.INFO, "Revoked remote device {0}", deviceId);
            revokedListeners.forEach(listener -> listener.accept(deviceId));
            changeListeners.forEach(Runnable::run);
        }
        return removed;
    }

    /**
     * @return the paired devices, oldest first.
     */
    public synchronized List<DeviceInfo> list() {
        return byHash.values().stream().map(Stored::info).toList();
    }

    /**
     * @param listener called with the device id when a device is revoked.
     */
    public void addRevokedListener(Consumer<String> listener) {
        revokedListeners.add(listener);
    }

    /**
     * @param listener called when a device is paired or revoked, for
     * example to refresh the settings.
     */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    /**
     * Save last-seen times; called when Quelea exits.
     */
    public synchronized void flush() {
        save();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            List<Stored> stored = GSON.fromJson(json, new TypeToken<List<Stored>>() {
            }.getType());
            if (stored != null) {
                for (Stored device : stored) {
                    if (device != null && device.tokenHash() != null && device.id() != null && device.scope() != null) {
                        byHash.put(device.tokenHash(), device);
                    }
                }
            }
        } catch (IOException | JsonParseException ex) {
            Path aside = file.resolveSibling(file.getFileName() + ".bad");
            LOGGER.log(Level.WARNING, "Could not read " + file + ", moving it to " + aside
                    + "; devices must be paired again", ex);
            try {
                Files.move(file, aside, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveEx) {
                LOGGER.log(Level.WARNING, "Could not move " + file + " aside", moveEx);
            }
        }
    }

    /**
     * Write the file atomically: a crash while saving leaves the old file.
     */
    private void save() {
        lastSaved = clock.millis();
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(new ArrayList<>(byHash.values())), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not save " + file, ex);
        }
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is always available", ex);
        }
    }

    /**
     * Trim a name a device sent: no control characters, a sensible length.
     */
    static String clean(String text, int maxLength, String fallback) {
        if (text == null) {
            return fallback;
        }
        String cleaned = text.replaceAll("\\p{Cntrl}", " ").strip();
        if (cleaned.isEmpty()) {
            return fallback;
        }
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
    }
}
