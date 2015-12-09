/*
 * This file is part of Quelea, free projection software for churches.
 * 
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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.quelea.services.languages.LabelGrabber;

/**
 * A dialog where the user can browse through the installed bibles.
 * <p/>
 * @author Michael
 */
public class BibleBrowseDialog extends Stage implements BibleChangeListener {

    private ComboBox<Bible> bibles;
    private ListView<BibleBook> books;
    private TextArea bibleText;

    /**
     * Create the bible browse dialog.
     */
    public BibleBrowseDialog() {
        BorderPane mainPane = new BorderPane();
        setTitle(LabelGrabber.INSTANCE.getLabel("bible.browser.title"));
        getIcons().add(new Image("file:icons/bible.png"));

        HBox northPanel = new HBox();
        bibles = new ComboBox<>();
        bibles.setEditable(false);
        bibles.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                updateBooks();
            }
        });
        BibleManager.get().registerBibleChangeListener(this);
        Label selectBibleLabel = new Label(LabelGrabber.INSTANCE.getLabel("bible.heading"));
        selectBibleLabel.setLabelFor(bibles);
        northPanel.getChildren().add(selectBibleLabel);
        northPanel.getChildren().add(bibles);
        northPanel.setSpacing(5);
        BorderPane.setMargin(northPanel, new Insets(0, 5, 5, 5));
        mainPane.setTop(northPanel);

        books = new ListView<>();
        books.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BibleBook>() {

            @Override
            public void changed(ObservableValue<? extends BibleBook> ov, BibleBook t, BibleBook t1) {
                BibleBook book = books.getSelectionModel().getSelectedItem();
                if(book != null) {
                    bibleText.setText(book.getText());
                }
            }
        });
        mainPane.setLeft(books);
        bibleText = new TextArea();
        bibleText.setWrapText(true);
        bibleText.setEditable(false);
        
        mainPane.setCenter(bibleText);
        updateBibles();
        
        setScene(new Scene(mainPane));
    }

    /**
     * Set the current selected bible on this dialog.
     * <p/>
     * @param bible the bible to select.
     */
    public void setBible(Bible bible) {
        bibles.getSelectionModel().select(bible);
    }

    /**
     * Set the current selected book on this dialog.
     * <p/>
     * @param book the book to select.
     */
    public void setBook(BibleBook book) {
        setBible(book.getBible());
        books.getSelectionModel().select(book);
    }

    /**
     * Set the current chapter on this dialog.
     * <p/>
     * @param chapter the chapter to select.
     */
    public void setChapter(BibleChapter chapter) {
        setBook(chapter.getBook());
    }

    /**
     * Update the books based on the bible selection
     */
    public final void updateBooks() {
        int index = books.getSelectionModel().getSelectedIndex();
        Bible currentBible = bibles.getSelectionModel().getSelectedItem();
        books.itemsProperty().get().clear();
        if(currentBible != null) {
            for(BibleBook book : currentBible.getBooks()) {
                books.itemsProperty().get().add(book);
            }
            books.getSelectionModel().select(index);
        }
    }

    /**
     * Update the list of bibles on this dialog.
     */
    @Override
    public final void updateBibles() {
        bibles.itemsProperty().get().clear();
        for(Bible bible : BibleManager.get().getBibles()) {
            bibles.itemsProperty().get().add(bible);
        }
    }

}
