/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.ImageManager;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.library.LibraryPanel;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.search.UniversalSearch.Kind;
import org.quelea.windows.search.UniversalSearch.Result;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;
import org.quelea.windows.video.VidPreviewDisplay;

/**
 * A search box that floats above the main window. The highlighted result is
 * previewed next to the list and added to the schedule with enter, except for
 * notices, which go on screen so have to be confirmed first. The filters for
 * the kinds of result have a shortcut each, and tab moves between them. Page up
 * and down scroll the preview, as the keyboard stays in the search field.
 * <p/>
 * This is a window of its own rather than part of the main window's scene, so
 * that nothing typed here can reach the shortcuts of the main window.
 *
 * @author Arvid
 */
public class UniversalSearchWindow extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final double WIDTH = 820;
    private static final double PREVIEW_WIDTH = 300;
    /** The padding of .universal-search-preview on both sides. */
    private static final double PREVIEW_PADDING = 24;
    /** Room around the box for its shadow, see .universal-search-root. */
    private static final double SHADOW_MARGIN = 32;
    private static final double ROW_HEIGHT = 54;
    private static final int MAX_VISIBLE_ROWS = 7;
    private static final double DISTANCE_FROM_TOP = 0.16;
    /** How much of the preview page up and down move, keeping some context. */
    private static final double PAGE_FRACTION = 0.9;

    private final UniversalSearch search = new UniversalSearch();
    private final ExecutorService searchService = createService("universal-search");
    private final ExecutorService previewService = createService("universal-search-preview");
    private final TextField searchField;
    private final ToggleGroup filters;
    private final List<KeyCombination> filterKeys = new ArrayList<>();
    private final ListView<Result> resultList;
    private final VBox previewPane;
    private final ImageView previewImage;
    private final VBox previewText;
    private final ScrollPane previewScroll;
    private final HBox resultPane;
    private final Label noResultsLabel;
    private VidPreviewDisplay videoPreview;
    private Future<?> pendingSearch;
    private Future<?> pendingPreview;
    /** The number of the latest search, and of the one in the list. */
    private long latestSearch;
    private long shownSearch;
    /** Enter was pressed before the results for the latest search were in. */
    private boolean addWhenReady;
    private int firstVisibleRow;
    private boolean bibleIndexRequested;

    /**
     * Create the search window.
     *
     * @param owner the main window, which this window stays above.
     */
    public UniversalSearchWindow(MainWindow owner) {
        initOwner(owner);
        initStyle(StageStyle.TRANSPARENT);

        searchField = new TextField();
        searchField.setPromptText(LabelGrabber.INSTANCE.getLabel("universal.search.prompt"));
        searchField.textProperty().addListener((obs, oldText, text) -> startSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox searchBox = new HBox(4, QueleaIcon.SEARCH.create(20), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("universal-search-field");

        filters = new ToggleGroup();
        HBox filterBar = new HBox(4, createFilter(LabelGrabber.INSTANCE.getLabel("all.text"), null));
        for (Kind kind : Kind.values()) {
            filterBar.getChildren().add(createFilter(LabelGrabber.INSTANCE.getLabel(kind.getLabel()), kind));
        }
        filters.selectToggle(filters.getToggles().getFirst());
        filters.selectedToggleProperty().addListener((obs, oldFilter, filter) -> {
            if (filter == null) {
                filters.selectToggle(oldFilter); // One is always chosen, a second click doesn't undo it
            } else {
                startSearch();
            }
        });

        resultList = new ListView<>();
        resultList.setFixedCellSize(ROW_HEIGHT);
        resultList.setFocusTraversable(false);
        resultList.setCellFactory(list -> new ResultCell());
        resultList.getSelectionModel().selectedItemProperty().addListener((obs, oldResult, result) -> showPreview(result));
        HBox.setHgrow(resultList, Priority.ALWAYS);

        previewImage = new ImageView();
        previewImage.setPreserveRatio(true);
        previewImage.setFitWidth(PREVIEW_WIDTH - PREVIEW_PADDING);
        previewText = new VBox(10);
        previewScroll = new ScrollPane(previewText);
        previewScroll.setFitToWidth(true);
        previewScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        previewScroll.setFocusTraversable(false);
        VBox.setVgrow(previewScroll, Priority.ALWAYS);
        previewPane = new VBox(8, previewImage, previewScroll);
        previewPane.getStyleClass().add("universal-search-preview");
        previewPane.setMinWidth(PREVIEW_WIDTH);
        previewPane.setMaxWidth(PREVIEW_WIDTH);

        resultPane = new HBox(8, resultList, previewPane);
        // The same height for every search, so that the window doesn't jump around while typing
        resultPane.setPrefHeight(MAX_VISIBLE_ROWS * ROW_HEIGHT + 2);

        noResultsLabel = new Label(LabelGrabber.INSTANCE.getLabel("universal.search.no.results"));
        noResultsLabel.getStyleClass().add("universal-search-detail");

        VBox box = new VBox(searchBox, filterBar, resultPane, noResultsLabel);
        box.getStyleClass().add("universal-search");
        box.setPrefWidth(WIDTH);
        StackPane root = new StackPane(box);
        root.getStyleClass().add("universal-search-root");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        StyleManager.apply(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        // A click on the preview takes the focus, but typing should carry on
        scene.focusOwnerProperty().addListener((obs, oldFocus, focus) -> {
            if (focus != null && focus != searchField) {
                Platform.runLater(() -> {
                    searchField.requestFocus();
                    searchField.end();
                });
            }
        });
        setScene(scene);

        showResults(0, "", List.of());
        // Clicking anywhere else means the search isn't wanted any more
        focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (!focused) {
                hide();
            }
        });
    }

    private static ExecutorService createService(String name) {
        return Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        });
    }

    private ToggleButton createFilter(String label, Kind kind) {
        ToggleButton button = new ToggleButton(label);
        button.setUserData(kind);
        button.setToggleGroup(filters);
        button.setFocusTraversable(false); // Typing carries on after a click on a filter
        button.getStyleClass().add("universal-search-filter");
        KeyCombination key = new KeyCodeCombination(KeyCode.getKeyCode(Integer.toString(filterKeys.size() + 1)),
                KeyCombination.SHORTCUT_DOWN);
        filterKeys.add(key);
        Label keyLabel = new Label(key.getDisplayText());
        keyLabel.getStyleClass().add("universal-search-filter-key");
        button.setGraphic(keyLabel);
        button.setContentDisplay(ContentDisplay.RIGHT);
        return button;
    }

    /**
     * Show an empty search box, or go back to the search box if it's showing.
     */
    public void open() {
        // Quelea only builds this index when something needs to search bible text
        if (!bibleIndexRequested && !BibleManager.get().isIndexInit()) {
            bibleIndexRequested = true;
            BibleManager.get().buildIndex();
        }
        if (!isShowing()) {
            filters.selectToggle(filters.getToggles().getFirst());
            searchField.clear();
            Stage owner = (Stage) getOwner();
            setX(owner.getX() + (owner.getWidth() - WIDTH) / 2 - SHADOW_MARGIN);
            setY(owner.getY() + owner.getHeight() * DISTANCE_FROM_TOP);
            show();
        }
        toFront();
        searchField.requestFocus();
        searchField.selectAll();
    }

    private void handleKey(KeyEvent event) {
        for (int i = 0; i < filterKeys.size(); i++) {
            if (filterKeys.get(i).match(event)) {
                filters.selectToggle(filters.getToggles().get(i));
                event.consume();
                return;
            }
        }
        switch (event.getCode()) {
            case ESCAPE -> hide();
            case ENTER -> addSelected();
            case UP -> moveSelection(-1);
            case DOWN -> moveSelection(1);
            case TAB -> moveFilter(event.isShiftDown() ? -1 : 1);
            case PAGE_UP -> scrollPreview(-1);
            case PAGE_DOWN -> scrollPreview(1);
            default -> {
                return;
            }
        }
        event.consume();
    }

    private void moveFilter(int change) {
        int count = filters.getToggles().size();
        int current = filters.getToggles().indexOf(filters.getSelectedToggle());
        filters.selectToggle(filters.getToggles().get((current + change + count) % count));
    }

    private void scrollPreview(int pages) {
        double viewport = previewScroll.getViewportBounds().getHeight();
        double hidden = previewText.getHeight() - viewport;
        if (hidden > 0) {
            double value = previewScroll.getVvalue() + pages * viewport * PAGE_FRACTION / hidden;
            previewScroll.setVvalue(Math.max(0, Math.min(1, value)));
        }
    }

    private void moveSelection(int change) {
        int row = resultList.getSelectionModel().getSelectedIndex() + change;
        if (row < 0 || row >= resultList.getItems().size()) {
            return;
        }
        resultList.getSelectionModel().select(row);
        // ListView.scrollTo() puts a row at the top, so only scroll when the row is out of view
        if (row < firstVisibleRow) {
            firstVisibleRow = row;
            resultList.scrollTo(firstVisibleRow);
        } else if (row >= firstVisibleRow + MAX_VISIBLE_ROWS) {
            firstVisibleRow = row - MAX_VISIBLE_ROWS + 1;
            resultList.scrollTo(firstVisibleRow);
        }
    }

    private void addSelected() {
        if (shownSearch != latestSearch) {
            // Typed quickly: what's in the list belongs to an earlier search
            addWhenReady = true;
            return;
        }
        Result selected = resultList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            hide();
            selected.action().run();
        }
    }

    private void startSearch() {
        String query = searchField.getText();
        long number = ++latestSearch;
        addWhenReady = false;
        if (pendingSearch != null) {
            // Not with an interrupt, the search indexes don't all cope with that
            pendingSearch.cancel(false);
        }
        if (query.isBlank()) {
            showResults(number, query, List.of());
            return;
        }
        Kind filter = (Kind) filters.getSelectedToggle().getUserData();
        LibraryPanel library = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel();
        UniversalSearch.Sources sources = new UniversalSearch.Sources(
                library.getBiblePanel().getBibleSelector().getValue(),
                List.copyOf(library.getTimerPanel().getTimerPanel().getListView().getItems()),
                List.copyOf(QueleaApp.get().getMainWindow().getNoticeDialog().getTemplates().getItems()));
        pendingSearch = searchService.submit(() -> {
            List<Result> results = List.of();
            try {
                results = search.search(query, filter, sources);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Universal search failed for: " + query, ex);
            }
            List<Result> found = results;
            Platform.runLater(() -> {
                if (number == latestSearch) {
                    showResults(number, query, found);
                }
            });
        });
    }

    private void showResults(long number, String query, List<Result> results) {
        shownSearch = number;
        firstVisibleRow = 0;
        resultList.getItems().setAll(results);
        resultList.getSelectionModel().selectFirst();
        resultList.scrollTo(0);
        setShown(resultPane, !results.isEmpty());
        setShown(noResultsLabel, results.isEmpty() && !query.isBlank());
        sizeToScene();
        if (addWhenReady) {
            addWhenReady = false;
            addSelected();
        }
    }

    private void showPreview(Result result) {
        if (pendingPreview != null) {
            pendingPreview.cancel(false);
        }
        previewImage.setImage(null);
        setShown(previewImage, false);
        boolean hasText = result != null && !result.previewText().isEmpty();
        boolean hasFile = result != null && result.previewFile() != null;
        previewText.getChildren().clear();
        if (hasText) {
            for (String part : result.previewText()) {
                Label label = new Label(part);
                label.setWrapText(true);
                previewText.getChildren().add(label);
            }
        }
        previewScroll.setVvalue(0);
        setShown(previewScroll, hasText);
        setShown(previewPane, hasText || hasFile);
        if (hasText && result.previewStart() > 0) {
            // Where the part ends up isn't known until the text is laid out
            Platform.runLater(() -> scrollToPreviewStart(result));
        }
        if (hasFile) {
            pendingPreview = previewService.submit(() -> {
                Image image = loadPreview(result);
                Platform.runLater(() -> {
                    if (image != null && result == resultList.getSelectionModel().getSelectedItem()) {
                        previewImage.setImage(image);
                        setShown(previewImage, true);
                    }
                });
            });
        }
    }

    private void scrollToPreviewStart(Result result) {
        if (result != resultList.getSelectionModel().getSelectedItem()) {
            return;
        }
        getScene().getRoot().applyCss();
        getScene().getRoot().layout();
        double hidden = previewText.getHeight() - previewScroll.getViewportBounds().getHeight();
        if (hidden > 0) {
            double top = previewText.getChildren().get(result.previewStart()).getBoundsInParent().getMinY();
            previewScroll.setVvalue(Math.min(1, top / hidden));
        }
    }

    /**
     * Load the picture of a result. This can take a while, so it shouldn't be
     * called on the FX thread.
     */
    private Image loadPreview(Result result) {
        File file = result.previewFile();
        try {
            if (result.kind() == Kind.VIDEO) {
                if (videoPreview == null) {
                    videoPreview = new VidPreviewDisplay();
                }
                // Copied, because the frame is only on loan from the video player
                Image frame = videoPreview.getPreviewImg(file.toURI());
                return new WritableImage(frame.getPixelReader(), (int) frame.getWidth(), (int) frame.getHeight());
            }
            double width = PREVIEW_WIDTH - PREVIEW_PADDING;
            return ImageManager.INSTANCE.getImage(file.toURI().toString(), width * 2, width * 2, true);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Couldn't create a preview of " + file, ex);
            return null;
        }
    }

    private static void setShown(javafx.scene.Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /**
     * A row of the result list.
     */
    private final class ResultCell extends ListCell<Result> {

        private final Label title = new Label();
        private final Label subtitle = new Label();
        private final Label kind = new Label();
        private final StackPane icon = new StackPane();
        private final HBox row;

        ResultCell() {
            subtitle.getStyleClass().add("universal-search-detail");
            kind.getStyleClass().add("universal-search-detail");
            kind.setMinWidth(Label.USE_PREF_SIZE);
            VBox text = new VBox(title, subtitle);
            text.setAlignment(Pos.CENTER_LEFT);
            // Take what's left of the row, however long the text, so it's cut off rather than scrolled
            text.setMinWidth(0);
            text.setPrefWidth(0);
            HBox.setHgrow(text, Priority.ALWAYS);
            row = new HBox(10, icon, text, kind);
            row.setAlignment(Pos.CENTER_LEFT);
            setOnMouseClicked(event -> {
                if (!isEmpty()) {
                    addSelected();
                }
            });
        }

        @Override
        protected void updateItem(Result result, boolean empty) {
            super.updateItem(result, empty);
            if (empty || result == null) {
                setGraphic(null);
                return;
            }
            icon.getChildren().setAll(result.kind().getIcon().create(20));
            title.setText(result.title());
            subtitle.setText(result.subtitle());
            setShown(subtitle, !result.subtitle().isEmpty());
            kind.setText(LabelGrabber.INSTANCE.getLabel(result.kind().getLabel()));
            setGraphic(row);
        }
    }
}
