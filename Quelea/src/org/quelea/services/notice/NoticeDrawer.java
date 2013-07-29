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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.QueleaApp;

/**
 * Responsible for drawing the notice animation on a particular canvas.
 * <p/>
 * @author Michael
 */
public class NoticeDrawer {

    private static final double BACKGROUND_OPACITY = 0.6;
    private static final double BACKGROUND_FADE_DURATION = 0.5;
    private static final double TEXT_SCROLL_DURATION = 10;
    private NoticeOverlay overlay;
    private DisplayCanvas canvas;
    private List<Notice> notices;
    private List<NoticesChangedListener> listeners;
    private boolean playing;
    private Rectangle backing;
    private Font noticeFont;

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
        noticeFont = Font.font("Arial", FontPosture.ITALIC, 50);
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
        if(!playing) {
            FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(noticeFont);
            if(!overlay.getChildren().contains(backing)) {
                backing = new Rectangle(canvas.getWidth(), metrics.getLineHeight(), Color.BROWN);
                backing.setOpacity(0);
                overlay.getChildren().add(backing);
                FadeTransition fadeTrans = new FadeTransition(Duration.seconds(BACKGROUND_FADE_DURATION), backing);
                fadeTrans.setFromValue(0);
                fadeTrans.setToValue(BACKGROUND_OPACITY);
                fadeTrans.play();
            }
            playing = true;
            final List<Notice> oldNotices = new ArrayList<>(notices);
            final HBox textGroup = new HBox(noticeFont.getSize() * 2);
            final StringBuilder builder = new StringBuilder();
            textGroup.setAlignment(Pos.BOTTOM_LEFT);
            for(int i = 0; i < notices.size(); i++) {
                Notice notice = notices.get(i);
                builder.append(notice.getText());
                Text noticeText = new Text(notice.getText());
                if(i % 2 == 0) {
                    noticeText.setFill(Color.WHITE);
                }
                else {
                    noticeText.setFill(Color.BLANCHEDALMOND);
                }
                noticeText.setFont(noticeFont);
                textGroup.getChildren().add(noticeText);
            }
            double width = metrics.computeStringWidth(builder.toString()) + textGroup.getSpacing() * (notices.size() - 1);
            textGroup.setTranslateX(canvas.getWidth());
            overlay.getChildren().add(textGroup);
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(textGroup.translateXProperty(), textGroup.getTranslateX())));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(TEXT_SCROLL_DURATION), new KeyValue(textGroup.translateXProperty(), -width)));
            timeline.play();
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    playing = false;
                    overlay.getChildren().remove(textGroup);
                    for(int i = notices.size() - 1; i >= 0; i--) {
                        Notice notice = notices.get(i);
                        if(oldNotices.contains(notice)) {
                            notice.decrementTimes();
                        }
                        if(notice.getTimes() == 0) {
                            notices.remove(notice);
                        }
                    }
                    QueleaApp.get().getMainWindow().getNoticeDialog().noticesUpdated();
                    if(!notices.isEmpty()) {
                        playNotices();
                    }
                    else {
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

    public Font getNoticeFont() {
        return noticeFont;
    }

    public void setNoticeFont(Font noticeFont) {
        this.noticeFont = noticeFont;
    }
}
