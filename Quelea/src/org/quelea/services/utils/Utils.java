/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.data.ThemeDTO;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * General utility class containing a bunch of static methods.
 * <p/>
 * @author Michael
 */
public final class Utils {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static final String TOOLBAR_BUTTON_STYLE = "-fx-background-insets: 0;-fx-background-color: rgba(0, 0, 0, 0);-fx-padding:3,6,3,6;";
    public static final String HOVER_TOOLBAR_BUTTON_STYLE = "-fx-background-insets: 0;-fx-padding:3,6,3,6;";

    /**
     * Don't instantiate me. I bite.
     */
    private Utils() {
        throw new AssertionError();
    }

    /**
     * Beep!
     */
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Get the debug log file, useful for debugging if something goes wrong (the
     * log is printed out to this location.)
     */
    public static File getDebugLog() {
        return new File(QueleaProperties.getQueleaUserHome(), "quelea-debuglog.txt");
    }

    /**
     * Set the button style for any buttons that are to be placed on a toolbar.
     * Change the padding and remove the default border.
     * <p/>
     * @param button the button to style.
     */
    public static void setToolbarButtonStyle(final Node button) {
        button.setStyle(Utils.TOOLBAR_BUTTON_STYLE);
        if(button instanceof ToggleButton) {
            ToggleButton toggle = (ToggleButton) button;
            toggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    if(t1) {
                        button.setStyle(Utils.HOVER_TOOLBAR_BUTTON_STYLE);
                    }
                    else {
                        button.setStyle(Utils.TOOLBAR_BUTTON_STYLE);
                    }
                }
            });
        }
        button.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                button.setStyle(Utils.HOVER_TOOLBAR_BUTTON_STYLE);
            }
        });
        button.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!(button instanceof ToggleButton && ((ToggleButton) button).isSelected())) {
                    button.setStyle(Utils.TOOLBAR_BUTTON_STYLE);
                }
            }
        });
    }

    /**
     * Sleep ignoring the exception.
     * <p/>
     * @param millis milliseconds to sleep.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException ex) {
            //Nothing
        }
    }

    public static boolean isOffscreen(SceneInfo info) {
        for(Screen screen : Screen.getScreens()) {
            if(screen.getBounds().intersects(info.getBounds())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Run something on the JavaFX platform thread and wait for it to complete.
     * <p/>
     * @param runnable the runnable to run.
     */
    public static void fxRunAndWait(Runnable runnable) {
        if(Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        final Semaphore sem = new Semaphore(1);
        try {
            sem.acquire();
        }
        catch(InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Interrupted!", ex);
        }
        Platform.runLater(runnable);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sem.release();
            }
        });
        try {
            sem.acquire();
        }
        catch(InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Interrupted!", ex);
        }
    }

    /**
     * Add the Quelea icon(s) to a stage.
     * <p/>
     * @param stage the stage to add the icons to.
     */
    public static void addIconsToStage(Stage stage) {
//        stage.getIcons().add(new Image("file:icons/logo64.png"));
//        stage.getIcons().add(new Image("file:icons/logo48.png"));
        stage.getIcons().add(new Image("file:icons/logo32.png"));
//        stage.getIcons().add(new Image("file:icons/logo16.png"));
    }

    /**
     * Converts an AWT rectangle to a JavaFX bounds object.
     * <p/>
     * @param rect the rectangle to convert.
     * @return the equivalent bounds.
     */
    public static Bounds getBoundsFromRect(Rectangle rect) {
        return new BoundingBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Converts a JavaFX Rectangle2D to a JavaFX bounds object.
     * <p/>
     * @param rect the Rectangle2D to convert.
     * @return the equivalent bounds.
     * <p/>
     */
    public static Bounds getBoundsFromRect2D(Rectangle2D rect) {
        return new BoundingBox(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Determine if we're running in a 64 bit JVM.
     * <p/>
     * @return true if it's a 64 bit JVM, false if it's 32 bit (or something
     * else.)
     */
    public static boolean is64Bit() {
        return System.getProperty("os.arch").contains("64"); //Rudimentary...
    }

    /**
     * Determine if we're running on a mac.
     * <p/>
     * @return true if we're running on a mac, false otherwise.
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Determine if we're running on Linux.
     * <p/>
     * @return true if we're running on Linux, false otherwise.
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * Determine if we're running on Windows.
     * <p/>
     * @return true if we're running on Windows, false otherwise.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * Get a file name without its extension.
     * <p/>
     * @param nameWithExtension the file name with the extension.
     * @return the file name without the extension.
     */
    public static String getFileNameWithoutExtension(String nameWithExtension) {
        if(!nameWithExtension.contains(".")) {
            return nameWithExtension;
        }
        String[] parts = nameWithExtension.split("\\.");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < parts.length - 1; i++) {
            ret.append(parts[i]);
            if(i != parts.length - 2) {
                ret.append(".");
            }
        }
        return ret.toString();
    }

    /**
     * Update a song in the background.
     * <p/>
     * @param song the song to update.
     * @param showError true if an error should be shown if there's a problem
     * updating the song, false otherwise.
     * @param silent true if we should update the song without showing a bar on
     * the status panel.
     */
    public static void updateSongInBackground(final SongDisplayable song, final boolean showError, final boolean silent) {
        final Runnable updateRunner = new Runnable() {
            @Override
            public void run() {
                boolean result = SongManager.get().updateSong(song);
                if(!result && showError) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("error.udpating.song.text"));
                        }
                    });
                }
            }
        };
        if(silent) {
            new Thread(updateRunner).start();
        }
        else {
            Utils.fxRunAndWait(new Runnable() {
                @Override
                public void run() {
                    final StatusPanel statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("updating.db"));
                    statusPanel.removeCancelButton();
                    new Thread() {
                        @Override
                        public void run() {
                            updateRunner.run();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    statusPanel.done();
                                }
                            });
                        }
                    }.start();
                }
            });
        }
    }

    /**
     * Wrap a runnable as one having a low priority.
     * <p/>
     * @param task the runnable to wrap.
     * @return a runnable having a low priority.
     */
    public static Runnable wrapAsLowPriority(final Runnable task) {
        return new Runnable() {
            @SuppressWarnings("CallToThreadYield")
            @Override
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
     * Get a font identical to the one given apart from in size.
     * <p/>
     * @param font the original font.
     * @param size the size of the new font.
     * @return the resized font.
     */
    public static Font getDifferentSizeFont(Font font, float size) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        for(Entry<TextAttribute, ?> entry : font.getAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        if(attributes.get(TextAttribute.SIZE) != null) {
            attributes.put(TextAttribute.SIZE, size);
        }
        return new Font(attributes);
    }

    /**
     * Calculates the largest size of the given font for which the given string
     * will fit into the given size.
     * <p/>
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

        while(maxSize - minSize > 2) {
            FontMetrics fm = g.getFontMetrics(new Font(font.getName(), font.getStyle(), curSize));
            int fontWidth = fm.stringWidth(string);
            int fontHeight = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();

            if((fontWidth > width) || (fontHeight > height)) {
                maxSize = curSize;
                curSize = (maxSize + minSize) / 2;
            }
            else {
                minSize = curSize;
                curSize = (minSize + maxSize) / 2;
            }
        }

        return curSize;
    }

    /**
     * Get the difference between two colours, from 0 to 100 where 100 is most
     * difference and 0 is least different.
     * <p/>
     * @param a the first colour
     * @param b the second colour
     * @return the difference between the colours.
     */
    public static double getColorDifference(Color a, Color b) {
        double ret = Math.abs(a.getRed() - b.getRed()) + Math.abs(a.getGreen() - b.getGreen()) + Math.abs(a.getBlue() - b.getBlue());
        return (ret / (255 * 3)) * 100;
    }

    /**
     * Remove all HTML tags from a string.
     * <p/>
     * @param str the string to remove the tags from.
     * @return the string with the tags removed.
     */
    public static String removeTags(String str) {
        return str.replaceAll("\\<.*?>", "");
    }

    /**
     * Determine whether the given stage is completely on the given screen.
     * <p/>
     * @param stage the stage to check.
     * @param monitorNum the monitor number to check.
     * @return true if the frame is totally on the screen, false otherwise.
     */
    public static boolean isFrameOnScreen(Stage stage, int monitorNum) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        return gds[monitorNum].getDefaultConfiguration().getBounds().contains(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    /**
     * Centre the given stage on the given monitor.
     * <p/>
     * @param stage the stage to centre.
     * @param monitorNum the monitor number to centre the frame on.
     */
    public static void centreOnMonitor(final Stage stage, int monitorNum) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        Rectangle bounds = gds[monitorNum].getDefaultConfiguration().getBounds();
        final int centreX = (int) (((int) (bounds.getMaxX() - bounds.getMinX()) / 2) + bounds.getMinX());
        final int centreY = (int) (((int) (bounds.getMaxY() - bounds.getMinY()) / 2) + bounds.getMinY());
        Runnable locationSetter = new Runnable() {
            @Override
            public void run() {
                stage.setX(centreX - stage.getWidth() / 2);
                stage.setY(centreY - stage.getHeight() / 2);
            }
        };
        if(Platform.isFxApplicationThread()) {
            locationSetter.run();
        }
        else {
            Platform.runLater(locationSetter);
        }
    }

    /**
     * Remove duplicates in a list whilst maintaining the order.
     * <p/>
     * @param <T> the type of the list.
     * @param list the list to remove duplicates.
     */
    public static <T> void removeDuplicateWithOrder(List<T> list) {
        Set<T> set = new HashSet<>();
        List<T> newList = new ArrayList<>();
        for(Iterator<T> iter = list.iterator(); iter.hasNext();) {
            T element = iter.next();
            if(set.add(element)) {
                newList.add(element);
            }
        }
        list.clear();
        list.addAll(newList);
    }

    /**
     * Copy a file from one place to another.
     * <p/>
     * @param sourceFile the source file
     * @param destFile the destination file
     * @throws IOException if something goes wrong.
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(sourceFile.isDirectory()) {
            if(sourceFile.getName().equals(".svn")) {
                return;
            }
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            for(File file : sourceFile.listFiles()) {
                copyFile(file, new File(destFile, file.getName()));
            }
            return;
        }
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Capitalise the first letter of a string.
     * <p/>
     * @param line the input string.
     * @return the the string with the first letter capitalised.
     */
    public static String capitaliseFirst(String line) {
        if(line.isEmpty()) {
            return line;
        }
        StringBuilder ret = new StringBuilder(line);
        ret.setCharAt(0, Character.toUpperCase(line.charAt(0)));
        return ret.toString();
    }

    /**
     * Get an abbreviation from a name based on the first letter of each word of
     * the name.
     * <p/>
     * @param name the name to use for the abbreviation.
     * @return the abbreviation.
     */
    public static String getAbbreviation(String name) {
        StringBuilder ret = new StringBuilder();
        String[] parts = name.split(" ");
        for(String str : parts) {
            if(!str.isEmpty()) {
                ret.append(Character.toUpperCase(str.charAt(0)));
            }
        }
        return ret.toString();
    }

    /**
     * Escape the XML special characters.
     * <p/>
     * @param s the string to escape.
     * @return the escaped string.
     */
    public static String escapeXML(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("’", "&apos;")
                /*
                 * TODO: These last two are there to solve a bug with funny
                 * characters ending up in the XML file. Still not sure how
                 * or why, but this does the job for now... Yeah. Bodge.
                 */
                .replace(new String(new byte[]{11}), "\n")
                .replace(new String(new byte[]{-3}), " ");
    }

    /**
     * Get the textual content from a file as a string, returning the given
     * error string if a problem occurs retrieving the content.
     * <p/>
     * @param fileName the filename to get the text from.
     * @param errorText the error string to return if things go wrong.
     * @return hopefully the text content of the file, or the errorText string
     * if we can't get the text content for some reason.
     */
    public static synchronized String getTextFromFile(String fileName, String errorText) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        }
        catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get the contents of " + fileName, ex);
            return errorText;
        }
    }

    /**
     * Resize a given image to the given width and height.
     * <p/>
     * @param image the image to resize.
     * @param width the width of the new image.
     * @param height the height of the new image.
     * @return the resized image.
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        if(width > 0 && height > 0 && (image.getWidth() != width || image.getHeight() != height)) {
            BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bdest.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, width, height, null);
            return bdest;
        }
        else {
            return image;
        }
    }

    /**
     * Determine whether a file is an image file.
     * <p/>
     * @param file the file to check.
     * @return true if the file is an image, false otherwise.
     */
    public static boolean fileIsImage(File file) {
        if(file.isDirectory() && !file.isHidden()) {
            return true;
        }
        else {
            for(String ext : getImageExtensions()) {
                if(hasExtension(file, ext)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Get a list of all supported image extensions.
     * <p/>
     * @return a list of all supported image extensions.
     */
    public static List<String> getImageExtensions() {
        List<String> ret = new ArrayList<>();
        ret.add("png");
        ret.add("tiff");
        ret.add("jpg");
        ret.add("jpeg");
        ret.add("gif");
        ret.add("bmp");
        return ret;
    }

    public static List<String> getImageFileExtensions() {
        List<String> ret = new ArrayList<>();
        for(String str : getImageExtensions()) {
            ret.add("*." + str);
        }
        return ret;
    }

    /**
     * Determine whether the given file has the given case insensitive
     * extension.
     * <p/>
     * @param file the file to check.
     * @param ext the extension to check.
     * @return true if it has the given extension, false otherwise.
     */
    public static boolean hasExtension(File file, String ext) {
        String name = file.getName().toLowerCase();
        if(!name.contains(".")) {
            return false;
        }
        String[] parts = name.split("\\.");
        String suffix = parts[parts.length - 1].toLowerCase().trim();
        return suffix.equals(ext.trim().toLowerCase());
    }

    /**
     * Get the names of all the fonts available on the current system.
     * <p/>
     * @return the names of all the fonts available.
     */
    public static String[] getAllFonts() {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Set<String> names = new HashSet<>();
        for(int i = 0; i < fonts.length; i++) {
            names.add(fonts[i].getFamily());
        }
        List<String> namesList = new ArrayList<>(names.size());
        for(String name : names) {
            namesList.add(name);
        }
        Collections.sort(namesList);
        return namesList.toArray(new String[namesList.size()]);
    }

    /**
     * Get an image filled with the specified colour.
     * <p/>
     * @param color the colour of the image.
     * @param width the width of the image.
     * @param height the height of the image.
     * @return the image.
     */
    public static Image getImageFromColour(final Color color) {
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, color);
        return image;
    }

    /**
     * Get an image to be shown as the background in place of a playing video.
     * <p/>
     * @return the image to be shown in place of a playing video.
     */
    public static Image getVidBlankImage() {
        return new Image("file:icons/vid preview.png");
    }

    /**
     * Parse a colour string to a colour.
     * <p/>
     * @param colour the colour string.
     * @return the colour.
     */
    public static Color parseColour(String colour) {
        if(colour == null || colour.trim().isEmpty()) {
            return ThemeDTO.DEFAULT_FONT_COLOR;
        }
        if(!colour.contains("[")) {
            try {
                return Color.web(colour);
            }
            catch(IllegalArgumentException ex) {
                return ThemeDTO.DEFAULT_FONT_COLOR;
            }
        }
        colour = colour.substring(colour.indexOf('[') + 1, colour.indexOf(']'));
        String[] parts = colour.split(",");
        double red = Double.parseDouble(parts[0].split("=")[1]);
        double green = Double.parseDouble(parts[1].split("=")[1]);
        double blue = Double.parseDouble(parts[2].split("=")[1]);
        if(red > 1.0) {
            red /= 255;
        }
        if(green > 1.0) {
            green /= 255;
        }
        if(blue > 1.0) {
            blue /= 255;
        }
        return new Color(red, green, blue, 1);
    }

    public static void enableDragAndDrop() {
        DragAndDrop.enable();
    }
}
