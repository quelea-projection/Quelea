/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.data.powerpoint;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.presentation.powerpoint.PowerPointService;

/**
 * A presentation whose slides PowerPoint rendered itself: PowerPoint saves the
 * presentation as a PDF (exactly as it would show it, fonts, SmartArt and all)
 * and the pages are drawn from that. Animations, transitions and videos are not
 * part of a PDF, so they are not shown.
 * <p>
 * The PDFs are kept in a cache named after the file contents, so a
 * presentation is exported once, even when a schedule with embedded media puts
 * it at a new place every time it is opened.
 */
public class RenderedPresentation implements Presentation {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int HEIGHT = 1080;
    private static final long EXPORT_TIMEOUT_MINUTES = 4;
    private final PresentationSlide[] slides;

    RenderedPresentation(File pdf) throws IOException {
        slides = render(pdf);
    }

    /**
     * @return true if presentations should be, and can be, rendered by
     * PowerPoint on this machine.
     */
    public static boolean isEnabled() {
        return QueleaProperties.get().getPPRender() && PowerPointService.canExportPdf();
    }

    /**
     * Get the presentation as rendered by PowerPoint, exporting it first if it
     * is not in the cache yet. Exporting waits for PowerPoint, so on the FX
     * thread only the cache is used.
     *
     * @param file the presentation.
     * @return the presentation, or null if PowerPoint could not render it.
     */
    public static RenderedPresentation load(File file) {
        try {
            File pdf = cacheFile(file);
            if (!pdf.isFile()) {
                if (Platform.isFxApplicationThread()) {
                    // E.g. a schedule being opened: don't hold up the UI, use
                    // Quelea's own rendering now and PowerPoint's next time.
                    LOGGER.log(Level.INFO, "{0} is not rendered by PowerPoint yet, rendering it in the background for next time", file);
                    PowerPointService.get().exportPdf(file, pdf).whenComplete((n, failure) -> {
                        if (failure != null) {
                            LOGGER.log(Level.WARNING, "PowerPoint could not render " + file, failure);
                        } else {
                            LOGGER.log(Level.INFO, "PowerPoint rendered {0} ({1} slides) in the background", new Object[]{file.getName(), n});
                        }
                    });
                    return null;
                }
                long start = System.nanoTime();
                PowerPointService.get().exportPdf(file, pdf).get(EXPORT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                LOGGER.log(Level.INFO, "PowerPoint exported {0} in {1} ms",
                        new Object[]{file.getName(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)});
            }
            LOGGER.log(Level.INFO, "Showing {0} as rendered by PowerPoint", file.getName());
            return new RenderedPresentation(pdf);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "PowerPoint could not render " + file + ", using Quelea's own rendering", ex);
            return null;
        }
    }

    /**
     * @param file the presentation.
     * @return where its PDF is (or would be) cached.
     */
    static File cacheFile(File file) throws IOException {
        File dir = new File(QueleaProperties.get().getQueleaUserHome(), "powerpoint-cache");
        Files.createDirectories(dir.toPath());
        return new File(dir, contentHash(file) + ".pdf");
    }

    /**
     * @param file a file.
     * @return the SHA-1 of its contents, in hex.
     */
    static String contentHash(File file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
        byte[] buffer = new byte[64 * 1024];
        try (InputStream in = Files.newInputStream(file.toPath())) {
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static PresentationSlide[] render(File pdf) throws IOException {
        List<PresentationSlide> ret = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDRectangle box = document.getPage(i).getMediaBox();
                // PDF units are 1/72 inch; render so the slide is HEIGHT pixels high.
                float dpi = HEIGHT * 72f / box.getHeight();
                BufferedImage page = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                WritableImage image = new WritableImage(page.getWidth(), page.getHeight());
                SwingFXUtils.toFXImage(page, image);
                page.flush();
                ret.add(new PresentationSlide(image));
            }
        }
        if (ret.isEmpty()) {
            throw new IOException("No pages in " + pdf);
        }
        return ret.toArray(new PresentationSlide[0]);
    }

    @Override
    public PresentationSlide getSlide(int index) {
        return slides[index];
    }

    @Override
    public PresentationSlide[] getSlides() {
        return slides;
    }
}
