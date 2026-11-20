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

/**
 * How the logo is placed on the screen.
 *
 * @param size     the largest part of the screen the logo may cover, as a
 *                 fraction (1 = fit the whole screen, 0.25 = fit in a quarter
 *                 of the screen's width and height). The logo always keeps its
 *                 aspect ratio.
 * @param position where the logo is anchored.
 * @param offsetX  extra horizontal shift as a fraction of the screen width
 *                 (positive is to the right).
 * @param offsetY  extra vertical shift as a fraction of the screen height
 *                 (positive is down).
 * @author Arvid
 */
public record LogoSettings(double size, LogoPosition position, double offsetX, double offsetY) {

    public static final double MIN_SIZE = 0.05;
    public static final double MAX_SIZE = 1;
    public static final double MIN_OFFSET = -0.5;
    public static final double MAX_OFFSET = 0.5;

    /**
     * Fit the whole screen, centred: how the logo was always shown before
     * these settings existed.
     */
    public static final LogoSettings DEFAULT = new LogoSettings(MAX_SIZE, LogoPosition.CENTER, 0, 0);

    public LogoSettings {
        size = clamp(size, MIN_SIZE, MAX_SIZE, MAX_SIZE);
        position = position == null ? LogoPosition.CENTER : position;
        offsetX = clamp(offsetX, MIN_OFFSET, MAX_OFFSET, 0);
        offsetY = clamp(offsetY, MIN_OFFSET, MAX_OFFSET, 0);
    }

    public LogoSettings withSize(double size) {
        return new LogoSettings(size, position, offsetX, offsetY);
    }

    public LogoSettings withPosition(LogoPosition position) {
        return new LogoSettings(size, position, offsetX, offsetY);
    }

    public LogoSettings withOffsetX(double offsetX) {
        return new LogoSettings(size, position, offsetX, offsetY);
    }

    public LogoSettings withOffsetY(double offsetY) {
        return new LogoSettings(size, position, offsetX, offsetY);
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }
}
