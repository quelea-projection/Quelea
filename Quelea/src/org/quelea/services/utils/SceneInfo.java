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

import javafx.geometry.Rectangle2D;

/**
 * The scene info used for storing the position and size of the main window
 * between runs of Quelea. Just a convenience class really; nothing fancy going
 * on here.
 * <p/>
 * @author Michael
 */
public class SceneInfo {

    private int x;
    private int y;
    private int w;
    private int h;
    private boolean max;

    public SceneInfo(double x, double y, double w, double h, boolean max) {
        this.x = (int) x;
        this.y = (int) y;
        this.w = (int) w;
        this.h = (int) h;
        this.max = max;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }
    
    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, w, h);
    }
    
    public boolean isMaximised() {
        return max;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + w + "," + h + "," + max;
    }
}
