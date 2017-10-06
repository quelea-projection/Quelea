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

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.UndoHandler;
import org.quelea.windows.lyrics.LyricsTextArea;

/**
 * The spell text area component - wraps a text area to provide spell check
 * capabilities.
 * <p/>
 * @author Michael
 */
public class SpellTextArea extends StackPane {

    private static final int CHECK_FREQ = 1000;
    private static final double WARNING_OPACITY = 0.5;
    private SpellingDialog dialog;
    private LyricsTextArea area;
    private KeyCode runSpellKey;
    private Speller speller;
    private ImageView warning;
    private Thread checkerThread;
    private volatile SimpleBooleanProperty spellingOkProperty;
    private UndoHandler undoHandler;

    /**
     * Create a new spell text area.
     */
    public SpellTextArea() {
        runSpellKey = KeyCode.F7;
        undoHandler = new UndoHandler();
        if (QueleaProperties.get().isDictionaryEnabled()) {
            speller = new Speller(QueleaProperties.get().getDictionary());
        } else {
            speller = new Speller(null);
        }
        area = new LyricsTextArea();
        spellingOkProperty = new SimpleBooleanProperty(speller.checkText(area.getText(), true));
        getChildren().add(area);
        warning = new ImageView("file:icons/warning.png");
        Tooltip.install(warning, new Tooltip(LabelGrabber.INSTANCE.getLabel("spelling.errors.in.doc.label")));
        StackPane.setAlignment(warning, Pos.TOP_RIGHT);
        StackPane.setMargin(warning, new Insets(5));
        warning.setOpacity(0);
        getChildren().add(warning);
        dialog = new SpellingDialog(speller);

        MenuItem undoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("undo.label"));
        undoItem.setOnAction(e -> {
            undo();
        });
        area.getContextMenu().getItems().add(undoItem);
        MenuItem redoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("redo.label"));
        redoItem.setOnAction(e -> {
            redo();
        });
        area.getContextMenu().getItems().add(redoItem);
        area.getContextMenu().setOnShown(e -> {
            undoItem.setDisable(!undoHandler.canUndo());
            redoItem.setDisable(!undoHandler.canRedo());
        });

        area.setOnKeyPressed((KeyEvent t) -> {
            if (t.getCode() == runSpellKey) {
                runSpellCheck();
            }
            if (t.getCode() == KeyCode.ENTER && t.isShiftDown()) {
                area.replaceText(area.getCaretPosition(), area.getCaretPosition(), "\n<>");
                area.refreshStyle();
            }
            if (t.getCode() == KeyCode.Z && t.isShortcutDown()) {
                undo();
            }
            if (t.getCode() == KeyCode.Y && t.isShortcutDown()) {
                redo();
            }
        });
        area.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, final String t1) {
                updateSpelling(false);
                if (!undoHandler.isUndo()) {
                    undoHandler.add(t, t1);
                } else {
                    undoHandler.setUndo(false);
                }

                if (checkerThread != null && checkerThread.isAlive()) {
                    checkerThread.interrupt();
                }
                checkerThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(CHECK_FREQ);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateSpelling(true);
                                }
                            });
                        } catch (InterruptedException ex) {
                            //Quit silently if interrupted
                        }
                    }
                };
                checkerThread.start();
            }
        });
    }

    /**
     * Activate a spell check for this area.
     */
    public void runSpellCheck() {
        dialog.check(SpellTextArea.this);
    }

    public void setDictionary(Dictionary dict) {
        speller.setDictionary(dict);
        updateSpelling(true);
    }

    /**
     * Check the spelling on this text area - called internally to update state,
     * but can be fired externally also.
     * <p/>
     * @param lastWord true if the last word should be included in the spell
     * check.
     */
    public void updateSpelling(boolean lastWord) {
        spellingOkProperty.set(speller.checkText(getText(), lastWord));
        FadeTransition transition = new FadeTransition(Duration.seconds(0.2), warning);
        if (spellingOkProperty.get()) {
            transition.setFromValue(warning.getOpacity());
            transition.setToValue(0);
        } else {
            transition.setFromValue(warning.getOpacity());
            transition.setToValue(WARNING_OPACITY);
        }
        transition.play();
    }

    /**
     * Get the boolean property representing whether the spelling is ok.
     *
     * @return true if the spelling is ok, false otherwise.
     */
    public BooleanProperty spellingOkProperty() {
        return spellingOkProperty;
    }

    /**
     * Get the underlying text area object used in this control.
     * <p/>
     * @return the text area object.
     */
    public LyricsTextArea getArea() {
        return area;
    }

    /**
     * Get the text on this text area, excluding any chords.
     * <p/>
     * @return the text area's text, without chord lines.
     */
    public String getText() {
        String[] lines = area.getText().split("\n");
        StringBuilder ret = new StringBuilder();
        for (String line : lines) {
            if (new LineTypeChecker(line).getLineType() != Type.CHORDS) {
                ret.append(line).append("\n");
            }
        }
        return ret.toString();
    }

    /**
     * Get the text on this text area, including any chords.
     * <p/>
     * @return the text area's text, with chord lines.
     */
    public String getTextAndChords() {
        String[] lines = area.getText().split("\n");
        StringBuilder ret = new StringBuilder();
        for (String line : lines) {
            ret.append(line).append("\n");
        }
        return ret.toString();
    }

    /**
     * Get the key used to run the spell check. F7 by default.
     * <p/>
     * @return the key used to run the spell check.
     */
    public KeyCode getRunSpellKey() {
        return runSpellKey;
    }

    /**
     * Set the keycode used to run the spell check. F7 by default.
     * <p/>
     * @param runSpellKey the key used to run the spell check.
     */
    public void setRunSpellKey(KeyCode runSpellKey) {
        this.runSpellKey = runSpellKey;
    }

    public void clearUndo() {
        if (undoHandler != null) {
            undoHandler.clearUndo();
        }
    }

    private void undo() {
        if (undoHandler.canUndo()) {
            String newText = undoHandler.undo();
            if (!newText.equals(getTextAndChords())) {
                area.replaceText(0, area.getText().length(), newText);
                int pos = undoHandler.getCaretPos(true);
                area.selectRange(pos, pos);
            }
        }
    }

    private void redo() {
        if (undoHandler.canRedo()) {
            String newText = undoHandler.redo();
            if (!newText.equals(getTextAndChords())) {
                area.replaceText(0, area.getText().length(), newText);
                int pos = undoHandler.getCaretPos(false);
                area.selectRange(pos, pos);
            }
        }
    }

}
