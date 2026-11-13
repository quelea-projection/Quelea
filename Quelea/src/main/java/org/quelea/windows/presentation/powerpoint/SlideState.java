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
package org.quelea.windows.presentation.powerpoint;

import java.util.Objects;

/**
 * The state of the PowerPoint slide show Quelea controls: the current slide
 * (1-based, 0 if unknown), the number of slides (0 if unknown) and the mode.
 */
public final class SlideState {

    public enum Mode {
        /** A slide is shown. */
        RUNNING,
        /** The show is blacked out. */
        BLACK,
        /** The show is whited out. */
        WHITE,
        /** The show is paused. */
        PAUSED,
        /** The show has gone past its last slide. */
        DONE,
        /** PowerPoint is running but no slide show is. */
        NOSHOW,
        /** PowerPoint is not running. */
        NOTRUNNING,
        /** The backend cannot tell (e.g. PowerPoint Viewer). */
        UNKNOWN
    }

    public static final SlideState NOT_RUNNING = new SlideState(0, 0, Mode.NOTRUNNING);
    public static final SlideState UNKNOWN = new SlideState(0, 0, Mode.UNKNOWN);

    private final int slide;
    private final int total;
    private final Mode mode;

    public SlideState(int slide, int total, Mode mode) {
        this.slide = Math.max(0, slide);
        this.total = Math.max(0, total);
        this.mode = Objects.requireNonNull(mode);
    }

    /**
     * Parse the "slide|total|MODE" string the AppleScript returns.
     *
     * @param text the text to parse.
     * @return the state.
     * @throws IllegalArgumentException if the text is not in that format.
     */
    public static SlideState parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("No state");
        }
        String[] parts = text.trim().split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Bad state: " + text);
        }
        try {
            return new SlideState(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                    Mode.valueOf(parts[2].trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Bad state: " + text, ex);
        }
    }

    public int getSlide() {
        return slide;
    }

    public int getTotal() {
        return total;
    }

    public Mode getMode() {
        return mode;
    }

    /**
     * @return true if a slide show is on screen (showing a slide, black,
     * white, paused or at its end).
     */
    public boolean isShowing() {
        return mode == Mode.RUNNING || mode == Mode.BLACK || mode == Mode.WHITE
                || mode == Mode.PAUSED || mode == Mode.DONE;
    }

    public boolean isBlack() {
        return mode == Mode.BLACK;
    }

    /**
     * @return true if the show is on (or past) its last slide.
     */
    public boolean isAtEnd() {
        return mode == Mode.DONE || (total > 0 && slide >= total);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SlideState)) {
            return false;
        }
        SlideState other = (SlideState) o;
        return slide == other.slide && total == other.total && mode == other.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slide, total, mode);
    }

    @Override
    public String toString() {
        return slide + "|" + total + "|" + mode;
    }
}
