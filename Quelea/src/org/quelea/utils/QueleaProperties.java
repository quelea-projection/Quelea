package org.quelea.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the properties specific to Quelea.
 * @author Michael
 */
public final class QueleaProperties extends Properties {

    public static final String PROP_FILE_LOCATION = "quelea.properties";
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final QueleaProperties INSTANCE = new QueleaProperties();

    /**
     * Load the properties from the properties file.
     */
    private QueleaProperties() {
        try {
            FileReader reader = new FileReader(PROP_FILE_LOCATION);
            try {
                load(reader);
            }
            finally {
                reader.close();
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't load properties", ex);
        }
    }

    /**
     * Get the singleton instance of this class.
     * @return the instance.
     */
    public static QueleaProperties get() {
        return INSTANCE;
    }

    /**
     * Get the current version number.
     * @return the current version number.
     */
    public String getVersion() {
        return getProperty("quelea.version", "");
    }

    /**
     * Get the extension used for quelea schedules.
     * @return the extension used for quelea schedules.
     */
    public String getScheduleExtension() {
        return getProperty("quelea.schedule.extension", "qsch");
    }

    /**
     * Get the number of the screen used for the control screen. This is the
     * screen that the main Quelea operator window will be displayed on.
     * @return the control screen number.
     */
    public int getControlScreen() {
        return Integer.parseInt(getProperty("control.screen", "0"));
    }

    /**
     * Get the number of the projector screen. This is the screen that the
     * projected output will be displayed on.
     * @return the projector screen number.
     */
    public int getProjectorScreen() {
        return Integer.parseInt(getProperty("projector.screen", "1"));
    }

}
