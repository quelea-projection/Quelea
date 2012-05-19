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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.quelea.displayable.*;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.main.quickedit.QuickEditDialog;
import org.quelea.windows.video.VideoPanel;

/**
 * The common superclass of the live / preview panels used for selecting the
 * lyrics / picture.
 *
 * @author Michael
 */
public abstract class LivePreviewPanel extends JPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Set<LyricCanvas> canvases = new HashSet<>();
    private final Set<LyricWindow> windows = new HashSet<>();
    private Displayable displayable;
    private JPanel cardPanel = new JPanel(new CardLayout());
    private static final String LYRICS_LABEL = "LYRICS";
    private static final String IMAGE_LABEL = "IMAGE";
    private static final String VIDEO_LABEL = "VIDEO";
    private static final String PRESENTATION_LABEL = "PPT";
    private static final String AUDIO_LABEL = "AUDIO";
    private String currentLabel;
    private SelectLyricsPanel lyricsPanel = new SelectLyricsPanel(this);
    private ImagePanel picturePanel = new ImagePanel(this);
    private PresentationPanel presentationPanel = new PresentationPanel(this);
    private VideoPanel videoPanel = new VideoPanel();
    private AudioPanel audioPanel = new AudioPanel(this);
    private QuickEditDialog quickEditDialog = new QuickEditDialog();
    /**
     * All the contained panels so they can be flipped through easily...
     */
    private final Set<ContainedPanel> containedSet = new HashSet<ContainedPanel>() {

        {
            this.add(lyricsPanel);
            this.add(picturePanel);
            this.add(videoPanel);
            this.add(presentationPanel);
        }
    };
    

    /**
     * Create the live preview panel, common superclass of live and preview
     * panels.
     */
    public LivePreviewPanel() {
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        cardPanel.add(lyricsPanel, LYRICS_LABEL);
        cardPanel.add(picturePanel, IMAGE_LABEL);
        cardPanel.add(videoPanel, VIDEO_LABEL);
        cardPanel.add(presentationPanel, PRESENTATION_LABEL);
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);

        lyricsPanel.getLyricsList().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if(me.isControlDown()) {
                    int index = lyricsPanel.getLyricsList().locationToIndex(me.getPoint());
                    doQuickEdit(index);
                }
            }
        });
        lyricsPanel.getLyricsList().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_Q) {
                    doQuickEdit(lyricsPanel.getLyricsList().getSelectedIndex());
                }
            }
        });
    }

    /**
     * Get the presentation panel on this live / preview panel.
     * @return the presentation panel.
     */
    protected PresentationPanel getPresentationPanel() {
        return presentationPanel;
    }
    
    /**
     * Perform a quick edit on the given index.
     *
     * @param index the index on which to perform the quick edit.
     */
    public void doQuickEdit(int index) {
        if(displayable instanceof Song) {
            Song song = (Song) displayable;
            quickEditDialog.setLocationRelativeTo(quickEditDialog.getParent());
            quickEditDialog.setSongSection(song, index);
            quickEditDialog.setVisible(true);
            setDisplayable(song, getIndex());
        }
    }

    /**
     * Update the one line mode for the lyrics panel from the properties file.
     */
    public void updateOneLineMode() {
        lyricsPanel.setOneLineMode(QueleaProperties.get().getOneLineMode());
    }

    /**
     * Add a key listener to this panel and all the contained panels.
     *
     * @param l the listener to add.
     */
    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        for(ContainedPanel panel : containedSet) {
            panel.addKeyListener(l);
        }
    }

    /**
     * Get the container panel (the one using the cardlayout that flips between
     * the various available panels.
     *
     * @return the container panel.
     */
    public JPanel getContainerPanel() {
        return cardPanel;
    }

    /**
     * Focus on this panel.
     */
    public void focus() {
        getCurrentPanel().focus();
    }

    /**
     * Clear all the contained panels to a null displayable.
     */
    public void clear() {
        displayable = null;
        if(PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.setDisplayable(null, 0);
        }
        for(ContainedPanel panel : containedSet) {
            panel.clear();
        }
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);
    }

    /**
     * Get the currently selected displayable index. Only suitable for
     * powerpoint / lyrics panels.
     *
     * @return the currently selected displayable index.
     */
    public int getIndex() {
        if(PRESENTATION_LABEL.equals(currentLabel)) {
            return presentationPanel.getIndex();
        }
        else {
            return lyricsPanel.getIndex();
        }
    }

    /**
     * Get the select lyrics panel on this panel.
     *
     * @return the select lyrics panel.
     */
    public SelectLyricsPanel getLyricsPanel() {
        return lyricsPanel;
    }

    /**
     * Set the displayable shown on this panel.
     *
     * @param d the displayable to show.
     * @param index the index of the displayable to show, if relevant.
     */
    public void setDisplayable(Displayable d, int index) {
        this.displayable = d;
        presentationPanel.stopCurrent();
        if(VIDEO_LABEL.equals(currentLabel)) {
            videoPanel.getVideoControlPanel().stopVideo();
        }
        if(PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.setDisplayable(null, 0);
        }
        if(d instanceof TextDisplayable) {
            lyricsPanel.showDisplayable((TextDisplayable) d, index);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);
            currentLabel = LYRICS_LABEL;
        }
        else if(d instanceof ImageDisplayable) {
            picturePanel.showDisplayable((ImageDisplayable) d);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, IMAGE_LABEL);
            currentLabel = IMAGE_LABEL;
        }
        else if(d instanceof VideoDisplayable) {
            videoPanel.showDisplayable((VideoDisplayable) d);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, VIDEO_LABEL);
            videoPanel.repaint();
            currentLabel = VIDEO_LABEL;
        }
        else if(d instanceof PresentationDisplayable) {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, PRESENTATION_LABEL);
            presentationPanel.setDisplayable((PresentationDisplayable) d, index);
            currentLabel = PRESENTATION_LABEL;
        }
        else if(d instanceof AudioDisplayable) {
            audioPanel.showDisplayable((AudioDisplayable) d);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, AUDIO_LABEL);
            currentLabel = AUDIO_LABEL;
        }
        else if(d==null) {
            LOGGER.log(Level.WARNING, "BUG: Called setDisplayable(null), should probably call clear() instead.");
            clear();
        }
        else {
            throw new RuntimeException("Displayable type not implemented: " + d.getClass());
        }
    }

    /**
     * Refresh the current content of this panel, if any exists.
     */
    public void refresh() {
        if(getDisplayable() != null) {
            setDisplayable(getDisplayable(), getIndex());
        }
    }

    /**
     * Set video properties - used to copy video properties from one panel to
     * another seamlessly. At present buggy, so commented out.
     *
     * @param other the panel to copy properties from.
     */
    public void setVideoProperties(LivePreviewPanel other) {
//        videoPanel.getVideoControlPanel().playVideo();
//        videoPanel.getVideoControlPanel().pauseVideo();
//        videoPanel.getVideoControlPanel().setTime(other.videoPanel.getVideoControlPanel().getTime());
    }

    /**
     * Pause the current video panel's video.
     */
    public void pauseVideo() {
        videoPanel.getVideoControlPanel().pauseVideo();
    }

    /**
     * Get the displayable currently being displayed, or null if there isn't
     * one.
     *
     * @return the current displayable.
     */
    public Displayable getDisplayable() {
        return displayable;
    }

    /**
     * Register a lyric canvas with this lyrics panel.
     *
     * @param canvas the canvas to register.
     */
    public final void registerLyricCanvas(final LyricCanvas canvas) {
        if(canvas == null) {
            return;
        }
        canvases.add(canvas);
    }

    /**
     * Register a lyric window with this lyrics panel.
     *
     * @param window the window to register.
     */
    public final void registerLyricWindow(final LyricWindow window) {
        if(window == null) {
            return;
        }
        windows.add(window);
    }

    /**
     * Register a video canvas on this live preview panel.
     *
     * @param canvas the canvas to register.
     */
    public final void registerVideoCanvas(final Canvas canvas) {
        videoPanel.getVideoControlPanel().registerCanvas(canvas);
    }

    /**
     * Get the canvases registered to this panel.
     *
     * @return the canvases.
     */
    public Set<LyricCanvas> getCanvases() {
        return canvases;
    }

    /**
     * Get the windows registered to this panel.
     *
     * @return the windows.
     */
    public Set<LyricWindow> getWindows() {
        return windows;
    }

    /**
     * Get the current panel being shown in the card layout.
     *
     * @return the current panel.
     */
    private ContainedPanel getCurrentPanel() {
        Component[] components = cardPanel.getComponents();
        for(Component c : components) {
            if(c.isVisible()) {
                return (ContainedPanel) c;
            }
        }
        return null;
    }

    public VideoPanel getVideoPanel() {
        return videoPanel;
    }
    
}
