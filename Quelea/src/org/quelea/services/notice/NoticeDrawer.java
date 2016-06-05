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

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.QueleaApp;

/**
 * Responsible for drawing the notice animation on a particular canvas.
 * <p/>
 * @author Michael
 */
public class NoticeDrawer {
    
    public enum NoticePosition {
        
        TOP("top"), BOTTOM("bottom");
    
        private String text;
        
        private NoticePosition(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
    }

    private static final double BACKGROUND_OPACITY = 0.6;
    private static final double BACKGROUND_FADE_DURATION = 0.5;
    private NoticeOverlay overlay;
    private DisplayCanvas canvas;
    private List<Notice> notices;
    private List<NoticesChangedListener> listeners;
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
        listeners = new ArrayList<>();
        overlay = new NoticeOverlay();
        playing = false;
    }

    public NoticeOverlay getOverlay() {
        return overlay;
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
        if (!playing) {
            playing = true;
            final List<Notice> oldNotices = new ArrayList<>();
            if (notices.isEmpty()) {
                return;
            }
            oldNotices.add(notices.get(0));
            final HBox textGroup = new HBox(notices.get(0).getFont().getFont().getSize() * 2);
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < oldNotices.size(); i++) {
                Notice notice = oldNotices.get(i);
                builder.append(notice.getText());
                Text noticeText = new Text(notice.getText());
                noticeText.setFill(notice.getColor().getColor());
                noticeText.setFont(notice.getFont().getFont());
                textGroup.getChildren().add(noticeText);
            }
            FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(oldNotices.get(0).getFont().getFont());
            double displayWidth = QueleaApp.get().getProjectionWindow().getWidth();
            double width = metrics.computeStringWidth(builder.toString()) + textGroup.getSpacing() * (notices.size() - 1);
            if (QueleaProperties.get().getNoticePosition()==NoticePosition.TOP) {
                StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
                overlay.setAlignment(Pos.BOTTOM_CENTER);
                textGroup.setAlignment(Pos.BOTTOM_LEFT);
            } else {
                StackPane.setAlignment(overlay, Pos.TOP_CENTER);
                overlay.setAlignment(Pos.TOP_CENTER);
                textGroup.setAlignment(Pos.TOP_LEFT);
            }
            if (!overlay.getChildren().contains(backing)) {
                backing = new Rectangle(displayWidth, metrics.getLineHeight()+5, QueleaProperties.get().getNoticeBackgroundColour());
                backing.setOpacity(0);
                overlay.getChildren().add(backing);
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
            overlay.getChildren().add(textGroup);
            Timeline timeline = new Timeline(25);
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(textGroup.translateXProperty(), textGroup.getTranslateX())));
            double baseDuration = QueleaProperties.get().getNoticeSpeed();
            if (excessWidth <= 0) {
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration), new KeyValue(textGroup.translateXProperty(), 0)));
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration + baseDuration / (displayWidth / width)), new KeyValue(textGroup.translateXProperty(), stopPoint)));
            } else {
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration), new KeyValue(textGroup.translateXProperty(), excessWidth / 2)));
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(baseDuration + baseDuration / (displayWidth / width)), new KeyValue(textGroup.translateXProperty(), stopPoint)));
            }
            timeline.play();
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    playing = false;
                    overlay.getChildren().remove(textGroup);
                    for (int i = notices.size() - 1; i >= 0; i--) {
                        Notice notice = notices.get(i);
                        if (oldNotices.contains(notice)) {
                            notice.decrementTimes();
                        }
                        if (notice.getTimes() == 0) {
                            notices.remove(notice);
                        }
                    }
                    QueleaApp.get().getMainWindow().getNoticeDialog().noticesUpdated();
                    if (!notices.isEmpty()) {
                        playNotices();
                    } else {
                        FadeTransition fadeTrans = new FadeTransition(Duration.seconds(BACKGROUND_FADE_DURATION), backing);
                        fadeTrans.setFromValue(BACKGROUND_OPACITY);
                        fadeTrans.setToValue(0);
                        fadeTrans.play();
                        fadeTrans.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                overlay.getChildren().remove(backing);
                            }
                        });
                    }
                }
            });
        }
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

    /**
     * Add a notice changed listener to this drawer.
     * <p/>
     * @param listener the listener to add.
     */
    public synchronized void addNoticeChangedListener(NoticesChangedListener listener) {
        listeners.add(listener);
    }

}
