package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeviceRegistryTest {

    @TempDir
    Path dir;

    private final MutableClock clock = new MutableClock();

    private DeviceRegistry registry() {
        return new DeviceRegistry(dir.resolve("remote-devices.json"), clock, new SecureRandom());
    }

    @Test
    void pairedTokenAuthenticates() {
        DeviceRegistry registry = registry();
        DeviceRegistry.Paired paired = registry.pair("Arvid's phone", "ios", Scope.CONTROL);

        DeviceAuth.Device device = registry.authenticate(paired.token()).orElseThrow();
        assertEquals(paired.deviceId(), device.id());
        assertEquals("Arvid's phone", device.name());
        assertEquals(Scope.CONTROL, device.scope());
        assertTrue(registry.authenticate("not-a-token").isEmpty());
        assertTrue(registry.authenticate(null).isEmpty());
        assertTrue(paired.token().length() >= 43, "256-bit token");
    }

    @Test
    void tokensAreUniqueAndOnlyHashesAreStored() throws Exception {
        DeviceRegistry registry = registry();
        DeviceRegistry.Paired a = registry.pair("A", "android", Scope.VIEW);
        DeviceRegistry.Paired b = registry.pair("B", "android", Scope.VIEW);
        assertNotEquals(a.token(), b.token());
        assertNotEquals(a.deviceId(), b.deviceId());

        String file = Files.readString(dir.resolve("remote-devices.json"));
        assertFalse(file.contains(a.token()), "the token itself must never be written");
        assertTrue(file.contains(DeviceRegistry.hash(a.token())));
        assertTrue(file.contains("\"view\""), "scopes are stored in lowercase like the API");
    }

    @Test
    void devicesSurviveARestart() {
        DeviceRegistry.Paired paired = registry().pair("Tablet", "ios", Scope.VIEW);
        DeviceRegistry reloaded = registry();
        assertEquals(Scope.VIEW, reloaded.authenticate(paired.token()).orElseThrow().scope());
        assertEquals(1, reloaded.list().size());
    }

    @Test
    void revokeStopsTheTokenAndTellsListeners() {
        DeviceRegistry registry = registry();
        DeviceRegistry.Paired paired = registry.pair("Phone", "ios", Scope.CONTROL);
        List<String> revoked = new ArrayList<>();
        List<String> changes = new ArrayList<>();
        registry.addRevokedListener(revoked::add);
        registry.addChangeListener(() -> changes.add("changed"));

        assertTrue(registry.revoke(paired.deviceId()));
        assertTrue(registry.authenticate(paired.token()).isEmpty());
        assertEquals(List.of(paired.deviceId()), revoked);
        assertEquals(1, changes.size());
        assertFalse(registry.revoke(paired.deviceId()), "already gone");
        assertTrue(registry().authenticate(paired.token()).isEmpty(), "revocation is saved");
    }

    @Test
    void lastSeenIsUpdated() {
        DeviceRegistry registry = registry();
        DeviceRegistry.Paired paired = registry.pair("Phone", "ios", Scope.CONTROL);
        long created = registry.list().getFirst().created();
        clock.advance(Duration.ofMinutes(10));
        registry.authenticate(paired.token());
        assertEquals(created + Duration.ofMinutes(10).toMillis(), registry.list().getFirst().lastSeen());
    }

    @Test
    void brokenFileIsMovedAsideInsteadOfFailing() throws Exception {
        Path file = dir.resolve("remote-devices.json");
        Files.writeString(file, "{not json");
        DeviceRegistry registry = registry();
        assertTrue(registry.list().isEmpty());
        assertTrue(Files.exists(dir.resolve("remote-devices.json.bad")));
        registry.pair("Phone", "ios", Scope.CONTROL);
        assertEquals(1, registry().list().size());
    }

    @Test
    void namesAreCleaned() {
        assertEquals("Phone", DeviceRegistry.clean("  Phone\n", 64, "x"));
        assertEquals("x", DeviceRegistry.clean("   ", 64, "x"));
        assertEquals("x", DeviceRegistry.clean(null, 64, "x"));
        assertEquals("abc", DeviceRegistry.clean("abcdef", 3, "x"));
    }
}
