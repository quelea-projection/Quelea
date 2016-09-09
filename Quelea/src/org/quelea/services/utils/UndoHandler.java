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
package org.quelea.services.utils;

import java.util.ArrayList;

/**
 * Class for handling undo/redo actions for a set of strings.
 *
 * @author Arvid
 */
public class UndoHandler {

    private final ArrayList<String> changes = new ArrayList<>();
    private int undoCount = 0;
    private boolean undo = false;

    /**
     * Reverse one step in the change list.
     *
     * @return Previous text.
     */
    public String undo() {
        if (undoCount > 0) {
            undo = true;
            undoCount--;
            return changes.get(undoCount);
        }
        return changes.get(0);
    }

    /**
     * Move forward one step in the change list.
     *
     * @return Next text.
     */
    public String redo() {
        if (undoCount < changes.size() - 1) {
            undo = true;
            undoCount++;
            return changes.get(undoCount);
        }
        return changes.get(changes.size() - 1);
    }

    /**
     * Add text to the change list.
     *
     * @param oldText The pre-edited text (only relevant for first entry).
     * @param newText The recently changed text.
     */
    public void add(String oldText, String newText) {
        if (undoCount == 0) {
            changes.add(0, oldText);
        }
        undoCount++;
        changes.add(undoCount, newText);
        if (undoCount < changes.size() - 1) {
            while (undoCount + 1 < changes.size()) {
                changes.remove(undoCount + 1);
            }
        }
    }

    /**
     * Check if text is updated by an undo action or by re-styling (e.g. for SpellTextArea).
     *
     * @return true if text is changed by undo, false otherwise.
     */
    public boolean isUndo() {
        return undo;
    }

    /**
     * Set if text is updated by an undo action or by re-styling (e.g. for SpellTextArea).
     *
     * @param undo true if text is changed by undo, false otherwise.
     */
    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    /**
     * Get current change count.
     *
     * @return change count.
     */
    public int getCount() {
        return undoCount;
    }

    /**
     * Get caret position for the changed line.
     *
     * @param undo true if last change was undo, false if it was redo.
     * @return caret postion for the end of the changed line.
     */
    public int getCaretPos(boolean undo) {
        int prev = 0;
        if (undo && undoCount != changes.size() - 1) {
            prev = undoCount + 1;
        } else if (!undo && undoCount != 0) {
            prev = undoCount - 1;
        }
        String[] partsOld = changes.get(prev).split("\n");
        String[] partsNew = changes.get(undoCount).split("\n");
        int caretPos = 0;
        int i = 0;
        for (String s : partsOld) {
            if (partsNew.length - 1 >= i) {
                if (!s.equals(partsNew[i])) {
                    int j = 0;
                    for (char c : partsNew[i].toCharArray()) {
                        if (c != s.charAt(j)) {
                            break;
                        }
                        caretPos++;
                        j++;
                    }
                    if (partsNew[i].length() > s.length()) {
                        caretPos += (partsNew[i].length() - s.length());
                    }
                    break;
                }
                caretPos += partsNew[i].length();
            }
            i++;
        }
        return caretPos + i;
    }

    /**
     * Clear the change list.
     */
    public void clearUndo() {
        changes.clear();
        undoCount = 0;
    }

    /**
     * Check if undo is possible.
     *
     * @return true if change list has another undo value.
     */
    public boolean canUndo() {
        return undoCount > 0;
    }

    /**
     * Check if redo is possible.
     *
     * @return true if change list has another redo value.
     */
    public boolean canRedo() {
        return undoCount < changes.size() - 1;
    }

}
