package org.quelea.mainwindow;

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

    /**
     * Create a new library panel.
     */
    public LibraryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Songs", new JPanel());
        tabbedPane.addTab("Bible", new JPanel());
        tabbedPane.addTab("Media", new JPanel());

        add(tabbedPane);
    }

}
