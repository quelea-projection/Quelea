package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LanAddressTest {

    private static LanAddress.Candidate nic(String name, String display, String ip) throws UnknownHostException {
        return new LanAddress.Candidate(name, display, InetAddress.getByName(ip));
    }

    @Test
    void defaultRouteWinsOverABridgeListedFirst() throws Exception {
        // The order macOS lists them on a Mac running a virtual machine.
        List<LanAddress.Candidate> nics = List.of(
                nic("bridge101", "bridge101", "192.168.97.0"),
                nic("en0", "en0", "192.168.1.112"));
        assertEquals(Optional.of(InetAddress.getByName("192.168.1.112")),
                LanAddress.choose(InetAddress.getByName("192.168.1.112"), nics));
    }

    @Test
    void withoutARouteBridgesAndTunnelsAreSkipped() throws Exception {
        List<LanAddress.Candidate> nics = List.of(
                nic("bridge100", "bridge100", "192.168.64.1"),
                nic("utun3", "utun3", "10.8.0.2"),
                nic("vmnet8", "vmnet8", "172.16.5.1"),
                nic("en0", "en0", "192.168.1.112"));
        assertEquals(Optional.of(InetAddress.getByName("192.168.1.112")), LanAddress.choose(null, nics));
    }

    @Test
    void routeThroughAVpnIsNotUsed() throws Exception {
        List<LanAddress.Candidate> nics = List.of(
                nic("utun4", "utun4", "10.8.0.2"),
                nic("en0", "en0", "192.168.1.112"));
        assertEquals(Optional.of(InetAddress.getByName("192.168.1.112")),
                LanAddress.choose(InetAddress.getByName("10.8.0.2"), nics));
    }

    @Test
    void windowsVirtualAdaptersAreSkipped() throws Exception {
        List<LanAddress.Candidate> nics = List.of(
                nic("eth3", "vEthernet (WSL)", "172.25.160.1"),
                nic("eth1", "VirtualBox Host-Only Ethernet Adapter", "192.168.56.1"),
                nic("wlan0", "Intel(R) Wi-Fi 6 AX201", "192.168.0.23"));
        assertEquals(Optional.of(InetAddress.getByName("192.168.0.23")), LanAddress.choose(null, nics));
    }

    @Test
    void privateAddressesComeFirst() throws Exception {
        List<LanAddress.Candidate> nics = List.of(
                nic("en1", "en1", "84.12.34.56"),
                nic("en0", "en0", "10.0.0.7"));
        assertEquals(Optional.of(InetAddress.getByName("10.0.0.7")), LanAddress.choose(null, nics));
    }

    @Test
    void noNetworkGivesEmpty() throws Exception {
        assertTrue(LanAddress.choose(null, List.of(nic("bridge0", "bridge0", "192.168.2.1"))).isEmpty());
    }
}
