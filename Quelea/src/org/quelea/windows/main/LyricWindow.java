package org.quelea.windows.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The full screen window used for displaying the projection.
 * @author Michael
 */
public class LyricWindow extends JWindow {

    private static final Cursor BLANK_CURSOR;
    private final LyricCanvas canvas;

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
        setArea(area);
        setCursor(BLANK_CURSOR);
        canvas = new LyricCanvas(4, 3);
        canvas.setPreferredSize(new Dimension((int) (area.getMaxX() - area.getMinX()), (int) (area.getMaxY() - area.getMinY())));
        panel.add(canvas);
        add(panel);
    }

    /**
     * Set the area of the lyric window.
     * @param area the area of the window.
     */
    public final void setArea(Rectangle area) {
        setSize((int) (area.getMaxX() - area.getMinX()), (int) (area.getMaxY() - area.getMinY()));
        setLocation((int) area.getMinX(), (int) area.getMinY());
    }

    /**
     * Get the canvas object that underlines this lyric window.
     * @return the lyric canvas backing this window.
     */
    public LyricCanvas getCanvas() {
        return canvas;
    }

}
