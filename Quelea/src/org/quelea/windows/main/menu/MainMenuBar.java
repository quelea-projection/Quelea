package org.quelea.windows.main.menu;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * The main menu bar that's displayed at the top of the main Quelea window.
 * @author Michael
 */
public class MainMenuBar extends JMenuBar {

    private final FileMenu fileMenu;
    private final DatabaseMenu databaseMenu;
    private final ToolsMenu toolsMenu;
    private final HelpMenu helpMenu;

    /**
     * Create a new main menu bar and initialise all the components.
     */
    public MainMenuBar() {
        add(fileMenu = new FileMenu());
        add(databaseMenu = new DatabaseMenu());
        add(toolsMenu = new ToolsMenu());
        add(helpMenu = new HelpMenu());
        addShortcuts();
    }

    /**
     * Add the shortcuts to the menus.
     */
    private void addShortcuts() {
        fileMenu.setMnemonic(KeyEvent.VK_F);
        databaseMenu.setMnemonic(KeyEvent.VK_D);
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        helpMenu.setMnemonic(KeyEvent.VK_H);
    }

    /**
     * Get the database menu.
     * @return the database menu.
     */
    public DatabaseMenu getDatabaseMenu() {
        return databaseMenu;
    }

    /**
     * Get the file menu.
     * @return the file menu.
     */
    public FileMenu getFileMenu() {
        return fileMenu;
    }

    /**
     * Get the help menu.
     * @return the help menu.
     */
    @Override
    public HelpMenu getHelpMenu() {
        return helpMenu;
    }

    /**
     * Get the tools menu.
     * @return the tools menu.
     */
    public ToolsMenu getToolsMenu() {
        return toolsMenu;
    }

}
