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

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;
import org.quelea.services.utils.LoggerUtils;

/**
 * The v2 remote control server: the JSON API and the WebSocket event stream
 * on one port, separate from the v1 server.
 */
public final class RemoteApiServer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Duration WS_IDLE_TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_MESSAGE_SIZE = 64 * 1024;

    /**
     * Server settings.
     *
     * @param port the port to listen on; 0 picks a free one (for tests).
     * @param publicEnabled true to answer the open endpoints under /public/,
     * which follow the mobile lyrics setting.
     * @param queleaVersion the Quelea version reported by /info.
     */
    public record Config(int port, boolean publicEnabled, String queleaVersion) {
    }

    private final Server server;
    private final ServerConnector connector;
    private final EventHub hub;
    private final DeviceAuth auth;
    private final PairingService pairing;

    /**
     * Create the server.
     *
     * @param config the settings.
     * @param control what the API can see and do in Quelea.
     * @param bus the events to forward to WebSocket clients.
     * @param auth checks tokens; a {@link DeviceRegistry} also handles
     * revocation.
     * @param pairing lets new devices pair, or null to refuse pairing.
     */
    public RemoteApiServer(Config config, QueleaControl control, RemoteEventBus bus, DeviceAuth auth,
            PairingService pairing) {
        this.pairing = pairing;
        this.auth = auth;
        QueuedThreadPool threads = new QueuedThreadPool(16, 2);
        threads.setName("remote-v2");
        threads.setDaemon(true);
        server = new Server(threads);

        HttpConfiguration http = new HttpConfiguration();
        http.setSendServerVersion(false);
        connector = new ServerConnector(server, new HttpConnectionFactory(http));
        connector.setPort(config.port());
        server.addConnector(connector);

        hub = new EventHub(control, auth, bus);
        WebSocketUpgradeHandler webSockets = WebSocketUpgradeHandler.from(server, container -> {
            container.setIdleTimeout(WS_IDLE_TIMEOUT);
            container.setMaxTextMessageSize(MAX_MESSAGE_SIZE);
            container.addMapping(ApiHandler.PREFIX + "/ws", hub::createPrivate);
            if (config.publicEnabled()) {
                container.addMapping(ApiHandler.PREFIX + "/public/ws", hub::createPublic);
            }
        });
        webSockets.setHandler(new ApiHandler(control, auth, pairing, config));
        auth.addRevokedListener(hub::revoked);
        server.setHandler(webSockets);
        server.setStopTimeout(1000);
    }

    /**
     * Start listening.
     *
     * @throws Exception if the port is taken or Jetty fails to start.
     */
    public void start() throws Exception {
        server.start();
        hub.start();
        LOGGER.log(Level.INFO, "Remote control v2 listening on port {0}", getPort());
    }

    /**
     * Stop the server and close all client connections.
     */
    public void stop() {
        hub.close();
        try {
            server.stop();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not stop remote control v2 server", ex);
        }
    }

    /**
     * @return the port the server listens on.
     */
    public int getPort() {
        return connector.getLocalPort();
    }

    /**
     * @return the pairing service, or null if pairing is not available.
     */
    public PairingService getPairing() {
        return pairing;
    }

    /**
     * @return the paired devices, or null if this server does not use a
     * {@link DeviceRegistry}.
     */
    public DeviceRegistry getDevices() {
        return auth instanceof DeviceRegistry registry ? registry : null;
    }

    /**
     * @return the number of connected WebSocket clients.
     */
    int clientCount() {
        return hub.clientCount();
    }
}
