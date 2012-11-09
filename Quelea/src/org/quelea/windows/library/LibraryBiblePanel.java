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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.quelea.QueleaApp;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleBook;
import org.quelea.bible.BibleChangeListener;
import org.quelea.bible.BibleManager;
import org.quelea.bible.BibleVerse;
import org.quelea.bible.ChapterVerseParser;
import org.quelea.displayable.BiblePassage;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

/**
 * The panel used to get bible verses.
 * @author Michael
 */
public class LibraryBiblePanel extends VBox implements BibleChangeListener {

    private final ComboBox<Bible> bibleSelector;
    private final ComboBox<BibleBook> bookSelector;
    private final TextField passageSelector;
    private final TextArea preview;
    private final Button addToSchedule;
    private final List<BibleVerse> verses;

    /**
     * Create and populate a new library bible panel.
     */
    public LibraryBiblePanel() {
        verses = new ArrayList<>();
        BibleManager.get().registerBibleChangeListener(this);
        bibleSelector = new ComboBox<>(FXCollections.observableArrayList(BibleManager.get().getBibles()));
        String selectedBibleName = QueleaProperties.get().getDefaultBible();
        for (int i = 0; i < bibleSelector.itemsProperty().get().size(); i++) {
            Bible bible = bibleSelector.itemsProperty().get().get(i);
            if (bible.getName().equals(selectedBibleName)) {
                bibleSelector.selectionModelProperty().get().select(i);
            }
        }
        bibleSelector.selectionModelProperty().get().select(0);
        getChildren().add(bibleSelector);
        HBox chapterPanel = new HBox();
        bookSelector = new ComboBox<>(FXCollections.observableArrayList(bibleSelector.selectionModelProperty().get().getSelectedItem().getBooks()));
        chapterPanel.getChildren().add(bookSelector);
        passageSelector = new TextField();
        chapterPanel.getChildren().add(passageSelector);
        passageSelector.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                addToSchedule.fire();
                passageSelector.setText("");
            }
        });
        getChildren().add(chapterPanel);
        preview = new TextArea();
        preview.setEditable(false);
        preview.setWrapText(true);
        BorderPane bottomPane = new BorderPane();
        VBox.setVgrow(bottomPane, Priority.SOMETIMES);
        bottomPane.setCenter(preview);
        addToSchedule = new Button(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), new ImageView(new Image("file:icons/tick.png")));
        addToSchedule.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                BiblePassage passage = new BiblePassage(bibleSelector.getSelectionModel().getSelectedItem().getName(), getBibleLocation(), getVerses());
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
            }
        });
        addToSchedule.setDisable(true);
        bottomPane.setBottom(addToSchedule);
        getChildren().add(bottomPane);

        addUpdateListeners();
        bibleSelector.selectionModelProperty().addListener(new ChangeListener<SingleSelectionModel<Bible>>() {

            @Override
            public void changed(ObservableValue<? extends SingleSelectionModel<Bible>> ov, SingleSelectionModel<Bible> t, SingleSelectionModel<Bible> t1) {
                if(bibleSelector.selectionModelProperty().get().isEmpty()) { //Nothing selected
                    return;
                }
                ObservableList<BibleBook> books = FXCollections.observableArrayList(bibleSelector.selectionModelProperty().get().getSelectedItem().getBooks());
                bookSelector.itemsProperty().set(books);
                update();
            }
        });

//        chapterPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
//        bibleSelector.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, bookSelector.getHeight()));
//        addPanel.setMaximumSize(new Dimension(chapterPanel.getMaximumSize().width, addToSchedule.getHeight()));
    }

    /**
     * Update the bibles in the panel based on the current bibles the bible
     * manager is aware of.
     */
    @Override
    public void updateBibles() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                ObservableList<Bible> bibles = FXCollections.observableArrayList(BibleManager.get().getBibles());
                bibleSelector.itemsProperty().set(bibles);
            }
        });
    }

    /**
     * Add the listeners that should call the update() method.
     */
    private void addUpdateListeners() {
        passageSelector.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                update();
            }
        });
        bookSelector.selectionModelProperty().addListener(new ChangeListener<SingleSelectionModel<BibleBook>>() {

            @Override
            public void changed(ObservableValue<? extends SingleSelectionModel<BibleBook>> ov, SingleSelectionModel<BibleBook> t, SingleSelectionModel<BibleBook> t1) {
                update();
            }
        });
    }

    /**
     * Update the text in the preview panel based on the contents of the fields.
     */
    private void update() {
        verses.clear();
        ChapterVerseParser cvp = new ChapterVerseParser(passageSelector.getText());
        BibleBook book = bookSelector.selectionModelProperty().get().getSelectedItem();
        if (book == null || book.getChapter(cvp.getFromChapter()) == null
                || book.getChapter(cvp.getToChapter()) == null
                || passageSelector.getText().isEmpty()) {
            getAddToSchedule().setDisable(true);
            preview.setText("");
            return;
        }
        getAddToSchedule().setDisable(false);
        StringBuilder ret = new StringBuilder();
        int toVerse = book.getChapter(cvp.getFromChapter()).getVerses().length - 1;
        if ((cvp.getFromChapter() == cvp.getToChapter()) && cvp.getToVerse() >= 0 && cvp.getToVerse() < book.getChapter(cvp.getFromChapter()).getVerses().length) {
            toVerse = cvp.getToVerse();
        }

        for (int v = cvp.getFromVerse(); v <= toVerse; v++) {
            BibleVerse verse = book.getChapter(cvp.getFromChapter()).getVerse(v);
            ret.append(verse.getText()).append(' ');
            verses.add(verse);
        }
        for (int c = cvp.getFromChapter() + 1; c < cvp.getToChapter(); c++) {
            for (BibleVerse verse : book.getChapter(c).getVerses()) {
                ret.append(verse.getText()).append(' ');
                verses.add(verse);
            }
        }
        if (cvp.getFromChapter() != cvp.getToChapter()) {
            for (int v = 0; v <= cvp.getToVerse(); v++) {
                BibleVerse verse = book.getChapter(cvp.getToChapter()).getVerse(v);
                if (verse != null) {
                    ret.append(verse.getText()).append(' ');
                    verses.add(verse);
                }
            }
        }
        preview.setText(ret.toString());
    }

    /**
     * Get all the verses currently shown in this panel.
     * @return all the verses in the current preview
     */
    public BibleVerse[] getVerses() {
        return verses.toArray(new BibleVerse[verses.size()]);
    }

    /**
     * Return the book, chapter and verse numbers as a string.
     * @return the location of the current passage.
     */
    public String getBibleLocation() {
        StringBuilder ret = new StringBuilder();
        ret.append(bookSelector.selectionModelProperty().get().getSelectedItem()).append(" ");
        ret.append(passageSelector.getText());
        return ret.toString();
    }

    /**
     * Get the bible selector used to select the type of bible to use.
     * @return the bible selector used to select the type of bible to use.
     */
    public ComboBox<Bible> getBibleSelector() {
        return bibleSelector;
    }

    /**
     * Get the preview text area.
     * @return the preview text area.
     */
    public TextArea getPreview() {
        return preview;
    }

    /**
     * Get the add to schedule button.
     * @return the add to schedule button.
     */
    public Button getAddToSchedule() {
        return addToSchedule;
    }

    /**
     * Get the book selector.
     * @return the book selector.
     */
    public ComboBox<BibleBook> getBookSelector() {
        return bookSelector;
    }

    /**
     * Get the passage selector.
     * @return the passage selector.
     */
    public TextField getPassageSelector() {
        return passageSelector;
    }
}
