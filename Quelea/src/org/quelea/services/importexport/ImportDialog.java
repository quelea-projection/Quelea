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
package org.quelea.services.importexport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.SongDuplicateChecker;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * An import dialog used for importing songs.
 * <p/>
 * @author Michael
 */
public abstract class ImportDialog extends Stage implements PropertyChangeListener {

    private final TextField locationField;
    private final Button importButton;
    private final CheckBox checkDuplicates;
    private final SelectSongsDialog importedDialog;
    private StatusPanel statusPanel;
    private boolean halt;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new import dialog.
     * <p/>
     * @param owner the owner of this dialog.
     * @param dialogLabels the labels to contain on the dialog as text to the
     * user before the file box.
     * @param fileFilter the filefilter to use in the file dialog, or null if
     * there should be no file dialog.
     * @param parser the parser to use for this import dialog.
     * @param selectDirectory true if the user should only be allowed to select
     * directories, false otherwise.
     */
    protected ImportDialog(String[] dialogLabels, ExtensionFilter fileFilter,
            final SongParser parser, final boolean selectDirectory) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(LabelGrabber.INSTANCE.getLabel("import.heading"));
        halt = false;
        importedDialog = new SelectImportedSongsDialog();
        VBox mainPane = new VBox();
        final FileChooser locationChooser = new FileChooser();
        locationChooser.getExtensionFilters().add(fileFilter);
        final DirectoryChooser dirChooser = new DirectoryChooser();
        for (String str : dialogLabels) {
            mainPane.getChildren().add(new Label(str));
        }

        checkDuplicates = new CheckBox(LabelGrabber.INSTANCE.getLabel("check.duplicates.text"));
        mainPane.getChildren().add(checkDuplicates);

        locationField = new TextField();
        if (fileFilter != null) {
            locationField.setEditable(false);
            locationField.setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
            locationField.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent t) {
                    if (!locationField.isDisable()) {
                        File file;
                        if (selectDirectory) {
                            file = dirChooser.showDialog(ImportDialog.this);
                        } else {
                            file = locationChooser.showOpenDialog(ImportDialog.this);
                        }
                        if (file != null) {
                            locationField.setText(file.getAbsolutePath());
                            importButton.setDisable(false);
                        }
                    }
                }
            });
            mainPane.getChildren().add(locationField);
        }

        importButton = new Button(LabelGrabber.INSTANCE.getLabel("import.button"));
        if (fileFilter != null) {
            importButton.setDisable(true);
        }
        mainPane.getChildren().add(importButton);
        importButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("importing.status"));
                statusPanel.getCancelButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                    @Override
                    public void handle(javafx.event.ActionEvent t) {
                        statusPanel.done();
                        halt = true;
                    }
                });
                final String location = locationField.getText();
                setActive();
                Thread worker = new Thread() {
                    private List<SongDisplayable> localSongs;
                    private boolean[] localSongsDuplicate;
                    private ExecutorService checkerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                    @Override
                    public void run() {
                        try {
                            localSongs = parser.getSongs(new File(location), statusPanel);
//                            Song[] localSongsArr = new Song[localSongs.size()];
//                            for(int i=0 ; i<localSongs.size() ; i++) {
//                                localSongsArr[i] = localSongs.get(i);
//                            }
//                            localSongsDuplicate = new boolean[localSongs.size()];
                            if (halt) {
                                localSongs = null;
                            }
                            statusPanel.setProgress(0);
                            if (checkDuplicates.isSelected()) {
//                                localSongsDuplicate = new SongDuplicateChecker().checkSongs(localSongsArr);
                                for (int i = 0; i < localSongs.size(); i++) {
                                    final int finali = i;
                                    checkerService.submit(Utils.wrapAsLowPriority(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!halt) {
                                                final boolean result = new SongDuplicateChecker().checkSong(localSongs.get(finali));
                                                localSongsDuplicate[finali] = result;
                                                final double progress = ((double) finali / localSongs.size());
                                                if (statusPanel.getProgress() < progress) {
                                                    statusPanel.setProgress(progress);
                                                }
                                            }
                                        }
                                    }));
                                }
                                try {
                                    checkerService.shutdown();
                                    checkerService.awaitTermination(365, TimeUnit.DAYS); //Year eh? ;-)
                                } catch (InterruptedException ex) {
                                    LOGGER.log(Level.WARNING, "Interrupted?!", ex);
                                }
                            }
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if ((localSongs == null || localSongs.isEmpty()) && !halt) {
                                        Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("import.no.songs.title"), LabelGrabber.INSTANCE.getLabel("import.no.songs.text"));
                                    } else if (!(localSongs == null || localSongs.isEmpty())) {

                                        getImportedDialog().setSongs(localSongs, localSongsDuplicate, true);
                                        getImportedDialog().show();
                                    }
                                    setIdle();
                                }
                            });
                        } catch (IOException ex) {
                            Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("import.error.message"));
                            LOGGER.log(Level.WARNING, "Error importing songs", ex);
                        }
                    }
                };
                worker.start();
            }
        });
        setResizable(false);

        setScene(new Scene(mainPane));
    }

    /**
     * Get the import button.
     * <p/>
     * @return the import button.
     */
    public Button getImportButton() {
        return importButton;
    }

    /**
     * Get the location field.
     * <p/>
     * @return the location field.
     */
    public TextField getLocationField() {
        return locationField;
    }

    /**
     * Get the dialog that appears after the songs have been imported.
     * <p/>
     * @return the imported songs dialog.
     */
    public SelectSongsDialog getImportedDialog() {
        return importedDialog;
    }

    /**
     * Called when the import is taking place, this disables the appropriate
     * controls.
     */
    public void setActive() {
        statusPanel.getProgressBar().setProgress(-1);
        hide();
        resetDialog();
    }

    /**
     * Called when the import has finished taking place, this resets the
     * controls.
     */
    public void setIdle() {
        statusPanel.done();
        halt = false;
        resetDialog();
    }

    private void resetDialog() {
        getLocationField().setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
        getLocationField().setDisable(false);
        getImportButton().setText(LabelGrabber.INSTANCE.getLabel("import.button"));
        hide();
    }

    /**
     * Update the progress bar.
     * <p/>
     * @param evt the property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("progress".equals(strPropertyName) && statusPanel != null) {
            int progress = (Integer) evt.getNewValue();
            statusPanel.getProgressBar().setProgress(progress);
        }
    }

    public void setAll(boolean all) {
        //to be overwritten
    }
}
