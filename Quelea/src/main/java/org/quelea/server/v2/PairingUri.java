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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The address in the pairing QR code, for example
 * {@code quelea://pair?h=192.168.1.20&p=1113&c=7F3K9Q&n=church-pc.local}.
 * The app connects to {@code h}, and looks up {@code n} again if the IP
 * address has changed since.
 */
public final class PairingUri {

    private PairingUri() {
    }

    /**
     * @param host the IP address of this computer.
     * @param port the v2 port.
     * @param code the pairing code.
     * @param hostName this computer's name, or null.
     * @return the pairing URI.
     */
    public static String build(String host, int port, String code, String hostName) {
        StringBuilder uri = new StringBuilder("quelea://pair?h=").append(encode(host))
                .append("&p=").append(port)
                .append("&c=").append(encode(code));
        String local = localName(hostName);
        if (local != null) {
            uri.append("&n=").append(encode(local));
        }
        return uri.toString();
    }

    /**
     * @param hostName a host name such as "church-pc" or "church-pc.local".
     * @return the name with ".local" for mDNS, or null if there is no usable
     * name.
     */
    static String localName(String hostName) {
        if (hostName == null || hostName.isBlank() || hostName.equalsIgnoreCase("localhost")
                || Character.isDigit(hostName.charAt(0)) && hostName.matches("[0-9.]+")) {
            return null;
        }
        String name = hostName.strip();
        if (name.toLowerCase().endsWith(".local")) {
            return name;
        }
        int dot = name.indexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name) + ".local";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
