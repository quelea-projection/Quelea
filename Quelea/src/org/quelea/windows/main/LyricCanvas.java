package org.quelea.windows.main;

import com.sun.jna.Memory;
import org.quelea.Theme;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.quelea.Background;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

/**
 * The canvas where the lyrics / images / media are drawn.
 * @author Michael
 */
public class LyricCanvas extends Canvas implements RenderCallback {

    private Theme theme;
    private String[] text;
    private Font font;
    private final int aspectWidth;
    private final int aspectHeight;
    private boolean cleared;
    private boolean blacked;
    private boolean capitaliseFirst;
    private int[] rgbBuffer;
    private BufferedImage videoImage;
    private int videoWidth = 800;
    private int videoHeight = 600;

    /**
     * Create a new canvas where the lyrics should be displayed.
     * @param aspectWidth  the aspect ratio width.
     * @param aspectHeight the aspect ratio height.
     */
    public LyricCanvas(int aspectWidth, int aspectHeight) {
        this.aspectWidth = aspectWidth;
        this.aspectHeight = aspectHeight;
        text = new String[]{};
        font = new Font("sans-serif", Font.BOLD, 300);
        theme = Theme.DEFAULT_THEME;
        rgbBuffer = new int[800 * 600];
        videoImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(videoWidth, videoHeight);
        videoImage.setAccelerationPriority(1.0f);
        setMinimumSize(new Dimension(20, 20));
    }

    /**
     * Set whether the first of each line should be capitalised.
     * @param val true if the first character should be, false otherwise.
     */
    public void setCapitaliseFirst(boolean val) {
        this.capitaliseFirst = val;
    }

    @Override
    public void display(Memory nativeBuffer) {
        nativeBuffer.read(0, rgbBuffer, 0, rgbBuffer.length);
        videoImage.setRGB(0, 0, videoWidth, videoHeight, rgbBuffer, 0, videoWidth);
        repaint();
    }

