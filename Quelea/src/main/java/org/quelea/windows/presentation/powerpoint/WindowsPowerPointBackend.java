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
import org.quelea.services.utils.QueleaProperties;

/**
 * Controls PowerPoint (VBScript) or PowerPoint Viewer (key events) on Windows.
 * This is the behaviour Quelea always had, now run off the FX thread; the
 * string answers of the scripts are turned into {@link SlideState}s here.
 */
class WindowsPowerPointBackend implements PowerPointBackend {

    private static boolean isViewer() {
        return QueleaProperties.get().getPPPath().contains("PPTVIEW");
    }

    @Override
    public SlideState open(File file, Rectangle2D screen) throws PowerPointException {
        // The screen is chosen as before: PowerPoint's own settings, or moving
        // the PowerPoint Viewer window (see WindowsPowerPointScripts).
        if (!new File(QueleaProperties.get().getPPPath()).exists()) {
            throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE,
                    "PowerPoint was not found at " + QueleaProperties.get().getPPPath());
        }
        String result = WindowsPowerPointScripts.openPresentation("\"" + file.getAbsolutePath() + "\"");
        if (result.contains("already running")) {
            throw new PowerPointException(PowerPointException.Kind.ALREADY_RUNNING, result);
        }
        if (result.startsWith("Could not") || result.startsWith("Interrupted")) {
            throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, result);
        }
        return state();
    }

    @Override
    public SlideState close() throws PowerPointException {
        WindowsPowerPointScripts.closePresentation();
        return SlideState.NOT_RUNNING;
    }

    @Override
    public void closeAll() throws PowerPointException {
        WindowsPowerPointScripts.closePresentation();
        try {
            Thread.sleep(500); // As before: give PowerPoint time to quit.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public SlideState next() throws PowerPointException {
        check(WindowsPowerPointScripts.gotoNext());
        return state();
    }

    @Override
    public SlideState previous() throws PowerPointException {
        check(WindowsPowerPointScripts.gotoPrevious());
        return state();
    }

    @Override
    public SlideState gotoSlide(int slide) throws PowerPointException {
        check(WindowsPowerPointScripts.gotoSlide(slide));
        return state();
    }

    @Override
    public SlideState setBlack(boolean black) throws PowerPointException {
        SlideState now = state();
        // The scripts can only toggle; with PowerPoint Viewer the state is
        // unknown, so toggle as Quelea always did.
        if (now.getMode() == SlideState.Mode.UNKNOWN || now.isBlack() != black) {
            check(WindowsPowerPointScripts.screenBlack());
            return state();
        }
        return now;
    }

    @Override
    public boolean setLoop(boolean loop) throws PowerPointException {
        if (isViewer()) {
            return false;
        }
        check(loop ? WindowsPowerPointScripts.loopPresentation() : WindowsPowerPointScripts.stopLoop());
        return true;
    }

    @Override
    public SlideState state() throws PowerPointException {
        if (isViewer()) {
            return SlideState.UNKNOWN;
        }
        return parseState(WindowsPowerPointScripts.getCurrentSlide(), WindowsPowerPointScripts.screenStatus());
    }

    /**
     * @param slide the output of getCurrentSlide.vbs.
     * @param status the output of screenStatus.vbs: 2 = normal, 3 = black.
     * @return the state.
     */
    static SlideState parseState(String slide, String status) {
        if (slide.contains("not running") || status.contains("not running")) {
            return SlideState.NOT_RUNNING;
        }
        int index;
        try {
            index = Integer.parseInt(slide.trim());
        } catch (NumberFormatException ex) {
            return new SlideState(0, 0, SlideState.Mode.NOSHOW);
        }
        SlideState.Mode mode = "3".equals(status.trim()) ? SlideState.Mode.BLACK : SlideState.Mode.RUNNING;
        return new SlideState(index, 0, mode);
    }

    private static void check(String result) throws PowerPointException {
        if (result != null && result.contains("not running")) {
            throw new PowerPointException(PowerPointException.Kind.NOT_RUNNING, result);
        }
    }

    @Override
    public void focus() throws PowerPointException {
        check(WindowsPowerPointScripts.focusPowerPoint());
    }

    @Override
    public boolean supportsPolling() {
        return false; // Every query starts cscript, too slow to poll.
    }

    @Override
    public void shutdown() {
        // Nothing kept open.
    }
}
