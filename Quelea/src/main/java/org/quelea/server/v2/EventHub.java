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

import com.google.gson.JsonObject;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.ServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.ServerUpgradeResponse;
import org.quelea.services.utils.LoggerUtils;

/**
 * The WebSocket part of the v2 API. Keeps track of connected clients and
 * forwards every {@link RemoteEvent} to them.
 * <p>
 * A client on {@code /api/v2/ws} must send {@code {"type":"auth","token":…}}
 * within {@link #AUTH_TIMEOUT}. It then gets a {@code snapshot} of the full
 * state followed by events only. A client on {@code /api/v2/public/ws} needs
 * no token and only gets the live item, section and screen events.
 */
public final class EventHub implements RemoteEventBus.Listener, AutoCloseable {

    static final Duration AUTH_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Close codes, see the design document.
     */
    static final int CLOSE_BAD_MESSAGE = 4400;
    static final int CLOSE_UNAUTHORIZED = 4401;
    static final int CLOSE_GOING_AWAY = 1001;
    static final int CLOSE_SERVER_ERROR = 1011;

    private static final Set<String> PUBLIC_TYPES = Set.of("live.item", "live.section", "live.screen");
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private final QueleaControl control;
    private final DeviceAuth auth;
    private final RemoteEventBus bus;
    private final Set<Client> clients = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "remote-v2-auth-timeout");
        t.setDaemon(true);
        return t;
    });
    private RemoteEventBus.Subscription subscription;

    EventHub(QueleaControl control, DeviceAuth auth, RemoteEventBus bus) {
        this.control = control;
        this.auth = auth;
        this.bus = bus;
    }

    void start() {
        subscription = bus.subscribe(this);
    }

    @Override
    public void close() {
        if (subscription != null) {
            subscription.close();
        }
        timer.shutdownNow();
        for (Client client : clients) {
            client.close(CLOSE_GOING_AWAY, "Quelea is shutting down");
        }
    }

    /**
     * @return the number of connected clients, authenticated or not.
     */
    int clientCount() {
        return clients.size();
    }

    Object createPrivate(ServerUpgradeRequest request, ServerUpgradeResponse response,
            org.eclipse.jetty.util.Callback callback) {
        return create(request, response, callback, false);
    }

    Object createPublic(ServerUpgradeRequest request, ServerUpgradeResponse response,
            org.eclipse.jetty.util.Callback callback) {
        return create(request, response, callback, true);
    }

    private Object create(ServerUpgradeRequest request, ServerUpgradeResponse response,
            org.eclipse.jetty.util.Callback callback, boolean isPublic) {
        if (!originAllowed(request)) {
            response.setStatus(403);
            callback.succeeded();
            return null;
        }
        return new Client(isPublic);
    }

    /**
     * Native apps send no Origin. A browser does, and then it must be a page
     * served by this server, so that other web sites cannot connect.
     */
    static boolean originAllowed(Request request) {
        String origin = request.getHeaders().get(HttpHeader.ORIGIN);
        if (origin == null || origin.isEmpty()) {
            return true;
        }
        String host = request.getHeaders().get(HttpHeader.HOST);
        try {
            URI uri = URI.create(origin);
            if (uri.getHost() == null || host == null) {
                return false;
            }
            String originHost = uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
            return originHost.equalsIgnoreCase(host);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void onEvent(long seq, RemoteEvent event) {
        String json = Json.message(event.type(), seq, event);
        boolean publicType = PUBLIC_TYPES.contains(event.type());
        for (Client client : clients) {
            client.deliver(seq, json, publicType);
        }
    }

    private enum State {
        AWAITING_AUTH, SNAPSHOTTING, READY, CLOSED
    }

    private record Pending(long seq, String json) {
    }

    /**
     * One connected WebSocket client.
     */
    public final class Client implements Session.Listener.AutoDemanding {

        private final boolean isPublic;
        private volatile Session session;
        // Guarded by this.
        private State state = State.AWAITING_AUTH;
        private List<Pending> pending;
        private ScheduledFuture<?> authTimeout;

        Client(boolean isPublic) {
            this.isPublic = isPublic;
        }

        @Override
        public void onWebSocketOpen(Session session) {
            this.session = session;
            clients.add(this);
            if (isPublic) {
                sendSnapshot();
            } else {
                synchronized (this) {
                    authTimeout = timer.schedule(this::authTimedOut, AUTH_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                }
            }
        }

        @Override
        public void onWebSocketText(String text) {
            JsonObject message;
            try {
                message = Json.object(text);
            } catch (ApiException ex) {
                close(CLOSE_BAD_MESSAGE, "Messages must be JSON objects");
                return;
            }
            String type = Json.optionalString(message, "type");
            if ("ping".equals(type)) {
                send(Json.write(Map.of("type", "pong")));
                return;
            }
            boolean awaitingAuth;
            synchronized (this) {
                awaitingAuth = state == State.AWAITING_AUTH;
            }
            if (!isPublic && awaitingAuth && "auth".equals(type)) {
                authenticate(Json.optionalString(message, "token"));
                return;
            }
            close(CLOSE_BAD_MESSAGE, "Unexpected message");
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason, Callback callback) {
            closed();
            callback.succeed();
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            LOGGER.log(Level.FINE, "Remote v2 WebSocket error", cause);
            closed();
        }

        private void authenticate(String token) {
            Optional<DeviceAuth.Device> device = auth.authenticate(token);
            synchronized (this) {
                if (authTimeout != null) {
                    authTimeout.cancel(false);
                }
            }
            if (device.isEmpty()) {
                close(CLOSE_UNAUTHORIZED, "Invalid token");
                return;
            }
            sendSnapshot();
        }

        private void authTimedOut() {
            boolean stillWaiting;
            synchronized (this) {
                stillWaiting = state == State.AWAITING_AUTH;
            }
            if (stillWaiting) {
                close(CLOSE_BAD_MESSAGE, "No auth message within " + AUTH_TIMEOUT.toSeconds() + " s");
            }
        }

        /**
         * Send the full state, then everything that happened after it.
         * Events that arrive while the state is read are held back, so the
         * snapshot always comes first and nothing is lost in between.
         */
        private void sendSnapshot() {
            long snapshotSeq;
            synchronized (this) {
                if (state == State.CLOSED) {
                    return;
                }
                state = State.SNAPSHOTTING;
                pending = new ArrayList<>();
                // Read the sequence before the state: an event at or below it
                // is already part of the state that follows.
                snapshotSeq = bus.lastSeq();
            }
            Object data;
            try {
                data = isPublic ? PublicLive.from(control) : control.getState();
            } catch (FxCall.TimeoutException ex) {
                close(CLOSE_SERVER_ERROR, "Quelea is not responding");
                return;
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Could not build remote v2 snapshot", ex);
                close(CLOSE_SERVER_ERROR, "Internal error");
                return;
            }
            synchronized (this) {
                if (state != State.SNAPSHOTTING) {
                    return;
                }
                send(Json.message("snapshot", snapshotSeq, data));
                for (Pending event : pending) {
                    if (event.seq() > snapshotSeq) {
                        send(event.json());
                    }
                }
                pending = null;
                state = State.READY;
            }
        }

        void deliver(long seq, String json, boolean publicType) {
            if (isPublic && !publicType) {
                return;
            }
            synchronized (this) {
                switch (state) {
                    case READY -> send(json);
                    case SNAPSHOTTING -> pending.add(new Pending(seq, json));
                    default -> {
                        // Not authenticated yet, or closed.
                    }
                }
            }
        }

        private void send(String json) {
            Session s = session;
            if (s != null && s.isOpen()) {
                s.sendText(json, Callback.NOOP);
            }
        }

        void close(int code, String reason) {
            Session s = session;
            closed();
            if (s != null && s.isOpen()) {
                s.close(code, reason, Callback.NOOP);
            }
        }

        private void closed() {
            clients.remove(this);
            synchronized (this) {
                state = State.CLOSED;
                pending = null;
                if (authTimeout != null) {
                    authTimeout.cancel(false);
                }
            }
        }
    }
}
