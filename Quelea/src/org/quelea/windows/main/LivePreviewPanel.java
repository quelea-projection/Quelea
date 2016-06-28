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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.image.ImagePanel;
import org.quelea.windows.imagegroup.ImageGroupPanel;
import org.quelea.windows.lyrics.SelectLyricsPanel;
import org.quelea.windows.main.quickedit.QuickEditDialog;
import org.quelea.windows.main.widgets.CardPane;
import org.quelea.windows.multimedia.MultimediaPanel;
import org.quelea.windows.pdf.PdfPanel;
import org.quelea.windows.timer.TimerPanel;
import org.quelea.windows.presentation.PresentationPanel;
import org.quelea.windows.web.WebPanel;

/**
 * The common superclass of the live / preview panels used for selecting the
 * lyrics / picture.
 * <p/>
 * @author Michael
 */
public abstract class LivePreviewPanel extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Set<DisplayStage> windows = new HashSet<>();
    private Displayable displayable;
    private final CardPane<AbstractPanel> cardPanel = new CardPane<>();
    private static final String LYRICS_LABEL = "LYRICS";
    private static final String IMAGE_LABEL = "IMAGE";
    private static final String VIDEO_LABEL = "VIDEO";
    private static final String TIMER_LABEL = "TIMER";
    private static final String AUDIO_LABEL = "AUDIO";
    private static final String PRESENTATION_LABEL = "PPT";
    private static final String PDF_LABEL = "PDF";
    private static final String WEB_LABEL = "WEB";
    private static final String IMAGE_GROUP_LABEL = "IMAGE_GROUP";
    private String currentLabel;
    private SelectLyricsPanel lyricsPanel = new SelectLyricsPanel(this);
    private final ImagePanel imagePanel = new ImagePanel();
    private final PresentationPanel presentationPanel = new PresentationPanel(this);
    private final PdfPanel pdfPanel = new PdfPanel(this);
    private final MultimediaPanel videoPanel = new MultimediaPanel();
    private final MultimediaPanel audioPanel = new MultimediaPanel();
    private final TimerPanel timerPanel = new TimerPanel();
    private final WebPanel webPanel = new WebPanel();
    private final ImageGroupPanel imageGroupPanel = new ImageGroupPanel(this);
    private final QuickEditDialog quickEditDialog = new QuickEditDialog();

    /**
     * Create the live preview panel, common superclass of live and preview
     * panels.
     */
    public LivePreviewPanel() {
        setCenter(cardPanel);
        cardPanel.add(lyricsPanel, LYRICS_LABEL);
        cardPanel.add(imagePanel, IMAGE_LABEL);
        cardPanel.add(videoPanel, VIDEO_LABEL);
        cardPanel.add(timerPanel, TIMER_LABEL);
        cardPanel.add(audioPanel, AUDIO_LABEL);
        cardPanel.add(presentationPanel, PRESENTATION_LABEL);
        cardPanel.add(pdfPanel, PDF_LABEL);
        cardPanel.add(webPanel, WEB_LABEL);
        cardPanel.add(imageGroupPanel, IMAGE_GROUP_LABEL);
        cardPanel.show(LYRICS_LABEL);
        lyricsPanel.getLyricsList().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.isControlDown() || me.isShiftDown()) {
                    doQuickEdit(lyricsPanel.getLyricsList().getQuickEditIndex());
                }
            }
        });

        lyricsPanel.getLyricsList().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.isControlDown() && ke.getCode() == KeyCode.Q) {
                    doQuickEdit(lyricsPanel.getCurrentIndex());
                }
            }
        });
        presentationPanel.buildLoopTimeline();
        setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
            }
        });
        setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) instanceof SongDisplayable) {
                    final SongDisplayable displayable = (SongDisplayable) event.getDragboard().getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT);
                    if (displayable != null) {
                        setDisplayable(displayable, 0);
                    }
                }
                event.consume();
            }
        });
    }

    public void selectFirstLyric() {
        if (LYRICS_LABEL.equals(currentLabel)) {
            lyricsPanel.selectFirst();
        } else if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.selectFirst();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.selectFirst();
        }
    }

    public void selectLastLyric() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.selectLast();
        } else if (LYRICS_LABEL.equals(currentLabel)) {
            lyricsPanel.selectLast();
        } else if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.selectLast();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.selectLast();
        }
    }

    /**
     * Get the presentation panel on this live / preview panel.
     * <p/>
     * @return the presentation panel.
     */
    public PresentationPanel getPresentationPanel() {
        return presentationPanel;
    }
    
    /**
     * Get the PDF panel on this live / preview panel.
     * <p/>
     * @return the PDF panel.
     */
    public PdfPanel getPdfPanel() {
        return pdfPanel;
    }
    
    /**
     * Get the image group panel on this live / preview panel.
     * <p/>
     * @return the image group panel.
     */
    public ImageGroupPanel getImageGroupPanel() {
        return imageGroupPanel;
    }

    /**
     * Perform a quick edit on the given index.
     * <p/>
     * @param index the index on which to perform the quick edit.
     */
    public void doQuickEdit(int index) {
        if (displayable instanceof SongDisplayable) {
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
     * <p/>
     * @return the container panel.
     */
    public Node getCurrentPane() {
        return cardPanel.getCurrentPane();
    }

    /**
     * Clear all the contained panels to a null displayable.
     */
    public void removeDisplayable() {
        displayable = null;
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.showDisplayable(null, 0);
        }
        if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.showDisplayable(null, 0);
        }
        if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.showDisplayable(null, 0);
        }
        for (Node panel : cardPanel) {
            if (panel instanceof ContainedPanel) {
                ((ContainedPanel) panel).removeCurrentDisplayable();
            } else {
                LOGGER.log(Level.WARNING, "Panel was {0} which isn't a ContainedPanel... can't clear it.", panel.getClass());
            }
        }
        if (currentLabel == null || !currentLabel.equals(LYRICS_LABEL)) {
            cardPanel.show(LYRICS_LABEL);
            currentLabel = LYRICS_LABEL;
        }
    }

    /**
     * Get the currently selected displayable index. Only suitable for
     * powerpoint / PDF / image group / lyrics panels.
     * <p/>
     * @return the currently selected displayable index.
     */
    public int getIndex() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            return presentationPanel.getIndex();
        } else if (PDF_LABEL.equals(currentLabel)) {
            return pdfPanel.getIndex();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            return imageGroupPanel.getIndex();
        } else {
            return lyricsPanel.getIndex();
        }
    }
    
    /**
     * Get the length of the current displayable. Only suitable for
     * powerpoint / PDF / image group / lyrics panels.
     * <p/>
     * @return the currently selected displayable index.
     */
    
    public int getLenght() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            return presentationPanel.getSlideCount();
        } else if (PDF_LABEL.equals(currentLabel)) {
            return pdfPanel.getSlideCount();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            return imageGroupPanel.getSlideCount();
        } else {
            return lyricsPanel.getSlideCount();
        }
    }

    /**
     * Advances currently selected displayable index. Only suitable for
     * powerpoint / lyrics panels.
     * <p/>
     */
    public void advance() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.advance();
        } else if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.advance();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.advance();
        } else if (LYRICS_LABEL.equals(currentLabel)) {
            lyricsPanel.advance();
        } else if (IMAGE_LABEL.equals(currentLabel)) {
            imagePanel.advance();
        } else if (VIDEO_LABEL.equals(currentLabel)) {
            videoPanel.advance();
        } else if (TIMER_LABEL.equals(currentLabel)) {
            timerPanel.advance();
        } else if (WEB_LABEL.equals(currentLabel)) {
            webPanel.advance();
        }
    }

    /**
     * Moves to previous slide in currently selected displayable index. Only
     * suitable for powerpoint / lyrics panels.
     * <p/>
     */
    public void previous() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.previous();
        } else if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.previous();
        } else if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.previous();
        } else if (LYRICS_LABEL.equals(currentLabel)) {
            lyricsPanel.previous();
        } else if (IMAGE_LABEL.equals(currentLabel)) {
            imagePanel.previous();
        } else if (VIDEO_LABEL.equals(currentLabel)) {
            videoPanel.previous();
        } else if (TIMER_LABEL.equals(currentLabel)) {
            timerPanel.previous();
        } else if (WEB_LABEL.equals(currentLabel)) {
            webPanel.previous();
        }
    }

    /**
     * Get the select lyrics panel on this panel.
     * <p/>
     * @return the select lyrics panel.
     */
    public SelectLyricsPanel getLyricsPanel() {
        return lyricsPanel;
    }

    /**
     * Set the displayable shown on this panel.
     * <p/>
     * @param displayable the displayable to show.
     * @param index the index of the displayable to show, if relevant.
     */
    public void setDisplayable(final Displayable displayable, final int index) {
        Utils.checkFXThread();
        if (!(this.displayable instanceof TextDisplayable && displayable instanceof TextDisplayable)) {
            lyricsPanel.removeCurrentDisplayable();
        }
        this.displayable = displayable;
        presentationPanel.stopCurrent();
        pdfPanel.stopCurrent();
        imageGroupPanel.stopCurrent();
        audioPanel.removeCurrentDisplayable();
        videoPanel.removeCurrentDisplayable();
        timerPanel.removeCurrentDisplayable();
        imagePanel.removeCurrentDisplayable();
        presentationPanel.removeCurrentDisplayable();
        pdfPanel.removeCurrentDisplayable();
        webPanel.removeCurrentDisplayable();
        imageGroupPanel.removeCurrentDisplayable();
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            presentationPanel.showDisplayable(null, 0);
        }
        if (PDF_LABEL.equals(currentLabel)) {
            pdfPanel.showDisplayable(null, 0);
        }
        if (IMAGE_GROUP_LABEL.equals(currentLabel)) {
            imageGroupPanel.showDisplayable(null, 0);
        }
        if (displayable instanceof TextDisplayable) {
            lyricsPanel.showDisplayable((TextDisplayable) displayable, index);
            cardPanel.show(LYRICS_LABEL);
            currentLabel = LYRICS_LABEL;
        } else if (displayable instanceof ImageDisplayable) {
            imagePanel.showDisplayable((ImageDisplayable) displayable);
            cardPanel.show(IMAGE_LABEL);
            currentLabel = IMAGE_LABEL;
        } else if (displayable instanceof VideoDisplayable) {
            videoPanel.showDisplayable((MultimediaDisplayable) displayable);
            cardPanel.show(VIDEO_LABEL);
            currentLabel = VIDEO_LABEL;
            if (QueleaProperties.get().getAutoPlayVideo() && LivePreviewPanel.this instanceof LivePanel) {
                videoPanel.play();
            }
        } else if (displayable instanceof TimerDisplayable) {
            timerPanel.showDisplayable((MultimediaDisplayable) displayable);
            cardPanel.show(TIMER_LABEL);
            currentLabel = TIMER_LABEL;
            if (LivePreviewPanel.this instanceof LivePanel) {
                timerPanel.play();
            }
        } else if (displayable instanceof AudioDisplayable) {
            audioPanel.showDisplayable((MultimediaDisplayable) displayable);
            cardPanel.show(AUDIO_LABEL);
            currentLabel = AUDIO_LABEL;
        } else if (displayable instanceof PresentationDisplayable) {
            presentationPanel.showDisplayable((PresentationDisplayable) displayable, index);
            cardPanel.show(PRESENTATION_LABEL);
            currentLabel = PRESENTATION_LABEL;
        } else if (displayable instanceof PdfDisplayable) {
            pdfPanel.showDisplayable((PdfDisplayable) displayable, index);
            cardPanel.show(PDF_LABEL);
            currentLabel = PDF_LABEL;
        } else if (displayable instanceof WebDisplayable) {
            webPanel.showDisplayable((WebDisplayable) displayable);
            cardPanel.show(WEB_LABEL);
            currentLabel = WEB_LABEL;
            webPanel.setText();
        } else if (displayable instanceof ImageGroupDisplayable) {
            imageGroupPanel.showDisplayable((ImageGroupDisplayable) displayable, index);
            cardPanel.show(IMAGE_GROUP_LABEL);
            currentLabel = IMAGE_GROUP_LABEL;
        } else if (displayable == null) {
//            LOGGER.log(Level.WARNING, "BUG: Called showDisplayable(null), should probably call clear() instead.", 
//                    new RuntimeException("BUG: Called showDisplayable(null), should probably call clear() instead.")); clear();
        } else {
            throw new RuntimeException("Displayable type not implemented: " + displayable.getClass());
        }
    }

    /**
     * Refresh the current content of this panel, if any exists.
     */
    public void refresh() {
        if (getDisplayable() != null) {
            setDisplayable(getDisplayable(), getIndex());
        }
    }

    /**
     * Get the displayable currently being displayed, or null if there isn't
     * one.
     * <p/>
     * @return the current displayable.
     */
    public Displayable getDisplayable() {
        return displayable;
    }

    /**
     * Register a display canvas with this lyrics panel.
     * <p/>
     * @param canvas the canvas to register.
     */
    public final void registerDisplayCanvas(final DisplayCanvas canvas) {
        if (canvas == null) {
            return;
        }
        for (AbstractPanel panel : cardPanel.getPanels()) {
            panel.registerDisplayCanvas(canvas);
        }
    }

    /**
     * Register a display window with this lyrics panel.
     * <p/>
     * @param window the window to register.
     */
    public final void registerDisplayWindow(final DisplayStage window) {
        if (window == null) {
            return;
        }
        windows.add(window);
    }

    /**
     * Get the canvases registered to this panel.
     * <p/>
     * @return the canvases.
     */
    public Set<DisplayCanvas> getCanvases() {
        return ((AbstractPanel) getCurrentPane()).getCanvases();
    }

    /**
     * Get the windows registered to this panel.
     * <p/>
     * @return the windows.
     */
    public Set<DisplayStage> getWindows() {
        return windows;
    }

    /**
     * Get the timer panel.
     * <p/>
     * @return the timer panel.
     */
    public TimerPanel getTimerPanel() {
        return timerPanel;
    }
    
    /**
     * Get the web panel.
     * <p/>
     * @return the web panel.
     */
    public WebPanel getWebPanel() {
        return webPanel;
    }
}
