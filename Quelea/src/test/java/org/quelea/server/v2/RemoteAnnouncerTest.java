package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RemoteAnnouncerTest {

    @Test
    void txtRecordWithoutTls() {
        assertEquals(Map.of("v", "2", "path", "/api/v2", "tls", "0", "quelea", "2027.0"),
                RemoteAnnouncer.txt(1113, 0, "2027.0", null));
    }

    @Test
    void txtRecordCarriesTheIpv4Address() throws Exception {
        Map<String, String> txt = RemoteAnnouncer.txt(1113, 0, "2027.0",
                java.net.InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 20}));
        assertEquals("192.168.1.20", txt.get("addr"));
    }

    @Test
    void txtRecordWithTls() {
        Map<String, String> txt = RemoteAnnouncer.txt(1113, 1114, "2027.0", null);
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

    @Test
    void bonjourCommandRegistersTheServiceWithItsTxtRecord() {
        java.util.Map<String, String> txt = new java.util.LinkedHashMap<>();
        txt.put("v", "2");
        txt.put("addr", "192.168.1.20");
        assertEquals(java.util.List.of("/usr/bin/dns-sd", "-R", "Quelea (church-pc)", "_quelea._tcp", "local", "1113",
                "v=2", "addr=192.168.1.20"), RemoteAnnouncer.dnsSdCommand("Quelea (church-pc)", 1113, txt));
    }
}
