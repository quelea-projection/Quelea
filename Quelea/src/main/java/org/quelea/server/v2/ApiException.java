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

/**
 * An error answered to an API client as
 * {@code {"error":{"code":"…","message":"…"}}} with an HTTP status.
 */
public final class ApiException extends RuntimeException {

    private final int status;
    private final String code;

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(400, "bad_request", message);
    }

    public static ApiException unauthorized() {
        return new ApiException(401, "unauthorized", "A valid token is required");
    }

    public static ApiException forbidden() {
        return new ApiException(403, "forbidden", "This device may not do that");
    }

    public static ApiException notFound(String message) {
        return new ApiException(404, "not_found", message);
    }

    public static ApiException methodNotAllowed(String method) {
        return new ApiException(405, "method_not_allowed", method + " is not allowed here");
    }

    public static ApiException unavailable(String message) {
        return new ApiException(503, "unavailable", message);
    }

    /**
     * @return the HTTP status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the machine-readable error code.
     */
    public String getCode() {
        return code;
    }
}
