package org.quelea.windows.main;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The panel that's used to display the library of media (pictures, video)
 * and songs. Items can be selected from here and added to the order of
 * service.
 * @author Michael
 */
public class LibraryPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private LibrarySongPanel songPanel;

    /**
     * Create a new library panel.
     */
    public LibraryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        tabbedPane = new JTabbedPane();

        songPanel = new LibrarySongPanel();
        tabbedPane.addTab("Songs", songPanel);
        tabbedPane.addTab("Bible", new JPanel());
        tabbedPane.addTab("Media", new JPanel());

        add(tabbedPane);
    }

    /**
     * Get the library song panel.
     * @return the library song panel.
     */
    public LibrarySongPanel getLibrarySongPanel() {
        return songPanel;
    }



}
