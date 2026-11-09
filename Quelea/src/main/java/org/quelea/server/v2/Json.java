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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.Map;

/**
 * JSON for the v2 API. Nulls are written out, so clients see
 * {@code "itemId":null} rather than a missing field.
 */
final class Json {

    private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    private Json() {
    }

    static String write(Object value) {
        return GSON.toJson(value);
    }

    /**
     * Write a message as sent over the WebSocket.
     */
    static String message(String type, long seq, Object data) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        msg.addProperty("seq", seq);
        msg.add("data", GSON.toJsonTree(data));
        return GSON.toJson(msg);
    }

    static String error(ApiException ex) {
        return GSON.toJson(Map.of("error", Map.of("code", ex.getCode(), "message", ex.getMessage())));
    }

    /**
     * Parse a request body as a JSON object.
     *
     * @throws ApiException 400 if it is not a JSON object.
     */
    static JsonObject object(String body) {
        if (body == null || body.isBlank()) {
            throw ApiException.badRequest("A JSON body is required");
        }
        try {
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ex) {
            throw ApiException.badRequest("The body is not a JSON object");
        }
    }

    /**
     * @return the field as an int.
     * @throws ApiException 400 if it is missing or not a number.
     */
    static int requireInt(JsonObject obj, String field) {
        try {
            if (obj.has(field) && !obj.get(field).isJsonNull()) {
                return obj.get(field).getAsInt();
            }
        } catch (RuntimeException ex) {
            // Falls through to the error below.
        }
        throw ApiException.badRequest("\"" + field + "\" must be a number");
    }

    /**
     * @return the field as a long.
     * @throws ApiException 400 if it is missing or not a number.
     */
    static long requireLong(JsonObject obj, String field) {
        try {
            if (obj.has(field) && !obj.get(field).isJsonNull()) {
                return obj.get(field).getAsLong();
            }
        } catch (RuntimeException ex) {
            // Falls through to the error below.
        }
        throw ApiException.badRequest("\"" + field + "\" must be a number");
    }

    /**
     * @return the field as a boolean.
     * @throws ApiException 400 if it is missing or not a boolean.
     */
    static boolean requireBoolean(JsonObject obj, String field) {
        Boolean value = optionalBoolean(obj, field);
        if (value == null) {
            throw ApiException.badRequest("\"" + field + "\" must be true or false");
        }
        return value;
    }

    /**
     * @return the field as a boolean, or null if it is missing.
     * @throws ApiException 400 if it is present but not a boolean.
     */
    static Boolean optionalBoolean(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        if (obj.get(field).isJsonPrimitive() && obj.get(field).getAsJsonPrimitive().isBoolean()) {
            return obj.get(field).getAsBoolean();
        }
        throw ApiException.badRequest("\"" + field + "\" must be true or false");
    }

    /**
     * @return the field as a string, or null if it is missing.
     */
    static String optionalString(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull() || !obj.get(field).isJsonPrimitive()) {
            return null;
        }
        return obj.get(field).getAsString();
    }
}
