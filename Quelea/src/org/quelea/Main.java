package org.quelea;

import com.inet.jortho.SpellChecker;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.quelea.bible.BibleManager;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.UpdateChecker;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;

/**
 * The main class, sets everything in motion...
 * @author Michael
 */
public final class Main {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static MainWindow mainWindow;
    private static LyricWindow fullScreenWindow;

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
        Application.get().setLyricWindow(fullScreenWindow);
        fullScreenWindow.toFront();

        LOGGER.log(Level.INFO, "Loading bibles");
        BibleManager.get(); //Pre-load bibles
        LOGGER.log(Level.INFO, "Loading bibles done");

        LOGGER.log(Level.INFO, "Registering dictionary");
        try {
            SpellChecker.registerDictionaries(new File("dictionaries").toURI().toURL(), "en", "en");
            SpellChecker.getOptions().setLanguageDisableVisible(false);
            SpellChecker.getOptions().setCaseSensitive(false);
        }
        catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't load dictionaries", ex);
        }
        LOGGER.log(Level.INFO, "Registered dictionary");

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setLaf();
                if (SongDatabase.get().errorOccurred()) {
                    JOptionPane.showMessageDialog(null, "It looks like you already have an instance of Quelea running, make sure you close all instances before running the program.", "Already running", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                mainWindow = new MainWindow();
                Application.get().setMainWindow(mainWindow);

                Utils.centreOnMonitor(mainWindow, controlScreen);
                mainWindow.setVisible(true);
                mainWindow.toFront();

                new UpdateChecker(mainWindow).checkUpdate(false, false, false);
                showWarning(gds.length);
                mainWindow.getMainPanel().getLiveLyricsPanel().registerLyricCanvas(fullScreenWindow.getCanvas());
                mainWindow.getMainPanel().getLiveLyricsPanel().registerLyricWindow(fullScreenWindow);
                fullScreenWindow.setVisible(!hidden);
            }
        });

    }

    /**
     * Attempt to set the look and feel of the components.
     */
    private static void setLaf() {
        try {
            UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
        }
        catch (Exception ex) {
            LOGGER.log(Level.INFO, "Couldn't set the look and feel to substance.", ex);
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
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
}
