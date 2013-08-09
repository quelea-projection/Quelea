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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.widgets.LoadingPane;

/**
 * A dialog that can be used for searching for bible passages.
 * <p/>
 * @author mjrb5
 */
public class BibleSearchDialog extends Stage implements BibleChangeListener {

    private TextField searchField;
    //private ListView<BibleChapter> searchResults;
    private BibleSearchTreeView searchResults;
    private ComboBox<String> bibles;
    private BibleSearchPopupMenu popupMenu;
    private LoadingPane overlay;
    private FlowPane chapterPane;
    private final Button addToSchedule;

    /**
     * Create a new bible searcher dialog.
     */
    public BibleSearchDialog() {
        BorderPane mainPane = new BorderPane();
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.search.title"));
        getIcons().add(new Image("file:icons/search.png"));
        overlay = new LoadingPane();
        searchField = new TextField();
        bibles = new ComboBox<>();
        bibles.setEditable(false);
        BibleManager.get().registerBibleChangeListener(this);
        updateBibles();
        addToSchedule = new Button(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), new ImageView(new Image("file:icons/tick.png")));
        addToSchedule.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                BibleChapter chap = (BibleChapter) searchResults.getSelectionModel().getSelectedItem().getValue().getParent();
                Bible bib = (Bible) chap.getParent().getParent();
                BiblePassage passage = new BiblePassage(bib.getBibleName(), chap.getBook() + " " + chap.getParent().getNum(), chap.getVerses());
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
            }
        });
        HBox northPanel = new HBox();
        northPanel.getChildren().add(bibles);
        northPanel.getChildren().add(searchField);
        northPanel.getChildren().add(addToSchedule);
        mainPane.setTop(northPanel);
        popupMenu = new BibleSearchPopupMenu();
        ScrollPane scrollPane = new ScrollPane();
        chapterPane = new FlowPane();
        scrollPane.setContent(chapterPane);
        scrollPane.setMinWidth(600);
        searchResults = new BibleSearchTreeView(chapterPane);
        VBox centrePanel = new VBox();
        StackPane searchPane = new StackPane();
        searchPane.getChildren().add(searchResults);
        searchPane.getChildren().add(overlay);
        centrePanel.getChildren().add(searchPane);
        mainPane.setCenter(scrollPane);
        centrePanel.setMinWidth(200);
        mainPane.setLeft(centrePanel);
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
//        searchResults.itemsProperty().get().clear();
        searchField.setText(LabelGrabber.INSTANCE.getLabel("initial.search.text"));
        searchField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t1.booleanValue()) {
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
        if (BibleManager.get().isIndexInit()) {
            searchResults.reset();
            overlay.show();
            final String text = searchField.getText();
            new Thread() {
                public void run() {
                    final BibleChapter[] results = BibleManager.get().getIndex().filter(text, null);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!text.trim().isEmpty()) {
                                for (BibleChapter chapter : results) {
                                    if (bibles.getSelectionModel().getSelectedIndex() == 0 || chapter.getBook().getBible().getName().equals(bibles.getSelectionModel().getSelectedItem())) {
                                        for (BibleVerse verse : chapter.getVerses()) {
                                            if (verse.getText().toLowerCase().contains(text.toLowerCase())) {
                                                searchResults.add(verse);
                                            }
                                        }
                                    }
                                }
                            }
                            overlay.hide();
                        }
                    });
                }
            }.start();
        }
    }

    /**
     * Update the list of bibles on this search dialog.
     */
    @Override
    public final void updateBibles() {
        bibles.itemsProperty().get().clear();
        bibles.itemsProperty().get().add(LabelGrabber.INSTANCE.getLabel("all.text"));
        for (Bible bible : BibleManager.get().getBibles()) {
            bibles.itemsProperty().get().add(bible.getName());
        }
        bibles.getSelectionModel().selectFirst();
    }
}
