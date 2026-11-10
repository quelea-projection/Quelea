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

import java.io.File;
import javafx.geometry.Rectangle2D;

/**
 * Controls PowerPoint on one platform. All methods block and are only called
 * from the {@link PowerPointService} worker thread, never the FX thread.
 */
interface PowerPointBackend {

    /**
     * Open the presentation and start its slide show.
     *
     * @param file the presentation.
     * @param screen the area Quelea projects to (screen coordinates), where the
     * show should go if the platform can steer that; null to leave it to
     * PowerPoint.
     */
    SlideState open(File file, Rectangle2D screen) throws PowerPointException;

    /**
     * Close the presentation opened with {@link #open(File)}.
     */
    SlideState close() throws PowerPointException;

    /**
     * Windows only: close whatever PowerPoint has open, after
     * {@link #open(File)} failed with {@link PowerPointException.Kind#ALREADY_RUNNING}.
     */
    default void closeAll() throws PowerPointException {
        close();
    }

    SlideState next() throws PowerPointException;

    SlideState previous() throws PowerPointException;

    /**
     * @param slide 1-based slide number.
     */
    SlideState gotoSlide(int slide) throws PowerPointException;

    SlideState setBlack(boolean black) throws PowerPointException;

    /**
     * Let PowerPoint loop the show itself, if the platform can. Returns false if
     * Quelea has to wrap around itself.
     */
    default boolean setLoop(boolean loop) throws PowerPointException {
        return false;
    }

    SlideState state() throws PowerPointException;

    /**
     * A problem the last command ran into without failing (e.g. the show
     * could not be moved to Quelea's screen), to tell the user about; cleared
     * by this call.
     *
     * @return the problem, or null.
     */
    default PowerPointException takeWarning() {
        return null;
    }

    /**
     * @return true if the show opened last is on the screen area that was
     * asked for (or none was), so Quelea's projection can make way for it.
     */
    default boolean isShowOnScreen() {
        return true;
    }

    void focus() throws PowerPointException;

    /**
     * @return true if {@link #state()} is cheap enough to call a few times a
     * second, so Quelea can follow slide changes made in PowerPoint itself.
     */
    boolean supportsPolling();

    void shutdown();
}
