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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
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
import org.apache.commons.text.StringEscapeUtils;
import org.javafx.dialog.Dialog;
import org.mozilla.universalchardet.UniversalDetector;
import org.quelea.data.ThemeDTO;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * General utility class containing a bunch of static methods.
 * <p/>
 * @author Michael, Ben
 */
public final class Utils {

	private static final Logger LOGGER = LoggerUtils.getLogger();
	public static final String TOOLBAR_BUTTON_STYLE = "-fx-background-insets: 0;-fx-background-color: rgba(0, 0, 0, 0);-fx-padding:3,6,3,6;-fx-text-fill: grey;";
	public static final String HOVER_TOOLBAR_BUTTON_STYLE = "-fx-background-insets: 0;-fx-padding:3,6,3,6;-fx-text-fill: grey;";

	/**
	 * Don't instantiate me. I bite.
	 */
	private Utils() {
		throw new AssertionError();
	}

	public static File getChangedFile(org.w3c.dom.Node node, Map<String, String> fileChanges) {
		return getChangedFile(node.getTextContent(), fileChanges);
	}

	public static File getChangedFile(String filePath, Map<String, String> fileChanges) {
		File file = new File(filePath);
		String changedFile = fileChanges.get(file.getAbsolutePath());
		if (!file.exists() && changedFile != null) {
			LOGGER.log(Level.INFO, "Changing {0} to {1}", new Object[]{file.getAbsolutePath(), changedFile});
			file = new File(changedFile);
		}
		return file;
	}

	public static String toRelativeStorePath(File f) {
		String[] parts = f.getAbsolutePath().split(Pattern.quote(System.getProperty("file.separator")));
		parts = Arrays.copyOfRange(parts, 1, parts.length);
		return String.join(System.getProperty("file.separator"), parts);
	}

	/**
	 * Get the debug log file, useful for debugging if something goes wrong (the
	 * log is printed out to this location.)
	 * <p>
	 * @return the debug log file.
	 */
	public static File getDebugLog() {
		return new File(QueleaProperties.get().getQueleaUserHome(), "quelea-debuglog.txt");
	}

