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
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * A transparent window that displays the lyrics - overlays on a LyricCanvas
 * object, which manages the transparent canvas contained within this window.
 *
 * @author Michael
 */
public class OverlayLyricWindow extends JWindow {

    private final TopLyricCanvas canvas;

    /**
     * Create a lyric window that overlays onto the given canvas.
     *
     * @param backingCanvas the canvas to overlay.
     */
    public OverlayLyricWindow(final LyricCanvas backingCanvas, final LyricCanvasData sharedData) {
        canvas = new TopLyricCanvas(false, sharedData);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);

        if(!backingCanvas.isShowing()) {
            backingCanvas.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && canvas.isShowing()) {
                        Window window = SwingUtilities.getWindowAncestor(backingCanvas);
                        window.addWindowListener(new WindowListener() {

                            @Override
                            public void windowOpened(WindowEvent e) {
                            }

                            @Override
                            public void windowClosing(WindowEvent e) {
                            }

                            @Override
                            public void windowClosed(WindowEvent e) {
                            }

                            @Override
                            public void windowIconified(WindowEvent e) {
                                setVisible(false);
                            }

                            @Override
                            public void windowDeiconified(WindowEvent e) {
                            }

                            @Override
                            public void windowActivated(WindowEvent e) {
                                setVisible(true);
                            }

                            @Override
                            public void windowDeactivated(WindowEvent e) {
                            }
                        });

                        window.addComponentListener(new ComponentListener() {

                            @Override
                            public void componentMoved(ComponentEvent e) {
                                updatePos(backingCanvas);
                            }

                            @Override
                            public void componentResized(ComponentEvent e) {
                                updatePos(backingCanvas);
                            }

                            @Override
                            public void componentShown(ComponentEvent e) {
                                updatePos(backingCanvas);
                            }

                            @Override
                            public void componentHidden(ComponentEvent e) {
                                updatePos(backingCanvas);
                            }
                        });
                        updatePos(backingCanvas);
                        backingCanvas.removeHierarchyListener(this);
                    }
                }
            });
        }
        else {
            updatePos(backingCanvas);
        }
        backingCanvas.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                updatePos(backingCanvas);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updatePos(backingCanvas);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updatePos(backingCanvas);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                updatePos(backingCanvas);
            }
        });
    }

    /**
     * Update the position of this window to match the given LyricCanvas.
     *
     * @param c the canvas to match the position of.
     */
    private void updatePos(LyricCanvas c) {
        if(c.isShowing()) {
            setArea(getOverlayArea(c));
            canvas.repaint();
            toFront();
            setVisible(true);
        }
        else {
            setVisible(false);
        }
    }

    /**
     * Get a rectangle based on the given lyric canvas.
     *
     * @param c the canvas to generate a rectangle of the area from.
     * @return a rectangle, representing the absolute location on screen of the
     * given canvas.
     */
    private Rectangle getOverlayArea(LyricCanvas c) {
        Point location = c.getLocationOnScreen();
        return new Rectangle((int) location.getX(), (int) location.getY(), c.getWidth(), c.getHeight());
    }

    /**
     * Set the area of the lyric window.
     *
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
     *
     * @return the lyric canvas backing this window.
     */
    public TopLyricCanvas getCanvas() {
        return canvas;
    }
}
