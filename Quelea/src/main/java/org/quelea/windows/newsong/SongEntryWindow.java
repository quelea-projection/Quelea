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
package org.quelea.windows.newsong;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.WindowEvent;
import org.javafx.dialog.Dialog;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.TranslatePanel;
import org.quelea.windows.main.QueleaApp;

/**
 * A new song window that users use for inserting the text content of a new
 * song.
 * <p/>
 * @author Michael
 */
public class SongEntryWindow extends Stage {

    private BasicSongPanel basicSongPanel;
    private DetailedSongPanel detailedSongPanel;
    private ThemePanel themePanel;
    private TranslatePanel translatePanel;
    private boolean updateDBOnHide;
    private boolean shouldSave;
    private boolean cancel;
    private final TabPane tabPane;
    private final Button confirmButton;
    private final Button cancelButton;
    private final CheckBox addToSchedCBox;
    private SongDisplayable song;
    private boolean disableTextAreaListeners;

    /**
     * Create and initialise the new song window.
     */
    public SongEntryWindow() {
        initModality(Modality.APPLICATION_MODAL);
        updateDBOnHide = true;
        Utils.addIconsToStage(this);

        confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("add.song.button"), new ImageView(new Image("file:icons/tick.png")));

        BorderPane mainPane = new BorderPane();
        tabPane = new TabPane();

        setupBasicSongPanel();
        Tab basicTab = new Tab(LabelGrabber.INSTANCE.getLabel("basic.information.heading"));
        basicTab.setContent(basicSongPanel);
        basicTab.setClosable(false);
        tabPane.getTabs().add(basicTab);

        setupDetailedSongPanel();
        Tab detailedTab = new Tab(LabelGrabber.INSTANCE.getLabel("detailed.info.heading"));
        detailedTab.setContent(detailedSongPanel);
        detailedTab.setClosable(false);
        tabPane.getTabs().add(detailedTab);

        setupTranslatePanel();
        Tab translateTab = new Tab(LabelGrabber.INSTANCE.getLabel("translate.heading"));
        translateTab.setContent(translatePanel);
        translateTab.setClosable(false);
        tabPane.getTabs().add(translateTab);

        basicSongPanel.getLyricsField().textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!disableTextAreaListeners) {
                    disableTextAreaListeners = true;
                    translatePanel.getDefaultLyricsArea().replaceText(newValue);
                    disableTextAreaListeners = false;
                }
            }
        });
        translatePanel.getDefaultLyricsArea().textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!disableTextAreaListeners) {
                    disableTextAreaListeners = true;
                    basicSongPanel.getLyricsField().replaceText(newValue);
                    disableTextAreaListeners = false;
                }
            }
        });

        setupThemePanel();
        Tab themeTab = new Tab(LabelGrabber.INSTANCE.getLabel("theme.heading"));
        themeTab.setContent(themePanel);
        themeTab.setClosable(false);
        tabPane.getTabs().add(themeTab);

        mainPane.setCenter(tabPane);

        confirmButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                cancel = false;
                saveSong();
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

