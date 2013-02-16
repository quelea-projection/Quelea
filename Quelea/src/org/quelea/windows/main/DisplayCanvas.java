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

import java.util.Objects;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.notice.NoticeDrawer;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * The canvas where the lyrics / images / media are drawn.
 * <p/>
 * @author Michael
 */
public class DisplayCanvas extends StackPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean cleared;
    private boolean blacked;
    private NoticeDrawer noticeDrawer;
    private boolean stageView;
    private Node background;
    private Displayable currentDisplayable;
    private final CanvasUpdater updater;
    private Priority dravingPriority = Priority.LOW;
    private Type type = Type.PREVIEW;

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
    public DisplayCanvas(boolean showBorder, boolean stageView, final CanvasUpdater updater, Priority dravingPriority) {
        this.stageView = stageView;
        this.dravingPriority = dravingPriority;
        setMinHeight(0);
        setMinWidth(0);
        noticeDrawer = new NoticeDrawer(this);
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
    }

    public void clear() {
        setCurrentDisplayable(null);
    }
    
    public void setType(Type type){
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

        void updateOnSizeChange();
    }

    private void updateCanvas(final CanvasUpdater updater) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (updater != null) {
                    updater.updateOnSizeChange();
                }
            }
        });
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.type);
        return hash;
    }
    
    /**
     * @return the background
     */
    public Node getBackground() {
        return background;
    }

    /**
     * @param background the background to set
     */
    public void setBackground(Node background) {
        this.background = background;
    }

    public ImageView getNewImageView() {
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
        updateCanvas(this.updater);
    }

    /**
     * Toggle the clearing of this canvas - still leave the background image in
     * place but remove all the text.
     */
    public void toggleClear() {
        cleared ^= true; //invert
        updateCanvas(this.updater);
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
     * Toggle the blacking of this canvas - remove the text and background image
     * (if any) just displaying a black screen.
     */
    public void toggleBlack() {
        blacked ^= true; //invert
        updateCanvas(this.updater);
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
