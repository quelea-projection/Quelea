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
package org.quelea.windows.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.Theme;
import org.quelea.VideoBackground;
import org.quelea.notice.NoticeDrawer;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.video.RemotePlayer;
import org.quelea.video.RemotePlayerFactory;

/**
 * A canvas that's used to display lyrics with a particular background.
 *
 * Technically speaking, this canvas only ever displays the background, it won't
 * display any lyrics or other foreground text. This implementation however will
 * overlay a separate window, kept synchronised with this canvas, that can be
 * used to display the lyrics.
 *
 * This adds to complexity, but enables us to deal with the background and
 * foreground completely separately, and crucially is the approach that makes
 * video backgrounds possible, since we can tell something like VLC to render
 * natively to this canvas maintaining full hardware acceleration.
 *
 * @author Michael
 */
public class LyricCanvas extends Canvas {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private LyricCanvasData data;
    private OverlayLyricWindow window;
    private RemotePlayer vidPlayer;

    /**
     * Create the lyric canvas.
     *
     * @param showBorder true if a border should be shown around any text on the
     * overlayed canvas, false otherwise.
     * @param stageView true if this canvas is a stage view, false otherwise.
     */
    public LyricCanvas(boolean showBorder, boolean stageView) {
        setMinimumSize(new Dimension(20, 20));
        setBackground(Color.BLACK);
        data = new LyricCanvasData(stageView);
        addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                    vidPlayer = RemotePlayerFactory.getEmbeddedRemotePlayer(LyricCanvas.this);
                    if(vidPlayer == null) {
                        LOGGER.log(Level.WARNING, "Null video player, there was probably an error setting up video.");
                    }
                    removeHierarchyListener(this);
                }
            }
        });
        window = new OverlayLyricWindow(this, data);
        window.setVisible(true);
    }

    /**
     * Get the top lyric canvas that's overlayed on this canvas to draw the
     * text.
     *
     * @return the top lyric (overlay) canvas.
     */
    private TopLyricCanvas getTopCanvas() {
        return window.getCanvas();
    }

    /**
     * Update the state (visibility, position) of the overlay. This is done
     * automatically where possible, but sometimes in cases where the right
     * event doesn't get fired this method needs to be called manually.
     */
    public void updateOverlayState() {
        window.updateState(this);
    }

    /**
     * Determine if this canvas is part of a stage view.
     *
     * @return true if its a stage view, false otherwise.
     */
    public boolean isStageView() {
        return data.isStageView();
    }

    /**
     * Override so we don't clear the canvas when we update - stops flickering.
     *
     * @param g the graphics to draw with.
     */
    @Override
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Paint the background image and the lyrics onto the canvas.
     *
     * @param g the graphics used for painting.
     */
    @Override
    public void paint(Graphics g) {
        if(g == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        if(data.isBlacked() || data.getTheme() == null) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        else {
            if(data.isStageView()) {
                g2d.setColor(QueleaProperties.get().getStageBackgroundColor());
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            else {
                g2d.drawImage(data.getTheme().getBackground().getImage(getWidth(), getHeight(), Integer.toString(getWidth())), 0, 0, null);
            }
        }
        g.dispose();
    }

    /**
     * Toggle the clearing of this canvas - still leave the background image in
     * place but remove all the text.
     */
    public void toggleClear() {
        data.toggleCleared();
        getTopCanvas().repaint();
        repaint();
    }

    /**
     * Determine whether this canvas is cleared.
     *
     * @return true if the canvas is cleared, false otherwise.
     */
    public boolean isCleared() {
        return data.isCleared();
    }

    /**
     * Toggle the blacking of this canvas - remove the text and background image
     * (if any) just displaying a black screen.
     */
    public void toggleBlack() {
        data.toggleBlacked();
        if(getTheme().getBackground() instanceof VideoBackground) {
            if(data.isBlacked()) {
                if(vidPlayer != null) {
                    vidPlayer.stop();
                }
            }
            else {
                if(vidPlayer != null) {
                    vidPlayer.play();
                }
            }
        }
        getTopCanvas().repaint();
        repaint();
    }

    /**
     * Determine whether this canvas is blacked.
     *
     * @return true if the canvas is blacked, false otherwise.
     */
    public boolean isBlacked() {
        return data.isBlacked();
    }

    /**
     * Set the theme of this canvas.
     *
     * @param theme the theme to place on the canvas.
     */
    public void setTheme(Theme theme) {
        Theme t1 = theme == null ? Theme.DEFAULT_THEME : theme;
        Theme t2 = data.getTheme() == null ? Theme.DEFAULT_THEME : data.getTheme();
        if(!t2.equals(t1)) {
            data.setTheme(t1);
            paint(getGraphics());
            getTopCanvas().repaint();
            if(data.getTheme().getBackground() instanceof VideoBackground && isShowing()) {
                VideoBackground background = (VideoBackground) data.getTheme().getBackground();
                if(background != null && !background.getVideoLocation().trim().isEmpty()) {
                    vidPlayer.loadLoop(background.getVideoFile().getAbsolutePath(), this);
                    vidPlayer.setMute(true);
                    vidPlayer.play();
                }
            }
            else if(vidPlayer != null) {
                vidPlayer.stop();
            }
        }
    }

    /**
     * Get the theme currently in use on the canvas.
     *
     * @return the current theme
     */
    public Theme getTheme() {
        return data.getTheme();
    }

    /**
     * Erase all text on the overlayed canvas.
     */
    public void eraseText() {
        getTopCanvas().eraseText();
    }

    /**
     * Set the text on the overlayed canvas.
     *
     * @param text the main text / lyrics, one line per array entry, to appear
     * on the display.
     * @param smallText the small text to set at the bottom right of the screen,
     * one line per array entry.
     */
    public void setText(String[] text, String[] smallText) {
        getTopCanvas().setText(text, smallText);
    }

    /**
     * Set whether the first character of each line should be forced to be a
     * capital.
     *
     * @param capitalise true if it should be capitalised, false otherwise.
     */
    public void setCapitaliseFirst(boolean capitalise) {
        getTopCanvas().setCapitaliseFirst(capitalise);
    }

    /**
     * Get the notice drawer on the overlayed canvas.
     *
     * @return the notice drawer.
     */
    public NoticeDrawer getNoticeDrawer() {
        return getTopCanvas().getNoticeDrawer();
    }
}
