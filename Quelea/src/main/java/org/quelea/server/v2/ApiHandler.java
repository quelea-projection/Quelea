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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    ApiHandler(QueleaControl control, DeviceAuth auth, PairingService pairing, RemoteApiServer.Config config) {
        this.control = control;
        this.auth = auth;

        route("GET", "/info", null, call -> new Info(2, config.queleaVersion(), false,
                pairing != null && pairing.isOpen(), config.publicEnabled()));
        route("POST", "/pair", null, call -> pair(pairing, call.body()));
        route("DELETE", "/devices/me", Scope.VIEW, call -> {
            auth.revoke(call.device().id());
            return Map.of("revoked", call.device().id());
        });
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
        // The live video, audio file or timer.
        route("POST", "/live/media/play", Scope.CONTROL, call -> control.playMedia());
        route("POST", "/live/media/pause", Scope.CONTROL, call -> control.pauseMedia());
        route("POST", "/live/media/stop", Scope.CONTROL, call -> control.stopMedia());
        route("PUT", "/live/media", Scope.CONTROL, call -> {
            JsonObject body = call.body();
            return control.setMedia(Json.optionalDouble(body, "position"), Json.optionalBoolean(body, "loop"));
        });
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
            if (body.has("image")) {
                return control.addLibraryFile(MediaLibrary.Kind.IMAGE, Json.optionalString(body, "image"),
                        call.ifRevision());
            }
            if (body.has("video")) {
                return control.addLibraryFile(MediaLibrary.Kind.VIDEO, Json.optionalString(body, "video"),
                        call.ifRevision());
            }
            if (body.has("document")) {
                return control.addLibraryFile(MediaLibrary.Kind.DOCUMENT, Json.optionalString(body, "document"),
                        call.ifRevision());
            }
            if (body.has("url")) {
                return control.addWebPage(Json.optionalString(body, "url"), call.ifRevision());
            }
            throw ApiException.badRequest("Give \"songId\", \"bible\": {\"translation\", \"reference\"}, "
                    + "\"image\", \"video\", \"document\" or \"url\"");
        });
        route("DELETE", "/schedule/items/" + ITEM_ID, Scope.CONTROL,
                call -> control.removeItem(call.match().group(1), call.ifRevision()));
        route("POST", "/schedule/items/" + ITEM_ID + "/move", Scope.CONTROL,
                call -> control.moveItem(call.match().group(1), Json.requireInt(call.body(), "to"), call.ifRevision()));
        route("POST", "/schedule/items/" + ITEM_ID + "/transpose", Scope.CONTROL,
                call -> control.transpose(call.match().group(1), Json.requireInt(call.body(), "semitones"),
                        call.ifRevision()));
        route("GET", "/schedule/items/" + ITEM_ID + "/slides/([0-9]+)\\.png", Scope.VIEW,
                call -> new Binary(control.slideImage(call.match().group(1), Integer.parseInt(call.match().group(2))),
                        "image/png", false));

        // Songs, bibles and themes. Names go in query parameters, since
        // they may contain spaces and slashes.
        route("GET", "/songs", Scope.VIEW,
                call -> control.searchSongs(call.query("q"), call.intQuery("limit", 50, 1, 200)));
        route("POST", "/songs", Scope.CONTROL, call -> {
            JsonObject body = call.body();
            return control.createSong(new Catalog.NewSong(Json.optionalString(body, "title"),
                    Json.optionalString(body, "author"), Json.optionalString(body, "lyrics"),
                    Json.optionalString(body, "ccli"), Json.optionalString(body, "copyright"),
                    Json.optionalString(body, "key")));
        });
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
        // The image and video libraries. Names go in query parameters.
        route("GET", "/library/(images|videos|documents)", Scope.VIEW,
                call -> control.libraryFiles(MediaLibrary.Kind.fromPath(call.match().group(1))));
        route("GET", "/library/(images|videos|documents)/thumb\\.png", Scope.VIEW,
                call -> new Binary(control.libraryThumbnail(MediaLibrary.Kind.fromPath(call.match().group(1)),
                        call.query("name")), "image/png", true));
        // An upload is the file itself as the body: PUT /library/images?name=photo.jpg
        route("PUT", "/library/(images|videos|documents)", Scope.CONTROL, call -> {
            try (InputStream data = Content.Source.asInputStream(call.request())) {
                return control.uploadLibraryFile(MediaLibrary.Kind.fromPath(call.match().group(1)),
                        call.query("name"), call.request().getLength(), data);
            } catch (IOException ex) {
                throw ApiException.badRequest("Could not read the upload: " + ex.getMessage());
            }
        });

        // Notices scrolling across the screen.
        route("GET", "/notices", Scope.VIEW, call -> control.notices());
        route("POST", "/notices", Scope.CONTROL, call -> {
            JsonObject body = call.body();
            Integer times = Json.optionalInt(body, "times");
            return control.addNotice(new Notices.Request(Json.optionalString(body, "text"),
                    times == null ? 1 : times, Json.optionalString(body, "color")));
        });
        route("DELETE", "/notices/(nt_[0-9]+)", Scope.CONTROL,
                call -> control.removeNotice(call.match().group(1)));
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
    private record Call(Request request, Matcher match, DeviceAuth.Device device) {

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
        } catch (PairingService.PairingException ex) {
            ApiException failed = pairingError(ex);
            status = failed.getStatus();
            body = Json.error(failed);
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
            DeviceAuth.Device device = null;
            if (route.scope() != null) {
                device = auth.authenticate(bearerToken(request))
                        .orElseThrow(ApiException::unauthorized);
                if (!device.can(route.scope())) {
                    throw ApiException.forbidden();
                }
            }
            return route.action().run(new Call(request, match, device));
        }
        if (pathKnown) {
            throw ApiException.methodNotAllowed(request.getMethod());
        }
        throw ApiException.notFound("No such endpoint: " + path);
    }

    /**
     * {"code":…} pairs with the code shown in Quelea, {"password":…} with
     * the remote control password. "name" and "platform" describe the device.
     */
    private static DeviceRegistry.Paired pair(PairingService pairing, JsonObject body) {
        if (pairing == null) {
            throw new PairingService.PairingException(PairingService.Failure.CLOSED, "Pairing is not available");
        }
        String name = Json.optionalString(body, "name");
        String platform = Json.optionalString(body, "platform");
        if (name == null || name.isBlank()) {
            throw ApiException.badRequest("\"name\" is required");
        }
        String code = Json.optionalString(body, "code");
        if (code != null) {
            return pairing.pairWithCode(code, name, platform);
        }
        String password = Json.optionalString(body, "password");
        if (password != null) {
            return pairing.pairWithPassword(password, name, platform);
        }
        throw ApiException.badRequest("Give \"code\" or \"password\"");
    }

    private static ApiException pairingError(PairingService.PairingException ex) {
        return switch (ex.getFailure()) {
            case CLOSED -> new ApiException(403, "pairing_closed", ex.getMessage());
            case WRONG -> new ApiException(401, "wrong_secret", ex.getMessage());
            case LOCKED -> new ApiException(429, "locked", ex.getMessage());
            case PASSWORD_DISABLED -> new ApiException(403, "password_disabled", ex.getMessage());
        };
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
