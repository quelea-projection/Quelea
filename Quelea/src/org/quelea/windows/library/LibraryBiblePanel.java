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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
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
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * The panel used to get bible verses.
 * <p/>
 * @author Michael
 */
public class LibraryBiblePanel extends VBox implements BibleChangeListener {

    private final ComboBox<Bible> bibleSelector;
    private final ComboBox<BibleBook> bookSelector;
    private final TextField passageSelector;
    private final WebView preview;
    private final Button addToSchedule;
    private final List<BibleVerse> verses;
    private boolean multi;
    private ObservableList<BibleBook> master;
    private WebEngine webEngine;
    private ChapterVerseParser cvp;

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
        preview = new WebView();
        webEngine = preview.getEngine();
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
        BorderPane bottomPane = new BorderPane();
        VBox.setVgrow(bottomPane, Priority.SOMETIMES);
        bottomPane.setCenter(preview);
        addToSchedule = new Button(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), new ImageView(new Image("file:icons/tick.png")));
        addToSchedule.setOnAction((ActionEvent t) -> {
            BiblePassage passage = new BiblePassage(bibleSelector.getSelectionModel().getSelectedItem().getName(), getBibleLocation(), getVerses(), multi);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
            passageSelector.setText("");
        });
        addToSchedule.setDisable(true);
        chapterPanel.getChildren().add(addToSchedule);
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
                        bookSelector.setItems(master.filtered((BibleBook b) -> b.getBookName().toLowerCase().startsWith(search.toLowerCase())
                                || b.getBSName().toLowerCase().startsWith(search.toLowerCase())));
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

    public ChapterVerseParser getCVP() {
        return cvp;
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
        String[] sections;
        if (!passageSelector.getText().contains(":") && passageSelector.getText().contains("-")) {
            String[] temp = passageSelector.getText().split("-");
            StringBuilder sb = new StringBuilder("");
            for (int i = Integer.valueOf(temp[0]); i <= Integer.valueOf(temp[1]); i++) {
                sb.append(i).append(",");
            }
            sections = sb.toString().split(",");
        } else {
            sections = passageSelector.getText().split("(;|,)");
        }
        ArrayList<BiblePassage> passages = new ArrayList<>();
        if (passageSelector.getText().isEmpty()) {
            getAddToSchedule().setDisable(true);
            webEngine.loadContent("");
        }
        StringBuilder previewText = new StringBuilder();
        multi = (sections.length > 1);
        previewText.append(getBibleViewHead());

        // Setup JavaScript/Java bridge
        webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", new JavaScriptBridge());
            }
        });

        for (String s : sections) {
            cvp = new ChapterVerseParser(s);
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

                previewText.append("<b><h3><span id=\"").append(cvp.getFromChapter() + 1).append("\">").append(cvp.getFromChapter() + 1).append("</span></h3></b>");
                String oldText = passageSelector.getText();
                for (BibleVerse verse : book.getChapter(cvp.getFromChapter()).getVerses()) {
                    if (verse != null) {
                        // Scroll to selected verse
                        if (!previewText.toString().contains("<body")) {

                            String firstVerse = oldText.replaceAll("((\\d+)(:\\d+)?-?(\\d+)?(;|,))+((\\d+)(:\\d+)?)(-?\\d+)?", "$6");
                            // Remove any non-numeric character in the end
                            if (firstVerse.substring(firstVerse.length() - 1).matches("-|,|;")) {
                                firstVerse = firstVerse.substring(0, firstVerse.length() - 1);
                            }
                            // Find the last number entered for passages separated with a hyphen
                            String lastVerse = "";
                            if (firstVerse.contains("-") && firstVerse.contains(":")) {
                                lastVerse = firstVerse.substring(0, firstVerse.indexOf(":") + 1) + firstVerse.substring(firstVerse.indexOf("-") + 1);
                                firstVerse = firstVerse.substring(0, firstVerse.indexOf("-"));
                            }
                            // Delete the last character if it is a colon
                            if (firstVerse.length() > 1 && firstVerse.substring(firstVerse.length() - 1).contains(":")) {
                                firstVerse = firstVerse.replaceAll(":", "");
                            }

                            // Scroll so that the most recent verse entered always is visible
                            if (lastVerse.length() > 0) {
                                previewText.append("    <body onload=\"scrollToBottom('").append(lastVerse).append("')\">");
                            } else {
                                previewText.append("    <body onload=\"scrollTo('").append(firstVerse).append("')\">");
                            }
                        }

                        // Only add and mark the selected verses but load the others from the chapter as well
                        String id = (cvp.getFromChapter() + 1) + ":" + verse.getNum();
                        if ((verse.getNum() >= cvp.getFromVerse() && verse.getNum() <= toVerse) || cvp.getFromVerse() == 0) {
                            verses.add(verse);
                            previewText.append("<mark>");
                            previewText.append("<span onclick=\"java.send('").append(verse.getNum()).append("')\" id=\"").append(id).append("\"><sup>").append(verse.getNum()).append("</sup>").append(' ').append(verse.getText()).append(' ').append("</span>");
                            previewText.append("</mark>");
                        } else {
                            previewText.append("<span onclick=\"java.send('").append(verse.getNum()).append("')\" id=\"").append(id).append("\"><sup>").append(verse.getNum()).append("</sup>").append(' ').append(verse.getText()).append(' ').append("</span>");
                        }
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
                webEngine.loadContent("");
                return;
            }
        }
        if (previewText.toString().trim().isEmpty()) {
            getAddToSchedule().setDisable(true);
        } else {
            getAddToSchedule().setDisable(false);
        }

        previewText.append("    </body>\n"
                + "</html>");

        webEngine.loadContent(previewText.toString());
    }

    private String getBibleViewHead() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Bible Browser</title>\n"
                + "        <meta charset=\"utf-8\">\n"
                + "        <meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n"
                + "        <meta name=\"mobile-web-app-capable\" content=\"yes\">\n"
                + "     <style>\n"
                + "         mark {\n"
                + "         background-color: #D7D7D7;\n"
                + "         color: black;\n"
                + "         }\n"
                + "         h3 {\n"
                + "             display: block;\n"
                + "             font-size: 1.67em;\n"
                + "             margin-top: 0.67em;\n"
                + "             margin-bottom: 0.0em;\n"
                + "             margin-left: 0;\n"
                + "             margin-right: 0;\n"
                + "         }"
                + "     </style>\n"
                + "     <script>\n"
                + "     function scrollTo(eleID) {\n"
                + "         var e = document.getElementById(eleID);\n"
                + "         if (!!e && e.scrollIntoView) {\n"
                + "             e.scrollIntoView();\n"
                + "         }\n"
                + "        }\n"
                + "     function scrollToBottom(elementID) {\n"
                + "         var el = document.getElementById(elementID);\n"
                + "         if (!!el && el.scrollIntoView) {\n"
                + "			el.scrollIntoView(false);\n"
                + "         }\n"
                + "     }\n"
                + "     </script>\n"
                + "    </head>\n";
    }

    /**
     * Class to receive the clicked verses in the WebView
     */
    public class JavaScriptBridge {

        public void send(String verse) {
            String oldText = passageSelector.getText();
            int verseNum = Integer.parseInt(verse);
            String chapterNum;
            if (!oldText.contains(":")) {
                chapterNum = oldText;
            } else {
                chapterNum = oldText.substring(0, oldText.lastIndexOf(":"));
            }
            ChapterVerseParser cvp = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getBiblePanel().getCVP();
            if (cvp != null) {
                final String chapter = chapterNum;
                final int fromVerse = cvp.getFromVerse();
                Platform.runLater(() -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(chapter).append(":");
                    if (fromVerse == 0) {
                        sb.append(verseNum);
                    } else if (verseNum > cvp.getToVerse()) {
                        sb.append(fromVerse).append("-").append(verseNum);
                    } else {
                        sb.append(verseNum).append("-").append(cvp.getToVerse());
                    }
                    passageSelector.setText(sb.toString());
                });

            }

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
    public WebView getPreview() {
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
