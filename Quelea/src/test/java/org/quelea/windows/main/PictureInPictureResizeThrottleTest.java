package org.quelea.windows.main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PictureInPictureResizeThrottleTest {
    @Test
    void limitsBurstsButAlwaysAppliesTheFinalGeometry() {
        var gate = new PictureInPictureWindow.NativeResizeGate();
        assertTrue(gate.shouldUpdate(0, false));
        for (long elapsed = 1_000_000; elapsed < 12_000_000; elapsed += 1_000_000) {
            assertFalse(gate.shouldUpdate(elapsed, false), "a native pulse must not trigger another resize immediately");
        }
        assertTrue(gate.shouldUpdate(12_000_000, false));
        assertFalse(gate.shouldUpdate(13_000_000, false));
        assertTrue(gate.shouldUpdate(14_000_000, true), "the final size must not be skipped");
        assertFalse(gate.shouldUpdate(15_000_000, false));
        assertTrue(gate.shouldUpdate(26_000_000, false));
    }
}
