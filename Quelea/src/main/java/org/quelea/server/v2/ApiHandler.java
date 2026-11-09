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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.quelea.services.utils.LoggerUtils;

/**
 * The REST part of the v2 API: routes requests under {@code /api/v2/},
 * checks the token and scope, and answers JSON.
 * <p>
 * Requests run on Jetty's threads. Everything that touches Quelea goes
 * through {@link QueleaControl}, which never blocks longer than its timeout.
 */
final class ApiHandler extends Handler.Abstract {

    static final String PREFIX = "/api/v2";
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private final QueleaControl control;
    private final DeviceAuth auth;
    private final List<Route> routes = new ArrayList<>();

    ApiHandler(QueleaControl control, DeviceAuth auth, RemoteApiServer.Config config) {
        this.control = control;
        this.auth = auth;
        Info info = new Info(2, config.queleaVersion(), false, false, config.publicEnabled());

        route("GET", "/info", null, call -> info);
        route("GET", "/state", Scope.VIEW, call -> control.getState());
        route("GET", "/schedule", Scope.VIEW, call -> control.getSchedule());
        route("GET", "/live/sections", Scope.VIEW, call -> control.getLiveSections());
        route("POST", "/live/next", Scope.CONTROL, call -> control.next());
        route("POST", "/live/prev", Scope.CONTROL, call -> control.previous());
        route("POST", "/live/item/next", Scope.CONTROL, call -> control.nextItem());
        route("POST", "/live/item/prev", Scope.CONTROL, call -> control.previousItem());
        route("POST", "/live/section", Scope.CONTROL,
                call -> control.gotoSection(Json.requireInt(call.body(), "index")));
        route("PUT", "/live/screen", Scope.CONTROL, call -> {
            JsonObject body = call.body();
            return control.setScreen(Json.optionalBoolean(body, "logo"), Json.optionalBoolean(body, "black"),
                    Json.optionalBoolean(body, "clear"));
        });
        route("PUT", "/recording", Scope.CONTROL,
                call -> control.setRecording(Json.requireBoolean(call.body(), "recording")));
        route("POST", "/schedule/items/" + ITEM_ID + "/live", Scope.CONTROL,
                call -> control.goLive(call.match().group(1)));

        // Schedule changes. ?ifRevision=N refuses the change with 409 if
        // someone else changed the schedule first.
        route("POST", "/schedule/items", Scope.CONTROL, call -> {
            JsonObject body = call.body();
            if (body.has("songId")) {
                return control.addSong(Json.requireLong(body, "songId"), call.ifRevision());
            }
            if (body.has("bible") && body.get("bible").isJsonObject()) {
                JsonObject bible = body.getAsJsonObject("bible");
                return control.addBible(Json.optionalString(bible, "translation"),
                        Json.optionalString(bible, "reference"), call.ifRevision());
            }
            throw ApiException.badRequest("Give \"songId\" or \"bible\": {\"translation\", \"reference\"}");
        });
        route("DELETE", "/schedule/items/" + ITEM_ID, Scope.CONTROL,
                call -> control.removeItem(call.match().group(1), call.ifRevision()));
        route("POST", "/schedule/items/" + ITEM_ID + "/move", Scope.CONTROL,
                call -> control.moveItem(call.match().group(1), Json.requireInt(call.body(), "to"), call.ifRevision()));
        route("GET", "/schedule/items/" + ITEM_ID + "/slides/([0-9]+)\\.png", Scope.VIEW,
                call -> new Binary(control.slideImage(call.match().group(1), Integer.parseInt(call.match().group(2))),
                        "image/png", false));

        // Songs, bibles and themes. Names go in query parameters, since
        // they may contain spaces and slashes.
        route("GET", "/songs", Scope.VIEW,
                call -> control.searchSongs(call.query("q"), call.intQuery("limit", 50, 1, 200)));
        route("GET", "/songs/([0-9]+)", Scope.VIEW,
                call -> control.getSong(Long.parseLong(call.match().group(1))));
        route("GET", "/bible/translations", Scope.VIEW, call -> control.bibleTranslations());
        route("GET", "/bible/books", Scope.VIEW, call -> control.bibleBooks(call.query("translation")));
        route("GET", "/bible/passage", Scope.VIEW,
                call -> control.biblePassage(call.query("translation"), call.query("ref")));
        route("GET", "/themes", Scope.VIEW, call -> control.themes());
        route("GET", "/themes/thumb\\.png", Scope.VIEW,
                call -> new Binary(control.themeThumbnail(call.query("name")), "image/png", true));
        route("PUT", "/live/theme", Scope.CONTROL,
                call -> control.setTheme(Json.optionalString(call.body(), "name")));
        if (config.publicEnabled()) {
            route("GET", "/public/live", null, call -> PublicLive.from(control));
        }
    }

