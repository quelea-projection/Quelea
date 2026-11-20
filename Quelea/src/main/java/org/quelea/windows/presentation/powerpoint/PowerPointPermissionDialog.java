/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;

/**
 * Explains a missing macOS permission (Automation or Accessibility) and offers
 * to open the right page of System Settings. macOS only asks once; after "Don't
 * Allow" the switch has to be turned on there by hand. Shown at most once per
 * kind of problem per run, so a clicker press per second does not pile up
 * dialogs.
 */
public final class PowerPointPermissionDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String AUTOMATION_URL = "x-apple.systempreferences:com.apple.preference.security?Privacy_Automation";
    private static final String ACCESSIBILITY_URL = "x-apple.systempreferences:com.apple.preference.security?Privacy_Accessibility";
    private static final Set<String> SHOWN = new HashSet<>();

    private PowerPointPermissionDialog() {
    }

    /**
     * Show the explanation for this problem, unless it was shown already.
     * Must be called on the FX thread.
     *
     * @param ex the failure.
     */
    public static void show(PowerPointException ex) {
        if (!ex.isPermissionProblem()) {
            return;
        }
        String messageKey;
        String url;
        switch (ex.getKind()) {
            case NOT_AUTHORIZED_SYSTEM_EVENTS:
                messageKey = "pp.permission.system.events.message";
                url = AUTOMATION_URL;
                break;
            case NOT_ACCESSIBLE:
                messageKey = ex.getCode() == PowerPointException.CODE_SHOW_OFF_SCREEN
                        ? "pp.permission.screen.message" : "pp.permission.accessibility.message";
                url = ACCESSIBILITY_URL;
                break;
            default:
                messageKey = "pp.permission.automation.message";
                url = AUTOMATION_URL;
                break;
        }
        if (!SHOWN.add(messageKey)) {
            return;
        }
        LabelGrabber labels = LabelGrabber.INSTANCE;
        Dialog.buildConfirmation(labels.getLabel("pp.permission.title"), labels.getLabel(messageKey))
                .setWarningIcon()
                .addLabelledButton(labels.getLabel("pp.permission.open.settings"), e -> openSettings(url))
                .addLabelledButton(labels.getLabel("close.button"), null)
                .build()
                .show();
    }

    private static void openSettings(String url) {
        try {
            new ProcessBuilder("/usr/bin/open", url).start();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not open System Settings", ex);
        }
    }
}
