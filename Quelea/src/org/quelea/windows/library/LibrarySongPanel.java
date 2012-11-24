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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.actionhandlers.NewSongActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveSongDBActionHandler;

/**
 * The panel used for browsing the database of songs and adding any songs to the order of service.
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
        songList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                checkRemoveButton();
            }
        });
        songList.itemsProperty().addListener(new ChangeListener<ObservableList<SongDisplayable>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<SongDisplayable>> ov, ObservableList<SongDisplayable> t, ObservableList<SongDisplayable> t1) {
                checkRemoveButton();
            }
        });
        ScrollPane listScrollPane = new ScrollPane();
        setCenter(listScrollPane);

        HBox northPanel = new HBox();
        Label searchLabel = new Label(LabelGrabber.INSTANCE.getLabel("library.song.search"));
        searchLabel.setAlignment(Pos.CENTER);
        northPanel.getChildren().add(searchLabel);
        searchBox = new TextField();
        HBox.setHgrow(searchBox, Priority.SOMETIMES);
        searchBox.setMaxWidth(Double.MAX_VALUE);
        searchBox.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                searchCancelButton.fire();
            }
        });
        searchBox.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                if(searchBox.getText().isEmpty()) {
                    searchCancelButton.setDisable(true);
                }
                else {
                    searchCancelButton.setDisable(false);
                }
                songList.filter(searchBox.getText());
            }
        });
        northPanel.getChildren().add(searchBox);
        searchCancelButton = new Button("", new ImageView(new Image("file:icons/cross.png")));
        searchCancelButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("clear.search.box")));
        searchCancelButton.setDisable(true);
        searchCancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                searchBox.clear();
            }
        });
        northPanel.getChildren().add(searchCancelButton);
        setTop(northPanel);

        ToolBar toolbar = new ToolBar();
        toolbar.setOrientation(Orientation.VERTICAL);
        
        addButton = new Button("",new ImageView(new Image("file:icons/newsongdb.png", 16, 16, false, true)));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.song.text")));
        addButton.setOnAction(new NewSongActionHandler());
        toolbar.getItems().add(addButton);
        removeButton = new Button("", new ImageView(new Image("file:icons/removedb.png", 16, 16, false, true)));
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.text")));
        removeButton.setDisable(true);
        removeButton.setOnAction(new RemoveSongDBActionHandler());
        toolbar.getItems().add(removeButton);
        setLeft(toolbar);
        setCenter(songList);

    }

    /**
     * Check whether the remove button should be enabled or disabled and set it accordingly.
     */
    private void checkRemoveButton() {
        if(songList.getSelectionModel().selectedIndexProperty().getValue() == -1 || songList.itemsProperty().get().size() == 0) {
            removeButton.setDisable(true);
        }
        else {
            removeButton.setDisable(false);
        }
    }

    /**
     * Get the song list behind this panel.
     * @return the song list.
     */
    public LibrarySongList getSongList() {
        return songList;
    }
}
