package org.quelea.windows.lyrics;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodRequests;
import org.fxmisc.richtext.InlineCssTextArea;

import java.util.Optional;

/**
 * Workaround for entering non-latin characters in the lyrics area.
 * Copied from https://github.com/FXMisc/RichTextFX/issues/146
 *
 * @author Arvid
 */
public class InputMethodRequestsObject implements InputMethodRequests {
    private InlineCssTextArea textArea;

    /**
     * Create a InputMethodRequest as a workaround for non-latin characters.
     *
     * @param textArea the InlineCssTextArea the object will be used on.
     */
    public InputMethodRequestsObject(InlineCssTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public String getSelectedText() {
        return "";
    }

    @Override
    public int getLocationOffset(int x, int y) {
        return 0;
    }

    @Override
    public void cancelLatestCommittedText() {

    }

    @Override
    public Point2D getTextLocation(int offset) {
        // Method for supporting Pinyin input on Mac.
        // TODO: Apparently only tested on MacOS, so might need more testing.
        //  Doesn't seem to affect any other language input as far as I can tell.
        Optional<Bounds> caretPositionBounds = textArea.getCaretBounds();
        if (caretPositionBounds.isPresent()) {
            Bounds bounds = caretPositionBounds.get();
            return new Point2D(bounds.getMaxX() - 5, bounds.getMaxY());
        }

        throw new NullPointerException();
    }
}