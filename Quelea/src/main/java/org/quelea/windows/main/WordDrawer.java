/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.LyricLine;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.Chord;
import org.quelea.utils.FXFontMetrics;
import org.quelea.utils.WrapTextResult;

/**
 *
 * @author Ben
 */
public abstract class WordDrawer extends DisplayableDrawer {

    protected Map<DisplayCanvas, Boolean> lastClearedState;
    protected static final Logger LOGGER = LoggerUtils.getLogger();

    public abstract void setTheme(ThemeDTO theme);

    public abstract void setText(String[] text, String[] translations, String[] smallText, boolean fade, double fontSize);

    public abstract ThemeDTO getTheme();

    public abstract void setCapitaliseFirst(boolean shouldCapitaliseFirst);

    public abstract void setText(TextDisplayable textDisplayable, int selectedIndex);

    protected boolean getLastClearedState() {
        Boolean val = lastClearedState.get(getCanvas());
        if (val == null) {
            return false;
        }
        return val;
    }

    protected void setLastClearedState(boolean val) {
        lastClearedState.put(getCanvas(), val);
    }
    
    private WrapTextResult getWrapTextProps(Font font, String lineToWrap, double width) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        String[] words = lineToWrap.split(" ");
        StringBuilder lineBuilder = new StringBuilder();
        List<LyricLine> lines = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String potentialStr = lineBuilder.toString() + word;
            if (metrics.computeStringWidth(potentialStr.replace("<sup>", "").replace("</sup>", "")) > width) {
                lines.add(new LyricLine(lineBuilder.toString()));
                lineBuilder = new StringBuilder(word + " ");
            }
            else {
                lineBuilder.append(word).append(" ");
            }
        }
        lines.add(new LyricLine(lineBuilder.toString()));
        //We're using the "fontsize" part of wraptextresult here as the height instead to reuse the same class, bit of a fudge...
        return new WrapTextResult(lines, metrics.getLineHeight() * lines.size());
    }
    
    protected WrapTextResult normalWrapText(Font font, String lineToWrap, double width, double height) {
        double min = 1;
        double max = font.getSize();
        
        double cur = (max-min)/2;
        
        font = new Font(font.getName(), cur);
        WrapTextResult result = getWrapTextProps(font, lineToWrap, width);
        
        int i=0;
        while(result.getFontSize()>height || result.getFontSize()<height-50) {
            i++;
            if(i>20) {
                break;
            }
            if (result.getFontSize() > height) {
                max = cur;
            } else if (result.getFontSize() < height) {
                min = cur;
            } else {
                throw new AssertionError("Shouldn't be here");
            }
            cur = ((max-min)/2)+min;
            font = new Font(font.getName(), cur);
            result = getWrapTextProps(font, lineToWrap, width);
        }
        return new WrapTextResult(result.getNewText(), cur);
    }

    /**
     * Pick a font size for the specified font that fits the given text into the
     * width and height provided.
     * <p>
     * @param font the font to use for calculations.
     * @param text the text to fit.
     * @param width the fit width.
     * @param height the fit height.
     * @return a font size for the specified font that fits the text into the
     * width and height provided.
     */
    protected double pickFontSize(Font font, List<LyricLine> text, double width, double height) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        double totalHeight = ((metrics.getLineHeight() + getLineSpacing()) * text.size());
        while (totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = new FXFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + getLineSpacing()) * text.size();
        }

        double totalWidth = longestLine(font, text);
        while (totalWidth > width) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            totalWidth = longestLine(font, text);
        }
        return font.getSize();
    }

    /**
     * Erase all the text on the getCanvas().
     */
    public void eraseText() {
        setText(null, null, null, true, -1);
    }

    protected double getLineSpacing() {
        double space = QueleaProperties.get().getAdditionalLineSpacing();
        double factor = getCanvas().getHeight() / 1000.0;
        return space * factor;
    }

    protected int longestLine(Font font, List<LyricLine> text) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        int longestLine = 0;
        for (int i = 0; i < text.size(); i++) {
            LyricLine line = text.get(i);
            if (new LineTypeChecker(line.getLine()).getLineType() == LineTypeChecker.Type.CHORDS && i < text.size() - 1) {
                List<Chord> chords = Chord.getChordsFromLine(line.getLine());
                String nextLine = text.get(i + 1).getLine();

                while (nextLine.length() < line.getLine().length()) {
                    nextLine += " ";
                }

                int maxX = 0;
                for (Chord chord : chords) {
                    int x = (int) metrics.computeStringWidth(nextLine.substring(0, chord.getIdx())) + (int) metrics.computeStringWidth(chord.getChord());
                    if (x > maxX) {
                        maxX = x;
                    }
                }
                if (maxX > longestLine) {
                    longestLine = maxX;
                }

            } else {
                int lineWidth = (int)metrics.computeStringWidth(line.getLine());
                if(lineWidth>longestLine) {
                    longestLine = lineWidth;
                }
            }
        }
        return longestLine;
    }

    protected String longestLine(Font font, ArrayList<String> text) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        double longestWidth = -1;
        String longestStr = null;
        for (String line : text) {
            double width = metrics.computeStringWidth(line);
            if (width > longestWidth) {
                longestWidth = width;
                longestStr = line;
            }
        }
        return longestStr;
    }

    /**
     * Determine if the given line contains the given string in the middle 80%
     * of the line.
     * <p/>
     * @param line the line to check.
     * @param str the string to use.
     * @return true if the line contains the delimiter, false otherwise.
     */
    protected static boolean containsNotAtEnd(String line, String str) {
        final int percentage = 80;
        int removeChars = (int) ((double) line.length() * ((double) (100 - percentage) / 100));
        return line.substring(removeChars, line.length() - removeChars).contains(str);
    }

    /**
     * Split a string with the given delimiter into two parts, using the
     * delimiter closest to the middle of the string.
     * <p/>
     * @param line the line to split.
     * @param delimiter the delimiter.
     * @return an array containing two strings split in the middle by the
     * delimiter.
     */
    protected static String[] splitMiddle(String line, char delimiter) {
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

    protected abstract void drawText(double defaultFontSize, boolean dumbWrap);

    @Override
    public void draw(Displayable displayable) {
        draw(displayable, -1);
    }

    protected void draw(Displayable displayable, double fontSize) {
        drawText(fontSize, displayable instanceof BiblePassage);
        if (getCanvas().getCanvasBackground() instanceof ImageView) {
            ImageView imgBackground = (ImageView) getCanvas().getCanvasBackground();
            imgBackground.setFitHeight(getCanvas().getHeight());
            imgBackground.setFitWidth(getCanvas().getWidth());
        } else if (getCanvas().getCanvasBackground() != null) {
            LOGGER.log(Level.WARNING, "BUG: Unrecognised image background - " + getCanvas().getCanvasBackground().getClass(), new RuntimeException("DEBUG EXCEPTION"));
        }
    }

    protected double pickSmallFontSize(Font font, String[] text, double width, double height) {
        FXFontMetrics metrics = new FXFontMetrics(font);
        ArrayList<String> al = new ArrayList<>();
        for (String te : text) {
            if (al.contains("\n")) {
                String[] te2 = te.split("\n");
                al.addAll(Arrays.asList(te2));
            } else {
                al.add(te);
            }
        }
        double totalHeight = ((metrics.getLineHeight() + getLineSpacing()) * al.size());
        while (totalHeight > height) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = new FXFontMetrics(font);
            totalHeight = (metrics.getLineHeight() + getLineSpacing()) * al.size();
        }

        String longestLine = longestLine(font, al);
        double totalWidth = metrics.computeStringWidth(longestLine);
        while (totalWidth > width) {
            font = new Font(font.getName(), font.getSize() - 0.5);
            if (font.getSize() < 1) {
                return 1;
            }
            metrics = new FXFontMetrics(font);
            totalWidth = metrics.computeStringWidth(longestLine);
        }

        return font.getSize();
    }

    /**
     * Returns a scaling factor for the canvas of this WordDrawer. This is the scale of this canvas when
     * compared to the canvas of the Projection Window. Using this scaling factor is it possible to
     * calculate the sizes of things on a preview/live when compared to the Projection Window.
     * For example the Maximum Font Size needs to be scaled for the preview/live displays.
     * <p>
     * @return The scaling factor for this canvas
     */
    protected double canvasScalingFactor() {
        double scalingFactor = 1;

        // If there is a projection window, and it has some size (avoid divide by zero errors!)
        if (QueleaApp.get().getProjectionWindow() != null && QueleaApp.get().getProjectionWindow().getWidth() != 0) {
            scalingFactor = getCanvas().getWidth() / QueleaApp.get().getProjectionWindow().getWidth();
        }

        return scalingFactor;
    }
}