	/**
	 * Check if a given file is in a given directory. (Doesn't include
	 * subfolders.)
	 * <p>
	 * @param dir the directory to check.
	 * @param file the file to check.
	 * @return true if the given file is in the given directory, false
	 * otherwise.
	 */
	public static boolean isInDir(File dir, File file) {
		File[] files = QueleaProperties.get().getImageDir().listFiles();
		for (File listFile : files) {
			if (file.equals(listFile)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the button style for any buttons that are to be placed on a toolbar.
	 * Change the padding and remove the default border.
	 * <p/>
	 * @param button the button to style.
	 */
	public static void setToolbarButtonStyle(final Node button) {
		button.setStyle(Utils.TOOLBAR_BUTTON_STYLE);
		if (button instanceof ToggleButton) {
			ToggleButton toggle = (ToggleButton) button;
			toggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
					if (t1) {
						button.setStyle(Utils.HOVER_TOOLBAR_BUTTON_STYLE);
					} else {
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
				if (!(button instanceof ToggleButton && ((ToggleButton) button).isSelected())) {
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
		} catch (InterruptedException ex) {
			//Nothing
		}
	}

	public static boolean isOffscreen(SceneInfo info) {
		for (Screen screen : Screen.getScreens()) {
			if (screen.getBounds().intersects(info.getBounds())) {
				return false;
			}
		}
		return true;
	}

	public static String incrementExtension(String name, String ext) {
		name = name.substring(0, name.length() - 4).trim();
		Pattern p = Pattern.compile(".*\\(([0-9]+)\\)");
		Matcher matcher = p.matcher(name);
		if (matcher.matches()) {
			int suffixLength = matcher.group(1).length() + 2;
			int nextNum = Integer.parseInt(matcher.group(1)) + 1;
			return name.substring(0, name.length() - suffixLength) + "(" + nextNum + ")." + ext;
		} else {
			return name + "(2)." + ext;
		}
	}

	/**
	 * Run something on the JavaFX platform thread and wait for it to complete.
	 * <p/>
	 * @param runnable the runnable to run.
	 */
	public static void fxRunAndWait(final Runnable runnable) {
		try {
			if (Platform.isFxApplicationThread()) {
				try {
					runnable.run();
				} catch (Exception e) {
					throw new ExecutionException(e);
				}
			} else {
				final Lock lock = new ReentrantLock();
				final Condition condition = lock.newCondition();
				lock.lock();
				try {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							lock.lock();
							try {
								runnable.run();
							} finally {
								try {
									condition.signal();
								} finally {
									lock.unlock();
								}
							}
						}
					});
					condition.await();
				} finally {
					lock.unlock();
				}
			}
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Execution error", ex);
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
	 * Get the string to pass VLC from the given video file. In many cases this
	 * is just the path, in the case of vlcarg files it is the contents of the
	 * file to pass VLC.
	 *
	 * @param file the file to grab the VLC path from.
	 * @return the VLC path.
	 */
	public static String getVLCStringFromFile(File file) {
		final String path = file.getAbsolutePath();
		final String[] parts = path.split("\\.");
		if (parts[parts.length - 1].trim().equalsIgnoreCase("vlcarg")) {
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(path));
				return new String(encoded, "UTF-8");
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING, "Couldn't get VLC string from file", ex);
				return path;
			}
		} else {
			return path;
		}
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
		if (!nameWithExtension.contains(".")) {
			return nameWithExtension;
		}
		String[] parts = nameWithExtension.split("\\.");
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			ret.append(parts[i]);
			if (i != parts.length - 2) {
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
		if (!song.checkDBUpdate() || song.isQuickInsert()) {
			return;
		}
		final Runnable updateRunner = new Runnable() {
			@Override
			public void run() {
				boolean result = SongManager.get().updateSong(song);
				if (result && song.checkDBUpdate()) {
					ObservableList<SongDisplayable> songs = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getListView().getItems();
					int replaceIdx = -1;
					for (int i = 0; i < songs.size(); i++) {
						if (song.getID() == songs.get(i).getID()) {
							replaceIdx = i;
							break;
						}
					}
					final int replaceIdxFi = replaceIdx;
					if (replaceIdx != -1) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								songs.remove(replaceIdxFi);
								songs.add(replaceIdxFi, song);
							}
						});
					}
				}
				if (!result && showError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Dialog.showError(LabelGrabber.INSTANCE.getLabel("error.text"), LabelGrabber.INSTANCE.getLabel("error.udpating.song.text"));
						}
					});
				}
			}
		};
		if (silent) {
			new Thread(updateRunner).start();
		} else {
			Utils.fxRunAndWait(new Runnable() {
				@Override
				public void run() {
					final StatusPanel statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("updating.db"));
					statusPanel.removeCancelButton();
					new Thread() {
						@Override
						public void run() {
							updateRunner.run();
							Platform.runLater(statusPanel::done);
						}
					}.start();
				}
			});
		}
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

	public static boolean fxThread() {
		return Platform.isFxApplicationThread();
	}

	public static void checkFXThread() {
		if (!fxThread()) {
			LOGGER.log(Level.WARNING, "Not on FX Thread!", new AssertionError());
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
		for (T element : list) {
			if (set.add(element)) {
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
	 * <p/>
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
	 * Get an abbreviation from a name based on the first letter of each word of
	 * the name.
	 * <p/>
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
	 * <p/>
	 * @param s the string to escape.
	 * @return the escaped string.
	 */
	public static String escapeXML(String s) {
		return StringEscapeUtils.escapeXml11(s);
	}

	public static synchronized String getTextFromFile(String fileName, String errorText) {
		return getTextFromFile(fileName, errorText, "UTF-8");
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
	public static synchronized String getTextFromFile(String fileName, String errorText, String encoding) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding))) {
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
	 * Determine whether a file is an image file.
	 * <p/>
	 * @param file the file to check.
	 * @return true if the file is an image, false otherwise.
	 */
	public static boolean fileIsImage(File file) {
		if (file.isDirectory() && !file.isHidden()) {
			return true;
		} else {
			for (String ext : getImageExtensions()) {
				if (hasExtension(file, ext)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Determine whether a file is an video file.
	 * <p/>
	 * @param file the file to check.
	 * @return true if the file is an video, false otherwise.
	 */
	public static boolean fileIsVideo(File file) {
		if (file.isDirectory() && !file.isHidden()) {
			return true;
		} else {
			for (String ext : getVideoExtensions()) {
				if (hasExtension(file, ext)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Determine whether a file is a timer file.
	 * <p/>
	 * @param file the file to check.
	 * @return true if the file is an timer, false otherwise.
	 */
	public static boolean fileIsTimer(File file) {
		if (file.isDirectory() && !file.isHidden()) {
			return true;
		} else {
			if (hasExtension(file, "*.cdt")) {
				return true;
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
		ret.add("PNG");
		ret.add("tiff");
		ret.add("TIFF");
		ret.add("jpg");
		ret.add("JPG");
		ret.add("jpeg");
		ret.add("JPEG");
		ret.add("gif");
		ret.add("GIF");
		ret.add("bmp");
		ret.add("BMP");
		return ret;
	}

	/**
	 * Get a list of all supported video extensions.
	 * <p/>
	 * @return a list of all supported video extensions.
	 */
	public static List<String> getVideoExtensions() {
		List<String> ret = new ArrayList<>();
		ret.add("mkv");
		ret.add("MKV");
		ret.add("mp4");
		ret.add("MP4");
		ret.add("m4v");
		ret.add("M4V");
		ret.add("flv");
		ret.add("FLV");
		ret.add("avi");
		ret.add("AVI");
		ret.add("mov");
		ret.add("MOV");
		ret.add("rm");
		ret.add("RM");
		ret.add("mpg");
		ret.add("MPG");
		ret.add("mpeg");
		ret.add("MPEG");
		ret.add("wmv");
		ret.add("WMV");
		ret.add("ogm");
		ret.add("OGM");
		ret.add("ogg");
		ret.add("OGG");
		ret.add("mrl");
		ret.add("MRL");
		ret.add("asx");
		ret.add("ASX");
		ret.add("m2ts");
		ret.add("M2TS");
		ret.add("ts");
		ret.add("TS");
		return ret;
	}

	/**
	 * Get a list of all supported audio extensions.
	 * <p/>
	 * @return a list of all supported audio extensions.
	 */
	public static List<String> getAudioExtensions() {
		List<String> ret = new ArrayList<>();
		ret.add("mp3");
		ret.add("MP3");
		ret.add("wav");
		ret.add("WAV");
		ret.add("wma");
		ret.add("WMA");
		// TODO: Add more extensions
		return ret;
	}

	/**
	 * Get a list of all supported multimedia extensions.
	 * <p/>
	 * @return a list of all supported multimedia extensions.
	 */
	public static List<String> getMultimediaExtensions() {
		List<String> ret = new ArrayList<>();
		ret.addAll(getVideoExtensions());
		ret.addAll(getAudioExtensions());
		return ret;
	}

	public static List<String> getImageAndVideoExtensions() {
		List<String> ret = new ArrayList<>();
		ret.addAll(getVideoExtensions());
		ret.addAll(getImageExtensions());
		return ret;
	}

	/**
	 * Get file extensions (in *.ext format) from a list of normal extensions.
	 * <p/>
	 * @param extensions the list of normal extensions.
	 * @return a list of file extensions.
	 */
	public static List<String> getFileExtensions(List<String> extensions) {
		List<String> ret = new ArrayList<>();
		for (String str : extensions) {
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
		if (!name.contains(".")) {
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
		for (Font font : fonts) {
			names.add(font.getFamily());
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
	 * <p/>
	 * @param color the colour of the image.
	 * @return the image.
	 */
	public static Image getImageFromColour(final Color color) {
		WritableImage image = new WritableImage(2, 2);
		PixelWriter writer = image.getPixelWriter();
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				writer.setColor(i, j, color);
			}
		}
		return image;
	}

	/**
	 * Parse a colour string to a colour.
	 * <p/>
	 * @param colour the colour string.
	 * @return the colour.
	 */
	public static Color parseColour(String colour) {
		if (colour == null || colour.trim().isEmpty()) {
			return ThemeDTO.DEFAULT_FONT_COLOR;
		}
		if (!colour.contains("[")) {
			try {
				return Color.web(colour);
			} catch (IllegalArgumentException ex) {
				return ThemeDTO.DEFAULT_FONT_COLOR;
			}
		}
		colour = colour.substring(colour.indexOf('[') + 1, colour.indexOf(']'));
		String[] parts = colour.split(",");
		double red = Double.parseDouble(parts[0].split("=")[1]);
		double green = Double.parseDouble(parts[1].split("=")[1]);
		double blue = Double.parseDouble(parts[2].split("=")[1]);
		if (red > 1.0) {
			red /= 255;
		}
		if (green > 1.0) {
			green /= 255;
		}
		if (blue > 1.0) {
			blue /= 255;
		}
		return new Color(red, green, blue, 1);
	}

	/**
	 * Extract a zip file to a temporary location and retrieve a list of all
	 * extracted files.
	 *
	 * @param zip the zip file to extract
	 * @return a list of all extracted files.
	 */
	public static List<File> extractZip(File zip) {
		try {
			return extractZipWithCharset(zip, null);
		} catch (Exception ex) {
			return extractZipWithCharset(zip, Charset.forName("CP866"));
		}
	}

	/**
	 * Extract a zip file to a temporary location and retrieve a list of all
	 * extracted files.
	 *
	 * @param zip the zip file to extract
	 * @param charset the charset to use on this zip file
	 * @return a list of all extracted files.
	 */
	private static List<File> extractZipWithCharset(File zip, Charset charset) {
		LOGGER.log(Level.INFO, "Extracting zip file {0}", zip.getAbsolutePath());
		int BUFFER = 2048;
		if (charset == null) {
			charset = StandardCharsets.UTF_8;
		}
		try (ZipFile zipFile = new ZipFile(zip, charset)) {
			File tempFolder = Files.createTempDirectory("qzipextract").toFile();
			tempFolder.deleteOnExit();

			Enumeration zipFileEntries = zipFile.entries();

			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(tempFolder, currentEntry);
				File destinationParent = destFile.getParentFile();

				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					try (BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry))) {
						int currentByte;
						byte[] data = new byte[BUFFER];

						FileOutputStream fos = new FileOutputStream(destFile);
						try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
							while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
								dest.write(data, 0, currentByte);
							}
							dest.flush();
						}
					}
				}

			}

			return Files.find(Paths.get(tempFolder.getAbsolutePath()), 999, (p, bfa) -> bfa.isRegularFile()).map(path -> path.toFile())
					.collect(Collectors.toList());
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Error extracting zip", ex);
			return Collections.emptyList();
		}
	}

	public static String getEncoding(File file) {
		String encoding;
		try {
			encoding = UniversalDetector.detectCharset(file);
		}
		catch (IOException ex) {
			encoding = null;
		}
		if (encoding == null) {
			encoding = "UTF-8";
			LOGGER.log(Level.WARNING, "Couldn't detect encoding, defaulting to " + encoding + " for " + file.getAbsolutePath());
		}
		else
		{
			LOGGER.log(Level.INFO, "Detected " + encoding + " encoding for " + file.getAbsolutePath());
		}

		return encoding;
	}

}
