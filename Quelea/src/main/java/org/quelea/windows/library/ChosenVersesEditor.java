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

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.bible.BibleReferenceParser;
import org.quelea.data.bible.BibleVersionText;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.style.StyleManager;

/**
 * A field for choosing which verses another version shows next to a passage,
 * for when Quelea's own matching is wrong. The passage is typed as in the
 * search field.
 *
 * @author Arvid
 */
public final class ChosenVersesEditor extends VBox {

    private static final String NUMBERS_ONLY = "[\\d\\s:,;\\-–.]+";

    private final TextField field = new TextField();
    private final Label note = new Label();
    private final Button apply = new Button(LabelGrabber.INSTANCE.getLabel("bible.match.apply.button"));
    private final Bible first;
    private final Bible other;
    private final BibleBook firstBook;
    private final String location;
    private final int rows;

    /**
     * Create the field.
     *
     * @param passage   the location of the passage in the first version.
     * @param first     the first version.
     * @param firstBook the book of the passage in the first version.
     * @param other     the version to choose the verses in.
     * @param current   how the passage is shown in that version now, or null.
     * @param rows      the number of verses of the passage in the first version.
     * @param onApply   called with the passage chosen, such as "Psalms 31:2-4",
     *                  or null to match the version automatically again.
     * @param onCancel  called when the field is closed without a change.
     */
    public ChosenVersesEditor(String passage, Bible first, BibleBook firstBook, Bible other, BibleVersionText current,
                              int rows, Consumer<String> onApply, Runnable onCancel) {
        super(8);
        this.first = first;
        this.other = other;
        this.firstBook = firstBook;
        this.location = current == null ? null : current.getLocation();
        this.rows = rows;
        getStyleClass().add("bible-match-editor");
        Label heading = new Label(LabelGrabber.INSTANCE.getLabel("bible.match.editor.label")
                .replace("$1", passage).replace("$2", other.getBibleName()));
        heading.getStyleClass().add("bible-match-name");
        heading.setWrapText(true);
        heading.setMinHeight(Region.USE_PREF_SIZE);
        heading.setLabelFor(field);
        field.getStyleClass().add("bible-match-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        StyleManager.markPrimaryButton(apply);
        apply.setMinWidth(Region.USE_PREF_SIZE);
        apply.setOnAction(event -> {
            BibleReferenceParser.Reference chosen = parse();
            if (chosen != null) {
                onApply.accept(chosen.getLocation());
            }
        });
        Button cancel = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"));
        cancel.setMinWidth(Region.USE_PREF_SIZE);
        cancel.setOnAction(event -> onCancel.run());
        field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !apply.isDisabled()) {
                apply.fire();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                onCancel.run();
                event.consume();
            }
        });
        HBox row = new HBox(6, field, apply, cancel);
        row.setAlignment(Pos.CENTER_LEFT);
        note.getStyleClass().add("bible-match-note");
        note.setWrapText(true);
        note.setMinHeight(Region.USE_PREF_SIZE);
        getChildren().addAll(heading, row, note);
        if (current != null && current.getMatch() == BibleVersionText.Match.CHOSEN) {
            Hyperlink reset = new Hyperlink(LabelGrabber.INSTANCE.getLabel("bible.match.reset"));
            reset.setOnAction(event -> onApply.accept(null));
            getChildren().add(reset);
        }
        String start = current == null ? null
                : current.getChosenReference() != null ? current.getChosenReference() : current.getLocation();
        if (start == null) {
            BibleBook book = firstBook == null ? null : other.getMatchingBook(firstBook);
            start = book == null ? "" : book.getBookName() + " ";
        }
        field.textProperty().addListener((obs, oldText, newText) -> updateNote());
        field.setText(start);
        updateNote();
    }

    /**
     * Put the cursor in the field.
     */
    public void focus() {
        field.requestFocus();
        field.end();
    }

    /**
     * Get the field the passage is typed in.
     *
     * @return the field.
     */
    public TextField getField() {
        return field;
    }

    /**
     * Read the passage typed in the field.
     *
     * @return the passage in the other version, or null if the field doesn't
     * hold one.
     */
    public BibleReferenceParser.Reference parse() {
        return parse(field.getText(), first, firstBook, other, location);
    }

    /**
     * Read a passage typed for another version. It's matched as in the search
     * field, so a book name can be shortened. A book can also be named as in
     * the first version, and chapters and verses without a book, such as
     * "31:2-4" or "2-4", are in the book and chapter shown now.
     *
     * @param text      the text typed.
     * @param first     the first version.
     * @param firstBook the book of the passage in the first version, or null.
     * @param other     the version to look the passage up in.
     * @param location  where the passage is in that version now, such as
     *                  "Psalms 31:1-4", or null if it isn't there.
     * @return the passage, or null if the text isn't a passage in that version.
     */
    public static BibleReferenceParser.Reference parse(String text, Bible first, BibleBook firstBook, Bible other,
                                                        String location) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String query = text.trim();
        if (query.matches(NUMBERS_ONLY)) {
            BibleBook book = location != null ? null : firstBook == null ? null : other.getMatchingBook(firstBook);
            String bookName = location != null ? location.replaceFirst(" \\d+:.*$", "") : book == null ? null : book.getBookName();
            if (bookName == null) {
                return null;
            }
            if (!query.contains(":") && location != null && location.matches(".* \\d+:.*")) {
                query = location.replaceFirst("^.* (\\d+):.*$", "$1") + ":" + query;
            }
            query = bookName + " " + query;
        }
        BibleReferenceParser.Reference ret = BibleReferenceParser.lookup(query, other).reference();
        if (ret != null) {
            return ret;
        }
        List<BibleReferenceParser.Reference> found = BibleReferenceParser.parse(query, other);
        if (!found.isEmpty()) {
            return found.getFirst();
        }
        // The book named as in the first version
        BibleReferenceParser.Lookup inFirst = BibleReferenceParser.lookup(query, first);
        if (inFirst.book() != null && !inFirst.passage().isEmpty()) {
            BibleBook book = other.getMatchingBook(inFirst.book());
            return BibleReferenceParser.resolvePassage(book, inFirst.passage());
        }
        return null;
    }

    private void updateNote() {
        BibleReferenceParser.Reference chosen = parse();
        apply.setDisable(chosen == null);
        boolean error = chosen == null && !field.getText().isBlank();
        note.getStyleClass().remove("bible-match-error");
        if (error) {
            note.getStyleClass().add("bible-match-error");
        }
        String firstName = first.getBibleName();
        if (chosen == null) {
            note.setText(LabelGrabber.INSTANCE.getLabel(field.getText().isBlank()
                    ? "bible.match.note.hint" : "bible.match.note.invalid").replace("$1", other.getBibleName()));
            return;
        }
        int count = chosen.verses().size();
        String how;
        if (count == rows) {
            how = LabelGrabber.INSTANCE.getLabel("bible.match.note.same").replace("$1", firstName);
        } else if (count > rows) {
            how = LabelGrabber.INSTANCE.getLabel("bible.match.note.more").replace("$1", Integer.toString(count))
                    .replace("$2", firstName).replace("$3", Integer.toString(rows));
        } else {
            how = LabelGrabber.INSTANCE.getLabel("bible.match.note.fewer").replace("$1", Integer.toString(count))
                    .replace("$2", firstName).replace("$3", Integer.toString(rows)).replace("$4", other.getBibleName());
        }
        note.setText(chosen.getLocation() + ": " + how);
    }
}
