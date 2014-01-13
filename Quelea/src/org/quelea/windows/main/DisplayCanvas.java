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
import org.quelea.data.displayable.Displayable;
import org.quelea.services.notice.NoticeDrawer;
import org.quelea.services.notice.NoticeOverlay;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * The canvas where the lyrics / images / media are drawn.
 * <p/>
 * @author Michael
 */
public class DisplayCanvas extends StackPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final ImageView BLACK_IMAGE = new ImageView(Utils.getImageFromColour(Color.BLACK));
    private boolean cleared;
    private boolean blacked;
    private NoticeDrawer noticeDrawer;
    private boolean stageView;
    private Node background;
    private Node currentBackground;
    private Node noticeOverlay;
    private Displayable currentDisplayable;
    private final CanvasUpdater updater;
    private Priority dravingPriority = Priority.LOW;
    private Type type = Type.PREVIEW;
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
        private int priority;

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
        noticeDrawer = new NoticeDrawer(this);
        noticeOverlay = noticeDrawer.getOverlay();
        getChildren().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change) {
                while(change.next()) {
                    if(!change.wasRemoved()) {
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
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    noticeOverlay.toFront();
                                }
                            });
                        }
                        catch(Exception ex) {
                            LOGGER.log(Level.WARNING, "Can't move notice overlay to front", ex);
                        }
                    }
                }
            }
        });
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
        if(!getChildren().contains(noticeOverlay)) {
            LOGGER.log(Level.WARNING, "Notice overlay was removed");
            getChildren().add(noticeOverlay);
        }
    }

    public void clearCurrentDisplayable() {
        setCurrentDisplayable(null);
    }

    public void clearApartFromNotice() {
        ObservableList<Node> list = FXCollections.observableArrayList(getChildren());
        for(Node node : list) {
            if(!(node instanceof NoticeOverlay)) {
                getChildren().remove(node);
            }
        }
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
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
                if(isVisibleInScene() && updater != null) {
                    updater.updateCallback();
                }
            }
        });
    }

    private boolean isVisibleInScene() {
        Node parent = DisplayCanvas.this;
        boolean visible = isVisible();
        if(!visible) {
            return visible;
        }
        while((parent = parent.getParent()) != null) {
            if(!parent.isVisible()) {
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
        if(this.updater != null) {
            updateCanvas(this.updater);
        }
    }

    /**
     * Toggle the clearing of this canvas - if cleared, still leave the
     * background image in place but remove all the text. Otherwise display as
     * normal.
     */
    public void setCleared(boolean cleared) {
        if(this.cleared == cleared) {
            return;
        }
        this.cleared = cleared;
        if(this.updater != null) {
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
     */
    public void setBlacked(boolean blacked) {
        if(this.blacked == blacked) {
            return;
        }
        this.blacked = blacked;
        if(blacked) {
            currentBackground = getCanvasBackground();
            clearApartFromNotice();
            setCanvasBackground(BLACK_IMAGE);
        }
        else {
            setCanvasBackground(currentBackground);
            Node imageView = null;
            if(currentBackground == null) {
                for(Node node : getChildren()) {
                    if(node instanceof ImageView) {
                        imageView = node;
                    }
                }
                if(imageView != null) {
                    getChildren().remove(imageView);
                }
            }
        }
        if(this.updater != null) {
            updateCanvas(this.updater);
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
}
