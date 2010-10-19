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

/**
 * The full screen window used for displaying the projection.
 * @author Michael
 */
public class LyricWindow extends JWindow {

    private static Cursor BLANK_CURSOR;
    private LyricCanvas canvas;

    /**
     * Initialise cursor hiding
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
        setSize((int)(area.getMaxX()-area.getMinX()), (int)(area.getMaxY()-area.getMinY()));
        setLocation((int) area.getMinX(), (int) area.getMinY());
        setCursor(BLANK_CURSOR);
        canvas = new LyricCanvas(4,3);
        canvas.setPreferredSize(new Dimension((int)(area.getMaxX()-area.getMinX()), (int)(area.getMaxY()-area.getMinY())));
        panel.add(canvas);
        add(panel);
    }

    /**
     * Get the canvas object that underlines this lyric window.
     * @return the lyric canvas backing this window.
     */
    public LyricCanvas getCanvas() {
        return canvas;
    }

}
