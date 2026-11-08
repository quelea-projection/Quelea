/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.main.actionhandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.search.UniversalSearchWindow;

/**
 * Open the universal search box.
 * <p/>
 * @author Arvid
 */
public class UniversalSearchActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        UniversalSearchWindow searchWindow = mainWindow.getUniversalSearchWindow();
        // The menu bar on a Mac also reacts while another window, like a dialog, is in front
        if (mainWindow.isFocused() || searchWindow.isShowing()) {
            searchWindow.open();
        }
    }
}