    /**
     * Answered by {@code GET /api/v2/info}, without a token.
     *
     * @param api the API version.
     * @param quelea the Quelea version.
     * @param tls true if the TLS port is open.
     * @param pairing true if pairing is open right now.
     * @param publicEnabled true if the open endpoints under /public/ answer.
     */
    record Info(int api, String quelea, boolean tls, boolean pairing, boolean publicEnabled) {
    }

    /**
     * Schedule item ids: "it_12", or "it_12-2" for the second entry of the
     * same displayable.
     */
    private static final String ITEM_ID = "(it_[0-9]+(?:-[0-9]+)?)";

    /**
     * A non-JSON answer, such as an image.
     *
     * @param data the bytes.
     * @param contentType the media type.
     * @param cacheable true if clients may cache it for a minute.
     */
    record Binary(byte[] data, String contentType, boolean cacheable) {
    }

    @FunctionalInterface
    private interface Action {

        Object run(Call call);
    }

    private record Route(String method, Pattern path, Scope scope, Action action) {
    }

    /**
     * One request being handled.
     */
    private record Call(Request request, Matcher match) {

        JsonObject body() {
            try {
                return Json.object(Content.Source.asString(request, StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw ApiException.badRequest("Could not read the request body");
            }
        }

        String query(String name) {
            return Request.extractQueryParameters(request, StandardCharsets.UTF_8).getValue(name);
        }

        int intQuery(String name, int defaultValue, int min, int max) {
            String value = query(name);
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            try {
                return Math.max(min, Math.min(max, Integer.parseInt(value.trim())));
            } catch (NumberFormatException ex) {
                throw ApiException.badRequest("\"" + name + "\" must be a number");
            }
        }

        Long ifRevision() {
            String value = query("ifRevision");
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ex) {
                throw ApiException.badRequest("\"ifRevision\" must be a number");
            }
        }
    }

    private void route(String method, String path, Scope scope, Action action) {
        routes.add(new Route(method, Pattern.compile(path), scope, action));
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        String path = Request.getPathInContext(request);
        if (!path.startsWith(PREFIX + "/")) {
            return false;
        }
        int status = 200;
        String body;
        try {
            Object result = dispatch(request, path.substring(PREFIX.length()));
            if (result instanceof Binary binary) {
                response.setStatus(200);
                response.getHeaders().put(HttpHeader.CONTENT_TYPE, binary.contentType());
                response.getHeaders().put(HttpHeader.CACHE_CONTROL, binary.cacheable() ? "max-age=60" : "no-store");
                response.write(true, ByteBuffer.wrap(binary.data()), callback);
                return true;
            }
            body = Json.write(result);
        } catch (ApiException ex) {
            status = ex.getStatus();
            body = Json.error(ex);
        } catch (NoSuchItemException | NoSuchElementException ex) {
            ApiException notFound = ApiException.notFound(ex.getMessage());
            status = notFound.getStatus();
            body = Json.error(notFound);
        } catch (ScheduleConflictException ex) {
            ApiException conflict = new ApiException(409, "conflict", ex.getMessage());
            status = conflict.getStatus();
            body = Json.error(conflict);
        } catch (IllegalArgumentException ex) {
            ApiException bad = ApiException.badRequest(ex.getMessage());
            status = bad.getStatus();
            body = Json.error(bad);
        } catch (FxCall.TimeoutException ex) {
            ApiException unavailable = ApiException.unavailable("Quelea is not responding");
            status = unavailable.getStatus();
            body = Json.error(unavailable);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Remote v2 request failed: " + request.getMethod() + " " + path, ex);
            status = 500;
            body = Json.error(new ApiException(500, "internal", "Internal error"));
        }
        response.setStatus(status);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
        response.getHeaders().put(HttpHeader.CACHE_CONTROL, "no-store");
        Content.Sink.write(response, true, body, callback);
        return true;
    }

    private Object dispatch(Request request, String path) {
        boolean pathKnown = false;
        for (Route route : routes) {
            Matcher match = route.path().matcher(path);
            if (!match.matches()) {
                continue;
            }
            pathKnown = true;
            if (!route.method().equals(request.getMethod())) {
                continue;
            }
            if (route.scope() != null) {
                DeviceAuth.Device device = auth.authenticate(bearerToken(request))
                        .orElseThrow(ApiException::unauthorized);
                if (!device.can(route.scope())) {
                    throw ApiException.forbidden();
                }
            }
            return route.action().run(new Call(request, match));
        }
        if (pathKnown) {
            throw ApiException.methodNotAllowed(request.getMethod());
        }
        throw ApiException.notFound("No such endpoint: " + path);
    }

    private static String bearerToken(Request request) {
        String header = request.getHeaders().get(HttpHeader.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
