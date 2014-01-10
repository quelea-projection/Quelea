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
package org.quelea.windows.main.actionhandlers;

import javafx.event.EventHandler;
import org.quelea.windows.main.QueleaApp;

/**
 * Action handler for displaying the dialog to view the contents of bibles.
 * @author Michael
 */
public class ViewBibleActionHandler implements EventHandler<javafx.event.ActionEvent> {

    @Override
    public void handle(javafx.event.ActionEvent t) {
        QueleaApp.get().getMainWindow().getBibleBrowseDialog().show();
    }
    
}
