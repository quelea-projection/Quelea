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
package org.quelea.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.StatusPanel;

/**
 * General utility class containing a bunch of static methods.
 *
 * @author Michael
 */
public final class Utils {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Don't instantiate me. I bite.
     */
    private Utils() {
        throw new AssertionError();
    }

    /**
     * Sleep ignoring the exception.
     * @param millis milliseconds to sleep.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            //Nothing
        }
    }

    /**
     * Determine if we're running in a 64 bit JVM.
     * @return true if it's a 64 bit JVM, false if it's 32 bit (or something
     * else.)
     */
    public static boolean is64Bit() {
        return System.getProperty("os.arch").contains("64"); //Rudimentary...
    }

    /**
     * Determine if we're running on a mac.
     * @return true if we're running on a mac, false otherwise.
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * Determine if we're running on Linux.
     * @return true if we're running on Linux, false otherwise.
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * Update a song in the background.
     * @param song the song to update.
     */
    public static void updateSongInBackground(final Song song, final boolean showError) {
        final StatusPanel statusPanel = Application.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("updating.db"));
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                boolean result = SongDatabase.get().updateSong(song);
                if (!result && showError) {
                    JOptionPane.showMessageDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("error.udpating.song.text"), LabelGrabber.INSTANCE.getLabel("error.text"), JOptionPane.ERROR_MESSAGE, null);
                }
                return null;
            }

            @Override
            protected void done() {
                statusPanel.done();
            }
        };
        worker.execute();
    }

    /**
     * Get the JFileChooser used for opening and saving schedules.
     * @return the JFileChooser.
     */
    public static JFileChooser getScheduleFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(FileFilters.SCHEDULE);
        return chooser;
    }

    /**
     * Wrap a runnable as one having a low priority.
     * @param task the runnable to wrap.
     * @return a runnable having a low priority.
     */
    public static Runnable wrapAsLowPriority(final Runnable task) {
        return new Runnable() {

            @SuppressWarnings("CallToThreadYield")
            public void run() {
                Thread t = Thread.currentThread();
                int oldPriority = t.getPriority();
                t.setPriority(Thread.MIN_PRIORITY);
                Thread.yield();
                task.run();
                t.setPriority(oldPriority);
            }
        };
    }

    /**
     * Determine whether translucency is supported.
     * @return true if it's supported, false otherwise.
     */
    public static boolean translucencySupported() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice device : gs) {
            if (device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a font identical to the one given apart from in size.
     * @param font the original font.
     * @param size the size of the new font.
     * @return the resized font.
     */
    public static Font getDifferentSizeFont(Font font, float size) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        for (Entry<TextAttribute, ?> entry : font.getAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        if (attributes.get(TextAttribute.SIZE) != null) {
            attributes.put(TextAttribute.SIZE, size);
        }
        return new Font(attributes);
    }

    /**
     * Calculates the largest size of the given font for which the given string 
     * will fit into the given size.
     * @param g the graphics to use in the current context.
     * @param font the original font to base the returned font on.
     * @param string the string to fit.
     * @param width the maximum width available.
     * @param height the maximum height available.
     * @return the maximum font size that fits into the given area.
     */
    public static int getMaxFittingFontSize(Graphics g, Font font, String string, int width, int height) {
        int minSize = 0;
        int maxSize = 288;
        int curSize = font.getSize();

        while (maxSize - minSize > 2) {
            FontMetrics fm = g.getFontMetrics(new Font(font.getName(), font.getStyle(), curSize));
            int fontWidth = fm.stringWidth(string);
            int fontHeight = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();

            if ((fontWidth > width) || (fontHeight > height)) {
                maxSize = curSize;
                curSize = (maxSize + minSize) / 2;
            } else {
                minSize = curSize;
                curSize = (minSize + maxSize) / 2;
            }
        }

        return curSize;
    }

    /**
     * Get the difference between two colours, from 0 to 100 where 100 is most
     * difference and 0 is least different.
     * @param a the first colour
     * @param b the second colour
     * @return the difference between the colours.
     */
    public static int getColorDifference(Color a, Color b) {
        double ret = Math.abs(a.getRed() - b.getRed()) + Math.abs(a.getGreen() - b.getGreen()) + Math.abs(a.getBlue() - b.getBlue());
        return (int) ((ret / (255 * 3)) * 100);
    }

    /**
     * Remove all HTML tags from a string.
     * @param str the string to remove the tags from.
     * @return the string with the tags removed.
     */
    public static String removeTags(String str) {
        return str.replaceAll("\\<.*?>", "");
    }

    /**
     * Determine whether the given frame is completely on the given screen.
     * @param frame      the frame to check.
     * @param monitorNum the monitor number to check.
     * @return true if the frame is totally on the screen, false otherwise.
     */
    public static boolean isFrameOnScreen(JFrame frame, int monitorNum) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        return gds[monitorNum].getDefaultConfiguration().getBounds().contains(frame.getBounds());
    }

    /**
     * Centre the given frame on the given monitor.
     * @param frame      the frame to centre.
     * @param monitorNum the monitor number to centre the frame on.
     */
    public static void centreOnMonitor(final JFrame frame, int monitorNum) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        Rectangle bounds = gds[monitorNum].getDefaultConfiguration().getBounds();
        final int centreX = (int) (((int) (bounds.getMaxX() - bounds.getMinX()) / 2) + bounds.getMinX());
        final int centreY = (int) (((int) (bounds.getMaxY() - bounds.getMinY()) / 2) + bounds.getMinY());
        Runnable locationSetter = new Runnable() {

            @Override
            public void run() {
                frame.setLocation(centreX - frame.getWidth() / 2, centreY - frame.getHeight() / 2);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            locationSetter.run();
        } else {
            SwingUtilities.invokeLater(locationSetter);
        }
    }

    /**
     * Remove duplicates in a list whilst maintaining the order.
     * @param <T>  the type of the list.
     * @param list the list to remove duplicates.
     */
    public static <T> void removeDuplicateWithOrder(List<T> list) {
        Set<T> set = new HashSet<>();
        List<T> newList = new ArrayList<>();
        for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
            T element = iter.next();
            if (set.add(element)) {
                newList.add(element);
            }
        }
        list.clear();
        list.addAll(newList);
    }

    /**
     * Copy a file from one place to another.
     * @param sourceFile the source file
     * @param destFile the destination file
     * @throws IOException if something goes wrong.
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equals(".svn")) {
                return;
            }
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            for (File file : sourceFile.listFiles()) {
                copyFile(file, new File(destFile, file.getName()));
            }
            return;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Capitalise the first letter of a string.
     * @param line the input string.
     * @return the the string with the first letter capitalised.
     */
    public static String capitaliseFirst(String line) {
        if (line.isEmpty()) {
            return line;
        }
        StringBuilder ret = new StringBuilder(line);
        ret.setCharAt(0, Character.toUpperCase(line.charAt(0)));
        return ret.toString();
    }

    /**
     * Get an abbreviation from a name based on the first letter of each word of the name.
     * @param name the name to use for the abbreviation.
     * @return the abbreviation.
     */
    public static String getAbbreviation(String name) {
        StringBuilder ret = new StringBuilder();
        String[] parts = name.split(" ");
        for (String str : parts) {
            if (!str.isEmpty()) {
                ret.append(Character.toUpperCase(str.charAt(0)));
            }
        }
        return ret.toString();
    }

    /**
     * Escape the XML special characters.
     * @param s the string to escape.
     * @return the escaped string.
     */
    public static String escapeXML(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    /**
     * Get the textual content from a file as a string, returning the given error string if a problem occurs retrieving
     * the content.
     * @param fileName  the filename to get the text from.
     * @param errorText the error string to return if things go wrong.
     * @return hopefully the text content of the file, or the errorText string if we can't get the text content for some
     *         reason.
     */
    public static synchronized String getTextFromFile(String fileName, String errorText) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get the contents of " + fileName, ex);
            return errorText;
        }
    }

    /**
     * Get an image icon from the location of a specified file.
     * @param location the location of the image to use.
     * @return the icon formed from the image, or null if an IOException occured.
     */
    public static ImageIcon getImageIcon(String location) {
        return getImageIcon(location, -1, -1);
    }

    /**
     * Get an image icon from the location of a specified file.
     * @param location the location of the image to use.
     * @param width the width of the given image icon.
     * @param height the height of the given image icon.
     * @return the icon formed from the image, or null if an IOException occured.
     */
    public static ImageIcon getImageIcon(String location, int width, int height) {
        Image image = getImage(location, width, height);
        if (image == null) {
            return null;
        }
        return new ImageIcon(image);
    }

    /**
     * Get an image from the location of a specified file.
     * @param location the location of the image to use.
     * @return the icon formed from the image, or null if an IOException occured.
     */
    public static BufferedImage getImage(String location) {
        return getImage(location, -1, -1);
    }

    /**
     * Get an image from the location of a specified file.
     * @param location the location of the image to use.
     * @param width the width of the returned image.
     * @param height the height of the returned image. 
     * @return the icon formed from the image, or null if an IOException occured.
     */
    public static BufferedImage getImage(String location, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(new File(location));
            if (width > 0 && height > 0) {
                return resizeImage(image, width, height);
            } else {
                return image;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get image: " + location, ex);
            return null;
        }
    }

    /**
     * Resize a given image to the given width and height.
     * @param image the image to resize.
     * @param width the width of the new image.
     * @param height the height of the new image.
     * @return the resized image.
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        if (width > 0 && height > 0 && (image.getWidth() != width || image.getHeight() != height)) {
            BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bdest.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, width, height, null);
            return bdest;
        } else {
            return image;
        }
    }

    /**
     * Convert the given icon to an image.
     * @param icon the icon to convert.
     * @return the converted icon.
     */
    public static BufferedImage iconToImage(Icon icon) {
        BufferedImage ret = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(new JLabel(), ret.createGraphics(), 0, 0);
        return ret;
    }

    /**
     * Determine whether a file is an image file.
     * @param file the file to check.
     * @return true if the file is an image, false otherwise.
     */
    public static boolean fileIsImage(File file) {
        if (file.isDirectory() && !file.isHidden()) {
            return true;
        } else {
            String suffix = file.getName().split("\\.")[file.getName().split("\\.").length - 1].toLowerCase().trim();
            return (suffix.equals("png")
                    || suffix.equals("bmp")
                    || suffix.equals("tif")
                    || suffix.equals("jpg")
                    || suffix.equals("jpeg")
                    || suffix.equals("gif"));
        }
    }

    /**
     * Get the names of all the fonts available on the current system.
     * @return the names of all the fonts available.
     */
    public static String[] getAllFonts() {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Set<String> names = new HashSet<>();
        for (int i = 0; i < fonts.length; i++) {
            names.add(fonts[i].getFamily());
        }
        List<String> namesList = new ArrayList<>(names.size());
        for (String name : names) {
            namesList.add(name);
        }
        Collections.sort(namesList);
        return namesList.toArray(new String[namesList.size()]);
    }

    /**
     * Get an image filled with the specified colour.
     * @param color  the colour of the image.
     * @param width  the width of the image.
     * @param height the height of the image.
     * @return the image.
     */
    public static BufferedImage getImageFromColour(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        return image;
    }

    /**
     * Parse a colour string to a colour.
     * @param colour the colour string.
     * @return the colour.
     */
    public static Color parseColour(String colour) {
        colour = colour.substring(colour.indexOf('[') + 1, colour.indexOf(']'));
        String[] parts = colour.split(",");
        int red = Integer.parseInt(parts[0].split("=")[1]);
        int green = Integer.parseInt(parts[1].split("=")[1]);
        int blue = Integer.parseInt(parts[2].split("=")[1]);
        return new Color(red, green, blue);
    }
}
