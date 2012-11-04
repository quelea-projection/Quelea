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
import org.quelea.QueleaApp;
import org.quelea.ThemeDTO;
import org.quelea.displayable.SongDisplayable;
import org.quelea.displayable.TextSection;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

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
    private final TabPane tabPane;
    private final Button confirmButton;
    private final Button cancelButton;
    private final CheckBox addToSchedCBox;
    private SongDisplayable song;

    /**
     * Create and initialise the new song window.
     * <p/>
     * @param owner the owner of this window.
     */
    public SongEntryWindow() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        getIcons().add(new Image("file:icons/logo.png"));

        BorderPane mainPane = new BorderPane();
        setTitle(LabelGrabber.INSTANCE.getLabel("song.entry.heading"));
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

        setupThemePanel();
        Tab themeTab = new Tab(LabelGrabber.INSTANCE.getLabel("theme.heading"));
        themeTab.setContent(themePanel);
        themeTab.setClosable(false);
        tabPane.getTabs().add(themeTab);

        mainPane.setCenter(tabPane);

        confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("add.song.button"), new ImageView(new Image("file:icons/tick.png")));
        confirmButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
                Utils.updateSongInBackground(getSong(), true, false);
                if(addToSchedCBox.isSelected()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(getSong());
                }
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
            }
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                hide();
            }
        });
        addToSchedCBox = new CheckBox(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"));
        HBox checkBoxPanel = new HBox();
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

        setScene(new Scene(mainPane));
    }

    /**
     * Called by the constructor to initialise the theme panel.
     */
    private void setupThemePanel() {
        themePanel = new ThemePanel();
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

    /**
     * Get the confirm button on the new song window.
     * <p/>
     * @return the confirm button.
     */
    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the new song window.
     * <p/>
     * @return the cancel button.
     */
    public Button getCancelButton() {
        return cancelButton;
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
        song = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("new.song.button"));
        confirmButton.setDisable(true);
        basicSongPanel.resetNewSong();
        detailedSongPanel.resetNewSong();
        themePanel.setTheme(ThemeDTO.DEFAULT_THEME);
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setDisable(false);
    }

    /**
     * Set this window up ready to edit an existing song.
     * <p/>
     * @param song the song to edit.
     */
    public void resetEditSong(SongDisplayable song) {
        setTitle(LabelGrabber.INSTANCE.getLabel("edit.song.title"));
        this.song = song;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("edit.song.button"));
        confirmButton.setDisable(false);
        basicSongPanel.resetEditSong(song);
        detailedSongPanel.resetEditSong(song);
        if(song.getSections().length > 0) {
            themePanel.setTheme(song.getSections()[0].getTheme());
        }
        tabPane.getSelectionModel().select(0);
        addToSchedCBox.setSelected(false);
        if(QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().itemsProperty().get().contains(song)) {
            addToSchedCBox.setDisable(true);
        }
        else {
            addToSchedCBox.setDisable(false);
        }
    }

    /**
     * Get the song that's been edited or created by the window.
     * <p/>
     * @return the song.
     */
    public SongDisplayable getSong() {
        if(song == null) {
            song = new SongDisplayable(getBasicSongPanel().getTitleField().getText(), getBasicSongPanel().getAuthorField().getText());
        }
        song.setLyrics(getBasicSongPanel().getLyricsField().getText());
        song.setTitle(getBasicSongPanel().getTitleField().getText());
        song.setAuthor(getBasicSongPanel().getAuthorField().getText());
        song.setTags(getDetailedSongPanel().getTagsPanel().getTagsAsString());
        song.setCcli(getDetailedSongPanel().getCcliField().getText());
        song.setCopyright(getDetailedSongPanel().getCopyrightField().getText());
        song.setPublisher(getDetailedSongPanel().getPublisherField().getText());
        song.setYear(getDetailedSongPanel().getYearField().getText());
        song.setKey(getDetailedSongPanel().getKeyField().getText());
        song.setCapo(getDetailedSongPanel().getCapoField().getText());
        song.setInfo(getDetailedSongPanel().getInfoField().getText());
        ThemeDTO tempTheme = song.getSections()[0].getTempTheme();
        for(TextSection section : song.getSections()) {
            section.setTheme(themePanel.getTheme());
            if(tempTheme != null) {
                section.setTempTheme(tempTheme);
            }
        }
        return song;
    }

    /**
     * Check whether the confirm button should be enabled or disabled and set it
     * accordingly.
     */
    private void checkConfirmButton() {
        if(getBasicSongPanel().getLyricsField().getText().trim().equals("")
                || getBasicSongPanel().getTitleField().getText().trim().equals("")) {
            confirmButton.setDisable(true);
        }
        else {
            confirmButton.setDisable(false);
        }
    }
}
