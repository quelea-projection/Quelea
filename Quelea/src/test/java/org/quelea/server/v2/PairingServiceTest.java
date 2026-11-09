package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PairingServiceTest {

    @TempDir
    Path dir;

    private final MutableClock clock = new MutableClock();
    private final AtomicBoolean passwordAllowed = new AtomicBoolean(true);
    private DeviceRegistry registry;
    private PairingService pairing;

    @BeforeEach
    void setUp() {
        registry = new DeviceRegistry(dir.resolve("devices.json"), clock, new SecureRandom());
        pairing = new PairingService(registry, () -> "secret", passwordAllowed::get, clock, new SecureRandom());
    }

    private PairingService.Failure failure(Runnable attempt) {
        return assertThrows(PairingService.PairingException.class, attempt::run).getFailure();
    }

    @Test
    void codeLooksRightAndPairsOnce() {
        PairingService.OpenCode code = pairing.open(Scope.CONTROL);
        assertTrue(code.code().matches("[2-9A-HJKMNP-Z]{6}"), code.code());
        assertTrue(pairing.isOpen());

        DeviceRegistry.Paired paired = pairing.pairWithCode(code.code().toLowerCase(), "Phone", "ios");
        assertEquals(Scope.CONTROL, paired.scope());
        assertTrue(registry.authenticate(paired.token()).isPresent());
        assertFalse(pairing.isOpen(), "one code, one device");
        assertEquals(PairingService.Failure.CLOSED, failure(() -> pairing.pairWithCode(code.code(), "Other", "ios")));
    }

    @Test
    void viewOnlyCodeGivesViewScope() {
        PairingService.OpenCode code = pairing.open(Scope.VIEW);
        assertEquals(Scope.VIEW, pairing.pairWithCode(code.code(), "Stage", "android").scope());
    }

    @Test
    void codeExpires() {
        PairingService.OpenCode code = pairing.open(Scope.CONTROL);
        clock.advance(PairingService.CODE_LIFETIME);
        assertFalse(pairing.isOpen());
        assertEquals(PairingService.Failure.CLOSED, failure(() -> pairing.pairWithCode(code.code(), "Phone", "ios")));
    }

    @Test
    void fiveWrongCodesClosePairing() {
        PairingService.OpenCode code = pairing.open(Scope.CONTROL);
        for (int i = 1; i < PairingService.MAX_FAILURES; i++) {
            assertEquals(PairingService.Failure.WRONG, failure(() -> pairing.pairWithCode("ZZZZZZ", "x", "x")));
        }
        assertEquals(PairingService.Failure.LOCKED, failure(() -> pairing.pairWithCode("ZZZZZZ", "x", "x")));
        assertEquals(PairingService.Failure.CLOSED, failure(() -> pairing.pairWithCode(code.code(), "x", "x")),
                "even the right code no longer works");

        PairingService.OpenCode reopened = pairing.open(Scope.CONTROL);
        assertTrue(pairing.pairWithCode(reopened.code(), "x", "x").token().length() > 0);
    }

    @Test
    void closeStopsTheCode() {
        PairingService.OpenCode code = pairing.open(Scope.CONTROL);
        pairing.close();
        assertEquals(PairingService.Failure.CLOSED, failure(() -> pairing.pairWithCode(code.code(), "x", "x")));
    }

    @Test
    void passwordPairing() {
        assertEquals(Scope.CONTROL, pairing.pairWithPassword("secret", "Laptop", "web").scope());
        assertEquals(PairingService.Failure.WRONG, failure(() -> pairing.pairWithPassword("Secret", "x", "x")));
        passwordAllowed.set(false);
        assertEquals(PairingService.Failure.PASSWORD_DISABLED,
                failure(() -> pairing.pairWithPassword("secret", "x", "x")));
    }

    @Test
    void wrongPasswordsLockForAMinute() {
        for (int i = 1; i < PairingService.MAX_FAILURES; i++) {
            failure(() -> pairing.pairWithPassword("guess", "x", "x"));
        }
        assertEquals(PairingService.Failure.LOCKED, failure(() -> pairing.pairWithPassword("guess", "x", "x")));
        assertEquals(PairingService.Failure.LOCKED, failure(() -> pairing.pairWithPassword("secret", "x", "x")),
                "locked even for the right password");
        clock.advance(PairingService.PASSWORD_LOCKOUT.plus(Duration.ofSeconds(1)));
        assertTrue(pairing.pairWithPassword("secret", "x", "x").token().length() > 0);
    }

    @Test
    void listenersHearAboutPairing() {
        List<String> names = new ArrayList<>();
        pairing.addPairedListener(paired -> names.add(paired.name()));
        pairing.pairWithPassword("secret", "Phone", "ios");
        assertEquals(List.of("Phone"), names);
    }
}
