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
package org.quelea.windows.library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddTimerActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveTimerActionHandler;

/**
 * The timer panel in the library.
 * <p/>
 * @author Ben
 */
public class LibraryTimerPanel extends BorderPane {

    private final TimerListPanel timerPanel;
    private final ToolBar toolbar;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Button removeButton;

    /**
     * Create a new library timer panel.
     */
    public LibraryTimerPanel() {
        timerPanel = new TimerListPanel(QueleaProperties.get().getTimerDir().getAbsolutePath());
        setCenter(timerPanel);
        toolbar = new ToolBar();

        timerPanel.getListView().getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            checkRemoveButton();
        });
        timerPanel.getListView().itemsProperty().addListener((ObservableValue<? extends ObservableList<TimerDisplayable>> observable, ObservableList<TimerDisplayable> oldValue, ObservableList<TimerDisplayable> newValue) -> {
            checkRemoveButton();
        });

        Button addButton = new Button("", new ImageView(new Image("file:icons/add.png")));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.timers.panel")));
        addButton.setOnAction(new AddTimerActionHandler());
        
        Button importButton = new Button("", new ImageView(new Image("file:icons/importbw.png")));
        importButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("import.heading")));
        importButton.setOnAction((ActionEvent t) -> {
            FileChooser chooser = new FileChooser();
            if (QueleaProperties.get().getLastDirectory() != null) {
                chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
            }
            chooser.getExtensionFilters().add(FileFilters.TIMERS);
            chooser.setInitialDirectory(QueleaProperties.get().getTimerDir().getAbsoluteFile());
            List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());
            if (files != null) {
                final boolean[] refresh = new boolean[]{false};
                for (final File f : files) {
                    QueleaProperties.get().setLastDirectory(f.getParentFile());
                    try {
                        final Path sourceFile = f.getAbsoluteFile().toPath();

                        if (new File(timerPanel.getDir(), f.getName()).exists()) {
                            Dialog d = Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.overwrite.title"), f.getName() + "\n" + LabelGrabber.INSTANCE.getLabel("confirm.overwrite.text"))
                                    .addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.replace.button"), (ActionEvent t1) -> {
                                        try {
                                            Files.delete(Paths.get(timerPanel.getDir(), f.getName()));
                                            Files.copy(sourceFile, Paths.get(timerPanel.getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                                            refresh[0] = true;
                                        } catch (IOException e) {
                                            LOGGER.log(Level.WARNING, "Could not delete or copy file back into directory.", e);
                                        }
                                    }).addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.continue.button"), (ActionEvent t1) -> {
                                        // DO NOTHING
                                    }).build();
                            d.showAndWait();
                        } else {
                            Files.copy(sourceFile, Paths.get(timerPanel.getDir(), f.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                            refresh[0] = true;
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Could not copy file into TimerPanel from FileChooser selection", ex);
                    }
                }
                if (refresh[0]) {
                    timerPanel.refresh();
                }
            }
        });
        ImageView removeIV = new ImageView(new Image("file:icons/removedb.png"));
        removeButton = new Button("", removeIV);
        Utils.setToolbarButtonStyle(removeButton);
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.timer.text")));
        removeButton.setDisable(true);
        removeButton.setOnAction(new RemoveTimerActionHandler());
        HBox toolbarBox = new HBox();
        toolbar.setOrientation(Orientation.VERTICAL);
        toolbarBox.getChildren().add(toolbar);
        Utils.setToolbarButtonStyle(addButton);
        Utils.setToolbarButtonStyle(importButton);
        toolbar.getItems().addAll(addButton, importButton, removeButton);
        setLeft(toolbarBox);
    }

    /**
     * Get the timer list panel.
     * <p/>
     * @return the timer list panel.
     */
    public TimerListPanel getTimerPanel() {
        return timerPanel;
    }

    /**
     * Check whether the remove button should be enabled or disabled and set it
     * accordingly.
     */
    private void checkRemoveButton() {
        if (timerPanel.getListView().getSelectionModel().selectedIndexProperty().getValue() == -1 || timerPanel.getListView().itemsProperty().get().size() == 0) {
            removeButton.setDisable(true);
        } else {
            removeButton.setDisable(false);
        }
    }
}
