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
package org.quelea.windows.style;

import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.quelea.windows.main.QueleaApp;

/**
 * Keeps windows inside the screen they are shown on, so that a window never
 * gets taller or wider than a small display and its title bar and buttons
 * stay reachable.
 */
public final class ScreenFit {

    private ScreenFit() {
        throw new AssertionError();
    }

    /**
     * Fit the window to its screen every time it is shown.
     *
     * @param stage the window.
     */
    public static void keepInsideScreen(Stage stage) {
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> fit(stage));
    }

    /**
     * Make the window no larger than its screen and move it so that all of
     * it, including the title bar, is inside the screen.
     *
     * @param stage the window.
     */
    public static void fit(Stage stage) {
        Rectangle2D screenBounds = getScreenBounds(stage);
        double maxWidth = screenBounds.getWidth();
        double maxHeight = screenBounds.getHeight();

        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);
        if (stage.getMinWidth() > maxWidth) {
            stage.setMinWidth(maxWidth);
        }
        if (stage.getMinHeight() > maxHeight) {
            stage.setMinHeight(maxHeight);
        }
        if (stage.getWidth() > maxWidth) {
            stage.setWidth(maxWidth);
        }
        if (stage.getHeight() > maxHeight) {
            stage.setHeight(maxHeight);
        }

        stage.setX(Math.max(screenBounds.getMinX(), Math.min(stage.getX(), screenBounds.getMaxX() - stage.getWidth())));
        stage.setY(Math.max(screenBounds.getMinY(), Math.min(stage.getY(), screenBounds.getMaxY() - stage.getHeight())));
    }

    /**
     * Get the usable bounds (without taskbar / menu bar) of the screen a
     * window belongs on: the screen of its owner, or else of Quelea's main
     * window, or else of the window itself, or else the primary screen.
     *
     * @param window the window.
     * @return the visual bounds of that screen.
     */
    public static Rectangle2D getScreenBounds(Window window) {
        Window reference = window instanceof Stage ? ((Stage) window).getOwner() : null;
        if (!isPlaced(reference) && QueleaApp.get() != null) {
            reference = QueleaApp.get().getMainWindow();
        }
        if (!isPlaced(reference)) {
            reference = window;
        }
        if (isPlaced(reference)) {
            List<Screen> screens = Screen.getScreensForRectangle(reference.getX(), reference.getY(),
                    Math.max(1, reference.getWidth()), Math.max(1, reference.getHeight()));
            if (!screens.isEmpty()) {
                return screens.get(0).getVisualBounds();
            }
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private static boolean isPlaced(Window window) {
        return window != null && window.isShowing() && !Double.isNaN(window.getX()) && !Double.isNaN(window.getY());
    }
}
