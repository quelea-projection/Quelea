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
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import org.quelea.services.utils.LoggerUtils;

/**
 * Announces the v2 server on the local network with mDNS/DNS-SD as
 * {@value #SERVICE_TYPE}, so apps can find Quelea without typing an address.
 * The TXT record says which API version and ports to use.
 * <p>
 * Networks that block multicast (many guest networks) hide the
 * announcement; the pairing QR code still works there.
 */
public final class RemoteAnnouncer implements AutoCloseable {

    /**
     * The DNS-SD service type. Apps list it in iOS's NSBonjourServices.
     */
    public static final String SERVICE_TYPE = "_quelea._tcp.local.";

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private final Object lock = new Object();
    private JmDNS jmdns;
    private boolean closed;

    /**
     * The TXT record of the announcement.
     *
     * @param port the v2 HTTP port.
     * @param tlsPort the TLS port, or 0 if TLS is off.
     * @param queleaVersion the Quelea version.
     * @return the TXT properties, in a stable order.
     */
    static Map<String, String> txt(int port, int tlsPort, String queleaVersion) {
        Map<String, String> txt = new LinkedHashMap<>();
        txt.put("v", "2");
        txt.put("path", ApiHandler.PREFIX);
        txt.put("tls", tlsPort > 0 ? "1" : "0");
        if (tlsPort > 0) {
            txt.put("tp", Integer.toString(tlsPort));
        }
        txt.put("quelea", queleaVersion);
        return txt;
    }

    /**
     * @param hostName this computer's name, or null.
     * @return the instance name shown in apps, for example
     * "Quelea (church-pc)".
     */
    static String instanceName(String hostName) {
        String local = PairingUri.localName(hostName);
        if (local == null) {
            return "Quelea";
        }
        return "Quelea (" + local.substring(0, local.length() - ".local".length()) + ")";
    }

    /**
     * Start announcing on a background thread; joining the multicast group
     * can take a few seconds and must not hold up the caller.
     *
     * @param address the address to announce on, the one in the QR code.
     * @param hostName this computer's name, or null.
     * @param port the v2 HTTP port.
     * @param tlsPort the TLS port, or 0.
     * @param queleaVersion the Quelea version.
     */
    public void start(InetAddress address, String hostName, int port, int tlsPort, String queleaVersion) {
        Thread starter = new Thread(() -> announce(address, hostName, port, tlsPort, queleaVersion),
                "remote-v2-mdns");
        starter.setDaemon(true);
        starter.start();
    }

    private void announce(InetAddress address, String hostName, int port, int tlsPort, String queleaVersion) {
        try {
            String local = PairingUri.localName(hostName);
            JmDNS created = JmDNS.create(address, local == null ? "quelea" : local.replace(".local", ""));
            ServiceInfo service = ServiceInfo.create(SERVICE_TYPE, instanceName(hostName), port, 0, 0,
                    txt(port, tlsPort, queleaVersion));
            created.registerService(service);
            synchronized (lock) {
                if (closed) {
                    // Quelea started shutting down while we were joining.
                    closeQuietly(created);
                    return;
                }
                jmdns = created;
            }
            LOGGER.log(Level.INFO, "Announcing remote control v2 as \"{0}\" on {1}",
                    new Object[]{service.getName(), address.getHostAddress()});
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Could not announce remote control v2 on the network", ex);
        }
    }

    /**
     * Stop announcing; apps see the service disappear.
     */
    @Override
    public void close() {
        JmDNS toClose;
        synchronized (lock) {
            closed = true;
            toClose = jmdns;
            jmdns = null;
        }
        if (toClose != null) {
            toClose.unregisterAllServices();
            closeQuietly(toClose);
        }
    }

    private static void closeQuietly(JmDNS jmdns) {
        try {
            jmdns.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Could not close mDNS", ex);
        }
    }
}
