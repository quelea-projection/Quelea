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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * Check if the TurboDB data exchange program is in place.
 * <p>
 * @author Michael
 */
public class ZWTurboDBChecker {

    private Dialog warningDialog;

    /**
     * Determine if the turbo db data exchange exe is present. If not, show a
     * message about what to do.
     * <p>
     * @return true if the exe is present and ok, false otherwise.
     */
    public boolean runChecks() {
        if(QueleaProperties.getTurboDBExe().exists()) {
            return true;
        }
        else {
            warningDialog = new Dialog.Builder().create()
                    .setWarningIcon()
                    .setMessage(LabelGrabber.INSTANCE.getLabel("no.tdb.message").replace("$1", "\""+QueleaProperties.getQueleaUserHome().getAbsolutePath()+"\""))
                    .setTitle(LabelGrabber.INSTANCE.getLabel("no.tdb.heading"))
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
            return false;
        }
    }

}
