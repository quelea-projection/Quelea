/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
