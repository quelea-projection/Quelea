/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.newsong;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;

/**
 * Shows one slide of the song being edited the way it will look live: with
 * the song's theme, the font size shared by all of its slides and its
 * translation lines. A small card that floats over the top right corner of
 * the lyrics in the song editor (like the song preview in the library),
 * letting clicks through.
 */
public class SlidePreviewPane extends VBox {

    /** Set while there's no slide to show; the card is then hidden (see base.css). */
    private static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");
    /** Width of the slide in the card. */
    private static final double WIDTH = 288;
    private final DisplayCanvas canvas;
    private final LyricDrawer drawer;
    private final Label caption;
    private SongDisplayable song;
    private ThemeDTO theme = ThemeDTO.DEFAULT_THEME;
    private int index;
    private ThemeDTO drawnTheme;

    public SlidePreviewPane() {
        getStyleClass().add("slide-preview");
        setSpacing(4);
        setFillWidth(false);
        caption = new Label();
        caption.getStyleClass().add("slide-preview-caption");
        canvas = new DisplayCanvas(false, this::redraw, DisplayCanvas.Priority.LOW);
        double height = Math.round(WIDTH / projectorRatio());
        canvas.setMinSize(WIDTH, height);
        canvas.setPrefSize(WIDTH, height);
        canvas.setMaxSize(WIDTH, height);
        Rectangle clip = new Rectangle(WIDTH, height);
        canvas.setClip(clip);
        drawer = new LyricDrawer();
        drawer.setCanvas(canvas);
        // The theme and text change all the time here, so don't cache sizes
        drawer.setUseFontSizeCache(false);
        // A still frame for video backgrounds: no extra video (and audio) player
        drawer.setPlayVideo(false);
        getChildren().addAll(caption, canvas);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMouseTransparent(true);
        pseudoClassStateChanged(EMPTY, true);
    }

    private static double projectorRatio() {
        DisplayStage projector = QueleaApp.get().getProjectionWindow();
        if (projector == null || projector.getHeight() <= 0 || projector.getWidth() <= 0) {
            return 16.0 / 9;
        }
        return projector.getWidth() / projector.getHeight();
    }

    /**
     * Show a slide of a song.
     *
     * @param song the song (a copy that isn't shown anywhere else.)
     * @param theme the theme to draw it with.
     * @param index the slide to show; kept within the song's slides.
     */
    public void show(SongDisplayable song, ThemeDTO theme, int index) {
        this.song = song;
        this.theme = theme == null ? ThemeDTO.DEFAULT_THEME : theme;
        this.index = index;
        redraw();
    }

    /**
     * @return the slide shown, or -1 if there is none.
     */
    public int getShownIndex() {
        return song == null || song.getSections().length == 0 ? -1 : index;
    }

    /**
     * Work out which slide of the lyrics the caret is in. Slides are
     * separated by blank lines. On a blank line between two slides the caret
     * counts as being in the next one; on a new, still empty line after the
     * last slide it counts as being in that slide.
     *
     * @param lyrics the lyrics text.
     * @param caret the caret position in the text.
     * @return the index of the slide, from 0.
     */
    public static int sectionAt(String lyrics, int caret) {
        String text = lyrics.replace("\r\n", "\n").replace('\r', '\n');
        // Positions shift when CRLFs are replaced: count them before the caret
        int crlfsBefore = 0;
        int limit = Math.max(0, Math.min(caret, lyrics.length()));
        for (int i = lyrics.indexOf("\r\n"); i >= 0 && i < limit - 1; i = lyrics.indexOf("\r\n", i + 2)) {
            crlfsBefore++;
        }
        int pos = Math.max(0, Math.min(limit - crlfsBefore, text.length()));
        int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
        int lineEnd = text.indexOf('\n', pos);
        if (lineEnd < 0) {
            lineEnd = text.length();
        }

        // Count the slides that start before the caret's line
        int count = 0;
        boolean inSection = false;
        String linesBefore = lineStart == 0 ? "" : text.substring(0, lineStart - 1);
        for (String line : linesBefore.split("\n", -1)) {
            if (line.trim().isEmpty()) {
                inSection = false;
            } else if (!inSection) {
                count++;
                inSection = true;
            }
        }
        boolean blankLine = text.substring(lineStart, lineEnd).trim().isEmpty();
        boolean textFollows = !text.substring(lineEnd).trim().isEmpty();
        if (!blankLine) {
            return inSection ? count - 1 : count;
        }
        if (textFollows || count == 0) {
            return count; // a gap: the slide that comes next
        }
        return count - 1; // a new line at the end of the last slide
    }

    private void redraw() {
        if (drawer == null) {
            return; // called by the canvas while it's being created
        }
        if (!theme.equals(drawnTheme)) {
            drawer.setTheme(theme);
            drawnTheme = theme;
        }
        int count = song == null ? 0 : song.getSections().length;
        pseudoClassStateChanged(EMPTY, count == 0);
        if (count == 0) {
            caption.setText("");
            drawer.setText(new String[0], null, null, false, -1);
            return;
        }
        index = Math.max(0, Math.min(index, count - 1));
        caption.setText(LabelGrabber.INSTANCE.getLabel("song.preview.slide")
                .replace("$1", Integer.toString(index + 1))
                .replace("$2", Integer.toString(count)));
        drawer.setText(song, index);
    }
}
