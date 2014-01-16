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
package org.quelea.windows.presentation;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.quelea.data.powerpoint.PresentationSlide;

/**
 * A JList for specifically displaying presentation slides.
 * <p/>
 * @author Michael
 */
public class PresentationPreview extends ScrollPane {

    private FlowPane flow;
    private List<SlideThumbnail> thumbnails = new ArrayList<>();
    private PresentationSlide[] slides;
    private PresentationSlide selectedSlide;
    private int selectedIndex = -1;
    private List<SlideChangedListener> listeners = new ArrayList<>();

    /**
     * Create a new presentation list.
     */
    public PresentationPreview() {
        setStyle("-fx-focus-color: transparent;-fx-background-color:linear-gradient(to bottom right, #c0c0c0, #e8e8e8);");
        flow = new FlowPane(20, 20);
        flow.setAlignment(Pos.CENTER);
        viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
                flow.setPrefWidth(newBounds.getWidth());
            }
        });
        setContent(flow);
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                int selected = -1;
                for(int i = 0; i < thumbnails.size(); i++) {
                    SlideThumbnail thumbnail = thumbnails.get(i);
                    Bounds bounds = new BoundingBox(thumbnail.getLayoutX(), thumbnail.getLayoutY() + getScrollOffset(), thumbnail.getWidth(), thumbnail.getHeight());
                    if(bounds.contains(t.getX(), t.getY())) {
                        selected = i;
                    }
                }
                if(selected != -1) {
                    select(selected + 1, true);
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.DOWN || t.getCode() == KeyCode.RIGHT) {
                    if(selectedIndex > 0 && selectedIndex <= slides.length - 1) {
                        select(selectedIndex + 1);
                    }
                }
                if(t.getCode() == KeyCode.UP || t.getCode() == KeyCode.LEFT) {
                    if(selectedIndex >= 2) {
                        select(selectedIndex - 1);
                    }
                }
            }
        });
        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean focused) {
                for(SlideThumbnail slide : thumbnails) {
                    slide.setActive(focused);
                }
            }
        });
    }

    public void addSlideChangedListener(SlideChangedListener listener) {
        listeners.add(listener);
    }

    private void fireSlideChangedListeners() {
        for(SlideChangedListener listener : listeners) {
            listener.slideChanged(selectedSlide);
        }
    }

    /**
     * Clear all current slides and set the slides in the list.
     * <p/>
     * @param slides the slides to put in the list.
     */
    public void setSlides(PresentationSlide[] slides) {
        if(this.slides == slides) {
            return;
        }
        this.slides = slides;
        flow.getChildren().clear();
        thumbnails.clear();
        for(int i = 0; i < slides.length; i++) {
            SlideThumbnail thumbnail = new SlideThumbnail(slides[i], i + 1);
            if(i == 0) {
                thumbnail.setSelected(true);
            }
            thumbnails.add(thumbnail);
            flow.getChildren().add(thumbnail);
            thumbnail.setActive(focusedProperty().get());
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public PresentationSlide getSelectedSlide() {
        return selectedSlide;
    }

    public void advanceSlide() {
        int nextSelection = getSelectedIndex() + 1;
        if(nextSelection < 1) {
            nextSelection = 1;
        }
        if(nextSelection > size()) {
            nextSelection = 1;
        }
        select(nextSelection);
    }

    public int size() {
        return thumbnails.size();
    }

    public void select(int index) {
        select(index, true);
    }

    public void select(int index, boolean fireUpdate) {
        if(selectedIndex == index) {
            return;
        }
        for(int i = 0; i < thumbnails.size(); i++) {
            SlideThumbnail thumbnail = thumbnails.get(i);
            boolean selected = thumbnail.getNum() == index;
            thumbnail.setSelected(selected);
            if(selected) {
                selectedIndex = index;
                if(selectedIndex >= 1) {
                    selectedSlide = slides[i];
                }
                else {
                    selectedSlide = null;
                }
            }
            ensureVisible(selectedIndex);
        }
        if(fireUpdate) {
            fireSlideChangedListeners();
        }
    }

    private void ensureVisible(int index) {
        if(index < 1) {
            return;
        }
        Bounds slideBounds = thumbnails.get(index - 1).getBoundsInParent();
        Bounds scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
        if(!scrollBounds.contains(slideBounds)) {
            while(slideBounds.getMinY() < scrollBounds.getMinY() && getVvalue() > getVmin()) {
                slideBounds = thumbnails.get(index - 1).getBoundsInParent();
                scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
                setVvalue(getVvalue() - 0.01);
            }
            while(slideBounds.getMaxY() > scrollBounds.getMaxY() && getVvalue() < getVmax()) {
                slideBounds = thumbnails.get(index - 1).getBoundsInParent();
                scrollBounds = new BoundingBox(0, getScrollFraction() * (flow.getHeight() - getHeight()), getWidth(), getHeight());
                setVvalue(getVvalue() + 0.01);
            }
        }
    }

    private double getScrollOffset() {
        return getScrollFraction() * (getHeight() - flow.getHeight());
    }

    private double getScrollFraction() {
        double vMin = getVmin();
        double vMax = getVmax();
        double vFrac = (getVvalue() - vMin) * (vMax - vMin);
        return vFrac;
    }

    public void clear() {
        thumbnails.clear();
        flow.getChildren().clear();
        this.slides = null;
        selectedIndex = -1;
        selectedSlide = null;
    }
}
