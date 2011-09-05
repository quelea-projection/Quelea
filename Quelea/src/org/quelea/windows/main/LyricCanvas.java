package org.quelea.windows.main;

import java.awt.event.ComponentEvent;
import org.quelea.notice.NoticeDrawer;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.quelea.Theme;
import org.quelea.graphics.keystone.KeystoneCorrector;
import org.quelea.notice.Notice;
import org.quelea.utils.GraphicsUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The canvas where the lyrics / images / media are drawn.
 * @author Michael
 */
public class LyricCanvas extends Canvas {

    private Theme theme;
    private String[] text;
    private String[] smallText;
    private boolean cleared;
    private boolean blacked;
    private boolean showBorder;
    private boolean capitaliseFirst;
    private boolean valid = false;
    private NoticeDrawer noticeDrawer;
    private Image offscreenImage;

    /**
     * Create a new canvas where the lyrics should be displayed.
     * @param showBorder true if the border should be shown around any text (only
     * if the options say so) false otherwise.
     */
    public LyricCanvas(boolean showBorder) {
        this.showBorder = showBorder;
        noticeDrawer = new NoticeDrawer(this);
        text = new String[]{};
        theme = Theme.DEFAULT_THEME;
        setMinimumSize(new Dimension(20, 20));
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                valid = false;
            }
        });
    }

    /**
     * Set whether the first of each line should be capitalised.
     * @param val true if the first character should be, false otherwise.
     */
    public void setCapitaliseFirst(boolean val) {
        this.capitaliseFirst = val;
        valid = false;
    }

    @Override
    public void repaint() {
        if (getWidth() > 0 && getHeight() > 0) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    paint(getGraphics());
                }
            });
        }
    }

    /**
     * Paint the background image and the lyrics onto the canvas.
     * @param g the graphics used for painting.
     */
    @Override
    public void paint(Graphics g) {
        Image noticeImage = noticeDrawer.getNoticeImage();
        if(noticeDrawer.getRedraw()) {
            valid = false;
        }
        if (!valid) {
            offscreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
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
                offscreen.drawImage(theme.getBackground().getImage(getWidth(), getHeight(), Integer.toString(getWidth())), 0, 0, null);
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
            drawSmallText(offscreen, themeFont);
            drawText(offscreen, themeFont);
        }
        if (noticeImage != null) {
            offscreenImage.getGraphics().drawImage(noticeImage, 0, getHeight() - noticeImage.getHeight(null), null);
        }
//        offscreenImage = new KeystoneCorrector(offscreenImage).getCorrectedImage();
        g.drawImage(offscreenImage, 0, 0, this);
        valid = true;
    }

    /**
     * Draw the small text to the given graphics object using the given font.
     * @param graphics the graphics object
     * @param font     the font to use for the text.
     * @return the height the small text takes on the canvas in pixels.
     */
    private int drawSmallText(Graphics graphics, Font font) {
        if (cleared || blacked || smallText == null
                || !QueleaProperties.get().checkDisplaySongInfoText()) {
            return 0;
        }
        int fontSize = getHeight() / 50;
        font = Utils.getDifferentSizeFont(font, fontSize);
        graphics.setFont(font);
        graphics.setColor(theme.getFontColor());
        FontMetrics metrics = graphics.getFontMetrics(font);

        int height = metrics.getHeight();
        int yPos = getHeight() - (height * smallText.length);

        for (String str : smallText) {
            int xPos = getWidth() - metrics.stringWidth(str);
            graphics.drawString(str, xPos, yPos);
            yPos += height;
        }
        return height * smallText.length;
    }

    /**
     * Draw the text and background to the given graphics object.
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
        Font newFont = Utils.getDifferentSizeFont(font, size);
        graphics.setFont(newFont);
        if (heightOffset > getHeight()) {
            if (font.getSize() > 8) {
                drawText(graphics, Utils.getDifferentSizeFont(font, font.getSize() - 2));
            }
        }
        else {
            int totalHeight = graphics.getFontMetrics().getHeight() * sanctifiedLines.size();
            heightOffset = (getHeight() - totalHeight) / 2;
            heightOffset += graphics.getFontMetrics().getHeight() / 2;
            for (String line : sanctifiedLines) {
                int width = graphics.getFontMetrics().stringWidth(line);
                int leftOffset = (getWidth() - width) / 2;
                GraphicsUtils graphicsUtils = new GraphicsUtils(graphics);
                if (showBorder) {
                    graphicsUtils.drawStringWithOutline(line, leftOffset, heightOffset, graphicsUtils.getInverseColor(), QueleaProperties.get().getOutlineThickness());
                }
                else {
                    graphics.drawString(line, leftOffset, heightOffset);
                }
                heightOffset += graphics.getFontMetrics().getHeight();
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
        int size = ensureLineCount(font, graphics, numLines + 1);
        while (size > 0 && graphics.getFontMetrics(font).stringWidth(line) >= getWidth()) {
            size--;
            font = Utils.getDifferentSizeFont(font, size);
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
            font = Utils.getDifferentSizeFont(font, font.getSize() - 1);
        } while (height > getHeight() && font.getSize() > 12);

        return font.getSize();
    }

    /**
     * Take the raw text and format it into a number of lines nicely, where the lines aren't more than the maximum
     * length.
     * @return processed, sanctified text that can be displayed nicely.
     */
    private List<String> sanctifyText() {
        List<String> ret = new ArrayList<>();
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
        List<String> sections = new ArrayList<>();
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
            }
//            else if(containsNotAtEnd(line, "-")) {
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
            valid = false;
            repaint();
        }
    }

    /**
     * Get the theme currently in use on the canvas.
     * @return the current theme
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Erase all the text on the canvas.
     */
    public void eraseText() {
        setText(null, null);
    }

    /**
     * Set the text to appear on the canvas. The lines will be automatically wrapped and if the text is too large to fit
     * on the screen in the current font, the size will be decreased until all the text fits.
     * @param text an array of the lines to display on the canvas, one entry in the array is one line.
     * @param smallText an array of the small lines to be displayed on the canvas. 
     */
    public void setText(String[] text, String[] smallText) {
        if (text == null) {
            text = new String[0];
        }
        if (smallText == null) {
            smallText = new String[0];
        }
        this.smallText = smallText;
        this.text = Arrays.copyOf(text, text.length);
        valid = false;
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

    public NoticeDrawer getNoticeDrawer() {
        return noticeDrawer;
    }

    public static void main(String[] args) {
        LyricCanvas canvas = new LyricCanvas(true);
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setVisible(true);

        canvas.setText(new String[]{"Line 1", "line 2", "BLAHBLAH BLAH BLAH"},
                new String[]{"Tim Hughes", "CCLI number poo", "I hate CCLI really"});

        canvas.getNoticeDrawer().addNotice(new Notice("Hello", 2));

//        try {
//            canvas.setTheme(new Theme(null, null, new Background("C:\\img.jpg", ImageIO.read(new File("C:\\img.jpg")))));
//            Thread.sleep(3000);
//            canvas.setTheme(new Theme(null, null, new Background("C:\\img2.jpg", ImageIO.read(new File("C:\\img2.jpg")))));
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }
}
