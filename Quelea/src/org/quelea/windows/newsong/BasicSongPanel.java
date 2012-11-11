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

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.javafx.dialog.Dialog;
import org.quelea.QueleaApp;
import org.quelea.data.chord.ChordLineTransposer;
import org.quelea.data.chord.ChordTransposer;
import org.quelea.data.chord.TransposeDialog;
import org.quelea.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.quelea.services.utils.LoggerUtils;

/**
 * The panel that manages the basic input of song information - the title,
 * author and lyrics.
 * <p/>
 * @author Michael
 */
public class BasicSongPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final TextArea lyricsArea;
    private final TextField titleField;
    private final TextField authorField;
    private final Button transposeButton;
    private final TransposeDialog transposeDialog;

    /**
     * Create and initialise the song panel.
     */
    public BasicSongPanel() {
        VBox centrePanel = new VBox();
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
        lyricsArea = new TextArea();
//        SpellChecker.register(lyricsArea); //TODO: Spell check.
        
//        lyricsArea.getDocument().addDocumentListener(new DocumentListener() { //TODO: Highlight
//            public void insertUpdate(DocumentEvent e) {
//                update();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                update();
//            }
//
//            public void changedUpdate(DocumentEvent e) {
//                update();
//            }
//
//            private void update() {
//                checkChords();
//                doHighlight();
//            }
//        });
        
        VBox lyricsPanel = new VBox();
        VBox.setVgrow(lyricsPanel, Priority.ALWAYS);
        ToolBar lyricsToolbar = new ToolBar();
        lyricsToolbar.getItems().add(getDictButton());
        lyricsToolbar.getItems().add(getAposButton());
        lyricsToolbar.getItems().add(getTrimLinesButton());
        transposeButton = getTransposeButton();
        lyricsToolbar.getItems().add(transposeButton);
        lyricsPanel.getChildren().add(lyricsToolbar);
        VBox.setVgrow(lyricsArea, Priority.ALWAYS);
        lyricsPanel.getChildren().add(lyricsArea);
        centrePanel.getChildren().add(lyricsPanel);
        setCenter(centrePanel);
    }

    /**
     * Check whether any chords are present and enable / disable the transpose
     * button appropriately.
     */
    private void checkChords() {
        String[] lines = lyricsArea.getText().split("\n");
        for(String line : lines) {
            if(new LineTypeChecker(line).getLineType() == Type.CHORDS) {
                transposeButton.setDisable(false);
                return;
            }
        }
        transposeButton.setDisable(true);
    }
    private final List<Object> highlights = new ArrayList<>();

    /**
     * Manage the highlighting.
     */
