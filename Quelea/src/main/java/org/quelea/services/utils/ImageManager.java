/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;

/**
 * An image manager that should be used to deal with all large images (to keep
 * memory overhead to a minimum.)
 * <p>
 * @author Michael
 */
public class ImageManager {

    public static final ImageManager INSTANCE = new ImageManager();
    private static final Logger LOGGER = LoggerUtils.getLogger();
    /**
     * The formats (file extensions) that JavaFX reads itself.
     */
    private static final Set<String> JAVAFX_FORMATS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "wbmp");
    private static final Object CONVERT_LOCK = new Object();
    /**
     * Could possibly be a soft hashmap in future to aid with caching even when
     * a reference isn't elsewhere? Not sure we need this for now though, so
     * leaving as is.
     */
    private final WeakHashMap<String, Image> images;

    private ImageManager() {
        images = new WeakHashMap<>();
    }

    /**
     * Get an image from a uri. This will load the full image into memory,
     * whatever its size. If you just need a preview of an image, then use the
     * other method in this class to load a smaller version! Images returned
     * using this method are cached, so if the same image is being used
     * elsewhere that points to this file, you'll get the same image object back
     * (as long as the previous image was obtained through this method.)
     * <p>
     * @param uri the URI to load.
     * @return the image at this URI.
     */
    public Image getImage(String uri) {
        if(uri == null) {
            return Utils.getImageFromColour(Color.BLACK);
        }
        Image img = images.get(uri);
        if(img == null) {
            img = new Image(loadableUri(uri));
            images.put(uri, img);
        }
        return img;
    }

    /**
     * Get an image with a particular size. Images using this method are not
     * cached at present, so only use for small images. (We could cache in the
     * future if necessary.)
     * <p>
     * @param uri the URI of the image to load.
     * @param width the width of the image.
     * @param height the height of the image.
     * @param preserveRatio true if the aspect ratio should be preserved, false
     * otherwise.
     * @return
     */
    public Image getImage(String uri, double width, double height, boolean preserveRatio) {
        return new Image(loadableUri(uri), width, height, preserveRatio, true);
    }

    /**
     * Get a URI that JavaFX can load the given image file from.
     * <p>
     * @param file the image file.
     * @return the URI to load the image from.
     * @see #loadableUri(String)
     */
    public static String loadableUri(File file) {
        return loadableUri(file.toURI().toString());
    }

    /**
     * Get a URI that JavaFX can load an image from. JavaFX only reads a few
     * formats itself; an image in another format that Java can read (such as
     * TIFF) is converted to PNG the first time, into a cache folder, and the
     * URI of the converted copy is returned. The copy is made again if the
     * original changes.
     * <p>
     * @param uri the URI of the image.
     * @return the URI to load the image from: the same URI if JavaFX can load
     * it (or it can't be converted), otherwise the URI of the converted copy.
     */
    public static String loadableUri(String uri) {
        if (uri == null) {
            return null;
        }
        File file = toFile(uri);
        if (file == null || !file.isFile()) {
            return uri;
        }
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (extension.isEmpty() || JAVAFX_FORMATS.contains(extension)) {
            return uri;
        }
        synchronized (CONVERT_LOCK) {
            try {
                File cached = new File(getCacheDir(), cacheName(file) + ".png");
                if (!cached.isFile()) {
                    BufferedImage original = ImageIO.read(file);
                    if (original == null) {
                        return uri;
                    }
                    // Draw it onto a plain image first: the readers can give
                    // image types that can't be written straight to PNG
                    BufferedImage plain = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = plain.createGraphics();
                    graphics.drawImage(original, 0, 0, null);
                    graphics.dispose();
                    cached.getParentFile().mkdirs();
                    File partial = new File(cached.getParentFile(), cached.getName() + ".part");
                    if (!ImageIO.write(plain, "png", partial) || !partial.renameTo(cached)) {
                        partial.delete();
                        return uri;
                    }
                }
                return cached.toURI().toString();
            } catch (IOException | RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Couldn't convert image " + file, ex);
                return uri;
            }
        }
    }

    private static File toFile(String uri) {
        if (!uri.startsWith("file:")) {
            return null;
        }
        try {
            return new File(URI.create(uri));
        } catch (IllegalArgumentException ex) {
            // Some places build "file:" + path without encoding it
            return new File(uri.substring("file:".length()));
        }
    }

    private static File getCacheDir() {
        return new File(QueleaProperties.get().getQueleaUserHome(), "imagecache");
    }

    private static String cacheName(File file) {
        String key = file.getAbsolutePath() + "|" + file.lastModified() + "|" + file.length();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder ret = new StringBuilder();
            for (byte b : hash) {
                ret.append(String.format("%02x", b));
            }
            return ret.toString();
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(key.hashCode());
        }
    }

}
