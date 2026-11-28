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
package org.quelea.services.notice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.utils.FXFontMetrics;

/**
 * Responsible for drawing the notice animation on a particular canvas.
 * <p/>
 * A drawer can also have mirrors: drawers on other canvases (such as the
 * live preview in the main window) that show the same notices at the same
 * time, scaled down to their canvas. Only this drawer keeps track of the
 * notices and how many times they have been shown.
 * <p/>
 * @author Michael
 */
public class NoticeDrawer {

    public enum NoticePosition {

        TOP("top"), BOTTOM("bottom");

        private String text;

        NoticePosition(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private static final double BACKGROUND_OPACITY = 0.6;
    private static final double BACKGROUND_FADE_DURATION = 0.5;
    private final NoticeOverlay overlay;
    private final DisplayCanvas canvas;
    private final List<Notice> notices;
    private final List<NoticeDrawer> mirrors = new ArrayList<>();
    /**
     * Where the notices are drawn: the overlay itself, or for a mirror a
     * layer in it that is the size of the projection window, scaled down to
     * the canvas.
     */
    private StackPane layer;
    private boolean playing;
    private Rectangle backing;

    /**
     * Create a new notice drawer.
     * <p/>
     * @param canvas the canvas to draw on.
     */
    public NoticeDrawer(DisplayCanvas canvas) {
        this.canvas = canvas;
        notices = Collections.synchronizedList(new ArrayList<Notice>());
        overlay = new NoticeOverlay();
        layer = overlay;
        playing = false;
    }

    public NoticeOverlay getOverlay() {
        return overlay;
    }

    /**
     * Show the notices of this drawer on another canvas as well, at the same
     * time and scaled to that canvas.
     * <p/>
     * @param mirror the drawer of the other canvas.
     */
    public void addMirror(NoticeDrawer mirror) {
        if (mirror == null || mirror == this || mirrors.contains(mirror)) {
            return;
        }
        mirror.drawScaled();
        mirrors.add(mirror);
    }

    /**
     * Draw the notices on a layer that is the size of the projection window,
     * scaled to the width of this drawer's canvas, and keep them inside the
     * canvas (they start off the side of the screen.)
     */
    private void drawScaled() {
        if (layer != overlay) {
            return;
        }
        layer = new StackPane();
        layer.setManaged(false);
        Scale scale = new Scale(1, 1, 0, 0);
        layer.getTransforms().add(scale);
        overlay.getChildren().add(layer);
        overlay.setMouseTransparent(true);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(overlay.widthProperty());
        clip.heightProperty().bind(overlay.heightProperty());
        overlay.setClip(clip);
        InvalidationListener fit = o -> {
            double displayWidth = getDisplayWidth();
            double width = canvas.getWidth();
            if (displayWidth <= 0 || width <= 0) {
                return;
            }
            double factor = width / displayWidth;
            scale.setX(factor);
            scale.setY(factor);
            layer.resize(displayWidth, canvas.getHeight() / factor);
        };
        canvas.widthProperty().addListener(fit);
        canvas.heightProperty().addListener(fit);
        DisplayStage projection = QueleaApp.get().getProjectionWindow();
        if (projection != null) {
            projection.widthProperty().addListener(fit);
        }
        fit.invalidated(null);
    }

    private static double getDisplayWidth() {
        DisplayStage projection = QueleaApp.get().getProjectionWindow();
        return projection == null ? 0 : projection.getWidth();
    }

    /**
     * Add a given notice.
     * <p/>
     * @param notice the notice to add.
     */
    public synchronized void addNotice(Notice notice) {
        notices.add(notice);
        playNotices();
    }

    private void playNotices() {
        canvas.ensureNoticesVisible(); //Shouldn't need this, but guards against any cases where the notice overlay may have been removed.
        if (playing || notices.isEmpty()) {
            return;
        }
        playing = true;
        final List<Notice> shown = new ArrayList<>();
        shown.add(notices.get(0));
        int queued = notices.size();
        for (NoticeDrawer mirror : mirrors) {
            mirror.show(shown, queued, null);
        }
        show(shown, queued, () -> {
            playing = false;
            for (int i = notices.size() - 1; i >= 0; i--) {
                Notice notice = notices.get(i);
                if (shown.contains(notice)) {
                    notice.decrementTimes();
                }
                if (notice.getTimes() == 0) {
                    notices.remove(notice);
                }
            }
            if (QueleaApp.get().getMainWindow() != null) {
                QueleaApp.get().getMainWindow().getNoticeDialog().noticesUpdated();
            }
            if (!notices.isEmpty()) {
                playNotices();
            } else {
                hideBacking();
                for (NoticeDrawer mirror : mirrors) {
                    mirror.hideBacking();
                }
            }
        });
    }

    /**
     * Scroll the given notices across this drawer's canvas once.
     * <p/>
     * @param shown the notices to show.
     * @param queued how many notices are waiting to be shown.
     * @param afterwards what to do when they have scrolled past, or null.
     */
    private void show(List<Notice> shown, int queued, Runnable afterwards) {
        canvas.ensureNoticesVisible();
        final HBox textGroup = new HBox(shown.get(0).getFont().getFont().getSize() * 2);
        final StringBuilder builder = new StringBuilder();
        for (Notice notice : shown) {
            builder.append(notice.getText());
            Text noticeText = new Text(notice.getText());
            noticeText.setFill(notice.getColor().getColor());
            noticeText.setFont(notice.getFont().getFont());
            textGroup.getChildren().add(noticeText);
        }
        FXFontMetrics metrics = new FXFontMetrics(shown.get(0).getFont().getFont());
        double displayWidth = getDisplayWidth();
        double width = metrics.computeStringWidth(builder.toString()) + textGroup.getSpacing() * (queued - 1);
        if (QueleaProperties.get().getNoticePosition() == NoticePosition.BOTTOM) {
            StackPane.setAlignment(layer, Pos.BOTTOM_CENTER);
            layer.setAlignment(Pos.BOTTOM_CENTER);
            textGroup.setAlignment(Pos.BOTTOM_LEFT);
        } else {
            StackPane.setAlignment(layer, Pos.TOP_CENTER);
            layer.setAlignment(Pos.TOP_CENTER);
            textGroup.setAlignment(Pos.TOP_LEFT);
        }
        if (!layer.getChildren().contains(backing)) {
            backing = new Rectangle(displayWidth, metrics.getLineHeight() + 5, QueleaProperties.get().getNoticeBackgroundColour());
            backing.setOpacity(0);
            layer.getChildren().add(backing);
            FadeTransition fadeTrans = new FadeTransition(Duration.seconds(BACKGROUND_FADE_DURATION), backing);
            fadeTrans.setFromValue(0);
            fadeTrans.setToValue(BACKGROUND_OPACITY);
            fadeTrans.play();
        }
        double excessWidth = width - displayWidth;
        double stopPoint = -width;
        if (excessWidth <= 0) {
            textGroup.setTranslateX(displayWidth);
        } else {
            stopPoint += excessWidth / 2;
            textGroup.setTranslateX(displayWidth + excessWidth / 2);
        }
        layer.getChildren().add(textGroup);
        Timeline timeline = new Timeline(25);
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(textGroup.translateXProperty(), textGroup.getTranslateX())));
        double baseDuration = 30.0 / QueleaProperties.get().getNoticeSpeed();
        if (excessWidth <= 0) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration), new KeyValue(textGroup.translateXProperty(), 0)));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration + baseDuration / (displayWidth / width)), new KeyValue(textGroup.translateXProperty(), stopPoint)));
        } else {
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration), new KeyValue(textGroup.translateXProperty(), excessWidth / 2)));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration + baseDuration / (displayWidth / width)), new KeyValue(textGroup.translateXProperty(), stopPoint)));
        }
        timeline.setOnFinished(t -> {
            layer.getChildren().remove(textGroup);
            if (afterwards != null) {
                afterwards.run();
            }
        });
        timeline.play();
    }

    /**
     * Fade out the background behind the notices, when there are no more.
     */
    private void hideBacking() {
        final Rectangle oldBacking = backing;
        if (oldBacking == null) {
            return;
        }
        // A notice that comes while this fades out gets a new background
        backing = null;
        FadeTransition fadeTrans = new FadeTransition(Duration.seconds(BACKGROUND_FADE_DURATION), oldBacking);
        fadeTrans.setFromValue(BACKGROUND_OPACITY);
        fadeTrans.setToValue(0);
        fadeTrans.setOnFinished(t -> layer.getChildren().remove(oldBacking));
        fadeTrans.play();
    }

    /**
     * Remove a given notice.
     * <p/>
     * @param notice notice to remove.
     */
    public synchronized void removeNotice(Notice notice) {
        notices.remove(notice);
    }

    /**
     * Get all the notices.
     * <p/>
     * @return a list of all the notices.
     */
    public synchronized List<Notice> getNotices() {
        return new ArrayList<>(notices);
    }
}
