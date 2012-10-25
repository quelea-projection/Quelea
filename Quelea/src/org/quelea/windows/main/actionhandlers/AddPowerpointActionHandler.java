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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.QueleaApp;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * The action listener for adding a powerpoint presentation.
 * @author Michael
 */
public class AddPowerpointActionHandler implements EventHandler<ActionEvent> {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Add the presentation
     * @param e the event.
     */
    @Override
    public void handle(ActionEvent t) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.POWERPOINT);
        final File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
        if(file != null) {
            new Thread() {

                private StatusPanel panel;
                private boolean halt;

                @Override
                public void run() {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            panel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("adding.presentation.status"));
                            panel.getProgressBar().setProgress(-1);
                            panel.getCancelButton().setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent t) {
                                    panel.done();
                                    halt = true;
                                }
                            });
                        }
                    });
                    try {
                        final PresentationDisplayable displayable = new PresentationDisplayable(file);
                        if(!halt) {
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
                                }
                            });
                        }
                    }
                    catch(IOException ex) {
                        if(!halt) {
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("adding.presentation.error.title"), LabelGrabber.INSTANCE.getLabel("adding.presentation.error.message"));
                                }
                            });
                        }
                    }
                    catch (RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't import presentation", ex);
                    }
                    while(panel == null) {
                        Utils.sleep(1000); //Quick bodge but hey, it works
                    }
                    panel.done();
                }
            }.start();
        }
    }
}