//        setMaxWidth(525);
//        setMaxHeight(600);
//        setWidth(525);
//        setHeight(600);
//        setResizable(false);
        setScene(new Scene(mainPane));
    }

    /**
     * Determine if the entry window was cancelled.
     * <p>
     * @return true if it was cancelled, false otherwise.
     */
    public boolean wasCancelled() {
        return cancel;
    }

    private boolean isChangeMade() {
        return basicSongPanel.hashChanged() || detailedSongPanel.hashChanged() || themePanel.hashChanged();
    }

    private void resetChange() {
        basicSongPanel.resetSaveHash();
        detailedSongPanel.resetSaveHash();
        themePanel.resetSaveHash();
    }

    private void checkSave() {
        if (shouldSave && isChangeMade() && attributesOk()) {
            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("confirm.entry.exit.title"),
                    LabelGrabber.INSTANCE.getLabel("confirm.entry.exit.text"), SongEntryWindow.this)
                    .addLabelledButton(LabelGrabber.INSTANCE.getLabel("save.text"), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            saveSong();
                        }
                    }).addLabelledButton(LabelGrabber.INSTANCE.getLabel("dont.save.text"), null)
                    .build().showAndWait();
        }
        hide();
    }

    private void saveSong() {
        resetChange();
        hide();
        SongDisplayable localSong = getSong();
        boolean quickInsert = song != null && song.isQuickInSert();
        if (shouldSave) {
            if (updateDBOnHide && !quickInsert) {
                Utils.updateSongInBackground(localSong, true, false);
            }
            if (addToSchedCBox.isSelected()) {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(localSong);
            }
            QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
//            QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh(); //Don't do this - restarts live video unnecessarily
        }
    }

    /**
     * Called by the constructor to initialise the theme panel.
     */
    private void setupThemePanel() {
        themePanel = new ThemePanel(basicSongPanel.getLyricsField(), confirmButton);
    }

    /**
     * Called by the constructor to initialise the detailed song panel.
     */
    private void setupDetailedSongPanel() {
        detailedSongPanel = new DetailedSongPanel();
    }

    /**
     * Called by the constructor to initialise the basic song panel.
     */
    private void setupBasicSongPanel() {
        basicSongPanel = new BasicSongPanel();
        basicSongPanel.getLyricsField().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                checkConfirmButton();
            }
        });

        basicSongPanel.getTitleField().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                checkConfirmButton();
            }
        });
    }

    private void setupTranslatePanel() {
        translatePanel = new TranslatePanel();
    }

    /**
     * Get the panel where the user enters the basic song information.
     * <p/>
     * @return the basic song panel.
     */
    public BasicSongPanel getBasicSongPanel() {
        return basicSongPanel;
    }

    /**
     * Get the translation panel.
     *
     * @return the translation panel.
     */
    public TranslatePanel getTranslatePanel() {
        return translatePanel;
    }

    /**
     * Get the panel where the user enters the more detailed song information.
     * <p/>
     * @return the detailed song panel.
     */
    public DetailedSongPanel getDetailedSongPanel() {
        return detailedSongPanel;
    }

    /**
     * Get the theme currently displayed on this window.
     * <p/>
     * @return the current theme.
     */
    public ThemeDTO getTheme() {
        return themePanel.getTheme();
    }

    /**
     * Set this window up ready to enter a new song.
     */
    public void resetNewSong() {
        setTitle(LabelGrabber.INSTANCE.getLabel("new.song.title"));
        shouldSave = true;
        song = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("new.song.button"));
        basicSongPanel.resetNewSong();
        detailedSongPanel.resetNewSong();
        translatePanel.clearSong();
        themePanel.setTheme(ThemeDTO.DEFAULT_THEME);
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setDisable(false);
        updateDBOnHide = true;
        confirmButton.setDisable(true);
        resetChange();
    }

    /**
     * Set this window up ready to enter a new song.
     */
    public void resetQuickInsert() {
        setTitle(LabelGrabber.INSTANCE.getLabel("quick.insert.text"));
        shouldSave = false;
        song = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"));
        confirmButton.setDisable(true);
        basicSongPanel.resetNewSong();
        detailedSongPanel.resetNewSong();
        themePanel.setTheme(ThemeDTO.DEFAULT_THEME);
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setDisable(true);
        updateDBOnHide = false;
        resetChange();
    }

    /**
     * Set this window up ready to edit an existing song.
     * <p/>
     * @param song the song to edit.
     */
    public void resetEditSong(SongDisplayable song) {
        setTitle(LabelGrabber.INSTANCE.getLabel("edit.song.title"));
        this.song = song;
        shouldSave = true;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("edit.song.button"));
        confirmButton.setDisable(false);
        basicSongPanel.resetEditSong(song);
        detailedSongPanel.resetEditSong(song);
        translatePanel.setSong(song);
        if (song.getSections().length > 0) {
            themePanel.setTheme(song.getSections()[0].getTheme());
        }
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        if (QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get().contains(song)) {
            addToSchedCBox.setDisable(true);
        } else {
            addToSchedCBox.setDisable(false);
        }
        updateDBOnHide = true;
        resetChange();
    }

    /**
     * Get the song that's been edited or created by the window.
     * <p/>
     * @return the song.
     */
    public SongDisplayable getSong() {
        if (song == null) {
            song = new SongDisplayable(getBasicSongPanel().getTitleField().getText(), getBasicSongPanel().getAuthorField().getText());
        }
        ThemeDTO tempTheme = null;
        if (song.getSections().length > 0) {
            tempTheme = song.getSections()[0].getTempTheme();
        }
        song.setLyrics(getBasicSongPanel().getLyricsField().getText());
        song.setTitle(getBasicSongPanel().getTitleField().getText());
        song.setAuthor(getBasicSongPanel().getAuthorField().getText());
        song.setTranslations(getTranslatePanel().getTranslations());
        song.setCcli(getDetailedSongPanel().getCcliField().getText());
        song.setCopyright(getDetailedSongPanel().getCopyrightField().getText());
        song.setPublisher(getDetailedSongPanel().getPublisherField().getText());
        song.setYear(getDetailedSongPanel().getYearField().getText());
        song.setKey(getDetailedSongPanel().getKeyField().getText());
        song.setCapo(getDetailedSongPanel().getCapoField().getText());
        song.setInfo(getDetailedSongPanel().getInfoField().getText());
        for (TextSection section : song.getSections()) {
            section.setTheme(themePanel.getTheme());
            if (tempTheme != null) {
                section.setTempTheme(tempTheme);
            }
        }
        song.setTheme(themePanel.getTheme());
        return song;
    }

    /**
     * Check whether the confirm button should be enabled or disabled and set it
     * accordingly.
     */
    private void checkConfirmButton() {
        confirmButton.setDisable(!attributesOk());
    }

    /**
     * Determine if this song entry window contains a song that could be saved.
     * <p/>
     * @return true if the song is viable (has a title and lyrics), false
     * otherwise.
     */
    private boolean attributesOk() {
        return !getBasicSongPanel().getTitleField().getText().trim().isEmpty(); //Allow empty lyrics for now
//        return !(getBasicSongPanel().getLyricsField().getText().trim().isEmpty()
//                || getBasicSongPanel().getTitleField().getText().trim().isEmpty());
    }
}
