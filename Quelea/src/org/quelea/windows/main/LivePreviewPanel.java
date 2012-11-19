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

import java.awt.Canvas;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.audio.AudioPanel;
import org.quelea.windows.lyrics.DisplayCanvas;
import org.quelea.windows.lyrics.DisplayWindow;
import org.quelea.windows.lyrics.SelectLyricsPanel;
import org.quelea.windows.main.quickedit.QuickEditDialog;
import org.quelea.windows.main.widgets.CardPane;
import org.quelea.windows.video.VideoPanel;

/**
 * The common superclass of the live / preview panels used for selecting the
 * lyrics / picture.
 *
 * @author Michael
 */
public abstract class LivePreviewPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Set<DisplayCanvas> canvases = new HashSet<>();
    private final Set<DisplayWindow> windows = new HashSet<>();
    private Displayable displayable;
    private CardPane cardPanel = new CardPane();
    private static final String LYRICS_LABEL = "LYRICS";
    private static final String IMAGE_LABEL = "IMAGE";
    private static final String VIDEO_LABEL = "VIDEO";
    private static final String AUDIO_LABEL = "AUDIO";
    private static final String PRESENTATION_LABEL = "PPT";
    private String currentLabel;
    private SelectLyricsPanel lyricsPanel = new SelectLyricsPanel(this);
    private ImagePanel picturePanel = new ImagePanel(this);
    private PresentationPanel presentationPanel = new PresentationPanel(this);
    private VideoPanel videoPanel = new VideoPanel();
    private AudioPanel audioPanel = new AudioPanel();
    private QuickEditDialog quickEditDialog = new QuickEditDialog();
    /**
     * All the contained panels so they can be flipped through easily...
     */
    private final Set<ContainedPanel> containedSet = new HashSet<ContainedPanel>() {

        {
            this.add(lyricsPanel);
            this.add(picturePanel);
            this.add(videoPanel);
            this.add(audioPanel);
            this.add(presentationPanel);
        }
    };

    /**
     * Create the live preview panel, common superclass of live and preview
     * panels.
     */
    public LivePreviewPanel() {
        setCenter(cardPanel);
        cardPanel.add(lyricsPanel, LYRICS_LABEL);
        cardPanel.add(picturePanel, IMAGE_LABEL);
        cardPanel.add(videoPanel, VIDEO_LABEL);
        cardPanel.add(audioPanel, AUDIO_LABEL);
        cardPanel.add(presentationPanel, PRESENTATION_LABEL);
        registerLyricCanvas(lyricsPanel.getPreviewCanvas());
        cardPanel.show(LYRICS_LABEL);
        
        lyricsPanel.getLyricsList().setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent me) {
                if(me.isControlDown()) {
                    doQuickEdit(lyricsPanel.getLyricsList().getSelectionModel().getSelectedIndex());
                }
            }
        });
        
        lyricsPanel.getLyricsList().setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                if(ke.isControlDown() && ke.getCode() == KeyCode.Q) {
                    doQuickEdit(lyricsPanel.getLyricsList().getSelectionModel().getSelectedIndex());
                }
            }
        });

    }
    
    /**
     * Pass focus down to the current card panel pane.
     */
    @Override
    public void requestFocus() {
        getCurrentPane().requestFocus();
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
        if(displayable instanceof SongDisplayable) {
            SongDisplayable song = (SongDisplayable) displayable;
            quickEditDialog.setSongSection(song, index);
            quickEditDialog.show();
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
     * Get the container panel (the one using the cardlayout that flips between
     * the various available panels.
     *
     * @return the container panel.
     */
    public Node getCurrentPane() {
        return cardPanel.getCurrentPane();
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
        cardPanel.show(LYRICS_LABEL);
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
     * @param displayable the displayable to show.
     * @param index the index of the displayable to show, if relevant.
     */
    public void setDisplayable(Displayable displayable, int index) {
        this.displayable = displayable;
        presentationPanel.stopCurrent();
        audioPanel.clear();
        videoPanel.clear();
        lyricsPanel.clear();
        picturePanel.clear();
        presentationPanel.clear();
        if(PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.setDisplayable(null, 0);
        }
        if(displayable instanceof TextDisplayable) {
            lyricsPanel.showDisplayable((TextDisplayable) displayable, index);
            cardPanel.show(LYRICS_LABEL);
            currentLabel = LYRICS_LABEL;
        }
        else if(displayable instanceof ImageDisplayable) {
            picturePanel.showDisplayable((ImageDisplayable) displayable);
            cardPanel.show(IMAGE_LABEL);
            currentLabel = IMAGE_LABEL;
        }
        else if(displayable instanceof VideoDisplayable) {
            videoPanel.showDisplayable((VideoDisplayable) displayable);
            cardPanel.show(VIDEO_LABEL);
            currentLabel = VIDEO_LABEL;
        }
        else if(displayable instanceof AudioDisplayable) {
            audioPanel.showDisplayable((AudioDisplayable) displayable);
            cardPanel.show(AUDIO_LABEL);
            currentLabel = AUDIO_LABEL;
        }
        else if(displayable instanceof PresentationDisplayable) {
            presentationPanel.setDisplayable((PresentationDisplayable) displayable, index);
            cardPanel.show(PRESENTATION_LABEL);
            currentLabel = PRESENTATION_LABEL;
        }
        else if(displayable==null) {
//            LOGGER.log(Level.WARNING, "BUG: Called setDisplayable(null), should probably call clear() instead.", 
//                    new RuntimeException("BUG: Called setDisplayable(null), should probably call clear() instead.")); clear();
        }
        else {
            throw new RuntimeException("Displayable type not implemented: " + displayable.getClass());
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
    public final void registerLyricCanvas(final DisplayCanvas canvas) {
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
    public final void registerLyricWindow(final DisplayWindow window) {
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
        videoPanel.getMultimediaControlPanel().registerCanvas(canvas);
    }

    /**
     * Register a video canvas on this live preview panel.
     *
     * @param canvas the canvas to register.
     */
    public final void registerAudioCanvas(final Canvas canvas) {
        audioPanel.getMultimediaControlPanel().registerCanvas(canvas);
    }
    
    /**
     * Get the canvases registered to this panel.
     *
     * @return the canvases.
     */
    public Set<DisplayCanvas> getCanvases() {
        return canvases;
    }

    /**
     * Get the windows registered to this panel.
     *
     * @return the windows.
     */
    public Set<DisplayWindow> getWindows() {
        return windows;
    }
}
