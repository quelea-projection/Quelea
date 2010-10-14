package org.quelea.mainwindow;

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.quelea.Utils;

/**
 * The toolbar displayed on the main window.
 * @author Michael
 */
public class MainToolbar extends JToolBar {

    /**
     * Initialise the icons and their actions on the main toolbar.
     */
    public MainToolbar() {
        setFloatable(false);
        add(new JButton(Utils.getImageIcon("icons/filenew.png")) {
            {
                setToolTipText("New schedule");
            }
        });
        add(new JButton(Utils.getImageIcon("icons/fileopen.png")) {
            {
                setToolTipText("Open schedule");
            }
        });
        add(new JButton(Utils.getImageIcon("icons/filesave.png")) {
            {
                setToolTipText("Save schedule");
            }
        });
    }

}
