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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PairingServiceTest {

    @TempDir
    Path dir;

    private final MutableClock clock = new MutableClock();
    private DeviceRegistry registry;
    private PairingService pairing;

    @BeforeEach
    void setUp() {
        registry = new DeviceRegistry(dir.resolve("devices.json"), clock, new SecureRandom());
        pairing = new PairingService(registry, clock, new SecureRandom());
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
    void listenersHearAboutPairing() {
        List<String> names = new ArrayList<>();
        pairing.addPairedListener(paired -> names.add(paired.name()));
        pairing.pairWithCode(pairing.open(Scope.CONTROL).code(), "Phone", "ios");
        assertEquals(List.of("Phone"), names);
    }

    @Test
    void requestExpiresUnanswered() {
        PairingService.Request request = pairing.request("Phone", "ios", "10.0.0.5");
        assertEquals(PairingService.RequestState.PENDING, pairing.requestStatus(request.id()).state());
        clock.advance(PairingService.REQUEST_LIFETIME);
        assertEquals(PairingService.RequestState.EXPIRED, pairing.requestStatus(request.id()).state());
        assertEquals(null, pairing.approve(request.id(), Scope.CONTROL));
        // Someone else may ask now.
        pairing.request("Tablet", "android", "10.0.0.6");
    }

    @Test
    void viewOnlyApprovalGivesViewScope() {
        PairingService.Request request = pairing.request("Stage", "android", null);
        DeviceRegistry.Paired paired = pairing.approve(request.id(), Scope.VIEW);
        assertEquals(Scope.VIEW, paired.scope());
        assertEquals(paired, pairing.requestStatus(request.id()).paired());
    }

    @Test
    void deniedAddressMayAskAgainLater() {
        pairing.deny(pairing.request("Phone", "ios", "10.0.0.5").id());
        assertEquals(PairingService.Failure.LOCKED, failure(() -> pairing.request("Phone", "ios", "10.0.0.5")));
        pairing.request("Other", "ios", "10.0.0.6");
        pairing.cancel(pairing.pendingRequest().id());
        clock.advance(PairingService.DENIED_LOCKOUT);
        pairing.request("Phone", "ios", "10.0.0.5");
    }
}
