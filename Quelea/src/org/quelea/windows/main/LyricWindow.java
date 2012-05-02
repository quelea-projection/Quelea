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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
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
     * Create a lyric window that overlays onto the given canvas.
     * @param overlayCanvas the canvas to overlay.
     */
    public LyricWindow(final Canvas overlayCanvas) {
        canvas = new LyricCanvas(false, false);
//        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setCursor(BLANK_CURSOR);
        add(canvas, BorderLayout.CENTER);
        
        if(!overlayCanvas.isShowing()) {
            overlayCanvas.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && canvas.isShowing()) {
                        updatePos(overlayCanvas);
                        overlayCanvas.removeHierarchyListener(this);
                    }
                }
            });
        }
        else {
            updatePos(overlayCanvas);
        }
        overlayCanvas.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                updatePos(overlayCanvas);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updatePos(overlayCanvas);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updatePos(overlayCanvas);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                updatePos(overlayCanvas);
            }
        });
        setVisible(true);
    }
    
    private void updatePos(Canvas c) {
        setArea(getOverlayArea(c));
        canvas.repaint();
        toFront();
    }
    
    private Rectangle getOverlayArea(Canvas c) {
        Point location = c.getLocationOnScreen();
        return new Rectangle((int)location.getX(), (int)location.getY(), c.getWidth(), c.getHeight());
    }

    /**
     * Create a new lyrics window positioned to fill the given rectangle.
     * @param area the area in which the window should be drawn.
     */
    public LyricWindow(Rectangle area, boolean stageView) {
//        setBackground(new Color(0,0,0,0));
        setLayout(new BorderLayout());
        setArea(area);
        setCursor(BLANK_CURSOR);
        canvas = new LyricCanvas(true, stageView);
        add(canvas, BorderLayout.CENTER);
    }

    /**
     * Set the area of the lyric window.
     * @param area the area of the window.
     */
    public final void setArea(final Rectangle area) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
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
