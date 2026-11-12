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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Finds the address other devices on the local network reach this computer
 * on: the one in the pairing QR code and in the mDNS announcement.
 * <p>
 * Taking the first interface with an IPv4 address is not enough: virtual
 * machines, containers and VPNs add bridges and tunnels (bridge101, vmnet8,
 * utun3, vEthernet …) whose addresses phones cannot reach.
 */
public final class LanAddress {

    /**
     * Interfaces that are never the LAN: bridges and adapters of virtual
     * machines and containers, VPN tunnels, Apple's peer-to-peer links.
     */
    private static final Pattern VIRTUAL_NAME = Pattern.compile(
            "^(bridge|vmenet|vmnet|vnic|utun|tun|tap|ppp|ipsec|awdl|llw|anpi|gif|stf|ap\\d|docker|br-|veth|virbr|"
            + "vboxnet|zt|tailscale|wg)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VIRTUAL_DISPLAY_NAME = Pattern.compile(
            "virtual|vmware|virtualbox|hyper-v|vethernet|docker|wsl|tap-|tunnel|vpn|loopback",
            Pattern.CASE_INSENSITIVE);
    /**
     * A public address used only to ask which interface the default route
     * uses; connecting a UDP socket sends nothing.
     */
    private static final String ROUTE_PROBE = "8.8.8.8";

    /**
     * An interface address that could be the LAN address.
     *
     * @param name the interface name, for example "en0".
     * @param displayName the interface's display name.
     * @param address the IPv4 address.
     */
    record Candidate(String name, String displayName, InetAddress address) {
    }

    private LanAddress() {
    }

    /**
     * @return the LAN address, or empty if there is no network.
     */
    public static Optional<InetAddress> find() {
        List<Candidate> candidates = candidates();
        return choose(defaultRouteAddress(), candidates);
    }

    /**
     * Pick the LAN address: the default route's address if it belongs to a
     * real interface, otherwise the best real interface.
     *
     * @param routed the address of the default route, or null.
     * @param candidates the IPv4 addresses of the interfaces that are up.
     * @return the chosen address.
     */
    static Optional<InetAddress> choose(InetAddress routed, List<Candidate> candidates) {
        List<Candidate> real = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (!isVirtual(candidate)) {
                real.add(candidate);
            }
        }
        if (routed != null) {
            for (Candidate candidate : real) {
                if (candidate.address().equals(routed)) {
                    return Optional.of(routed);
                }
            }
        }
        return real.stream().min(Comparator.comparingInt(LanAddress::rank)).map(Candidate::address);
    }

    static boolean isVirtual(Candidate candidate) {
        return VIRTUAL_NAME.matcher(candidate.name()).find()
                || candidate.displayName() != null && VIRTUAL_DISPLAY_NAME.matcher(candidate.displayName()).find();
    }

    /**
     * Lower is better: private addresses on wired or wireless adapters first.
     */
    private static int rank(Candidate candidate) {
        int rank = 0;
        if (!candidate.address().isSiteLocalAddress()) {
            rank += 10;
        }
        String name = candidate.name().toLowerCase(Locale.ROOT);
        if (!(name.startsWith("en") || name.startsWith("eth") || name.startsWith("wl"))) {
            rank += 1;
        }
        return rank;
    }

    private static InetAddress defaultRouteAddress() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(ROUTE_PROBE), 9);
            InetAddress local = socket.getLocalAddress();
            return local == null || local.isAnyLocalAddress() || local.isLoopbackAddress() ? null : local;
        } catch (IOException | RuntimeException ex) {
            // No default route, for example on a network without a router.
            return null;
        }
    }

    private static List<Candidate> candidates() {
        List<Candidate> candidates = new ArrayList<>();
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (!nic.isUp() || nic.isLoopback() || nic.isVirtual() || nic.isPointToPoint()) {
                        continue;
                    }
                } catch (SocketException ex) {
                    continue;
                }
                for (InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()
                            && !address.isLinkLocalAddress()) {
                        candidates.add(new Candidate(nic.getName(), nic.getDisplayName(), address));
                    }
                }
            }
        } catch (SocketException ex) {
            // No interfaces to list.
        }
        return candidates;
    }
}
