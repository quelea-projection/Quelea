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
package org.quelea.data.logo;

import javafx.geometry.Rectangle2D;

/**
 * Works out where on the screen the logo goes.
 *
 * @author Arvid
 */
public final class LogoLayout {

    private LogoLayout() {
    }

    /**
     * Compute where the logo should be drawn.
     * <p>
     * The logo is scaled (keeping its aspect ratio) to fit in a box of
     * {@code size} times the screen's width and height, anchored at the
     * chosen position and then shifted by the offsets. It's always kept fully
     * on screen.
     *
     * @param areaWidth     width of the screen (or canvas).
     * @param areaHeight    height of the screen (or canvas).
     * @param contentWidth  natural width of the logo (only its aspect ratio
     *                      matters).
     * @param contentHeight natural height of the logo.
     * @param settings      the logo settings.
     * @return the bounds of the logo, relative to the top left of the area.
     * Empty (zero size) if there is nothing to show.
     */
    public static Rectangle2D computeBounds(double areaWidth, double areaHeight,
                                            double contentWidth, double contentHeight,
                                            LogoSettings settings) {
        if (!(areaWidth > 0) || !(areaHeight > 0) || !(contentWidth > 0) || !(contentHeight > 0)) {
            return new Rectangle2D(0, 0, 0, 0);
        }
        if (settings == null) {
            settings = LogoSettings.DEFAULT;
        }
        double boxWidth = areaWidth * settings.size();
        double boxHeight = areaHeight * settings.size();
        double scale = Math.min(boxWidth / contentWidth, boxHeight / contentHeight);
        double width = Math.min(areaWidth, contentWidth * scale);
        double height = Math.min(areaHeight, contentHeight * scale);

        LogoPosition position = settings.position();
        double x = (areaWidth - width) * position.getHorizontal() + settings.offsetX() * areaWidth;
        double y = (areaHeight - height) * position.getVertical() + settings.offsetY() * areaHeight;
        x = Math.max(0, Math.min(areaWidth - width, x));
        y = Math.max(0, Math.min(areaHeight - height, y));
        return new Rectangle2D(x, y, width, height);
    }
}
