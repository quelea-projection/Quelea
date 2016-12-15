/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.utils;

import java.util.WeakHashMap;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * An image manager that should be used to deal with all large images (to keep
 * memory overhead to a minimum.)
 * <p>
 * @author Michael
 */
public class ImageManager {

    public static final ImageManager INSTANCE = new ImageManager();
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
            img = new Image(uri);
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
        return new Image(uri, width, height, preserveRatio, true);
    }

}
