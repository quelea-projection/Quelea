package org.quelea.windows.library;

import javax.swing.*;

/**
 * The panel that's used to display the library of media (pictures, video) and songs. Items can be selected from here
 * and added to the order of service.
 * @author Michael
 */
public class LibraryPanel extends JPanel {

    private final LibrarySongPanel songPanel;
    private final LibraryBiblePanel biblePanel;
    private final LibraryImagePanel imagePanel;
    private final LibraryVideoPanel videoPanel;

    /**
     * Create a new library panel.
     */
    public LibraryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();

        songPanel = new LibrarySongPanel();
        tabbedPane.addTab("Songs", songPanel);
        biblePanel = new LibraryBiblePanel();
        tabbedPane.addTab("Bible", biblePanel);
        imagePanel = new LibraryImagePanel();
        tabbedPane.addTab("Image", imagePanel);
        videoPanel = new LibraryVideoPanel();
        tabbedPane.addTab("Video", videoPanel);

        tabbedPane.setEnabledAt(2, false);
        tabbedPane.setEnabledAt(3, false);
        add(tabbedPane);

    }

    /**
     * Get the library song panel.
     * @return the library song panel.
     */
    public LibrarySongPanel getLibrarySongPanel() {
        return songPanel;
    }

    /**
     * Get the library bible panel.
     * @return the library bible panel.
     */
    public LibraryBiblePanel getBiblePanel() {
        return biblePanel;
    }

    /**
     * Get the library image panel.
     * @return the library image panel.
     */
    public LibraryImagePanel getImagePanel() {
        return imagePanel;
    }

    /**
     * Get the library video panel.
     * @return the library video panel.
     */
    public LibraryVideoPanel getVideoPanel() {
        return videoPanel;
    }

}
