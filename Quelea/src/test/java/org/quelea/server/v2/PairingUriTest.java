package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PairingUriTest {

    @Test
    void buildsTheQrAddress() {
        assertEquals("quelea://pair?h=192.168.1.20&p=1113&c=7F3K9Q&n=church-pc.local",
                PairingUri.build("192.168.1.20", 1113, "7F3K9Q", "church-pc"));
    }

    @Test
    void localNames() {
        assertEquals("church-pc.local", PairingUri.localName("church-pc"));
        assertEquals("Church-Mac.local", PairingUri.localName("Church-Mac.local"));
        assertEquals("church-pc.local", PairingUri.localName("church-pc.example.org"));
        assertNull(PairingUri.localName("localhost"));
        assertNull(PairingUri.localName("192.168.1.20"));
        assertNull(PairingUri.localName(null));
    }

    @Test
    void leavesOutTheNameWhenUnknown() {
        assertEquals("quelea://pair?h=10.0.0.5&p=1113&c=ABCDEF", PairingUri.build("10.0.0.5", 1113, "ABCDEF", null));
    }
}
