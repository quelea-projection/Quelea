/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.newsong;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.javafx.dialog.Dialog;
import org.quelea.data.chord.ChordLineTransposer;
import org.quelea.data.chord.ChordTransposer;
import org.quelea.data.chord.TransposeDialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.spelling.Dictionary;
import org.quelea.services.languages.spelling.DictionaryManager;
import org.quelea.services.languages.spelling.SpellTextArea;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.LyricsTextArea;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel that manages the basic input of song information - the title,
 * author and lyrics.
 * <p/>
 * @author Michael
 */
public class BasicSongPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final SpellTextArea lyricsArea;
    private final TextField titleField;
    private final TextField authorField;
    private final Button transposeButton;
    private final Button nonBreakingLineButton;
    private final Button chorusButton;
    private final Button bridgeButton;
    private final Button preChorusButton;
    private final Button tagButton;
    private final SplitMenuButton verseButton;
    private final ComboBox<Dictionary> dictSelector;
    private final TransposeDialog transposeDialog;
    private String saveHash = "";

    /**
     * Create and initialise the song panel.
     */
    public BasicSongPanel() {
        final VBox centrePanel = new VBox();
        transposeDialog = new TransposeDialog();
        GridPane topPanel = new GridPane();

        titleField = new TextField();
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        Label titleLabel = new Label(LabelGrabber.INSTANCE.getLabel("title.label"));
        GridPane.setConstraints(titleLabel, 1, 1);
        topPanel.getChildren().add(titleLabel);
        titleLabel.setLabelFor(titleField);
        GridPane.setConstraints(titleField, 2, 1);
        topPanel.getChildren().add(titleField);

        authorField = new TextField();
        GridPane.setHgrow(authorField, Priority.ALWAYS);
        Label authorLabel = new Label(LabelGrabber.INSTANCE.getLabel("author.label"));
        GridPane.setConstraints(authorLabel, 1, 2);
        topPanel.getChildren().add(authorLabel);
        authorLabel.setLabelFor(authorField);
        GridPane.setConstraints(authorField, 2, 2);
        topPanel.getChildren().add(authorField);

        centrePanel.getChildren().add(topPanel);
        lyricsArea = new SpellTextArea();
        lyricsArea.setMaxHeight(Double.MAX_VALUE);

        final VBox mainPanel = new VBox();
        ToolBar lyricsToolbar = new ToolBar();
        transposeButton = getTransposeButton();
        nonBreakingLineButton = getNonBreakingLineButton();
        lyricsToolbar.getItems().add(transposeButton);
        lyricsToolbar.getItems().add(nonBreakingLineButton);

        chorusButton = getTitleButton("chorus.png", "chorus.tooltip", "Chorus");
        preChorusButton = getTitleButton("pre_chorus.png", "prechorus.tooltip", "Pre-chorus");
        bridgeButton = getTitleButton("bridge.png", "bridge.tooltip", "Bridge");
        tagButton = getTitleButton("tag_coda.png", "tag.tooltip", "Tag");
        verseButton = getVerseButton();
        lyricsToolbar.getItems().add(new Separator());
        lyricsToolbar.getItems().add(chorusButton);
        lyricsToolbar.getItems().add(preChorusButton);
        lyricsToolbar.getItems().add(bridgeButton);
        lyricsToolbar.getItems().add(tagButton);
        lyricsToolbar.getItems().add(verseButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        lyricsToolbar.getItems().add(spacer);
        dictSelector = new ComboBox<>();
        dictSelector.addEventFilter(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> {
            dictSelector.requestFocus();
            //To be deleted when fixed in java #comboboxbug
        });
        Tooltip.install(dictSelector, new Tooltip(LabelGrabber.INSTANCE.getLabel("dictionary.language.text")));
        for (Dictionary dict : DictionaryManager.INSTANCE.getDictionaries()) {
            dictSelector.getItems().add(dict);
        }
        dictSelector.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<Dictionary>() {

            @Override
            public void changed(ObservableValue<? extends Dictionary> ov, Dictionary t, Dictionary t1) {
                lyricsArea.setDictionary(dictSelector.getValue());
            }
        });

        dictSelector.getSelectionModel().select(QueleaProperties.get().getDictionary());
        lyricsToolbar.getItems().add(dictSelector);
        lyricsToolbar.getItems().add(getDictButton());
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
        mainPanel.getChildren().add(lyricsToolbar);
        VBox.setVgrow(lyricsArea, Priority.ALWAYS);
        mainPanel.getChildren().add(lyricsArea);
        centrePanel.getChildren().add(mainPanel);
        setCenter(centrePanel);
    }

    public void resetSaveHash() {
        saveHash = getSaveHash();
    }

    public boolean hashChanged() {
        return !getSaveHash().equals(saveHash);
    }

    private String getSaveHash() {
        return "" + lyricsArea.getText().hashCode() + titleField.getText().hashCode() + authorField.getText().hashCode();
    }

    private Button getNonBreakingLineButton() {
        Button ret = new Button("", new ImageView(new Image("file:icons/nonbreakline.png", 24, 24, false, true)));
        Utils.setToolbarButtonStyle(ret);
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("nonbreak.tooltip")));
        ret.setOnAction((event) -> {
            int caretPos = lyricsArea.getArea().getCaretPosition();
            String[] parts = lyricsArea.getTextAndChords().split("\n");
            int lineIndex = lineFromPos(lyricsArea.getTextAndChords(), caretPos);
            String line = parts[lineIndex];
            if (line.trim().isEmpty()) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        lyricsArea.getArea().replaceText(caretPos, caretPos, "<>");
                        lyricsArea.getArea().refreshStyle();
                    }
                });
            } else {
                int nextLinePos = nextLinePos(lyricsArea.getTextAndChords(), caretPos);
                if (nextLinePos >= lyricsArea.getTextAndChords().length()) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            lyricsArea.getArea().replaceText(nextLinePos, nextLinePos, "\n<>\n");
                            lyricsArea.getArea().refreshStyle();
                        }
                    });
                } else {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            lyricsArea.getArea().replaceText(nextLinePos, nextLinePos, "<>\n");
                            lyricsArea.getArea().refreshStyle();
                        }
                    });
                }
            }
        });
        return ret;
    }

    /**
     * Get a title button
     * @param fileName Image file name
     * @param label Tooltip label
     * @param titleName Title name to be inserted
     * @return the title button
     */
    private Button getTitleButton(String fileName, String label, String titleName) {
        Button ret = new Button("", new ImageView(new Image("file:icons/" + fileName, 24, 24, false, true)));
        Utils.setToolbarButtonStyle(ret);
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel(label)));
        ret.setOnAction((event) -> {
            insertTitle(titleName, "");
        });
        return ret;
    }

    private int nextLinePos(String s, int pos) {
        if (pos == 0 || s.charAt(pos - 1) == '\n') {
            return pos;
        }
        while (s.charAt(pos) != '\n' && pos <= s.length()) {
            pos++;
        }
        return pos + 1;
    }

    private int lineFromPos(String s, int pos) {
        int ret = 0;
        for (int i = 0; i <= pos - 1; i++) {
            if (s.charAt(i) == '\n') {
                ret++;
            }
        }
        return ret;
    }

    /**
     * Get the button used for transposing the chords.
     * <p/>
     * @return the button used for transposing the chords.
     */
    private Button getTransposeButton() {
        Button ret = new Button("", new ImageView(new Image("file:icons/transpose.png", 24, 24, false, true)));
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("transpose.tooltip")));
        ret.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                String originalKey = getKey(0);
                if (originalKey == null) {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("no.chords.title"), LabelGrabber.INSTANCE.getLabel("no.chords.message"));
                    return;
                }
                transposeDialog.setKey(originalKey);
                transposeDialog.showAndWait();
                int semitones = transposeDialog.getSemitones();

                TextField keyField = QueleaApp.get().getMainWindow().getSongEntryWindow().getDetailedSongPanel().getKeyField();
                if (!keyField.getText().isEmpty()) {
                    keyField.setText(new ChordTransposer(keyField.getText()).transpose(semitones, null));
                }

                String key = getKey(semitones);

                StringBuilder newText = new StringBuilder(getLyricsField().getText().length());
                for (String line : getLyricsField().getText().split("\n")) {
                    if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                        newText.append(new ChordLineTransposer(line).transpose(semitones, key));
                    } else {
                        newText.append(line);
                    }
                    newText.append('\n');
                }
                int pos = getLyricsField().getCaretPosition();
                getLyricsField().replaceText(newText.toString());
                getLyricsField().positionCaret(pos);
            }
        });
        Utils.setToolbarButtonStyle(ret);
        return ret;
    }

    /**
     * Get the given key of the song (or as best we can work out if it's not
     * specified) transposed by the given number of semitones.
     * <p/>
     * @param semitones the number of semitones to transpose the key.
     * @return the key, transposed.
     */
    private String getKey(int semitones) {
        TextField keyField = QueleaApp.get().getMainWindow().getSongEntryWindow().getDetailedSongPanel().getKeyField();
        String key = keyField.getText();
        if (key == null || key.isEmpty()) {
            for (String line : getLyricsField().getText().split("\n")) {
                if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                    String first;
                    int i = 0;
                    do {
                        first = line.split("\\s+")[i++];
                    } while (first.isEmpty());
                    key = new ChordTransposer(first).transpose(semitones, null);
                    if (key.length() > 2) {
                        key = key.substring(0, 2);
                    }
                    if (key.length() == 2) {
                        if (key.charAt(1) == 'B') {
                            key = Character.toString(key.charAt(0)) + "b";
                        } else if (key.charAt(1) != 'b' && key.charAt(1) != '#') {
                            key = Character.toString(key.charAt(0));
                        }
                    }
                    break;
                }
            }
        }

        if (key == null || key.isEmpty()) {
            key = null;
        }
        return key;
    }

    /**
     * Get the spell checker button.
     * <p/>
     * @return the spell checker button.
     */
    private Button getDictButton() {
        Button button = new Button("", new ImageView(new Image("file:icons/dictionary.png", 24, 24, false, true)));
        button.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("run.spellcheck.label") + " (F7)"));
        button.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                lyricsArea.runSpellCheck();
            }
        });
        button.disableProperty().bind(lyricsArea.spellingOkProperty());
        Utils.setToolbarButtonStyle(button);
        return button;
    }

    /**
     * Reset this panel so new song data can be entered.
     */
    public void resetNewSong() {
        getTitleField().clear();
        getAuthorField().clear();
        getLyricsField().replaceText("");
        getTitleField().requestFocus();
        lyricsArea.clearUndo();
    }

    /**
     * Reset this panel so an existing song can be edited.
     * <p/>
     * @param song the song to edit.
     */
    public void resetEditSong(SongDisplayable song) {
        getTitleField().setText(song.getTitle());
        getAuthorField().setText(song.getAuthor());
        getLyricsField().clear();
        getLyricsField().insertText(0, song.getLyrics(true, true));
        getLyricsField().refreshStyle();
        getLyricsField().requestFocus();
        lyricsArea.clearUndo();
    }

    /**
     * Get the lyrics field.
     * <p/>
     * @return the lyrics field.
     */
    public LyricsTextArea getLyricsField() {
        return lyricsArea.getArea();
    }

    /**
     * Get the title field.
     * <p/>
     * @return the title field.
     */
    public TextField getTitleField() {
        return titleField;
    }

    /**
     * Get the author field.
     * <p/>
     * @return the author field.
     */
    public TextField getAuthorField() {
        return authorField;
    }

    /**
     * Get the verse title button
     *
     * @return verse button
     */
    private SplitMenuButton getVerseButton() {
        SplitMenuButton m = new SplitMenuButton();
        m.setGraphic(new ImageView(new Image("file:icons/verse.png", 24, 24, false, true)));
        m.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("verse.tooltip")));
        m.setOnMouseClicked((MouseEvent event) -> {
            insertTitle("Verse", "");
        });
        for (int i = 1; i < 10; i++) {
            MenuItem mi = new MenuItem("", new ImageView(new Image("file:icons/verse" + i + ".png", 24, 24, false, true)));
            final int finalI = i;
            mi.setOnAction((ActionEvent event) -> {
                insertTitle("Verse", Integer.toString(finalI));
            });
            m.getItems().add(mi);
        }
        return m;
    }

    /**
     * Insert a title in the lyrics
     *
     * @param title The name of the section title, e.g. "Chorus"
     * @param number The number of the title as a string, e.g. (Verse) "2", and
     * use "" if no number is wanted
     */
    private void insertTitle(String title, String number) {
        int caretPos = lyricsArea.getArea().getCaretPosition();
        String[] parts = lyricsArea.getTextAndChords().split("\n");
        if (parts.length == 0) {
            Platform.runLater(() -> {
                lyricsArea.getArea().replaceText(0, 0, title + " " + number + "\n");
                lyricsArea.getArea().refreshStyle();
            });
        } else {
            if (caretPos == lyricsArea.getTextAndChords().length() + 1) {
                Platform.runLater(() -> {
                    lyricsArea.getArea().replaceText(caretPos, caretPos, title + " " + number + "\n");
                    lyricsArea.getArea().refreshStyle();
                });
            } else {
                int lineIndex = lineFromPos(lyricsArea.getTextAndChords(), caretPos);
                String line = parts[lineIndex];
                if (line.trim().isEmpty()) {
                    Platform.runLater(() -> {
                        lyricsArea.getArea().replaceText(caretPos, caretPos, "\n" + title + " " + number);
                        lyricsArea.getArea().refreshStyle();
                    });
                } else {
                    int nextLinePos = nextLinePos(lyricsArea.getTextAndChords(), caretPos);
                    if (nextLinePos >= lyricsArea.getTextAndChords().length()) {
                        Platform.runLater(() -> {
                            lyricsArea.getArea().replaceText(nextLinePos, nextLinePos, "\n\n" + title + " " + number + "\n");
                            lyricsArea.getArea().refreshStyle();
                        });
                    } else {
                        Platform.runLater(() -> {
                            lyricsArea.getArea().replaceText(nextLinePos, nextLinePos, title + " " + number + "\n");
                            lyricsArea.getArea().refreshStyle();
                        });
                    }
                }
            }
        }
    }

}
