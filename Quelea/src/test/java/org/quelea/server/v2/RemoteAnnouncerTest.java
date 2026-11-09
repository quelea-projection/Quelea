package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RemoteAnnouncerTest {

    @Test
    void txtRecordWithoutTls() {
        assertEquals(Map.of("v", "2", "path", "/api/v2", "tls", "0", "quelea", "2027.0"),
                RemoteAnnouncer.txt(1113, 0, "2027.0"));
    }

    @Test
    void txtRecordWithTls() {
        Map<String, String> txt = RemoteAnnouncer.txt(1113, 1114, "2027.0");
        assertEquals("1", txt.get("tls"));
        assertEquals("1114", txt.get("tp"));
    }

    @Test
    void instanceNames() {
        assertEquals("Quelea (church-pc)", RemoteAnnouncer.instanceName("church-pc"));
        assertEquals("Quelea (Church-Mac)", RemoteAnnouncer.instanceName("Church-Mac.local"));
        assertEquals("Quelea", RemoteAnnouncer.instanceName(null));
        assertEquals("Quelea", RemoteAnnouncer.instanceName("localhost"));
    }
}
