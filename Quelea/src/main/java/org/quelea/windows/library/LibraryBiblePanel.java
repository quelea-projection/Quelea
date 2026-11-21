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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
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
import org.quelea.data.bible.BibleVersionText;
import org.quelea.data.bible.versification.Versification;
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
    private static final DataFormat VERSION_FORMAT = DataFormat.lookupMimeType("application/x-quelea-bible-version") != null
            ? DataFormat.lookupMimeType("application/x-quelea-bible-version") : new DataFormat("application/x-quelea-bible-version");
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
    /** The gap a dragged chip leaves where it would land. */
    private final Region chipSlot = new Region();
    private HBox draggedChip;
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
    private final Map<String, String> chosenReferences = new HashMap<>();
    private ChosenVersesEditor matchEditor;
    private String chosenFor;
    private Bible matchEditing;
    private BiblePassage preview;
    private final IntegerProperty chosenChanged = new SimpleIntegerProperty();

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
        chipSlot.getStyleClass().add("bible-version-drop-slot");
        acceptChipDrops();
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
            BibleBook book = primary.getMatchingBook(lookup.book());
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
            makeDraggable(chip, bible);
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
     * Let a version be moved by dragging its chip. While it's dragged, the
     * chip leaves a gap where it would land, and the other chips move aside.
     *
     * @param chip  the chip.
     * @param bible its version.
     */
    private void makeDraggable(HBox chip, Bible bible) {
        chip.setUserData(bible);
        chip.setOnDragDetected(event -> {
            if (versions.size() < 2) {
                return;
            }
            Dragboard dragboard = chip.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(VERSION_FORMAT, bible.getBibleName());
            dragboard.setContent(content);
            dragboard.setDragView(chip.snapshot(null, null), event.getX(), event.getY());
            draggedChip = chip;
            chipSlot.setMinSize(chip.getWidth(), chip.getHeight());
            chipSlot.setPrefSize(chip.getWidth(), chip.getHeight());
            chipSlot.setMaxSize(chip.getWidth(), chip.getHeight());
            versionPane.getChildren().add(versionPane.getChildren().indexOf(chip), chipSlot);
            chip.setVisible(false);
            chip.setManaged(false);
            event.consume();
        });
        chip.setOnDragDone(event -> {
            endChipDrag();
            event.consume();
        });
    }

    /**
     * Follow a dragged chip over the other chips, and move it to where it's
     * dropped.
     */
    private void acceptChipDrops() {
        versionPane.setOnDragOver(event -> {
            if (draggedChip != null && event.getDragboard().hasContent(VERSION_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
                Point2D point = versionPane.sceneToLocal(event.getSceneX(), event.getSceneY());
                moveChipSlot(point.getX(), point.getY());
                event.consume();
            }
        });
        versionPane.setOnDragDropped(event -> {
            if (draggedChip == null) {
                return;
            }
            List<Bible> order = new ArrayList<>();
            for (Node node : versionPane.getChildren()) {
                if (node == chipSlot) {
                    order.add((Bible) draggedChip.getUserData());
                } else if (node != draggedChip && node.getUserData() instanceof Bible other) {
                    order.add(other);
                }
            }
            event.setDropCompleted(true);
            event.consume();
            if (!order.equals(versions)) {
                // After the drag has finished, as the chips are made again
                Platform.runLater(() -> versions.setAll(order));
            }
        });
    }

    /**
     * Move the gap left by a dragged chip to where a drop at this point would
     * put it: before the first chip whose middle is after the point.
     */
    private void moveChipSlot(double x, double y) {
        if (chipSlot.getBoundsInParent().contains(x, y)) {
            return;
        }
        List<Node> children = versionPane.getChildren();
        int before = 0;
        for (Node node : children) {
            if (node == chipSlot || node == draggedChip || !(node.getUserData() instanceof Bible)) {
                continue;
            }
            Bounds bounds = node.getBoundsInParent();
            if (y >= bounds.getMaxY() || (y >= bounds.getMinY() && x > bounds.getCenterX())) {
                before++;
            }
        }
        children.remove(chipSlot);
        int index = 0;
        int seen = 0;
        while (index < children.size() && seen < before) {
            Node node = children.get(index);
            if (node != draggedChip && node.getUserData() instanceof Bible) {
                seen++;
            }
            index++;
        }
        children.add(index, chipSlot);
    }

    private void endChipDrag() {
        versionPane.getChildren().remove(chipSlot);
        if (draggedChip != null) {
            draggedChip.setVisible(true);
            draggedChip.setManaged(true);
            draggedChip = null;
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
        BiblePassage passage = SelectBibleVersionActionHandler.createPassage(lookup.reference(), List.copyOf(versions),
                Map.copyOf(chosenReferences));
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
        // Start over from the books
        searchField.clear();
        expanded = Expanded.NONE;
        searchField.requestFocus();
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
        // Every verse cell redraws itself too, in case the list shows a cell before it's recreated
        chosenChanged.set(chosenChanged.get() + 1);
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
            addMatchBoxes();
        } else if (chapter != null) {
            title = lookup.book().getBookName() + " " + lookup.chapter();
            detail = LabelGrabber.INSTANCE.getLabel("bible.pick.verses.hint");
        } else if (lookup.book() != null && !typingBook) {
            title = lookup.book().getBookName();
            detail = LabelGrabber.INSTANCE.getLabel("bible.pick.chapter.hint");
        }
        if (lookup.reference() == null || typingBook) {
            preview = null;
        }
        selectionTitle.setText(title);
        selectionDetail.setText(detail);
        selectionHint.setText(hint);
        warningBox.setVisible(!warningBox.getChildren().isEmpty());
        warningBox.setManaged(!warningBox.getChildren().isEmpty());
        addButton.setDisable(lookup.reference() == null || typingBook);
    }

    /**
     * Work out how the passage will be shown in the other versions, keeping the
     * verses the user chose for this passage.
     *
     * @return the passage in every version, or null if there is only one.
     */
    private BiblePassage createPreview() {
        String key = getPrimaryBible().getBibleName() + "|" + lookup.reference().getLocation();
        if (!key.equals(chosenFor)) {
            chosenFor = key;
            chosenReferences.clear();
            matchEditing = null;
        }
        chosenReferences.keySet().removeIf(name -> versions.stream().noneMatch(bible -> bible.getBibleName().equals(name)));
        if (matchEditing != null && !versions.subList(1, versions.size()).contains(matchEditing)) {
            matchEditing = null;
        }
        if (versions.size() < 2) {
            return null;
        }
        BibleReferenceParser.Reference reference = lookup.reference();
        return new BiblePassage(getPrimaryBible().getName(), reference.getLocation(),
                reference.verses().toArray(new BibleVerse[0]), reference.multi()).withVersions(versions, chosenReferences);
    }

    /**
     * Show how each of the other versions is matched with the first one: the
     * verses it shows and a button to choose them, a warning where Quelea
     * doesn't know its numbering, or the field to choose the verses in.
     */
    private void addMatchBoxes() {
        preview = createPreview();
        if (preview == null || !preview.isParallel()) {
            return;
        }
        List<BibleVersionText> texts = preview.getVersionTexts();
        for (int i = 1; i < versions.size() && i < texts.size(); i++) {
            Bible other = versions.get(i);
            BibleVersionText text = texts.get(i);
            if (other == matchEditing) {
                warningBox.getChildren().add(matchEditor(other, text));
            } else if (text.getMatch() != BibleVersionText.Match.CHOSEN
                    && (text.getMatch() == BibleVersionText.Match.BY_NUMBER || text.getLocation() == null)) {
                warningBox.getChildren().add(matchWarning(other, text));
            } else {
                warningBox.getChildren().add(matchStatus(other, text));
            }
        }
    }

    private HBox matchStatus(Bible other, BibleVersionText text) {
        Label name = new Label(other.getBibleName());
        name.getStyleClass().add("bible-match-name");
        name.setMinWidth(Region.USE_PREF_SIZE);
        Label location = new Label(text.getLocation());
        location.setTextOverrun(OverrunStyle.ELLIPSIS);
        location.setMinWidth(0);
        HBox heading = new HBox(8, name, location);
        boolean chosen = text.getMatch() == BibleVersionText.Match.CHOSEN;
        Label status = new Label(LabelGrabber.INSTANCE.getLabel(chosen ? "bible.match.chosen" : "bible.match.automatic"));
        status.getStyleClass().addAll("bible-match-status", chosen ? "bible-match-chosen" : "bible-match-automatic");
        VBox body = new VBox(2, heading, status);
        body.setMinWidth(0);
        HBox.setHgrow(body, Priority.ALWAYS);
        Button change = new Button(LabelGrabber.INSTANCE.getLabel("bible.match.change.button"));
        change.setMinWidth(Region.USE_PREF_SIZE);
        change.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("bible.match.choose.button").replace("$1", other.getBibleName())));
        change.setOnAction(event -> openMatchEditor(other));
        HBox ret = new HBox(8, body, change);
        ret.setAlignment(Pos.CENTER_LEFT);
        ret.getStyleClass().add("bible-match");
        return ret;
    }

    private VBox matchWarning(Bible other, BibleVersionText text) {
        Label message = new Label(LabelGrabber.INSTANCE.getLabel(text.getLocation() == null
                ? "bible.match.missing.warning" : "bible.match.unknown.warning").replace("$1", other.getBibleName()));
        message.setWrapText(true);
        message.setMinHeight(Region.USE_PREF_SIZE);
        HBox.setHgrow(message, Priority.ALWAYS);
        HBox row = new HBox(8, QueleaIcon.WARNING.create(14), message);
        Button choose = new Button(LabelGrabber.INSTANCE.getLabel("bible.match.choose.button").replace("$1", other.getBibleName()));
        choose.setOnAction(event -> openMatchEditor(other));
        VBox ret = new VBox(8, row, choose);
        ret.getStyleClass().add("bible-warning");
        return ret;
    }

    private ChosenVersesEditor matchEditor(Bible other, BibleVersionText text) {
        matchEditor = new ChosenVersesEditor(lookup.reference().getLocation(), getPrimaryBible(), lookup.reference().book(),
                other, text, preview.getVerses().length, chosen -> {
                    if (chosen == null) {
                        chosenReferences.remove(other.getBibleName());
                    } else {
                        chosenReferences.put(other.getBibleName(), chosen);
                    }
                    closeMatchEditor();
                }, this::closeMatchEditor);
        return matchEditor;
    }

    private void openMatchEditor(Bible other) {
        matchEditing = other;
        refresh();
        if (matchEditor != null) {
            matchEditor.focus();
        }
    }

    private void closeMatchEditor() {
        matchEditing = null;
        matchEditor = null;
        refresh();
    }

    private BibleVersionText getPreviewText(Bible bible) {
        if (preview == null) {
            return null;
        }
        int index = versions.indexOf(bible);
        List<BibleVersionText> texts = preview.getVersionTexts();
        return index < 0 || index >= texts.size() ? null : texts.get(index);
    }

    /**
     * Find the row of the preview a verse of the first version is shown on.
     *
     * @return the row, or -1 if the verse isn't in the passage.
     */
    private int getPreviewRow(BibleVerse verse) {
        if (preview == null) {
            return -1;
        }
        BibleVerse[] rows = preview.getVerses();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == verse) {
                return i;
            }
        }
        return -1;
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
        private final ChangeListener<Number> redraw = (obs, oldValue, newValue) -> render(getItem(), isEmpty());

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
            chosenChanged.addListener(new WeakChangeListener<>(redraw));
        }

        @Override
        protected void updateItem(BibleVerse verse, boolean empty) {
            super.updateItem(verse, empty);
            render(verse, empty);
        }

        private void render(BibleVerse verse, boolean empty) {
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
                int row = getPreviewRow(verse);
                for (Bible other : versions.subList(1, versions.size())) {
                    BibleVersionText version = row < 0 ? null : getPreviewText(other);
                    String ref;
                    String shown;
                    if (version != null) {
                        ref = version.getVerseRef(row);
                        shown = version.getVerseText(row);
                        if (shown != null && shown.isEmpty()) {
                            shown = null;
                            ref = null;
                        }
                    } else {
                        List<BibleVerse> found = Versification.match(verse, other).verses();
                        boolean renumbered = found.size() > 1 || (found.size() == 1
                                && (found.get(0).getNum() != verse.getNum() || found.get(0).getChapterNum() != verse.getChapterNum()));
                        ref = renumbered ? BiblePassage.describe(found) : null;
                        List<String> texts = new ArrayList<>();
                        for (BibleVerse otherVerse : found) {
                            texts.add(clean(otherVerse.getText()));
                        }
                        shown = found.isEmpty() ? null : String.join(" ", texts);
                    }
                    boolean above = version != null && "".equals(version.getVerseText(row));
                    Label name = new Label(other.getBibleName() + (ref != null ? " · " + ref : ""));
                    name.getStyleClass().add("bible-other-version-name");
                    Label otherText = new Label(shown != null ? clean(shown) : LabelGrabber.INSTANCE.getLabel(above
                            && version.getMatch() != BibleVersionText.Match.CHOSEN ? "bible.shown.above" : "bible.not.in.version"));
                    otherText.getStyleClass().add("bible-other-version-text");
                    if (shown == null) {
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
