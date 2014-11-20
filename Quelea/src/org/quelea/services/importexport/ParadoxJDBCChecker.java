/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.importexport;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * Check if the Paradox JDBC driver is in place.
 * <p>
 * @author Michael
 */
public class ParadoxJDBCChecker {

    private Dialog warningDialog;

    /**
     * Determine if the paradox db driver is present. If not, show a message
     * about what to do.
     */
    public void runChecks() {
        if (!isOk()) {
            warningDialog = new Dialog.Builder().create()
                    .setWarningIcon()
                    .setMessage(LabelGrabber.INSTANCE.getLabel("no.pdb.message").replace("$1", "\"" + QueleaProperties.getQueleaUserHome().getAbsolutePath() + "\""))
                    .setTitle(LabelGrabber.INSTANCE.getLabel("no.pdb.heading"))
                    .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            warningDialog.hide();
                        }
                    })
                    .setOwner(QueleaApp.get().getMainWindow())
                    .build();
            warningDialog.centerOnScreen();
            warningDialog.showAndWait();
        }
    }

    /**
     * Determine if the JDBC library is in place and working.
     * @return true if it's in place and working, false otherwise.
     */
    private boolean isOk() {
        try {
            File jarFile = new File(QueleaProperties.getQueleaUserHome().getAbsolutePath(), "Paradox_JDBC41.jar");
            URL u = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/");
            String classname = "com.hxtt.sql.paradox.ParadoxDriver";
            URLClassLoader ucl = new URLClassLoader(new URL[]{u});
            Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
