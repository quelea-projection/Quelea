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
package org.quelea.data.bible;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.library.ContextMenuListCell;

/**
 * A dialog that can be used for searching for bible passages.
 * <p/>
 * @author mjrb5
 */
public class BibleSearchDialog extends Stage implements BibleChangeListener {

    private TextField searchField;
    private ListView<BibleChapter> searchResults;
    private ComboBox<String> bibles;
    private BibleSearchPopupMenu popupMenu;

    /**
     * Create a new bible searcher dialog.
     */
    public BibleSearchDialog() {
        BorderPane mainPane = new BorderPane();
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.search.title"));
        getIcons().add(new Image("file:icons/search.png"));
        searchField = new TextField();
        bibles = new ComboBox<>();
        bibles.setEditable(false);
        BibleManager.get().registerBibleChangeListener(this);
        updateBibles();
        HBox northPanel = new HBox();
        northPanel.getChildren().add(bibles);
        northPanel.getChildren().add(searchField);
        mainPane.setTop(northPanel);
        popupMenu = new BibleSearchPopupMenu();
        searchResults = new ListView<>();
//        searchResults.setCellRenderer(new SearchPreviewRenderer());
        searchResults.setCellFactory(ContextMenuListCell.<BibleChapter>forListView(popupMenu));
        VBox centrePanel = new VBox();
        centrePanel.getChildren().add(searchResults);
        mainPane.setCenter(centrePanel);
        bibles.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                update();
            }
        });
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                update();
            }
        });
        reset();

        setScene(new Scene(mainPane));
    }

    /**
     * Reset this dialog.
     */
    public final void reset() {
        searchResults.itemsProperty().get().clear();
        searchField.setText(LabelGrabber.INSTANCE.getLabel("initial.search.text"));
        searchField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if(t1.booleanValue()) {
                    searchField.setText("");
                    searchField.focusedProperty().removeListener(this);
                }
            }
        });
        searchField.setDisable(true);
        BibleManager.get().runOnIndexInit(new Runnable() {
            @Override
            public void run() {
                searchField.setDisable(false);
            }
        });
    }

    /**
     * Update the results based on the entered text.
     */
    private void update() {
        if(BibleManager.get().isIndexInit()) {
            final String text = searchField.getText();
            final BibleChapter[] results = BibleManager.get().getIndex().filter(text, null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    searchResults.itemsProperty().get().clear();
                    if(!text.trim().isEmpty()) {
                        for(BibleChapter chapter : results) {
                            if(bibles.getSelectionModel().getSelectedIndex() == 0 || chapter.getBook().getBible().getName().equals(bibles.getSelectionModel().getSelectedItem())) {
                                searchResults.itemsProperty().get().add(chapter);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Update the list of bibles on this search dialog.
     */
    @Override
    public final void updateBibles() {
        bibles.itemsProperty().get().clear();
        bibles.itemsProperty().get().add(LabelGrabber.INSTANCE.getLabel("all.text"));
        for(Bible bible : BibleManager.get().getBibles()) {
            bibles.itemsProperty().get().add(bible.getName());
        }
    }
}
