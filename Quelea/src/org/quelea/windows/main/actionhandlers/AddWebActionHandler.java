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

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextInputDialog;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.windows.main.QueleaApp;

/**
 * The action handler responsible for letting the user add a websites to the
 * schedule.
 * <p>
 * @author Arvid
 */
public class AddWebActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent t) {
        TextInputDialog dialog = new TextInputDialog("http://");
        dialog.setTitle("Web page URL");
        dialog.setHeaderText("Enter web page URL");
        dialog.setContentText("Please enter the web page you would like to display:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            WebDisplayable displayable = new WebDisplayable(result.get());
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
    }

}
