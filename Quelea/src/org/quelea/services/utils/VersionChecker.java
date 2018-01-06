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
package org.quelea.services.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks the latest version number online to see if an upgrade is required.
 * @author Michael
 */
public class VersionChecker {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String PREFIX = "Latest version:";
    private final String urlStr;

    /**
     * Create a new version checker that checks the specified url for the version number.
     * @param url the URL to use to check the version.
     */
    public VersionChecker(String url) {
        this.urlStr = url;
    }

    /**
     * Get the latest version, or null if an error occured and it couldn't be found.
     * @return the latest version.
     */
    public Version getLatestVersion() {
        LOGGER.log(Level.INFO, "Checking for an updated version...");
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return extractVersion(content.toString());
            }
        }
        catch(Exception ex) {
            LOGGER.log(Level.INFO, "Couldn't get version", ex);
            return null;
        }
    }

    /**
     * Extract a version from the specified content.
     * @param content the content to use to get the specified version.
     * @return the version.
     */
    private Version extractVersion(String content) {
        try {
            int startIndex = content.indexOf(PREFIX);
            return new Version(content.substring(startIndex + PREFIX.length(), content.indexOf('<', startIndex)).trim(), "");
        }
        catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Couldn't extract version from string: " + content, ex);
            return null;
        }
    }
}
