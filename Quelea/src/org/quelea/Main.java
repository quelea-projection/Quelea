package org.quelea;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.quelea.display.Song;
import org.quelea.display.SongSection;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Version;
import org.quelea.utils.VersionChecker;
import org.quelea.windows.library.LibrarySongPanel;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * The main class, sets everything in motion...
 * @author Michael
 */
public final class Main {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static MainWindow mainWindow;
    private static LyricWindow fullScreenWindow;
    private static SongDatabase database;

    /**
     * Don't instantiate me. I bite.
     */
    private Main() {
        throw new AssertionError();
    }

    /**
     * Go go go!
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        LOGGER.log(Level.INFO, "Number of displays: {0}", gds.length);

        int controlScreenProp = QueleaProperties.get().getControlScreen();
        final int controlScreen;
        int projectorScreen = QueleaProperties.get().getProjectorScreen();

        if (gds.length <= controlScreenProp) {
            controlScreen = 0;
        }
        else {
            controlScreen = controlScreenProp;
        }
        final boolean hidden;
        if (projectorScreen >= gds.length || projectorScreen < 0) {
            hidden = true;
        }
        else {
            hidden = false;
        }
        if (hidden) {
            LOGGER.log(Level.INFO, "Hiding projector display on monitor 0 (base 0!)");
            fullScreenWindow = new LyricWindow(gds[0].getDefaultConfiguration().getBounds());
        }
        else {
            LOGGER.log(Level.INFO, "Starting projector display on monitor {0} (base 0!)", projectorScreen);
            fullScreenWindow = new LyricWindow(gds[projectorScreen].getDefaultConfiguration().getBounds());
        }
        fullScreenWindow.toFront();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setLaf();
                try {
                    database = new SongDatabase();
                }
                catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "SQL excpetion - hopefully this is just because quelea is already running", ex);
                    JOptionPane.showMessageDialog(null, "It looks like you already have an instance of Quelea running, make sure you close all instances before running the program.", "Already running", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                mainWindow = new MainWindow();
                addDBSongs();

                addNewSongWindowListeners();
                addSongPanelListeners();
                addOptionListeners();
                addMenuListeners();

                mainWindow.setLocation((int) gds[controlScreen].getDefaultConfiguration().getBounds().getMinX() + 100, (int) gds[controlScreen].getDefaultConfiguration().getBounds().getMinY() + 100);
                mainWindow.setVisible(true);

                checkUpdate(false, false);
                showWarning(gds.length);
                mainWindow.getMainPanel().getLiveLyricsPanel().registerLyricCanvas(fullScreenWindow.getCanvas());
                mainWindow.getMainPanel().getLiveLyricsPanel().registerLyricWindow(fullScreenWindow);
                fullScreenWindow.setVisible(!hidden);
            }
        });
    }

    /**
     * Add the required action listeners to the menus.
     */
    private static void addMenuListeners() {
        mainWindow.getMainMenuBar().getHelpMenu().getUpdateCheck().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                checkUpdate(true, true);
            }
        });
    }

    /**
     * Add the required action listeners to the options window.
     */
    private static void addOptionListeners() {
        mainWindow.getOptionsWindow().getOKButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateDisplay();
                updateGeneral();
            }

            /**
             * Update the general part of the options form.
             */
            private void updateGeneral() {
                QueleaProperties props = QueleaProperties.get();
                boolean checkUpdate = mainWindow.getOptionsWindow().getGeneralPanel().getStartupUpdateCheckBox().isSelected();
                props.setCheckUpdate(checkUpdate);
                boolean checkCapital = mainWindow.getOptionsWindow().getGeneralPanel().getCapitalFirstCheckBox().isSelected();
                props.setCapitalFirst(checkCapital);
                int maxCharsPerLine = mainWindow.getOptionsWindow().getGeneralPanel().getMaxCharsSlider().getValue();
                props.setMaxChars(maxCharsPerLine);
                int minLines = mainWindow.getOptionsWindow().getGeneralPanel().getMinLinesSlider().getValue();
                props.setMinLines(minLines);
            }

            /**
             * Update the display part of the options form.
             */
            private void updateDisplay() {
                QueleaProperties props = QueleaProperties.get();
                int monitorDisplay = mainWindow.getOptionsWindow().getDisplayPanel().getControlDisplay();
                int projectorDisplay = mainWindow.getOptionsWindow().getDisplayPanel().getProjectorDisplay();
                props.setControlScreen(monitorDisplay);
                props.setProjectorScreen(projectorDisplay);

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                final GraphicsDevice[] gds = ge.getScreenDevices();
                if (projectorDisplay == -1) {
                    if (fullScreenWindow != null) {
                        fullScreenWindow.setVisible(false);
                    }
                }
                else {
                    if (fullScreenWindow == null) {
                        fullScreenWindow = new LyricWindow(gds[projectorDisplay].getDefaultConfiguration().getBounds());
                    }
                    fullScreenWindow.setVisible(true);
                    fullScreenWindow.setArea(gds[projectorDisplay].getDefaultConfiguration().getBounds());
                }
                mainWindow.setLocation((int) gds[monitorDisplay].getDefaultConfiguration().getBounds().getMinX() + 100, (int) gds[monitorDisplay].getDefaultConfiguration().getBounds().getMinY() + 100);
            }
        });
    }

    /**
     * Add the required action listeners to the song panel.
     */
    private static void addSongPanelListeners() {
        final LibrarySongPanel songPanel = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel();
        songPanel.getRemoveButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Song song = (Song) songPanel.getSongList().getSelectedValue();
                if (song == null) {
                    return;
                }
                int confirmResult = JOptionPane.showConfirmDialog(mainWindow, "Really remove " + song.getTitle() + " from the database? This action cannnot be undone.", "Confirm remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmResult == JOptionPane.NO_OPTION) {
                    return;
                }
                if (!database.removeSong(song)) {
                    JOptionPane.showMessageDialog(mainWindow, "There was an error removing the song from the database.", "Error", JOptionPane.ERROR_MESSAGE, null);
                }
                SortedListModel model = (SortedListModel) songPanel.getSongList().getModel();
                model.removeElement(song);
            }
        });
    }

    /**
     * Add the required action listeners to the buttons on the new song window.
     */
    private static void addNewSongWindowListeners() {
        final SongEntryWindow songEntryWindow = mainWindow.getNewSongWindow();
        songEntryWindow.getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                Song song = songEntryWindow.getSong();
                for (SongSection section : song.getSections()) {
                    section.setTheme(songEntryWindow.getTheme());
                }
                SortedListModel model = (SortedListModel) mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getModel();
                model.removeElement(song);
                if (!database.updateSong(song)) {
                    JOptionPane.showMessageDialog(mainWindow, "There was an error updating the song in the database.", "Error", JOptionPane.ERROR_MESSAGE, null);
                }
                songEntryWindow.setVisible(false);
                model.add(song);
            }
        });
    }

    /**
     * Add the songs to the GUI from the database.
     */
    private static void addDBSongs() {
        SortedListModel model = (SortedListModel) mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getModel();
        for (Song song : database.getSongs()) {
            model.add(song);
        }
    }

    /**
     * Check whether there's an update to Quelea, display a message if so.
     * @param showIfLatest true if the user should see a message even if they're
     * running the latest version.
     * @param showIfError true if the user should see a message if there is an
     * error.
     */
    private static void checkUpdate(boolean showIfLatest, boolean showIfError) {
        if (QueleaProperties.get().checkUpdate()) {
            Version latestVersion = new VersionChecker(QueleaProperties.get().getUpdateURL()).getLatestVersion();
            if(latestVersion==null) {
                if(showIfError) {
                    showUpdateError();
                }
                return;
            }
            Version curVersion = QueleaProperties.get().getVersion();
            LOGGER.log(Level.INFO, "Checked updates, current version is {0} and latest version is {1}",
                    new Object[]{curVersion.getVersionString(), latestVersion.getVersionString()});
            if (curVersion.compareTo(latestVersion) == -1) {
                if (Desktop.isDesktopSupported()) {
                    int result = JOptionPane.showConfirmDialog(mainWindow,
                            "There is a newer version of Quelea available (" + latestVersion.getVersionString() + "). "
                            + "Visit the web page to download it now?",
                            "Update available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDownloadLocation()));
                        }
                        catch(Exception ex) {
                            LOGGER.log(Level.WARNING, "Couldn't open browser", ex);
                            if (showIfError) {
                                showUpdateError();
                            }
                            return;
                        }
                    }
                }
                else {
                    JOptionPane.showMessageDialog(mainWindow,
                            "There is a newer version of Quelea available (" + latestVersion.getVersionString() + "). "
                            + "You can download it here: " + QueleaProperties.get().getDownloadLocation(),
                            "Update available", JOptionPane.INFORMATION_MESSAGE, null);
                }
            }
            else if(showIfLatest) {
                JOptionPane.showMessageDialog(mainWindow, "You are running the latest version of Quelea ("
                        + curVersion.getVersionString() + ").", "Already up-to-date!", JOptionPane.INFORMATION_MESSAGE, null);
            }
        }
    }

    /**
     * Show a message saying there was an error checking for updates.
     */
    private static void showUpdateError() {
        JOptionPane.showMessageDialog(mainWindow, "Sorry, there was an error checking for updates."
                + "Please check your internet connection then try again.", "Error", JOptionPane.ERROR_MESSAGE, null);
    }

    /**
     * If it's appropriate, show the warning about only having 1 monitor.
     * @param numMonitors the number of monitors.
     */
    private static void showWarning(int numMonitors) {
        if (numMonitors <= 1 && QueleaProperties.get().showSingleMonitorWarning()) {
            JOptionPane.showMessageDialog(mainWindow, "Looks like you've only got one monitor installed. "
                    + "This is fine if you're just using Quelea to prepare some schedules, but if you're "
                    + "using it in a live setting Quelea needs 2 monitors to work properly.", "Only one monitor",
                    JOptionPane.WARNING_MESSAGE, null);
        }
    }

    /**
     * Attempt to set the look and feel of the components.
     */
    private static void setLaf() {
        try {
            UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.INFO, "Couldn't set the look and feel to substance.", ex);
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }
}
