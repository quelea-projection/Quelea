package org.quelea.notice;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricCanvas;

/**
 *
 * @author Michael
 */
public class NoticeManager {

    private static final int DELAY = 33;
    private static final Font FONT = new Font("Sans serif", 0, 20);
    public static final int BOX_HEIGHT = 30;
    private static final int SPEED = 8;
    private LyricCanvas canvas;
    private int boxHeight;
    private int stringPos;
    private List<Notice> notices;
    private List<Notice> inUseNotices;
    private String noticeString;
    private int noticeWidth;
    private final Object monitor = new Object();

    public NoticeManager(LyricCanvas canvas) {
        this.canvas = canvas;
        notices = Collections.synchronizedList(new ArrayList<Notice>());
        inUseNotices = Collections.synchronizedList(new ArrayList<Notice>());
        noticeString = "";
        start();
    }

    public BufferedImage getNoticeImage() {
        if (boxHeight == 0) {
            return null;
        }
        BufferedImage ret = new BufferedImage(canvas.getWidth(), boxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = ret.getGraphics();
        noticeWidth = g.getFontMetrics(FONT).stringWidth(noticeString.toString());
        int height = g.getFontMetrics(FONT).getHeight();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, ret.getWidth(), ret.getHeight());
        if (boxHeight == BOX_HEIGHT) {
            g.setFont(FONT);
            g.setColor(Color.WHITE);
            g.drawString(noticeString.toString(), stringPos, ret.getHeight() / 2+height/4);
        }
        return ret;
    }

    private void recalculateNoticeString() {
        StringBuilder builder = new StringBuilder();
        for (Notice notice : inUseNotices) {
            notice.decrementTimes();
        }
        for (int i = notices.size() - 1; i >= 0; i--) {
            if (notices.get(i).getTimes() < 0) {
                notices.remove(notices.get(i));
            }
        }
        inUseNotices.clear();
        for (int i = 0; i < notices.size(); i++) {
            Notice notice = notices.get(i);
            inUseNotices.add(notice);
            builder.append(notice.getStr());
            if (i != notices.size() - 1) {
                builder.append("    //    ");
            }
        }
        noticeString = builder.toString();
    }

    private void start() {
        stringPos = canvas.getWidth();
        Runnable runnable = Utils.wrapAsLowPriority(new Runnable() {

            public void run() {
                while (true) {
                    synchronized (monitor) {
                        try {
                            monitor.wait();
                        }
                        catch (InterruptedException ex) {
                            continue;
                        }
                    }

                    while (boxHeight < BOX_HEIGHT) {
                        boxHeight += 2;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                canvas.repaint();
                            }
                        });
                        Utils.sleep(DELAY);
                    }
                    recalculateNoticeString();
                    while (!notices.isEmpty()) {
                        while (stringPos > -noticeWidth) {
                            stringPos -= SPEED;
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    canvas.repaint();
                                }
                            });
                            Utils.sleep(DELAY);
                        }
                        recalculateNoticeString();
                        stringPos = canvas.getWidth();
                    }
                    while (boxHeight > 0) {
                        boxHeight -= 2;
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

    public void addNotice(Notice notice) {
        notices.add(notice);
        if (notices.size() == 1) {
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    public void removeNotice(Notice notice) {
        notices.remove(notice);
    }

    public List<Notice> getNotices() {
        return new ArrayList<>(notices);
    }
}
