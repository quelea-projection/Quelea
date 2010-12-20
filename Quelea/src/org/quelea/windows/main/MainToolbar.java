package org.quelea.windows.main;

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.quelea.Utils;

/**
 * The toolbar displayed on the main window.
 * @author Michael
 */
public class MainToolbar extends JToolBar {

    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;

    /**
     * Initialise the icons and their actions on the main toolbar.
     */
    public MainToolbar() {
        setFloatable(false);
        newButton = new JButton(Utils.getImageIcon("icons/filenew.png"));
        newButton.setToolTipText("New schedule");
        add(newButton);
        openButton = new JButton(Utils.getImageIcon("icons/fileopen.png"));
        openButton.setToolTipText("Open schedule");
        add(openButton);
        saveButton = new JButton(Utils.getImageIcon("icons/filesave.png"));
        saveButton.setToolTipText("Save schedule");
        add(saveButton);
    }

    public JButton getNewButton() {
        return newButton;
    }

    public JButton getOpenButton() {
        return openButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

}
