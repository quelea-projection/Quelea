/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

/**
 * Percentage-based (0-1) margins to be applied to a display
 * <p/>
 * @author Dallon Feldner
 */
public class PercentMargins {

    private double top;
    private double right;
    private double bottom;
    private double left;

    public PercentMargins(double top, double right, double bottom, double left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    @Override
    public String toString() {
        return top + "," + right + "," + bottom + "," + left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public double getLeft() {
        return left;
    }

    public Bounds applyMargins(Bounds coords) {
        double leftMargin = left * coords.getWidth();
        double topMargin = top * coords.getHeight();

        return new BoundingBox(
                coords.getMinX() + leftMargin,
                coords.getMinY() + topMargin,
                coords.getWidth() - leftMargin - right* coords.getWidth(),
                coords.getHeight() - topMargin - bottom* coords.getHeight()
        );
    }

}

