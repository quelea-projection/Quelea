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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
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

	private static final Logger LOGGER = LoggerUtils.getLogger();
	private final TextField locationField;
	private final Button importButton;
	private final Button closeButton;
	private final CheckBox checkDuplicates;
	private final SelectSongsDialog importedDialog;
	private final List<File> files;
	private StatusPanel statusPanel;
	private boolean halt;

	/**
	 * Create a new import dialog.
	 * <p/>
	 * @param dialogLabels the labels to contain on the dialog as text to the
	 * user before the file box.
	 * @param fileFilter the filefilter to use in the file dialog, or null if
	 * there should be no file dialog.
	 * @param parser the parser to use for this import dialog.
	 * @param options import dialog options
	 */
	public ImportDialog(String[] dialogLabels, ExtensionFilter fileFilter,
			final SongParser parser, final ImportDialogOptions options) {
		initModality(Modality.APPLICATION_MODAL);
		setTitle(LabelGrabber.INSTANCE.getLabel("import.heading"));
		files = new ArrayList<>();
		halt = false;
		importedDialog = new SelectImportedSongsDialog();
		importButton = new Button(LabelGrabber.INSTANCE.getLabel("import.button"), new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-import-light.png" :"file:icons/ic-import.png", 16, 16, false, true)));

		VBox mainPane = new VBox();
		final FileChooser locationChooser = new FileChooser();
		if (QueleaProperties.get().getLastDirectory() != null) {
			locationChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
		}
		locationChooser.getExtensionFilters().add(fileFilter);
		if (options.isAllowZip()) {
			locationChooser.getExtensionFilters().add(FileFilters.ZIP);
		}

		VBox textPane = new VBox();
		for (String str : dialogLabels) {
			textPane.getChildren().add(new Label(str));
		}
		VBox.setMargin(textPane, new Insets(10));
		mainPane.getChildren().add(textPane);

		checkDuplicates = new CheckBox(LabelGrabber.INSTANCE.getLabel("check.duplicates.text"));
		VBox.setMargin(checkDuplicates, new Insets(0, 0, 10, 12));
		mainPane.getChildren().add(checkDuplicates);

		locationField = new TextField();
		VBox.setMargin(locationField, new Insets(0, 10, 0, 10));
		if (fileFilter != null) {
			locationField.setEditable(false);
			locationField.setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
			locationField.setOnMouseClicked(evt -> {
				if (!locationField.isDisable()) {
					files.clear();
					if (options.isAllowMultiple()) {
						List<File> f = locationChooser.showOpenMultipleDialog(ImportDialog.this);
						if (f != null) {
							files.addAll(f);
						}
					} else {
						File f = locationChooser.showOpenDialog(ImportDialog.this);
						if (f != null) {
							files.add(f);
						}
					}
					if(options.isAllowZip()) {
						List<File> tempList = new ArrayList<>();
						for(File file : files) {
							if(file.getName().endsWith(".zip")) {
								tempList.addAll(Utils.extractZip(file));
							}
							else {
								tempList.add(file);
							}
						}
						files.clear();
						files.addAll(tempList);
					}
					if (!files.isEmpty()) {
						QueleaProperties.get().setLastDirectory(files.get(0).getParentFile());
						StringBuilder locationContent = new StringBuilder();
						for (int i = 0; i < files.size(); i++) {
							locationContent.append(files.get(0).getAbsolutePath());
							if (i < files.size() - 1) {
								locationContent.append("; ");
							}
						}
						locationField.setText(locationContent.toString());
						importButton.setDisable(false);
					}
				}
			});
			mainPane.getChildren().add(locationField);
		}

		closeButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
		closeButton.setOnAction(evt -> {
			hide();
		});
		if (fileFilter != null) {
			importButton.setDisable(true);
		}
		HBox buttonPane = new HBox(10);
		buttonPane.setAlignment(Pos.CENTER);
		StackPane.setMargin(importButton, new Insets(10));
		buttonPane.getChildren().add(importButton);
		buttonPane.getChildren().add(closeButton);
		VBox.setMargin(buttonPane, new Insets(10));
		mainPane.getChildren().add(buttonPane);
		importButton.setOnAction(evt -> {
			statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("importing.status"));
			statusPanel.getCancelButton().setOnAction(evt2 -> {
				statusPanel.done();
				halt = true;
			});
			setActive();
			Thread worker = new Thread() {
				private List<SongDisplayable> localSongs;
				private Map<SongDisplayable, Boolean> localSongsDuplicate;
				private final ExecutorService checkerService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

				@Override
				public void run() {
					try {
						localSongs = new ArrayList<>();
						localSongsDuplicate = new HashMap<>();
						for (File file : files) {
							localSongs.addAll(parser.getSongs(file, statusPanel));
						}
						if (halt) {
							localSongs = null;
						}
						Collections.sort(localSongs);
						statusPanel.setProgress(0);
						if (checkDuplicates.isSelected()) {
							for (int i = 0; i < (localSongs == null ? 0 : localSongs.size()); i++) {
								final int finali = i;
								checkerService.submit(() -> {
									try {
										if (!halt) {
											final boolean result = new SongDuplicateChecker().checkSong(localSongs.get(finali));
											localSongsDuplicate.put(localSongs.get(finali), result);
											final double progress = ((double) finali / localSongs.size());
											if (statusPanel.getProgress() < progress) {
												statusPanel.setProgress(progress);
											}
										}
									} catch (Throwable ex) {
										ex.printStackTrace();
									}
								});
							}
							try {
								checkerService.shutdown();
								checkerService.awaitTermination(365, TimeUnit.DAYS); //Year eh? ;-)
							} catch (InterruptedException ex) {
								LOGGER.log(Level.WARNING, "Interrupted?!", ex);
							}
						}
						Platform.runLater(() -> {
							if ((localSongs == null || localSongs.isEmpty()) && !halt) {
								Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("import.no.songs.title"), LabelGrabber.INSTANCE.getLabel("import.no.songs.text"));
							} else if (!(localSongs == null || localSongs.isEmpty())) {
								getImportedDialog().setSongs(localSongs, localSongsDuplicate, true);
								getImportedDialog().show();
							}
							setIdle();
						});
					} catch (IOException ex) {
						Platform.runLater(() -> {
							Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("import.error.message"));
						});
						LOGGER.log(Level.WARNING, "Error importing songs", ex);
					}
				}
			};
			worker.start();
		});
		setResizable(false);

		Scene scene = new Scene(mainPane);
		if (QueleaProperties.get().getUseDarkTheme()) {
			scene.getStylesheets().add("org/modena_dark.css");
		}
		setScene(scene);
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
		locationField.setText(LabelGrabber.INSTANCE.getLabel("click.select.file.text"));
		locationField.setDisable(false);
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

	public void setRange(boolean range) {
		//to be overwritten
	}
}
