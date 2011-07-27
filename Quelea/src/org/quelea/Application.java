package org.quelea;

import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.StatusPanelGroup;

/**
 * A singleton class for grabbing application wide objects with ease such as the main window.
 * @author Michael
 */
public class Application {

    private static final Application INSTANCE = new Application();
    private MainWindow mainWindow;
    private LyricWindow lyricWindow;

    /**
     * Get the singleton instance.
     * @return the instance.
     */
    public static Application get() {
        return INSTANCE;
    }

    /**
     * Get the lyric window.
     * @return the lyric window.
     */
    public LyricWindow getLyricWindow() {
        return lyricWindow;
    }

    /**
     * Get the main window.
     * @return the main window.
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * Get the status panel group. Shortcut method but provided here for
     * convenience.
     * @return the status panel group.
     */
    public StatusPanelGroup getStatusGroup() {
        return mainWindow.getMainPanel().getStatusPanelGroup();
    }

    /**
     * Set the lyric window.
     * @param lyricWindow the lyric window.
     */
    public void setLyricWindow(LyricWindow lyricWindow) {
        this.lyricWindow = lyricWindow;
    }

    /**
     * Set the main window.
     * @param mainWindow the main window.
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

}
