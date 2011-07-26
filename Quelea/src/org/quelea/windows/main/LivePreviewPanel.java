package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.ImageDisplayable;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.VideoDisplayable;

/**
 * The common superclass of the live / preview panels used for selecting the
 * lyrics / picture.
 * @author Michael
 */
public abstract class LivePreviewPanel extends JPanel {

    private final Set<LyricCanvas> canvases = new HashSet<LyricCanvas>();
    private final Set<LyricWindow> windows = new HashSet<LyricWindow>();
    private Displayable displayable;
    private JPanel cardPanel = new JPanel(new CardLayout());
    private static final String LYRICS_LABEL = "LYRICS";
    private static final String IMAGE_LABEL = "IMAGE";
    private static final String VIDEO_LABEL = "VIDEO";
    private static final String PRESENTATION_LABEL = "PPT";
    private String currentLabel;
    private SelectLyricsPanel lyricsPanel = new SelectLyricsPanel(this);
    private ImagePanel picturePanel = new ImagePanel(this);
    private PowerpointPanel powerpointPanel = new PowerpointPanel(this);
    private VideoPanel videoPanel = new VideoPanel();
    private final Set<ContainedPanel> containedSet = new HashSet<ContainedPanel>() {

        {
            this.add(lyricsPanel);
            this.add(picturePanel);
            this.add(videoPanel);
            this.add(powerpointPanel);
        }
    };

    public LivePreviewPanel() {
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        cardPanel.add(lyricsPanel, LYRICS_LABEL);
        cardPanel.add(picturePanel, IMAGE_LABEL);
        cardPanel.add(videoPanel, VIDEO_LABEL);
        cardPanel.add(powerpointPanel, PRESENTATION_LABEL);
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);
    }

    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        for (ContainedPanel panel : containedSet) {
            panel.addKeyListener(l);
        }
    }

    public JPanel getContainerPanel() {
        return cardPanel;
    }

    public void focus() {
        getCurrentPanel().focus();
    }

    public void clear() {
        displayable = null;
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            powerpointPanel.setDisplayable(null, 0);
        }
        for (ContainedPanel panel : containedSet) {
            panel.clear();
        }
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);
    }

    public int getIndex() {
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            return powerpointPanel.getIndex();
        }
        else {
            return lyricsPanel.getIndex();
        }
    }

    public void setDisplayable(Displayable d, int index) {
        this.displayable = d;
        if (VIDEO_LABEL.equals(currentLabel)) {
            videoPanel.getVideoControlPanel().stopVideo();
        }
        if (PRESENTATION_LABEL.equals(currentLabel)) {
            powerpointPanel.setDisplayable(null, 0);
        }
        if (d instanceof TextDisplayable) {
            lyricsPanel.showDisplayable((TextDisplayable) d, index);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, LYRICS_LABEL);
            currentLabel = LYRICS_LABEL;
        }
        else if (d instanceof ImageDisplayable) {
            picturePanel.showDisplayable((ImageDisplayable) d);
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, IMAGE_LABEL);
            currentLabel = IMAGE_LABEL;
        }
        else if (d instanceof VideoDisplayable) {
            videoPanel.showDisplayable((VideoDisplayable) d);
            for (LyricCanvas lc : videoPanel.getVideoControlPanel().getRegisteredCanvases()) {
                lc.setText(new String[]{});
            }
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, VIDEO_LABEL);
            videoPanel.repaint();
            currentLabel = VIDEO_LABEL;
        }
        else if (d instanceof PresentationDisplayable) {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, PRESENTATION_LABEL);
            powerpointPanel.setDisplayable((PresentationDisplayable) d, index);
            currentLabel = PRESENTATION_LABEL;
        }
        else {
            throw new RuntimeException("Displayable type not implemented: " + d.getClass());
        }
    }

    public void setVideoProperties(LivePreviewPanel other) {
//        videoPanel.getVideoControlPanel().playVideo();
//        videoPanel.getVideoControlPanel().pauseVideo();
//        videoPanel.getVideoControlPanel().setTime(other.videoPanel.getVideoControlPanel().getTime());
    }

    public void pauseVideo() {
        videoPanel.getVideoControlPanel().pauseVideo();
    }

    /**
     * Get the displayable currently being displayed, or null if there isn't
     * one.
     * @return the current displayable.
     */
    public Displayable getDisplayable() {
        return displayable;
    }

    /**
     * Register a lyric canvas with this lyrics panel.
     * @param canvas the canvas to register.
     */
    public final void registerLyricCanvas(final LyricCanvas canvas) {
        if (canvas == null) {
            return;
        }
        canvases.add(canvas);
    }

    /**
     * Register a lyric window with this lyrics panel.
     * @param window the window to register.
     */
    public final void registerLyricWindow(final LyricWindow window) {
        if (window == null) {
            return;
        }
        windows.add(window);
    }

    public final void registerVideoCanvas(final LyricCanvas canvas) {
        videoPanel.getVideoControlPanel().registerCanvas(canvas);
    }

    /**
     * Get the canvases registered to this panel.
     * @return the canvases.
     */
    public Set<LyricCanvas> getCanvases() {
        return canvases;
    }

    /**
     * Get the windows registered to this panel.
     * @return the windows.
     */
    public Set<LyricWindow> getWindows() {
        return windows;
    }

    /**
     * Get the current panel being shown in the card layout.
     * @return the current panel.
     */
    private ContainedPanel getCurrentPanel() {
        Component[] components = cardPanel.getComponents();
        for (Component c : components) {
            if (c.isVisible()) {
                return (ContainedPanel) c;
            }
        }
        return null;
    }
}
