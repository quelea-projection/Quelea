package org.quelea.windows.main;

import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.quelea.data.displayable.Displayable;

public abstract class DisplayableDrawer {

    /**
     * The style class for the grey area behind pictures in the operator's
     * previews.
     */
    public static final String PREVIEW_BACKDROP_STYLE_CLASS = "preview-backdrop";

    private DisplayCanvas canvas;

    public DisplayableDrawer() {
        this.canvas = null;
    }

    public void setCanvas(DisplayCanvas canvas) {
        this.canvas = canvas;
    }

    public DisplayCanvas getCanvas() {
        return canvas;
    }

    public abstract void draw(Displayable displayable);

    public abstract void clear();

    public abstract void requestFocus();

    /**
     * Determine whether this drawer draws onto one of the operator's previews,
     * rather than onto the projector or the stage view.
     *
     * @return true if the canvas is neither the projection nor the stage
     * window's canvas.
     */
    protected boolean isPreviewCanvas() {
        QueleaApp app = QueleaApp.get();
        DisplayStage projection = app.getProjectionWindow();
        DisplayStage stage = app.getStageWindow();
        return (projection == null || getCanvas() != projection.getCanvas())
                && (stage == null || getCanvas() != stage.getCanvas());
    }

    /**
     * Give the box a picture (an image, a PDF page or a presentation slide) is
     * centred in a background for the parts the picture doesn't cover. The
     * display canvas and its window are transparent, so without one those
     * parts would show whatever is behind the projector window. On the
     * projector and stage view they're black; in the operator's previews they
     * use the preview backdrop. The box is removed when the next item is
     * drawn, so the background never stays behind e.g. a song.
     *
     * @param box the box filling the canvas that the picture is centred in.
     * @param preview true if the box is on one of the operator's previews.
     */
    public static void setPictureBackdrop(Region box, boolean preview) {
        if (preview) {
            box.setBackground(null);
            if (!box.getStyleClass().contains(PREVIEW_BACKDROP_STYLE_CLASS)) {
                box.getStyleClass().add(PREVIEW_BACKDROP_STYLE_CLASS);
            }
        } else {
            box.getStyleClass().remove(PREVIEW_BACKDROP_STYLE_CLASS);
            box.setBackground(Background.fill(Color.BLACK));
        }
    }
}
