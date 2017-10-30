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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.javafx.dialog.Dialog;
import org.quelea.data.SaveCallback;
import org.quelea.data.Schedule;
import org.quelea.data.ScheduleSaver;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SceneInfo;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.presentation.PowerPointHandler;

/**
 * The exit action listener - called when the user requests they wish to exit
 * Quelea.
 * <p/>
 * @author Michael
 */
public class ExitActionHandler implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean cancel = false;

    /**
     * Call this method when the event is fired.
     */
    @Override
    public void handle(ActionEvent t) {
        exit(t);
    }

    private boolean block = false;

    /**
     * Process the necessary logic to cleanly exit from Quelea.
     *
     * @param t the event that caused the exit.
     */
    public void exit(Event t) {
        LOGGER.log(Level.INFO, "exit() called");
        block = false;
        MainWindow mainWindow = QueleaApp.get().getMainWindow();
        t.consume();
        Schedule schedule = mainWindow.getMainPanel().getSchedulePanel().getScheduleList().getSchedule();
        if (!schedule.isEmpty() && schedule.isModified()) {
            cancel = true;
            Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("save.before.exit.title"), LabelGrabber.INSTANCE.getLabel("save.before.exit.text")).addYesButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //Save schedule
                    block = true;
                    new ScheduleSaver().saveSchedule(false, new SaveCallback() {
                        @Override
                        public void saved(boolean success) {
                            cancel = !success;
                            block = false;
                        }
                    });
                }
            }).addNoButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    cancel = false;
                }
            }).addCancelButton(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    //No need to do anything
                }
            }).build();
            d.showAndWait();
            while (block) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    //Meh.
                }
            }
            if (cancel) {
                return; //Don't exit
            }
        }
        LOGGER.log(Level.INFO, "Saving window position...");
        QueleaProperties.get().setSceneInfo(new SceneInfo(mainWindow.getX(), mainWindow.getY(), mainWindow.getWidth(), mainWindow.getHeight(), mainWindow.isMaximized()));
        QueleaProperties.get().setMainDivPos(mainWindow.getMainPanel().getMainDivPos());
        QueleaProperties.get().setPrevLiveDivPos(mainWindow.getMainPanel().getPrevLiveDivPos());
        QueleaProperties.get().setCanvasDivPos(mainWindow.getMainPanel().getLivePanel().getLyricsPanel().getSplitPane().getDividerPositions()[0]);
        QueleaProperties.get().setLibraryDivPos(mainWindow.getMainPanel().getLibraryDivPos());
        LOGGER.log(Level.INFO, "Hiding main window...");
        mainWindow.hide();
        LOGGER.log(Level.INFO, "Cleaning up displayables before exiting..");
        for (Object obj : mainWindow.getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get()) {
            Displayable d = (Displayable) obj;
            if (d != null) {
                LOGGER.log(Level.INFO, "Cleaning up {0}", d.getClass());
                d.dispose();
            }
        }

        LOGGER.log(Level.INFO, "Try to close OOfice if opened");
        OOUtils.closeOOApp();
        if (QueleaApp.get().getMobileLyricsServer() != null) {
            LOGGER.log(Level.INFO, "Stopping mobile lyrics server");
            QueleaApp.get().getMobileLyricsServer().stop();
        }
        if (QueleaApp.get().getRemoteControlServer() != null) {
            LOGGER.log(Level.INFO, "Stopping remote control server");
            QueleaApp.get().getRemoteControlServer().stop();
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable() instanceof PresentationDisplayable) {
            LOGGER.log(Level.INFO, "Closing open PowerPoint presentations");
            PowerPointHandler.closePresentation();
        }
        
        LOGGER.log(Level.INFO, "Checking if Quelea currently is recording audio");
        MainToolbar toolbar = mainWindow.getMainToolbar();
        RecordingsHandler recHandler = toolbar.getRecordButtonHandler().getRecordingsHandler();
        if (toolbar.getRecordButtonHandler() != null && recHandler != null) {
            if (recHandler.getIsRecording()) {
                block = true;
                Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("save.recording.before.exit.title"), LabelGrabber.INSTANCE.getLabel("save.recording.before.exit.message"))
                        .addYesButton((ActionEvent t1) -> {
                            toolbar.stopRecording();
                        }).addNoButton((ActionEvent t1) -> {
                            System.exit(0);
                        }).build();
                d.setOnCloseRequest((WindowEvent we) -> {
                    System.exit(0);
                });
                Thread thread = new Thread(() -> {
                    while (block) {
                        try {
                            Thread.sleep(500);
                            if (recHandler.getFinishedSaving()) {
                                Platform.runLater(() -> {
                                    d.close();
                                });
                                if (QueleaProperties.get().getConvertRecordings()) {
                                    boolean converting = recHandler.isConverting();
                                    if (!converting) {
                                        block = false;
                                        System.exit(0);
                                    }
                                } else {
                                    block = false;
                                    System.exit(0);
                                }
                            }
                        } catch (InterruptedException ex) {
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
                d.showAndWait();
                return; //Don't exit until the recording is saved and converted
            }
        }
        System.exit(0);
    }
}
