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
package org.quelea.services.languages.spelling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;

/**
 * The spelling dialog used for correcting misspelt words.
 * <p/>
 * @author Michael
 */
public class SpellingDialog {

    private Stage dialogStage;
    private FlowPane textPane;
    private ListView<String> suggestions;
    private Speller speller;
    private Set<String> wordsToCorrect;
    private Map<String, String> correctedWords;
    private String origText;
    private SpellTextArea area;

    /**
     * Create a spelling dialog with a particular speller (used for managing the
     * corrections.)
     * <p/>
     * @param speller the speller to use for checking the spelling.
     */
    public SpellingDialog(final Speller speller) {
        this.speller = speller;
        textPane = new FlowPane();
        suggestions = new ListView<>();
        correctedWords = new HashMap<>();
        Button ignoreButton = new Button(LabelGrabber.INSTANCE.getLabel("ignore.text"));
        ignoreButton.setMaxWidth(Double.MAX_VALUE);
        ignoreButton.setMinWidth(60);
        ignoreButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String replaceWord = wordsToCorrect.iterator().next();
                speller.addIgnoreWord(replaceWord);
                wordsToCorrect.remove(replaceWord);
                nextWord();
            }
        });
        Button addButton = new Button(LabelGrabber.INSTANCE.getLabel("add.text"));
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String replaceWord = wordsToCorrect.iterator().next();
                speller.addWord(replaceWord);
                wordsToCorrect.remove(replaceWord);
                nextWord();
            }
        });
        Button correctButton = new Button(LabelGrabber.INSTANCE.getLabel("correct.text"));
        correctButton.setMaxWidth(Double.MAX_VALUE);
        correctButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String replaceWord = wordsToCorrect.iterator().next();
                correctedWords.put(replaceWord, suggestions.getSelectionModel().getSelectedItem());
                StringBuilder replaceText = new StringBuilder();
                for(String line : area.getArea().getText().split("\n")) {
                    if(new LineTypeChecker(line).getLineType() != Type.CHORDS) {
                        line = line.replace(replaceWord, suggestions.getSelectionModel().getSelectedItem());
                    }
                    replaceText.append(line).append("\n");
                }
                area.getArea().replaceText(replaceText.toString().trim());
                wordsToCorrect.remove(replaceWord);
                nextWord();
            }
        });
        correctButton.disableProperty().bind(suggestions.getSelectionModel().selectedItemProperty().isNull());
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                dialogStage.hide();
            }
        });
        dialogStage = new Stage();
//        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
//        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle(LabelGrabber.INSTANCE.getLabel("spelling.check.title"));
        BorderPane mainPane = new BorderPane();
        VBox rightPanel = new VBox(5);
        rightPanel.setAlignment(Pos.CENTER);
        BorderPane.setMargin(rightPanel, new Insets(10));
        rightPanel.getChildren().add(ignoreButton);
        rightPanel.getChildren().add(addButton);
        rightPanel.getChildren().add(correctButton);
        rightPanel.setFillWidth(true);
        mainPane.setCenter(rightPanel);
        HBox southPanel = new HBox();
        southPanel.setAlignment(Pos.CENTER_RIGHT);
        BorderPane.setMargin(southPanel, new Insets(5));
        southPanel.getChildren().add(cancelButton);
        mainPane.setBottom(southPanel);
        VBox centrePanel = new VBox(10);
        centrePanel.getChildren().add(textPane);
        centrePanel.getChildren().add(suggestions);
        mainPane.setLeft(centrePanel);
        mainPane.setPadding(new Insets(5));
        dialogStage.setScene(new Scene(mainPane, 300, 200));
    }

    /**
     * Run a check on a particular spell text area.
     * <p/>
     * @param area the area to use to correct the text.
     */
    public void check(SpellTextArea area) {
        wordsToCorrect = speller.getMisspeltWords(area.getText());
        if(wordsToCorrect.isEmpty()) {
            return;
        }
        dialogStage.show();
        this.area = area;
        this.origText = area.getText();
        correctedWords.clear();
        nextWord();
    }

    /**
     * Navigate to the next misspelt word, or break out if complete.
     */
    private void nextWord() {
        if(wordsToCorrect.isEmpty()) {
            dialogStage.hide();
            area.updateSpelling(true);
            Button ok = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"));
            VBox.setMargin(ok, new Insets(10));
            final Stage doneSpellingStage = new Stage();
            doneSpellingStage.setTitle(LabelGrabber.INSTANCE.getLabel("complete.title"));
            ok.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    doneSpellingStage.hide();
                }
            });
            doneSpellingStage.initModality(Modality.WINDOW_MODAL);
            VBox spellingBox = new VBox();
            spellingBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("spelling.complete.text")));
            spellingBox.getChildren().add(ok);
            spellingBox.setAlignment(Pos.CENTER);
            spellingBox.setPadding(new Insets(15));
            doneSpellingStage.setScene(new Scene(spellingBox));
            doneSpellingStage.show();
            return;
        }
        List<String> origPieces = Arrays.asList(Pattern.compile(Speller.SPELLING_REGEX, Pattern.UNICODE_CHARACTER_CLASS).split(origText));
        String replaceWord = wordsToCorrect.iterator().next();
        int index = origPieces.indexOf(replaceWord);
        int lower = index - 6;
        if(lower < 0) {
            lower = 0;
        }
        int upper = index + 6;
        if(upper > origPieces.size() - 1) {
            upper = origPieces.size() - 1;
        }
        textPane.getChildren().clear();
        textPane.getChildren().add(new Text("..."));
        for(String str : origPieces.subList(lower, upper + 1)) {
            Text text = new Text(str + " ");
            if(str.equals(replaceWord)) {
                text.setFill(Color.RED);
            }
            textPane.getChildren().add(text);
        }
        textPane.getChildren().add(new Text("..."));
        suggestions.getItems().clear();
        for(String suggestion : speller.getSuggestions(replaceWord)) {
            suggestions.getItems().add(suggestion);
        }
    }
}
