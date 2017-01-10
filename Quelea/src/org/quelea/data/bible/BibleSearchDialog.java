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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    private BibleSearchTreeView searchResults;
    private ComboBox<String> bibles;
    private ScrollPane scrollPane;
    private LoadingPane overlay;
    private FlowPane chapterPane;
    private final Button addToSchedule;
    private final Text resultsField;

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
        chapterPane = new FlowPane();
        scrollPane = new ScrollPane();
        scrollPane.setContent(chapterPane);
        searchResults = new BibleSearchTreeView(scrollPane, bibles);
        resultsField = new Text(" " + LabelGrabber.INSTANCE.getLabel("bible.search.keep.typing"));
        resultsField.setFont(Font.font("Sans", 14));
        addToSchedule = new Button(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), new ImageView(new Image("file:icons/tick.png")));

        BibleManager.get().registerBibleChangeListener(this);
        updateBibles();

        //top panel
        HBox northPanel = new HBox();
        northPanel.setPadding(new Insets(5, 5, 5, 5));
        northPanel.getChildren().addAll(bibles, searchField, addToSchedule, resultsField);
        mainPane.setTop(northPanel);

        //center panel
        StackPane searchPane = new StackPane();
        searchPane.getChildren().addAll(searchResults, overlay);

        SplitPane centerPanel = new SplitPane();
        centerPanel.setDividerPosition(0, 0.3);
        centerPanel.getItems().addAll(searchPane, scrollPane);
        mainPane.setCenter(centerPanel);

        //Sizing
        this.setHeight(600);
        this.setWidth(800);
        this.setMinHeight(300);
        this.setMinWidth(500);

        // Event handlers
        bibles.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                searchResults.resetRoot();
                update();
            }
        });
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                update();
            }
        });
        addToSchedule.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (searchResults.getSelectionModel().getSelectedItem().getValue() instanceof BibleVerse) {
                    BibleChapter chap = (BibleChapter) searchResults.getSelectionModel().getSelectedItem().getValue().getParent();
                    Bible bib = (Bible) chap.getParent().getParent();
                    BiblePassage passage = new BiblePassage(bib.getBibleName(), chap.getBook() + " " + chap.toString(), chap.getVerses(), false);
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(passage);
                }
            }
        });
        setOnShown((WindowEvent event) -> {
            if (!BibleManager.get().isIndexInit()) {
                BibleManager.get().refreshAndLoad();
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
        BibleManager.get().runOnIndexInit(() -> {
            searchField.setDisable(false);
        });
    }

    private ExecutorService updateExecutor = Executors.newSingleThreadExecutor();
    private ExecRunnable lastUpdateRunnable = null;

    private interface ExecRunnable extends Runnable {

        void cancel();
    }

    /**
     * Update the results based on the entered text.
     */
    private void update() {
        final String text = searchField.getText();
        if (text.length() > 3) {
            if (BibleManager.get().isIndexInit()) {
                searchResults.reset();
                overlay.show();
                ExecRunnable execRunnable = new ExecRunnable() {
                    private volatile boolean cancel = false;

                    public void cancel() {
                        cancel = true;
                    }

                    public void run() {
                        if (cancel) {
                            return;
                        }
                        final BibleChapter[] results = BibleManager.get().getIndex().filter(text, null);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                searchResults.reset();
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
                                String resultsfoundSuffix = LabelGrabber.INSTANCE.getLabel("bible.search.results.found");
                                if (searchResults.size() == 1 && LabelGrabber.INSTANCE.isLocallyDefined("bible.search.result.found")) {
                                    resultsfoundSuffix = LabelGrabber.INSTANCE.getLabel("bible.search.result.found");
                                }
                                resultsField.setText(" " + searchResults.size() + " " + resultsfoundSuffix);
                            }
                        });
                    }
                };
                if (lastUpdateRunnable != null) {
                    lastUpdateRunnable.cancel();
                }
                lastUpdateRunnable = execRunnable;
                updateExecutor.submit(execRunnable);
            }
        }
        searchResults.reset();
        resultsField.setText(" " + LabelGrabber.INSTANCE.getLabel("bible.search.keep.typing"));
        chapterPane.getChildren().clear();
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
