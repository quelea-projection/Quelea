/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * The full screen window used for displaying the projection.
 * @author Michael
 */
public class LyricWindow extends JWindow {

    private static final Cursor BLANK_CURSOR;
    private final LyricCanvas canvas;

    /**
     * Initialise cursor hiding.
     */
    static {
        BufferedImage blankImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(blankImg, new Point(0, 0), "blank cursor");
    }

    /**
     * Create a new lyrics window positioned to fill the given rectangle.
     * @param area the area in which the window should be drawn.
     */
    public LyricWindow(Rectangle area) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        setArea(area);
        setCursor(BLANK_CURSOR);
        canvas = new LyricCanvas(true, false);
        canvas.setPreferredSize(new Dimension((int) (area.getMaxX() - area.getMinX()), (int) (area.getMaxY() - area.getMinY())));
        panel.add(canvas);
        add(panel);
    }

    /**
     * Set the area of the lyric window.
     * @param area the area of the window.
     */
    public final void setArea(final Rectangle area) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (canvas != null) {
                    canvas.setPreferredSize(new Dimension((int) (area.getMaxX() - area.getMinX()), (int) (area.getMaxY() - area.getMinY())));
                }
                setSize((int) (area.getMaxX() - area.getMinX()), (int) (area.getMaxY() - area.getMinY()));
                setLocation((int) area.getMinX(), (int) area.getMinY());
            }
        });
    }

    /**
     * Get the canvas object that underlines this lyric window.
     * @return the lyric canvas backing this window.
     */
    public LyricCanvas getCanvas() {
        return canvas;
    }
}
