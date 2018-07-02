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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveSongDBActionHandler;

/**
 * The panel used for browsing the database of songs and adding any songs to the
 * order of service.
 * <p/>
 * @author Michael
 */
public class LibrarySongPanel extends BorderPane {

    private final TextField searchBox;
    private final Button searchCancelButton;
    private final LibrarySongList songList;
    private final Button removeButton;
    private final Button addButton;

    /**
     * Create and initialise the library song panel.
     */
    public LibrarySongPanel() {
        songList = new LibrarySongList(true);
        songList.getListView().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                checkRemoveButton();
            }
        });
        songList.getListView().itemsProperty().addListener(new ChangeListener<ObservableList<SongDisplayable>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<SongDisplayable>> ov, ObservableList<SongDisplayable> t, ObservableList<SongDisplayable> t1) {
                checkRemoveButton();
            }
        });
        ScrollPane listScrollPane = new ScrollPane();
        setCenter(listScrollPane);

        HBox northPanel = new HBox(3);
        Label searchLabel = new Label(LabelGrabber.INSTANCE.getLabel("library.song.search"));
        searchLabel.setMaxHeight(Double.MAX_VALUE);
        searchLabel.setAlignment(Pos.CENTER);
        northPanel.getChildren().add(searchLabel);
        searchBox = new TextField();
        HBox.setHgrow(searchBox, Priority.SOMETIMES);
        searchBox.setMaxWidth(Double.MAX_VALUE);
        searchBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.ESCAPE) {
                    searchCancelButton.fire();
                }
            }
        });
        searchBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                searchCancelButton.setDisable(searchBox.getText().isEmpty());
                songList.filter(searchBox.getText());
            }
        });
        northPanel.getChildren().add(searchBox);
        searchCancelButton = new Button("", new ImageView(new Image("file:icons/cross.png")));
        Utils.setToolbarButtonStyle(searchCancelButton);
        searchCancelButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("clear.search.box")));
        searchCancelButton.setDisable(true);
        searchCancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                searchBox.clear();
            }
        });
        northPanel.getChildren().add(searchCancelButton);
        BorderPane.setMargin(northPanel, new Insets(0, 5, 0, 5));
        setTop(northPanel);

        ToolBar toolbar = new ToolBar();
        toolbar.setOrientation(Orientation.VERTICAL);

        ImageView addIV = new ImageView(new Image("file:icons/newsongdb.png"));
        addIV.setFitWidth(16);
        addIV.setFitHeight(16);
        addButton = new Button("", addIV);
        Utils.setToolbarButtonStyle(addButton);
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.song.text")));
        addButton.setOnAction(new NewSongActionHandler());
        toolbar.getItems().add(addButton);
        ImageView removeIV = new ImageView(new Image("file:icons/removedb.png"));
        removeIV.setFitWidth(16);
        removeIV.setFitHeight(16);
        removeButton = new Button("", removeIV);
        Utils.setToolbarButtonStyle(removeButton);
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.text")));
        removeButton.setDisable(true);
        removeButton.setOnAction(new RemoveSongDBActionHandler());
        toolbar.getItems().add(removeButton);
        setLeft(toolbar);
        setCenter(songList);

    }

    /**
     * Check whether the remove button should be enabled or disabled and set it
     * accordingly.
     */
    private void checkRemoveButton() {
        if(songList.getListView().getSelectionModel().selectedIndexProperty().getValue() == -1 || songList.getListView().itemsProperty().get().size() == 0) {
            removeButton.setDisable(true);
        }
        else {
            removeButton.setDisable(false);
        }
    }

    /**
     * Get the song list behind this panel.
     * <p/>
     * @return the song list.
     */
    public LibrarySongList getSongList() {
        return songList;
    }

    /**
     * Get the search box in this panel.
     * <p/>
     * @return the search box.
     */
    public TextField getSearchBox() {
        return searchBox;
    }
}