    /**
     * Paint the background image and the lyrics onto the canvas.
     * @param g the graphics used for painting.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Image offscreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics offscreen = offscreenImage.getGraphics();
        offscreen.setColor(getForeground());
        super.paint(offscreen);
        if (blacked || theme == null) {
            Color temp = offscreen.getColor();
            offscreen.setColor(Color.BLACK);
            offscreen.fillRect(0, 0, getWidth(), getHeight());
            offscreen.setColor(temp);
        }
        else {
            offscreen.drawImage(theme.getBackground().getImage(getWidth(), getHeight()), 0, 0, null);
        }
        Color fontColour = theme.getFontColor();
        if (fontColour == null) {
            fontColour = Theme.DEFAULT_FONT_COLOR;
        }
        offscreen.setColor(fontColour);
        Font themeFont = theme.getFont();
        if (themeFont == null) {
            themeFont = Theme.DEFAULT_FONT;
        }
        drawText(offscreen, themeFont);
        g.drawImage(offscreenImage, 0, 0, this);
    }

    /**
     * Draw the text and background to the given graphics object.
     * @param graphics the graphics object
     * @param font     the font to use for the text.
     */
    private void drawText(Graphics graphics, Font font) {
        if (cleared || blacked) {
            return;
        }
        graphics.setFont(font);
        graphics.setColor(theme.getFontColor());
        FontMetrics metrics = graphics.getFontMetrics(font);
        int heightOffset = 0;
        int maxWidth = 0;
        String maxLine = "";
        List<String> sanctifiedLines = sanctifyText();
        for (String line : sanctifiedLines) {
            int width = metrics.stringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
                maxLine = line;
            }
        }
        int size = getFontSize(font, graphics, maxLine, sanctifiedLines.size());
        Font newFont = getDifferentSizeFont(font, size);
        graphics.setFont(newFont);
        if (heightOffset > getHeight()) {
            if (font.getSize() > 8) {
                drawText(graphics, getDifferentSizeFont(font, font.getSize() - 2));
            }
        }
        else {
            heightOffset = 0;
            for (String line : sanctifiedLines) {
                int width = graphics.getFontMetrics().stringWidth(line);
                int leftOffset = (getWidth() - width) / 2;
                heightOffset += graphics.getFontMetrics().getHeight();
                graphics.drawString(line, leftOffset, heightOffset);
            }
        }
    }

    /**
     * Based on the longest line, return the largest font size that can be used to fit this line.
     * @param font     the initial starting font to use.
     * @param graphics the graphics of this canvas.
     * @param line     the longest line.
     * @return the largest font size that can be used.
     */
    private int getFontSize(Font font, Graphics graphics, String line, int numLines) {
        int size = ensureLineCount(font, graphics, numLines);
        while (size > 0 && graphics.getFontMetrics(font).stringWidth(line) >= getWidth()) {
            size--;
            font = getDifferentSizeFont(font, size);
        }
        return size;
    }

    /**
     * Return a font size that ensures we have at least the required number of lines available per slide.
     * @param font     the initial font to use.
     * @param graphics the graphics of the canvas.
     * @return the largest font size that can be used.
     */
    private int ensureLineCount(Font font, Graphics graphics, int numLines) {
        int height;
        int lineCount = QueleaProperties.get().getMinLines();
        if (numLines > lineCount) {
            lineCount = numLines;
        }
        do {
            height = graphics.getFontMetrics(font).getHeight() * lineCount;
            font = getDifferentSizeFont(font, font.getSize() - 1);
        } while (height > getHeight() && font.getSize() > 12);

        return font.getSize();
    }

    /**
     * Take the raw text and format it into a number of lines nicely, where the lines aren't more than the maximum
     * length.
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<String> sanctifyText() {
        List<String> ret = new ArrayList<String>();
        int maxLength = QueleaProperties.get().getMaxChars();
        for (String line : text) {
            ret.addAll(splitLine(line, maxLength));
        }
        return ret;
    }

    /**
     * Given a line of any length, sensibly split it up into several lines.
     * @param line the line to split.
     * @return the split line (or the unaltered line if it is less than or equal to the allowed length.
     */
    private List<String> splitLine(String line, int maxLength) {
        List<String> sections = new ArrayList<String>();
        if (line.length() > maxLength) {
            if (containsNotAtEnd(line, ";")) {
                for (String s : splitMiddle(line, ';')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            }
            else if (containsNotAtEnd(line, ",")) {
                for (String s : splitMiddle(line, ',')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            }
            else if (containsNotAtEnd(line, " ")) {
                for (String s : splitMiddle(line, ' ')) {
                    sections.addAll(splitLine(s, maxLength));
                }
            } //            else if(containsNotAtEnd(line, "-")) {
            //                for(String s : splitMiddle(line, '-')) {
            //                    sections.addAll(splitLine(s, maxLength));
            //                }
            //            }
            else {
                sections.addAll(splitLine(new StringBuilder(line).insert(line.length() / 2, "-").toString(), maxLength));
            }
        }
        else {
            line = line.trim();
            if (capitaliseFirst && QueleaProperties.get().checkCapitalFirst()) {
                line = Utils.capitaliseFirst(line);
            }
            sections.add(line);
        }
        return sections;
    }

    /**
     * Determine if the given line contains the given string in the middle 80% of the line.
     * @param line the line to check.
     * @param str  the string to use.
     * @return true if the line contains the delimiter, false otherwise.
     */
    private static boolean containsNotAtEnd(String line, String str) {
        final int percentage = 80;
        int removeChars = (int) ((double) line.length() * ((double) (100 - percentage) / 100));
        return line.substring(removeChars, line.length() - removeChars).contains(str);
    }

    /**
     * Split a string with the given delimiter into two parts, using the delimiter closest to the middle of the string.
     * @param line      the line to split.
     * @param delimiter the delimiter.
     * @return an array containing two strings split in the middle by the delimiter.
     */
    private static String[] splitMiddle(String line, char delimiter) {
        final int middle = (int) (((double) line.length() / 2) + 0.5);
        int nearestIndex = -1;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == delimiter) {
                int curDistance = Math.abs(nearestIndex - middle);
                int newDistance = Math.abs(i - middle);
                if (newDistance < curDistance || nearestIndex < 0) {
                    nearestIndex = i;
                }
            }
        }
        return new String[]{line.substring(0, nearestIndex + 1), line.substring(nearestIndex + 1, line.length())};
    }

    /**
     * Toggle the clearing of this canvas - still leave the background image in place but remove all the text.
     */
    public void toggleClear() {
        cleared ^= true; //invert
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
     * Toggle the blacking of this canvas - remove the text and background image (if any) just displaying a black
     * screen.
     */
    public void toggleBlack() {
        blacked ^= true; //invert
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
     * Set the theme of this canvas.
     * @param theme the theme to place on the canvas.
     */
    public void setTheme(Theme theme) {
        Theme t1 = theme == null ? Theme.DEFAULT_THEME : theme;
        Theme t2 = this.theme == null ? Theme.DEFAULT_THEME : this.theme;
        if (!t2.equals(t1)) {
            this.theme = t1;
            repaint();
        }
    }

    /**
     * Prevent small changes to the canvas' size. This is a workaround to a bug where the canvas was sometimes resized
     * constantly by very small intervals (a few pixels) causing annoying flickering.
     * @param width  the width of the component.
     * @param height the height of the component.
     */
//    @Override
//    public void setSize(int width, int height) {
//        final int MAX_OFFSET = 5; //changes less than this will be discarded.
//        int widthDiff = Math.abs(width - getWidth());
//        int heightDiff = Math.abs(height - getHeight());
//        if(widthDiff > MAX_OFFSET || heightDiff > MAX_OFFSET) {
//            super.setSize(width, height);
//        }
//    }
    /**
     * Get the theme currently in use on the canvas.
     * @return the current theme
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Set the text to appear on the canvas. The lines will be automatically wrapped and if the text is too large to fit
     * on the screen in the current font, the size will be decreased until all the text fits.
     * @param text an array of the lines to display on the canvas, one entry in the array is one line.
     */
    public void setText(String[] text) {
        if (text == null) {
            text = new String[0];
        }
        this.text = Arrays.copyOf(text, text.length);
        repaint();
    }

    /**
     * Get the text currently set to appear on the canvas. The text may or may not be shown depending on whether the
     * canvas is blacked or cleared.
     * @return the current text.
     */
    public String[] getText() {
        return Arrays.copyOf(text, text.length);
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
     * Fix the aspect ratio of this lyric canvas so that it's always reduced in size to the aspect ratio specified.
     */
//    public void fixAspectRatio() {
//        Dimension currentSize = getSize();
//        double width = currentSize.getWidth();
//        double height = currentSize.getHeight();
//        double estWidth = (height / aspectHeight) * aspectWidth;
//        double estHeight = (width / aspectWidth) * aspectHeight;
//        if(estWidth < width) {
//            super.setSize(new Dimension((int) estWidth, (int) height));
//            font = getDifferentSizeFont(font, (float) estWidth / 16);
//        }
//        else {
//            super.setSize(new Dimension((int) width, (int) estHeight));
//            font = getDifferentSizeFont(font, (float) width / 16);
//        }
//    }
    /**
     * Get a font identical to the one given apart from in size.
     * @param size the size of the new font.
     * @return the resized font.
     */
    private Font getDifferentSizeFont(Font bigFont, float size) {
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        for (Entry<TextAttribute, ?> entry : bigFont.getAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        if (attributes.get(TextAttribute.SIZE) != null) {
            attributes.put(TextAttribute.SIZE, size);
        }
        return new Font(attributes);
    }

    public static void main(String[] args) {
        LyricCanvas canvas = new LyricCanvas(4, 3);
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setVisible(true);

        try {
            canvas.setTheme(new Theme(null, null, new Background("C:\\img.jpg", ImageIO.read(new File("C:\\img.jpg")))));
            Thread.sleep(3000);
            canvas.setTheme(new Theme(null, null, new Background("C:\\img2.jpg", ImageIO.read(new File("C:\\img2.jpg")))));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
