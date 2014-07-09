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
package org.quelea.windows.mediaLoop.mediaLoopCreator;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.mediaLoop.MediaLoopEditorPanel;

/**
 * A new media loop creator window, where users can insert media into a slide
 * show-like loopable structure.
 * <p/>
 * @author Michael
 */
public class MediaLoopCreatorWindow extends Stage {

    private MediaLoopEditorPanel editorPanel;
    private boolean updateDBOnHide;
    private boolean shouldSave;
    private boolean cancel;
    private final TabPane tabPane;
    private final Button confirmButton;
    private final Button cancelButton;
    private final CheckBox addToSchedCBox;
    private MediaLoopDisplayable mediaLoop;

    /**
     * Create and initialise the media loop creator window.
     */
    public MediaLoopCreatorWindow() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        updateDBOnHide = true;
        Utils.addIconsToStage(this);

        confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("add.mediaLoop.button"), new ImageView(new Image("file:icons/tick.png")));

        BorderPane mainPane = new BorderPane();
        tabPane = new TabPane();

        setupMediaLoopCreatorPanel();
        Tab basicTab = new Tab(LabelGrabber.INSTANCE.getLabel("mediaLoop.creator.mainPage"));
        basicTab.setContent(editorPanel);
        basicTab.setClosable(false);
        tabPane.getTabs().add(basicTab);

        mainPane.setCenter(tabPane);

        confirmButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                cancel = false;
                if (!attributesOk()) {
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("notify.mediaLoop.needMoreAttributes.title"),
                            LabelGrabber.INSTANCE.getLabel("notify.mediaLoop.needMoreAttributes.text"), MediaLoopCreatorWindow.this)
                            .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), null)
                            .build().showAndWait();
                    return;
                }
                saveMediaLoop();

            }
        });

        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                checkSave();
            }
        });

        addToSchedCBox = new CheckBox(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"));
        HBox checkBoxPanel = new HBox();
        HBox.setMargin(addToSchedCBox, new Insets(0, 0, 0, 10));
        checkBoxPanel.getChildren().add(addToSchedCBox);
        VBox bottomPanel = new VBox();
        bottomPanel.setSpacing(5);
        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().add(confirmButton);
        buttonPanel.getChildren().add(cancelButton);
        bottomPanel.getChildren().add(checkBoxPanel);
        bottomPanel.getChildren().add(buttonPanel);
        BorderPane.setMargin(bottomPanel, new Insets(10, 0, 5, 0));
        mainPane.setBottom(bottomPanel);

        setOnShowing(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                cancel = true;
            }
        });
        setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                checkSave();
            }
        });

        setWidth(1250);
        setHeight(700);

        setScene(new Scene(mainPane));
    }

    /**
     * Determine if the media creator window was cancelled.
     * <p>
     * @return true if it was cancelled, false otherwise.
     */
    public boolean wasCancelled() {
        return cancel;
    }

    /**
     * Determine if there has been a change made to the media loop
     *
     * @return true if changed, false otherwise
     */
    private boolean isChangeMade() {
        return editorPanel.hashChanged();
    }

    /**
     * Reset the changes that were made to the media loop.
     */
    private void resetChange() {
        editorPanel.resetSaveHash();

    }
    
    /**
     * The File to add to the media loop
     * @param inputFile the file to be added
     */
    public void addFile(File inputFile){
        
    }

    /**
     * See if things should be saved before hiding.
     */
    private void checkSave() {
        if (shouldSave && isChangeMade() && attributesOk()) {
            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.mediaLoop.entry.exit.title"),
                    LabelGrabber.INSTANCE.getLabel("confirm.mediaLoop.entry.exit.text"), MediaLoopCreatorWindow.this)
                    .addLabelledButton(LabelGrabber.INSTANCE.getLabel("save.text"), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            saveMediaLoop();
                        }
                    }).addLabelledButton(LabelGrabber.INSTANCE.getLabel("dont.save.text"), null)
                    .build().showAndWait();
        }
        hide();
    }

    /**
     * Save the media loop.
     */
    private void saveMediaLoop() {
        resetChange();
        hide();
        MediaLoopDisplayable localMediaLoop = getMediaLoop();
        boolean quickInsert = mediaLoop != null && mediaLoop.isQuickInsert();
        if (shouldSave) {
            if (updateDBOnHide && !quickInsert) {
                Utils.updateMediaLoopInBackground(localMediaLoop, true, false);
            }
            if (addToSchedCBox.isSelected()) {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(localMediaLoop);
            }
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
            QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh();
        }
    }

    /**
     * Called by the constructor to initialise the media loop creator window.
     */
    private void setupMediaLoopCreatorPanel() {
        editorPanel = new MediaLoopEditorPanel();

    }

    /**
     * Get the confirm button on the media loop creator window.
     * <p/>
     * @return the confirm button.
     */
    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the media loop creator window.
     * <p/>
     * @return the cancel button.
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Get the panel where the user creates the media loop.
     * <p/>
     * @return the editor panel.
     */
    public MediaLoopEditorPanel getMediaLoopEditorPanel() {
        return editorPanel;
    }

    /**
     * Set this window up ready to create a new media loop.
     */
    public void resetNewMediaLoop() {
        setTitle(LabelGrabber.INSTANCE.getLabel("new.mediaLoop.title"));
        shouldSave = true;
        mediaLoop = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("new.mediaLoop.button"));

        editorPanel.resetNewMediaLoop();
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setDisable(false);
        updateDBOnHide = true;
        resetChange();
    }

    /**
     * Set this window up ready to create new media loop.
     */
    public void resetQuickInsert() {
        setTitle(LabelGrabber.INSTANCE.getLabel("quick.insert.text"));
        shouldSave = false;
        mediaLoop = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"));
        confirmButton.setDisable(true);
        editorPanel.resetNewMediaLoop();
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setDisable(true);
        updateDBOnHide = false;
        resetChange();
    }

    /**
     * Set this window up ready to edit an existing media loop.
     * <p/>
     * @param mediaLoop the media loop to edit.
     */
    public void resetEditMediaLoop(MediaLoopDisplayable mediaLoop) {
        setTitle(LabelGrabber.INSTANCE.getLabel("edit.mediaLoop"));
        this.mediaLoop = mediaLoop;
        shouldSave = true;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("edit.mediaLoop"));
        confirmButton.setDisable(false);
        editorPanel.resetEditMediaLoop(mediaLoop);
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        if (QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get().contains(mediaLoop)) {
            addToSchedCBox.setDisable(true);
        } else {
            addToSchedCBox.setDisable(false);
        }
        updateDBOnHide = true;
        resetChange();
    }

    /**
     * Get the media loop that's been edited or created by the window.
     * <p/>
     * @return the media loop.
     */
    public MediaLoopDisplayable getMediaLoop() {

        mediaLoop = new MediaLoopDisplayable();

        mediaLoop.setTitle(getMediaLoopEditorPanel().getTitleField().getText().trim());
        for (MediaFile file : getMediaLoopEditorPanel().getMediaFiles()) {
            mediaLoop.add(file);
        }
        return mediaLoop;
    }

    /**
     * Determine if this media loop window contains a media loop that could be
     * saved.
     * <p/>
     * @return true if the media loop is viable (has a title and items), false
     * otherwise.
     */
    private boolean attributesOk() {
        return !(getMediaLoopEditorPanel().getMediaFiles().size() <= 0
                || getMediaLoopEditorPanel().getTitleField().getText().trim().isEmpty());
    }
}
