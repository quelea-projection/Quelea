/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.notice;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.TopLyricCanvas;

/**
 * Responsible for drawing the notice animation on a particular canvas.
 * @author Michael
 */
public class NoticeDrawer {

    private static final int DELAY = 40;
    private Font font = new Font("Sans serif", 0, 2);
    private TopLyricCanvas canvas;
    private int boxHeight;
    private int stringPos;
    private List<Notice> notices;
    private List<Notice> inUseNotices;
    private List<NoticesChangedListener> listeners;
    private String noticeString;
    private BufferedImage image;
    private boolean redraw;
    private int noticeWidth;
    private final Object lock = new Object();

    /**
     * Create a new notice drawer.
     * @param canvas the canvas to draw on.
     */
    public NoticeDrawer(TopLyricCanvas canvas) {
        this.canvas = canvas;
        notices = Collections.synchronizedList(new ArrayList<Notice>());
        inUseNotices = Collections.synchronizedList(new ArrayList<Notice>());
        noticeString = "";
        listeners = new ArrayList<>();
        start();
    }

    /**
     * Get the image used for this notice.
     * @return the notice image.
     */
    public BufferedImage getNoticeImage() {
        if (boxHeight == 0) {
            return null;
        }
        if (image == null || redraw) {
            image = new BufferedImage(canvas.getWidth(), boxHeight, BufferedImage.TYPE_INT_RGB);
        }
        else {
            image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
        }
        int finalHeight = QueleaProperties.get().getNoticeBoxHeight();
        Graphics g = image.getGraphics();
        if (boxHeight == finalHeight - finalHeight / 20) {
            font = Utils.getDifferentSizeFont(font, Utils.getMaxFittingFontSize(g, font, noticeString, Integer.MAX_VALUE, finalHeight));
        }
        noticeWidth = g.getFontMetrics(font).stringWidth(noticeString.toString());
        int height = g.getFontMetrics(font).getHeight();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        if (boxHeight == finalHeight) {
            g.setFont(font);
            g.setColor(Color.WHITE);
            g.drawString(noticeString.toString(), stringPos, image.getHeight() / 2 + height / 4);
        }
        return image;
    }

    /**
     * Recalculate the notice string, called when any notices change.
     * @param decrement whether to decrement all the notices.
     */
    private void recalculateNoticeString(boolean decrement) {
        StringBuilder builder = new StringBuilder();
        if (decrement) {
            for (Notice notice : inUseNotices) {
                notice.decrementTimes();
            }
            for (int i = notices.size() - 1; i >= 0; i--) {
                if (notices.get(i) == null || notices.get(i).getTimes() <= 0) {
                    notices.remove(notices.get(i));
                }
            }
        }
        for (NoticesChangedListener listener : listeners) {
            listener.noticesUpdated(notices);
        }
        inUseNotices.clear();
        for (int i = 0; i < notices.size(); i++) {
            Notice notice = notices.get(i);
            inUseNotices.add(notice);
            builder.append(notice.getText());
            if (i != notices.size() - 1) {
                builder.append("    //    ");
            }
        }
        noticeString = builder.toString();
    }

    /**
     * Determine if we need to redraw notices (performance thing.)
     * @return true if we do, false otherwise.
     */
    public boolean getRedraw() {
        return redraw;
    }

    /**
     * Start the background notice thread.
     */
    private void start() {
        stringPos = canvas.getWidth();
        Runnable runnable = Utils.wrapAsLowPriority(new Runnable() {

            public void run() {
                while (true) {
                    redraw = false;
                    if (notices.isEmpty()) {
                        synchronized (lock) {
                            try {
                                lock.wait();
                            }
                            catch (InterruptedException ex) {
                                continue;
                            }
                        }
                    }
                    int finalHeight = QueleaProperties.get().getNoticeBoxHeight();
                    int speed = QueleaProperties.get().getNoticeBoxSpeed();

                    redraw = true;
                    while (boxHeight < finalHeight) {
                        boxHeight += finalHeight / 20;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                canvas.repaint();
                            }
                        });
                        Utils.sleep(DELAY);
                    }
                    redraw = false;
                    recalculateNoticeString(false);
                    while (!notices.isEmpty()) {
                        while (stringPos > -noticeWidth) {
                            stringPos -= speed;
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    canvas.repaint();
                                }
                            });
                            Utils.sleep(DELAY);
                        }
                        recalculateNoticeString(true);
                        stringPos = canvas.getWidth();
                    }
                    redraw = true;
                    while (boxHeight > 0) {
                        boxHeight -= finalHeight / 20;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                canvas.repaint();
                            }
                        });
                        Utils.sleep(DELAY);
                    }
                }
            }
        });


        new Thread(runnable).start();
    }
    private boolean first = true; //Yeah... bodge.

    /**
     * Add a given notice.
     * @param notice the notice to add.
     */
    public void addNotice(Notice notice) {
        notices.add(notice);
        if (first) {
            first = false;
            notice.setTimes(notice.getTimes() + 1);
        }
        if (notices.size() == 1) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * Remove a given notice.
     * @param notice notice to remove.
     */
    public void removeNotice(Notice notice) {
        notices.remove(notice);
    }

    /**
     * Get all the notices.
     * @return a list of all the notices.
     */
    public List<Notice> getNotices() {
        return new ArrayList<>(notices);
    }

    /**
     * Add a notice changed listener to this drawer.
     * @param listener the listener to add.
     */
    public void addNoticeChangedListener(NoticesChangedListener listener) {
        listeners.add(listener);
    }
}
