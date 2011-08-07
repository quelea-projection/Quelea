package org.quelea.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks for any updates to Quelea.
 * @author Michael
 */
public class UpdateChecker {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Component owner;

    public UpdateChecker(Component owner) {
        this.owner = owner;
    }

    /**
     * Check whether there's an update to Quelea, display a message if so.
     * @param showIfLatest true if the user should see a message even if they're running the latest version.
     * @param showIfError  true if the user should see a message if there is an error.
     * @param forceCheck   true if we should check even if the properties state otherwise.
     */
    public void checkUpdate(boolean showIfLatest, boolean showIfError, boolean forceCheck) {
        if(forceCheck || QueleaProperties.get().checkUpdate()) {
            Version latestVersion = new VersionChecker(QueleaProperties.get().getUpdateURL()).getLatestVersion();
            if(latestVersion == null) {
                if(showIfError) {
                    showUpdateError();
                }
                return;
            }
            Version curVersion = QueleaProperties.VERSION;
            LOGGER.log(Level.INFO, "Checked updates, current version is {0} and latest version is {1}",
                    new Object[]{curVersion.getVersionString(), latestVersion.getVersionString()});
            if(curVersion.compareTo(latestVersion) == -1) {
                if(Desktop.isDesktopSupported()) {
                    int result = JOptionPane.showConfirmDialog(owner,
                            "There is a newer version of Quelea available (" + latestVersion.getVersionString() + "). "
                                    + "Visit the web page to download it now?",
                            "Update available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                    if(result == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDownloadLocation()));
                        }
                        catch(URISyntaxException | IOException ex) {
                            LOGGER.log(Level.WARNING, "Couldn't open browser", ex);
                            if(showIfError) {
                                showUpdateError();
                            }
                            return;
       
                        }
                    }
                }
                else {
                    JOptionPane.showMessageDialog(owner,
                            "There is a newer version of Quelea available (" + latestVersion.getVersionString() + "). "
                                    + "You can download it here: " + QueleaProperties.get().getDownloadLocation(),
                            "Update available", JOptionPane.INFORMATION_MESSAGE, null);
                }
            }
            else if(showIfLatest) {
                JOptionPane.showMessageDialog(owner, "You are running the latest version of Quelea ("
                        + curVersion.getVersionString() + ").", "Already up-to-date!", JOptionPane.INFORMATION_MESSAGE, null);
            }
        }
    }

    /**
     * Show a message saying there was an error checking for updates.
     */
    private void showUpdateError() {
        JOptionPane.showMessageDialog(owner, "Sorry, there was an error checking for updates."
                + "Please check your internet connection then try again.", "Error", JOptionPane.ERROR_MESSAGE, null);
    }

}
