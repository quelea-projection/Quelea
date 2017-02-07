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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.reactfx.EventStream;

/**
 *
 * @author Michael
 */
public class LyricsTextArea extends InlineCssTextArea {

    public LyricsTextArea() {
        ContextMenu contextMenu = new ContextMenu();
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        MenuItem paste = new MenuItem(LabelGrabber.INSTANCE.getLabel("paste.label"));
        contextMenu.setOnShown(e -> {
            paste.setDisable(!systemClipboard.hasContent(DataFormat.PLAIN_TEXT));
        });

        paste.setOnAction(e -> {
            String clipboardText = systemClipboard.getString();
            this.insertText(this.getCaretPosition(), clipboardText);
        });

        contextMenu.getItems().add(paste);
        this.setContextMenu(contextMenu);
        textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshStyle();
                    }
                });
            }
        });
        UndoManager blankManager = new UndoManager() {

            @Override
            public boolean undo() {
                return false;
            }

            @Override
            public boolean redo() {
                return false;
            }

            @Override
            public ObservableBooleanValue undoAvailableProperty() {
                return new SimpleBooleanProperty(false);
            }

            @Override
            public boolean isUndoAvailable() {
                return false;
            }

            @Override
            public ObservableBooleanValue redoAvailableProperty() {
                return new SimpleBooleanProperty(false);
            }

            @Override
            public boolean isRedoAvailable() {
                return false;
            }

            @Override
            public ObservableBooleanValue performingActionProperty() {
                return new SimpleBooleanProperty(false);
            }

            @Override
            public boolean isPerformingAction() {
                return false;
            }

            @Override
            public void preventMerge() {
            }

            @Override
            public void forgetHistory() {
            }

            @Override
            public UndoManager.UndoPosition getCurrentPosition() {
                return null;
            }

            @Override
            public ObservableBooleanValue atMarkedPositionProperty() {
                return null;
            }

            @Override
            public boolean isAtMarkedPosition() {
                return true;
            }

            @Override
            public void close() {
            }
        };
        setUndoManager(new UndoManagerFactory() {
            @Override
            public <C> UndoManager create(EventStream<C> stream, Function<? super C, ? extends C> fnctn, Consumer<C> cnsmr) {
                return blankManager;
            }

            @Override
            public <C> UndoManager create(EventStream<C> stream, Function<? super C, ? extends C> fnctn, Consumer<C> cnsmr, BiFunction<C, C, Optional<C>> bf) {
                return blankManager;
            }
        });
    }

    public void refreshStyle() {
        setStyles(getText());
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
                setStyle(charPos, charPos + line.length(), "-fx-fill: blue; -fx-font-weight: bold;");
            } else if (new LineTypeChecker(line).getLineType() == Type.CHORDS) {
                setStyle(charPos, charPos + line.length(), "-fx-fill: grey; -fx-font-style: italic;");
            } else if (new LineTypeChecker(line).getLineType() == Type.NONBREAK) {
                setStyle(charPos, charPos + line.length(), "-fx-fill: red; -fx-font-weight: bold;");
            } else if(new LineTypeChecker(line).getLineType() != new LineTypeChecker(oldLine).getLineType()) {
                setStyle(charPos, charPos + line.length(), "");
            }
            charPos += line.length() + 1;
        }
        oldLines = lines;
    }

}
