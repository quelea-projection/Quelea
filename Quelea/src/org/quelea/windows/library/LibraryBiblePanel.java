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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleChangeListener;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.bible.ChapterVerseParser;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel used to get bible verses.
 * <p/>
 * @author Michael
 */
public class LibraryBiblePanel extends VBox implements BibleChangeListener {

    private final ComboBox<Bible> bibleSelector;
    private final ComboBox<BibleBook> bookSelector;
    private final TextField passageSelector;
    private final TextArea preview;
    private final Button addToSchedule;
    private final List<BibleVerse> verses;
    private boolean multi;
    private ObservableList<BibleBook> master;

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
                break;
            }
        }
        this.setSpacing(5.0);
        getChildren().add(bibleSelector);
        HBox chapterPanel = new HBox();
        chapterPanel.setSpacing(5.0);
        if (bibleSelector.getItems().isEmpty()) {
            bookSelector = new ComboBox<>();
        } else {
            Bible bible = bibleSelector.selectionModelProperty().get().getSelectedItem();
            if (bible == null) {
                bible = bibleSelector.getItems().get(0);
                bibleSelector.setValue(bibleSelector.getItems().get(0));
            }
            bookSelector = new ComboBox<>(FXCollections.observableArrayList(bible.getBooks()));
            bookSelector.selectionModelProperty().get().select(0);
        }
        chapterPanel.getChildren().add(bookSelector);
        passageSelector = new TextField();
        passageSelector.setPromptText(LabelGrabber.INSTANCE.getLabel("bible.passage.selector.prompt"));
        chapterPanel.getChildren().add(passageSelector);
        passageSelector.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if (!addToSchedule.isDisable()) {
                    addToSchedule.fire();
                    passageSelector.setText("");
                }
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
        addToSchedule.setOnAction((ActionEvent t) -> {
            BiblePassage passage = new BiblePassage(bibleSelector.getSelectionModel().getSelectedItem().getName(), getBibleLocation(), getVerses(), multi);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
        });
        addToSchedule.setDisable(true);
        chapterPanel.getChildren().add(addToSchedule);
        //bottomPane.setBottom(addToSchedule);
        getChildren().add(bottomPane);

        addUpdateListeners();
        bibleSelector.valueProperty().addListener((ObservableValue<? extends Bible> ov, Bible t, Bible t1) -> {
            if (bibleSelector.selectionModelProperty().get().isEmpty()) { //Nothing selected
                return;
            }
            ObservableList<BibleBook> books = FXCollections.observableArrayList(bibleSelector.getSelectionModel().getSelectedItem().getBooks());
            int selectedIndex = bookSelector.getSelectionModel().getSelectedIndex();
            bookSelector.itemsProperty().set(books);
            if (bookSelector.getItems().size() > selectedIndex) {
                bookSelector.getSelectionModel().select(selectedIndex);
            }
            update();
        });

        bookSelector.setOnKeyReleased(new EventHandler<KeyEvent>() {

            String search = "";
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            Runnable task = this::clear;

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.SPACE) {

                    if (!executor.isShutdown()) {
                        executor.shutdownNow();
                    }
                    executor = Executors.newSingleThreadScheduledExecutor();
                    executor.schedule(task, 5, TimeUnit.SECONDS);

                    if (event.getCode() == KeyCode.BACK_SPACE) {
                        if (search.length() > 0) {
                            search = search.substring(0, search.length() - 1);
                        }
                    } else {
                        search += event.getText();
                    }

                    Platform.runLater(() -> {
                        refreshMaster();
                        bookSelector.setItems(master.filtered((BibleBook b) -> b.getBookName().toLowerCase().startsWith(search.toLowerCase()) ||
                                b.getBSName().toLowerCase().startsWith(search.toLowerCase())));
                        bookSelector.getSelectionModel().selectFirst();
                    });

                }
            }

            private void clear() {
                search = "";
                executor.shutdown();
                Platform.runLater(() -> {
                    bookSelector.setItems(master);
                });
            }
        });
        bookSelector.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                Platform.runLater(() -> {
                    refreshMaster();
                    bookSelector.setItems(master);
                });
            }
        });
    }

    // Should be on FX thread at all times
    private void refreshMaster() {
        master = FXCollections.observableArrayList(bibleSelector.getSelectionModel().getSelectedItem().getBooks());
    }

    /**
     * Update the bibles in the panel based on the current bibles the bible
     * manager is aware of.
     */
    @Override
    public void updateBibles() {
        Platform.runLater(() -> {
            ObservableList<Bible> bibles = FXCollections.observableArrayList(BibleManager.get().getBibles());
            bibleSelector.itemsProperty().set(bibles);
            Bible selectedBible = null;
            for (Bible bible : bibles) {
                if (bible.getBibleName().equals(QueleaProperties.get().getDefaultBible())) {
                    selectedBible = bible;
                }
            }
            if (selectedBible == null) {
                bibleSelector.selectionModelProperty().get().selectFirst();
            } else {
                bibleSelector.selectionModelProperty().get().select(selectedBible);
            }
        });
    }

    /**
     * Add the listeners that should call the update() method.
     */
    private void addUpdateListeners() {
        passageSelector.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            update();
        });
        bookSelector.valueProperty().addListener((ObservableValue<? extends BibleBook> ov, BibleBook t, BibleBook t1) -> {
            update();
        });
    }

    /**
     * Update the text in the preview panel based on the contents of the fields.
     */
    private void update() {
        verses.clear();
        String[] sections = passageSelector.getText().split("(;|,)");
        ArrayList<BiblePassage> passages = new ArrayList<>();
        if (passageSelector.getText().isEmpty()) {
            getAddToSchedule().setDisable(true);
        }
        StringBuilder previewText = new StringBuilder();
        multi = (sections.length > 1);
        for (String s : sections) {
            ChapterVerseParser cvp = new ChapterVerseParser(s);
            BibleBook book = bookSelector.selectionModelProperty().get().getSelectedItem();
            if (book != null
                    && book.getChapter(cvp.getFromChapter()) != null
                    && book.getChapter(cvp.getToChapter()) != null
                    && book.getChapter(cvp.getFromChapter()).getVerses() != null) {

                getAddToSchedule().setDisable(false);
                int toVerse = book.getChapter(cvp.getFromChapter()).getVerses().length;
                if ((cvp.getFromChapter() == cvp.getToChapter()) && cvp.getToVerse() >= 0 && cvp.getToVerse() < book.getChapter(cvp.getFromChapter()).getVerses().length) {
                    toVerse = cvp.getToVerse();
                }

                for (int v = cvp.getFromVerse(); v <= toVerse; v++) {
                    BibleVerse verse = book.getChapter(cvp.getFromChapter()).getVerse(v);
                    if (verse != null) {
                        previewText.append(verse.getText()).append(' ');
                        verses.add(verse);
                    }
                }
                for (int c = cvp.getFromChapter() + 1; c < cvp.getToChapter(); c++) {
                    for (BibleVerse verse : book.getChapter(c).getVerses()) {
                        previewText.append(verse.getText()).append(' ');
                        verses.add(verse);
                    }
                }
                if (cvp.getFromChapter() != cvp.getToChapter()) {
                    for (int v = 0; v <= cvp.getToVerse(); v++) {
                        BibleVerse verse = book.getChapter(cvp.getToChapter()).getVerse(v);
                        if (verse != null) {
                            previewText.append(verse.getText()).append(' ');
                            verses.add(verse);
                        }
                    }
                }

            } else {
                getAddToSchedule().setDisable(true);
                return;
            }
        }
        preview.setText(previewText.toString());
        if (previewText.toString().trim().isEmpty()) {
            getAddToSchedule().setDisable(true);
        } else {
            getAddToSchedule().setDisable(false);
        }
    }

    /**
     * Get all the verses currently shown in this panel.
     * <p/>
     * @return all the verses in the current preview
     */
    public BibleVerse[] getVerses() {
        return verses.toArray(new BibleVerse[verses.size()]);
    }

    /**
     * Return the book, chapter and verse numbers as a string.
     * <p/>
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
     * <p/>
     * @return the bible selector used to select the type of bible to use.
     */
    public ComboBox<Bible> getBibleSelector() {
        return bibleSelector;
    }

    /**
     * Get the preview text area.
     * <p/>
     * @return the preview text area.
     */
    public TextArea getPreview() {
        return preview;
    }

    /**
     * Get the add to schedule button.
     * <p/>
     * @return the add to schedule button.
     */
    public Button getAddToSchedule() {
        return addToSchedule;
    }

    /**
     * Get the book selector.
     * <p/>
     * @return the book selector.
     */
    public ComboBox<BibleBook> getBookSelector() {
        return bookSelector;
    }

    /**
     * Get the passage selector.
     * <p/>
     * @return the passage selector.
     */
    public TextField getPassageSelector() {
        return passageSelector;
    }
}
