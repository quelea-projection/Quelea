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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Web addresses that remotes add to the schedule.
 */
final class WebAddress {

    private WebAddress() {
    }

    /**
     * Check a web address from a remote, as Quelea's add website dialog would
     * take it, but only http and https: file: and similar addresses would
     * let a remote put the computer's own files on the screen.
     *
     * @param url the address; "example.com" means "http://example.com".
     * @return the address to load.
     * @throws IllegalArgumentException if it is not a web address.
     */
    static String check(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("\"url\" is required");
        }
        String trimmed = url.strip();
        if (trimmed.length() > 2000) {
            throw new IllegalArgumentException("The web address is too long");
        }
        try {
            URI uri = new URI(trimmed);
            // "church-pc:8080/slides" is a host and port, not a scheme.
            if (uri.getScheme() == null || trimmed.matches("[^:/]+:\\d+(/.*)?")) {
                uri = new URI("http://" + trimmed);
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
                throw new IllegalArgumentException("Only http and https web addresses can be added");
            }
            return uri.toString();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("\"" + trimmed + "\" is not a web address");
        }
    }
}