//    private void doHighlight() {
//        for(Object highlight : highlights) {
//            lyricsArea.getHighlighter().removeHighlight(highlight);
//        }
//        highlights.clear();
//        try {
//            Highlighter hilite = lyricsArea.getHighlighter();
//            String text = lyricsArea.getText();
//            String[] lines = text.split("\n");
//            List<HighlightIndex> indexes = new ArrayList<>();
//            int offset = 0;
//            for(int i = 0; i < lines.length; i++) {
//                String line = lines[i];
//                LineTypeChecker.Type type = new LineTypeChecker(line).getLineType();
//                if(type == LineTypeChecker.Type.TITLE && i > 0 && !lines[i - 1].trim().isEmpty()) {
//                    type = LineTypeChecker.Type.NORMAL;
//                }
//                if(type != LineTypeChecker.Type.NORMAL) {
//                    int startIndex = offset;
//                    int endIndex = startIndex + line.length();
//                    Color highlightColor = type.getHighlightColor();
//                    if(highlightColor != null) {
//                        indexes.add(new HighlightIndex(startIndex, endIndex, highlightColor));
//                    }
//                }
//                offset += line.length() + 1;
//            }
//
//            for(HighlightIndex index : indexes) {
//                highlights.add(hilite.addHighlight(index.getStartIndex(), index.getEndIndex(), new DefaultHighlightPainter(index.getHighlightColor())));
//            }
//        }
//        catch(BadLocationException ex) {
//            LOGGER.log(Level.SEVERE, "Bug in highlighting", ex);
//        }
//    }

    /**
     * Get the button used for transposing the chords.
     * <p/>
     * @return the button used for transposing the chords.
     */
    private Button getTransposeButton() {
        Button ret = new Button("", new ImageView(new Image("file:icons/transpose.png", 24, 24, false, true)));
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("transpose.tooltip")));
        ret.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                String originalKey = getKey(0);
                if(originalKey == null) {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("no.chords.title"), LabelGrabber.INSTANCE.getLabel("no.chords.message"));
                    return;
                }
                transposeDialog.setKey(originalKey);
                transposeDialog.show();
                int semitones = transposeDialog.getSemitones();

                TextField keyField = QueleaApp.get().getMainWindow().getSongEntryWindow().getDetailedSongPanel().getKeyField();
                if(!keyField.getText().isEmpty()) {
                    keyField.setText(new ChordTransposer(keyField.getText()).transpose(semitones, null));
                }

                String key = getKey(semitones);

                StringBuilder newText = new StringBuilder(getLyricsField().getText().length());
                for(String line : getLyricsField().getText().split("\n")) {
                    if(new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                        newText.append(new ChordLineTransposer(line).transpose(semitones, key));
                    }
                    else {
                        newText.append(line);
                    }
                    newText.append('\n');
                }
                int pos = getLyricsField().getCaretPosition();
                getLyricsField().setText(newText.toString());
            }
        });
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
        if(key == null || key.isEmpty()) {
            for(String line : getLyricsField().getText().split("\n")) {
                if(new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                    String first;
                    int i = 0;
                    do {
                        first = line.split("\\s+")[i++];
                    } while(first.isEmpty());
                    key = new ChordTransposer(first).transpose(semitones, null);
                    if(key.length() > 2) {
                        key = key.substring(0, 2);
                    }
                    if(key.length() == 2) {
                        if(key.charAt(1) == 'B') {
                            key = Character.toString(key.charAt(0)) + "b";
                        }
                        else if(key.charAt(1) != 'b' && key.charAt(1) != '#') {
                            key = Character.toString(key.charAt(0));
                        }
                    }
                    break;
                }
            }
        }

        if(key.isEmpty()) {
            key = null;
        }
        return key;
    }

    /**
     * Get the remove chords button.
     * <p/>
     * @return the remove chords button.
     */
    private Button getTrimLinesButton() {
        Button button = new Button("", new ImageView(new Image("file:icons/trimLines.png", 24, 24, false, true)));
        button.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("trim.lines.tooltip")));
        button.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                StringBuilder newText = new StringBuilder();
                for(String line : lyricsArea.getText().split("\n")) {
                    newText.append(line.trim()).append("\n");
                }
                lyricsArea.setText(newText.toString());
            }
        });
        return button;
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
//                SpellChecker.showSpellCheckerDialog(lyricsArea, SpellChecker.getOptions());
            }
        });
        button.setDisable(true); //TOOD: Enable
        return button;
    }

    /**
     * Get the button to fix apostrophes.
     * <p/>
     * @return the button to fix apostrophes.
     */
    private Button getAposButton() {
        Button button = new Button("", new ImageView(new Image("file:icons/apos.png", 24, 24, false, true)));
        button.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("fix.apos.label")));
        button.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                lyricsArea.setText(lyricsArea.getText().replace("`", "'").replace("’", "'"));
            }
        });
        return button;
    }

    /**
     * Reset this panel so new song data can be entered.
     */
    public void resetNewSong() {
        getTitleField().clear();
        getAuthorField().clear();
        getLyricsField().clear();
        getTitleField().requestFocus();
    }

    /**
     * Reset this panel so an existing song can be edited.
     * <p/>
     * @param song the song to edit.
     */
    public void resetEditSong(SongDisplayable song) {
        getTitleField().setText(song.getTitle());
        getAuthorField().setText(song.getAuthor());
        getLyricsField().setText(song.getLyrics(true, true));
        getLyricsField().requestFocus();
    }

    /**
     * Get the lyrics field.
     * <p/>
     * @return the lyrics field.
     */
    public TextArea getLyricsField() {
        return lyricsArea;
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

    private class customFTPolicy extends FocusTraversalPolicy {

        List<Component> c; // components in order

        private customFTPolicy(Component[] c) {
            this.c = Arrays.asList(c);
        }

        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            return c.get((c.indexOf(aComponent) + 1) % c.size());
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            int x = (c.indexOf(aComponent) - 1);
            return c.get((x < 0) ? c.size() - 1 : x);
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            return c.get(0);
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            return c.get(c.size() - 1);
        }

        @Override
        public Component getDefaultComponent(Container aContainer) {
            return c.get(0);
        }
    }
}
