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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleChangeListener;
import org.quelea.data.bible.BibleChapter;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleReferenceParser;
import org.quelea.data.bible.BibleReferenceParser.Lookup;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.SelectBibleVersionActionHandler;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;

/**
 * The panel used to get bible verses. A reference is typed in the search field,
 * which works the same way as the universal search, and the books, chapters and
 * verses below follow what is typed. Picking a book, chapter or verse there
 * writes the reference in the search field, so the two always agree.
 * <p/>
 * Whatever is being chosen gets the room: the books are a grid until a book is
 * chosen, then they shrink to a narrow column and the chapters are a grid, and
 * once a chapter is chosen the chapters shrink as well and the verses are
 * shown with their text.
 *
 * @author Michael
 */
public class LibraryBiblePanel extends VBox implements BibleChangeListener {

    private static final int BOOK_COLUMNS = 7;
    private static final int CHAPTER_COLUMNS = 8;
    private static final int LAST_OLD_TESTAMENT_BOOK = 39;
    private static final int LAST_NEW_TESTAMENT_BOOK = 66;
    /**
     * The first book number of each group of books (law, history, poetry,
     * prophets, gospels, Acts, letters and Revelation), with every other group
     * tinted so the grid is easier to scan.
     */
    private static final int[] GROUP_STARTS = {1, 6, 18, 23, 40, 44, 45, 66};
    private static final String[] GROUP_TINTS = {"bible-genre-a", null, "bible-genre-b", null,
        "bible-genre-a", null, "bible-genre-b", null};
    private static final double BOOK_RAIL_WIDTH = 58;
    private static final double CHAPTER_RAIL_WIDTH = 44;
    private static final double CELL_HEIGHT = 32;
    private static final int MAX_SHORT_NAME_LENGTH = 6;
    private static final Pattern NUMBERED_NAME = Pattern.compile("^(\\d+\\.?)\\s*(\\p{L}+)");
    private static final String CHOSEN = "bible-chosen";
    private static final String MATCH = "bible-match";
    private static final String FIRST_MATCH = "bible-first-match";
    private static final String DIMMED = "bible-dimmed";

    private enum Expanded {
        NONE, BOOKS, CHAPTERS
    }

    private final ObservableList<Bible> versions = FXCollections.observableArrayList();
    private final FlowPane versionPane = new FlowPane(6, 6);
    private final MenuButton addVersionButton;
    private final TextField searchField = new TextField();
    private final Button addButton;
    private final HBox browser = new HBox();
    private final Label noBiblesLabel;

    private final VBox bookGridContent = new VBox();
    private final ScrollPane bookGrid = scrollPane(bookGridContent);
    private final Label gridNote = new Label();
    private final VBox bookRailContent = new VBox();
    private final ScrollPane bookRailScroll = railScrollPane(bookRailContent);
    private final VBox bookRail = new VBox();
    private final VBox chapterGridContent = new VBox();
    private final ScrollPane chapterGrid = scrollPane(chapterGridContent);
    private final Label chapterGridTitle = new Label();
    private final GridPane chapterCells = grid(CHAPTER_COLUMNS);
    private final Label chapterGridHint = new Label();
    private final VBox chapterRailContent = new VBox();
    private final ScrollPane chapterRailScroll = railScrollPane(chapterRailContent);
    private final VBox chapterRail = new VBox();
    private final ListView<BibleVerse> verseList = new ListView<>();

    private final Label selectionTitle = new Label();
    private final Label selectionDetail = new Label();
    private final Label selectionHint = new Label();
    private final VBox warningBox = new VBox(8);

    private final Map<BibleBook, Button> bookCells = new IdentityHashMap<>();
    private final Map<BibleBook, Button> bookRailItems = new IdentityHashMap<>();
    private final List<Button> chapterGridItems = new ArrayList<>();
    private final List<Button> chapterRailItems = new ArrayList<>();
    private final Set<BibleVerse> chosenVerses = Collections.newSetFromMap(new IdentityHashMap<>());

    private Bible shownBible;
    private BibleBook shownBook;
    private BibleChapter shownChapter;
    private Lookup lookup = Lookup.NONE;
    private boolean typingBook;
    private Expanded expanded = Expanded.NONE;
    private String scrolledTo;

