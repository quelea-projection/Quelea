package org.quelea.windows.main;

import java.awt.BorderLayout;
import org.quelea.displayable.VideoDisplayable;

/**
 * A panel used in the live / preview panels for displaying videos.
 * @author Michael
 */
public class VideoPanel extends ContainedPanel {

    private VideoControlPanel controlPanel = new VideoControlPanel();

    /**
     * Create a new image panel.
     * @param container the container this panel is contained within.
     */
    public VideoPanel() {
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.CENTER);
    }

    @Override
    public void focus() {
        //TODO: Something probably
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        //Nada. Can't clear a video.
    }

    public VideoControlPanel getVideoControlPanel() {
        return controlPanel;
    }

    /**
     * Show a given video displayable on the panel.
     * @param displayable the video displayable.
     */
    public void showDisplayable(VideoDisplayable displayable) {
        controlPanel.loadVideo(displayable.getFile().getAbsolutePath());
    }

}
