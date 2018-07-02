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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.notice.NoticeDrawer;
import org.quelea.services.notice.NoticeOverlay;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.LogoImage;

/**
 * The canvas where the lyrics / images / media are drawn.
 * <p/>
 * @author Michael
 */
public class DisplayCanvas extends StackPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean cleared;
    private boolean blacked;
    private final NoticeDrawer noticeDrawer;
    private final boolean stageView;
    private Node background;
    private final LogoImage logoImage;
    private final Rectangle black = new Rectangle();
    private final Node noticeOverlay;
    private Displayable currentDisplayable;
    private final CanvasUpdater updater;
    private Priority dravingPriority = Priority.LOW;
    private final boolean playVideo;

    public enum Type {

        STAGE,
        PREVIEW,
        FULLSCREEN
    };

    public enum Priority {

        HIGH(0),
        MID(1),
        LOW(2);
        private final int priority;

        private Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    };

    /**
     * Create a new canvas where the lyrics should be displayed.
     * <p/>
     * @param showBorder true if the border should be shown around any text
     * (only if the options say so) false otherwise.
     * @param stageView true if this canvas is on a stage view, false if it's on
     * a main projection view.
     * @param playVideo true if this canvas should play video. (At present, only
     * one canvas can do this due to VLC limitations.)
     * @param updater the updater that will update this canvas.
     * @param dravingPriority the drawing priority of this canvas when it's
     * updating.
     */
    public DisplayCanvas(boolean showBorder, boolean stageView, boolean playVideo, final CanvasUpdater updater, Priority dravingPriority) {
        setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        this.playVideo = playVideo;
        this.stageView = stageView;
        this.dravingPriority = dravingPriority;
        setMinHeight(0);
        setMinWidth(0);
        background = getNewImageView();
        this.updater = updater;
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                updateCanvas(updater);
            }
        });
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                updateCanvas(updater);
            }
        });
        getChildren().add(background);

        black.setFill(Color.BLACK);
        black.widthProperty().bind(this.widthProperty());
        black.heightProperty().bind(this.heightProperty());
        black.setOpacity(0);
        getChildren().add(black);

        logoImage = new LogoImage(stageView);

        logoImage.minWidthProperty().bind(widthProperty());
        logoImage.maxWidthProperty().bind(widthProperty());
        logoImage.minHeightProperty().bind(heightProperty());
        logoImage.maxHeightProperty().bind(heightProperty());
        logoImage.setOpacity(0);
        getChildren().add(logoImage);

        if (stageView) {
            black.setFill(QueleaProperties.get().getStageBackgroundColor());
        }

        noticeDrawer = new NoticeDrawer(this);
        noticeOverlay = noticeDrawer.getOverlay();
        final Runnable[] r = new Runnable[1];
        final ListChangeListener<Node> listener = new ListChangeListener<Node>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change) {
                while (change.next()) {
                    if (!change.wasRemoved()) {
                        try {
                            /**
                             * Platform.runLater() is necessary here to avoid
                             * exceptions on some implementations, including
                             * JFX8 at the time of writing. You can't modify a
                             * list inside its listener, so the
                             * Platform.runLater() delays it until after the
                             * listener is complete (this is necessary even
                             * though we're on the EDT.)
                             * <p>
                             * https://javafx-jira.kenai.com/browse/RT-35275
                             */
                            if (r[0] != null) {
                                Platform.runLater(r[0]);
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Can't move notice overlay to front", ex);
                        }
                    }
                }
            }
        };
        getChildren().addListener(listener);
        r[0] = new Runnable() {
            @Override
            public void run() {
                getChildren().removeListener(listener);
                pushLogoNoticeToFront();
                getChildren().addListener(listener);
            }
        };
        getChildren().add(noticeOverlay);
    }

    public final boolean getPlayVideo() {
        return playVideo;
    }

    /**
     * If the notice overlay has been removed from this canvas, add it. This
     * shouldn't ever be the case, but means the notices will still work if it
     * has been removed somehow. Otherwise, notices would require a restart to
     * work.
     */
    public void ensureNoticesVisible() {
        if (!getChildren().contains(noticeOverlay)) {
            LOGGER.log(Level.WARNING, "Notice overlay was removed");
            getChildren().add(noticeOverlay);
        }
    }

    public void clearCurrentDisplayable() {
        setCurrentDisplayable(null);
    }

    public void clearNonPermanentChildren() {
        ObservableList<Node> list = FXCollections.observableArrayList(getChildren());
        for (Node node : list) {
            if (!(node instanceof NoticeOverlay) && node != logoImage && node != black) {
                getChildren().remove(node);
            }
        }
    }

    /**
     * @return the currentDisplayable
     */
    public Displayable getCurrentDisplayable() {
        return currentDisplayable;
    }

    /**
     * @param currentDisplayable the currentDisplayable to set
     */
    public void setCurrentDisplayable(Displayable currentDisplayable) {
        this.currentDisplayable = currentDisplayable;
    }

    /**
     * @return the dravingPriority
     */
    public Priority getDravingPriority() {
        return dravingPriority;
    }

    public interface CanvasUpdater {

        void updateCallback();
    }

    private void updateCanvas(final CanvasUpdater updater) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (isVisibleInScene() && updater != null) {
                    updater.updateCallback();
                }
            }
        });
    }

    private boolean isVisibleInScene() {
        Node parent = DisplayCanvas.this;
        boolean visible = isVisible();
        if (!visible) {
            return visible;
        }
        while ((parent = parent.getParent()) != null) {
            if (!parent.isVisible()) {
                visible = false;
                break;
            }
        }
        return visible;
    }

    /**
     * @return the background
     */
    public Node getCanvasBackground() {
        return background;
    }

    /**
     * @param background the background to set
     */
    public void setCanvasBackground(Node background) {
        this.background = background;
    }

    public final ImageView getNewImageView() {
        ImageView ret = new ImageView(Utils.getImageFromColour(Color.BLACK));
        ret.setFitHeight(getHeight());
        ret.setFitWidth(getWidth());
        StackPane.setAlignment(ret, Pos.CENTER);
        return ret;
    }

    /**
     * Determine if this canvas is part of a stage view.
     * <p/>
     * @return true if its a stage view, false otherwise.
     */
    public boolean isStageView() {
        return stageView;
    }

    public void update() {
        if (this.updater != null) {
            updateCanvas(this.updater);
        }
    }

    /**
     * Toggle the clearing of this canvas - if cleared, still leave the
     * background image in place but remove all the text. Otherwise display as
     * normal.
     * <p>
     * @param cleared cleared if the text on this canvas should be cleared,
     * false otherwise.
     */
    public void setCleared(boolean cleared) {
        if (this.cleared == cleared) {
            return;
        }
        this.cleared = cleared;
        if (this.updater != null) {
            updateCanvas(this.updater);
        }
    }

    /**
     * Determine whether this canvas is cleared.
     * <p/>
     * @return true if the canvas is cleared, false otherwise.
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Toggle the blacking of this canvas - if blacked, remove the text and
     * background image (if any) just displaying a black screen. Otherwise
     * display as normal.
     * <p>
     * @param blacked true if this canvas should be set blacked, false
     * otherwise.
     */
    public void setBlacked(boolean blacked) {
        this.blacked = blacked;
        if (blacked) {
            black.toFront();
            FadeTransition ft = new FadeTransition(Duration.millis(QueleaProperties.get().getBlackFadeDuration()), black);
            ft.setToValue(1);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(QueleaProperties.get().getBlackFadeDuration()), black);
            ft.setToValue(0);
            ft.play();
        }
    }

    /**
     * Determine whether this canvas is blacked.
     * <p/>
     * @return true if the canvas is blacked, false otherwise.
     */
    public boolean isBlacked() {
        return blacked;
    }

    /**
     * Get the notice drawer, used for drawing notices onto this lyrics canvas.
     * <p/>
     * @return the notice drawer.
     */
    public NoticeDrawer getNoticeDrawer() {
        return noticeDrawer;
    }

    /**
     * This method fades the logo in and out of view when the logo display
     * button is used.
     * <p>
     *
     * @param selected true to display the logo screen, false to remove it.
     */
    public void setLogoDisplaying(boolean selected) {
        if (selected) {
            logoImage.toFront();
            FadeTransition ft = new FadeTransition(Duration.millis(QueleaProperties.get().getLogoFadeDuration()), logoImage);
            ft.setToValue(1);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(QueleaProperties.get().getLogoFadeDuration()), logoImage);
            ft.setToValue(0);
            ft.play();
        }
    }

    public void pushLogoNoticeToFront() {
        black.toFront();
        logoImage.toFront();
        noticeOverlay.toFront();
    }

    /**
     * Update logo removes, reloads, and re-adds the logo image from the quelea
     * properties file. This method is triggered when a successful right-click
     * is completed on the lyric panel logo button.
     */
    public void updateLogo() {
        logoImage.refresh();
    }

    public void makeClickable(boolean clickable) {
        // Make webview clickable
        if (black != null) {
            black.setMouseTransparent(clickable);
        }
        if (logoImage != null) {
            logoImage.setMouseTransparent(clickable);
        }
        if (background != null) {
            background.setMouseTransparent(clickable);
        }
        if (noticeOverlay != null) {
            noticeOverlay.setMouseTransparent(clickable);
        }
    }
}
