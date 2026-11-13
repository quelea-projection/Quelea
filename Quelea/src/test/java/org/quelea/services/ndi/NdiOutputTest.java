package org.quelea.services.ndi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NdiOutputTest {

    @Test
    void keepsAFullHdCanvasAsItIs() {
        assertArrayEquals(new int[]{1920, 1080}, NdiOutput.frameSize(1920, 1080, 1));
    }

    @Test
    void usesThePixelsOfAScaledScreen() {
        assertArrayEquals(new int[]{1920, 1080}, NdiOutput.frameSize(1280, 720, 1.5));
    }

    @Test
    void doesNotEnlargeBeyondTheScreen() {
        assertArrayEquals(new int[]{1280, 720}, NdiOutput.frameSize(1280, 720, 1));
    }

    @Test
    void shrinksA4kCanvasKeepingItsShape() {
        assertArrayEquals(new int[]{1920, 1080}, NdiOutput.frameSize(3840, 2160, 1));
        assertArrayEquals(new int[]{1662, 1080}, NdiOutput.frameSize(1512, 982, 2));
    }

    @Test
    void alwaysGivesEvenSizes() {
        assertArrayEquals(new int[]{1024, 766}, NdiOutput.frameSize(1025, 767, 1));
    }

    @Test
    void hasNoFrameForAnEmptyCanvas() {
        assertNull(NdiOutput.frameSize(0, 0, 1));
    }
}
