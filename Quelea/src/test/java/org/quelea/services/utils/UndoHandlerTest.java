package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UndoHandlerTest")
public class UndoHandlerTest {

    @Test
    @DisplayName("Undo and redo walk through the recorded text and stop at each end")
    public void undoRedoHistory() {
        UndoHandler history = new UndoHandler();
        Assertions.assertFalse(history.canUndo());
        Assertions.assertFalse(history.canRedo());

        history.add("", "A");
        history.add("A", "AB");
        history.add("AB", "ABC");
        Assertions.assertEquals(3, history.getCount());
        Assertions.assertEquals("AB", history.undo());
        Assertions.assertEquals("A", history.undo());
        Assertions.assertEquals("", history.undo());
        Assertions.assertFalse(history.canUndo());
        Assertions.assertEquals("", history.undo());
        Assertions.assertEquals("A", history.redo());
        Assertions.assertEquals("AB", history.redo());
        Assertions.assertEquals("ABC", history.redo());
        Assertions.assertFalse(history.canRedo());
        Assertions.assertEquals("ABC", history.redo());
    }

    @Test
    @DisplayName("Editing after undo discards the abandoned redo history")
    public void editingAfterUndoBranchesHistory() {
        UndoHandler history = new UndoHandler();
        history.add("", "A");
        history.add("A", "AB");
        history.add("AB", "ABC");
        Assertions.assertEquals("AB", history.undo());
        Assertions.assertEquals("A", history.undo());
        history.add("A", "AX");
        Assertions.assertFalse(history.canRedo());
        Assertions.assertEquals("A", history.undo());
        Assertions.assertEquals("", history.undo());
        Assertions.assertEquals("A", history.redo());
        Assertions.assertEquals("AX", history.redo());
        Assertions.assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("Editing after undoing to the start keeps the original text")
    public void editingAfterUndoToStart() {
        UndoHandler history = new UndoHandler();
        history.add("original", "first");
        history.add("first", "second");
        Assertions.assertEquals("first", history.undo());
        Assertions.assertEquals("original", history.undo());
        history.add("original", "replacement");
        Assertions.assertFalse(history.canRedo());
        Assertions.assertEquals("original", history.undo());
        Assertions.assertEquals("replacement", history.redo());
    }

    @Test
    @DisplayName("Clearing history allows a new sequence of edits")
    public void clearStartsANewHistory() {
        UndoHandler history = new UndoHandler();
        history.add("old", "changed");
        history.clearUndo();
        Assertions.assertEquals(0, history.getCount());
        Assertions.assertFalse(history.canUndo());
        Assertions.assertFalse(history.canRedo());
        history.add("new", "newer");
        Assertions.assertEquals("new", history.undo());
        Assertions.assertEquals("newer", history.redo());
    }

    @Test
    @DisplayName("The caret follows an insertion when undoing and redoing")
    public void caretFollowsInsertion() {
        UndoHandler history = new UndoHandler();
        history.add("abc", "aXbc");
        Assertions.assertEquals("abc", history.undo());
        Assertions.assertEquals(1, history.getCaretPos(true));
        Assertions.assertEquals("aXbc", history.redo());
        Assertions.assertEquals(2, history.getCaretPos(false));
    }

    @Test
    @DisplayName("Caret positions include preceding lines")
    public void caretOnSecondLine() {
        UndoHandler history = new UndoHandler();
        history.add("first\nsecond", "first\nsecond!");
        Assertions.assertEquals("first\nsecond", history.undo());
        Assertions.assertEquals(12, history.getCaretPos(true));
        Assertions.assertEquals("first\nsecond!", history.redo());
        Assertions.assertEquals(13, history.getCaretPos(false));
    }
}
