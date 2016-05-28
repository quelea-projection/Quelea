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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * The action listener for adding a PDF presentation.
 * <p>
 * @author Arvid, based on AddPowerpointActionHandler
 */
public class AddPdfActionHandler implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Add the PDF
     * <p>
     * @param t the event.
     */
    @Override
    public void handle(ActionEvent t) {
        FileChooser chooser = new FileChooser();
        if (QueleaProperties.get().getLastDirectory() != null) {
            chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
        }
        chooser.getExtensionFilters().add(FileFilters.PDF_GENERIC);
        final List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());
        addPDF(files);
    }

    public void addPDF(List<File> files) {
        if(files != null) {
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
                        for(File file : files) {
                            final PdfDisplayable displayable = new PdfDisplayable(file);
                            if(!halt) {
                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        QueleaProperties.get().setLastDirectory(file.getParentFile());
                                        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
                                    }
                                });
                            }
                        }
                    }
                    catch(IOException ex) {
                        System.err.println("IO " + ex);
                        if(!halt) {
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    Dialog.showError(LabelGrabber.INSTANCE.getLabel("adding.presentation.error.title"), LabelGrabber.INSTANCE.getLabel("adding.presentation.error.message"));
                                }
                            });
                        }
                    }
                    catch(RuntimeException ex) {
                        System.err.println("RE " + ex);
                        LOGGER.log(Level.WARNING, "Couldn't import presentation", ex);
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                Dialog.showError(LabelGrabber.INSTANCE.getLabel("adding.presentation.error.title"), LabelGrabber.INSTANCE.getLabel("adding.presentation.error.message"));
                            }
                        });
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
