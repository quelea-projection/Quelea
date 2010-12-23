package org.quelea.utils;

import java.io.BufferedReader;
import java.io.IOException;
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
     * Create a new version checker that checks the specified url for the
     * version number.
     */
    public VersionChecker(String url) {
        this.urlStr = url;
    }

    /**
     * Get the latest version, or null if an error occured and it couldn't be
     * found.
     * @return the latest version.
     */
    public Version getLatestVersion() {
        LOGGER.log(Level.INFO, "Checking for an updated version...");
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            try {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return extractVersion(content.toString());
            }
            finally {
                reader.close();
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get version", ex);
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
            return new Version(content.substring(startIndex + PREFIX.length(), content.indexOf('<', startIndex)).trim());
        }
        catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Couldn't extract version from string: " + content, ex);
            return null;
        }
    }
}
