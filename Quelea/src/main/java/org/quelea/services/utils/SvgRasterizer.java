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
package org.quelea.services.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns SVG (vector) images into bitmaps of any size, so a vector logo can be
 * drawn sharply at the projector's real resolution. Uses Apache Batik, which
 * is already on the classpath for FOP (PDF printing).
 *
 * @author Arvid
 */
public final class SvgRasterizer {

    /**
     * Largest bitmap we'll render, in either direction. Way beyond any
     * projector, but stops a silly size from using all the memory.
     */
    public static final int MAX_SIZE = 8192;

    private static final Map<String, CachedSize> SIZES = new ConcurrentHashMap<>();

    private record CachedSize(long lastModified, Dimension2D size) {
    }

    private SvgRasterizer() {
    }

    /**
     * Determine whether the given file name / URI points to an SVG image.
     *
     * @param uri the file name or URI.
     * @return true if it ends with .svg or .svgz (ignoring case).
     */
    public static boolean isSvg(String uri) {
        if (uri == null) {
            return false;
        }
        String lower = uri.toLowerCase(Locale.ROOT);
        return lower.endsWith(".svg") || lower.endsWith(".svgz");
    }

    /**
     * Get the natural size of an SVG image, in SVG user units (pixels). Only
     * its aspect ratio really matters, since it can be drawn at any size.
     * <p>
     * Uses the width and height of the image if they are absolute, otherwise
     * its viewBox, otherwise the bounds of what is drawn. The result is
     * cached (until the file changes).
     *
     * @param uri the URI of the SVG file.
     * @return the natural size, never zero.
     * @throws IOException if the file can't be read or isn't a valid SVG.
     */
    public static Dimension2D getIntrinsicSize(String uri) throws IOException {
        long lastModified = lastModified(uri);
        CachedSize cached = SIZES.get(uri);
        if (cached != null && cached.lastModified() == lastModified) {
            return cached.size();
        }
        Dimension2D size = readIntrinsicSize(uri);
        SIZES.put(uri, new CachedSize(lastModified, size));
        return size;
    }

    private static Dimension2D readIntrinsicSize(String uri) throws IOException {
        UserAgentAdapter userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);
        try {
            Document doc = loader.loadDocument(uri);
            Element root = doc.getDocumentElement();
            GraphicsNode node = new GVTBuilder().build(ctx, doc);

            String width = root.getAttributeNS(null, "width").trim();
            String height = root.getAttributeNS(null, "height").trim();
            if (isAbsolute(width) && isAbsolute(height)) {
                java.awt.geom.Dimension2D docSize = ctx.getDocumentSize();
                if (docSize != null && docSize.getWidth() > 0 && docSize.getHeight() > 0) {
                    return new Dimension2D(docSize.getWidth(), docSize.getHeight());
                }
            }
            String viewBox = root.getAttributeNS(null, "viewBox").trim();
            if (!viewBox.isEmpty()) {
                float[] box = ViewBox.parseViewBoxAttribute(root, viewBox, ctx);
                if (box != null && box[2] > 0 && box[3] > 0) {
                    return new Dimension2D(box[2], box[3]);
                }
            }
            java.awt.geom.Rectangle2D bounds = node == null ? null : node.getPrimitiveBounds();
            if (bounds != null && bounds.getWidth() > 0 && bounds.getHeight() > 0) {
                return new Dimension2D(bounds.getMaxX(), bounds.getMaxY());
            }
            return new Dimension2D(100, 100);
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            // Batik reports invalid documents with (unchecked) BridgeExceptions & co.
            throw new IOException("Couldn't read SVG " + uri + ": " + ex.getMessage(), ex);
        } finally {
            ctx.dispose();
        }
    }

    private static boolean isAbsolute(String length) {
        return !length.isEmpty() && !length.endsWith("%");
    }

    /**
     * Render an SVG image to a bitmap of exactly the given size. The image is
     * scaled to fit, keeping its aspect ratio (any spare room is transparent).
     * Can (and preferably should) be called off the FX thread.
     *
     * @param uri    the URI of the SVG file.
     * @param width  width of the bitmap in pixels.
     * @param height height of the bitmap in pixels.
     * @return the rendered image (ARGB, transparent where the SVG draws
     * nothing).
     * @throws IOException if the file can't be read or isn't a valid SVG.
     */
    public static BufferedImage renderToBufferedImage(String uri, int width, int height) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Size must be positive, was " + width + "x" + height);
        }
        width = Math.min(width, MAX_SIZE);
        height = Math.min(height, MAX_SIZE);
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, (float) height);
        try {
            transcoder.transcode(new TranscoderInput(uri), null);
        } catch (TranscoderException | RuntimeException ex) {
            throw new IOException("Couldn't render SVG " + uri + ": " + ex.getMessage(), ex);
        }
        if (transcoder.image == null) {
            throw new IOException("Couldn't render SVG " + uri);
        }
        return transcoder.image;
    }

    /**
     * Render an SVG image to a JavaFX image of exactly the given size.
     *
     * @see #renderToBufferedImage(String, int, int)
     */
    public static WritableImage render(String uri, int width, int height) throws IOException {
        BufferedImage image = renderToBufferedImage(uri, width, height);
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels;
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        } else {
            pixels = image.getRGB(0, 0, w, h, null, 0, w);
        }
        WritableImage fxImage = new WritableImage(w, h);
        fxImage.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);
        return fxImage;
    }

    private static long lastModified(String uri) {
        try {
            URI parsed = URI.create(uri);
            if ("file".equalsIgnoreCase(parsed.getScheme())) {
                return new File(parsed).lastModified();
            }
        } catch (IllegalArgumentException ex) {
            // Not a file URI, so we can't tell when it changes
        }
        return 0;
    }

    /**
     * An image transcoder that keeps the result in memory instead of writing
     * it out.
     */
    private static class BufferedImageTranscoder extends ImageTranscoder {

        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.image = img;
        }
    }
}
