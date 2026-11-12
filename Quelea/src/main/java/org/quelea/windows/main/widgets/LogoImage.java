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
package org.quelea.windows.main.widgets;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.quelea.data.logo.LogoLayout;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.ImageManager;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SvgRasterizer;
import org.quelea.services.utils.Utils;
import org.quelea.windows.video.VidLogoDisplay;
import org.quelea.windows.video.VideoStills;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The logo image - on its own pane with a black background, so nothing behind
 * it shows through around the logo (or through transparent parts of it).
 * <p>
 * The logo is sized and placed according to the logo settings (see
 * {@link LogoSettings}). SVG logos are rendered at the exact pixel size they
 * are shown at, so they stay sharp on any screen.
 *
 * @author Michael
 */
public class LogoImage extends Pane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    /**
     * One background thread renders the SVG logos of all canvases, one at a
     * time.
     */
    private static final ExecutorService SVG_RENDERER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "logo-svg-renderer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Every logo image on a display canvas, so a changed logo (from wherever
     * it's changed) reaches all of them, not just the canvases of the panel
     * that happens to be showing. Weak, so canvases that are thrown away
     * aren't kept alive.
     */
    private static final Set<LogoImage> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    private final ImageView logoImage;
    private final boolean stageView;
    /**
     * True for a preview (e.g. in the options), which shows the logo it's
     * given rather than the saved one.
     */
    private final boolean preview;
    private LogoSettings settings = LogoSettings.DEFAULT;
    /**
     * The URI of the SVG logo being shown, or null if the logo isn't an SVG.
     */
    private String svgUri;
    private Dimension2D svgSize;
    private int svgPixelWidth;
    private int svgPixelHeight;
    /**
     * Bumped whenever the SVG logo needs rendering again, so renders that have
     * been overtaken (the canvas was resized again, or the logo changed) are
     * skipped or thrown away. Also used for video stills in previews.
     */
    private final AtomicLong svgGeneration = new AtomicLong();

    /**
     * Create a new logo image
     * <p>
     * @param stageView true if the logo image is being constructed on a stage
     * view, else false.
     */
    public LogoImage(boolean stageView) {
        this(stageView, false);
        synchronized (INSTANCES) {
            INSTANCES.add(this);
        }
        refresh();
    }

    private LogoImage(boolean stageView, boolean preview) {
        this.stageView = stageView;
        this.preview = preview;
        logoImage = new ImageView();
        logoImage.setSmooth(true);
        setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        // A new image (e.g. the first frame of a video logo) may have a
        // different shape to the last one
        logoImage.imageProperty().addListener((obs, oldImage, newImage) -> {
            if (svgUri == null && !sameSize(oldImage, newImage)) {
                requestLayout();
            }
        });
        // Most canvases never show the logo, so an SVG logo is only rendered
        // once it starts to fade in
        opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
            if (svgUri != null && oldOpacity.doubleValue() <= 0 && newOpacity.doubleValue() > 0) {
                requestLayout();
            }
        });
        watchRenderScale();
        getChildren().add(logoImage);
    }

    /**
     * Create a logo image for previewing logo settings before they're saved.
     * It shows whatever is passed to {@link #showLogo(String, LogoSettings)},
     * and isn't changed by {@link #refreshAll()}. A video logo is previewed as
     * a still frame, so the real logo video isn't switched over.
     *
     * @return the preview logo image, empty (black) to start with.
     */
    public static LogoImage createPreview() {
        return new LogoImage(false, true);
    }

    /**
     * Show the current logo (and its size and position) from the properties
     * on every logo image of every display canvas. Must be called on the FX
     * thread.
     */
    public static void refreshAll() {
        List<LogoImage> all;
        synchronized (INSTANCES) {
            all = new ArrayList<>(INSTANCES);
        }
        for (LogoImage logo : all) {
            logo.refresh();
        }
    }

    /**
     * Update this logo image with the correct one (and the correct size and
     * position) from the properties file. Does nothing for a preview.
     */
    public final void refresh() {
        if (preview) {
            return;
        }
        if (stageView) {
            resetContent(QueleaProperties.get().getLogoSettings());
            logoImage.imageProperty().unbind();
            logoImage.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            requestLayout();
        } else {
            showLogo(QueleaProperties.get().getLogoImageURI(), QueleaProperties.get().getLogoSettings());
        }
    }

    /**
     * Show the given logo, sized and placed by the given settings.
     *
     * @param uri      the URI of the logo (image, SVG or video), or null for
     *                 none (just black).
     * @param settings how to size and place it.
     */
    public void showLogo(String uri, LogoSettings settings) {
        resetContent(settings);
        logoImage.imageProperty().unbind();
        if (uri == null) {
            logoImage.setImage(null);
        } else if (isVid(uri)) {
            if (preview) {
                logoImage.setImage(null);
                showVideoStill(uri);
            } else {
                VidLogoDisplay.INSTANCE.setURI(URI.create(uri));
                logoImage.imageProperty().bind(VidLogoDisplay.INSTANCE.imageProperty());
            }
        } else if (SvgRasterizer.isSvg(uri)) {
            logoImage.setImage(null);
            try {
                svgSize = SvgRasterizer.getIntrinsicSize(uri);
                svgUri = uri;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't load SVG logo " + uri, ex);
            }
        } else {
            logoImage.setImage(ImageManager.INSTANCE.getImage(uri));
        }
        requestLayout();
    }

    private void resetContent(LogoSettings settings) {
        this.settings = settings == null ? LogoSettings.DEFAULT : settings;
        svgUri = null;
        svgSize = null;
        svgPixelWidth = 0;
        svgPixelHeight = 0;
        svgGeneration.incrementAndGet();
        logoImage.setPreserveRatio(false);
    }

    private void showVideoStill(String uri) {
        final long generation = svgGeneration.get();
        File file;
        try {
            file = new File(URI.create(uri));
        } catch (IllegalArgumentException ex) {
            return;
        }
        Image cached = VideoStills.getCached(file);
        if (cached != null) {
            logoImage.setImage(cached);
            return;
        }
        VideoStills.load(file).thenAccept(still -> Platform.runLater(() -> {
            if (svgGeneration.get() == generation && still.isPresent()) {
                logoImage.setImage(still.get());
            }
        }));
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        if (stageView) {
            // Stage view just shows its background colour
            showAt(new Rectangle2D(0, 0, width, height));
            return;
        }
        double contentWidth = 0;
        double contentHeight = 0;
        if (svgUri != null) {
            contentWidth = svgSize.getWidth();
            contentHeight = svgSize.getHeight();
        } else if (logoImage.getImage() != null) {
            contentWidth = logoImage.getImage().getWidth();
            contentHeight = logoImage.getImage().getHeight();
        }
        Rectangle2D bounds = LogoLayout.computeBounds(width, height, contentWidth, contentHeight, settings);
        // Whole pixels, so the logo isn't blurred by being drawn between them
        bounds = new Rectangle2D(Math.round(bounds.getMinX()), Math.round(bounds.getMinY()),
                Math.round(bounds.getWidth()), Math.round(bounds.getHeight()));
        showAt(bounds);
        if (svgUri != null && getOpacity() > 0) {
            renderSvg(bounds.getWidth(), bounds.getHeight());
        }
    }

    private void showAt(Rectangle2D bounds) {
        boolean visible = bounds.getWidth() > 0 && bounds.getHeight() > 0;
        // (A fit size of 0 would mean "natural size" to the image view)
        logoImage.setVisible(visible);
        if (visible) {
            logoImage.setFitWidth(bounds.getWidth());
            logoImage.setFitHeight(bounds.getHeight());
            logoImage.relocate(bounds.getMinX(), bounds.getMinY());
        }
    }

    /**
     * Render the SVG logo (in the background) at the size it is shown at, in
     * real screen pixels, unless it is already that size.
     */
    private void renderSvg(double width, double height) {
        Window window = getScene() == null ? null : getScene().getWindow();
        double scaleX = window == null ? 1 : window.getRenderScaleX();
        double scaleY = window == null ? 1 : window.getRenderScaleY();
        int pixelWidth = (int) Math.min(SvgRasterizer.MAX_SIZE, Math.round(width * scaleX));
        int pixelHeight = (int) Math.min(SvgRasterizer.MAX_SIZE, Math.round(height * scaleY));
        if (pixelWidth <= 0 || pixelHeight <= 0
                || (pixelWidth == svgPixelWidth && pixelHeight == svgPixelHeight)) {
            return;
        }
        svgPixelWidth = pixelWidth;
        svgPixelHeight = pixelHeight;
        final long generation = svgGeneration.incrementAndGet();
        final String uri = svgUri;
        SVG_RENDERER.execute(() -> {
            if (svgGeneration.get() != generation) {
                return; // Overtaken by a newer size or logo, don't bother
            }
            try {
                Image image = SvgRasterizer.render(uri, pixelWidth, pixelHeight);
                Platform.runLater(() -> {
                    if (svgGeneration.get() == generation) {
                        logoImage.setImage(image);
                    }
                });
            } catch (IOException | RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Couldn't render SVG logo " + uri, ex);
            }
        });
    }

    /**
     * Lay out again (and so re-render an SVG logo) if the window moves to a
     * screen with a different scale (HiDPI).
     */
    private void watchRenderScale() {
        ChangeListener<Number> scaleListener = (obs, oldScale, newScale) -> requestLayout();
        ChangeListener<Window> windowListener = (obs, oldWindow, newWindow) -> {
            if (oldWindow != null) {
                oldWindow.renderScaleXProperty().removeListener(scaleListener);
                oldWindow.renderScaleYProperty().removeListener(scaleListener);
            }
            if (newWindow != null) {
                newWindow.renderScaleXProperty().addListener(scaleListener);
                newWindow.renderScaleYProperty().addListener(scaleListener);
            }
            requestLayout();
        };
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.windowProperty().removeListener(windowListener);
                windowListener.changed(null, oldScene.getWindow(), null);
            }
            if (newScene != null) {
                newScene.windowProperty().addListener(windowListener);
                windowListener.changed(null, null, newScene.getWindow());
            }
        });
    }

    private static boolean sameSize(Image a, Image b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight();
    }

    /**
     * @return where the logo is currently drawn, relative to this pane (for
     * testing).
     */
    Rectangle2D getLogoBounds() {
        if (!logoImage.isVisible()) {
            return new Rectangle2D(0, 0, 0, 0);
        }
        return new Rectangle2D(logoImage.getLayoutX(), logoImage.getLayoutY(),
                logoImage.getFitWidth(), logoImage.getFitHeight());
    }

    /**
     * @return the image currently shown (for testing).
     */
    Image getShownImage() {
        return logoImage.getImage();
    }

    private boolean isVid(String uri) {
        return Utils.getVideoExtensions().stream().anyMatch(uri::endsWith);
    }
}
