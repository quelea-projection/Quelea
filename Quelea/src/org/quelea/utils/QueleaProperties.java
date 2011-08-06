package org.quelea.utils;

import org.quelea.bible.Bible;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the properties specific to Quelea.
 * @author Michael
 */
public final class QueleaProperties extends Properties {

    public static final Version VERSION = new Version("0.3");
    private static final QueleaProperties INSTANCE = new QueleaProperties();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Color ACTIVE_SELECTION = new Color(200, 255, 255);

    /**
     * Load the properties from the properties file.
     */
    private QueleaProperties() {
        try {
            if (!getPropFile().exists()) {
                getPropFile().createNewFile();
            }
            try (FileReader reader = new FileReader(getPropFile())) {
                load(reader);
            }
        }
        catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, "Couldn't load properties", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Get the properties file.
     * @return the properties file.
     */
    private File getPropFile() {
        return new File(getQueleaUserHome(), "quelea.properties");
    }

    /**
     * Save these properties to the file.
     */
    private void write() {
        try (FileWriter writer = new FileWriter(getPropFile())) {
            store(writer, "Auto save");
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't store properties", ex);
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
     * Get the Quelea home directory in the user's directory.
     * @return the Quelea home directory.
     */
    public static File getQueleaUserHome() {
        File ret = new File(new File(System.getProperty("user.home")), "My Quelea");
        if (!ret.exists()) {
            ret.mkdir();
        }
        return ret;
    }

    /**
     * Get the directory used for storing the bibles.
     * @return the bibles directory.
     */
    public File getBibleDir() {
        return new File(getProperty("bibles.dir", "bibles"));
    }

    /**
     * Get the extension used for quelea schedules.
     * @return the extension used for quelea schedules.
     */
    public String getScheduleExtension() {
        return getProperty("quelea.schedule.extension", "qsch");
    }

    /**
     * Get the extension used for quelea song packs.
     * @return the extension used for quelea song packs.
     */
    public String getSongPackExtension() {
        return getProperty("quelea.songpack.extension", "qsp");
    }

    /**
     * Get the number of the screen used for the control screen. This is the screen that the main Quelea operator window
     * will be displayed on.
     * @return the control screen number.
     */
    public int getControlScreen() {
        return Integer.parseInt(getProperty("control.screen", "0"));
    }

    /**
     * Set the control screen output.
     * @param screen the number of the screen to use for the output.
     */
    public void setControlScreen(int screen) {
        setProperty("control.screen", Integer.toString(screen));
        write();
    }

    /**
     * Get the number of the projector screen. This is the screen that the projected output will be displayed on.
     * @return the projector screen number.
     */
    public int getProjectorScreen() {
        return Integer.parseInt(getProperty("projector.screen", "1"));
    }

    /**
     * Set the control screen output.
     * @param screen the number of the screen to use for the output.
     */
    public void setProjectorScreen(int screen) {
        setProperty("projector.screen", Integer.toString(screen));
        write();
    }

    /**
     * Get the maximum number of characters allowed on any one line of projected text. If the line is longer than this,
     * it will be split up intelligently.
     * @return the maximum number of characters allowed on any one line of projected text.
     */
    public int getMaxChars() {
        return Integer.parseInt(getProperty("max.chars", "30"));
    }

    /**
     * Set the max chars value.
     * @param maxChars the maximum number of characters allowed on any one line of projected text.
     */
    public void setMaxChars(int maxChars) {
        setProperty("max.chars", Integer.toString(maxChars));
        write();
    }

    /**
     * Get the minimum number of lines that should be displayed on each page. This purely applies to font sizes, the
     * font will be adjusted so this amount of lines can fit on. This stops small lines becoming huge in the preview
     * window rather than displaying normally.
     * @return the minimum line count.
     */
    public int getMinLines() {
        return Integer.parseInt(getProperty("min.lines", "10"));
    }

    /**
     * Set the min lines value.
     * @param maxChars the minimum line count.
     */
    public void setMinLines(int minLines) {
        setProperty("min.lines", Integer.toString(minLines));
        write();
    }

    /**
     * Determine whether the single monitor warning should be shown (this warns the user they only have one monitor
     * installed.)
     * @return true if the warning should be shown, false otherwise.
     */
    public boolean showSingleMonitorWarning() {
        return Boolean.parseBoolean(getProperty("single.monitor.warning", "true"));
    }

    /**
     * Set whether the single monitor warning should be shown.
     * @param val true if the warning should be shown, false otherwise.
     */
    public void setSingleMonitorWarning(boolean val) {
        setProperty("single.monitor.warning", Boolean.toString(val));
        write();
    }

    /**
     * Get the URL to download Quelea.
     * @return the URL to download Quelea.
     */
    public String getDownloadLocation() {
        return getProperty("download.location", "http://code.google.com/p/quelea-projection/downloads/list");
    }

    /**
     * Get the URL to the Quelea website.
     * @return the URL to the Quelea website.
     */
    public String getWebsiteLocation() {
        return getProperty("website.location", "http://www.quelea.org/");
    }

    /**
     * Get the URL to the Quelea discussion forum.
     * @return the URL to the Quelea discussion forum.
     */
    public String getDiscussLocation() {
        return getProperty("discuss.location", "https://groups.google.com/group/quelea-discuss");
    }

    /**
     * Get the URL used for checking the latest version.
     * @return the URL used for checking the latest version.
     */
    public String getUpdateURL() {
        return getProperty("update.url", "http://code.google.com/p/quelea-projection/");
    }

    /**
     * Determine whether we should check for updates each time the program starts.
     * @return true if we should check for updates, false otherwise.
     */
    public boolean checkUpdate() {
        return Boolean.parseBoolean(getProperty("check.update", "true"));
    }

    /**
     * Set whether we should check for updates each time the program starts.
     * @param val true if we should check for updates, false otherwise.
     */
    public void setCheckUpdate(boolean val) {
        setProperty("check.update", Boolean.toString(val));
        write();
    }

    /**
     * Determine whether the first letter of all displayed lines should be a capital.
     * @return true if it should be a capital, false otherwise.
     */
    public boolean checkCapitalFirst() {
        return Boolean.parseBoolean(getProperty("capital.first", "true"));
    }

    /**
     * Set whether the first letter of all displayed lines should be a capital.
     * @param val true if it should be a capital, false otherwise.
     */
    public void setCapitalFirst(boolean val) {
        setProperty("capital.first", Boolean.toString(val));
        write();
    }

    /**
     * Determine whether the song info text should be displayed.
     * @return true if it should be a displayed, false otherwise.
     */
    public boolean checkDisplaySongInfoText() {
        return Boolean.parseBoolean(getProperty("display.songinfotext", "true"));
    }

    /**
     * Set whether the song info text should be displayed.
     * @param val true if it should be displayed, false otherwise.
     */
    public void setDisplaySongInfoText(boolean val) {
        setProperty("display.songinfotext", Boolean.toString(val));
        write();
    }

    /**
     * Get the default bible to use.
     * @return the default bible.
     */
    public String getDefaultBible() {
        return getProperty("default.bible");
    }

    /**
     * Set the default bible.
     * @param biblename the name of the default bible.
     */
    public void setDefaultBible(Bible bible) {
        setProperty("default.bible", bible.getName());
        write();
    }

    /**
     * Get the maximum number of verses allowed in any one bible reading. Too many will crash the program!
     * @return the maximum number of verses allowed.
     */
    public int getMaxVerses() {
        return Integer.parseInt(getProperty("max.verses", "100"));
    }

    /**
     * Set the maximum number of verses allowed in any one bible reading. Too many will crash the program!
     * @param val the maximum number of verses allowed.
     */
    public void setMaxVerses(int val) {
        setProperty("max.verses", Integer.toString(val));
        write();
    }

    /**
     * Get the colour used to signify an active list.
     * @return the colour used to signify an active list.
     */
    public Color getActiveSelectionColor() {
        return ACTIVE_SELECTION;
    }
}
