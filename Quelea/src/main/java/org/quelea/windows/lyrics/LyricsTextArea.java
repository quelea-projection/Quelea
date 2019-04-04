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
package org.quelea.windows.lyrics;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.undo.UndoManagerFactory;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.quelea.services.utils.QueleaProperties;

/**
 * @author Michael
 */
public class LyricsTextArea extends StackPane {

    private InlineCssTextArea textArea;

    public LyricsTextArea() {
        textArea = new InlineCssTextArea();
        ContextMenu contextMenu = new ContextMenu();
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        MenuItem paste = new MenuItem(LabelGrabber.INSTANCE.getLabel("paste.label"));
        contextMenu.setOnShown(e -> {
            paste.setDisable(!systemClipboard.hasContent(DataFormat.PLAIN_TEXT));
        });

        paste.setOnAction(e -> {
            String clipboardText = systemClipboard.getString();
            textArea.insertText(textArea.getCaretPosition(), clipboardText);
        });

        contextMenu.getItems().add(paste);
        textArea.setContextMenu(contextMenu);
        textArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            Platform.runLater(this::refreshStyle);
        });

        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 10pt;");
        textArea.setUndoManager(UndoManagerFactory.zeroHistorySingleChangeUM(textArea.richChanges()));
        getChildren().add(new VirtualizedScrollPane<>(textArea));
        textArea.getStyleClass().add("text-area");
    }

    public InlineCssTextArea getTextArea() {
        return textArea;
    }

    public void refreshStyle() {
        setStyles(textArea.getText());
    }

    private String[] oldLines;

    private void setStyles(String text) {
        String[] lines = text.split("\n");
        int charPos = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String oldLine = null;
            if (oldLines != null && i < oldLines.length) {
                oldLine = oldLines[i];
            }
            if (new LineTypeChecker(line).getLineType() == Type.TITLE) {
                textArea.clearStyle(charPos, charPos + line.length());
                if (QueleaProperties.get().getUseDarkTheme()) {
                    textArea.setStyle(charPos, charPos + line.length(), "-fx-fill: rgb(50,160,255); -fx-font-weight: bold;");
                } else {
                    textArea.setStyle(charPos, charPos + line.length(), "-fx-fill: blue; -fx-font-weight: bold;");
                }
            } else if (new LineTypeChecker(line).getLineType() == Type.CHORDS) {
                textArea.clearStyle(charPos, charPos + line.length());
                if (QueleaProperties.get().getUseDarkTheme()) {
                    textArea.setStyle(charPos, charPos + line.length(), "-fx-fill: rgb(200,200,200); -fx-font-style: italic;");
                } else {
                    textArea.setStyle(charPos, charPos + line.length(), "-fx-fill: grey; -fx-font-style: italic;");
                }
            } else if (new LineTypeChecker(line).getLineType() == Type.NONBREAK) {
                textArea.clearStyle(charPos, charPos + line.length());
                textArea.setStyle(charPos, charPos + line.length(), "-fx-fill: red; -fx-font-weight: bold;");
            } else if (new LineTypeChecker(line).getLineType() != new LineTypeChecker(oldLine).getLineType()) {
                textArea.clearStyle(charPos, charPos + line.length());
                textArea.setStyle(charPos, charPos + line.length(), "");
            }
            charPos += line.length() + 1;
        }
        oldLines = lines;
    }

}
