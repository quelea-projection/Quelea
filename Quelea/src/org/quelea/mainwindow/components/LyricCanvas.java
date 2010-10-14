package org.quelea.mainwindow.components;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The canvas where the lyrics / images / media are drawn.
 * @author Michael
 */
public class LyricCanvas extends Canvas {

    private BufferedImage backgroundImage;
    /** The default colour, used if none is given. */
    public static final Color DEFAULT_COLOUR = Color.BLACK;
    private Color backgroundColor;
    private String[] text;
    private Font font;
    private int aspectWidth;
    private int aspectHeight;
    private boolean cleared;
    private boolean blacked;

    /**
     * Create a new canvas where the lyrics should be displayed.
     */
    public LyricCanvas(int aspectWidth, int aspectHeight) {
        this.aspectWidth = aspectWidth;
        this.aspectHeight = aspectHeight;
        setMinimumSize(new Dimension(10, 10));
        text = new String[]{};
        font = new Font("Comic Sans MS", Font.BOLD, 72);
        backgroundColor = DEFAULT_COLOUR;
    }

    /**
     * Paint the background image and the lyrics onto the canvas.
     * @param g the graphics used for painting.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        fixAspectRatio();
        if(backgroundImage != null) {
            AffineTransform scaler = AffineTransform.getScaleInstance((double) getHeight() / backgroundImage.getHeight(null), (double) getWidth() / backgroundImage.getHeight(null));
            ((Graphics2D) g).drawRenderedImage(backgroundImage, scaler);
        }
        else {
            if(blacked) {
                setBackground(Color.BLACK);
            }
            else {
                setBackground(backgroundColor);
            }
        }
        g.setFont(font);
        g.setColor(Color.WHITE);
        drawText(g, font);
    }

    /**
     * Draw the text and background to the given graphics object.
     * @param graphics the graphics object
     * @param font the font to use for the text.
     */
    private void drawText(Graphics graphics, Font font) {
        if(cleared || blacked) {
            return;
        }
        graphics.setFont(font);
        ArrayList<String> lines = new ArrayList<String>();
        FontMetrics metrics = graphics.getFontMetrics(font);
        int heightOffset = 0;
        for(String line : text) {
            String[] words = line.split(" ");
            StringBuilder builder = new StringBuilder();
            for(String word : words) {
                int futureWidth = metrics.stringWidth(builder.toString() + " " + word);
                if(futureWidth > getWidth()) {
                    lines.add(builder.toString());
                    heightOffset += metrics.getHeight();
                    builder = new StringBuilder();
                }
                builder.append(word).append(" ");
            }
            heightOffset += metrics.getHeight();
            lines.add(builder.toString());
        }
        if(heightOffset > getHeight()) {
            drawText(graphics, getDifferentSizeFont(font, font.getSize()-10));
        }
        else {
            heightOffset = 0;
            for(String line : lines) {
                int width = metrics.stringWidth(line);
                int leftOffset = (getWidth() - width) / 2;
                heightOffset += metrics.getHeight();
                graphics.drawString(line, leftOffset, heightOffset);
            }
        }
    }

    /**
     * Toggle the clearing of this canvas - still leave the background image
     * in place but remove all the text.
     */
    public void toggleClear() {
        cleared = !cleared;
        repaint();
    }

    /**
     * Determine whether this canvas is cleared.
     * @return true if the canvas is cleared, false otherwise.
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Toggle the blacking of this canvas - remove the text and background
     * image (if any) just displaying a black screen.
     */
    public void toggleBlack() {
        blacked = !blacked;
        repaint();
    }

    /**
     * Determine whether this canvas is blacked.
     * @return true if the canvas is blacked, false otherwise.
     */
    public boolean isBlacked() {
        return blacked;
    }

    /**
     * Set the background image of this canvas. The image will be scaled up or
     * down to fit the canvas size.
     * @param image the background image to place on the canvas.
     */
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
    }

    /**
     * Set the background colour of the canvas. This will replace any image
     * currently being used for the background colour.
     * @param colour the colour to use as the background colour.
     */
    public void setBackgroundColour(Color colour) {
        backgroundImage = null;
        backgroundColor = colour;
        repaint();
    }

    /**
     * Set the text to appear on the canvas. The lines will be automatically
     * wrapped and if the text is too large to fit on the screen in the current
     * font, the size will be decreased until all the text fits.
     * @param text an array of the lines to display on the canvas, one entry
     * in the array is one line.
     */
    public void setText(String[] text) {
        this.text = text;
        repaint();
    }

    /**
     * Get the text currently set to appear on the canvas. The text may or may
     * not be shown depending on whether the canvas is blacked or cleared.
     * @return the current text.
     */
    public String[] getText() {
        return text;
    }

    /**
     * Change the font used for the text on the canvas.
     * @param font the new font.
     */
    public void changeFont(Font font) {
        this.font = font;
        repaint();
    }

    /**
     * Fix the aspect ratio of this lyric canvas so that it's always reduced
     * in size to the aspect ratio specified.
     */
    public void fixAspectRatio() {
        Dimension currentSize = getSize();
        double width = currentSize.getWidth();
        double height = currentSize.getHeight();
        double estWidth = (height / aspectHeight) * aspectWidth;
        double estHeight = (width / aspectWidth) * aspectHeight;
        if(estWidth < width) {
            super.setSize(new Dimension((int) estWidth, (int) height));
            font = getDifferentSizeFont(font, (float)estWidth/16);
        }
        else {
            super.setSize(new Dimension((int) width, (int) estHeight));
            font = getDifferentSizeFont(font, (float)width/16);
        }
    }

    /**
     * Get a font identical to the one given apart from in size.
     * @param size the size of the new font.
     * @return the resized font.
     */
    private Font getDifferentSizeFont(Font bigFont, float size) {
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        for(Entry<TextAttribute, ?> entry : bigFont.getAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        if(attributes.get(TextAttribute.SIZE) != null) {
            attributes.put(TextAttribute.SIZE, size);
        }
        return new Font(attributes);
    }
}
