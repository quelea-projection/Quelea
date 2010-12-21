package org.quelea.windows.main;

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.quelea.Utils;

/**
 * The toolbar displayed on the main window.
 * @author Michael
 */
public class MainToolbar extends JToolBar {

    private final JButton newButton;
    private final JButton openButton;
    private final JButton saveButton;


    /**
     * Initialise the icons and their actions on the main toolbar.
     */
    public MainToolbar() {
        setFloatable(false);
        newButton = new JButton(Utils.getImageIcon("icons/filenew.png"));
        newButton.setToolTipText("New");
        add(newButton);
        openButton = new JButton(Utils.getImageIcon("icons/fileopen.png"));
        openButton.setToolTipText("Open...");
        add(openButton);
        saveButton = new JButton(Utils.getImageIcon("icons/filesave.png"));
        saveButton.setToolTipText("Save...");
        add(saveButton);
    }

    /**
     * Get the new button on the toolbar.
     * @return the new button.
     */
    public JButton getNewButton() {
        return newButton;
    }

    /**
     * Get the open button on the toolbar.
     * @return the open button.
     */
    public JButton getOpenButton() {
        return openButton;
    }

    /**
     * Get the save button on the toolbar.
     * @return the save button.
     */
    public JButton getSaveButton() {
        return saveButton;
    }

}