    /**
     * Create and populate a new library bible panel.
     */
    public LibraryBiblePanel() {
        getStyleClass().add("bible-panel");
        BibleManager.get().registerBibleChangeListener(this);

        versionPane.setAlignment(Pos.CENTER_LEFT);
        versionPane.setStyle("-fx-padding: 12 14 8 14;");
        addVersionButton = new MenuButton(LabelGrabber.INSTANCE.getLabel("bible.add.version.button"), QueleaIcon.ADD.create(12));
        addVersionButton.getStyleClass().add("bible-add-version");
        addVersionButton.setOnShowing(event -> fillVersionMenu());

        searchField.setPromptText(LabelGrabber.INSTANCE.getLabel("bible.find.passage.prompt"));
        searchField.addEventFilter(KeyEvent.KEY_PRESSED, this::searchKeyPressed);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        addButton = new Button(LabelGrabber.INSTANCE.getLabel("bible.add.passage.button"), QueleaIcon.ADD.create(12));
        addButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text")));
        StyleManager.markPrimaryButton(addButton);
        addButton.setMinWidth(Region.USE_PREF_SIZE);
        addButton.setOnAction(event -> addToSchedule());
        HBox searchRow = new HBox(6, searchField, addButton);
        searchRow.setStyle("-fx-padding: 0 14 12 14;");

        gridNote.getStyleClass().add("bible-grid-note");
        gridNote.setWrapText(true);
        bookGridContent.getStyleClass().add("bible-grid-pane");
        // The heads stay put while the books and chapters under them scroll
        bookRail.getChildren().setAll(railHead("bible.all.books.button", "bible.show.all.books", Expanded.BOOKS), bookRailScroll);
        bookRail.getStyleClass().addAll("bible-rail", "bible-book-rail");
        fixWidth(bookRail, BOOK_RAIL_WIDTH);
        chapterRail.getChildren().setAll(railHead("bible.all.chapters.button", "bible.show.all.chapters", Expanded.CHAPTERS), chapterRailScroll);
        chapterRail.getStyleClass().add("bible-rail");
        fixWidth(chapterRail, CHAPTER_RAIL_WIDTH);
        VBox.setVgrow(bookRailScroll, Priority.ALWAYS);
        VBox.setVgrow(chapterRailScroll, Priority.ALWAYS);
        chapterGridTitle.getStyleClass().add("bible-grid-heading");
        chapterGridHint.getStyleClass().add("bible-grid-note");
        chapterGridHint.setWrapText(true);
        chapterGridContent.getStyleClass().add("bible-grid-pane");
        chapterGridContent.getChildren().addAll(chapterGridTitle, chapterCells, chapterGridHint);
        verseList.getStyleClass().add("bible-verse-list");
        verseList.setCellFactory(list -> new VerseCell());
        verseList.setFocusTraversable(false);
        HBox.setHgrow(bookGrid, Priority.ALWAYS);
        HBox.setHgrow(chapterGrid, Priority.ALWAYS);
        HBox.setHgrow(verseList, Priority.ALWAYS);
        noBiblesLabel = new Label(LabelGrabber.INSTANCE.getLabel("bible.no.bibles"));
        noBiblesLabel.setWrapText(true);
        noBiblesLabel.getStyleClass().add("muted-label");
        noBiblesLabel.setStyle("-fx-padding: 20 14 20 14;");
        browser.getStyleClass().add("bible-browser");
        VBox.setVgrow(browser, Priority.ALWAYS);

        selectionTitle.getStyleClass().add("bible-selection-title");
        selectionDetail.getStyleClass().add("bible-selection-detail");
        selectionDetail.setTextOverrun(OverrunStyle.ELLIPSIS);
        selectionHint.getStyleClass().add("bible-selection-hint");
        selectionHint.setMinWidth(Region.USE_PREF_SIZE);
        VBox selectionText = new VBox(selectionTitle, selectionDetail);
        selectionText.setMinWidth(0);
        HBox.setHgrow(selectionText, Priority.ALWAYS);
        HBox selectionRow = new HBox(10, selectionText, selectionHint);
        selectionRow.setAlignment(Pos.CENTER_LEFT);
        VBox selectionBar = new VBox(selectionRow, warningBox);
        selectionBar.getStyleClass().add("bible-selection-bar");

        getChildren().addAll(versionPane, searchRow, browser, selectionBar);

        versions.addListener((ListChangeListener<Bible>) change -> versionsChanged());
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            expanded = Expanded.NONE;
            refresh();
        });
        resetVersions();
    }

    /**
     * Show the bible versions set in the options again: the default bible,
     * followed by the other versions if they're always used.
     */
    public void resetVersions() {
        Bible bible = BibleManager.get().getBibleFromName(QueleaProperties.get().getDefaultBible());
        if (bible == null && !(BibleManager.get().getBibles().length == 0)) {
            bible = BibleManager.get().getBibles()[0];
        }
        versions.setAll(bible == null ? List.of() : BibleManager.get().getDefaultVersions(bible));
    }

    /**
     * Update the bibles in the panel based on the current bibles the bible
     * manager is aware of.
     */
    @Override
    public void updateBibles() {
        Platform.runLater(() -> {
            // The bibles are new objects after a reload, so find them by name
            List<Bible> kept = new ArrayList<>();
            for (Bible version : versions) {
                Bible bible = BibleManager.get().getBibleFromName(version.getName());
                if (bible != null) {
                    kept.add(bible);
                }
            }
            shownBible = null;
            if (kept.isEmpty()) {
                resetVersions();
            } else {
                versions.setAll(kept);
            }
        });
    }

    /**
     * Get the bible that passages are looked up in, the first of the versions.
     *
     * @return the bible, or null if there are no bibles.
     */
    public Bible getPrimaryBible() {
        return versions.isEmpty() ? null : versions.getFirst();
    }

    /**
     * Get the versions passages are shown in.
     *
     * @return the versions, in display order.
     */
    public List<Bible> getVersions() {
        return List.copyOf(versions);
    }

    /**
     * Get the field the reference is typed in.
     *
     * @return the search field.
     */
    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Get the add to schedule button.
     *
     * @return the add to schedule button.
     */
    public Button getAddToSchedule() {
        return addButton;
    }

    private void versionsChanged() {
        Bible primary = getPrimaryBible();
        buildChips();
        if (primary != shownBible && primary != null && lookup.book() != null && !typingBook) {
            // Keep the passage, with the book's name in the new bible
            BibleBook book = primary.getBookByNumber(lookup.book().getBookNumber());
            if (book != null) {
                String text = searchField.getText();
                String rest = lookup.passage().isEmpty() ? (text.endsWith(" ") ? " " : "") : " " + lookup.passage();
                buildBooks(primary);
                searchField.setText(book.getBookName() + rest);
            }
        }
        refresh();
    }

    private void buildChips() {
        versionPane.getChildren().clear();
        for (int i = 0; i < versions.size(); i++) {
            Bible bible = versions.get(i);
            Label number = new Label(Integer.toString(i + 1));
            number.getStyleClass().add("bible-version-number");
            Label name = new Label(getChipName(bible));
            if (!name.getText().equals(bible.getBibleName())) {
                name.setTooltip(new Tooltip(bible.getBibleName()));
            }
            HBox chip = new HBox(number, name);
            chip.getStyleClass().add("bible-version-chip");
            if (versions.size() > 1) {
                Button remove = new Button(null, QueleaIcon.CLOSE.create(12));
                remove.getStyleClass().add("bible-version-remove");
                String label = LabelGrabber.INSTANCE.getLabel("bible.remove.version.tooltip").replace("$1", bible.getBibleName());
                remove.setTooltip(new Tooltip(label));
                remove.setAccessibleText(label);
                remove.setOnAction(event -> versions.remove(bible));
                chip.getChildren().add(remove);
            } else {
                chip.setStyle("-fx-padding: 3 10 3 6;");
            }
            versionPane.getChildren().add(chip);
        }
        if (BibleManager.get().getBibles().length > versions.size()) {
            versionPane.getChildren().add(addVersionButton);
        }
    }

    /**
     * Get the name to show on a version's chip. With several versions, long
     * names such as "King James Version" are shortened to "KJV" so the chips
     * fit next to each other.
     */
    private String getChipName(Bible bible) {
        String name = bible.getBibleName();
        String[] words = name.trim().split("\\s+");
        if (versions.size() < 2 || words.length < 3 || name.contains("(")) {
            return name;
        }
        return Utils.getAbbreviation(name.trim());
    }

    private void fillVersionMenu() {
        addVersionButton.getItems().clear();
        for (Bible bible : BibleManager.get().getBibles()) {
            if (!versions.contains(bible)) {
                MenuItem item = new MenuItem(bible.getBibleName());
                item.setOnAction(event -> versions.add(bible));
                addVersionButton.getItems().add(item);
            }
        }
    }

    private void searchKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case TAB -> {
                if (typingBook && !event.isShiftDown()) {
                    pickBook(lookup.books().getFirst());
                    event.consume();
                }
            }
            case ENTER -> {
                if (typingBook) {
                    pickBook(lookup.books().getFirst());
                } else if (!addButton.isDisable()) {
                    addToSchedule();
                }
                event.consume();
            }
            case ESCAPE -> {
                if (expanded != Expanded.NONE) {
                    expanded = Expanded.NONE;
                    refresh();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    private void addToSchedule() {
        if (lookup.reference() == null || typingBook || versions.isEmpty()) {
            return;
        }
        BiblePassage passage = SelectBibleVersionActionHandler.createPassage(lookup.reference(), List.copyOf(versions));
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
        // Stay in the book, as the next passage is often from the same one
        searchField.setText(lookup.reference().book().getBookName() + " ");
    }

    private void pickBook(BibleBook book) {
        if (expanded == Expanded.BOOKS && book == lookup.book()) {
            expanded = Expanded.NONE;
            refresh();
        } else {
            searchField.setText(book.getBookName() + " ");
        }
        searchField.requestFocus();
        searchField.end();
    }

    private void pickChapter(int chapter) {
        if (expanded == Expanded.CHAPTERS && chapter == lookup.chapter()) {
            expanded = Expanded.NONE;
            refresh();
        } else {
            searchField.setText(lookup.book().getBookName() + " " + chapter);
        }
        searchField.requestFocus();
        searchField.end();
    }

    private void pickVerse(BibleVerse verse, boolean range) {
        String start = lookup.book().getBookName() + " " + lookup.chapter() + ":";
        BibleVerse anchor = chosenVerses.stream().min(Comparator.comparingInt(BibleVerse::getNum)).orElse(null);
        if (range && anchor != null && chosenVerses.size() < shownChapter.getVerses().length) {
            int from = Math.min(anchor.getNum(), verse.getNum());
            int to = Math.max(anchor.getNum(), verse.getNum());
            searchField.setText(start + from + (to == from ? "" : "-" + to));
        } else {
            searchField.setText(start + verse.getNum());
        }
        searchField.requestFocus();
        searchField.end();
    }

    private void expand(Expanded what) {
        expanded = what;
        refresh();
    }

    /**
     * Bring the books, chapters, verses and the selection up to date with the
     * search field and the versions.
     */
    private void refresh() {
        Bible primary = getPrimaryBible();
        searchField.setDisable(primary == null);
        if (primary == null) {
            lookup = Lookup.NONE;
            typingBook = false;
            show(noBiblesLabel);
            updateSelection(null);
            return;
        }
        if (primary != shownBible) {
            buildBooks(primary);
        }
        String text = searchField.getText();
        lookup = BibleReferenceParser.lookup(text, primary);
        typingBook = lookup.book() != null && lookup.passage().isEmpty() && !text.endsWith(" ");
        BibleBook book = lookup.book();
        if (book != shownBook) {
            buildChapters(book);
        }
        BibleChapter chapter = book == null || lookup.chapter() == 0 ? null : book.getChapter(lookup.chapter() - 1);
        chosenVerses.clear();
        if (lookup.reference() != null && chapter != null) {
            for (BibleVerse verse : lookup.reference().verses()) {
                if (verse.getChapter() == chapter) {
                    chosenVerses.add(verse);
                }
            }
        }

        boolean showBookGrid = book == null || typingBook || expanded == Expanded.BOOKS;
        boolean showChapterGrid = !showBookGrid && (chapter == null || expanded == Expanded.CHAPTERS);
        updateBooks(text);
        updateChapters();
        if (showBookGrid) {
            show(bookGrid);
        } else if (showChapterGrid) {
            chapterGridTitle.setText(LabelGrabber.INSTANCE.getLabel("bible.chapter.count")
                    .replace("$1", book.getBookName()).replace("$2", Integer.toString(book.getChapters().length)));
            chapterGridHint.setText(expanded == Expanded.CHAPTERS
                    ? LabelGrabber.INSTANCE.getLabel("bible.pick.chapter.again").replace("$1", Integer.toString(lookup.chapter()))
                    : LabelGrabber.INSTANCE.getLabel("bible.keep.typing.hint").replace("$1", book.getBookName().toLowerCase()));
            show(bookRail, chapterGrid);
        } else {
            show(bookRail, chapterRail, verseList);
        }
        if (chapter != shownChapter) {
            shownChapter = chapter;
            List<BibleVerse> verses = new ArrayList<>();
            if (chapter != null) {
                Arrays.stream(chapter.getVerses())
                        .filter(verse -> verse != null)
                        .sorted(Comparator.comparingInt(BibleVerse::getNum))
                        .forEach(verses::add);
            }
            verseList.getItems().setAll(verses);
        }
        verseList.refresh();
        updateSelection(chapter);
        String where = book == null ? "" : book.getBookNumber() + "|" + lookup.chapter() + "|" + showBookGrid + showChapterGrid
                + "|" + chosenVerses.stream().mapToInt(BibleVerse::getNum).min().orElse(0);
        if (!where.equals(scrolledTo)) {
            scrolledTo = where;
            Platform.runLater(this::scrollToChosen);
        }
    }

    private void show(Node... nodes) {
        if (!browser.getChildren().equals(List.of(nodes))) {
            browser.getChildren().setAll(nodes);
        }
    }

    private void buildBooks(Bible bible) {
        shownBible = bible;
        shownBook = null;
        bookCells.clear();
        bookRailItems.clear();
        bookGridContent.getChildren().setAll(gridNote);
        bookRailContent.getChildren().clear();
        String[][] sections = {
            {"bible.old.testament", "bible.old.testament.short"},
            {"bible.new.testament", "bible.new.testament.short"},
            {"bible.other.books", "bible.other.books.short"}};
        for (int section = 0; section < sections.length; section++) {
            List<BibleBook> books = new ArrayList<>();
            for (BibleBook book : bible.getBooks()) {
                if (getSection(book) == section) {
                    books.add(book);
                }
            }
            if (books.isEmpty()) {
                continue;
            }
            Label heading = new Label(LabelGrabber.INSTANCE.getLabel(sections[section][0]));
            heading.getStyleClass().add("bible-grid-heading");
            GridPane cells = grid(BOOK_COLUMNS);
            Label railLabel = new Label(LabelGrabber.INSTANCE.getLabel(sections[section][1]));
            railLabel.getStyleClass().add("bible-rail-label");
            railLabel.setMaxWidth(Double.MAX_VALUE);
            railLabel.setAlignment(Pos.CENTER);
            bookRailContent.getChildren().add(railLabel);
            for (int i = 0; i < books.size(); i++) {
                BibleBook book = books.get(i);
                String tooltip = book.getBookName() + " · " + book.getChapters().length;
                Button cell = cell(getShortName(book), () -> pickBook(book));
                cell.setTooltip(new Tooltip(tooltip));
                cell.setAccessibleText(book.getBookName());
                String tint = getTint(book);
                if (tint != null) {
                    cell.getStyleClass().add(tint);
                }
                cells.add(cell, i % BOOK_COLUMNS, i / BOOK_COLUMNS);
                bookCells.put(book, cell);

                Button item = railItem(getShortName(book), () -> pickBook(book));
                item.setTooltip(new Tooltip(book.getBookName()));
                item.setAccessibleText(book.getBookName());
                if (i > 0 && isGroupStart(book)) {
                    item.getStyleClass().add("bible-group-start");
                }
                bookRailContent.getChildren().add(item);
                bookRailItems.put(book, item);
            }
            VBox group = new VBox(6, heading, cells);
            bookGridContent.getChildren().add(group);
        }
        bookGridContent.getChildren().add(legend());
    }

    private void buildChapters(BibleBook book) {
        shownBook = book;
        chapterCells.getChildren().clear();
        chapterGridItems.clear();
        chapterRailItems.clear();
        chapterRailContent.getChildren().clear();
        if (book == null) {
            return;
        }
        for (int i = 0; i < book.getChapters().length; i++) {
            int number = i + 1;
            Button cell = cell(Integer.toString(number), () -> pickChapter(number));
            cell.setAccessibleText(book.getBookName() + " " + number);
            chapterCells.add(cell, i % CHAPTER_COLUMNS, i / CHAPTER_COLUMNS);
            chapterGridItems.add(cell);
            Button item = railItem(Integer.toString(number), () -> pickChapter(number));
            item.setAccessibleText(book.getBookName() + " " + number);
            chapterRailContent.getChildren().add(item);
            chapterRailItems.add(item);
        }
    }

    private void updateBooks(String text) {
        List<BibleBook> matches = typingBook ? lookup.books() : List.of();
        for (Map.Entry<BibleBook, Button> entry : bookCells.entrySet()) {
            BibleBook book = entry.getKey();
            int match = matches.indexOf(book);
            toggleStyle(entry.getValue(), CHOSEN, !typingBook && book == lookup.book());
            toggleStyle(entry.getValue(), MATCH, match >= 0);
            toggleStyle(entry.getValue(), FIRST_MATCH, match == 0);
            toggleStyle(entry.getValue(), DIMMED, typingBook && match < 0);
        }
        for (Map.Entry<BibleBook, Button> entry : bookRailItems.entrySet()) {
            toggleStyle(entry.getValue(), CHOSEN, entry.getKey() == lookup.book());
        }
        String note = null;
        if (typingBook) {
            String first = lookup.books().getFirst().getBookName();
            note = lookup.books().size() == 1
                    ? LabelGrabber.INSTANCE.getLabel("bible.book.matching").replace("$1", first)
                    : LabelGrabber.INSTANCE.getLabel("bible.books.matching").replace("$1", Integer.toString(lookup.books().size())).replace("$2", first);
        } else if (expanded == Expanded.BOOKS && lookup.book() != null) {
            note = LabelGrabber.INSTANCE.getLabel("bible.pick.book.again").replace("$1", lookup.book().getBookName());
        } else if (lookup.book() == null && !text.isBlank()) {
            note = LabelGrabber.INSTANCE.getLabel("bible.no.book.matching").replace("$1", text.trim());
        }
        gridNote.setText(note);
        gridNote.setVisible(note != null);
        gridNote.setManaged(note != null);
    }

    private void updateChapters() {
        for (int i = 0; i < chapterGridItems.size(); i++) {
            boolean chosen = i + 1 == lookup.chapter();
            toggleStyle(chapterGridItems.get(i), CHOSEN, chosen);
            toggleStyle(chapterRailItems.get(i), CHOSEN, chosen);
        }
    }

    private void updateSelection(BibleChapter chapter) {
        String title = LabelGrabber.INSTANCE.getLabel("bible.no.passage.chosen");
        String detail = LabelGrabber.INSTANCE.getLabel("bible.type.or.pick.hint");
        String hint = "";
        warningBox.getChildren().clear();
        if (lookup.reference() != null && !typingBook) {
            title = lookup.reference().getLocation();
            detail = versions.stream().map(Bible::getBibleName).collect(Collectors.joining(" + "));
            hint = chapter == null ? "" : LabelGrabber.INSTANCE.getLabel("bible.range.hint");
            if (chapter != null) {
                addVerseCountWarnings(chapter);
            }
        } else if (chapter != null) {
            title = lookup.book().getBookName() + " " + lookup.chapter();
            detail = LabelGrabber.INSTANCE.getLabel("bible.pick.verses.hint");
        } else if (lookup.book() != null && !typingBook) {
            title = lookup.book().getBookName();
            detail = LabelGrabber.INSTANCE.getLabel("bible.pick.chapter.hint");
        }
        selectionTitle.setText(title);
        selectionDetail.setText(detail);
        selectionHint.setText(hint);
        warningBox.setVisible(!warningBox.getChildren().isEmpty());
        warningBox.setManaged(!warningBox.getChildren().isEmpty());
        addButton.setDisable(lookup.reference() == null || typingBook);
    }

    /**
     * Warn when another version has a different number of verses in the
     * chapter, as its verse numbers may then not line up with the first one's.
     */
    private void addVerseCountWarnings(BibleChapter chapter) {
        Bible primary = getPrimaryBible();
        int count = chapter.getVerses().length;
        for (Bible other : versions.subList(1, versions.size())) {
            BibleBook book = other.getBookByNumber(lookup.book().getBookNumber());
            BibleChapter otherChapter = book == null ? null : book.getChapter(lookup.chapter() - 1);
            if (otherChapter == null || otherChapter.getVerses().length == count) {
                continue;
            }
            Label text = new Label(LabelGrabber.INSTANCE.getLabel("bible.verse.count.warning")
                    .replace("$1", other.getBibleName())
                    .replace("$2", Integer.toString(otherChapter.getVerses().length))
                    .replace("$3", lookup.book().getBookName() + " " + lookup.chapter())
                    .replace("$4", primary.getBibleName())
                    .replace("$5", Integer.toString(count)));
            text.setWrapText(true);
            text.setMinHeight(Region.USE_PREF_SIZE);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox warning = new HBox(QueleaIcon.WARNING.create(14), text);
            warning.getStyleClass().add("bible-warning");
            warningBox.getChildren().add(warning);
        }
    }

    private void scrollToChosen() {
        BibleBook book = lookup.book();
        if (book != null) {
            scrollTo(bookRailScroll, bookRailItems.get(book));
            int chapter = lookup.chapter();
            Button item = chapter > 0 && chapter <= chapterRailItems.size() ? chapterRailItems.get(chapter - 1) : null;
            scrollTo(chapterRailScroll, item);
            scrollTo(chapterGrid, chapter > 0 && chapter <= chapterGridItems.size() ? chapterGridItems.get(chapter - 1) : null);
        }
        scrollTo(bookGrid, typingBook ? bookCells.get(lookup.books().getFirst()) : bookCells.get(book));
        int first = verseList.getItems().size();
        for (BibleVerse verse : chosenVerses) {
            first = Math.min(first, verseList.getItems().indexOf(verse));
        }
        verseList.scrollTo(first < verseList.getItems().size() ? Math.max(0, first - 1) : 0);
    }

    /**
     * Scroll so that a node shows, a little way from the top.
     */
    private static void scrollTo(ScrollPane pane, Node node) {
        Node content = pane.getContent();
        if (node == null || pane.getScene() == null) {
            pane.setVvalue(0);
            return;
        }
        pane.applyCss();
        pane.layout();
        Bounds bounds = content.sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        double scrollable = content.getBoundsInLocal().getHeight() - pane.getViewportBounds().getHeight();
        if (scrollable <= 0) {
            pane.setVvalue(0);
            return;
        }
        double top = bounds.getMinY() - CELL_HEIGHT * 1.5;
        pane.setVvalue(Math.max(0, Math.min(1, top / scrollable)));
    }

    private Button railHead(String label, String tooltip, Expanded what) {
        Button head = new Button(LabelGrabber.INSTANCE.getLabel(label), QueleaIcon.GRID.create(14));
        head.getStyleClass().add("bible-rail-head");
        head.setContentDisplay(ContentDisplay.TOP);
        head.setMaxWidth(Double.MAX_VALUE);
        head.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel(tooltip)));
        head.setAccessibleText(LabelGrabber.INSTANCE.getLabel(tooltip));
        head.setOnAction(event -> expand(what));
        return head;
    }

    private HBox legend() {
        HBox legend = new HBox(12);
        String[][] entries = {
            {"bible-genre-a", "bible.genre.law.gospels"},
            {null, "bible.genre.history.prophets"},
            {"bible-genre-b", "bible.genre.poetry.letters"}};
        for (String[] entry : entries) {
            Region swatch = new Region();
            swatch.getStyleClass().add("bible-legend-swatch");
            if (entry[0] != null) {
                swatch.getStyleClass().add(entry[0]);
            }
            Label label = new Label(LabelGrabber.INSTANCE.getLabel(entry[1]), swatch);
            label.getStyleClass().add("bible-legend");
            label.setGraphicTextGap(5);
            legend.getChildren().add(label);
        }
        return legend;
    }

    private static Button cell(String text, Runnable action) {
        Button cell = new Button(text);
        cell.getStyleClass().add("bible-cell");
        cell.setMinWidth(0);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(CELL_HEIGHT);
        cell.setPrefHeight(CELL_HEIGHT);
        cell.setTextOverrun(OverrunStyle.CLIP);
        cell.setOnAction(event -> action.run());
        return cell;
    }

    private static Button railItem(String text, Runnable action) {
        Button item = new Button(text);
        item.getStyleClass().add("bible-rail-item");
        item.setMaxWidth(Double.MAX_VALUE);
        item.setMinWidth(0);
        item.setTextOverrun(OverrunStyle.CLIP);
        item.setFocusTraversable(false);
        item.setOnAction(event -> action.run());
        return item;
    }

    private static GridPane grid(int columns) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        for (int i = 0; i < columns; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / columns);
            column.setMinWidth(0);
            grid.getColumnConstraints().add(column);
        }
        return grid;
    }

    private static ScrollPane scrollPane(Node content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return pane;
    }

    /**
     * A scroll pane for a narrow column, without a scroll bar taking up half of
     * it. It still scrolls with the mouse wheel or a trackpad.
     */
    private static ScrollPane railScrollPane(Node content) {
        ScrollPane pane = scrollPane(content);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return pane;
    }

    private static void fixWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    private static void toggleStyle(Node node, String styleClass, boolean on) {
        if (!on) {
            node.getStyleClass().remove(styleClass);
        } else if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * Get the part of the bible a book is in: 0 for the Old Testament, 1 for
     * the New Testament and 2 for anything else.
     */
    static int getSection(BibleBook book) {
        int number = book.getBookNumber();
        if (number >= 1 && number <= LAST_OLD_TESTAMENT_BOOK) {
            return 0;
        }
        return number > LAST_OLD_TESTAMENT_BOOK && number <= LAST_NEW_TESTAMENT_BOOK ? 1 : 2;
    }

    private static int getGroup(BibleBook book) {
        int number = book.getBookNumber();
        if (number < 1 || number > LAST_NEW_TESTAMENT_BOOK) {
            return -1;
        }
        int group = 0;
        while (group + 1 < GROUP_STARTS.length && GROUP_STARTS[group + 1] <= number) {
            group++;
        }
        return group;
    }

    private static boolean isGroupStart(BibleBook book) {
        int group = getGroup(book);
        return group >= 0 && GROUP_STARTS[group] == book.getBookNumber();
    }

    private static String getTint(BibleBook book) {
        int group = getGroup(book);
        return group < 0 ? null : GROUP_TINTS[group];
    }

    /**
     * Get a short name for a book that fits in the grid: the short name from
     * the bible if it has one, otherwise the start of the name.
     *
     * @param book the book.
     * @return the short name, e.g. "Gen" or "1 Sam".
     */
    static String getShortName(BibleBook book) {
        String name = book.getBookName() == null ? "" : book.getBookName().trim();
        String shortName = book.getBSName() == null ? "" : book.getBSName().trim();
        if (!shortName.isEmpty() && !shortName.equals(name) && shortName.length() <= MAX_SHORT_NAME_LENGTH) {
            return shortName;
        }
        if (name.length() <= MAX_SHORT_NAME_LENGTH - 1) {
            return name;
        }
        Matcher numbered = NUMBERED_NAME.matcher(name);
        if (numbered.find()) {
            String word = numbered.group(2);
            return numbered.group(1) + " " + word.substring(0, Math.min(3, word.length()));
        }
        return name.substring(0, 4);
    }

    /**
     * A verse with its text, and when it's chosen, the text in the other
     * versions under it.
     */
    private final class VerseCell extends ListCell<BibleVerse> {

        private final Label number = new Label();
        private final Label text = new Label();
        private final VBox body = new VBox(6, text);
        private final HBox root = new HBox(8, number, body);

        private VerseCell() {
            number.getStyleClass().add("bible-verse-number");
            text.getStyleClass().add("bible-verse-text");
            text.setMinWidth(0);
            text.setTextOverrun(OverrunStyle.ELLIPSIS);
            body.setMinWidth(0);
            HBox.setHgrow(body, Priority.ALWAYS);
            root.setAlignment(Pos.TOP_LEFT);
            setPrefWidth(0);
            addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (getItem() != null) {
                    pickVerse(getItem(), event.isShiftDown());
                }
            });
        }

        @Override
        protected void updateItem(BibleVerse verse, boolean empty) {
            super.updateItem(verse, empty);
            setText(null);
            if (empty || verse == null) {
                setGraphic(null);
                getStyleClass().remove(CHOSEN);
                return;
            }
            boolean chosen = chosenVerses.contains(verse);
            toggleStyle(this, CHOSEN, chosen);
            number.setText(Integer.toString(verse.getNum()));
            text.setText(clean(verse.getText()));
            text.setWrapText(chosen);
            body.getChildren().setAll(text);
            if (chosen) {
                for (Bible other : versions.subList(1, versions.size())) {
                    BibleVerse found = BiblePassage.findVerse(other, verse);
                    Label name = new Label(other.getBibleName());
                    name.getStyleClass().add("bible-other-version-name");
                    Label otherText = new Label(found == null
                            ? LabelGrabber.INSTANCE.getLabel("bible.not.in.version") : clean(found.getText()));
                    otherText.getStyleClass().add("bible-other-version-text");
                    if (found == null) {
                        otherText.getStyleClass().add("bible-missing");
                    }
                    otherText.setWrapText(true);
                    otherText.setMinWidth(0);
                    VBox box = new VBox(name, otherText);
                    box.getStyleClass().add("bible-other-version");
                    box.setMinWidth(0);
                    body.getChildren().add(box);
                }
            }
            setGraphic(root);
        }
    }

    private static String clean(String verseText) {
        return verseText == null ? "" : verseText.replaceAll("\\s+", " ").trim();
    }
}
