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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;

/**
 * Checks for any updates to Quelea.
 * @author Michael
 */
public class UpdateChecker {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Check whether there's an update to Quelea, display a message if so.
     * @param showIfLatest true if the user should see a message even if they're running the latest version.
     * @param showIfError  true if the user should see a message if there is an error.
     * @param forceCheck   true if we should check even if the properties state otherwise.
     */
    public void checkUpdate(boolean showIfLatest, final boolean showIfError, boolean forceCheck) {
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
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("newer.version.available.title"), LabelGrabber.INSTANCE.getLabel("newer.version.available")+" (" + latestVersion.getVersionString() + "). "
                                    + LabelGrabber.INSTANCE.getLabel("visit.webpage.now")).addYesButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
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
                    }).addNoButton(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            //Nothing needed
                        }
                    }).build().showAndWait();
                    
                }
                else {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("newer.version.available.title"), LabelGrabber.INSTANCE.getLabel("newer.version.available")+" (" + latestVersion.getVersionString() + "). "
                                    + LabelGrabber.INSTANCE.getLabel("download.manual.update")+": " + QueleaProperties.get().getDownloadLocation());
                }
            }
            else if(showIfLatest) {
                Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("no.newer.version.available.title"), LabelGrabber.INSTANCE.getLabel("no.newer.version.available")+" ("
                        + curVersion.getVersionString() + ").");
            }
        }
    }

    /**
     * Show a message saying there was an error checking for updates.
     */
    private void showUpdateError() {
        Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.checking.updates.title"),
                LabelGrabber.INSTANCE.getLabel("error.checking.updates.text"));
    }

}
